package com.toyProject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
//    @Bean
//    public Executor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//
//        // 스레드 풀
//        executor.setCorePoolSize(10); // 최소 스레드 10개
//        executor.setMaxPoolSize(50);  // 최대 스레드 50개
//        executor.setQueueCapacity(100); // 대기 큐 크기 100개
//        executor.setThreadNamePrefix("Async-Executor-");
//
//        executor.initialize();
//        return executor;
//    }
}