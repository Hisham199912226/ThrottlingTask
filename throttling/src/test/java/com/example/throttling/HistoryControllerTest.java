package com.example.throttling;

import com.example.throttling.controller.HistoryController;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HistoryControllerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private RateLimiter rateLimiter;
    @Mock
    private Bucket bucket;
    @Mock
    private ConsumptionProbe probe;
    @Mock
    private AtomicInteger throttledRequests;
    private HistoryController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new HistoryController();
        ReflectionTestUtils.setField(controller, "rateLimiter", rateLimiter);
        ReflectionTestUtils.setField(controller, "throttledRequests", throttledRequests);
    }

    @Test
    public void testValidIpAddressWithRequestsRemaining(){
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/history");

        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(true);
        when(rateLimiter.resolveBucket(anyString())).thenReturn(bucket);


        ResponseEntity<String> response = controller.getHistoryUrl(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testValidIpAddressWithNoRequestsRemainingAndMaxThrottledRequestsNotExceeded() {
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/history");

        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(rateLimiter.resolveBucket(anyString())).thenReturn(bucket);
        when(throttledRequests.get()).thenReturn(15);

        ResponseEntity<String> response = controller.getHistoryUrl(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testValidIpAddressWithNoRequestsRemainingAndMaxThrottledRequestsExceeded() {
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/history");

        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(rateLimiter.resolveBucket(anyString())).thenReturn(bucket);
        when(throttledRequests.get()).thenReturn(40);

        ResponseEntity<String> response = controller.getHistoryUrl(request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    public void testThrottleDelay() {
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/history");

        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(rateLimiter.resolveBucket(anyString())).thenReturn(bucket);
        when(throttledRequests.get()).thenReturn(15);

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = controller.getHistoryUrl(request);
        long endTime = System.currentTimeMillis();

        long elapsedTime = endTime - startTime;

        assertTrue(elapsedTime >= 3000);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testInvalidIpAddressWithBadRequestResponse(){
        when(request.getRemoteAddr()).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/history");

        ResponseEntity<String> response = controller.getHistoryUrl(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
