package com.github.charlemaznable.core.codec;

import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.alibaba.fastjson.JSON.parse;
import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.github.charlemaznable.core.lang.Empty.isEmpty;
import static com.github.charlemaznable.core.lang.Iterablee.forEach;
import static com.github.charlemaznable.core.lang.Mapp.map;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;

public class Json {

    public static String jsonWithType(Object obj) {
        return toJSONString(obj, SerializerFeature.WriteClassName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T unJsonWithType(String json) {
        return (T) parse(json);
    }

    public static String jsonPretty(Object obj) {
        return toJSONString(obj, SerializerFeature.PrettyFormat);
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

    public static Map<String, String> descFlat(Object obj) {
        Map<String, String> flat = newHashMap();
        Map<String, Object> desc = desc(obj);
        flatMapping("", desc, flat);
        return flat;
    }

    @SuppressWarnings("unchecked")
    private static void flatMapping(String key, Map<String, Object> desc, Map<String, String> target) {
        if (isEmpty(desc)) return;

        desc.forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String k, Object v) {
                String mk = mappingKey(key, k);
                if (v instanceof Map) {
                    flatMapping(mk, (Map<String, Object>) v, target);

                } else if (v instanceof Collection) {
                    val collection = (Collection<Object>) v;
                    forEach(collection, (index, item) -> accept(
                            mk + "[" + index + "]", item));

                } else {
                    target.put(mk, v.toString());
                }
            }
        });
    }

    private static String mappingKey(String superKey, String subKey) {
        return isEmpty(superKey) ? subKey : superKey + "." + subKey;
    }
}
