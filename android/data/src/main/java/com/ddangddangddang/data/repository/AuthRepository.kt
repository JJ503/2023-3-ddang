package com.ddangddangddang.data.repository

import com.ddangddangddang.data.model.request.KakaoLoginRequest
import com.ddangddangddang.data.model.request.RefreshTokenRequest
import com.ddangddangddang.data.model.response.TokenResponse
import com.ddangddangddang.data.remote.ApiResponse

interface AuthRepository {
    suspend fun loginByKakao(kakaoToken: KakaoLoginRequest): ApiResponse<TokenResponse>

    suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): ApiResponse<TokenResponse>

    fun getAccessToken(): String

    fun getRefreshToken(): String
}
