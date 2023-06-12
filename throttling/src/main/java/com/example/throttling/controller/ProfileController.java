package com.example.throttling.controller;

import com.example.throttling.IpAddressExtractor;
import com.example.throttling.RateLimiter;
import com.example.throttling.RequestUrlCreator;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@RestController
@RequestMapping("/api")
public class ProfileController {

    private static final int REQUEST_LIMIT_PER_MINUTE = 10;
    private static final int THROTTLE_DELAY_MS = 3000;
    private final RateLimiter rateLimiter = new RateLimiter(REQUEST_LIMIT_PER_MINUTE, Duration.ofMinutes(1));

    @GetMapping("/profile")
    public ResponseEntity<String> getProfileUrl(HttpServletRequest request) {
        String ipAddress = IpAddressExtractor.getClientIpAddress(request);

        if (isValidIpAddress(ipAddress)) {
            Bucket bucket = rateLimiter.resolveBucket(ipAddress);

            if (!tryConsumeFromBucket(bucket)) {
                applyThrottleDelay();
            }

            String profileUrl = RequestUrlCreator.getRequestUrl(request);
            return ResponseEntity.ok(profileUrl);
        } else {
            return ResponseEntity.badRequest().body("Invalid IP Address");
        }
    }

    private boolean isValidIpAddress(String ipAddress) {
        return ipAddress != null && !ipAddress.isEmpty();
    }

    private boolean tryConsumeFromBucket(Bucket bucket) {
        if(bucket == null)
            throw  new IllegalArgumentException();
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return probe.isConsumed();
    }

    private void applyThrottleDelay() {
        try {
            Thread.sleep(THROTTLE_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

