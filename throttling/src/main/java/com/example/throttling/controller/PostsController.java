package com.example.throttling.controller;

import com.example.throttling.IpAddressExtractor;
import com.example.throttling.RequestUrlCreator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PostsController {

    @GetMapping("/posts")
    public ResponseEntity<String> getPostsUrl(HttpServletRequest request){
        String ipAddress = IpAddressExtractor.getClientIpAddress(request);
        if (isValidIpAddress(ipAddress)) {
            String url = RequestUrlCreator.getRequestUrl(request);
            return ResponseEntity.ok(url);
        }
        return ResponseEntity.badRequest().body("Invalid IP Address");
    }


    private boolean isValidIpAddress(String ipAddress) {
        return ipAddress != null && !ipAddress.isEmpty();
    }
}
