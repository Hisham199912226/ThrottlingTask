package com.example.throttling;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUrlCreator {

    public static String getRequestUrl(HttpServletRequest request) {
        if(request == null)
            throw new IllegalArgumentException();
        String hostHeader = request.getHeader("Host");
        return "http://".concat(hostHeader).concat(request.getRequestURI());
    }
}
