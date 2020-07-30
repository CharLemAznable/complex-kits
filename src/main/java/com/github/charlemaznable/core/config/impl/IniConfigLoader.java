package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ConfigLoader;
import com.github.charlemaznable.core.config.Configable;
import com.github.charlemaznable.core.config.ex.ConfigException;
import com.google.auto.service.AutoService;

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
        var props = new Properties();
        try (var reader = new InputStreamReader(
                requireNonNull(urlAsInputStream(url)), UTF_8)) {
            var iniReader = new IniReader(reader);
            for (var section : iniReader.getSections()) {
                var sectionProps = iniReader.getSection(section);
                if (isNull(sectionProps)) continue;

                var prefix = section.equals("") ? "" : section + '.';
                for (var entry : sectionProps.entrySet()) {
                    var key = prefix + entry.getKey();

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
