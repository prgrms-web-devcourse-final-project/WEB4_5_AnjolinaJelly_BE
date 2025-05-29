package com.jelly.zzirit.domain.item.queue;

import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus;
import jakarta.annotation.Nonnull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 딜레이 큐에 추가될 작업 단위
 * <p>
 * case 1) 타임 딜 시작 시 상태 업데이트 작업
 * </p>
 * <p>
 * case 2) 타임 딜 종료 시 상태 업데이트 작업
 * </p>
 * @param timeDealId         타임 딜 아이디
 * @param nextTimeDealStatus 작업 실행 후 타임 딜 상태
 * @param nextItemStatus     작업 실행 후 상품 상태
 * @param triggerAt          작업 실행 시점
 */
public record TimeDealTask(
    Long timeDealId,
    TimeDealStatus nextTimeDealStatus,
    ItemStatus nextItemStatus,
    LocalDateTime triggerAt
) implements Delayed {

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = triggerAt
            .atZone(ZoneId.systemDefault()) // 시스템 시간대 반영
            .toInstant()
            .toEpochMilli() - System.currentTimeMillis(); // epoch millis로 변환

        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@Nonnull Delayed other) {
        return this.triggerAt.compareTo(((TimeDealTask) other).triggerAt);
    }

    @Override
    public String toString() { // 로깅에 사용
        LocalDateTime now = LocalDateTime.now();
        Duration delay = Duration.between(triggerAt, now);

        return "TimeDealTask{" +
            "타임 딜 아이디=" + timeDealId +
            ", 변경된 타임 딜 상태=" + nextTimeDealStatus +
            ", 예정된 실행 시각=" + triggerAt +
            ", 현재 시각=" + now +
            ", 지연 시간=" + delay.toMillis() + "ms" +
            '}';
    }

}
