package com.example.throttling;

import io.github.bucket4j.*;
import lombok.Data;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RateLimiter {

    private final Map<String, Bucket> ipAddressToBucketMap = new ConcurrentHashMap<>();
    private int requestLimit;
    private Duration refillDuration;

    public RateLimiter(int capacity, Duration duration) {
        validateConfiguration(capacity,duration);
        this.requestLimit = capacity;
        this.refillDuration = duration;
    }

    private void validateConfiguration(int requestLimit, Duration refillDuration) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer.");
        }

        if (refillDuration == null || refillDuration.isNegative() || refillDuration.isZero()) {
            throw new IllegalArgumentException("Duration must be a positive non-null value.");
        }
    }

    public Bucket resolveBucket(String ipAddress){
        if(ipAddress == null)
            throw new IllegalArgumentException("Invalid Ip Address.");
        return ipAddressToBucketMap.computeIfAbsent(ipAddress, this::newBucket);
    }

    private Bucket newBucket(String s) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(requestLimit, Refill.intervally(requestLimit, refillDuration)))
                .build();
    }
}
