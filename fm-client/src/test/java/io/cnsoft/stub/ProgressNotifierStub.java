package io.cnsoft.stub;

import io.cnsoft.notifier.data.AskData;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.notifier.progress.ProgressSession;
import lombok.Getter;

/**
 * Created by Jamon on 23.04.2016.
 */
public class ProgressNotifierStub implements ProgressNotifier {

    @Getter
    private AskData currentAskData;


    @Override
    public void initializeNotifier(OperationTypes operationType, ProgressSession.ProgressTypes progressType) {

    }

    @Override
    public void setMaximumProgress(long maximum) {

    }

    @Override
    public void setMessage(String message) {

    }

    @Override
    public void updateProgress(long currentProgress) {

    }

    @Override
    public void updateProgress(long currentProgress, String message) {

    }

    @Override
    public void incrementProgress(long increment) {

    }

    @Override
    public void incrementProgress(long increment, String message) {

    }

    @Override
    public void finishOperation() {

    }

    @Override
    public void sendError(Exception exception) {

    }

    @Override
    public void sendError(Exception exception, String message) {

    }

    @Override
    public void sendError(String message) {

    }

    @Override
    public void askQuestion(AskData data) {
        currentAskData = data;
    }

}
