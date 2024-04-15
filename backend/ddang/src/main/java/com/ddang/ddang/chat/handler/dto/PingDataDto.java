package com.ddang.ddang.chat.handler.dto;

import java.util.Map;

public record PingDataDto(long chatRoomId, long lastMessageId) {

    private static final String CHAT_ROOM_ID_KEY = "chatRoomId";
    private static final String LAST_MESSAGE_ID = "lastMessageId";

    public static PingDataDto from(final Map<String, String> chatPingData) {
        return new PingDataDto(Long.parseLong(chatPingData.get(CHAT_ROOM_ID_KEY)),
                               Long.parseLong(chatPingData.get(LAST_MESSAGE_ID))
        );
    }
}
