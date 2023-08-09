package com.ddangddangddang.data.repository

import android.content.Context
import com.ddangddangddang.data.datasource.AuthLocalDataSource
import com.ddangddangddang.data.datasource.AuthRemoteDataSource
import com.ddangddangddang.data.local.AuthSharedPreference
import com.ddangddangddang.data.model.request.KakaoLoginRequest
import com.ddangddangddang.data.model.request.RefreshTokenRequest
import com.ddangddangddang.data.model.response.TokenResponse
import com.ddangddangddang.data.remote.ApiResponse
import com.ddangddangddang.data.remote.Service

class AuthRepositoryImpl private constructor(
    private val localDataSource: AuthLocalDataSource,
    private val remoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun loginByKakao(kakaoToken: KakaoLoginRequest): ApiResponse<TokenResponse> {
        val response = remoteDataSource.loginByKakao(kakaoToken)
        if (response is ApiResponse.Success) {
            localDataSource.saveToken(response.body)
        }
        return response
    }

    override suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): ApiResponse<TokenResponse> {
        val response = remoteDataSource.refreshToken(refreshTokenRequest)
        if (response is ApiResponse.Success) {
            localDataSource.saveToken(response.body)
        }
        return response
    }

    override fun getAccessToken(): String = localDataSource.getAccessToken()

    override fun getRefreshToken(): String = localDataSource.getRefreshToken()

    companion object {
        @Volatile
        private var instance: AuthRepositoryImpl? = null

        fun getInstance(context: Context, service: Service): AuthRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: createInstance(context, service)
            }
        }

        private fun createInstance(context: Context, service: Service): AuthRepositoryImpl {
            val sharedPreferences = AuthSharedPreference(context)
            val localDataSource = AuthLocalDataSource(sharedPreferences)
            val remoteDataSource = AuthRemoteDataSource(service)
            return AuthRepositoryImpl(localDataSource, remoteDataSource)
                .also { instance = it }
        }
    }
}
