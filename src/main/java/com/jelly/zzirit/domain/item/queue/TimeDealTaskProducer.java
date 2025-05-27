package com.jelly.zzirit.domain.item.queue;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.global.exception.custom.TimeDealQueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.jelly.zzirit.domain.item.entity.ItemStatus.NONE;
import static com.jelly.zzirit.domain.item.entity.ItemStatus.TIME_DEAL;
import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.ENDED;
import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.ONGOING;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealTaskProducer {

    private final BlockingQueue<TimeDealTask> queue;

    public void produce(TimeDeal timeDeal) {
        try { // 딜레이 큐에 상태 변경 작업 추가
            boolean startTaskQueued = queue.offer(
                new TimeDealTask(timeDeal.getId(), ONGOING, TIME_DEAL, timeDeal.getStartTime()),
                3, TimeUnit.SECONDS // 블로킹 시간 3초 제한
            );
            boolean endTaskQueued = queue.offer(
                new TimeDealTask(timeDeal.getId(), ENDED, NONE, timeDeal.getEndTime()),
                3, TimeUnit.SECONDS // 블로킹 시간 3초 제한
            );

            if (!startTaskQueued || !endTaskQueued) {
                throw new TimeDealQueueException("타임 딜 및 상품 상태 변경 작업 추가 실패");
            }

        } catch (InterruptedException e) { // 스레드 종료 요청 수신
            Thread.currentThread().interrupt(); // 인터럽트 플래그 복원
            log.error("타임 딜 및 상품 상태 변경 작업 추가 중 인터럽트 발생: 타임 딜 아이디={}", timeDeal.getId(), e);
            throw new TimeDealQueueException("타임 딜 및 상품 상태 변경 작업 추가 중 예외 발생", e); // 트랜잭션 롤백 유도
        }
    }

}
