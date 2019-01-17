package com.github.charlemaznable.config.impl;

import java.util.List;

import static com.github.charlemaznable.lang.Listt.newArrayList;

public class ConfigRow {

    private String rowKey;

    private List<ConfigCell> cells = newArrayList();

    public ConfigCell getCell(String cellName) {
        ConfigCell ret = null;
        for (ConfigCell cell : cells) {
            if (cellName.equals(cell.getCellName())) {
                ret = cell;
                break;
            }
        }
        return ret;
    }

    public ConfigCell getCell(int index) {
        return cells.get(index);
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public List<ConfigCell> getCells() {
        return cells;
    }

    public void setCells(List<ConfigCell> cells) {
        this.cells = cells;
    }

    public void addCell(ConfigCell cell) {
        cells.add(cell);
    }

    @Override
    public String toString() {
        return "ConfigRow [rowKey=" + rowKey + ", cells=" + cells + "]";
    }
}
