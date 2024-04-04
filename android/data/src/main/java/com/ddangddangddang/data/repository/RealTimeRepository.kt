package com.ddangddangddang.data.repository

import com.ddangddangddang.data.model.request.WebSocketRequest
import com.ddangddangddang.data.model.response.ChatMessageResponse
import com.ddangddangddang.data.model.response.ChatWebSocketResponse
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.flow.Flow

interface RealTimeRepository {
    suspend fun send(data: WebSocketRequest): ChatWebSocketResponse
    fun observeChatMessage(): Flow<ChatMessageResponse>
    fun observeWebSocketEvent(): Flow<WebSocket.Event>
}
