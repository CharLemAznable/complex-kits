package com.github.charlemaznable.core.config.impl;

import com.github.charlemaznable.core.config.ex.ConfigException;
import lombok.val;
import lombok.var;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;
import static org.apache.commons.lang3.StringUtils.trim;

public class TableReader {

    private static final String COMMENT = "#";

    private static final String ROW_PREFIX = "#!";

    private List<ConfigTable> tables = newArrayList();

    private String tableName = "";

    private Set<Integer> rowKeyIndex = newHashSet();

    private ConfigTable configTable = null;

    private String[] cols = null;

    public TableReader(Reader reader) throws IOException {
        val bufferedReader = new BufferedReader(reader);
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
        val trimLine = trim(line);
        return trim(substring(trimLine, 1, trimLine.length() - 1));
    }

    private static boolean isTableName(String line) {
        return line != null && line.startsWith("[") && line.endsWith("]");
    }

    private void dealEachLine(BufferedReader bufferedReader) throws IOException {
        for (var line = bufferedReader.readLine(); line != null; line = bufferedReader
                .readLine()) {
            if (isEmpty(line) || isCommentLine(line)) continue;

            line = trim(line);
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
        val splitLine = new CSVLineReader().parseLine(line);
        val row = new ConfigRow();
        val rowKey = new StringBuilder();
        for (var i = 0; i < splitLine.length; i++) {
            val value = trim(splitLine[i]);
            if (rowKeyIndex.contains(i)) {
                rowKey.append(value);
            }
            val cell = new ConfigCell(cols[i], value);
            row.addCell(cell);
        }
        val rowKeyStr = rowKey.toString();
        if (isEmpty(rowKeyStr)) {
            throw new ConfigException(
                    "table [" + tableName + "] config has no rowKey!");
        }
        row.setRowKey(rowKeyStr);
        configTable.addRow(row);
    }

    private void doWhenIsRowCols(String line) {
        val splitLine = split(substring(line, 2, line.length()), ',');
        cols = new String[splitLine.length];
        for (var i = 0; i < splitLine.length; i++) {
            var str = trim(splitLine[i]);
            if (endsWith(str, "*")) {
                str = substringBeforeLast(str, "*");
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
