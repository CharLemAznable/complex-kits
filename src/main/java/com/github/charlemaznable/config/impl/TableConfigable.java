package com.github.charlemaznable.config.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.github.charlemaznable.config.ex.ConfigException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static com.github.charlemaznable.lang.ClzPath.urlAsInputStream;

public class TableConfigable extends DefaultConfigable {

    public TableConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        Properties props = new Properties();

        Reader reader = null;
        try {
            reader = new InputStreamReader(
                    Objects.requireNonNull(urlAsInputStream(url)), Charsets.UTF_8);
            TableReader tableReader = new TableReader(reader);
            List<ConfigTable> tables = tableReader.getTables();
            for (ConfigTable table : tables) {
                String tableName = table.getTableName();
                if (props.containsKey(tableName)) {
                    throw new ConfigException(
                            "duplicate key [" + tableName + "] in file...");
                }
                props.put(tableName, table);
            }
        } catch (IOException ignored) {
        } finally {
            Closeables.closeQuietly(reader);
        }
        return props;
    }
}
