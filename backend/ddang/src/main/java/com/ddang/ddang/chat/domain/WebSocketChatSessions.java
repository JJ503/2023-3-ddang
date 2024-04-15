package com.ddang.ddang.chat.domain;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ddang.ddang.chat.domain.WebSocketSessions.CHAT_ROOM_ID_KEY;

@Getter
@Component
public class WebSocketChatSessions {

    private final Map<Long, WebSocketSessions> chatRoomSessions = new ConcurrentHashMap<>();

    public void add(final WebSocketSession session, final Long chatRoomId) {
        chatRoomSessions.putIfAbsent(chatRoomId, new WebSocketSessions());
        final WebSocketSessions webSocketSessions = chatRoomSessions.get(chatRoomId);
        webSocketSessions.putIfAbsent(session, chatRoomId);
    }

    public WebSocketSessions findSessionsByChatRoomId(final Long chatRoomId) {
        return chatRoomSessions.get(chatRoomId);
    }

    public boolean containsByUserId(final Long chatRoomId, final Long userId) {
        final WebSocketSessions webSocketSessions = chatRoomSessions.get(chatRoomId);

        return webSocketSessions.contains(userId);
    }

    public void remove(final WebSocketSession session) {
        final long chatRoomId = Long.parseLong(String.valueOf(session.getAttributes().get(CHAT_ROOM_ID_KEY)));
        final WebSocketSessions webSocketSessions = chatRoomSessions.get(chatRoomId);
        webSocketSessions.remove(session);
    }
}
