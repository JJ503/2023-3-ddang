package com.ddang.ddang.chat.infrastructure.persistence.fixture;

import com.ddang.ddang.auction.domain.Auction;
import com.ddang.ddang.auction.domain.BidUnit;
import com.ddang.ddang.auction.domain.Price;
import com.ddang.ddang.auction.domain.repository.AuctionRepository;
import com.ddang.ddang.auction.infrastructure.persistence.AuctionRepositoryImpl;
import com.ddang.ddang.auction.infrastructure.persistence.JpaAuctionRepository;
import com.ddang.ddang.auction.infrastructure.persistence.QuerydslAuctionRepository;
import com.ddang.ddang.bid.domain.Bid;
import com.ddang.ddang.bid.domain.BidPrice;
import com.ddang.ddang.bid.domain.repository.BidRepository;
import com.ddang.ddang.bid.infrastructure.persistence.BidRepositoryImpl;
import com.ddang.ddang.bid.infrastructure.persistence.JpaBidRepository;
import com.ddang.ddang.category.domain.Category;
import com.ddang.ddang.category.infrastructure.persistence.JpaCategoryRepository;
import com.ddang.ddang.chat.domain.ChatRoom;
import com.ddang.ddang.chat.domain.repository.ChatRoomRepository;
import com.ddang.ddang.chat.infrastructure.persistence.ChatRoomRepositoryImpl;
import com.ddang.ddang.chat.infrastructure.persistence.JpaChatRoomRepository;
import com.ddang.ddang.image.domain.AuctionImage;
import com.ddang.ddang.image.domain.ProfileImage;
import com.ddang.ddang.user.domain.Reliability;
import com.ddang.ddang.user.domain.User;
import com.ddang.ddang.user.domain.repository.UserRepository;
import com.ddang.ddang.user.infrastructure.persistence.JpaUserRepository;
import com.ddang.ddang.user.infrastructure.persistence.UserRepositoryImpl;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("NonAsciiCharacters")
public class ChatRoomRepositoryImplFixture {

    @Autowired
    private JpaCategoryRepository categoryRepository;

    private UserRepository userRepository;

    private AuctionRepository auctionRepository;

    private BidRepository bidRepository;

    private ChatRoomRepository chatRoomRepository;

    protected User 판매자;
    protected User 구매자;
    protected Auction 경매;
    private Bid 입찰;
    protected ChatRoom 채팅방;
    protected Long 존재하지_않는_채팅방_아이디 = -999L;

    @BeforeEach
    void fixtureSetUp(
            @Autowired final JPAQueryFactory jpaQueryFactory,
            @Autowired final JpaAuctionRepository jpaAuctionRepository,
            @Autowired final JpaUserRepository jpaUserRepository,
            @Autowired final JpaChatRoomRepository jpaChatRoomRepository,
            @Autowired final JpaBidRepository jpaBidRepository
    ) {
        auctionRepository = new AuctionRepositoryImpl(jpaAuctionRepository, new QuerydslAuctionRepository(jpaQueryFactory));
        userRepository = new UserRepositoryImpl(jpaUserRepository);
        chatRoomRepository = new ChatRoomRepositoryImpl(jpaChatRoomRepository);
        bidRepository = new BidRepositoryImpl(jpaBidRepository);

        final Category 전자기기_카테고리 = new Category("전자기기");
        final Category 전자기기_서브_노트북_카테고리 = new Category("노트북 카테고리");
        final ProfileImage 프로필_이미지 = new ProfileImage("upload.png", "store.png");
        final AuctionImage 경매이미지1 = new AuctionImage("경매이미지1.png", "경매이미지1.png");
        final AuctionImage 경매이미지2 = new AuctionImage("경매이미지2.png", "경매이미지2.png");

        판매자 = User.builder()
                  .name("판매자")
                  .profileImage(프로필_이미지)
                  .reliability(new Reliability(4.7d))
                  .oauthId("12345")
                  .build();
        구매자 = User.builder()
                  .name("구매자")
                  .profileImage(프로필_이미지)
                  .reliability(new Reliability(4.7d))
                  .oauthId("12346")
                  .build();
        경매 = Auction.builder()
                    .seller(판매자)
                    .title("맥북")
                    .description("맥북 팔아요")
                    .subCategory(전자기기_서브_노트북_카테고리)
                    .startPrice(new Price(10_000))
                    .bidUnit(new BidUnit(1_000))
                    .closingTime(LocalDateTime.now())
                    .build();

        입찰 = new Bid(경매, 구매자, new BidPrice(15_000));

        채팅방 = new ChatRoom(경매, 구매자);

        전자기기_카테고리.addSubCategory(전자기기_서브_노트북_카테고리);
        categoryRepository.save(전자기기_카테고리);

        userRepository.save(판매자);
        userRepository.save(구매자);

        경매.addAuctionImages(List.of(경매이미지1, 경매이미지2));
        auctionRepository.save(경매);

        bidRepository.save(입찰);
        경매.updateLastBid(입찰);

        chatRoomRepository.save(채팅방);
    }
}
