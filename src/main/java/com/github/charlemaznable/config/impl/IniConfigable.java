package com.github.charlemaznable.config.impl;

import com.github.charlemaznable.config.ex.ConfigException;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.urlAsInputStream;

public class IniConfigable extends DefaultConfigable {

    public IniConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        Reader reader = null;
        Properties props = new Properties();
        try {
            reader = new InputStreamReader(
                    Objects.requireNonNull(urlAsInputStream(url)), Charsets.UTF_8);
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
            Closeables.closeQuietly(reader);
        }
        return props;
    }
}
