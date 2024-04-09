package com.ddang.ddang.auction.application;

import com.ddang.ddang.auction.application.dto.CreateAuctionDto;
import com.ddang.ddang.auction.application.dto.CreateInfoAuctionDto;
import com.ddang.ddang.auction.application.dto.ReadAuctionDto;
import com.ddang.ddang.auction.application.dto.ReadAuctionsDto;
import com.ddang.ddang.auction.application.exception.AuctionNotFoundException;
import com.ddang.ddang.auction.application.exception.UserForbiddenException;
import com.ddang.ddang.auction.application.fixture.AuctionServiceFixture;
import com.ddang.ddang.auction.domain.Auction;
import com.ddang.ddang.auction.presentation.dto.request.ReadAuctionSearchCondition;
import com.ddang.ddang.bid.domain.Bid;
import com.ddang.ddang.category.application.exception.CategoryNotFoundException;
import com.ddang.ddang.configuration.IsolateDatabase;
import com.ddang.ddang.fixture.AuctionDtoFixture;
import com.ddang.ddang.fixture.AuctionFixture;
import com.ddang.ddang.fixture.TestFixtureBuilder;
import com.ddang.ddang.image.domain.StoreImageProcessor;
import com.ddang.ddang.region.application.exception.RegionNotFoundException;
import com.ddang.ddang.user.application.exception.UserNotFoundException;
import org.assertj.core.api.*;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.List;

import static com.ddang.ddang.fixture.AuctionDtoFixture.*;
import static com.ddang.ddang.fixture.AuctionDtoFixture.경매_생성_dto_생성;
import static com.ddang.ddang.fixture.AuctionFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@IsolateDatabase
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class AuctionServiceRefactorTest extends AuctionServiceFixture {

    @MockBean
    StoreImageProcessor imageProcessor;

    @Autowired
    AuctionService auctionService;

    @Autowired
    TestFixtureBuilder fixtureBuilder;

    @Test
    void 경매를_등록한다() {
        // given
        given(imageProcessor.storeImageFiles(any())).willReturn(List.of(경매_이미지_엔티티));
        final Auction 경매 = 기본_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리);
        // TODO: 2024-04-7 혹은 경매와 동일한 매개변수를 갖는 생성자를 만들어도 무관, 일단은 코드의 중복같아서 현재처럼 진행함 (enum으로 변경한다는 의미)

        final CreateAuctionDto 경매_생성_dto = 경매_생성_dto_생성(경매, List.of(경매_이미지_파일));

        // when
        final CreateInfoAuctionDto actual = auctionService.create(경매_생성_dto);

        // then
        assertThat(actual.id()).isPositive();
    }

    @Test
    void 지정한_아이디에_대한_판매자가_없는_경우_경매를_등록하면_예외가_발생한다() {
        // given
        final Auction 경매 = 기본_경매.생성(List.of(역삼동), 가구_서브_의자_카테고리);
        final Long 존재하지_않는_사용자_ID = -999L;
        final CreateAuctionDto 존재하지_않는_판매자의_경매_생성_dto = 경매_생성_dto_생성(경매, 존재하지_않는_사용자_ID, List.of(경매_이미지_파일));

        // when & then
        assertThatThrownBy(() -> auctionService.create(존재하지_않는_판매자의_경매_생성_dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("지정한 판매자를 찾을 수 없습니다.");
    }

    @Test
    void 지정한_아이디에_해당하는_지역이_없을때_경매를_등록하면_예외가_발생한다() {
        final Auction 경매 = 기본_경매.생성(판매자, 가구_서브_의자_카테고리);
        final Long 존재하지_않는_지역_ID = -999L;
        final CreateAuctionDto 존재하지_않는_지역의_경매_생성_dto = 경매_생성_dto_생성(경매, List.of(존재하지_않는_지역_ID), List.of(경매_이미지_파일));

        // when & then
        assertThatThrownBy(() -> auctionService.create(존재하지_않는_지역의_경매_생성_dto))
                .isInstanceOf(RegionNotFoundException.class)
                .hasMessage("지정한 세 번째 지역이 없습니다.");
    }

    @Test
    void 지정한_아이디에_해당하는_지역이_세_번째_지역이_아닐_떄_경매를_등록하면_예외가_발생한다() {
        final Auction 경매 = 기본_경매.생성(판매자, List.of(강남구), 가구_서브_의자_카테고리);
        final CreateAuctionDto 두_번째_지역의_경매_생성_dto = 경매_생성_dto_생성(경매, List.of(경매_이미지_파일));

        // when & then
        assertThatThrownBy(() -> auctionService.create(두_번째_지역의_경매_생성_dto))
                .isInstanceOf(RegionNotFoundException.class)
                .hasMessage("지정한 세 번째 지역이 없습니다.");
    }

//    @Test
//    void 지정한_아이디에_해당하는_카테고리가_없을때_경매를_등록하면_예외가_발생한다() {
//        final Auction 경매 = 기본_경매.생성(List.of(강남구), 가구_서브_의자_카테고리);
//        final Long 존재하지_않는_카테고리_ID = -999L;
//        final CreateAuctionDto 두_번째_지역의_경매_생성_dto = 경매_생성_dto_생성(경매, List.of(경매_이미지_파일));
//
//        // when & then
//        assertThatThrownBy(() -> auctionService.create(존재하지_않는_카테고리의_경매_생성_dto))
//                .isInstanceOf(CategoryNotFoundException.class)
//                .hasMessage("지정한 하위 카테고리가 없거나 하위 카테고리가 아닙니다.");
//    }

    @Test
    void 지정한_아이디에_해당하는_카테고리가_서브_카테고리가_아닐_떄_경매를_등록하면_예외가_발생한다() {
        final Auction 경매 = 기본_경매.생성(판매자, List.of(강남구), 가구_카테고리);
        final CreateAuctionDto 메인_카테고리의_경매_생성_dto = 경매_생성_dto_생성(경매, List.of(경매_이미지_파일));

        // when & then
        assertThatThrownBy(() -> auctionService.create(메인_카테고리의_경매_생성_dto))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("지정한 하위 카테고리가 없거나 하위 카테고리가 아닙니다.");
    }

    @Test
    void 지정한_아이디에_해당하는_경매를_조회한다() {
        // given
        final Auction 경매 = fixtureBuilder.buildAuction(기본_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));

        // when
        final ReadAuctionDto actual = auctionService.readByAuctionId(경매.getId());

        // then
        assertThat(actual.id()).isEqualTo(경매.getId());
    }

    @Test
    void 지정한_아이디에_해당하는_경매가_없는_경매를_조회시_예외가_발생한다() {
        // given
        final Long 존재하지_않는_경매_ID = -999L;

        // when & then
        assertThatThrownBy(() -> auctionService.readByAuctionId(존재하지_않는_경매_ID))
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessage("지정한 아이디에 대한 경매를 찾을 수 없습니다.");
    }

    @Test
    void 첫번째_페이지의_경매_목록을_조회한다() {
        // given
        final Auction 경매 = fixtureBuilder.buildAuction(기본_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));

        // when
        final ReadAuctionsDto actual = auctionService.readAllByCondition(
                PageRequest.of(0, 1, Sort.by(Order.desc("id"))),
                new ReadAuctionSearchCondition(null)
        );

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            final List<ReadAuctionDto> actualReadAuctionDtos = actual.readAuctionDtos();
            softAssertions.assertThat(actualReadAuctionDtos).hasSize(1);
            softAssertions.assertThat(actualReadAuctionDtos.get(0).title()).isEqualTo(경매.getTitle());
        });
    }

    @Test
    void 지정한_아이디에_해당하는_경매를_삭제한다() {
        // given
        final Auction 경매 = fixtureBuilder.buildAuction(종료가_3일_뒤인_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));

        // when & then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThatCode(() -> auctionService.deleteByAuctionId(경매.getId(), 판매자.getId())).doesNotThrowAnyException();
            assertThatThrownBy(() -> auctionService.readByAuctionId(경매.getId()))
                    .isInstanceOf(AuctionNotFoundException.class)
                    .hasMessage("지정한 아이디에 대한 경매를 찾을 수 없습니다.");
        });
    }

    @Test
    void 지정한_아이디에_해당하는_경매가_없는_경매를_삭제시_예외가_발생한다() {
        // given
        final Long 존재하지_않는_경매_ID = -999L;

        // when & then
        assertThatThrownBy(() -> auctionService.deleteByAuctionId(존재하지_않는_경매_ID, 판매자.getId()))
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessage("지정한 아이디에 대한 경매를 찾을 수 없습니다.");
    }

    @Test
    void 지정한_아이디에_해당하는_회원이_없는_경우_삭제시_예외가_발생한다() {
        // given
        final Auction 경매 = fixtureBuilder.buildAuction(종료가_3일_뒤인_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));
        final Long 존재하지_않는_사용자_ID = -999L;

        // when & then
        assertThatThrownBy(() -> auctionService.deleteByAuctionId(경매.getId(), 존재하지_않는_사용자_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("회원 정보를 찾을 수 없습니다.");
    }

    @Test
    void 지정한_아이디에_해당하는_회원과_판매자가_일치하지_않는_경우_삭제시_예외가_발생한다() {
        // given
        final Auction 경매 = fixtureBuilder.buildAuction(기본_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));

        // when & then
        assertThatThrownBy(() -> auctionService.deleteByAuctionId(경매.getId(), 구매자.getId()))
                .isInstanceOf(UserForbiddenException.class)
                .hasMessage("권한이 없습니다.");
    }

    @Test
    void 회원이_등록한_경매_목록을_조회한다() {
        // given
        final PageRequest pageRequest = PageRequest.of(0, 3);
        final Auction 경매1 = fixtureBuilder.buildAuction(기본_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));
        final Auction 경매2 = fixtureBuilder.buildAuction(종료가_3일_뒤인_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));
        final Auction 경매3 = fixtureBuilder.buildAuction(이미_종료된_경매.생성(판매자, List.of(역삼동), 가구_서브_의자_카테고리));

        // when
        final ReadAuctionsDto actual = auctionService.readAllByUserId(판매자.getId(), pageRequest);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.readAuctionDtos()).hasSize(3);
            softAssertions.assertThat(actual.readAuctionDtos().get(0).id()).isEqualTo(경매3.getId());
            softAssertions.assertThat(actual.readAuctionDtos().get(1).id()).isEqualTo(경매2.getId());
            softAssertions.assertThat(actual.readAuctionDtos().get(2).id()).isEqualTo(경매1.getId());
        });
    }

    // TODO: 2024-04-9 입찰은 픽스처가 없어서 애매해서 진행 x
    @Test
    void 회원이_참여한_경매_목록을_조회한다() {
        // given
        final PageRequest pageRequest = PageRequest.of(0, 3);

        // when
        final ReadAuctionsDto actual = auctionService.readAllByBidderId(구매자.getId(), pageRequest);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.readAuctionDtos()).hasSize(2);
            softAssertions.assertThat(actual.readAuctionDtos().get(0).id()).isEqualTo(구매자가_입찰한_경매2.getId());
            softAssertions.assertThat(actual.readAuctionDtos().get(1).id()).isEqualTo(구매자가_입찰한_경매1.getId());
        });
    }
}
