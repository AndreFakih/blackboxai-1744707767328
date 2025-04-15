package com.elvecha.ui.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CustomTableModelTest {
    private CustomTableModel model;
    private List<String> columnNames;
    private List<List<Object>> testData;

    @Before
    public void setUp() {
        // Set up column names
        columnNames = Arrays.asList("No", "Name", "Value", "Status");
        model = new CustomTableModel(columnNames);

        // Set up test data
        testData = new ArrayList<>();
        testData.add(Arrays.asList(1, "Test 1", 100.0, "Active"));
        testData.add(Arrays.asList(2, "Test 2", 200.0, "Inactive"));
        testData.add(Arrays.asList(3, "Test 3", 300.0, "Active"));
    }

    @Test
    public void testInitialization() {
        assertNotNull("Model should not be null", model);
        assertEquals("Column count should match", columnNames.size(), model.getColumnCount());
        assertEquals("Initial row count should be 0", 0, model.getRowCount());
    }

    @Test
    public void testColumnNames() {
        for (int i = 0; i < columnNames.size(); i++) {
            assertEquals("Column name should match",
                columnNames.get(i), model.getColumnName(i));
        }
    }

    @Test
    public void testAddRow() {
        List<Object> rowData = Arrays.asList(1, "Test", 100.0, "Active");
        model.addRow(rowData);
        
        assertEquals("Row count should be 1", 1, model.getRowCount());
        assertEquals("Data should match", "Test", model.getValueAt(0, 1));
    }

    @Test
    public void testRemoveRow() {
        // Add test data
        model.addRow(testData.get(0));
        model.addRow(testData.get(1));
        assertEquals("Should have 2 rows", 2, model.getRowCount());

        // Remove row
        model.removeRow(0);
        assertEquals("Should have 1 row after removal", 1, model.getRowCount());
        assertEquals("Remaining data should match", 
            testData.get(1).get(1), model.getValueAt(0, 1));
    }

    @Test
    public void testSetData() {
        // Set test data
        model.setData(testData);
        
        assertEquals("Row count should match data size", 
            testData.size(), model.getRowCount());
            
        for (int i = 0; i < testData.size(); i++) {
            for (int j = 0; j < testData.get(i).size(); j++) {
                assertEquals("Data should match at " + i + "," + j,
                    testData.get(i).get(j), model.getValueAt(i, j));
            }
        }
    }

    @Test
    public void testClearData() {
        // Add test data
        model.setData(testData);
        assertTrue("Should have data", model.getRowCount() > 0);
        
        // Clear data
        model.clearData();
        assertEquals("Should have no rows after clear", 0, model.getRowCount());
    }

    @Test
    public void testGetColumnClass() {
        model.setData(testData);
        
        assertEquals("First column should be Integer", 
            Integer.class, model.getColumnClass(0));
        assertEquals("Second column should be String", 
            String.class, model.getColumnClass(1));
        assertEquals("Third column should be Double", 
            Double.class, model.getColumnClass(2));
    }

    @Test
    public void testSetValueAt() {
        // Add a row
        model.addRow(testData.get(0));
        
        // Modify value
        model.setValueAt("Modified", 0, 1);
        assertEquals("Value should be updated", 
            "Modified", model.getValueAt(0, 1));
    }

    @Test
    public void testIsCellEditable() {
        // By default, cells should not be editable
        assertFalse("Cells should not be editable by default",
            model.isCellEditable(0, 0));
    }

    @Test
    public void testNullValues() {
        List<Object> rowWithNull = Arrays.asList(1, null, 100.0, "Active");
        model.addRow(rowWithNull);
        
        assertNull("Null value should be preserved", 
            model.getValueAt(0, 1));
    }

    @Test
    public void testLargeDataset() {
        // Create large dataset
        List<List<Object>> largeData = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeData.add(Arrays.asList(i, "Test " + i, i * 100.0, "Active"));
        }
        
        // Test performance and correctness
        long startTime = System.currentTimeMillis();
        model.setData(largeData);
        long endTime = System.currentTimeMillis();
        
        assertEquals("All rows should be added", 
            1000, model.getRowCount());
        assertTrue("Setting large data should be reasonably fast", 
            (endTime - startTime) < 1000); // Should take less than 1 second
    }

    @Test
    public void testGetData() {
        model.setData(testData);
        List<List<Object>> retrievedData = model.getData();
        
        assertEquals("Retrieved data size should match", 
            testData.size(), retrievedData.size());
            
        for (int i = 0; i < testData.size(); i++) {
            assertEquals("Row data should match", 
                testData.get(i), retrievedData.get(i));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInvalidRowAccess() {
        model.getValueAt(999, 0); // Should throw exception
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInvalidColumnAccess() {
        model.getValueAt(0, 999); // Should throw exception
    }
}
