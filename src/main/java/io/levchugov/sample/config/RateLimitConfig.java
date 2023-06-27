package io.levchugov.sample.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.time}")
    private Integer time;
    @Value("${rate-limit.request-count}")
    private Integer requestCount;

}
