package com.elvecha.ui.renderers;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CustomTableRendererTest {
    private CustomTableRenderer renderer;
    private JTable table;
    private DefaultTableModel tableModel;

    @Before
    public void setUp() {
        // Create renderer with type column index 2
        renderer = new CustomTableRenderer(2);
        
        // Create test table
        String[] columnNames = {"No", "Name", "Type", "Value"};
        Object[][] data = {
            {1, "Test 1", "Benefit", 100.0},
            {2, "Test 2", "Cost", 200.0},
            {3, "Test 3", "Benefit", 300.0}
        };
        tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);
    }

    @Test
    public void testBasicRendering() {
        Component c = renderer.getTableCellRendererComponent(
            table, "Test", false, false, 0, 0);
        
        assertNotNull("Rendered component should not be null", c);
        assertTrue("Renderer should return a JLabel", c instanceof JLabel);
    }

    @Test
    public void testBenefitTypeRendering() {
        Component c = renderer.getTableCellRendererComponent(
            table, "Benefit", false, false, 0, 2);
        
        Color backgroundColor = c.getBackground();
        assertNotNull("Background color should be set", backgroundColor);
        // Verify it's using the benefit color (light green)
        assertEquals(new Color(230, 255, 230), backgroundColor);
    }

    @Test
    public void testCostTypeRendering() {
        Component c = renderer.getTableCellRendererComponent(
            table, "Cost", false, false, 0, 2);
        
        Color backgroundColor = c.getBackground();
        assertNotNull("Background color should be set", backgroundColor);
        // Verify it's using the cost color (light red)
        assertEquals(new Color(255, 230, 230), backgroundColor);
    }

    @Test
    public void testHoverEffect() {
        // Set hovered row
        renderer.setHoveredRow(1);
        
        Component c = renderer.getTableCellRendererComponent(
            table, "Test", false, false, 1, 0);
        
        Color backgroundColor = c.getBackground();
        assertNotNull("Background color should be set", backgroundColor);
        // Verify it's using the hover color
        assertEquals(new Color(242, 242, 242), backgroundColor);
    }

    @Test
    public void testAlternateRowColors() {
        // Test even row
        Component c1 = renderer.getTableCellRendererComponent(
            table, "Test", false, false, 0, 1);
        assertEquals("Even row should be white", 
            Color.WHITE, c1.getBackground());

        // Test odd row
        Component c2 = renderer.getTableCellRendererComponent(
            table, "Test", false, false, 1, 1);
        assertEquals("Odd row should be light gray", 
            new Color(250, 250, 250), c2.getBackground());
    }

    @Test
    public void testSelectionRendering() {
        Component c = renderer.getTableCellRendererComponent(
            table, "Test", true, false, 0, 0);
        
        assertEquals("Selected cell should use table's selection background",
            table.getSelectionBackground(), c.getBackground());
        assertEquals("Selected cell should use table's selection foreground",
            table.getSelectionForeground(), c.getForeground());
    }

    @Test
    public void testBorderRendering() {
        JLabel label = (JLabel) renderer.getTableCellRendererComponent(
            table, "Test", false, false, 0, 0);
        
        assertNotNull("Border should be set", label.getBorder());
    }

    @Test
    public void testNumericAlignment() {
        // Test numeric value
        JLabel label1 = (JLabel) renderer.getTableCellRendererComponent(
            table, 100.0, false, false, 0, 3);
        assertEquals("Numeric values should be right-aligned",
            SwingConstants.RIGHT, label1.getHorizontalAlignment());

        // Test non-numeric value
        JLabel label2 = (JLabel) renderer.getTableCellRendererComponent(
            table, "Text", false, false, 0, 1);
        assertEquals("Non-numeric values should be center-aligned",
            SwingConstants.CENTER, label2.getHorizontalAlignment());
    }

    @Test
    public void testNullValueRendering() {
        Component c = renderer.getTableCellRendererComponent(
            table, null, false, false, 0, 0);
        
        assertNotNull("Component should be created for null value", c);
        assertTrue("Renderer should handle null values", c instanceof JLabel);
    }

    @Test
    public void testTypeColumnBoundary() {
        // Test with type column index beyond table columns
        CustomTableRenderer boundaryRenderer = new CustomTableRenderer(999);
        Component c = boundaryRenderer.getTableCellRendererComponent(
            table, "Test", false, false, 0, 0);
        
        assertNotNull("Should handle invalid type column index", c);
    }

    @Test
    public void testHoverRowBoundary() {
        // Set invalid hover row
        renderer.setHoveredRow(999);
        
        Component c = renderer.getTableCellRendererComponent(
            table, "Test", false, false, 0, 0);
        
        assertNotNull("Should handle invalid hover row", c);
        // Should use default background for non-hovered rows
        assertNotEquals(new Color(242, 242, 242), c.getBackground());
    }
}
