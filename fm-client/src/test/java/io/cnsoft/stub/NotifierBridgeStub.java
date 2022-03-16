package io.cnsoft.stub;

import io.cnsoft.notifier.bridge.NotifierBridge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jamon on 08.03.2016.
 */
public class NotifierBridgeStub implements NotifierBridge {

    @Getter
    private Object currentView;

    @Getter
    private NotifierBridge.Methods currentMethod;

    @Getter
    private Map<Integer, ExecutionModel> executionList = new HashMap<>();

    private int executionCounter;

    @Override
    public void execute(Methods method, Object data) {
        executionCounter++;

        currentMethod = method;
        currentView = data;

        executionList.put(executionCounter, new ExecutionModel(currentMethod, currentView));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionModel {
        @Getter
        private NotifierBridge.Methods method;

        @Getter
        private Object view;

        @Override
        public boolean equals(Object val){
            if(val instanceof ExecutionModel) {
                return ((ExecutionModel)val).getMethod().equals(method);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return method.hashCode();
        }
    }

}




























