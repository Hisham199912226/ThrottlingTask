package com.example.throttling;

import jakarta.servlet.http.HttpServletRequest;

public class IpAddressExtractor {
    public static String getClientIpAddress(HttpServletRequest request) {
        if(request == null)
            throw new IllegalArgumentException("Invalid Http Request");
        String headerName = "X-Forwarded-For";
        String ipAddress = request.getHeader(headerName);
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
