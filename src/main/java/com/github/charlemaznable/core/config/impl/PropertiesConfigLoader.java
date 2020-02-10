package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ConfigLoader;
import com.github.charlemaznable.core.config.Configable;
import com.google.auto.service.AutoService;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.ClzPath.urlAsInputStream;
import static com.github.charlemaznable.core.spring.ClzResolver.getResources;
import static com.google.common.io.Closeables.closeQuietly;

@AutoService(ConfigLoader.class)
public final class PropertiesConfigLoader implements ConfigLoader {

    @Override
    public List<URL> loadResources(String basePath) {
        return getResources(basePath, "properties");
    }

    @Override
    public Configable loadConfigable(URL url) {
        return new DefaultConfigable(buildProperties(url));
    }

    private Properties buildProperties(URL url) {
        val properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = urlAsInputStream(url);
            properties.load(inputStream);
        } catch (IOException ignored) {
            // ignored
        } finally {
            closeQuietly(inputStream);
        }

        return properties;
    }
}
