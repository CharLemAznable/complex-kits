package com.github.charlemaznable.config.impl;

import com.github.charlemaznable.config.ex.ConfigException;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.urlAsInputStream;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Objects.requireNonNull;

public class PropsConfigable extends DefaultConfigable {

    public PropsConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        PropsReader reader = null;
        val props = new Properties();
        try {
            reader = new PropsReader(new InputStreamReader(
                    requireNonNull(urlAsInputStream(url)), UTF_8));
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
        } finally {
            closeQuietly(reader);
        }
    }
}
