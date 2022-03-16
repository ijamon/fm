package io.cnsoft.notifier.progress;

import io.cnsoft.domain.ExceptionStub;
import io.cnsoft.notifier.bridge.NodeBridgeImpl;
import io.cnsoft.notifier.bridge.NotifierBridge;
import io.cnsoft.notifier.data.AskData;
import io.cnsoft.notifier.data.StartOperationData;
import io.cnsoft.notifier.data.UpdateProgressData;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Created by Jamon on 06.03.2016.
 */
public class ProgressNotifierImpl implements ProgressNotifier {

    private ProgressSession session;

    private OperationTypes operation = OperationTypes.DEFAULT;

    private NotifierBridge notifierBridge;

    public ProgressNotifierImpl() {
        this(new NodeBridgeImpl());
    }

    public ProgressNotifierImpl(NotifierBridge notifierBridge) {
        this.notifierBridge = notifierBridge;
    }

    @Override
    public void initializeNotifier(OperationTypes operationType, ProgressSession.ProgressTypes progressType) {
        if (!operation.isDefault()) return;

        operation = operationType;
        session = new ProgressSession(progressType);

        StartOperationData data = new StartOperationData(operation, progressType);
        notifierBridge.execute(NotifierBridge.Methods.START_OPERATION, data);
    }

    @Override
    public void setMaximumProgress(long maximum) {
        if(session == null) return;

        session.setMaximumProgress(maximum);
    }

    @Override
    public void incrementProgress(long increment) {
        incrementProgress(increment, null);
    }

    @Override
    public void incrementProgress(long increment, String message) {
        if(session == null) return;

        //internal
        session.incrementProgress(increment);

        //dto
        UpdateProgressData data = new UpdateProgressData(session.getProgressPercantage(), message);

        //outside level
        notifierBridge.execute(NotifierBridge.Methods.UPDATE_PROGRESS, data);
    }

    @Override
    public void updateProgress(long currentProgress) {
        updateProgress(currentProgress, null);
    }

    @Override
    public void updateProgress(long currentProgress, String message) {
        if(session == null) return;

        session.updateProgress(currentProgress);

        UpdateProgressData data = new UpdateProgressData(session.getProgressPercantage(), message);

        notifierBridge.execute(NotifierBridge.Methods.UPDATE_PROGRESS, data);
    }

    @Override
    public void finishOperation() {
        if (operation.isDefault()) return;

        OperationTypes operationForNotifier = operation;
        operation = OperationTypes.DEFAULT;
        notifierBridge.execute(NotifierBridge.Methods.FINISH_OPERATION, operationForNotifier);
    }

    @Override
    public void setMessage(String message) {
        if (operation.isDefault()) return;

        UpdateProgressData data = new UpdateProgressData(message);
        notifierBridge.execute(NotifierBridge.Methods.SET_MESSAGE, data);
    }

    @Override
    public void askQuestion(AskData data) {
        if (operation.isDefault()) return;

        notifierBridge.execute(NotifierBridge.Methods.ASK_QUESTION, data);
    }

    @Override
    public void sendError(String message) {
        if (operation.isDefault()) return;

        ExceptionStub exStub = new ExceptionStub(message);
        notifierBridge.execute(NotifierBridge.Methods.SEND_ERROR, exStub);
    }

    @Override
    public void sendError(Exception exception) {
        if (operation.isDefault()) return;

        String message = ExceptionUtils.getMessage(exception);
        String stackTrace = ExceptionUtils.getStackTrace(exception);

        ExceptionStub exStub = new ExceptionStub(message, stackTrace);

        notifierBridge.execute(NotifierBridge.Methods.SEND_ERROR, exStub);
    }

    @Override
    public void sendError(Exception exception, String message) {
        if (operation.isDefault()) return;

        String exMessage = ExceptionUtils.getMessage(exception);
        String fullMessage = String.format("%s %s", message, exMessage);
        String stackTrace = ExceptionUtils.getStackTrace(exception);

        ExceptionStub exStub = new ExceptionStub(fullMessage, stackTrace);

        notifierBridge.execute(NotifierBridge.Methods.SEND_ERROR, exStub);
    }

}



















