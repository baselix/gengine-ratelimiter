package ru.gurkin.gengine.ratelimiter.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ru.gurkin.gengine.ratelimiter.config.properties.RateLimiterProperties;
import ru.gurkin.gengine.ratelimiter.service.impl.RateLimiterServiceImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class RateLimiterServiceImplTest {
    private final RateLimiterProperties rateLimiterProperties = new RateLimiterProperties(Duration.ofSeconds(1L), 5000L);
    private final RateLimiterService service = new RateLimiterServiceImpl(rateLimiterProperties);


    @Test
    void acquireTest(){
        long intervalsCount = 10;
        long allowedNumber = 0;
        long disallowedNumber = 0;
        Instant startTime = Instant.now();
        while(Instant.now().isBefore(startTime.plusMillis(intervalsCount * rateLimiterProperties.getLimitationIntervalDuration().toMillis()))){
            if(service.acquire("group", "key")){
                allowedNumber++;
            } else {
                disallowedNumber++;
            }
        }
        log.info("Allowed number = {}", allowedNumber);
        log.info("Disallowed number = {}", disallowedNumber);
        long total = allowedNumber + disallowedNumber;
        log.info("Total: {}", total);
        log.info("Rps: {}", total/(intervalsCount * rateLimiterProperties.getLimitationIntervalDuration().getSeconds()));
        assertThat(allowedNumber).isEqualTo(intervalsCount * rateLimiterProperties.getLimit());
    }

    @Test
    void multiThreadAcquireTest(){
        long intervalsCount = 10;
        AtomicLong allowedNumber = new AtomicLong();
        AtomicLong disallowedNumber = new AtomicLong();
        List<Thread> threads = new ArrayList<>();
        List<Requester> requesters = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            Requester requester = new Requester(intervalsCount, rateLimiterProperties.getLimitationIntervalDuration());
            Thread thread = new Thread(requester);
            threads.add(thread);
            requesters.add(requester);
        }
        threads.forEach(Thread::start);
        AtomicBoolean isAllThreadsStopped = new AtomicBoolean(false);
        while (!isAllThreadsStopped.get()){
            isAllThreadsStopped.set(true);
            threads.forEach((Thread it) -> {
                if(it.getState() != Thread.State.TERMINATED){
                    isAllThreadsStopped.set(false);
                }
            });
        }
        log.info("---------------THREAD RESULTS----------------------");
        requesters.forEach(it -> {
            it.printResults();
            allowedNumber.addAndGet(it.allowedNumber);
            disallowedNumber.addAndGet(it.disallowedNumber);
        });
        log.info("---------------TOTAL RESULTS----------------------");
        long total = allowedNumber.get() + disallowedNumber.get();
        log.info("Rps: {}; Total: {}; Allowed number = {}; Disallowed number = {}",
                total / (intervalsCount * rateLimiterProperties.getLimitationIntervalDuration().getSeconds()),
                total, allowedNumber, disallowedNumber);
    }

    @Data
    @RequiredArgsConstructor
    private class Requester implements Runnable{
        private final long intervalsCount;
        private final Duration limitationIntervalDuration;
        private long allowedNumber = 0;
        private long disallowedNumber = 0;

        @Override
        public void run() {
            Instant startTime = Instant.now();
            while(Instant.now().isBefore(startTime.plusMillis(intervalsCount * limitationIntervalDuration.toMillis()))){
                if(service.acquire("group", "key")){
                    allowedNumber++;
                } else {
                    disallowedNumber++;
                }
            }
        }

        public void printResults(){
            long total = allowedNumber + disallowedNumber;
            log.info("Rps: {}; Total: {}; Allowed number = {}; Disallowed number = {}",
                    total / (intervalsCount * limitationIntervalDuration.getSeconds()),
                    total, allowedNumber, disallowedNumber);
        }
    }
}
