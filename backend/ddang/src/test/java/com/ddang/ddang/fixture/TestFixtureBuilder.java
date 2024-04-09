package com.ddang.ddang.fixture;

import com.ddang.ddang.auction.domain.Auction;
import com.ddang.ddang.auction.domain.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: 2024-04-9 무슨 이름이 적절할지 모르겠음
@Component
public class TestFixtureBuilder {

    @Autowired
    private AuctionRepository auctionRepository;

    public Auction buildAuction(final Auction auction) {
        return auctionRepository.save(auction);
    }
}
