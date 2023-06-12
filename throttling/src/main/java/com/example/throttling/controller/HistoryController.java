package com.example.throttling.controller;

import com.example.throttling.*;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class HistoryController {
    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final int MAX_THROTTLED_REQUESTS = 30;
    private final AtomicInteger throttledRequests = new AtomicInteger(1);
    private static final int THROTTLE_DELAY_MS = 3000;
    private final RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));


    @GetMapping("/history")
    public ResponseEntity<String> getHistoryUrl(HttpServletRequest request){
        String ipAddress = IpAddressExtractor.getClientIpAddress(request);

        if (isValidIpAddress(ipAddress)) {
            Bucket bucket = rateLimiter.resolveBucket(ipAddress);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                if (exceedMaxThrottledRequests()) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable");
                }
                throttleRequest();
            }

            String historyUrl = RequestUrlCreator.getRequestUrl(request);
            return ResponseEntity.ok(historyUrl);
        } else {
            return ResponseEntity.badRequest().body("Invalid IP Address");
        }
    }

    private boolean isValidIpAddress(String ipAddress) {
        return ipAddress != null && !ipAddress.isEmpty();
    }

    private boolean exceedMaxThrottledRequests() {
        return throttledRequests.get() > MAX_THROTTLED_REQUESTS;
    }

    private void throttleRequest() {
        try {
            throttledRequests.getAndIncrement();
            Thread.sleep(THROTTLE_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            throttledRequests.getAndDecrement();
        }
    }
}
