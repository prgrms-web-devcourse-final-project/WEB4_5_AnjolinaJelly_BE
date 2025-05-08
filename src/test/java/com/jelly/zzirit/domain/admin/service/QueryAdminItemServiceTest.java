package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemResponse;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueryAdminItemServiceTest {

    @InjectMocks
    private QueryAdminItemService queryAdminItemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemStockRepository itemStockRepository;

    @Test
    void 관리자_상품_조회_상품ID로_조회하면_단일_결과를_Page로_반환한다() {
        // given
        Long itemId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // mock 객체들
        Item item = mock(Item.class);
        ItemStock itemStock = mock(ItemStock.class);
        TypeBrand typeBrand = mock(TypeBrand.class);
        Type type = mock(Type.class);
        Brand brand = mock(Brand.class);

        // 내부 객체 연결 설정
        when(item.getId()).thenReturn(itemId);
        when(item.getTypeBrand()).thenReturn(typeBrand);
        when(typeBrand.getType()).thenReturn(type);
        when(typeBrand.getBrand()).thenReturn(brand);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemStockRepository.findAllByItemIdIn(List.of(itemId))).thenReturn(List.of(itemStock));
        when(itemStock.getItem()).thenReturn(item);

        // when
        PageResponse<AdminItemResponse> response = queryAdminItemService.getItems(null, itemId, pageable);

        // then
        assertNotNull(response); // todo: response에 getter까지 붙여가며 실제로 응답이 하나인지 검증?
        verify(itemRepository).findById(itemId);
        verify(itemStockRepository).findAllByItemIdIn(List.of(itemId));
    }

    @Test
    void 관리자_상품_조회_상품ID로_조회했지만_없으면_빈페이지를_반환한다() {
        // given
        Long itemId = 99L;
        Pageable pageable = PageRequest.of(0, 10);

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // when
        PageResponse<AdminItemResponse> response = queryAdminItemService.getItems(null, itemId, pageable);

        // then
        assertNotNull(response); // todo: response에 getter까지 붙여가며 실제로 응답이 빈 리스트인지 검증?
        verify(itemRepository).findById(itemId);
        verify(itemStockRepository).findAllByItemIdIn(List.of()); // 빈 리스트에 대한 호출
    }

    @Test
    void 관리자_상품_조회_이름으로_조회하면_조건에_맞는_상품들을_반환한다() {
        // given
        String name = "냉장고";
        Pageable pageable = PageRequest.of(0, 10);

        Item item1 = mock(Item.class);
        Item item2 = mock(Item.class);
        when(item1.getId()).thenReturn(1L);
        when(item2.getId()).thenReturn(2L);

        // item 필드 mock
        TypeBrand typeBrand = mock(TypeBrand.class);
        Type type = mock(Type.class);
        Brand brand = mock(Brand.class);
        when(item1.getTypeBrand()).thenReturn(typeBrand);
        when(item2.getTypeBrand()).thenReturn(typeBrand);
        when(typeBrand.getType()).thenReturn(type);
        when(typeBrand.getBrand()).thenReturn(brand);

        // DB 리턴
        when(itemRepository.findAllByNameContainingIgnoreCase(name, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(item1, item2)));

        ItemStock stock1 = mock(ItemStock.class);
        ItemStock stock2 = mock(ItemStock.class);
        when(stock1.getItem()).thenReturn(item1);
        when(stock2.getItem()).thenReturn(item2);

        when(itemStockRepository.findAllByItemIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(stock1, stock2));

        // when
        PageResponse<AdminItemResponse> response = queryAdminItemService.getItems(name, null, pageable);

        // then
        assertNotNull(response); // todo: response에 getter까지 붙여가며 실제로 응답이 2개인지 검증?
        verify(itemRepository).findAllByNameContainingIgnoreCase(name, pageable);
        verify(itemStockRepository).findAllByItemIdIn(List.of(1L, 2L));
    }

    @Test
    void 관리자_상품_조회_필터가_없으면_전체목록을_조회한다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(1L);

        TypeBrand typeBrand = mock(TypeBrand.class);
        Type type = mock(Type.class);
        Brand brand = mock(Brand.class);
        when(item.getTypeBrand()).thenReturn(typeBrand);
        when(typeBrand.getType()).thenReturn(type);
        when(typeBrand.getBrand()).thenReturn(brand);

        when(itemRepository.findAll(pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(item)));

        ItemStock stock = mock(ItemStock.class);
        when(stock.getItem()).thenReturn(item);
        when(itemStockRepository.findAllByItemIdIn(List.of(1L)))
                .thenReturn(List.of(stock));

        // when
        PageResponse<AdminItemResponse> response = queryAdminItemService.getItems(null, null, pageable);

        // then
        assertNotNull(response); // todo: response에 getter까지 붙여가며 실제로 응답이 1개인지 검증?
        verify(itemRepository).findAll(pageable);
        verify(itemStockRepository).findAllByItemIdIn(List.of(1L));
    }
}
