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

public class TableConfigable extends DefaultConfigable {

    public TableConfigable(URL url) {
        super(buildProperties(url));
    }

    private static Properties buildProperties(URL url) {
        val props = new Properties();

        Reader reader = null;
        try {
            reader = new InputStreamReader(
                    requireNonNull(urlAsInputStream(url)), UTF_8);
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
        } finally {
            closeQuietly(reader);
        }
        return props;
    }
}