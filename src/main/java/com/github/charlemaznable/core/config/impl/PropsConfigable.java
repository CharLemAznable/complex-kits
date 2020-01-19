package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ex.ConfigException;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.ClzPath.urlAsInputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public final class PropsConfigable extends DefaultConfigable {

    public PropsConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        val props = new Properties();
        try (PropsReader reader = new PropsReader(new InputStreamReader(
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
