package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.Configable;
import lombok.val;

import java.util.Properties;

import static com.github.charlemaznable.core.codec.Base64.unBase64;
import static com.github.charlemaznable.core.crypto.AES.decrypt;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.text.StringSubstitutor.replace;

public final class DefaultConfigable extends BaseConfigable {

    private Properties properties;

    public DefaultConfigable() {
        this.properties = new Properties();
    }

    public DefaultConfigable(Properties properties) {
        this.properties = properties;
    }

    @Override
    public final boolean exists(String key) {
        return properties.containsKey(key);
    }

    @Override
    public final Properties getProperties() {
        return properties;
    }

    public final void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public final String getStr(String key) {
        String property = properties.getProperty(key);
        if (isNull(property)) return null;

        // ${key}会在properties中定义了key时进行替换，否则保持原样
        property = replace(property, properties);

        if (startsWith(property, "{AES}")) {
            property = decrypt(unBase64(property.substring(5)), "defaultconfig");
        }

        return trim(property);
    }

    @Override
    public final Configable subset(String prefix) {
        if (isEmpty(prefix)) return new DefaultConfigable(new Properties());

        val prefixMatch = prefix.charAt(prefix.length() - 1) != '.' ? prefix + '.' : prefix;
        val subProps = subProperties(properties, prefixMatch);
        return new DefaultConfigable(subProps);
    }

    public static Properties subProperties(Properties properties, String prefixMatch) {
        val subProps = new Properties();
        for (val entry : properties.entrySet()) {
            val key = (String) entry.getKey();
            if (!key.startsWith(prefixMatch)) continue;

            subProps.put(key.substring(prefixMatch.length()), entry.getValue());
        }
        return subProps;
    }
}
