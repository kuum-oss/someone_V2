package org.example.infrastructure.web;

import org.example.infrastructure.repository.BaseRepositoryIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class WebServerLoadTest extends BaseRepositoryIntegrationTest {

    private WebServer webServer;
    private int port;
    private HttpClient httpClient;

    @BeforeEach
    void startServer() {
        webServer = new WebServer();
        port = webServer.start(0); // dynamically select free port
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void stopServer() {
        if (webServer != null) {
            webServer.stop();
        }
    }

    @Test
    void testConcurrentStaticAssetRequests() throws Exception {
        int concurrentUsers = 30;
        int requestsPerUser = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<HttpResponse<String>>> futures = new ArrayList<>();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger tooManyRequestsCount = new AtomicInteger(0);

        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            for (int r = 0; r < requestsPerUser; r++) {
                // Request static asset to ensure it is not rate limited
                futures.add(executor.submit(() -> {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:" + port + "/css/style.css")) // static pattern
                            .header("X-Forwarded-For", "192.168.1." + userId) // simulate different users
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200 || response.statusCode() == 404) {
                        successCount.incrementAndGet();
                    } else if (response.statusCode() == 429) {
                        tooManyRequestsCount.incrementAndGet();
                    }
                    return response;
                }));
            }
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(20, TimeUnit.SECONDS);
        assertTrue(finished, "Load test did not finish in time");

        // Verify that no static asset request was rate limited (429)
        assertEquals(0, tooManyRequestsCount.get(), "Static assets should NOT trigger 429 rate limit");
        assertEquals(concurrentUsers * requestsPerUser, successCount.get(), "All requests should succeed or return 404, but not fail on concurrency");
    }

    @Test
    void testConcurrentDynamicRequestsWithinRateLimit() throws Exception {
        int concurrentUsers = 15;
        int requestsPerUser = 3;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<HttpResponse<String>>> futures = new ArrayList<>();

        AtomicInteger status200or302 = new AtomicInteger(0);
        AtomicInteger status429 = new AtomicInteger(0);

        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            futures.add(executor.submit(() -> {
                for (int r = 0; r < requestsPerUser; r++) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:" + port + "/shop"))
                            .header("X-Forwarded-For", "172.16.0." + userId) // separate IP per user
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200 || response.statusCode() == 302) {
                        status200or302.incrementAndGet();
                    } else if (response.statusCode() == 429) {
                        status429.incrementAndGet();
                    }
                    Thread.sleep(150); // sleep between sequential requests from the same user
                }
                return null;
            }));
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(20, TimeUnit.SECONDS);
        assertTrue(finished, "Load test did not finish in time");

        // Because we simulated separate IPs and delayed requests by 150ms (> 100ms interval limit), they should not trigger 429
        assertEquals(0, status429.get(), "Requests from different users above min interval limit should NOT trigger 429");
        assertEquals(concurrentUsers * requestsPerUser, status200or302.get(), "All user visits should load shop page successfully");
    }
}
