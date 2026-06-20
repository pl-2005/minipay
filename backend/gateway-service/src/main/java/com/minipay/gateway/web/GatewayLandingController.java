package com.minipay.gateway.web;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayLandingController {
    private final URI frontendUrl;

    public GatewayLandingController(@Value("${minipay.frontend-url:http://localhost}") String frontendUrl) {
        this.frontendUrl = URI.create(frontendUrl);
    }

    @GetMapping("/")
    public ResponseEntity<Void> redirectToFrontend() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(frontendUrl)
                .build();
    }
}
