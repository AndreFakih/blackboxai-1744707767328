package com.elvecha.ui;

import com.elvecha.ui.panels.*;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private CriteriaPanel criteriaPanel;
    private AlternativePanel alternativePanel;
    private EvaluationPanel evaluationPanel;
    private ResultPanel resultPanel;
    private JToolBar toolBar;

    public MainFrame() {
        initializeFrame();
        initializeComponents();
        setupLayout();
        this.setVisible(true);
    }

    private void initializeFrame() {
        setTitle("El Vecha Wedding Organizer DSS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        // Set the background color to pastel theme
        getContentPane().setBackground(Color.decode("#f5f2e8"));
    }

    private void initializeComponents() {
        // Initialize toolbar
        toolBar = createToolBar();
        
        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 12));
        tabbedPane.setBackground(Color.decode("#f5f2e8"));
        
        // Initialize panels
        criteriaPanel = new CriteriaPanel();
        alternativePanel = new AlternativePanel();
        evaluationPanel = new EvaluationPanel();
        resultPanel = new ResultPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Kriteria", createTabIcon("criteria"), criteriaPanel);
        tabbedPane.addTab("Alternatif", createTabIcon("alternative"), alternativePanel);
        tabbedPane.addTab("Penilaian", createTabIcon("evaluation"), evaluationPanel);
        tabbedPane.addTab("Hasil", createTabIcon("result"), resultPanel);
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(Color.WHITE);
        
        // Add button
        JButton addButton = createToolBarButton("Tambah", "add");
        addButton.addActionListener(e -> handleAdd());
        toolbar.add(addButton);
        
        // Edit button
        JButton editButton = createToolBarButton("Edit", "edit");
        editButton.addActionListener(e -> handleEdit());
        toolbar.add(editButton);
        
        // Delete button
        JButton deleteButton = createToolBarButton("Hapus", "delete");
        deleteButton.addActionListener(e -> handleDelete());
        toolbar.add(deleteButton);
        
        // Add separator
        toolbar.addSeparator();
        
        // Save button
        JButton saveButton = createToolBarButton("Simpan", "save");
        saveButton.addActionListener(e -> handleSave());
        toolbar.add(saveButton);
        
        return toolbar;
    }

    private JButton createToolBarButton(String text, String iconName) {
        JButton button = new JButton(text);
        try {
            // Load icon from Font Awesome or similar
            ImageIcon icon = new ImageIcon(new URL("https://raw.githubusercontent.com/FortAwesome/Font-Awesome/master/svgs/solid/" + iconName + ".svg"));
            button.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconName);
        }
        button.setFocusPainted(false);
        return button;
    }

    private ImageIcon createTabIcon(String iconName) {
        try {
            // Load icon from Font Awesome or similar
            return new ImageIcon(new URL("https://raw.githubusercontent.com/FortAwesome/Font-Awesome/master/svgs/solid/" + iconName + ".svg"));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconName);
            return null;
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add some padding
        ((JComponent) getContentPane()).setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }

    private void handleAdd() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 0: // Kriteria
                criteriaPanel.handleAdd();
                break;
            case 1: // Alternatif
                alternativePanel.handleAdd();
                break;
            case 2: // Penilaian
                evaluationPanel.handleAdd();
                break;
        }
    }

    private void handleEdit() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 0: // Kriteria
                criteriaPanel.handleEdit();
                break;
            case 1: // Alternatif
                alternativePanel.handleEdit();
                break;
            case 2: // Penilaian
                evaluationPanel.handleEdit();
                break;
        }
    }

    private void handleDelete() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 0: // Kriteria
                criteriaPanel.handleDelete();
                break;
            case 1: // Alternatif
                alternativePanel.handleDelete();
                break;
            case 2: // Penilaian
                evaluationPanel.handleDelete();
                break;
        }
    }

    private void handleSave() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 0: // Kriteria
                criteriaPanel.handleSave();
                break;
            case 1: // Alternatif
                alternativePanel.handleSave();
                break;
            case 2: // Penilaian
                evaluationPanel.handleSave();
                break;
            case 3: // Hasil
                resultPanel.handleSave();
                break;
        }
    }

    // Getter methods for panels
    public CriteriaPanel getCriteriaPanel() {
        return criteriaPanel;
    }
    
    public AlternativePanel getAlternativePanel() {
        return alternativePanel;
    }
    
    public EvaluationPanel getEvaluationPanel() {
        return evaluationPanel;
    }
    
    public ResultPanel getResultPanel() {
        return resultPanel;
    }
}
