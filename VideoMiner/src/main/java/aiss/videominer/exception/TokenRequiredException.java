package aiss.videominer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.FORBIDDEN, reason="VideoMiner calls require authorization")
public class TokenRequiredException extends Exception {
}
