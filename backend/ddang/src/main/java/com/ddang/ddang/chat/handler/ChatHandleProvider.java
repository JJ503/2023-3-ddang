package com.ddang.ddang.chat.handler;

import com.ddang.ddang.websocket.handler.dto.ChatMessageType;
import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.SessionAttributeDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface ChatHandleProvider {

    List<SendMessageDto> createResponse(
            final SessionAttributeDto sessionAttributeDto,
            final Map<String, String> data
    ) throws JsonProcessingException;

    ChatMessageType supportsChatType();
}
