package com.ddang.ddang.chat.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class WebSocketSessions {

    protected static final String CHAT_ROOM_ID_KEY = "chatRoomId";
    private static final String USER_ID_KEY = "userId";

    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void putIfAbsent(final WebSocketSession session, final Long chatRoomId) {
        if (!sessions.contains(session)) {
            session.getAttributes().put(CHAT_ROOM_ID_KEY, chatRoomId);
            sessions.add(session);
        }
    }

    public boolean contains(final Long userId) {
        log.info("WebSocketSessions.contains userId : {}, currentSessions : {}", userId, sessions);
        return sessions.stream()
                       .anyMatch(session -> session.getAttributes().get(USER_ID_KEY) == userId);
    }

    public Optional<WebSocketSession> findByUserId(final long userId) {
        return sessions.stream()
                       .filter(session -> session.getAttributes().get(USER_ID_KEY).equals(userId))
                       .findFirst();
    }

    public void remove(final WebSocketSession session) {
        sessions.remove(session);
    }
}
