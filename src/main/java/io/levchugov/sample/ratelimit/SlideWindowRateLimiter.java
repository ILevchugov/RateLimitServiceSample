package io.levchugov.sample.ratelimit;

import io.levchugov.sample.config.RateLimitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class SlideWindowRateLimiter implements RateLimiter {

    private final ConcurrentMap<String, LinkedList<LocalDateTime>> limitMap = new ConcurrentHashMap<>();
    private final RateLimitConfig rateLimitConfig;
    private final LockByKey lockByKey = new LockByKey();

    @Override
    public boolean doesLimitReached(String ip, LocalDateTime currentTime) {
        try {
            lockByKey.lock(ip);
            var count = getCount(ip, currentTime);
            if (count > rateLimitConfig.getRequestCount() - 1) {
                return true;
            } else {
                add(ip, currentTime);
                return false;
            }
        } finally {
            lockByKey.unlock(ip);
        }
    }

    private void add(String ip, LocalDateTime currentTime) {
        try {
            lockByKey.lock(ip);
            LinkedList<LocalDateTime> times = limitMap.get(ip);
            if (times == null || times.isEmpty()) {
                times = new LinkedList<>();
                limitMap.put(ip, times);
            } else {
                deleteTail(times, currentTime);
            }
            times.add(currentTime);
        } finally {
            lockByKey.unlock(ip);
        }
    }

    private int getCount(String ip, LocalDateTime currentTime) {
        var times = limitMap.get(ip);
        if (times == null) {
            return 0;
        }
        deleteTail(times, currentTime);
        return times.size();

    }


    private void deleteTail(LinkedList<LocalDateTime> times, LocalDateTime currentTime) {
        var firstTime = currentTime.minus(rateLimitConfig.getTime(), ChronoUnit.SECONDS);
        times.removeIf(t -> t.isBefore(firstTime));
    }

}
