# Reactive Rate Limiter in Java 21 using Project Reactor library

## Overview
This repository provides an implementation of a reactive rate limiter using the Reactor library. The rate limiter enforces request limits asynchronously and allows customization of the maximum requests and time windows.

## Features
- Reactive programming model using Reactor API.
- Supports asynchronous waiting for request availability.
- Configurable rate limits and timeouts.
- Clean and extensible design.

## Requirements
- Java 21
- Maven or any compatible build tool

## Usage

### Example
```java
import com.example.ratelimiter.RateLimiter;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        ReactiveRateLimiter rateLimiter = new ReactiveRateLimiter(5, Duration.ofSeconds(1));

        // Attempt an immediate request
        boolean allowed = rateLimiter.tryAcquire();
        System.out.println("Immediate request allowed: " + allowed);

        // Wait asynchronously for a request
        rateLimiter.acquire(Duration.ofSeconds(5))
            .doOnNext(isAllowed -> {
                if (isAllowed) {
                    System.out.println("Request allowed after waiting.");
                } else {
                    System.out.println("Request timed out.");
                }
            })
            .block();
    }
}
```
