package ru.gurkin.gengine.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "ru.gurkin.gengine")
@EnableConfigurationProperties
@ConfigurationPropertiesScan("ru.gurkin.gengine.ratelimiter.config.properties")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
