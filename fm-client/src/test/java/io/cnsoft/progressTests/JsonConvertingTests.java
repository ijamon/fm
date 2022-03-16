package io.cnsoft.progressTests;

import io.cnsoft.helper.JsonHelper;
import io.cnsoft.notifier.data.AskOverwriteData;
import io.cnsoft.notifier.data.StartOperationData;
import io.cnsoft.notifier.data.UpdateProgressData;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.notifier.progress.ProgressSession;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Jamon on 13.03.2016.
 */
public class JsonConvertingTests {

    @Test
    public void progressViewConvertingTest(){
        //{"identifier":{"id":"commonProgress"},"percantageView":"21.23","percantage":21.23,"message":"test message"}
        String expectedJson = "{\"message\":\"test message\",\"percantage\":21.23}";

        UpdateProgressData data = new UpdateProgressData(21.23, "test message");
        JSONObject converter = new JSONObject(data);
        String json = converter.toString();

        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void operationViewConvertingTest() {
        //{"operationType":{"operationView":"Копирование","operationId":"copy"},"gamut":{"id":"commonAndPartProgress"}}
        String expectedJson = "{\"operationType\":{\"operationView\":\"Копирование\",\"default\":false,\"operationId\":\"copy\"},\"progressType\":{\"id\":\"knownEndpoint\"}}";

        StartOperationData data = new StartOperationData(ProgressNotifier.OperationTypes.COPY, ProgressSession.ProgressTypes.KnownEndpoint);

        JSONObject converter = new JSONObject(data);
        String json = converter.toString();

        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void askAnswersTest() {
        boolean boolResult = JsonHelper.fromJson("true", Boolean.class);
        Assert.assertEquals(true, boolResult);

        AskOverwriteData.OverwriteOperations overwriteResult = JsonHelper.fromJson("OVERWRITE_ALL", AskOverwriteData.OverwriteOperations.class);
        Assert.assertEquals(AskOverwriteData.OverwriteOperations.OVERWRITE_ALL, overwriteResult);
    }

}




















