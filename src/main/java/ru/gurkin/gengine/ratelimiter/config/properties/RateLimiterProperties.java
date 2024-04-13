package ru.gurkin.gengine.ratelimiter.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "gengine.ratelimiter")
public class RateLimiterProperties {
    private Duration limitationIntervalDuration;
    private Long limit;
}
