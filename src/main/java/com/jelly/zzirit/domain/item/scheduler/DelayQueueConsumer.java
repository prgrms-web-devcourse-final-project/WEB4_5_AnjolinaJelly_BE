//package com.jelly.zzirit.domain.item.scheduler;
//
//import lombok.AllArgsConstructor;
//
//import java.time.Instant;
//import java.util.Date;
//import java.util.concurrent.BlockingQueue;
//
//@AllArgsConstructor
//public class DelayQueueConsumer implements Runnable { // executorService 통해 별도의 쓰레드로 실행 가능
//    private final BlockingQueue<DelayedObject> queue;
//    private final int numberOfElements;
//
//    @Override
//    public void run() {
//        for (int i = 0; i < numberOfElements; i++) {
//            try{
//                DelayedObject object = queue.take(); // 요소가 없거나 딜레이가 안 끝났으면 기다림
//                System.out.println("(" + Date.from(Instant.now()) + ") Get object : " + object.getData());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
