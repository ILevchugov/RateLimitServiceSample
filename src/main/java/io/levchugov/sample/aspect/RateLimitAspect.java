package io.levchugov.sample.aspect;

import io.levchugov.sample.exception.RateLimitException;
import io.levchugov.sample.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final HttpServletRequest request;

    private final RateLimiter rateLimiter;

    @Pointcut("@annotation(io.levchugov.sample.aspect.RateLimit)")
    public void annotationPointCutDefinition() {
    }

    @Before("annotationPointCutDefinition()")
    public void invoke() {
        if (rateLimiter.doesLimitReached(request.getRemoteAddr(), LocalDateTime.now())) {
            throw new RateLimitException();
        }
    }
}
