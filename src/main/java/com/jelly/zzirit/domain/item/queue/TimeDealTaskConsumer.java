package com.jelly.zzirit.domain.item.queue;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealTaskConsumer {

    private final TimeDealTaskExecutor executor;
    private final BlockingQueue<TimeDealTask> queue;

    @PostConstruct
    public void consume() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    TimeDealTask task = queue.take(); // 딜레이 큐에서 작업 소비
                    log.info("[0] 딜레이 큐에서 작업 소비: {}", task);
                    executor.execute(task); // 상태 변경 작업 수행
                } catch (InterruptedException ex) { // 스레드 종료 요청 수신
                    Thread.currentThread().interrupt(); // 인터럽트 플래그 복원
                    break;
                } catch (RuntimeException ex) {
                    log.error("타임 딜 작업 처리 중 예외 발생", ex);
                }
            }
        }, "time-deal-delay-queue-consumer");
        thread.setDaemon(true);
        thread.start();
    }

}
