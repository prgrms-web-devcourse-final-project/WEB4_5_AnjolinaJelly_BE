package com.jelly.zzirit.global.config;

import com.jelly.zzirit.domain.item.queue.TimeDealTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

@Configuration
public class DelayQueueConfig {

    @Bean
    public BlockingQueue<TimeDealTask> timeDealDelayQueue() {
        return new DelayQueue<>();
    }

}
