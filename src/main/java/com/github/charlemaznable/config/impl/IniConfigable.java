package com.github.charlemaznable.config.impl;

import com.github.charlemaznable.config.ex.ConfigException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.urlAsInputStream;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Objects.requireNonNull;

public class IniConfigable extends DefaultConfigable {

    public IniConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        Reader reader = null;
        Properties props = new Properties();
        try {
            reader = new InputStreamReader(requireNonNull(urlAsInputStream(url)), UTF_8);
            IniReader iniReader = new IniReader(reader);
            for (String section : iniReader.getSections()) {
                Properties sectionProps = iniReader.getSection(section);
                if (sectionProps == null) continue;

                String prefix = section.equals("") ? "" : section + '.';
                for (Map.Entry<Object, Object> entry : sectionProps.entrySet()) {
                    String key = prefix + entry.getKey();

                    if (!props.containsKey(key)) {
                        props.put(key, entry.getValue().toString());
                        continue;
                    }

                    throw new ConfigException("duplicate key in file " + url
                            + " line " + iniReader.getLineNumber());
                }
            }
        } catch (IOException ex) {
            throw new ConfigException("read ini file error " + url, ex);
        } finally {
            closeQuietly(reader);
        }
        return props;
    }
}
