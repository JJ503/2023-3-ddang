package com.ddang.ddang.chat.handler.dto;

import java.util.Map;

public record MessageDataDto(long chatRoomId, long receiverId, String contents) {

    private static final String CHAT_ROOM_ID_KEY = "chatRoomId";
    private static final String RECEIVER_ID_KEY = "receiverId";
    private static final String CONTENTS_ID_KEY = "contents";

    public static MessageDataDto from(final Map<String, String> data) {
        return new MessageDataDto(
                Long.parseLong(data.get(CHAT_ROOM_ID_KEY)),
                Long.parseLong(data.get(RECEIVER_ID_KEY)),
                data.get(CONTENTS_ID_KEY)
        );
    }
}
