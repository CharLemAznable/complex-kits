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
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

@AutoService(ConfigLoader.class)
public final class IniConfigLoader implements ConfigLoader {

    @Override
    public List<URL> loadResources(String basePath) {
        return getResources(basePath, "ini");
    }

    @Override
    public Configable loadConfigable(URL url) {
        return new DefaultConfigable(buildProperties(url));
    }

    private Properties buildProperties(URL url) {
        val props = new Properties();
        try (val reader = new InputStreamReader(
                requireNonNull(urlAsInputStream(url)), UTF_8)) {
            val iniReader = new IniReader(reader);
            for (val section : iniReader.getSections()) {
                val sectionProps = iniReader.getSection(section);
                if (isNull(sectionProps)) continue;

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
