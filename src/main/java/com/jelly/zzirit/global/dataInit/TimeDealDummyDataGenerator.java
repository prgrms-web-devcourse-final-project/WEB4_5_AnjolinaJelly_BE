package com.jelly.zzirit.global.dataInit;

import com.jelly.zzirit.domain.item.entity.*;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.*;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealDummyDataGenerator{

    private final BrandRepository brandRepository;
    private final TypeRepository typeRepository;
    private final TypeBrandRepository typeBrandRepository;
    private final TimeDealRepository timeDealRepository;
    private final TimeDealItemRepository timeDealItemRepository;
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public void generateInitialData() {
        long totalCount = timeDealRepository.count();

        if (totalCount < 20000) {
            int toCreate = (int) (20000 - totalCount);
            log.info("📦 타임딜 부족: {}개 → {}개 추가 생성", totalCount, toCreate);
            generateDeals(toCreate);
        } else if (totalCount > 20000) {
            int toDelete = (int) (totalCount - 20000);
            log.info("🧹 타임딜 과잉: {}개 → {}개 삭제", totalCount, toDelete);
            timeDealRepository.deleteTopNByIdDesc(toDelete);
        } else {
            log.info("✅ 타임딜 개수 정확함: 20,000개");
        }
    }

    private void generateDeals(int count) {
        List<TimeDeal> timeDeals = new ArrayList<>();
        List<TimeDealItem> timeDealItems = new ArrayList<>();
        List<ItemStock> itemStocks = new ArrayList<>();

        LocalDateTime latestEndTime = timeDealRepository.findMaxEndTime()
                .orElse(LocalDateTime.now());
        ZonedDateTime baseEndTime = latestEndTime.atZone(KST).plusMinutes(10);

        for (int i = 0; i < count; i++) {
            ZonedDateTime dealStart = baseEndTime.plusMinutes(20L * i);
            ZonedDateTime dealEnd = dealStart.plusMinutes(10);

            TimeDeal timeDeal = TimeDeal.builder()
                    .name("타임딜 " + (i + 1))
                    .startTime(dealStart.toLocalDateTime())
                    .endTime(dealEnd.toLocalDateTime())
                    .discountRatio(20)
                    .status(TimeDeal.TimeDealStatus.SCHEDULED)
                    .build();
            timeDeals.add(timeDeal);
        }

        timeDealRepository.saveAll(timeDeals);

        // Item(1L)이 없으면 관련 엔티티들 생성
        Item item;
        if (!itemRepository.existsById(1L)) {
            log.info("📌 Item ID 1이 존재하지 않아 더미 엔티티 생성 중");

            Brand dummyBrand = Brand.builder()
                    .name("더미 브랜드")
                    .build();

            Type dummyType = Type.builder()
                    .name("더미 종류")
                    .build();

            TypeBrand dummyTypeBrand = TypeBrand.builder()
                    .brand(dummyBrand)
                    .type(dummyType)
                    .build();

            item = Item.builder()
                    .name("더미 상품")
                    .price(BigDecimal.valueOf(10000))
                    .imageUrl("https://dummy.image.url/item.jpg")
                    .itemStatus(ItemStatus.NONE)
                    .typeBrand(dummyTypeBrand)
                    .build();

            ItemStock itemStock = ItemStock.builder()
                    .item(item)
                    .quantity(9999)
                    .soldQuantity(0)
                    .build();

            // save 순서 주의: 자식보다 부모 먼저!
            brandRepository.save(dummyBrand);
            typeRepository.save(dummyType);
            typeBrandRepository.save(dummyTypeBrand);
            itemRepository.save(item); // Cascade가 없으면 따로 다 저장해도 됨
            itemStockRepository.save(itemStock);

            log.info("✅ 더미 상품 및 재고 데이터 생성 완료");
        } else {
            item = itemRepository.getReferenceById(1L);
        }

        BigDecimal discountedPrice = item.getPrice()
                .multiply(BigDecimal.valueOf(0.8)); // 20% 할인

        for (TimeDeal timeDeal : timeDeals) {
            TimeDealItem timeDealItem = TimeDealItem.builder()
                    .price(discountedPrice)
                    .timeDeal(timeDeal)
                    .item(item)
                    .build();
            timeDealItems.add(timeDealItem);

            ItemStock itemStock = ItemStock.builder()
                    .timeDealItem(timeDealItem)
                    .quantity(10)
                    .soldQuantity(0)
                    .build();
            itemStocks.add(itemStock);
        }

        timeDealItemRepository.saveAll(timeDealItems);
        itemStockRepository.saveAll(itemStocks);

        log.info("🎉 타임딜 더미 데이터 생성 완료!");
    }
}
