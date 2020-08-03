package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ConfigLoader;
import com.github.charlemaznable.core.config.Configable;
import com.github.charlemaznable.core.config.ex.ConfigException;
import com.google.auto.service.AutoService;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.ClzPath.urlAsInputStream;
import static com.github.charlemaznable.core.spring.ClzResolver.getResources;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

@AutoService(ConfigLoader.class)
public final class PropsConfigLoader implements ConfigLoader {

    @Override
    public List<URL> loadResources(String basePath) {
        return getResources(basePath, "props");
    }

    @Override
    public Configable loadConfigable(URL url) {
        return new DefaultConfigable(buildProperties(url));
    }

    private Properties buildProperties(URL url) {
        val props = new Properties();
        try (val reader = new PropsReader(new InputStreamReader(
                requireNonNull(urlAsInputStream(url)), UTF_8))) {
            while (reader.nextProperty()) {
                val propertyName = reader.getPropertyName();
                if (props.containsKey(propertyName)) {
                    throw new ConfigException("duplicate key ["
                            + propertyName + "] in file...");
                }
                props.put(propertyName, reader.getPropertyValue());
            }
            return props;

        } catch (IOException ex) {
            throw new ConfigException("read props file error: ", ex);
        }
    }
}
