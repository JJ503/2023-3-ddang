package com.ddangddangddang.data.model.request

sealed class WebSocketRequest(val type: String, open val data: WebSocketDataRequest) {

    data class ChatRequest(
        override val data: WebSocketDataRequest,
    ) : WebSocketRequest("chattings", data)

    sealed class WebSocketDataRequest(val type: String) {
        data class ChatPingDataRequest(
            val chatRoomId: Long,
            val lastMessageId: Long?,
        ) : WebSocketDataRequest(PING_TYPE)

        data class ChatMessageDataRequest(
            val chatRoomId: Long,
            val receiverId: Long,
            val contents: String,
        ) : WebSocketDataRequest(MESSAGE_TYPE)

        companion object {
            private const val PING_TYPE = "ping"
            private const val MESSAGE_TYPE = "message"
        }
    }
}
