package com.jelly.zzirit.domain.item.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.timeDeal.TimeDealCreateItem;
import com.jelly.zzirit.domain.item.dto.timeDeal.TimeDealModalItem;
import com.jelly.zzirit.domain.item.dto.timeDeal.response.SearchTimeDeal;
import com.jelly.zzirit.domain.item.dto.timeDeal.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.domain.timeDeal.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.timeDeal.dto.response.SearchTimeDealItem;
import com.jelly.zzirit.global.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimeDealService {
	private final ItemRepository itemRepository;
	private final TimeDealRepository timeDealRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;

	// 타임딜 생성
	@Transactional
	public TimeDealCreateResponse createTimeDeal(TimeDealCreateRequest request) {

		// 1. 요청 정보로 타임딜을 먼저 저장합니다.(타임딜 아이템 제외)
		TimeDeal timeDeal = timeDealRepository.save(
			new TimeDeal(
				request.title(),
				TimeDeal.TimeDealStatus.SCHEDULED,    // 생성시 동시에 계획됨 상태 설정. 만약 등록 시점이 타임딜 진행시 점이라면?
				request.startTime(),
				request.endTime(),
				request.discountRate()));    // 엔티티 상에선 discountRatio. 추후 통일(우선순위 낮음)

		// 2. 요청으로 들어온 items(id, quantity)와 위에서 저장한 타임딜 정보로 타임딜 아이템을 저장합니다.
		request.items().forEach(item -> {

			// 2-1. 타임딜에 등록된 아이템은 기존 아이템(originItem)내용에 Type만 TIME_DEAL인 새로운 아이템으로 새롭게 저장됩니다.
			// 해당 부분 코드 ItemService로 이동 필요?
			Item originItem = itemRepository.findById(item.itemId()).orElseThrow();    // 해당 상품이 없다면? -> 예외처리 필요.
			Item clonedItemForTimeDeal = itemRepository.save(new Item(
					originItem.getName(),
					originItem.getImageUrl(),
					originItem.getPrice(),
					ItemStatus.TIME_DEAL,    // 타입만 변경
					originItem.getTypeBrand()
				)
			);

			// 2-2. 저장된 타임딜과, 타입이 타임딜인 아이템을 이용해 중간 엔티티인 타임딜 아이템을 저장합니다.

			// 타임딜 할인율 적용 가격 계산
			BigDecimal discountedPrice = clonedItemForTimeDeal.getPrice().multiply(
				BigDecimal.ONE.subtract(BigDecimal.valueOf(timeDeal.getDiscountRatio()).divide(BigDecimal.valueOf(100)))
			);

			// 타임딜 아이템 저장
			timeDealItemRepository.save(new TimeDealItem(discountedPrice, timeDeal, clonedItemForTimeDeal));

			// 2-3. 요청에 포함된 quantity을 이용해 상품 재고를 저장합니다.
			itemStockRepository.save(new ItemStock(clonedItemForTimeDeal, item.quantity(), item.quantity()));
		});

		// 응답
		List<TimeDealCreateItem> responseItems =
			timeDealItemRepository.findAllByTimeDeal(timeDeal).stream()
				.map(tdi -> {
					Long itemId = tdi.getItem().getId();
					int quantity = itemStockRepository.findByItemId(itemId)
						.map(ItemStock::getQuantity)
						.orElse(0);
					return new TimeDealCreateItem(itemId, quantity);
				}).toList();

		return new TimeDealCreateResponse(
			timeDeal.getId(),
			timeDeal.getName(),
			timeDeal.getStartTime().toString(),
			timeDeal.getEndTime().toString(),
			timeDeal.getDiscountRatio(),
			responseItems
		);
	}

	// 타임딜 등록 모달 생성
	public List<TimeDealModalItem> getModalItems(List<Long> itemIds) {
		return itemRepository.findAllById(itemIds).stream()
			.map(item -> new TimeDealModalItem(item.getId(), item.getName(),
				new BigDecimal(String.valueOf(item.getPrice())).intValue()))
			.toList();
	}

	// 타임틸 관리자 조회
	public PageResponse<SearchTimeDeal> getTimeDeals(
		String timeDealName,
		Long timeDealId,
		String timeDealItemName,
		Long timeDealItemId,
		TimeDeal.TimeDealStatus status,
		int page,
		int size
	) {
		List<SearchTimeDeal> result = new ArrayList<>();
		List<TimeDeal> timeDeals;
		List<TimeDealItem> timeDealItems = new ArrayList<>();

		// 1. 타임딜 이름으로 검색 + 타임딜 상태 필터 적용
		if (timeDealName != null && !timeDealName.isEmpty()) {

			// 입력된 키워드가 포함된 제목을 가진 타임딜 리스트 db 조회
			timeDeals = timeDealRepository.findByNameContaining(timeDealName);

			timeDeals.forEach(timeDeal -> {
				// 타임딜 상태 필터링
				if (status != null && timeDeal.getStatus() != status)
					return;

				// 타임딜에 포함된 아이템 리스트 조회 후 반환 형식 변환
				List<TimeDealItem> tdItems = timeDealItemRepository.findAllByTimeDeal(timeDeal);
				List<SearchTimeDealItem> items = tdItems.stream()
					.map(this::toSearchTimeDealItem)
					.toList();
				result.add(toSearchTimeDeal(timeDeal, items));
			});
		}

		// 2. 타임딜 아이디로 검색 + 타임딜 상태 필터 적용
		if (timeDealId != null) {

			TimeDeal timeDeal = timeDealRepository.findById(timeDealId).orElse(null);

			// 타임딜 상태 필터링
			if (timeDeal != null && (status == null || timeDeal.getStatus() == status)) {

				// 타임딜에 포함된 아이템 리스트 조회 후 반환 형식 변환
				List<TimeDealItem> tdItems = timeDealItemRepository.findAllByTimeDeal(timeDeal);
				List<SearchTimeDealItem> items = tdItems.stream()
					.map(this::toSearchTimeDealItem)
					.toList();
				result.add(toSearchTimeDeal(timeDeal, items));
			}
		}

		// 3. 아이템 이름으로 검색 + 타임딜 상태 필터 적용
		if (timeDealItemName != null && !timeDealItemName.isEmpty()) {

			// 입력된 키워드가 포함된 제목을 가진 아이템 리스트 db 조회
			timeDealItems = timeDealItemRepository.findByItem_NameContaining(timeDealItemName);

			timeDealItems.forEach(timeDealItem -> {

				// 해당 아이템이 포함된 타임딜 정보 조회
				TimeDeal timeDeal = timeDealItem.getTimeDeal();
				if (status != null && timeDeal.getStatus() != status)
					return;

				// 검색한 아이템과 해당 타임딜 정보로 반환 형식 변환
				List<SearchTimeDealItem> items = List.of(toSearchTimeDealItem(timeDealItem));
				result.add(toSearchTimeDeal(timeDeal, items));
			});
		}

		// 4. 아이템 아이디로 검색 + 타임딜 상태 필터 적용 (주의. 타임딜 아이템 아이디가 아니라 아이템 테이블의 아이디이다.)
		if (timeDealItemId != null) {
			TimeDealItem timeDealItem = timeDealItemRepository.findTimeDealItemById(timeDealItemId);
			if (timeDealItem != null) {
				TimeDeal timeDeal = timeDealItem.getTimeDeal();
				if (status == null || timeDeal.getStatus() == status) {
					List<SearchTimeDealItem> items = List.of(toSearchTimeDealItem(timeDealItem));
					result.add(toSearchTimeDeal(timeDeal, items));
				}
			}
		}

		// 페이징 처리
		int start = page * size;
		int end = Math.min(start + size, result.size());
		List<SearchTimeDeal> pagedResult = result.subList(start, end);

		return new PageResponse<>(
			pagedResult,
			page,
			size,
			result.size(),
			(int)Math.ceil((double)result.size() / size),
			end >= result.size()
		);
	}

	// 타임딜 아이템을 응답 형식으로 변환
	private SearchTimeDealItem toSearchTimeDealItem(TimeDealItem timeDealItem) {
		int quantity = itemStockRepository.findByItemId(timeDealItem.getItem().getId())
			.map(ItemStock::getQuantity)
			.orElse(0);
		return new SearchTimeDealItem(
			timeDealItem.getId(),
			timeDealItem.getItem().getName(),
			quantity,
			timeDealItem.getItem().getPrice(),
			timeDealItem.getPrice()
		);
	}

	private SearchTimeDeal toSearchTimeDeal(TimeDeal timeDeal, List<SearchTimeDealItem> items) {
		return new SearchTimeDeal(
			timeDeal.getId(),
			timeDeal.getName(),
			timeDeal.getStartTime(),
			timeDeal.getEndTime(),
			timeDeal.getStatus(),
			timeDeal.getDiscountRatio(),
			items
		);
	}
}