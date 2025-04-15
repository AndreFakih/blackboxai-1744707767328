package com.elvecha.ui.models;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CustomTableModel extends AbstractTableModel {
    private List<String> columnNames;
    private List<List<Object>> data;

    public CustomTableModel(List<String> columnNames) {
        this.columnNames = columnNames;
        this.data = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        data.get(rowIndex).set(columnIndex, value);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Make specific columns editable based on requirements
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (data.isEmpty()) {
            return Object.class;
        }
        return getValueAt(0, columnIndex).getClass();
    }

    public void addRow(List<Object> rowData) {
        data.add(rowData);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void removeRow(int rowIndex) {
        data.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void setData(List<List<Object>> newData) {
        data = newData;
        fireTableDataChanged();
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void clearData() {
        data.clear();
        fireTableDataChanged();
    }
}
