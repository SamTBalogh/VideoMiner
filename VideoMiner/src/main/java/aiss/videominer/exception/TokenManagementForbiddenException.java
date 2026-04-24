package aiss.videominer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Token management requires a valid X-Token-Management-Key header")
public class TokenManagementForbiddenException extends Exception {
}
