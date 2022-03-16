package io.cnsoft.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Jamon on 19.06.2016.
 */
public class ExceptionStub {

    @Getter
    @Setter
    public String message;

    @Getter
    @Setter
    public String stackTrace;

    public ExceptionStub(String message){
        setMessage(message);
    }

    public ExceptionStub(String message, String stackTrace){
        setMessage(message);
        setStackTrace(stackTrace);
    }
}
