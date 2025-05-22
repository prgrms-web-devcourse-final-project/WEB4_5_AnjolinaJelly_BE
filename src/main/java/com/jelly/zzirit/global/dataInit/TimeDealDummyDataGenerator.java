package com.jelly.zzirit.global.dataInit;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TimeDealDummyDataGenerator implements CommandLineRunner {

    private final TimeDealRepository timeDealRepository;
    private final TimeDealItemRepository timeDealItemRepository;
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional
    public void run(String... args) {
        long totalCount = timeDealRepository.count();

        if (totalCount < 20000) {
            int toCreate = (int) (20000 - totalCount);
            System.out.println("📦 타임딜 부족: " + totalCount + "개 → " + toCreate + "개 추가 생성");
            generateDeals(toCreate);
        } else if (totalCount > 20000) {
            int toDelete = (int) (totalCount - 20000);
            System.out.println("🧹 타임딜 과잉: " + totalCount + "개 → " + toDelete + "개 삭제");
            timeDealRepository.deleteTopNByIdDesc(toDelete);
        } else {
            System.out.println("✅ 타임딜 개수 정확함: 20,000개");
        }
    }

    private void generateDeals(int count) {
        LocalDateTime latestEndTime = timeDealRepository.findMaxEndTime()
                .orElse(LocalDateTime.of(2025, 5, 21, 18, 0));
        ZonedDateTime baseEndTime = latestEndTime.atZone(KST).plusMinutes(10);

        List<TimeDeal> timeDeals = new ArrayList<>();
        List<TimeDealItem> timeDealItems = new ArrayList<>();
        List<ItemStock> itemStocks = new ArrayList<>();

        Item originalItem = itemRepository.getReferenceById(1L);

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

        Item clonedItem = itemRepository.save(Item.from(originalItem));
        BigDecimal discountedPrice = clonedItem.getPrice()
                .multiply(BigDecimal.valueOf(0.8)); // 20% 할인
        ItemStock itemStock = ItemStock.builder()
                .item(clonedItem)
                .quantity(10)
                .soldQuantity(0)
                .build();
        itemStocks.add(itemStock);
        itemStockRepository.saveAll(itemStocks);

        for (TimeDeal timeDeal : timeDeals) {
            TimeDealItem timeDealItem = TimeDealItem.builder()
                    .price(discountedPrice)
                    .timeDeal(timeDeal)
                    .item(clonedItem)
                    .build();
            timeDealItems.add(timeDealItem);
        }

        timeDealItemRepository.saveAll(timeDealItems);

        System.out.println("🎉 타임딜 더미 데이터 생성 완료!");
    }
}
