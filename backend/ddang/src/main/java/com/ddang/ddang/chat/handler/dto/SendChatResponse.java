package com.ddang.ddang.chat.handler.dto;

import java.util.List;

public record SendChatResponse(SendMessageStatus status, List<MessageDto> messages) {
}
