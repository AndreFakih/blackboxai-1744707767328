package com.elvecha.ui.panels;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import com.elvecha.util.SAWCalculator;
import com.elvecha.util.PDFExporter;
import com.elvecha.ui.models.CustomTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ResultPanel extends JPanel {
    private JTable table;
    private CustomTableModel tableModel;
    private List<Alternative> alternativeList;
    private List<Criteria> criteriaList;
    private SAWCalculator sawCalculator;
    private ChartPanel chartPanel;
    private JPanel detailPanel;
    private DecimalFormat df;

    public ResultPanel() {
        sawCalculator = new SAWCalculator();
        df = new DecimalFormat("#.###");
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        // Initialize table model with columns
        List<String> columns = Arrays.asList(
            "Ranking", 
            "Wedding Organizer", 
            "Nilai Akhir", 
            "Status"
        );
        
        tableModel = new CustomTableModel(columns);
        
        // Initialize table
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        
        // Initialize chart panel
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        
        // Initialize detail panel
        detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBackground(Color.decode("#f5f2e8"));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.decode("#f5f2e8"));

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.decode("#f5f2e8"));
        
        JButton calculateButton = new JButton("Hitung Peringkat");
        calculateButton.addActionListener(e -> calculateRankings());
        
        JButton exportButton = new JButton("Export PDF");
        exportButton.addActionListener(e -> exportToPDF());
        
        topPanel.add(calculateButton);
        topPanel.add(exportButton);
        add(topPanel, BorderLayout.NORTH);

        // Center panel with table and chart
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left side - Table and Details
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.decode("#f5f2e8"));
        
        // Table
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(400, 300));
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Details
        JScrollPane detailScrollPane = new JScrollPane(detailPanel);
        detailScrollPane.setPreferredSize(new Dimension(400, 200));
        leftPanel.add(detailScrollPane, BorderLayout.SOUTH);
        
        // Right side - Chart
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.decode("#f5f2e8"));
        rightPanel.add(chartPanel, BorderLayout.CENTER);
        
        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightPanel);
        mainSplitPane.setDividerLocation(500);
        
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void calculateRankings() {
        if (alternativeList == null || criteriaList == null || 
            alternativeList.isEmpty() || criteriaList.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Data kriteria dan alternatif harus diisi terlebih dahulu!");
            return;
        }

        try {
            // Calculate rankings using SAW
            List<Alternative> rankedAlternatives = 
                sawCalculator.calculate(criteriaList, new ArrayList<>(alternativeList));
            
            // Update table
            List<List<Object>> data = new ArrayList<>();
            int rank = 1;
            
            for (Alternative alt : rankedAlternatives) {
                List<Object> row = new ArrayList<>();
                row.add(rank++);
                row.add(alt.getName());
                row.add(df.format(alt.getFinalScore()));
                row.add(alt.getFinalScore() >= 0.7 ? "Sangat Direkomendasikan" :
                       alt.getFinalScore() >= 0.5 ? "Direkomendasikan" : "Kurang Direkomendasikan");
                data.add(row);
            }
            
            tableModel.setData(data);
            
            // Update chart
            updateChart(rankedAlternatives);
            
            // Update details
            updateDetails(rankedAlternatives);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Terjadi kesalahan dalam perhitungan: " + e.getMessage());
        }
    }

    private void updateChart(List<Alternative> rankedAlternatives) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add data points
        for (Alternative alt : rankedAlternatives) {
            dataset.addValue(alt.getFinalScore(), "Nilai", alt.getName());
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
            "Perbandingan Nilai Wedding Organizer",  // Chart title
            "Wedding Organizer",                     // X-axis label
            "Nilai",                                 // Y-axis label
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Customize chart
        chart.setBackgroundPaint(Color.white);
        
        // Update chart panel
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }

    private void updateDetails(List<Alternative> rankedAlternatives) {
        detailPanel.removeAll();
        
        // Add title
        JLabel titleLabel = new JLabel("Detail Perhitungan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(titleLabel);
        detailPanel.add(Box.createVerticalStrut(10));
        
        // Add calculation details for each alternative
        for (Alternative alt : rankedAlternatives) {
            JPanel altPanel = new JPanel();
            altPanel.setLayout(new BoxLayout(altPanel, BoxLayout.Y_AXIS));
            altPanel.setBackground(Color.decode("#f5f2e8"));
            altPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel nameLabel = new JLabel(alt.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            altPanel.add(nameLabel);
            
            for (Criteria crit : criteriaList) {
                Double value = alt.getCriteriaValue(crit.getName());
                String detail = String.format("%s: %s (Bobot: %.2f)", 
                    crit.getName(), 
                    df.format(value), 
                    crit.getWeight()
                );
                JLabel detailLabel = new JLabel(detail);
                detailLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                altPanel.add(detailLabel);
            }
            
            JLabel scoreLabel = new JLabel(
                "Nilai Akhir: " + df.format(alt.getFinalScore())
            );
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 12));
            altPanel.add(scoreLabel);
            
            detailPanel.add(altPanel);
            detailPanel.add(Box.createVerticalStrut(10));
        }
        
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void exportToPDF() {
        if (alternativeList == null || alternativeList.isEmpty() || 
            criteriaList == null || criteriaList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tidak ada data yang dapat diekspor!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Hasil sebagai PDF");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
            }
            public String getDescription() {
                return "PDF Files (*.pdf)";
            }
        });

        // Set default file name with timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultFileName = "Hasil_SAW_" + sdf.format(new Date()) + ".pdf";
        fileChooser.setSelectedFile(new File(defaultFileName));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Add .pdf extension if not present
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            try {
                // Check if file exists
                if (file.exists()) {
                    int response = JOptionPane.showConfirmDialog(this,
                        "File sudah ada. Apakah Anda ingin menimpanya?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION);
                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // Export to PDF
                PDFExporter.exportResults(file.getAbsolutePath(), alternativeList, criteriaList);
                
                // Show success message with option to open file
                int openFile = JOptionPane.showConfirmDialog(this,
                    "PDF berhasil dibuat!\nApakah Anda ingin membuka file?",
                    "Sukses",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (openFile == JOptionPane.YES_OPTION) {
                    // Open PDF with default system viewer
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Gagal membuat PDF: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void setData(List<Alternative> alternatives, List<Criteria> criteria) {
        this.alternativeList = new ArrayList<>(alternatives);
        this.criteriaList = new ArrayList<>(criteria);
        
        // Reinitialize components with new data
        initializeComponents();
        setupLayout();
        
        // Calculate and display rankings
        SwingUtilities.invokeLater(() -> {
            try {
                calculateRankings();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan saat menghitung peringkat: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void handleSave() {
        // Implement save functionality if needed
        JOptionPane.showMessageDialog(this, "Hasil perhitungan berhasil disimpan!");
    }
}
