package com.ddang.ddang.fixture;

import com.ddang.ddang.auction.application.dto.CreateAuctionDto;
import com.ddang.ddang.auction.domain.Auction;
import com.ddang.ddang.region.domain.AuctionRegion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class AuctionDtoFixture {

    public static CreateAuctionDto 경매_생성_dto_생성(
            final Auction auction,
            final Long sellerId,
            final Long categoryId,
            final List<Long> regionIds,
            final List<MultipartFile> auctionImages
    ) {
        return new CreateAuctionDto(
                auction.getTitle(),
                auction.getDescription(),
                auction.getBidUnit().getValue(),
                auction.getStartPrice().getValue(),
                auction.getClosingTime(),
                processRegionIds(auction, regionIds),
                processCategoryId(auction, categoryId),
                auctionImages,
                processSellerId(auction, sellerId)
        );
    }

    private static List<Long> processRegionIds(final Auction auction, final List<Long> regionIds) {
        if (regionIds != null) {
            return regionIds;
        }

        return auction.getAuctionRegions()
                      .stream()
                      .map(auctionRegion -> auctionRegion.getThirdRegion().getId())
                      .toList();
    }

    private static Long processCategoryId(final Auction auction, final Long categoryId) {
        if (categoryId != null) {
            return categoryId;
        }

        return auction.getSubCategory().getId();
    }

    private static Long processSellerId(final Auction auction, final Long sellerId) {
        if (sellerId != null) {
            return sellerId;
        }

        return auction.getSeller().getId();
    }

    public static CreateAuctionDto 경매_생성_dto_생성(
            final Auction auction,
            final Long sellerId,
            final List<MultipartFile> auctionImages
    ) {
        return 경매_생성_dto_생성(auction, sellerId, null, null, auctionImages);
    }

    public static CreateAuctionDto 경매_생성_dto_생성(
            final Auction auction,
            final List<Long> regionIds,
            final List<MultipartFile> auctionImages
    ) {
        return 경매_생성_dto_생성(auction, null, null, regionIds, auctionImages);
    }

    public static CreateAuctionDto 경매_생성_dto_생성(
            final Auction auction,
            final List<MultipartFile> auctionImages
    ) {
        return 경매_생성_dto_생성(auction, null, null, null, auctionImages);
    }
}
