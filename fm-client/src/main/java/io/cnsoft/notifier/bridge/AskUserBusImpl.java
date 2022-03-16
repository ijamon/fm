package io.cnsoft.notifier.bridge;

import io.cnsoft.domain.FileWrapper;
import io.cnsoft.helper.JsonHelper;
import io.cnsoft.notifier.data.AskData;
import io.cnsoft.notifier.data.AskOverwriteData;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Jamon on 24.04.2016.
 */
public class AskUserBusImpl implements AskUserBus {

    private AskData currentAskData;

    private boolean cancelled;

    public void processOverwriteAsk(FileWrapper source, FileWrapper dest) throws IllegalArgumentException {
        if(currentAskData != null) throw new IllegalArgumentException();

        AskOverwriteData data = new AskOverwriteData(source, dest);
        data.setLatch(new CountDownLatch(1));

        currentAskData = data;
    }

    public void processArchiveOverwriteAsk(FileWrapper archive) throws IllegalArgumentException {
        if(currentAskData != null) throw new IllegalArgumentException();

        AskOverwriteData data = new AskOverwriteData(archive);
        data.setLatch(new CountDownLatch(1));

        currentAskData = data;
    }

    public void answerCurrentQuestion(String data) {
        if(currentAskData == null || data == null) return;

        if(currentAskData instanceof AskOverwriteData){
            currentAskData.setResult(JsonHelper.fromJson(data, AskOverwriteData.OverwriteOperations.class));
        }

        currentAskData.getLatch().countDown();
    }

    public void awaitCurrentDataLatch() throws Exception {
        if(currentAskData == null) return;

        currentAskData.getLatch().await();
    }

    public AskData getAskData(){
        return currentAskData;
    }

    public <T> T getAskResult() {
        if(currentAskData != null){
            return (T) currentAskData.getResult();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void cleanAskData() {
        currentAskData = null;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean value) {
        cancelled = value;
        answerCurrentQuestion("CANCEL");
    }

}
