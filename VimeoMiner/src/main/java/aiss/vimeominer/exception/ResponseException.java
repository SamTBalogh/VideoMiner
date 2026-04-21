package aiss.vimeominer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.TOO_MANY_REQUESTS)
public class ResponseException  extends Exception{

    public ResponseException(String message) {
        super(message);
    }

    public static String parseVimeo(String str){
        if(str.contains("too many API requests")){
            str = "Too many API requests made. Wait a minute.";
        }
        return str;
    }
}
