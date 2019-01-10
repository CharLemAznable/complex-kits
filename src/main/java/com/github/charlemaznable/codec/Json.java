package com.github.charlemaznable.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.lang.Mapp.map;

public class Json {

    public static String jsonWithType(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteClassName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T unJsonWithType(String json) {
        return (T) JSON.parse(json);
    }

    public static String json(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String jsonOf(Object... keyAndValues) {
        return JSON.toJSONString(map(keyAndValues));
    }

    public static Map<String, Object> unJson(String json) {
        return JSON.parseObject(json);
    }

    public static <T> T unJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    public static List unJsonArray(String json) {
        return JSON.parseArray(json);
    }

    public static <T> List<T> unJsonArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> desc(Object obj) {
        return unJson(json(obj), Map.class);
    }

    public static <T> T spec(Map map, Class<T> clz) {
        return unJson(json(map), clz);
    }

    public static <T> T trans(Object obj, Class<T> clz) {
        return unJson(json(obj), clz);
    }
}
