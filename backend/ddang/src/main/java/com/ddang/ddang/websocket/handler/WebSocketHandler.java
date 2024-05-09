package com.ddang.ddang.websocket.handler;

import com.ddang.ddang.websocket.handler.dto.SendMessageDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageDto;
import com.ddang.ddang.websocket.handler.dto.TextMessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;

import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.CONNECTED;
import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.TYPE;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final WebSocketHandleTextMessageProviderComposite providerComposite;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
        final String payload = message.getPayload();
        final TextMessageDto textMessageDto = objectMapper.readValue(payload, TextMessageDto.class);
        session.getAttributes().put(TYPE.getName(), textMessageDto.type());

        final WebSocketHandleTextMessageProvider provider = providerComposite.findProvider(textMessageDto.type());
        final List<SendMessageDto> sendMessageDtos = provider.handleCreateSendMessage(session, textMessageDto.data());
        for (SendMessageDto sendMessageDto : sendMessageDtos) {
            sendMessageDto.session()
                          .sendMessage(sendMessageDto.textMessage());
        }
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        final String type = String.valueOf(session.getAttributes().get(TYPE.getName()));
        final TextMessageType textMessageType = TextMessageType.valueOf(type);
        final WebSocketHandleTextMessageProvider provider = providerComposite.findProvider(textMessageType);
        provider.remove(session);
    }

    @Override
    public void handlePongMessage(WebSocketSession session, PongMessage message) {
        final Map<String, Object> attributes = session.getAttributes();
        attributes.put(CONNECTED.getName(), true);
    }
}
