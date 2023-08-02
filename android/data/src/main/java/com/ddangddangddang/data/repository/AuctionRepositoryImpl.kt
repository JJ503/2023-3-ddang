package com.ddangddangddang.data.repository

import androidx.lifecycle.LiveData
import com.ddangddangddang.data.datasource.AuctionLocalDataSource
import com.ddangddangddang.data.datasource.AuctionRemoteDataSource
import com.ddangddangddang.data.model.request.AuctionBidRequest
import com.ddangddangddang.data.model.request.RegisterAuctionRequest
import com.ddangddangddang.data.model.response.AuctionDetailResponse
import com.ddangddangddang.data.model.response.AuctionPreviewResponse
import com.ddangddangddang.data.model.response.AuctionPreviewsResponse
import com.ddangddangddang.data.remote.ApiResponse
import com.ddangddangddang.data.remote.Service
import java.io.File

class AuctionRepositoryImpl private constructor(
    private val localDataSource: AuctionLocalDataSource,
    private val remoteDataSource: AuctionRemoteDataSource,
) : AuctionRepository {

    override fun observeAuctionPreviews(): LiveData<List<AuctionPreviewResponse>> {
        return localDataSource.observeAuctionPreviews()
    }

    override suspend fun getAuctionPreviews(
        lastAuctionId: Long?,
        size: Int,
    ): ApiResponse<AuctionPreviewsResponse> {
        val response = remoteDataSource.getAuctionPreviews(lastAuctionId, size)
        if (response is ApiResponse.Success) {
            localDataSource.addAuctionPreviews(response.body.auctions)
        }
        return response
    }

    override suspend fun getAuctionDetail(id: Long): ApiResponse<AuctionDetailResponse> {
        return remoteDataSource.getAuctionDetail(id)
    }

    override suspend fun registerAuction(
        images: List<File>,
        auction: RegisterAuctionRequest,
    ): ApiResponse<AuctionPreviewResponse> {
        val response = remoteDataSource.registerAuction(images, auction)
        if (response is ApiResponse.Success) {
            localDataSource.addAuctionPreview(response.body)
        }
        return response
    }

    override suspend fun submitAuctionBid(
        auctionId: Long,
        bidPrice: Int,
    ): ApiResponse<Unit> {
        return remoteDataSource.submitAuctionBid(AuctionBidRequest(auctionId, bidPrice))
    }

    companion object {
        @Volatile
        private var instance: AuctionRepositoryImpl? = null

        fun getInstance(service: Service): AuctionRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: createInstance(service)
            }
        }

        private fun createInstance(service: Service): AuctionRepositoryImpl {
            val localDataSource = AuctionLocalDataSource()
            val remoteDataSource = AuctionRemoteDataSource(service)
            return AuctionRepositoryImpl(localDataSource, remoteDataSource)
                .also { instance = it }
        }
    }
}
