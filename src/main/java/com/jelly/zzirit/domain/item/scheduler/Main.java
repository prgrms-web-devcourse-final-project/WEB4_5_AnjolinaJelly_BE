package com.jelly.zzirit.domain.item.scheduler;

import java.util.concurrent.*;
import java.util.concurrent.DelayQueue;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        /*
        딜레이 큐가 상속받은 친구들
        delayed : 이 큐에는 지연 시간 가진 객체만 들어올 수 있다.
        abstractqueue : 큐 기본 로직 가지고 있다.
        blockingqueue : 쓰레드 간 안전하게 기다렸다가 넣고 꺼낼 수 있다. todo: 쓰레드?? 알아보기
         */
        BlockingQueue<DelayedObject> queue = new DelayQueue<>();
        int numberOfElements = 3;
        int interval = 1000;
        int delay = 5000;

        DelayQueueProducer producer = new DelayQueueProducer(queue, numberOfElements, delay, interval);
        DelayQueueConsumer consumer = new DelayQueueConsumer(queue, numberOfElements);

        executor.submit(producer);
        executor.submit(consumer);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}
