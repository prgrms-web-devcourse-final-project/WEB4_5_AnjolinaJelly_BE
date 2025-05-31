package com.jelly.zzirit.domain.item.queue;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealQueueService {

    private final TimeDealRepository timeDealRepository;
    private final TimeDealTaskProducer timeDealTaskProducer;

    public void enqueueTimeDealTasksInPeriod(LocalDateTime start, LocalDateTime end) {
        // 시작 일시 또는 종료 일시가 이번 달에 포함되는 타임 딜 목록 조회
        List<TimeDeal> timeDeals =
            timeDealRepository.findAllByStartOrEndInPeriod(start, end);

        timeDeals.forEach(timeDeal -> {
            try {
                timeDealTaskProducer.enqueueTimeDealTasks(timeDeal); // 딜레이 큐에 상태 변경 작업 추가
            } catch (InterruptedException e) { // 스레드 종료 요청 수신
                Thread.currentThread().interrupt(); // 인터럽트 플래그 복원
                log.error("딜레이 큐 초기화 중 인터럽트 발생: 타임 딜 아이디={}", timeDeal.getId(), e);
            } catch (Exception e) {
                log.error("딜레이 큐 초기화 중 예외 발생: 타임 딜 아이디={}", timeDeal.getId(), e);
            }
        });
    }

}
