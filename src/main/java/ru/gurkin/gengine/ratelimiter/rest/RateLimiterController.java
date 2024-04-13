package ru.gurkin.gengine.ratelimiter.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gurkin.gengine.ratelimiter.service.RateLimiterService;

@RestController
@RequestMapping("ratelimiter")
@RequiredArgsConstructor
public class RateLimiterController {

    private final RateLimiterService service;

    @GetMapping("/acquire")
    public boolean acquire(
        @RequestParam String group,
        @RequestParam String key
    ) {
        return service.acquire(group, key);
    }
}
