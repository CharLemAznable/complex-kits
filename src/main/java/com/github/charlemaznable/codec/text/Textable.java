package com.github.charlemaznable.codec.text;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.github.charlemaznable.codec.Json.desc;
import static com.github.charlemaznable.lang.Listt.isNotEmpty;
import static com.github.charlemaznable.lang.Mapp.newHashMap;
import static com.github.charlemaznable.lang.Str.toStr;
import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class Textable {

    public String toText() {
        return this.toText(null);
    }

    public String toText(Processor processor) {
        Map<String, String> result = newHashMap();

        Map<String, Object> describe = desc(this);
        for (String key : describe.keySet()) {
            if (isNotEmpty(excludedKeys()) &&
                    excludedKeys().contains(key)) continue;

            String value = toStr(describe.get(key));
            if (isEmpty(value)) continue;
            result.put(key, processor == null ?
                    value : processor.process(value));
        }

        return on(entrySeparator()).withKeyValueSeparator(
                keyValueSeparator()).join(new TreeMap<>(result));
    }

    protected List<String> excludedKeys() {
        return newArrayList("sign");
    }

    protected String entrySeparator() {
        return "&";
    }

    protected String keyValueSeparator() {
        return "=";
    }

    public interface Processor {

        String process(String origin);
    }
}
