package com.github.charlemaznable.codec;

import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parse;
import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.github.charlemaznable.lang.Mapp.map;

public class Json {

    public static String jsonWithType(Object obj) {
        return toJSONString(obj, SerializerFeature.WriteClassName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T unJsonWithType(String json) {
        return (T) parse(json);
    }

    public static String json(Object obj) {
        return toJSONString(obj);
    }

    public static String jsonOf(Object... keyAndValues) {
        return toJSONString(map(keyAndValues));
    }

    public static Map<String, Object> unJson(String json) {
        return parseObject(json);
    }

    public static <T> T unJson(String json, Class<T> clazz) {
        return parseObject(json, clazz);
    }

    public static List unJsonArray(String json) {
        return parseArray(json);
    }

    public static <T> List<T> unJsonArray(String json, Class<T> clazz) {
        return parseArray(json, clazz);
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
