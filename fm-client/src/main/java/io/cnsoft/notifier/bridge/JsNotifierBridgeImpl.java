package io.cnsoft.notifier.bridge;

import org.json.JSONObject;
import java.applet.Applet;

//import ru.real.fs.FileSystemApplet;

/**
 * Created by Jamon on 01.03.2016.
 */
public class JsNotifierBridgeImpl implements NotifierBridge {

    public void execute(Methods method, Object data) {

    }

    /*
    private Applet applet;

    private JSObject window;

    private final Object[] emptyArgument = new Object[]{};

    public JsNotifierBridgeImpl(){
        this(FileSystemApplet.getInstance());
    }

    public JsNotifierBridgeImpl(){

    }

    public JsNotifierBridgeImpl(Applet applet) {
        this.applet = applet;
        window = JSObject.getWindow(applet);
    }

    public void execute(Methods method){
        window.call(method.getMethodName(), emptyArgument);
    }

    public void execute(Methods method, Object data) {
        if(data == null) return;

        JSONObject json = new JSONObject(data);
        String jsonString = json.toString();
        Object[] arguments = new Object[]{jsonString};

        window.call(method.getMethodName(), arguments);
    }
    */
}
