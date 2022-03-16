package io.cnsoft.progressTests;

import io.cnsoft.notifier.bridge.NotifierBridge;
import io.cnsoft.notifier.data.StartOperationData;
import io.cnsoft.notifier.data.UpdateProgressData;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.notifier.progress.ProgressNotifierImpl;
import io.cnsoft.notifier.progress.ProgressSession;
import io.cnsoft.stub.NotifierBridgeStub;
import org.junit.Assert;
import org.junit.Test;
import io.cnsoft.stub.NotifierBridgeStub;

/**
 * Created by Jamon on 01.03.2016.
 */
public class ProgressNotifierTests {

    private final double delta = 0.001;

    @Test
    public void initializeNotifierFullCycleTest(){
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);
        notifier.initializeNotifier(ProgressNotifier.OperationTypes.COPY, ProgressSession.ProgressTypes.KnownEndpoint);

        Assert.assertEquals(NotifierBridge.Methods.START_OPERATION, bridgeStub.getCurrentMethod());

        StartOperationData data = (StartOperationData) bridgeStub.getCurrentView();
        Assert.assertNotNull(data);
        Assert.assertEquals(ProgressNotifier.OperationTypes.COPY, data.getOperationType());
    }

    @Test
    public void incrementFullCycleTest(){
        String copyMessage = "copy test message";
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        notifier.initializeNotifier(ProgressNotifier.OperationTypes.COPY, ProgressSession.ProgressTypes.KnownEndpoint);
        notifier.setMaximumProgress(200);
        notifier.incrementProgress(40, copyMessage);

        Assert.assertEquals(NotifierBridge.Methods.UPDATE_PROGRESS, bridgeStub.getCurrentMethod());

        UpdateProgressData data = (UpdateProgressData) bridgeStub.getCurrentView();
        Assert.assertNotNull(data);
        Assert.assertEquals(20.0, data.getPercantage(), delta);
    }

    @Test
    public void finishOperationFullCycleTest(){
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        notifier.initializeNotifier(ProgressNotifier.OperationTypes.COPY, ProgressSession.ProgressTypes.KnownEndpoint);
        notifier.finishOperation();

        Assert.assertEquals(NotifierBridge.Methods.FINISH_OPERATION, bridgeStub.getCurrentMethod());
    }


}



















