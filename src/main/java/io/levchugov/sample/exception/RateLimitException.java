package io.levchugov.sample.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Rate Limit Reached", code = HttpStatus.BAD_GATEWAY)
public class RateLimitException extends RuntimeException {

    public RateLimitException() {
        super();
    }

}
