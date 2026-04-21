package aiss.YouTubeMiner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason = "Channel not found with that Id")
public class ChannelNotFoundException extends Exception{
}

