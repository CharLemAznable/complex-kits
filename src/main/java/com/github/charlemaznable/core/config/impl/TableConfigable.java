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

public class TableConfigable extends DefaultConfigable {

    public TableConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        val props = new Properties();

        try (Reader reader = new InputStreamReader(
                requireNonNull(urlAsInputStream(url)), UTF_8)) {
            val tableReader = new TableReader(reader);
            val tables = tableReader.getTables();
            for (val table : tables) {
                val tableName = table.getTableName();
                if (props.containsKey(tableName)) {
                    throw new ConfigException(
                            "duplicate key [" + tableName + "] in file...");
                }
                props.put(tableName, table);
            }
        } catch (IOException ignored) {
            // ignored
        }
        return props;
    }
}
