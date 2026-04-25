package aiss.videominer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "VideoMiner calls require Authorization: Bearer <token>")
public class TokenRequiredException extends Exception {
}
