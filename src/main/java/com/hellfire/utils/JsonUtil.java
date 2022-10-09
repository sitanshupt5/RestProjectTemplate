package com.hellfire.utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.InputStream;
import java.util.Objects;

public class JsonUtil {

    public static DocumentContext getDocumenrContext(String json) {return JsonPath.parse(json);}

    public static DocumentContext getDocumenrContext(InputStream json) {return JsonPath.parse(json);}

    public static String getNodeValue(String json, String path){
        return Objects.isNull(getDocumenrContext(json).read("$."+path))?null
                :getDocumenrContext(json).read("$."+path).toString();
    }

    public static String getNodeValue(InputStream json, String path){
        return Objects.isNull(getDocumenrContext(json).read("$."+path))?null
                :getDocumenrContext(json).read("$."+path).toString();
    }

    public static String setNodeValue(String json, String path, String valueToReplace){
        return getDocumenrContext(json).set("$."+path, valueToReplace).jsonString();
    }

}
