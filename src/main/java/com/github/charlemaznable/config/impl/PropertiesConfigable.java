package com.github.charlemaznable.config.impl;

import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.urlAsInputStream;

public class PropertiesConfigable extends DefaultConfigable {

    public PropertiesConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = urlAsInputStream(url);
            properties.load(inputStream);
        } catch (IOException ignore) {
        } finally {
            Closeables.closeQuietly(inputStream);
        }

        return properties;
    }
}
