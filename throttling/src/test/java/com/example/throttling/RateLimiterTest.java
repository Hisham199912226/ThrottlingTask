package com.example.throttling;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    public void setup() {
        int requestLimit = 10;
        Duration refillDuration = Duration.ofMinutes(1);
        rateLimiter = new RateLimiter(requestLimit, refillDuration);
    }

    @Test
    public void resolveBucket_ValidIpAddress_ReturnsBucket() {
        String ipAddress = "127.0.0.1";
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        Assertions.assertNotNull(bucket);
    }

    @Test
    public void resolveBucket_NullIpAddress_ThrowsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> rateLimiter.resolveBucket(null));
    }

    @Test
    public void resolveBucket_SameIpAddress_ReturnsSameBucketInstance() {
        String ipAddress = "127.0.0.1";
        Bucket bucket1 = rateLimiter.resolveBucket(ipAddress);
        Bucket bucket2 = rateLimiter.resolveBucket(ipAddress);
        Assertions.assertSame(bucket1, bucket2);
    }

    @Test
    public void resolveBucket_DifferentIpAddresses_ReturnsDifferentBucketInstances() {
        String ipAddress1 = "127.0.0.1";
        String ipAddress2 = "192.168.0.1";
        Bucket bucket1 = rateLimiter.resolveBucket(ipAddress1);
        Bucket bucket2 = rateLimiter.resolveBucket(ipAddress2);
        Assertions.assertNotSame(bucket1, bucket2);
    }

    @Test
    public void resolveBucket_InvalidCapacity_ThrowsIllegalArgumentException() {
        int invalidCapacity = 0;
        Duration refillDuration = Duration.ofMinutes(1);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(invalidCapacity, refillDuration));
    }

    @Test
    public void resolveBucket_InvalidDuration_ThrowsIllegalArgumentException() {
        int requestLimit = 10;
        Duration invalidDuration = Duration.ofSeconds(-10);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(requestLimit, invalidDuration));
    }
}
