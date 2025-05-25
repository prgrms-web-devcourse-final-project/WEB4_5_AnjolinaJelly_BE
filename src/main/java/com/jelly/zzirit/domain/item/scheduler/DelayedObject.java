//package com.jelly.zzirit.domain.item.scheduler;
//
//import lombok.Getter;
//
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.Delayed;
//
//public class DelayedObject implements Delayed {
//    @Getter
//    private final String data; // 데이터 + 식별자 역할
//    private final long expTime;
//
//    public DelayedObject(String data, long delayInMillis){
//        this.data = data;
//        this.expTime = System.currentTimeMillis() + delayInMillis; // 현재 시간 + 지연시간 = 만료시각 계산
//    }
//
//    @Override
//    public long getDelay(TimeUnit unit) {
//        return unit.convert(expTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
//    }
//
//    @Override
//    public int compareTo(Delayed o) {
//        DelayedObject that = (DelayedObject) o; // 비교 대상 객체 다운캐스팅
//        int c = Long.compare(expTime, that.expTime);
//        if(c != 0) return c; // 만료 시간이 다르면, 비교 결과 리턴
//        return Integer.compare(System.identityHashCode(this), System.identityHashCode(that)); // 만료 시간이 같으면, 객체 id 해시값으로 판별
//        // todo: 마지막줄 삭제
//    }
//}
