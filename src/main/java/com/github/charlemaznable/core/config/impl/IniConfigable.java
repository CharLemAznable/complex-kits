package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ex.ConfigException;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import static com.github.charlemaznable.core.lang.ClzPath.urlAsInputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public final class IniConfigable extends DefaultConfigable {

    public IniConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        val props = new Properties();
        try (Reader reader = new InputStreamReader(
                requireNonNull(urlAsInputStream(url)), UTF_8)) {
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
        }
        return props;
    }
}
