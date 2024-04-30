package com.ddang.ddang.chat.handler.dto;

import java.util.List;

public record HandleMessageResponse(SendMessageStatus status, List<MessageDto> messages) {
}
