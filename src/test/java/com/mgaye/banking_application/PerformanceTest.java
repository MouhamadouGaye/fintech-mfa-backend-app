package com.mgaye.banking_application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.mgaye.banking_application.dto.request.AuthenticationRequest;
import com.mgaye.banking_application.dto.response.AuthenticationResponse;

// # ========== PERFORMANCE AND LOAD TESTING ==========

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void loginEndpoint_UnderLoad_ShouldMaintainPerformance() throws InterruptedException {
        int numberOfThreads = 10;
        int requestsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long startTime = System.currentTimeMillis();

                        AuthenticationRequest request = AuthenticationRequest.builder()
                                .email("load-test-" + j + "@test.com")
                                .password("TestPass123!")
                                .build();

                        restTemplate.postForEntity(
                                "http://localhost:" + port + "/api/auth/login",
                                request,
                                AuthenticationResponse.class);

                        long responseTime = System.currentTimeMillis() - startTime;
                        responseTimes.add(responseTime);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        // Performance assertions
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        assertThat(averageResponseTime).isLessThan(1000.0); // Average < 1 second
        assertThat(maxResponseTime).isLessThan(5000L); // Max < 5 seconds
        assertThat(responseTimes).hasSize(numberOfThreads * requestsPerThread);

        System.out.println("Performance Test Results:");
        System.out.println("Total requests: " + responseTimes.size());
        System.out.println("Average response time: " + averageResponseTime + "ms");
        System.out.println("Max response time: " + maxResponseTime + "ms");
    }
}
