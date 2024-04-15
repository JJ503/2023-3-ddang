package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.application.MessageService;
import com.ddang.ddang.chat.application.dto.CreateMessageDto;
import com.ddang.ddang.chat.application.event.MessageNotificationEvent;
import com.ddang.ddang.chat.application.event.UpdateReadMessageLogEvent;
import com.ddang.ddang.chat.domain.Message;
import com.ddang.ddang.chat.domain.WebSocketChatSessions;
import com.ddang.ddang.chat.handler.dto.ChatMessageDataDto;
import com.ddang.ddang.chat.handler.dto.HandleMessageResponse;
import com.ddang.ddang.chat.handler.dto.MessageDto;
import com.ddang.ddang.chat.handler.dto.SendMessageStatus;
import com.ddang.ddang.chat.presentation.dto.request.CreateMessageRequest;
import com.ddang.ddang.websocket.handler.dto.ChatMessageType;
import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.SessionAttributeDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MessageTypeHandler implements ChatHandleProvider {

    private final WebSocketChatSessions sessions;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher messageLogEventPublisher;
    private final ApplicationEventPublisher messageNotificationEventPublisher;

    @Override
    public ChatMessageType supportsChatType() {
        return ChatMessageType.MESSAGE;
    }

    @Override
    public List<SendMessageDto> createResponse(
            final WebSocketSession session,
            final Map<String, String> data
    ) throws JsonProcessingException {
        final SessionAttributeDto sessionAttributes = getSessionAttributes(session);

        return createSendMessageResponse(data, sessionAttributes);
    }


    private SessionAttributeDto getSessionAttributes(final WebSocketSession session) {
        final Map<String, Object> attributes = session.getAttributes();

        return objectMapper.convertValue(attributes, SessionAttributeDto.class);
    }

    private List<SendMessageDto> createSendMessageResponse(
            final Map<String, String> data,
            final SessionAttributeDto sessionAttribute
    ) throws JsonProcessingException {
        final Long writerId = sessionAttribute.userId();
        final ChatMessageDataDto messageData = objectMapper.convertValue(data, ChatMessageDataDto.class);
        final CreateMessageDto createMessageDto = createMessageDto(messageData, writerId);
        final Message message = messageService.create(createMessageDto);
        sendNotificationIfReceiverNotInSession(message, sessionAttribute);

        return createSendMessages(message, writerId, createMessageDto.chatRoomId());
    }

    private void sendNotificationIfReceiverNotInSession(
            final Message message,
            final SessionAttributeDto sessionAttribute
    ) {
        if (!sessions.containsByUserId(message.getChatRoom().getId(), message.getReceiver().getId())) {
            final String profileImageAbsoluteUrl = String.valueOf(sessionAttribute.baseUrl());
            messageNotificationEventPublisher.publishEvent(new MessageNotificationEvent(
                    message,
                    profileImageAbsoluteUrl)
            );
        }
    }

    private List<SendMessageDto> createSendMessages(
            final Message message,
            final Long writerId,
            final Long chatRoomId
    ) throws JsonProcessingException {
        final Set<WebSocketSession> groupSessions = sessions.getSessionsByChatRoomId(message.getChatRoom().getId());

        final List<SendMessageDto> sendMessageDtos = new ArrayList<>();
        for (final WebSocketSession currentSession : groupSessions) {
            final MessageDto messageDto = MessageDto.of(message, isMyMessage(currentSession, writerId));
            final TextMessage textMessage = createTextMessage(messageDto);
            sendMessageDtos.add(new SendMessageDto(currentSession, textMessage));
            updateReadMessageLog(currentSession, chatRoomId, message);
        }

        return sendMessageDtos;
    }

    private CreateMessageDto createMessageDto(
            final ChatMessageDataDto messageData,
            final Long userId
    ) {
        final CreateMessageRequest request = new CreateMessageRequest(messageData.receiverId(), messageData.contents());

        return CreateMessageDto.of(userId, messageData.chatRoomId(), request);
    }

    private boolean isMyMessage(
            final WebSocketSession session,
            final Long writerId
    ) {
        final long userId = Long.parseLong(String.valueOf(session.getAttributes().get("userId")));

        return writerId.equals(userId);
    }

    private TextMessage createTextMessage(final MessageDto messageDto) throws JsonProcessingException {
        final HandleMessageResponse handleMessageResponse = new HandleMessageResponse(
                SendMessageStatus.SUCCESS,
                List.of(messageDto)
        );

        return new TextMessage(objectMapper.writeValueAsString(handleMessageResponse));
    }

    private void updateReadMessageLog(
            final WebSocketSession currentSession,
            final Long chatRoomId,
            final Message message
    ) {
        final SessionAttributeDto sessionAttributes = getSessionAttributes(currentSession);
        final UpdateReadMessageLogEvent updateReadMessageLogEvent = new UpdateReadMessageLogEvent(
                sessionAttributes.userId(),
                chatRoomId,
                message.getId()
        );
        messageLogEventPublisher.publishEvent(updateReadMessageLogEvent);
    }
}
