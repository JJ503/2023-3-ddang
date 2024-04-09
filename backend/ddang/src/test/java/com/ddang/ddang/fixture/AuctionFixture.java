package com.ddang.ddang.fixture;

import com.ddang.ddang.auction.domain.Auction;
import com.ddang.ddang.auction.domain.BidUnit;
import com.ddang.ddang.auction.domain.Price;
import com.ddang.ddang.category.domain.Category;
import com.ddang.ddang.region.domain.AuctionRegion;
import com.ddang.ddang.region.domain.Region;
import com.ddang.ddang.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("NonAsciiCharacters")
public enum AuctionFixture {

    기본_경매("제목", "내용", 1_000, 1_000, LocalDateTime.now()),
    종료가_3일_뒤인_경매("제목", "내용", 1_000, 1_000, LocalDateTime.now().plusDays(3)),
    이미_종료된_경매("제목", "내용", 1_000, 1_000, LocalDateTime.now().minusDays(3));

    // TODO: 2024-04-7 enum 내 변수도 한글로 하는 게 편할까나요?
    private final String title;
    private final String description;
    private final int bidUnit;
    private final int startPrice;
    private final LocalDateTime closingTime;

    AuctionFixture(
            final String title,
            final String description,
            final int bidUnit,
            final int startPrice,
            final LocalDateTime closingTime
    ) {
        this.title = title;
        this.description = description;
        this.bidUnit = bidUnit;
        this.startPrice = startPrice;
        this.closingTime = closingTime;
    }

    // TODO: 2024-04-7 만약 카테고리, 지역을 설정하지 않을 것이라면 제거해도 무관 혹은 두 가지 버전을 만들던가
    public Auction 생성(
            final User seller,
            final List<Region> thirdRegions,
            final Category subCategory
    ) {
        final Auction auction = Auction.builder()
                                       .seller(seller)
                                       .title(title)
                                       .description(description)
                                       .subCategory(subCategory)
                                       .bidUnit(new BidUnit(bidUnit))
                                       .startPrice(new Price(startPrice))
                                       .closingTime(closingTime)
                                       .build();

        final List<AuctionRegion> auctionRegions = thirdRegions.stream()
                                                               .map(AuctionRegion::new)
                                                               .toList();
        auction.addAuctionRegions(auctionRegions);

        return auction;
    }

    public Auction 생성(final List<Region> thirdRegions, final Category subCategory) {
        return 생성(null, thirdRegions, subCategory);

    }

    public Auction 생성(final User seller, final Category subCategory) {
        return 생성(seller, List.of(), subCategory);
    }

    public Auction 생성(final User seller, final List<Region> thirdRegions) {
        return 생성(seller, thirdRegions, null);
    }
}
