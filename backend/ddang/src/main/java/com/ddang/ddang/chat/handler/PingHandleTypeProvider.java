package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.application.MessageService;
import com.ddang.ddang.chat.application.dto.ReadMessageDto;
import com.ddang.ddang.chat.handler.dto.ChatPingDataDto;
import com.ddang.ddang.chat.handler.dto.HandleMessageResponse;
import com.ddang.ddang.chat.handler.dto.MessageDto;
import com.ddang.ddang.chat.handler.dto.SendMessageStatus;
import com.ddang.ddang.chat.presentation.dto.request.ReadMessageRequest;
import com.ddang.ddang.websocket.handler.dto.ChattingType;
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
public class PingHandleTypeProvider implements TypeHandleProvider {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Override
    public ChattingType supportsChatType() {
        return ChattingType.PING;
    }

    // TODO: 2024/04/15 예외 처리
    @Override
    public List<SendMessageDto> createResponse(
            final WebSocketSession session, final Map<String, String> data
    ) throws JsonProcessingException {
        final SessionAttributeDto sessionAttribute = getSessionAttributes(session);

        return createPingResponse(sessionAttribute, data, session);
    }

    private SessionAttributeDto getSessionAttributes(final WebSocketSession session) {
        final Map<String, Object> attributes = session.getAttributes();

        return objectMapper.convertValue(attributes, SessionAttributeDto.class);
    }

    private List<SendMessageDto> createPingResponse(
            final SessionAttributeDto sessionAttribute, final Map<String, String> data,
            final WebSocketSession userSession
    ) throws JsonProcessingException {
        final ChatPingDataDto pingData = objectMapper.convertValue(data, ChatPingDataDto.class);
        final ReadMessageRequest readMessageRequest = new ReadMessageRequest(sessionAttribute.userId(),
                                                                             pingData.chatRoomId(),
                                                                             pingData.lastMessageId()
        );
        final List<ReadMessageDto> readMessageDtos = messageService.readAllByLastMessageId(readMessageRequest);

        final List<MessageDto> messageDtos = convertToMessageDto(readMessageDtos, userSession);
        final HandleMessageResponse handleMessageResponse = new HandleMessageResponse(SendMessageStatus.SUCCESS,
                                                                                      messageDtos
        );
        return List.of(new SendMessageDto(userSession,
                                          new TextMessage(objectMapper.writeValueAsString(handleMessageResponse))
        ));
    }

    private List<MessageDto> convertToMessageDto(
            final List<ReadMessageDto> readMessageDtos, final WebSocketSession session
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
}
