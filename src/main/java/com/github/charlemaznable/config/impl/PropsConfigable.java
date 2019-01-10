package com.github.charlemaznable.config.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.github.charlemaznable.config.ex.ConfigException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.urlAsInputStream;

public class PropsConfigable extends DefaultConfigable {

    public PropsConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        PropsReader reader = null;
        Properties props = new Properties();
        try {
            reader = new PropsReader(new InputStreamReader(
                    Objects.requireNonNull(urlAsInputStream(url)), Charsets.UTF_8));
            while (reader.nextProperty()) {
                String propertyName = reader.getPropertyName();
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
            Closeables.closeQuietly(reader);
        }
    }
}
