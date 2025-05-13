package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueryAdminServiceTest {

    @InjectMocks
    private QueryAdminService queryAdminService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemStockRepository itemStockRepository;

    @DisplayName("관리자 상품 조회 - 상품 ID로 조회하면 단일 결과를 Page로 반환한다")
    @Test
    void 관리자_상품_조회_상품ID로_조회하면_단일_결과를_Page로_반환한다() {
        // given
        Long itemId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        AdminItemFetchResponse mockDto = new AdminItemFetchResponse(
                itemId,
                "아이폰 15",
                "https://example.com/image.png",
                "스마트폰",
                "애플",
                new BigDecimal("1500000"),
                100
        );
        Page<AdminItemFetchResponse> mockPage = new PageImpl<>(List.of(mockDto), pageable, 1);

        // when - itemId 기준으로만 조회
        when(itemRepository.searchItemById(eq(itemId), eq(pageable))).thenReturn(mockPage);

        // 실행
        PageResponse<AdminItemFetchResponse> response = queryAdminService.getSearchItems(itemId, null, pageable);

        // then
        assertNotNull(response); // todo: response에 getter까지 붙여가며 실제로 응답이 빈 리스트인지 검증?
        assertEquals(1, response.getContent().size());
        assertEquals("아이폰 15", response.getContent().get(0).name()); // getter 혹은 record 필드 접근

        verify(itemRepository).searchItemById(itemId, pageable);
    }


    @DisplayName("관리자 상품 조회 - 상품 ID로 조회했지만 없으면 빈 페이지를 반환한다")
    @Test
    void 관리자_상품_조회_상품ID로_조회했지만_없으면_빈페이지를_반환한다() {
        // given
        Long itemId = 99L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminItemFetchResponse> emptyPage = Page.empty(pageable);

        when(itemRepository.searchItemById(eq(itemId), eq(pageable))).thenReturn(emptyPage);

        // when
        PageResponse<AdminItemFetchResponse> response = queryAdminService.getSearchItems(itemId, null, pageable);

        // then
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        verify(itemRepository).searchItemById(itemId, pageable);
    }


    @DisplayName("관리자 상품 조회 - 이름으로 조회하면 조건에 맞는 상품들을 반환한다")
    @Test
    void 관리자_상품_조회_이름으로_조회하면_조건에_맞는_상품들을_반환한다() {
        // given
        String name = "냉장고";
        Pageable pageable = PageRequest.of(0, 10);

        AdminItemFetchResponse dto1 = new AdminItemFetchResponse(
                1L, "삼성 냉장고", "https://img1.png", "냉장고", "삼성", new BigDecimal("1000000"), 50
        );
        AdminItemFetchResponse dto2 = new AdminItemFetchResponse(
                2L, "LG 냉장고", "https://img2.png", "냉장고", "LG", new BigDecimal("1200000"), 30
        );
        Page<AdminItemFetchResponse> resultPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        when(itemRepository.searchItemsByName(eq(name), eq(pageable))).thenReturn(resultPage);

        // when
        PageResponse<AdminItemFetchResponse> response = queryAdminService.getSearchItems(null, name, pageable);

        // then
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals("삼성 냉장고", response.getContent().get(0).name());
        assertEquals("LG 냉장고", response.getContent().get(1).name());

        verify(itemRepository).searchItemsByName(name, pageable);
    }


    @DisplayName("관리자 상품 조회 - 필터가 없으면 전체 목록을 조회한다")
    @Test
    void 관리자_상품_조회_필터가_없으면_전체목록을_조회한다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        AdminItemFetchResponse dto = new AdminItemFetchResponse(
                1L, "아이폰", "https://img.png", "스마트폰", "애플", new BigDecimal("1500000"), 100
        );
        Page<AdminItemFetchResponse> resultPage = new PageImpl<>(List.of(dto), pageable, 1);

        when(itemRepository.findAllItems(eq(pageable))).thenReturn(resultPage);

        // when
        PageResponse<AdminItemFetchResponse> response = queryAdminService.getSearchItems(null, null, pageable);

        // then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("아이폰", response.getContent().get(0).name());

        verify(itemRepository).findAllItems(pageable);
    }
}
