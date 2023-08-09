package com.ddangddangddang.android.feature.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.ddangddangddang.android.model.AuctionDetailModel
import com.ddangddangddang.android.model.mapper.AuctionDetailModelMapper.toPresentation
import com.ddangddangddang.android.util.livedata.SingleLiveEvent
import com.ddangddangddang.data.model.request.GetChatRoomIdRequest
import com.ddangddangddang.data.remote.ApiResponse
import com.ddangddangddang.data.repository.AuctionRepository
import com.ddangddangddang.data.repository.ChatRepository
import kotlinx.coroutines.launch

class AuctionDetailViewModel(
    private val auctionRepository: AuctionRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val _event: SingleLiveEvent<AuctionDetailEvent> = SingleLiveEvent()
    val event: LiveData<AuctionDetailEvent>
        get() = _event

    private val _auctionDetailModel: MutableLiveData<AuctionDetailModel> = MutableLiveData()
    val auctionDetailModel: LiveData<AuctionDetailModel>
        get() = _auctionDetailModel

    val auctionDetailBottomButtonStatus: LiveData<AuctionDetailBottomButtonStatus> =
        _auctionDetailModel.map {
            AuctionDetailBottomButtonStatus.find(it)
        }

    val minBidPrice: Int
        get() {
            val auction = auctionDetailModel.value ?: return 0
            return auction.lastBidPrice + auction.bidUnit
        }

    val text = ""

    fun loadAuctionDetail(auctionId: Long) {
        viewModelScope.launch {
            when (val response = auctionRepository.getAuctionDetail(auctionId)) {
                is ApiResponse.Success -> _auctionDetailModel.value = response.body.toPresentation()
                is ApiResponse.Failure -> {}
                is ApiResponse.NetworkError -> {}
                is ApiResponse.Unexpected -> {}
            }
        }
    }

    fun handleAuctionDetailBottomButton() {
        auctionDetailBottomButtonStatus.value?.let {
            when (it) {
                is AuctionDetailBottomButtonStatus.BidAuction -> popupAuctionBidEvent()
                is AuctionDetailBottomButtonStatus.EnterAuctionChatRoom -> enterChatRoomEvent()
                is AuctionDetailBottomButtonStatus.FinishAuction -> {}
            }
        }
    }

    private fun popupAuctionBidEvent() {
        _auctionDetailModel.value?.let {
            _event.value = AuctionDetailEvent.PopupAuctionBid
        }
    }

    private fun enterChatRoomEvent() {
        _auctionDetailModel.value?.let {
            viewModelScope.launch {
                val request = GetChatRoomIdRequest(it.id)
                when (val response = chatRepository.getChatRoomId(request)) {
                    is ApiResponse.Success -> {
                        _event.value = AuctionDetailEvent.EnterChatRoom(response.body.chatRoomId)
                    }

                    is ApiResponse.Failure -> {}
                    is ApiResponse.NetworkError -> {}
                    is ApiResponse.Unexpected -> {}
                }
            }
        }
    }

    fun exitEvent() {
        _event.value = AuctionDetailEvent.Exit
    }

    sealed class AuctionDetailEvent {
        object Exit : AuctionDetailEvent()
        object PopupAuctionBid : AuctionDetailEvent()
        data class EnterChatRoom(val chatId: Long) : AuctionDetailEvent()
    }
}
