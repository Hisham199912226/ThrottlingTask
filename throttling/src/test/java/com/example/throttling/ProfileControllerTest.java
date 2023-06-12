package com.example.throttling;

import com.example.throttling.controller.HistoryController;
import com.example.throttling.controller.ProfileController;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProfileControllerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private RateLimiter rateLimiter;
    @Mock
    private Bucket bucket;
    @Mock
    private ConsumptionProbe probe;
    private ProfileController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new ProfileController();
        ReflectionTestUtils.setField(controller, "rateLimiter", rateLimiter);
    }

    @Test
    public void testValidIpAddressWithRequestsRemaining() {
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/profile");

        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(true);
        when(rateLimiter.resolveBucket(anyString())).thenReturn(bucket);

        ResponseEntity<String> response = controller.getProfileUrl(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testValidIpAddressWithNoRequestsRemaining() {
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/profile");

        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(rateLimiter.resolveBucket(anyString())).thenReturn(bucket);

        ResponseEntity<String> response = controller.getProfileUrl(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testThrottleDelay() {
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/profile");

        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(rateLimiter.resolveBucket(anyString())).thenReturn(bucket);

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = controller.getProfileUrl(request);
        long endTime = System.currentTimeMillis();

        long elapsedTime = endTime - startTime;

        assertTrue(elapsedTime >= 3000);
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }
    @Test
    public void testGetProfileUrl_WithInvalidIpAddress_ReturnsBadRequest() {
        when(request.getRemoteAddr()).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/profile");

        ResponseEntity<String> response = controller.getProfileUrl(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
