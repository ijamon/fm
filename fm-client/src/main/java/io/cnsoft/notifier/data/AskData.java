package io.cnsoft.notifier.data;

import lombok.*;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Jamon on 21.04.2016.
 */
@NoArgsConstructor
public class AskData <T> extends BaseData {

    @Getter
    @Setter
    public Questions question;

    @Getter
    @Setter
    protected T result;

    /*
    Should migrate to up-level model
     */
    @Getter
    @Setter
    protected CountDownLatch latch;

    public AskData(Questions question, String message){
        super(message);
        setQuestion(question);
    }

    @AllArgsConstructor
    public enum Questions {
        OVERWRITE_CONFIRMATION("overwriteConfirmation"),

        DELETE_CONFIRMATION ("deleteConfirmation");

        @Getter
        private String id;
    }
}
