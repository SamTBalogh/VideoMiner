package aiss.vimeominer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason = "Videos not found with that Channel Id")
public class VideosNotFoundException extends Exception{
}
