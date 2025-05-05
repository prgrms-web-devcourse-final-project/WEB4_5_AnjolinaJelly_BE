package com.jelly.zzirit.domain.adminItem.service;

import com.jelly.zzirit.domain.adminItem.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.TypeBrand;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TypeBrandRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandAdminItemService {
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;
    private final TypeRepository typeRepository;
    private final BrandRepository brandRepository;
    private final TypeBrandRepository typeBrandRepository;

    @Transactional // 트랜잭션
    public Empty createItem(ItemCreateRequest request) {
        TypeBrand typeBrand = typeBrandRepository.findByTypeIdAndBrandId(request.typeId(), request.brandId())
                .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.TYPE_BRAND_NOT_FOUND)); // todo: 예외 처리 괜춘?

        // 상품생성요청 dto -> 상품 객체 생성, 저장
        Item item = request.toItemEntity(typeBrand);
        itemRepository.save(item); // save 후 item 객체에 id 채워짐

        // 상품생성요청 dto -> 재고 객체 생성, 저장
        ItemStock itemStock = request.toItemStockEntity(item);
        itemStockRepository.save(itemStock);

        return Empty.getInstance(); // 싱글턴 - getInstance() 사용
    }

    @Transactional
    public Empty updateItem(@NotNull Long itemId, ItemCreateRequest request) {

        // 상품 조회, dto 보고 타입과 브랜드 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
        TypeBrand typeBrand = typeBrandRepository.findByTypeIdAndBrandId(request.typeId(), request.brandId())
                .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.TYPE_BRAND_NOT_FOUND)); // todo: 예외 처리 괜춘?

        // 상품 업데이트
        item.update(request, typeBrand);

        // 상품 재고 조회
        ItemStock itemStock = itemStockRepository.findByItemId(itemId)
            .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND));

        // 상품 재고 업데이트
        itemStock.update(request, item);

        return Empty.getInstance();
    }

    @Transactional
    public Empty deleteItem(@NotNull Long itemId) {
        // todo: 삭제 검증 로직 논의 필요 - 이미 판매된 상품은 삭제 불가 등
        // todo: soft delete 사용할지 논의 필요
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
        ItemStock itemStock = itemStockRepository.findByItemId(itemId)
            .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND));

        itemStockRepository.delete(itemStock);
        itemRepository.delete(item);

        return Empty.getInstance();
    }
}