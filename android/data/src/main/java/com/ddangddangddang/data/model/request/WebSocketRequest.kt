package com.ddangddangddang.data.model.request

sealed class WebSocketRequest(val type: String, open val data: WebSocketDataRequest) {

    data class ChatMessageRequest(
        override val data: WebSocketDataRequest.ChatMessageDataRequest,
    ) : WebSocketRequest("chattings", data)

    sealed class WebSocketDataRequest {
        data class ChatPingDataRequest(
            private val type: String = PING_TYPE,
            val chatRoomId: Long,
            val lastMessageId: Long,
        ) : WebSocketDataRequest()

        data class ChatMessageDataRequest(
            private val type: String = MESSAGE_TYPE,
            val chatRoomId: Long,
            val receiverId: Long,
            val contents: String,
        ) : WebSocketDataRequest()

        companion object {
            private const val PING_TYPE = "ping"
            private const val MESSAGE_TYPE = "message"
        }
    }
}
