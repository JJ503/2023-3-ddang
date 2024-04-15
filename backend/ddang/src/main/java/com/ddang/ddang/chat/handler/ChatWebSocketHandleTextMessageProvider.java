package com.ddang.ddang.chat.handler;

import com.ddang.ddang.chat.domain.WebSocketChatSessions;
import com.ddang.ddang.websocket.handler.WebSocketHandleTextMessageProvider;
import com.ddang.ddang.websocket.handler.dto.ChatMessageType;
import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandleTextMessageProvider implements WebSocketHandleTextMessageProvider {

    private static final String CHATROOM_ID_KEY = "chatRoomId";
    private static final String TYPE_KEY = "type";

    private final WebSocketChatSessions sessions;
    private final ChatHandleTypeProviderComposite chatHandleTypeProviderComposite;

    @Override
    public TextMessageType supportTextMessageType() {
        return TextMessageType.CHATTINGS;
    }

    @Override
    public List<SendMessageDto> handleCreateSendMessage(
            final WebSocketSession session,
            final Map<String, String> chatMessageData
    ) throws JsonProcessingException {
        final long chatRoomId = getChatRoomId(chatMessageData);
        sessions.add(session, chatRoomId);

        final ChatMessageType type = ChatMessageType.findMessageType(chatMessageData.get(TYPE_KEY));
        final ChatHandleProvider provider = chatHandleTypeProviderComposite.findProvider(type);

        return provider.createResponse(session, chatMessageData);
    }

    private long getChatRoomId(final Map<String, String> data) {
        return Long.parseLong(data.get(CHATROOM_ID_KEY));
    }

    @Override
    public void remove(final WebSocketSession session) {
        sessions.remove(session);
    }
}
