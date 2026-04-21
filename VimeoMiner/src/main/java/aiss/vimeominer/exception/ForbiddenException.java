package aiss.vimeominer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.FORBIDDEN)
public class ForbiddenException  extends Exception{


    public ForbiddenException(String message) {
        super(message);
    }

    public static String parseVideo(String str){
        int strIndex = str.indexOf("\"message\"");
        int startIndex = str.indexOf("\"", strIndex + 10);
        int endIndex = str.indexOf("\"", startIndex + 1);
        return str.substring(startIndex + 1, endIndex);
    }

}
