package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.application.MessageService;
import com.ddang.ddang.chat.application.dto.ReadMessageDto;
import com.ddang.ddang.chat.handler.dto.ChatPingDto;
import com.ddang.ddang.chat.handler.dto.SendChatResponse;
import com.ddang.ddang.chat.handler.dto.MessageDto;
import com.ddang.ddang.chat.handler.dto.SendMessageStatus;
import com.ddang.ddang.chat.presentation.dto.request.ReadMessageRequest;
import com.ddang.ddang.websocket.handler.dto.ChatMessageType;
import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.SessionAttributeDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PingTypeHandler implements ChatHandleProvider {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public ChatMessageType supportsChatType() {
        return ChatMessageType.PING;
    }

    @Override
    public List<SendMessageDto> createResponse(
            final WebSocketSession session,
            final Map<String, String> chatPingData
    ) throws JsonProcessingException {
        final ReadMessageRequest readMessageRequest = createReadMessageRequest(session, chatPingData);
        final List<ReadMessageDto> readMessageDtos = messageService.readAllByLastMessageId(readMessageRequest);
        final SendChatResponse sendChatResponse = createSendChatResponse(readMessageDtos, session);

        return List.of(createSendMessageDto(session, sendChatResponse));
    }

    private ReadMessageRequest createReadMessageRequest(
            final WebSocketSession session,
            final Map<String, String> chatPingData
    ) {
        final SessionAttributeDto sessionAttribute = convertToSessionAttributes(session);
        final ChatPingDto pingDto = objectMapper.convertValue(chatPingData, ChatPingDto.class);

        return new ReadMessageRequest(sessionAttribute.userId(), pingDto.chatRoomId(), pingDto.lastMessageId());
    }

    private SessionAttributeDto convertToSessionAttributes(final WebSocketSession session) {
        final Map<String, Object> attributes = session.getAttributes();

        return objectMapper.convertValue(attributes, SessionAttributeDto.class);
    }

    private SendChatResponse createSendChatResponse(
            final List<ReadMessageDto> readMessageDtos,
            final WebSocketSession session
    ) {
        final List<MessageDto> messageDtos = convertToMessageDto(readMessageDtos, session);

        return new SendChatResponse(SendMessageStatus.SUCCESS, messageDtos);
    }

    private List<MessageDto> convertToMessageDto(
            final List<ReadMessageDto> readMessageDtos,
            final WebSocketSession session
    ) {
        return readMessageDtos.stream()
                              .map(readMessageDto -> MessageDto.of(readMessageDto,
                                                                   isMyMessage(session, readMessageDto.writerId())
                              ))
                              .toList();
    }

    private boolean isMyMessage(
            final WebSocketSession session, final Long writerId
    ) {
        final long userId = Long.parseLong(String.valueOf(session.getAttributes().get("userId")));

        return writerId.equals(userId);
    }

    private SendMessageDto createSendMessageDto(
            final WebSocketSession session,
            final SendChatResponse sendChatResponse
    ) throws JsonProcessingException {
        final TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(sendChatResponse));

        return new SendMessageDto(session, textMessage);
    }
}
