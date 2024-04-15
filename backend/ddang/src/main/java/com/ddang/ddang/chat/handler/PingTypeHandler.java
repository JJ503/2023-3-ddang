package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.application.MessageService;
import com.ddang.ddang.chat.application.dto.ReadMessageDto;
import com.ddang.ddang.chat.domain.WebSocketChatSessions;
import com.ddang.ddang.chat.domain.WebSocketSessions;
import com.ddang.ddang.chat.handler.dto.ChatPingDto;
import com.ddang.ddang.chat.handler.dto.MessageDto;
import com.ddang.ddang.chat.handler.dto.PingDataDto;
import com.ddang.ddang.chat.handler.dto.SendChatResponse;
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
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class PingTypeHandler implements ChatHandleProvider {

    private final WebSocketChatSessions sessions;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public ChatMessageType supportsChatType() {
        return ChatMessageType.PING;
    }

    @Override
    public List<SendMessageDto> createResponse(
            final SessionAttributeDto sessionAttributeDto,
            final Map<String, String> chatPingData
    ) throws JsonProcessingException {
        final PingDataDto pingDataDto = PingDataDto.from(chatPingData);
        final ReadMessageRequest readMessageRequest = createReadMessageRequest(sessionAttributeDto, pingDataDto);
        final List<ReadMessageDto> readMessageDtos = messageService.readAllByLastMessageId(readMessageRequest);
        final SendChatResponse sendChatResponse = createSendChatResponse(readMessageDtos, sessionAttributeDto);

        return List.of(createSendMessageDto(sessionAttributeDto, pingDataDto, sendChatResponse));
    }

    private ReadMessageRequest createReadMessageRequest(
            final SessionAttributeDto sessionAttributeDto,
            final PingDataDto chatPingData
    ) {
        final ChatPingDto pingDto = objectMapper.convertValue(chatPingData, ChatPingDto.class);

        return new ReadMessageRequest(sessionAttributeDto.userId(), pingDto.chatRoomId(), pingDto.lastMessageId());
    }

    private SendChatResponse createSendChatResponse(
            final List<ReadMessageDto> readMessageDtos,
            final SessionAttributeDto session
    ) {
        final List<MessageDto> messageDtos = convertToMessageDto(readMessageDtos, session);

        return new SendChatResponse(SendMessageStatus.SUCCESS, messageDtos);
    }

    private List<MessageDto> convertToMessageDto(
            final List<ReadMessageDto> readMessageDtos,
            final SessionAttributeDto sessionAttributeDto
    ) {
        return readMessageDtos.stream()
                              .map(readMessageDto -> MessageDto.of(readMessageDto, isMyMessage(sessionAttributeDto,
                                                                                               readMessageDto.writerId()
                              ))).toList();
    }

    private boolean isMyMessage(final SessionAttributeDto session, final Long writerId) {
        final long userId = session.userId();

        return writerId.equals(userId);
    }

    private SendMessageDto createSendMessageDto(
            final SessionAttributeDto sessionAttributeDto,
            final PingDataDto chatPingData,
            final SendChatResponse sendChatResponse
    ) throws JsonProcessingException {
        final TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(sendChatResponse));
        final WebSocketSessions sessions = this.sessions.findSessionsByChatRoomId(chatPingData.chatRoomId());
        final WebSocketSession userSession = sessions.findByUserId(sessionAttributeDto.userId())
                                                     .orElseThrow(() -> new NoSuchElementException(
                                                             "웹소켓에 연결된 사용자가 존재하지 않습니다."
                                                     ));

        return new SendMessageDto(userSession, textMessage);
    }
}
