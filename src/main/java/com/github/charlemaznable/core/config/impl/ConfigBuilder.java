package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.Configable;

import java.util.Properties;

public class ConfigBuilder implements DefConfigSetter {

    private Properties properties;

    @Override
    public void setDefConfig(Configable defConfig) {
        properties = new Properties(defConfig != null ? defConfig.getProperties() : null);
    }

    public void addConfig(Configable config) {
        properties.putAll(config.getProperties());
    }

    public Configable buildConfig() {
        return new DefaultConfigable(properties);
    }
}
