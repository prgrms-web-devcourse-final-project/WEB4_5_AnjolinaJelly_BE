package com.jelly.zzirit.domain.admin.service;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandAdminItemServiceTest {
    @InjectMocks
    private CommandAdminItemService commandAdminItemService;

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemStockRepository itemStockRepository;
    @Mock
    private TypeBrandRepository typeBrandRepository;
    @Mock
    private S3Service s3Service;

    @Test
    void 이미지가_없으면_예외를_던진다() {
        // given: 빈 이미지 URL로 생성 요청을 구성
        ItemCreateRequest request = new ItemCreateRequest(
                "상품 이름", 30, new BigDecimal("100000"), 1L, 1L, ""
        );

        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.createItem(request));
        assertEquals(BaseResponseStatus.IMAGE_REQUIRED, exception.getStatus());
    }

    @Test
    void TypeBrand가_없으면_예외를_던진다() {
        // given: 정상적인 이미지와 요청 데이터 구성
        ItemCreateRequest request = new ItemCreateRequest(
                "상품 이름", 30, new BigDecimal("100000"), 1L, 1L, "http://image.com/item.jpg"
        );

        // TypeBrandRepository가 빈 값을 반환하도록 설정
        when(typeBrandRepository.findByTypeIdAndBrandId(1L, 1L)).thenReturn(Optional.empty());

        // when & then: 예외 발생을 검증
        InvalidItemException exception = assertThrows(InvalidItemException.class,
                () -> commandAdminItemService.createItem(request));
        assertEquals(BaseResponseStatus.TYPE_BRAND_NOT_FOUND,exception.getStatus());
    }

    @Test
    void 정상적인_요청이면_Item과_Stock을_저장하고_Empty를_반환한다() {
        // given: 가짜 TypeBrand 객체 생성
        TypeBrand typeBrand = mock(TypeBrand.class);

        // 가짜 Item, ItemStock 객체 생성
        Item item = mock(Item.class);
        ItemStock itemStock = mock(ItemStock.class);

        //정상적인 이미지와 요청 데이터 구성
        ItemCreateRequest request = mock(ItemCreateRequest.class);
        when(request.imageUrl()).thenReturn("http://image.com/item.jpg");
        when(request.typeId()).thenReturn(1L);
        when(request.brandId()).thenReturn(1L);
        when(request.toItemEntity(any())).thenReturn(item);
        when(request.toItemStockEntity(any())).thenReturn(itemStock);

        // request의 toItemEntity(), toItemStockEntity()가 새로운 객체를 생성한다고 가정
        when(typeBrandRepository.findByTypeIdAndBrandId(1L, 1L)).thenReturn(Optional.of(typeBrand));
        when(request.toItemEntity(typeBrand)).thenReturn(item);
        when(request.toItemStockEntity(item)).thenReturn(itemStock);

        // when: 서비스 호출
        Empty result = commandAdminItemService.createItem(request);

        // then: 저장 메서드가 각각 한 번씩 호출됐는지, 반환값이 Empty인지 검증
        verify(itemRepository).save(item);              // 상품 저장 확인
        verify(itemStockRepository).save(itemStock);    // 재고 저장 확인
        assertSame(Empty.getInstance(), result);        // 반환값이 Empty 싱글턴인지 확인
    }
}
