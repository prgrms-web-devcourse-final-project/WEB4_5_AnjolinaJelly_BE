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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
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
                .orElse(LocalDateTime.of(2025, 5, 21, 18, 0));
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

        Item item = itemRepository.getReferenceById(1L);
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
