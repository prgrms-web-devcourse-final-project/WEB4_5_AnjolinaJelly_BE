package com.jelly.zzirit.domain.adminItem.service;

import com.jelly.zzirit.domain.adminItem.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.BrandRepository;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TypeRepository;
import com.jelly.zzirit.domain.order.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandAdminItemService {
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;
    private final TypeRepository typeRepository;
    private final BrandRepository brandRepository;

    @Transactional // 트랜잭션
    public Empty createItem(ItemCreateRequest request) {
        Type type = typeRepository.findById(request.typeId())
                .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.TYPE_NOT_FOUND)); // todo: 예외 처리 괜춘?
        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new InvalidItemException(BaseResponseStatus.BRAND_NOT_FOUND));

        Item item = request.toItemEntity(type, brand);
        itemRepository.save(item); // save 후 item 객체에 id 채워짐

        ItemStock itemStock = request.toItemStockEntity(item);
        itemStockRepository.save(itemStock);

        return Empty.getInstance(); // 싱글턴 - getInstance() 사용
    }
    
}
