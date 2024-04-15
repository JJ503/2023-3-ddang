package com.ddang.ddang.chat.handler;

import com.ddang.ddang.websocket.handler.dto.ChattingType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChatHandleTypeProviderComposite {

    private final Map<ChattingType, TypeHandleProvider> mappings;

    public ChatHandleTypeProviderComposite(final Set<TypeHandleProvider> providers) {
        this.mappings = providers.stream()
                                 .collect(Collectors.toMap(TypeHandleProvider::supportsChatType, provider -> provider));
    }

    public TypeHandleProvider findProvider(final ChattingType chattingType) {
        return mappings.get(chattingType);
    }
}
