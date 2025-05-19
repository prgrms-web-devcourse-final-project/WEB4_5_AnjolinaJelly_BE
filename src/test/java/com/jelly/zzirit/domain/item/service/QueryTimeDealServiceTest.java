package com.jelly.zzirit.domain.item.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.item.dto.request.TimeDealSearchCondition;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse.TimeDealFetchItem;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealQueryRepository;
import com.jelly.zzirit.global.dto.PageResponse;

@ExtendWith(MockitoExtension.class)
class QueryTimeDealServiceTest {

	@Mock
	private TimeDealQueryRepository timeDealQueryRepository;

	@InjectMocks
	private QueryTimeDealService queryTimeDealService;

	@Nested
	@DisplayName("타임딜 단일 조건 검색")
	class Search {
		@Test
		void 검색조건이_없는_경우_전체_리스트를_반환한다() {
			TimeDealSearchCondition condition = TimeDealSearchCondition.from(null, null, null, null,
				TimeDeal.TimeDealStatus.ONGOING);
			given(timeDealQueryRepository.search(condition)).willReturn(List.of(createMockResponse()));

			List<TimeDealFetchResponse> result = queryTimeDealService.search(condition);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).timeDealName()).isEqualTo("노트북타임딜");
		}

		@Test
		void 타임딜_이름으로_검색한다() {
			TimeDealSearchCondition condition = TimeDealSearchCondition.from(
				"노트북타임딜", null, null, null, TimeDeal.TimeDealStatus.ONGOING
			);
			TimeDealFetchResponse mockResponse = createMockResponse();
			given(timeDealQueryRepository.search(condition)).willReturn(List.of(mockResponse));

			List<TimeDealFetchResponse> result = queryTimeDealService.search(condition);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).timeDealName()).isEqualTo("노트북타임딜");
		}

		@Test
		void 타임딜_ID로_검색한다() {
			TimeDealSearchCondition condition = TimeDealSearchCondition.from(
				null, 1L, null, null, TimeDeal.TimeDealStatus.ONGOING
			);
			TimeDealFetchResponse mockResponse = createMockResponse();
			given(timeDealQueryRepository.search(condition)).willReturn(List.of(mockResponse));

			List<TimeDealFetchResponse> result = queryTimeDealService.search(condition);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).timeDealId()).isEqualTo(1L);
		}

		@Test
		void 상품_이름으로_검색한다() {
			TimeDealSearchCondition condition = TimeDealSearchCondition.from(
				null, null, "맥북", null, TimeDeal.TimeDealStatus.ONGOING
			);
			TimeDealFetchResponse mockResponse = createMockResponse();
			given(timeDealQueryRepository.search(condition)).willReturn(List.of(mockResponse));

			List<TimeDealFetchResponse> result = queryTimeDealService.search(condition);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).items().get(0).itemName()).isEqualTo("맥북");
		}

		@Test
		void 상품_ID로_검색한다() {
			TimeDealSearchCondition condition = TimeDealSearchCondition.from(
				null, null, null, 11L, TimeDeal.TimeDealStatus.ONGOING
			);
			TimeDealFetchResponse mockResponse = createMockResponse();
			given(timeDealQueryRepository.search(condition)).willReturn(List.of(mockResponse));

			List<TimeDealFetchResponse> result = queryTimeDealService.search(condition);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).items().get(0).itemId()).isEqualTo(99L);
		}

		private TimeDealFetchResponse createMockResponse() {
			TimeDeal timeDeal = TimeDeal.builder()
				.id(1L)
				.name("노트북타임딜")
				.status(TimeDeal.TimeDealStatus.ONGOING)
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusDays(1))
				.discountRatio(10)
				.build();

			TimeDealFetchItem item = new TimeDealFetchItem(
				99L, "맥북", 100,
				BigDecimal.valueOf(1200000), BigDecimal.valueOf(1000000)
			);
			return TimeDealFetchResponse.from(timeDeal, List.of(item));
		}
	}

	@Nested
	@DisplayName("타임딜 페이징 검색")
	class SearchWithPaging {

		@Test
		void 타임딜_조건과_페이지정보로_페이징_결과를_반환한다() {
			// given
			TimeDealSearchCondition condition = TimeDealSearchCondition.from(
				"노트북타임딜", 1L, "맥북", 11L, TimeDeal.TimeDealStatus.ONGOING
			);

			TimeDeal timeDeal = TimeDeal.builder()
				.id(1L)
				.name("노트북타임딜")
				.status(TimeDeal.TimeDealStatus.ONGOING)
				.startTime(LocalDateTime.now())
				.endTime(LocalDateTime.now().plusDays(1))
				.discountRatio(10)
				.build();

			TimeDealFetchItem item = new TimeDealFetchItem(
				99L,
				"맥북",
				100,
				BigDecimal.valueOf(1200000),
				BigDecimal.valueOf(1000000)
			);

			TimeDealFetchResponse mockResponse = TimeDealFetchResponse.from(timeDeal, List.of(item));

			PageResponse<TimeDealFetchResponse> pageResponse = new PageResponse<>(
				List.of(mockResponse),
				0, 10, 1, 1, true
			);

			given(timeDealQueryRepository.searchWithPaging(condition, 0, 10))
				.willReturn(pageResponse);

			// when
			PageResponse<TimeDealFetchResponse> result = queryTimeDealService.getTimeDeals(
				"노트북타임딜", 1L, "맥북", 11L, TimeDeal.TimeDealStatus.ONGOING, 0, 10
			);

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getTotalPages()).isEqualTo(1);
			assertThat(result.getPageNumber()).isEqualTo(0);
		}
	}
}