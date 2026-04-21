package aiss.YouTubeMiner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason = "List of Channels not found with that name")
public class ListChannelsNotFoundException extends Exception{
}
