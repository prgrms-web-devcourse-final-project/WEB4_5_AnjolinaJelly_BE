package com.jelly.zzirit.domain.item.scheduler;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
public class DelayedTimeDeal implements Delayed {

    private final Long timeDealId;
    private final Boolean isStarting;
    private final long expTime;

    public DelayedTimeDeal(TimeDeal timeDeal, Boolean isStarting) {
        this.timeDealId = timeDeal.getId();
        this.isStarting = isStarting;

        LocalDateTime targetTime = isStarting ? timeDeal.getStartTime() : timeDeal.getEndTime();
        this.expTime = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        DelayedTimeDeal that = (DelayedTimeDeal) o; // 비교 대상 객체 다운캐스팅
        int c = Long.compare(expTime, that.expTime);
        if(c != 0) return c; // 만료 시간이 다르면, 비교 결과 리턴
        return Integer.compare(System.identityHashCode(this), System.identityHashCode(that)); // 만료 시간이 같으면, 객체 id 해시값으로 판별
        // todo: 마지막줄 삭제
    }
}
