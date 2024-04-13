package ru.gurkin.gengine.ratelimiter.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gurkin.gengine.ratelimiter.config.properties.RateLimiterProperties;
import ru.gurkin.gengine.ratelimiter.model.TimestampNode;
import ru.gurkin.gengine.ratelimiter.service.RateLimiterService;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {

    private final RateLimiterProperties rateLimiterProperties;

    private TimestampNode head = new TimestampNode(Instant.now().toEpochMilli());

    /**
     * @param group группа
     * @param key   ключ
     * @return результат(true - взаимодействие разрешено, false - взаимодействие запрещено)
     */
    @Override
    public boolean acquire(String group, String key) {
        synchronized (this) {
            Instant startTime = Instant.now();
            long timestamp = startTime.toEpochMilli();
            long limitationInterval = rateLimiterProperties.getLimitationIntervalDuration().toMillis();
            long limit = rateLimiterProperties.getLimit();
            TimestampNode timestampNode = head;
            TimestampNode currentTimestampNode = null;
            long count = 0;
            do{
                // Если мы прошли все ноды и не нашли текущий таймстемп, то надо его добавить
                if(timestampNode.getNext() == null && currentTimestampNode == null) {
                    timestampNode.setNext(new TimestampNode(timestamp));
                }
                // Если нода попала в интересующий нас интервал
                if (timestampNode.getTimestamp() >= timestamp - limitationInterval){
                    // если нашли ноду для текущего таймстемпа
                    if(timestampNode.getTimestamp() == timestamp){
                        // то сохраним ссылку на него, для возможности увеличения счетчика в дальнейшем
                        currentTimestampNode = timestampNode;
                    }
                    // Подсчитываем сумму удачных запросов в нодах
                    count += timestampNode.getNumberOfAllowed();
                    // Если количество запросов для уже пройденных нод достигло лимита или превысило его, то запрещаем
                    if (count >= limit) {
                        return logResultAndReturnFalse(startTime);
                    }
                    // Если мы прошли все ноды и не нашли текущий таймстемп, то надо его добавить
                    if(timestampNode.getNext() == null && currentTimestampNode == null) {
                        timestampNode.setNext(new TimestampNode(timestamp));
                    }
                } else {
                    // Если нода не попала в интересующий нас интервал, сдвигаем head
                    // Здесь getNext() никогда не будет null
                    head = timestampNode.getNext();
                }
                timestampNode = timestampNode.getNext();
            } while (timestampNode != null);
            // Если пройдя все ноды количество запросов не превысило лимит,
            // и мы нашли нужную ноду в интервале, то разрешаем и увеличиваем количество разрешенных
            if(currentTimestampNode != null) {
                currentTimestampNode.setNumberOfAllowed(currentTimestampNode.getNumberOfAllowed() + 1);
                return logResultAndReturnTrue(startTime);
            }
            // Попасть сюда мы не должны, т.к. нода для текущего таймстемпа в любом случае будет последней
            // и должна попасть в интервал
            throw new IllegalStateException(String.format("Node for current timestamp %s was not found", startTime));
        }
    }

    private boolean logResultAndReturnFalse(Instant startTime) {
        logAcquireResult(false, startTime);
        return false;
    }

    private boolean logResultAndReturnTrue(Instant startTime) {
        logAcquireResult(true, startTime);
        return true;
    }

    private void logAcquireResult(boolean result, Instant startTime) {
        log.debug("Result: {}. Runs at {}ms", result, Duration.between(startTime, Instant.now()).toMillis());
    }
}
