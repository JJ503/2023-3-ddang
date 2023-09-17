package com.ddangddangddang.android.feature.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddangddangddang.android.model.ProfileModel
import com.ddangddangddang.android.util.image.toAdjustImageFile
import com.ddangddangddang.android.util.livedata.SingleLiveEvent
import com.ddangddangddang.data.model.request.ProfileUpdateRequest
import com.ddangddangddang.data.remote.ApiResponse
import com.ddangddangddang.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileChangeViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _event: SingleLiveEvent<Event> = SingleLiveEvent()
    val event: LiveData<Event>
        get() = _event

    private val _profile: MutableLiveData<Uri> = MutableLiveData()
    val profile: LiveData<Uri>
        get() = _profile

    val userNickname: MutableLiveData<String> = MutableLiveData()

    fun setupProfile(profileModel: ProfileModel, defaultUri: Uri) {
        _profile.value = profileModel.profileImage?.let { Uri.parse(it) } ?: defaultUri
        userNickname.value = profileModel.name
    }

    fun setExitEvent() {
        _event.value = Event.Exit
    }

    fun selectProfileImage() {
        _event.value = Event.NavigateToSelectProfileImage
    }

    fun setProfileImageUri(uri: Uri) {
        _profile.value = uri
    }

    fun submitProfile(context: Context) {
        val name = userNickname.value ?: return
        val profileImageUri = profile.value ?: return
        viewModelScope.launch {
            val file = runCatching { profileImageUri.toAdjustImageFile(context) }
                .getOrNull() ?: return@launch

            when (userRepository.updateProfile(file, ProfileUpdateRequest(name))) {
                is ApiResponse.Success -> {
                    _event.value = Event.SuccessProfileChange
                }

                is ApiResponse.Failure -> {}
                is ApiResponse.NetworkError -> {}
                is ApiResponse.Unexpected -> {}
            }
        }
    }

    sealed class Event {
        object Exit : Event()
        object NavigateToSelectProfileImage : Event()
        object SuccessProfileChange : Event()
    }
}
