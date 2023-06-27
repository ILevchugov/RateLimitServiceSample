package io.levchugov.sample.controller;

import io.levchugov.sample.config.RateLimitConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PingControllerTest {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSingleThreadRequestCountOk() throws Exception {
        int num = 0;
        for (int i = 0; i < rateLimitConfig.getRequestCount() * 2; i++) {
            var result = ping("192.168.0.5");
            var status = result.getResponse().getStatus();
            if (status == 200) {
                num++;
            }
        }
        Assertions.assertEquals(rateLimitConfig.getRequestCount(), num);
    }

    @Test
    void testTimeSingleThreadBothPeriodOk() throws Exception {
        for (int i = 0; i < rateLimitConfig.getRequestCount(); i++) {
            var result = ping("192.168.0.4");
            Assertions.assertEquals(200, result.getResponse().getStatus());
        }

        await().pollDelay(Duration.of(rateLimitConfig.getTime(), ChronoUnit.SECONDS)).until(() -> true);

        for (int i = 0; i < rateLimitConfig.getRequestCount(); i++) {
            var result = ping("192.168.0.4");
            Assertions.assertEquals(200, result.getResponse().getStatus());
        }

    }

    @Test
    void testTimeSingleThreadOnePeriodOkOneFailed() throws Exception {
        for (int i = 0; i < rateLimitConfig.getRequestCount(); i++) {
            var result = ping("192.168.0.3");
            Assertions.assertEquals(200, result.getResponse().getStatus());
        }

        Assertions.assertEquals(502, ping("192.168.0.3").getResponse().getStatus());

        await().pollDelay(Duration.of(rateLimitConfig.getTime(), ChronoUnit.SECONDS)).until(() -> true);

        for (int i = 0; i < rateLimitConfig.getRequestCount(); i++) {
            var result = ping("192.168.0.3");
            Assertions.assertEquals(200, result.getResponse().getStatus());
        }

    }

    @Test
    void testCounterWithConcurrencyOk() throws InterruptedException {
        int numberOfThreads = 50;
        var ips = List.of(
                "192.168.0.1", "192.168.0.2",
                "192.168.0.8", "192.168.0.9",
                "192.168.0.10"
        );
        ExecutorService service = Executors.newFixedThreadPool(50);

        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            service.execute(() -> {
                try {
                    for (int j = 0; j < rateLimitConfig.getRequestCount(); j++) {
                        var result = ping(ips.get(finalI % ips.size()));
                        if (result.getResponse().getStatus() == 200) {
                            successCount.incrementAndGet();
                        } else {
                            break;
                        }
                    }
                    latch.countDown();
                } catch (Exception e) {
                    //ignore
                }
            });
        }
        latch.await();
        Assertions.assertEquals(rateLimitConfig.getRequestCount() * ips.size(), successCount.get());
    }

    private MvcResult ping(String ip) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders.get(
                                "/ping"
                        )
                        .with(remoteAddr(ip))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    private static RequestPostProcessor remoteAddr(String remoteAddr) {
        return request -> {
            request.setRemoteAddr(remoteAddr);
            return request;
        };
    }

}