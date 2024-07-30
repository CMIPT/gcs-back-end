package edu.cmipt.gcs.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@SpringBootTest
@AutoConfigureMockMvc
public class SwaggerTmpControllerTests {
    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = MockMvcWebTestClient.bindToController(new SwaggerTmpController()).build();
    }

    @Test
    public void testGetUser() {
        webClient.get().uri("/api/users/1").exchange().expectStatus().isOk().expectBody(Boolean.class).isEqualTo(true);
    }
}
