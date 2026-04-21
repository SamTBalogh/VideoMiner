package aiss.YouTubeMiner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.FORBIDDEN)
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

    public static String parseYoutube(String str){
        if(str.contains("quota")){
            str = "API key quota has been exceded";
        }
        return str;
    }
}
