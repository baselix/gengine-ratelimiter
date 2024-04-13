package ru.gurkin.gengine.ratelimiter.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "gengine")
public class AppProperties {
    private String variable;
    private String ratelimiterVariable;
}
