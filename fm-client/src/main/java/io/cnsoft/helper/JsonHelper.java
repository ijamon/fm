package io.cnsoft.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by maxim on 02.12.15.
 */
public class JsonHelper {

    //public static final Gson gson = new GsonBuilder().serializeNulls().create();
    public static final Gson gson = new GsonBuilder()
                                            .setPrettyPrinting()
                                            .create();

    private JsonHelper() { throw new IllegalAccessError(); }

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static <T> T fromJson(String data, Class<T> expectedType){
        return gson.fromJson(data, expectedType);
    }
}
