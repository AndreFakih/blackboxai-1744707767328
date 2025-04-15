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

public class EvaluationPanel extends JPanel {
    private JTable table;
    private CustomTableModel tableModel;
    private List<Alternative> alternativeList;
    private List<Criteria> criteriaList;
    private JComboBox<String> filterCombo;
    private JPanel evaluationForm;

    public EvaluationPanel() {
        initializeComponents();
        setupLayout();
        addListeners();
    }

    private void initializeComponents() {
        // Initialize filter combo
        filterCombo = new JComboBox<>(new String[]{"Semua Kriteria", "Benefit", "Cost"});
        filterCombo.setFont(new Font("Arial", Font.PLAIN, 12));

        // Initialize table model with columns
        List<String> columns = new ArrayList<>();
        columns.add("No");
        columns.add("Wedding Organizer");
        if (criteriaList != null) {
            for (Criteria criteria : criteriaList) {
                columns.add(criteria.getName());
            }
        }
        columns.add("Status");

        tableModel = new CustomTableModel(columns);
        
        // Initialize table
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        
        // Set custom renderer
        CustomTableRenderer renderer = new CustomTableRenderer(-1);
        table.setDefaultRenderer(Object.class, renderer);
        
        // Initialize evaluation form
        evaluationForm = new JPanel();
        evaluationForm.setLayout(new BoxLayout(evaluationForm, BoxLayout.Y_AXIS));
        evaluationForm.setBackground(Color.decode("#f5f2e8"));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.decode("#f5f2e8"));

        // Top panel with filter
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.decode("#f5f2e8"));
        topPanel.add(new JLabel("Filter: "));
        topPanel.add(filterCombo);
        add(topPanel, BorderLayout.NORTH);

        // Split pane for table and evaluation form
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left side - Table
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(500, 400));
        
        // Right side - Evaluation form
        JScrollPane formScrollPane = new JScrollPane(evaluationForm);
        formScrollPane.setPreferredSize(new Dimension(300, 400));
        
        splitPane.setLeftComponent(tableScrollPane);
        splitPane.setRightComponent(formScrollPane);
        splitPane.setDividerLocation(500);
        
        add(splitPane, BorderLayout.CENTER);
    }

    private void addListeners() {
        filterCombo.addActionListener(e -> refreshTable());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    showEvaluationForm(selectedRow);
                }
            }
        });
    }

    private void showEvaluationForm(int selectedRow) {
        evaluationForm.removeAll();
        
        if (alternativeList == null || criteriaList == null || 
            selectedRow >= alternativeList.size()) {
            evaluationForm.revalidate();
            evaluationForm.repaint();
            return;
        }

        Alternative alternative = alternativeList.get(selectedRow);
        
        // Add title
        JLabel titleLabel = new JLabel("Evaluasi: " + alternative.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        evaluationForm.add(titleLabel);
        evaluationForm.add(Box.createVerticalStrut(10));

        // Add criteria evaluation fields
        Map<String, JTextField> valueFields = new HashMap<>();
        
        for (Criteria criteria : criteriaList) {
            JPanel criteriaPanel = new JPanel();
            criteriaPanel.setLayout(new BoxLayout(criteriaPanel, BoxLayout.Y_AXIS));
            criteriaPanel.setBackground(Color.decode("#f5f2e8"));
            criteriaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel nameLabel = new JLabel(criteria.getName());
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            JTextField valueField = new JTextField(10);
            valueField.setText(String.valueOf(
                alternative.getCriteriaValue(criteria.getName()) != null ? 
                alternative.getCriteriaValue(criteria.getName()) : 0.0
            ));
            valueFields.put(criteria.getName(), valueField);
            
            criteriaPanel.add(nameLabel);
            criteriaPanel.add(valueField);
            criteriaPanel.add(Box.createVerticalStrut(5));
            
            evaluationForm.add(criteriaPanel);
            evaluationForm.add(Box.createVerticalStrut(10));
        }

        // Add save button
        JButton saveButton = new JButton("Simpan Evaluasi");
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveButton.addActionListener(e -> {
            try {
                // Validate and save values
                for (Criteria criteria : criteriaList) {
                    JTextField field = valueFields.get(criteria.getName());
                    String valueStr = field.getText().trim();
                    
                    if (valueStr.isEmpty()) {
                        JOptionPane.showMessageDialog(this, 
                            "Nilai untuk " + criteria.getName() + " tidak boleh kosong!");
                        return;
                    }
                    
                    try {
                        double value = Double.parseDouble(valueStr);
                        if (value < 0) {
                            JOptionPane.showMessageDialog(this, 
                                "Nilai untuk " + criteria.getName() + " tidak boleh negatif!");
                            return;
                        }
                        alternative.setCriteriaValue(criteria.getName(), value);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, 
                            "Nilai untuk " + criteria.getName() + " harus berupa angka!");
                        return;
                    }
                }
                
                refreshTable();
                JOptionPane.showMessageDialog(this, "Evaluasi berhasil disimpan!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage());
            }
        });

        evaluationForm.add(saveButton);
        
        evaluationForm.revalidate();
        evaluationForm.repaint();
    }

    private void refreshTable() {
        if (alternativeList == null || criteriaList == null) {
            return;
        }

        List<List<Object>> data = new ArrayList<>();
        int counter = 1;
        String filterType = (String) filterCombo.getSelectedItem();

        for (Alternative alternative : alternativeList) {
            boolean includeRow = true;
            
            if (!filterType.equals("Semua Kriteria")) {
                // Check if alternative has any criteria of the selected type
                boolean hasMatchingCriteria = false;
                for (Criteria criteria : criteriaList) {
                    if (criteria.getType().equalsIgnoreCase(filterType)) {
                        hasMatchingCriteria = true;
                        break;
                    }
                }
                includeRow = hasMatchingCriteria;
            }

            if (includeRow) {
                List<Object> row = new ArrayList<>();
                row.add(counter++);
                row.add(alternative.getName());
                
                for (Criteria criteria : criteriaList) {
                    Double value = alternative.getCriteriaValue(criteria.getName());
                    row.add(value != null ? value : 0.0);
                }
                
                // Add status (complete/incomplete)
                boolean isComplete = true;
                for (Criteria criteria : criteriaList) {
                    if (alternative.getCriteriaValue(criteria.getName()) == null) {
                        isComplete = false;
                        break;
                    }
                }
                row.add(isComplete ? "✓ Lengkap" : "⚠ Belum Lengkap");
                
                data.add(row);
            }
        }

        tableModel.setData(data);
    }

    public void handleAdd() {
        // Not needed for evaluation panel
    }

    public void handleEdit() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Wedding Organizer yang akan dievaluasi!");
            return;
        }
        showEvaluationForm(selectedRow);
    }

    public void handleDelete() {
        // Not needed for evaluation panel
    }

    public void handleSave() {
        // Save all evaluations
        JOptionPane.showMessageDialog(this, "Semua evaluasi berhasil disimpan!");
    }

    public void setData(List<Alternative> alternatives, List<Criteria> criteria) {
        this.alternativeList = new ArrayList<>(alternatives);
        this.criteriaList = new ArrayList<>(criteria);
        
        // Reinitialize components with new data
        initializeComponents();
        setupLayout();
        
        // Refresh the table and evaluation form
        refreshTable();
        
        // If there are alternatives, show the first one in the evaluation form
        if (!alternativeList.isEmpty()) {
            showEvaluationForm(0);
        }
    }
}
