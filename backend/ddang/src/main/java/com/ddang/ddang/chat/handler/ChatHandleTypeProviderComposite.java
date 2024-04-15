package com.ddang.ddang.chat.handler;

import com.ddang.ddang.websocket.handler.dto.ChatMessageType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChatHandleTypeProviderComposite {

    private final Map<ChatMessageType, ChatHandleProvider> mappings;

    public ChatHandleTypeProviderComposite(final Set<ChatHandleProvider> providers) {
        this.mappings = providers.stream()
                                 .collect(Collectors.toMap(ChatHandleProvider::supportsChatType, provider -> provider));
    }

    public ChatHandleProvider findProvider(final ChatMessageType chatMessageType) {
        return mappings.get(chatMessageType);
    }
}
