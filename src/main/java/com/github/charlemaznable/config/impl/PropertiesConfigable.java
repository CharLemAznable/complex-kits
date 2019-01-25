package com.github.charlemaznable.config.impl;

import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.urlAsInputStream;
import static com.google.common.io.Closeables.closeQuietly;

public class PropertiesConfigable extends DefaultConfigable {

    public PropertiesConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        val properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = urlAsInputStream(url);
            properties.load(inputStream);
        } catch (IOException ignore) {
        } finally {
            closeQuietly(inputStream);
        }

        return properties;
    }
}
