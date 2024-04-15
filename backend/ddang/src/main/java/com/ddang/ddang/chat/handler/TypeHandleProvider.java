package com.ddang.ddang.chat.handler;

import com.ddang.ddang.websocket.handler.dto.ChattingType;
import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

public interface TypeHandleProvider {

    List<SendMessageDto> createResponse(final WebSocketSession session, final Map<String, String> data) throws JsonProcessingException;

    ChattingType supportsChatType();
}
