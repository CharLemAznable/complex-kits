package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.Configable;

import java.util.Properties;

import static java.util.Objects.nonNull;

public final class ConfigBuilder implements DefConfigSetter {

    private Properties properties;

    @Override
    public void setDefConfig(Configable defConfig) {
        properties = new Properties(nonNull(defConfig) ? defConfig.getProperties() : null);
    }

    public void addConfig(Configable config) {
        properties.putAll(config.getProperties());
    }

    public Configable buildConfig() {
        return new DefaultConfigable(properties);
    }
}
