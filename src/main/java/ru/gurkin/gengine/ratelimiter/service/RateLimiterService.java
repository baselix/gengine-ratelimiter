package ru.gurkin.gengine.ratelimiter.service;

public interface RateLimiterService {
    boolean acquire(String group, String key);
}
