package com.example.throttling;

import com.example.throttling.controller.HistoryController;
import com.example.throttling.controller.PostsController;
import com.example.throttling.controller.ProfileController;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PostsControllerTest {

    @Mock
    private HttpServletRequest request;

    private PostsController controller;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new PostsController();
    }

    @Test
    public void testValidIpAddressWithOkResponse(){
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/posts");

        ResponseEntity<String> response = controller.getPostsUrl(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testInvalidIpAddressWithBadRequestResponse(){
        when(request.getRemoteAddr()).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getRequestURI()).thenReturn("/posts");

        ResponseEntity<String> response = controller.getPostsUrl(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
