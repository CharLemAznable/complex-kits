package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ex.ConfigException;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.ClzPath.urlAsInputStream;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Objects.requireNonNull;

public class IniConfigable extends DefaultConfigable {

    public IniConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        Reader reader = null;
        val props = new Properties();
        try {
            reader = new InputStreamReader(requireNonNull(urlAsInputStream(url)), UTF_8);
            val iniReader = new IniReader(reader);
            for (val section : iniReader.getSections()) {
                val sectionProps = iniReader.getSection(section);
                if (sectionProps == null) continue;

                val prefix = section.equals("") ? "" : section + '.';
                for (val entry : sectionProps.entrySet()) {
                    val key = prefix + entry.getKey();

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
