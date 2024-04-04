package com.ddangddangddang.data.repository

import com.ddangddangddang.data.model.request.WebSocketRequest
import com.ddangddangddang.data.model.response.ChatMessageResponse
import com.ddangddangddang.data.model.response.ChatWebSocketResponse
import com.ddangddangddang.data.remote.scarlet.WebSocketService
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RealTimeRepositoryImpl @Inject constructor(
    private val service: WebSocketService,
) : RealTimeRepository {
    override suspend fun send(data: WebSocketRequest): ChatWebSocketResponse {
        return service.send(data)
    }

    override fun observeChatMessage(): Flow<ChatMessageResponse> = service.observeChatMessage()

    override fun observeWebSocketEvent(): Flow<WebSocket.Event> = service.observeWebSocketEvent()
}
