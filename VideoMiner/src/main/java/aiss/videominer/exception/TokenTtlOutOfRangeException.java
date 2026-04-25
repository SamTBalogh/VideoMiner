package aiss.videominer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Token TTL is outside the allowed range")
public class TokenTtlOutOfRangeException extends Exception {
}
