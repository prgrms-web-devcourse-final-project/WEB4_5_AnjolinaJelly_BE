//package com.jelly.zzirit.domain.item.scheduler;
//
//import lombok.AllArgsConstructor;
//
//import java.time.Instant;
//import java.util.Date;
//import java.util.UUID;
//import java.util.concurrent.BlockingQueue;
//
//@AllArgsConstructor
//public class DelayQueueProducer implements Runnable {
//    private final BlockingQueue<DelayedObject> queue;
//    private final Integer numberOfElements;
//    private final int delay;
//    private final int interval;
//
//    @Override
//    public void run() {
//        for(int i = 0; i < numberOfElements; i++){
//            DelayedObject object = new DelayedObject(UUID.randomUUID().toString(), delay);
//            System.out.println("(" + Date.from(Instant.now()) + ") Put object : " + object.getData());
//
//            try{
//                queue.put(object); // 큐가 가득찬 경우 기다렸다가 넣음
//                Thread.sleep(interval); // 다음 객체 넣기 전까지 interval만큼 대기
//            } catch(InterruptedException ie){
//                ie.printStackTrace();
//            }
//        }
//    }
//}
