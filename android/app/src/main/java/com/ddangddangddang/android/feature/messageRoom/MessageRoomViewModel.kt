package com.ddangddangddang.android.feature.messageRoom

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddangddangddang.android.feature.common.ErrorType
import com.ddangddangddang.android.model.MessageModel
import com.ddangddangddang.android.model.MessageRoomDetailModel
import com.ddangddangddang.android.model.mapper.MessageModelMapper.toPresentation
import com.ddangddangddang.android.model.mapper.MessageRoomDetailModelMapper.toPresentation
import com.ddangddangddang.android.util.livedata.SingleLiveEvent
import com.ddangddangddang.data.model.request.WebSocketRequest
import com.ddangddangddang.data.remote.callAdapter.ApiResponse
import com.ddangddangddang.data.repository.ChatRepository
import com.ddangddangddang.data.repository.RealTimeRepository
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageRoomViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val realTimeRepository: RealTimeRepository,
) : ViewModel() {
    private var isPing = false
    var isConnected = false
        set(value) {
            if (!value) isPing = false
            field = value
        }
    val inputMessage: MutableLiveData<String> = MutableLiveData("")

    private val _event: SingleLiveEvent<MessageRoomEvent> = SingleLiveEvent()
    val event: LiveData<MessageRoomEvent>
        get() = _event
    private val _messageRoomInfo: MutableLiveData<MessageRoomDetailModel> = MutableLiveData()

    val messageRoomInfo: LiveData<MessageRoomDetailModel>
        get() = _messageRoomInfo
    private val _messages: MutableLiveData<List<MessageViewItem>> = MutableLiveData()
    val messages: LiveData<List<MessageViewItem>>
        get() = _messages

    private val lastMessageId: Long?
        get() = _messages.value?.lastOrNull()?.id

    private var isSubmitLoading: Boolean = false

    init {
        observeChatMessage()
        observeWebSocketEvent()
    }

    private fun observeChatMessage() {
        viewModelScope.launch {
            realTimeRepository.observeChatMessage().collect {
                when (it.sendMessageStatus) {
                    "SUCCESS" -> {
                        isPing = true
                        addMessages(it.messages.map { it.toPresentation() }.toViewItems())
                    }

                    "DISCONNECTED" -> {
                        Log.d("WS", "DISCONNECTED")
                        isPing = false
                    }

                    "FORBIDDEN" -> {
                        Log.d("WS", "FORBIDDEN")
                        isPing = false
                        // 방 권한 없음을 알리고, 나가기 처리 해야함.
                    }
                }
            }
        }
    }

    private fun observeWebSocketEvent() {
        viewModelScope.launch {
            realTimeRepository.observeWebSocketEvent().collect {
                Log.d("WS", it.toString())
                if (it is WebSocket.Event.OnConnectionOpened<Any>) {
                    sendPing()
                }
            }
        }
    }

    fun loadMessageRoom(roomId: Long) {
        viewModelScope.launch {
            when (val response = chatRepository.getChatRoom(roomId)) {
                is ApiResponse.Success -> {
                    _messageRoomInfo.value = response.body.toPresentation()
                }

                is ApiResponse.Failure -> {
                    _event.value =
                        MessageRoomEvent.FailureEvent.LoadRoomInfo(ErrorType.FAILURE(response.error))
                }

                is ApiResponse.NetworkError -> {
                    _event.value =
                        MessageRoomEvent.FailureEvent.LoadRoomInfo(ErrorType.NETWORK_ERROR)
                }

                is ApiResponse.Unexpected -> {
                    _event.value = MessageRoomEvent.FailureEvent.LoadRoomInfo(ErrorType.UNEXPECTED)
                }
            }
        }
    }

    private fun addMessages(messages: List<MessageViewItem>) {
        _messages.value = _messages.value?.plus(messages) ?: messages
    }

    private fun List<MessageModel>.toViewItems(): List<MessageViewItem> {
        var previousSendDate = _messages.value?.lastOrNull()?.createdDateTime?.toLocalDate()
        return map { messageModel ->
            val sendDate = messageModel.createdDateTime.toLocalDate()
            val isFirstAtDate = (sendDate == previousSendDate).not()
            previousSendDate = sendDate
            messageModel.toViewItem(isFirstAtDate)
        }
    }

    private fun MessageModel.toViewItem(
        isFirstAtDate: Boolean,
    ): MessageViewItem {
        return if (isMyMessage) {
            MessageViewItem.MyMessageViewItem(id, createdDateTime, contents, isFirstAtDate)
        } else {
            MessageViewItem.PartnerMessageViewItem(id, createdDateTime, contents, isFirstAtDate)
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            Log.d("WS - TYPE", "MESSAGE")
            _messageRoomInfo.value?.let {
                val message = inputMessage.value
                if (message.isNullOrEmpty()) return@launch
                if (isSubmitLoading || !isConnected) return@launch
                if (!isPing) {
                    sendPing()
                    if (!isPing) return@launch
                }
                isSubmitLoading = true
                val data = WebSocketRequest.WebSocketDataRequest.ChatMessageDataRequest(
                    it.roomId,
                    it.messagePartnerId,
                    message,
                )

                val response = realTimeRepository.send(WebSocketRequest.ChatRequest(data))
                if (response) {
                    inputMessage.value = ""
                }
                isSubmitLoading = false
            }
        }
    }

    private fun sendPing() {
        viewModelScope.launch {
            _messageRoomInfo.value?.let {
                if (isSubmitLoading) return@launch
                isSubmitLoading = true

                val data = WebSocketRequest.WebSocketDataRequest.ChatPingDataRequest(
                    it.roomId,
                    lastMessageId,
                )

                if (realTimeRepository.send(WebSocketRequest.ChatRequest(data))) {
                    Log.d("WS - TYPE", "PING - SUCCESS")
                } else {
                    Log.d("WS - TYPE", "PING - FAILED")
                }

                isSubmitLoading = false
            }
        }
    }

    fun setExitEvent() {
        _event.value = MessageRoomEvent.Exit
    }

    fun setReportEvent() {
        _messageRoomInfo.value?.let { _event.value = MessageRoomEvent.Report(it.roomId) }
    }

    fun setRateEvent() {
        _event.value = MessageRoomEvent.Rate
    }

    fun setNavigateToAuctionDetailEvent() {
        _messageRoomInfo.value?.let {
            _event.value = MessageRoomEvent.NavigateToAuctionDetail(it.auctionId)
        }
    }

    sealed class MessageRoomEvent {
        object Exit : MessageRoomEvent()
        data class Report(val roomId: Long) : MessageRoomEvent()
        object Rate : MessageRoomEvent()
        data class NavigateToAuctionDetail(val auctionId: Long) : MessageRoomEvent()
        sealed class FailureEvent(val type: ErrorType) : MessageRoomEvent() {
            class LoadRoomInfo(type: ErrorType) : FailureEvent(type)
            class LoadMessages(type: ErrorType) : FailureEvent(type)
            class SendMessage(type: ErrorType) : FailureEvent(type)
        }
    }
}
