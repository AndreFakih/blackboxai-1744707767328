package com.elvecha.ui.renderers;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableRenderer extends DefaultTableCellRenderer {
    private static final Color HOVER_COLOR = new Color(242, 242, 242);
    private static final Color BENEFIT_COLOR = new Color(230, 255, 230);
    private static final Color COST_COLOR = new Color(255, 230, 230);
    private static final Color ALTERNATE_ROW_COLOR = new Color(250, 250, 250);
    
    private int hoveredRow = -1;
    private int typeColumnIndex = -1;

    public CustomTableRenderer(int typeColumnIndex) {
        this.typeColumnIndex = typeColumnIndex;
    }

    public void setHoveredRow(int row) {
        this.hoveredRow = row;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        } else {
            // Reset colors
            c.setForeground(table.getForeground());
            
            // Apply background colors based on conditions
            if (row == hoveredRow) {
                c.setBackground(HOVER_COLOR);
            } else if (typeColumnIndex >= 0 && column == typeColumnIndex) {
                // Color based on benefit/cost
                if (value != null && value.toString().equalsIgnoreCase("benefit")) {
                    c.setBackground(BENEFIT_COLOR);
                } else if (value != null && value.toString().equalsIgnoreCase("cost")) {
                    c.setBackground(COST_COLOR);
                }
            } else {
                // Alternate row colors
                c.setBackground(row % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_COLOR);
            }
        }

        // Add border
        setBorder(BorderFactory.createCompoundBorder(
            getBorder(),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));

        // Center align the content
        setHorizontalAlignment(SwingConstants.CENTER);

        // Make numbers right-aligned
        if (value instanceof Number) {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        return c;
    }
}
