package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.Configable;
import com.github.charlemaznable.core.config.ex.ConfigNotFoundException;
import com.github.charlemaznable.core.config.ex.ConfigValueFormatException;
import lombok.val;

import java.util.List;
import java.util.regex.Pattern;

import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Json.unJsonArray;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public abstract class BaseConfigable implements Configable {

    private static final String CONFIG_NOT_FOUND = " not found in config system";
    private static final String CONFIG_FORMAT_PREFIX = "'s value [";
    private static Pattern numberPattern = Pattern
            .compile("(-?[0-9]+\\.[0-9]*|[0-9]*\\.[0-9]+|-?[0-9]+).*");

    @Override
    public final int getInt(String key) {
        return parseInt(key, getStr(key));
    }

    public final int parseInt(String key, String str) {
        if (isEmpty(str)) throw new ConfigNotFoundException(key + CONFIG_NOT_FOUND);

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key
                    + CONFIG_FORMAT_PREFIX + str + "] is not an int");

        val intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return 0;

        return Integer.valueOf(intStr);
    }

    @Override
    public final long getLong(String key) {
        return parseLong(key, getStr(key));
    }

    public final long parseLong(String key, String str) {
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + CONFIG_NOT_FOUND);

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key
                    + CONFIG_FORMAT_PREFIX + str + "] is not a long");

        val intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return 0;

        return Long.valueOf(intStr);
    }

    @Override
    public final boolean getBool(String key) {
        return parseBool(key, getStr(key));
    }

    public final boolean parseBool(String key, String str) {
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + CONFIG_NOT_FOUND);

        return toBool(str);
    }

    @Override
    public final float getFloat(String key) {
        return parseFloat(key, getStr(key));
    }

    public final float parseFloat(String key, String str) {
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + CONFIG_NOT_FOUND);

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key
                    + CONFIG_FORMAT_PREFIX + str + "] is not a float");

        return Float.valueOf(matcher.group(1));
    }

    @Override
    public final double getDouble(String key) {
        return parseDouble(key, getStr(key));
    }

    public final double parseDouble(String key, String str) {
        if (isEmpty(str))
            throw new ConfigNotFoundException(key + CONFIG_NOT_FOUND);

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches())
            throw new ConfigValueFormatException(key
                    + CONFIG_FORMAT_PREFIX + str + "] is not a double");

        return Double.valueOf(matcher.group(1));
    }

    @Override
    public final int getInt(String key, int defaultValue) {
        val str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        val intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return defaultValue;

        return Integer.valueOf(intStr);
    }

    @Override
    public final long getLong(String key, long defaultValue) {
        val str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        val intStr = substringBefore(matcher.group(1), ".");
        if (isEmpty(intStr)) return defaultValue;

        return Long.valueOf(intStr);
    }

    @Override
    public final boolean getBool(String key, boolean defaultValue) {
        val str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        return toBool(str);
    }

    private boolean toBool(String str) {
        return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str)
                || "on".equalsIgnoreCase(str) || "y".equalsIgnoreCase(str);
    }

    @Override
    public final float getFloat(String key, float defaultValue) {
        val str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        return Float.valueOf(matcher.group(1));
    }

    @Override
    public final double getDouble(String key, double defaultValue) {
        val str = getStr(key);
        if (isEmpty(str)) return defaultValue;

        val matcher = numberPattern.matcher(str);
        if (!matcher.matches()) return defaultValue;

        return Double.valueOf(matcher.group(1));
    }

    @Override
    public final String getStr(String key, String defaultValue) {
        return defaultIfEmpty(getStr(key), defaultValue);
    }

    @Override
    public final <T> T getBean(String key, Class<T> beanClass) {
        return parseBean(key, getStr(key), beanClass);
    }

    public final <T> T parseBean(String key, String str, Class<T> beanClass) {
        if (isEmpty(str)) return null;

        T bean;
        try {
            bean = unJson(str, beanClass);
        } catch (Exception ex) {
            throw new ConfigValueFormatException(key + "'s value is not in JSONObject format");
        }

        if (AfterPropertiesSet.class.isAssignableFrom(beanClass))
            ((AfterPropertiesSet) bean).afterPropertiesSet();

        return bean;
    }

    @Override
    public final <T> List<T> getBeans(String key, Class<T> beanClass) {
        return parseBeans(key, getStr(key), beanClass);
    }

    public final <T> List<T> parseBeans(String key, String str, Class<T> beanClass) {
        List<T> beans = newArrayList();
        if (isEmpty(str)) return beans;

        try {
            if (str.startsWith("[")) beans = unJsonArray(str, beanClass);
            else beans.add(unJson(str, beanClass));
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
