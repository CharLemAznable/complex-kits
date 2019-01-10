package com.github.charlemaznable.config.impl;

import com.github.charlemaznable.config.ex.ConfigException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TableReader {

    private static final String COMMENT = "#";

    private static final String ROW_PREFIX = "#!";

    private List<ConfigTable> tables = new ArrayList<>();

    private String tableName = "";

    private Set<Integer> rowKeyIndex = new HashSet<>();

    private ConfigTable configTable = null;

    private String[] cols = null;

    public TableReader(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        dealEachLine(bufferedReader);
        tables.add(configTable);
    }

    private static boolean isCommentLine(String line) {
        return line != null && line.startsWith(COMMENT) && !line.startsWith(ROW_PREFIX);
    }

    private static boolean isRowCols(String line) {
        return line != null && line.startsWith(ROW_PREFIX);
    }

    private static String generateTableNameFromLine(String line) {
        String trimLine = StringUtils.trim(line);
        return StringUtils.trim(StringUtils.substring(trimLine, 1, trimLine.length() - 1));
    }

    private static boolean isTableName(String line) {
        return line != null && line.startsWith("[") && line.endsWith("]");
    }

    private void dealEachLine(BufferedReader bufferedReader) throws IOException {
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader
                .readLine()) {
            if (StringUtils.isEmpty(line) || isCommentLine(line)) continue;

            line = StringUtils.trim(line);
            if (isTableName(line)) {
                doWhenIsTableName(line);
                continue;
            }

            if (isRowCols(line)) {
                doWhenIsRowCols(line);
                continue;
            }

            doWhenIsData(line);
        }
    }

    private void doWhenIsData(String line) {
        String[] splitLine = new CSVLineReader().parseLine(line);
        ConfigRow row = new ConfigRow();
        StringBuilder rowKey = new StringBuilder();
        for (int i = 0; i < splitLine.length; i++) {
            String value = StringUtils.trim(splitLine[i]);
            if (rowKeyIndex.contains(i)) {
                rowKey.append(value);
            }
            ConfigCell cell = new ConfigCell(cols[i], value);
            row.addCell(cell);
        }
        String rowKeyStr = rowKey.toString();
        if (StringUtils.isEmpty(rowKeyStr)) {
            throw new ConfigException(
                    "table [" + tableName + "] config has no rowKey!");
        }
        row.setRowKey(rowKeyStr);
        configTable.addRow(row);
    }

    private void doWhenIsRowCols(String line) {
        String[] splitLine = StringUtils.split(
                StringUtils.substring(line, 2, line.length()), ',');
        cols = new String[splitLine.length];
        for (int i = 0; i < splitLine.length; i++) {
            String str = StringUtils.trim(splitLine[i]);
            if (StringUtils.endsWith(str, "*")) {
                str = StringUtils.substringBeforeLast(str, "*");
                rowKeyIndex.add(i);
            }
            cols[i] = str;
        }
    }

    private void doWhenIsTableName(String line) {
        if (null != configTable) tables.add(configTable);
        tableName = generateTableNameFromLine(line);
        configTable = new ConfigTable(tableName);
        rowKeyIndex.clear();
    }

    public List<ConfigTable> getTables() {
        return tables;
    }

    public void setTables(List<ConfigTable> tables) {
        this.tables = tables;
    }
}
