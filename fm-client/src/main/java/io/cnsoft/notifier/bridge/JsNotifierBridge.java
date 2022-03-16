package io.cnsoft.notifier.bridge;

/**
 * Created by Jamon on 23.05.2016.
 */
public interface JsNotifierBridge {

    void executeMethod(String method);

    void executeMethodWithData(String method, String data);

}
