package io.cnsoft.notifier.bridge;

import io.cnsoft.domain.FileWrapper;
import io.cnsoft.notifier.data.AskData;

/**
 * Created by Jamon on 21.04.2016.
 */
public interface AskUserBus {

    void processOverwriteAsk(FileWrapper source, FileWrapper dest) throws IllegalArgumentException;

    void processArchiveOverwriteAsk(FileWrapper archive) throws IllegalArgumentException;

    void answerCurrentQuestion(String data);

    void awaitCurrentDataLatch() throws Exception;

    AskData getAskData();

    <T> T getAskResult();

    void cleanAskData();

    boolean isCancelled();

    void setCancelled(boolean value);
}
