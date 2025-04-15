package com.elvecha.ui.panels;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import com.elvecha.ui.models.CustomTableModel;
import com.elvecha.ui.renderers.CustomTableRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class AlternativePanel extends JPanel {
    private JTable table;
    private CustomTableModel tableModel;
    private List<Alternative> alternativeList;
    private JTextField searchField;
    private List<Criteria> criteriaList;

    public AlternativePanel() {
        alternativeList = new ArrayList<>();
        initializeComponents();
        setupLayout();
        addListeners();
    }

    private void initializeComponents() {
        // Initialize table model with dynamic columns based on criteria
        List<String> columns = new ArrayList<>();
        columns.add("No");
        columns.add("Nama WO");
        if (criteriaList != null) {
            for (Criteria criteria : criteriaList) {
                columns.add(criteria.getName());
            }
        }
        columns.add("Aksi");
        
        tableModel = new CustomTableModel(columns);
        
        // Initialize table
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        
        // Set custom renderer
        CustomTableRenderer renderer = new CustomTableRenderer(-1); // No type column in alternatives
        table.setDefaultRenderer(Object.class, renderer);
        
        // Add mouse listeners for hover effect
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                renderer.setHoveredRow(row);
                table.repaint();
            }
        });
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                renderer.setHoveredRow(-1);
                table.repaint();
            }
        });

        // Search field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.decode("#f5f2e8"));

        // Search panel at top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.decode("#f5f2e8"));
        searchPanel.add(new JLabel("Cari WO: "));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Table in center with scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(Color.decode("#f5f2e8"));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addListeners() {
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });
    }

    private void search() {
        String searchText = searchField.getText().toLowerCase();
        List<List<Object>> filteredData = new ArrayList<>();
        int counter = 1;

        for (Alternative alternative : alternativeList) {
            if (alternative.getName().toLowerCase().contains(searchText)) {
                List<Object> row = createRowData(counter++, alternative);
                filteredData.add(row);
            }
        }

        tableModel.setData(filteredData);
    }

    private List<Object> createRowData(int counter, Alternative alternative) {
        List<Object> row = new ArrayList<>();
        row.add(counter);
        row.add(alternative.getName());
        
        if (criteriaList != null) {
            for (Criteria criteria : criteriaList) {
                Double value = alternative.getCriteriaValue(criteria.getName());
                row.add(value != null ? value : 0.0);
            }
        }
        
        row.add("✏️🗑️"); // Action icons
        return row;
    }

    public void handleAdd() {
        if (criteriaList == null || criteriaList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap tambahkan kriteria terlebih dahulu!");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tambah Wedding Organizer", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create form components
        JTextField nameField = new JTextField(20);
        Map<String, JTextField> criteriaFields = new HashMap<>();

        // Add name field
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nama WO:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        // Add criteria fields
        int row = 1;
        for (Criteria criteria : criteriaList) {
            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel(criteria.getName() + ":"), gbc);
            
            JTextField valueField = new JTextField(20);
            criteriaFields.put(criteria.getName(), valueField);
            
            gbc.gridx = 1;
            dialog.add(valueField, gbc);
            row++;
        }

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Simpan");
        JButton cancelButton = new JButton("Batal");

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nama WO tidak boleh kosong!");
                    return;
                }

                Alternative newAlternative = new Alternative(name);
                
                // Validate and set criteria values
                for (Criteria criteria : criteriaList) {
                    JTextField field = criteriaFields.get(criteria.getName());
                    String valueStr = field.getText().trim();
                    
                    if (valueStr.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, 
                            "Nilai untuk " + criteria.getName() + " tidak boleh kosong!");
                        return;
                    }
                    
                    try {
                        double value = Double.parseDouble(valueStr);
                        if (value < 0) {
                            JOptionPane.showMessageDialog(dialog, 
                                "Nilai untuk " + criteria.getName() + " tidak boleh negatif!");
                            return;
                        }
                        newAlternative.setCriteriaValue(criteria.getName(), value);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, 
                            "Nilai untuk " + criteria.getName() + " harus berupa angka!");
                        return;
                    }
                }

                alternativeList.add(newAlternative);
                refreshTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Terjadi kesalahan: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void handleEdit() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Wedding Organizer yang akan diedit!");
            return;
        }

        Alternative selectedAlternative = alternativeList.get(selectedRow);
        // Create edit dialog similar to handleAdd() but with pre-filled values
        // ... (implement similar to handleAdd with pre-filled values)
    }

    public void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Wedding Organizer yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus Wedding Organizer ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            alternativeList.remove(selectedRow);
            refreshTable();
        }
    }

    public void handleSave() {
        // Implement save functionality (e.g., to file or database)
        JOptionPane.showMessageDialog(this, "Data Wedding Organizer berhasil disimpan!");
    }

    private void refreshTable() {
        List<List<Object>> data = new ArrayList<>();
        int counter = 1;
        for (Alternative alternative : alternativeList) {
            List<Object> row = createRowData(counter++, alternative);
            data.add(row);
        }
        tableModel.setData(data);
    }

    public void setCriteriaList(List<Criteria> criteriaList) {
        this.criteriaList = criteriaList;
        // Reinitialize table with updated columns
        initializeComponents();
        refreshTable();
    }

    public List<Alternative> getAlternativeList() {
        return alternativeList;
    }

    public void setAlternativeList(List<Alternative> alternatives) {
        this.alternativeList = new ArrayList<>(alternatives);
        refreshTable();
    }

    public void setCriteriaList(List<Criteria> criteria) {
        this.criteriaList = new ArrayList<>(criteria);
        // Reinitialize table with updated columns
        initializeComponents();
        setupLayout();
        refreshTable();
    }
}
