package com.ddang.ddang.websocket.configuration;

import com.ddang.ddang.authentication.configuration.AuthenticationInterceptorService;
import com.ddang.ddang.authentication.configuration.exception.UserUnauthorizedException;
import com.ddang.ddang.authentication.domain.dto.AuthenticationUserInfo;
import com.ddang.ddang.image.presentation.util.ImageRelativeUrl;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.BASE_URL;
import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.CONNECTED;
import static com.ddang.ddang.websocket.handler.dto.WebSocketAttributeKey.USER_ID;

@Component
@RequiredArgsConstructor
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    private final AuthenticationInterceptorService authenticationInterceptorService;

    @Override
    public boolean beforeHandshake(
            final ServerHttpRequest request,
            final ServerHttpResponse response,
            final WebSocketHandler wsHandler,
            final Map<String, Object> attributes
    ) throws Exception {
        attributes.put(USER_ID.getName(), findUserId(request));
        attributes.put(BASE_URL.getName(), ImageRelativeUrl.USER.calculateAbsoluteUrl());
        attributes.put(CONNECTED.getName(), true);

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    private Long findUserId(final ServerHttpRequest request) {
        final String accessToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        authenticationInterceptorService.handleAccessToken(accessToken);

        final AuthenticationUserInfo authenticationUserInfo = authenticationInterceptorService.getAuthenticationUserInfo();
        if (authenticationUserInfo.userId() == null) {
            throw new UserUnauthorizedException("로그인이 필요한 기능입니다.");
        }

        return authenticationUserInfo.userId();
    }
}
