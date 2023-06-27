package io.levchugov.sample.ratelimit;

import java.time.LocalDateTime;

public interface RateLimiter {

    boolean doesLimitReached(String ip, LocalDateTime currentTime);

}
