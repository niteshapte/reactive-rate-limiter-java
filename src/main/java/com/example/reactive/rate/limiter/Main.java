package com.example.reactive.rate.limiter;

import java.time.Duration;

public class Main {

	public static void main(String[] args) {
	ReactiveRateLimiter rateLimiter = new ReactiveRateLimiter(5, Duration.ofSeconds(1)); // Max 5 request with duration of 1 second

        // Test tryAcquire
        for (int i = 0; i < 7; i++) {
            System.out.println("Request allowed immediately: " + rateLimiter.tryAcquire());
        }

        // Test acquire with timeout
        rateLimiter.acquire(Duration.ofSeconds(5))
                .doOnNext(allowed -> {
                    if (allowed) {
                        System.out.println("Request allowed after waiting.");
                    } else {
                        System.out.println("Request timed out.");
                    }
                })
                .block();
    }
}
