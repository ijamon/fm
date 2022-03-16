package io.cnsoft.notifier.bridge;

import org.json.JSONObject;

/**
 * Created by Jamon on 23.05.2016.
 */
public class NodeBridgeImpl implements NotifierBridge {

    private static JsNotifierBridge invokePoint;

    /*
    Вызывается из JS, устанавливая js объект с заданным интерфейсом  (JsNotifierBridge) и js коллбеками
     */
    public static void setInvokePoint(JsNotifierBridge jsInvokePoint){
        invokePoint = jsInvokePoint;
    }

    @Override
    public void execute(Methods method, Object data) {
        if(invokePoint == null || data == null) return;

        JSONObject json = new JSONObject(data);
        String jsonData = json.toString();

        invokePoint.executeMethodWithData(method.getMethodName(), jsonData);
    }

}
