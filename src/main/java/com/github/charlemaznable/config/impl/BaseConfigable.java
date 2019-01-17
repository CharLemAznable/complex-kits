package com.github.charlemaznable.config.impl;

import com.github.charlemaznable.config.Configable;
import com.github.charlemaznable.config.ex.ConfigNotFoundException;
import com.github.charlemaznable.config.ex.ConfigValueFormatException;
import com.github.charlemaznable.config.utils.AfterPropertiesSet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.charlemaznable.codec.Json.unJson;
import static com.github.charlemaznable.codec.Json.unJsonArray;
import static com.github.charlemaznable.lang.Listt.newArrayList;
import static com.github.charlemaznable.lang.Str.isEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public abstract class BaseConfigable implements Configable {

    private static Pattern numberPattern = Pattern
            .compile("(-?[0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+|-?[0-9]+).*");

    @Override
    public int getInt(String key) {
        String str = getStr(key);
        if (isEmpty(str)) throw new ConfigNotFoundException(key + " not found in config system");

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key + "'s value [" + str + "] is not an int");

        String intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return 0;

        return Integer.valueOf(intStr);
    }

    @Override
    public long getLong(String key) {
        String str = getStr(key);
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + " not found in config system");

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key + "'s value [" + str + "] is not a long");

        String intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return 0;

        return Long.valueOf(intStr);
    }

    @Override
    public boolean getBool(String key) {
        String str = getStr(key);
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + " not found in config system");

        return toBool(str);
    }

    @Override
    public float getFloat(String key) {
        String str = getStr(key);
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + " not found in config system");

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key + "'s value [" + str + "] is not a float");

        return Float.valueOf(matcher.group(1));
    }

    @Override
    public double getDouble(String key) {
        String str = getStr(key);
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + " not found in config system");

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key + "'s value [" + str + "] is not a double");

        return Double.valueOf(matcher.group(1));
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        String intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return defaultValue;

        return Integer.valueOf(intStr);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        String intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return defaultValue;

        return Long.valueOf(intStr);
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        String str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        return toBool(str);
    }

    private boolean toBool(String str) {
        return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str)
                || "on".equalsIgnoreCase(str) || "y".equalsIgnoreCase(str);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        String str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        return Float.valueOf(matcher.group(1));
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        String str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        Matcher matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        return Double.valueOf(matcher.group(1));
    }

    @Override
    public String getStr(String key, String defaultValue) {
        return defaultIfEmpty(getStr(key), defaultValue);
    }

    @Override
    public List<String> getKeyPrefixes() {
        List<String> keyPrefixes = newArrayList();

        for (Object key : getProperties().keySet()) {
            String strKey = (String) key;

            String keyPrefix = substringBefore(strKey, ".");
            if (!keyPrefixes.contains(keyPrefix)) keyPrefixes.add(keyPrefix);
        }

        return keyPrefixes;
    }

    @Override
    public <T> T getBean(String key, Class<T> beanClass) {
        String json = getStr(key);
        if (isEmpty(json)) return null;

        T bean;
        try {
            bean = unJson(json, beanClass);
        } catch (Exception ex) {
            throw new ConfigValueFormatException(key + "'s value is not in JSONObject format");
        }

        if (AfterPropertiesSet.class.isAssignableFrom(beanClass))
            ((AfterPropertiesSet) bean).afterPropertiesSet();

        return bean;
    }

    @Override
    public <T> List<T> getBeans(String key, Class<T> beanClass) {
        List<T> beans = newArrayList();
        String json = getStr(key);
        if (isEmpty(json)) return beans;

        try {
            if (json.startsWith("[")) beans = unJsonArray(json, beanClass);
            else beans.add(unJson(json, beanClass));
        } catch (Exception ex) {
            throw new ConfigValueFormatException(key + "'s value is not in JSONArray format");
        }

        if (AfterPropertiesSet.class.isAssignableFrom(beanClass))
            for (T bean : beans) {
                ((AfterPropertiesSet) bean).afterPropertiesSet();
            }

        return beans;
    }
}
