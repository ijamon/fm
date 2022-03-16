package io.cnsoft.stub;

import io.cnsoft.domain.FileWrapper;
import io.cnsoft.helper.JsonHelper;
import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.data.AskData;
import io.cnsoft.notifier.data.AskOverwriteData;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Jamon on 24.04.2016.
 */
public class AskUserBusStub  implements AskUserBus {

    private AskData currentAskData;

    private boolean cancelled;

    @Getter
    @Setter
    public AskOverwriteData.OverwriteOperations firstAnswer = null;

    @Getter
    @Setter
    public AskOverwriteData.OverwriteOperations secondAnswer = null;

    @Override
    public void processOverwriteAsk(FileWrapper source, FileWrapper dest) throws IllegalArgumentException {
        if(currentAskData != null) throw new IllegalArgumentException();

        AskOverwriteData data = new AskOverwriteData(source, dest);

        currentAskData = data;
    }

    @Override
    public void processArchiveOverwriteAsk(FileWrapper archive) throws IllegalArgumentException {
        if(currentAskData != null) throw new IllegalArgumentException();

        AskOverwriteData data = new AskOverwriteData(archive);

        currentAskData = data;
    }

    @Override
    public void answerCurrentQuestion(String data) {
        if(currentAskData == null || data == null) return;

        if(currentAskData instanceof AskOverwriteData){
            currentAskData.setResult(JsonHelper.fromJson(data, AskOverwriteData.OverwriteOperations.class));
        }
    }

    @Override
    public void awaitCurrentDataLatch() throws Exception {
        if(!firstAnswer.isDefault()) {
            answerCurrentQuestion(firstAnswer.getId().toUpperCase());
            firstAnswer = AskOverwriteData.OverwriteOperations.DEFAULT;
        } else if(!secondAnswer.isDefault()){
            answerCurrentQuestion(secondAnswer.getId().toUpperCase());
            secondAnswer = AskOverwriteData.OverwriteOperations.DEFAULT;
        }
    }

    @Override
    public AskData getAskData() {
        return currentAskData;
    }

    @Override
    public <T> T getAskResult() {
        if(currentAskData != null){
            return (T) currentAskData.getResult();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void cleanAskData() {
        currentAskData = null;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

}
