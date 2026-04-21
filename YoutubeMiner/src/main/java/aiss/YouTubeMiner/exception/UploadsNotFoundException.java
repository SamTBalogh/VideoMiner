package aiss.YouTubeMiner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason = "List of Uploads not found for that ChannelID")
public class UploadsNotFoundException extends Exception{
}
