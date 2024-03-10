package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.application.MessageService;
import com.ddang.ddang.chat.application.dto.CreateMessageDto;
import com.ddang.ddang.chat.application.dto.ReadMessageDto;
import com.ddang.ddang.chat.domain.WebSocketChatSessions;
import com.ddang.ddang.chat.handler.dto.ChatMessageDataDto;
import com.ddang.ddang.chat.presentation.dto.request.CreateMessageRequest;
import com.ddang.ddang.chat.presentation.dto.response.ReadMessageResponse;
import com.ddang.ddang.websocket.handler.WebSocketHandleTextMessageProvider;
import com.ddang.ddang.websocket.handler.dto.SendMessagesDto;
import com.ddang.ddang.websocket.handler.dto.SessionAttributeDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandleTextMessageProvider implements WebSocketHandleTextMessageProvider {

    private final WebSocketChatSessions sessions;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public TextMessageType supportTextMessageType() {
        return TextMessageType.CHATTINGS;
    }

    @Override
    public List<SendMessagesDto> handle(
            final WebSocketSession session,
            final Map<String, String> data
    ) throws Exception {
        final SessionAttributeDto sessionAttribute = getSessionAttributes(session);
        final ChatMessageDataDto messageData = objectMapper.convertValue(data, ChatMessageDataDto.class);
        sessions.add(session, messageData.chatRoomId());

        final Long senderId = sessionAttribute.userId();
        final CreateMessageDto createMessageDto = createMessageDto(messageData, senderId);
        final ReadMessageDto messageDto = createMessageDto(createMessageDto, sessionAttribute);

        return createSendMessages(session, messageDto, senderId);
    }

    private ReadMessageDto createMessageDto(
            final CreateMessageDto createMessageDto,
            final SessionAttributeDto sessionAttribute
    ) {
        if (sessions.containsByUserId(createMessageDto.chatRoomId(), createMessageDto.receiverId())) {
            return messageService.create(createMessageDto);
        }

        return messageService.createWithNotification(createMessageDto, sessionAttribute.baseUrl());
    }

    private SessionAttributeDto getSessionAttributes(final WebSocketSession session) {
        final Map<String, Object> attributes = session.getAttributes();

        return objectMapper.convertValue(attributes, SessionAttributeDto.class);
    }

    private CreateMessageDto createMessageDto(final ChatMessageDataDto messageData, final Long userId) {
        final CreateMessageRequest request = new CreateMessageRequest(
                messageData.receiverId(),
                messageData.contents()
        );

        return CreateMessageDto.of(userId, messageData.chatRoomId(), request);
    }

    private List<SendMessagesDto> createSendMessages(
            final WebSocketSession session,
            final ReadMessageDto messageDto,
            final Long senderId
    ) throws JsonProcessingException {
        final Set<WebSocketSession> groupSessions = sessions.getSessionsByChatRoomId(messageDto.chatRoomId());

        final List<SendMessagesDto> sendMessagesDtos = new ArrayList<>();
        for (WebSocketSession currentSession : groupSessions) {
            final TextMessage textMessage = createTextMessage(messageDto, senderId, currentSession);
            sendMessagesDtos.add(new SendMessagesDto(session, textMessage));
        }

        return sendMessagesDtos;
    }

    private TextMessage createTextMessage(
            final ReadMessageDto messageDto,
            final Long senderId,
            final WebSocketSession session
    ) throws JsonProcessingException {
        final ReadMessageResponse response = ReadMessageResponse.of(
                messageDto,
                isMyMessage(session, senderId)
        );

        return new TextMessage(objectMapper.writeValueAsString(response));
    }

    private boolean isMyMessage(final WebSocketSession session, final Long senderId) {
        final long userId = Long.parseLong(String.valueOf(session.getAttributes().get("userId")));

        return senderId.equals(userId);
    }

    @Override
    public void remove(final WebSocketSession session) {
        sessions.remove(session);
    }
}
