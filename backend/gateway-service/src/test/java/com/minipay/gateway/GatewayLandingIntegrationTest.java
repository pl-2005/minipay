package com.minipay.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "minipay.frontend-url=http://frontend.test")
class GatewayLandingIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void rootRedirectsToConfiguredFrontend() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "http://frontend.test");
    }
}
