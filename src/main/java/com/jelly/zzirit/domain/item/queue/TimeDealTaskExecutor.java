package com.jelly.zzirit.domain.item.queue;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealTaskExecutor {

    private final TimeDealRepository timeDealRepository;
    private final TimeDealItemRepository timeDealItemRepository;

    @Transactional
    public void execute(TimeDealTask task) {
        log.info("[1] execute() 진입: {}", task);

        // 타임 딜 조회
        timeDealRepository.findById(task.timeDealId())
            .ifPresent(timeDeal -> {
                // 이미 상태가 업데이트된 경우 중복 업데이트 방지
                if (timeDeal.getStatus() == task.nextTimeDealStatus()) {
                    return;
                }

                // 타임 딜 상태 업데이트
                timeDeal.updateStatus(task.nextTimeDealStatus());

                // 타임 딜 상품 및 상품 조회
                List<TimeDealItem> timeDealItems = timeDealItemRepository.findAllWithItemsByTimeDeal(timeDeal);

                // 상품 상태 업데이트
                timeDealItems.forEach(timeDealItem ->
                    timeDealItem.getItem().changeItemStatus(task.nextItemStatus())
                );

                log.info("[2] 딜레이 큐를 이용한 상태 변경 작업 완료: {}", task);
            });

        // 타임 딜이 존재하지 않으면 롤백된 작업으로 간주하고 상태 변경 생략
    }

}
