package com.ruoyi.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import java.util.Iterator;
import java.util.List;

/**
 * Created by admin on 2016/4/13.
 */
public class JsonUtil {

    static {
        System.setProperty("fastjson.compatibleWithJavaBean", "true");
    }

    /**
     * 对象转JSON
     * @param object
     * @return
     */
    public static String object2Json(Object object) {
        if (object == null) {
            return null;
        }
        return JSONObject.toJSONString(object);
    }

    /**
     * 从字符型Json获取对象
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getObjectFromJson(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * 从Json获取对象列表
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getObjectListFromJson(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        return JSON.parseArray(json, clazz);
    }

    /**
     * 从Json获取JSON对象
     * @param json
     * @return
     */
    public static JSONObject getJSONObjectFromJson(String json) {
        if (json == null) {
            return null;
        }
        return JSONObject.parseObject(json);
    }

    /**
     * 从Obj获取JSON对象
     * @param object
     * @return
     */
    public static JSONObject getJSONObjectFromObj(Object object) {
        if (object == null) {
            return null;
        }
        return (JSONObject) JSONObject.toJSON(object);
    }
}
