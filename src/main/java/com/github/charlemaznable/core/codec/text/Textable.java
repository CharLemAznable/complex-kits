package com.github.charlemaznable.core.codec.text;

import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.codec.Json.desc;
import static com.github.charlemaznable.core.lang.Listt.isNotEmpty;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class Textable {

    public static final String DEFAULT_ENTRY_SEPARATOR = "&";
    public static final String DEFAULT_KEY_VALUE_SEPARATOR = "=";

    public final String toText() {
        return this.toText(defaultProcessor());
    }

    public final String toText(Processor processor) {
        Map<String, String> result = newHashMap();

        Map<String, Object> describe = desc(this);
        for (val entry : describe.entrySet()) {
            val key = entry.getKey();
            val value = toStr(entry.getValue());
            if ((isNotEmpty(excludedKeys()) &&
                    excludedKeys().contains(key)) ||
                    isEmpty(value)) continue;

            result.put(key, isNull(processor) ?
                    value : processor.process(value));
        }

        return new TreeMap<>(result).entrySet().stream()
                .map(e -> e.getKey() + keyValueSeparator() + e.getValue())
                .collect(Collectors.joining(entrySeparator()));
    }

    protected List<String> excludedKeys() {
        return newArrayList();
    }

    protected String entrySeparator() {
        return DEFAULT_ENTRY_SEPARATOR;
    }

    protected String keyValueSeparator() {
        return DEFAULT_KEY_VALUE_SEPARATOR;
    }

    protected Processor defaultProcessor() {
        return null;
    }

    public interface Processor {

        String process(String origin);
    }
}
