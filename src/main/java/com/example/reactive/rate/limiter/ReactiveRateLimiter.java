package com.example.reactive.rate.limiter;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

public class ReactiveRateLimiter {

	private final int maxRequests;
    private final Duration period;
    private final Queue<Long> requestTimestamps;

    public ReactiveRateLimiter(int maxRequests, Duration period) {
        this.maxRequests = maxRequests;
        this.period = period;
        this.requestTimestamps = new ConcurrentLinkedQueue<>();
    }

    /**
     * Attempts to allow a request immediately based on the rate limit.
     *
     * @return True if the request is allowed, false otherwise.
     */
    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        cleanUp(now);

        if (requestTimestamps.size() < maxRequests) {
            requestTimestamps.add(now);
            return true;
        }
        return false;
    }

    /**
     * Waits asynchronously until a request is allowed or the timeout expires.
     *
     * @param timeout Maximum time to wait for an available slot.
     * @return A Mono that emits true if the request is allowed, or false if it times out.
     */
    public Mono<Boolean> acquire(Duration timeout) {
        Sinks.One<Boolean> sink = Sinks.one();

        long deadline = System.currentTimeMillis() + timeout.toMillis();

        Mono.defer(() -> {
            if (tryAcquire()) {
                sink.tryEmitValue(true);
                return Mono.empty();
            } else if (System.currentTimeMillis() >= deadline) {
                sink.tryEmitValue(false);
                return Mono.empty();
            } else {
                return Mono.delay(Duration.ofMillis(10))
                        .then(Mono.empty()); // Retry after a short delay
            }
        })
        .repeatWhenEmpty(Queues.SMALL_BUFFER_SIZE, repeat -> repeat)
        .subscribe();

        return sink.asMono();
    }

    /**
     * Removes expired request timestamps outside the current period.
     *
     * @param now The current timestamp in milliseconds.
     */
    private void cleanUp(long now) {
        long expirationTime = now - period.toMillis();
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < expirationTime) {
            requestTimestamps.poll();
        }
    }
}
