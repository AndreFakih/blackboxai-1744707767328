package com.elvecha.ui.panels;

import com.elvecha.model.Criteria;
import com.elvecha.ui.models.CustomTableModel;
import com.elvecha.ui.renderers.CustomTableRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class CriteriaPanel extends JPanel {
    private JTable table;
    private CustomTableModel tableModel;
    private List<Criteria> criteriaList;
    private JTextField searchField;
    
    public CriteriaPanel() {
        criteriaList = new ArrayList<>();
        initializeComponents();
        setupLayout();
        addListeners();
    }

    private void initializeComponents() {
        // Initialize table model with columns
        List<String> columns = Arrays.asList("No", "Nama Kriteria", "Bobot", "Jenis");
        tableModel = new CustomTableModel(columns);
        
        // Initialize table
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        
        // Set custom renderer
        CustomTableRenderer renderer = new CustomTableRenderer(3); // 3 is the index of "Jenis" column
        table.setDefaultRenderer(Object.class, renderer);
        
        // Add mouse listener for hover effect
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
        searchPanel.add(new JLabel("Cari: "));
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

        for (Criteria criteria : criteriaList) {
            if (criteria.getName().toLowerCase().contains(searchText)) {
                List<Object> row = new ArrayList<>();
                row.add(counter++);
                row.add(criteria.getName());
                row.add(criteria.getWeight());
                row.add(criteria.getType());
                filteredData.add(row);
            }
        }

        tableModel.setData(filteredData);
    }

    public void handleAdd() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tambah Kriteria", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create form components
        JTextField nameField = new JTextField(20);
        JTextField weightField = new JTextField(20);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Benefit", "Cost"});

        // Add components to dialog
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nama Kriteria:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Bobot:"), gbc);
        gbc.gridx = 1;
        dialog.add(weightField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Jenis:"), gbc);
        gbc.gridx = 1;
        dialog.add(typeCombo, gbc);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Simpan");
        JButton cancelButton = new JButton("Batal");

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double weight = Double.parseDouble(weightField.getText().trim());
                String type = (String) typeCombo.getSelectedItem();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nama kriteria tidak boleh kosong!");
                    return;
                }

                if (weight <= 0 || weight > 1) {
                    JOptionPane.showMessageDialog(dialog, "Bobot harus antara 0 dan 1!");
                    return;
                }

                Criteria newCriteria = new Criteria(name, weight, type);
                criteriaList.add(newCriteria);
                refreshTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Bobot harus berupa angka!");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void handleEdit() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kriteria yang akan diedit!");
            return;
        }

        Criteria selectedCriteria = criteriaList.get(selectedRow);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Kriteria", true);
        // Similar to handleAdd() but with pre-filled values
        // ... (implement similar to handleAdd with pre-filled values)
    }

    public void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kriteria yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus kriteria ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            criteriaList.remove(selectedRow);
            refreshTable();
        }
    }

    public void handleSave() {
        // Implement save functionality (e.g., to file or database)
        JOptionPane.showMessageDialog(this, "Data kriteria berhasil disimpan!");
    }

    private void refreshTable() {
        List<List<Object>> data = new ArrayList<>();
        int counter = 1;
        for (Criteria criteria : criteriaList) {
            List<Object> row = new ArrayList<>();
            row.add(counter++);
            row.add(criteria.getName());
            row.add(criteria.getWeight());
            row.add(criteria.getType());
            data.add(row);
        }
        tableModel.setData(data);
    }

    public List<Criteria> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<Criteria> criteria) {
        this.criteriaList = new ArrayList<>(criteria);
        refreshTable();
    }
}
