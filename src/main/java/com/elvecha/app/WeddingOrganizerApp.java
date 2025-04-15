package com.elvecha.app;

import com.elvecha.ui.MainFrame;
import javax.swing.*;
import java.awt.*;

public class WeddingOrganizerApp {
    
    public static void main(String[] args) {
        // Set the look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Customize UI defaults
            UIManager.put("Panel.background", Color.decode("#f5f2e8"));
            UIManager.put("OptionPane.background", Color.decode("#f5f2e8"));
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.font", new Font("Arial", Font.PLAIN, 12));
            UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 12));
            UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 12));
            UIManager.put("Table.font", new Font("Arial", Font.PLAIN, 12));
            UIManager.put("TableHeader.font", new Font("Arial", Font.BOLD, 12));
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and show the application window
        SwingUtilities.invokeLater(() -> {
            try {
                // Create splash screen
                JWindow splash = createSplashScreen();
                splash.setVisible(true);

                // Simulate loading time
                Thread.sleep(2000);

                // Create and show main frame
                MainFrame mainFrame = new MainFrame();
                
                // Ask to load sample data
                int option = JOptionPane.showConfirmDialog(mainFrame,
                    "Apakah Anda ingin memuat data contoh untuk demonstrasi?",
                    "Muat Data Contoh",
                    JOptionPane.YES_NO_OPTION);
                    
                if (option == JOptionPane.YES_OPTION) {
                    try {
                        // Generate sample data
                        List<Criteria> criteriaList = DummyDataGenerator.generateSampleCriteria();
                        List<Alternative> alternativeList = DummyDataGenerator.generateSampleAlternatives();
                        
                        // Update all panels with sample data
                        mainFrame.getCriteriaPanel().setCriteriaList(criteriaList);
                        mainFrame.getAlternativePanel().setCriteriaList(criteriaList);
                        mainFrame.getAlternativePanel().setAlternativeList(alternativeList);
                        mainFrame.getEvaluationPanel().setData(alternativeList, criteriaList);
                        mainFrame.getResultPanel().setData(alternativeList, criteriaList);
                        
                        JOptionPane.showMessageDialog(mainFrame,
                            "Data contoh berhasil dimuat!",
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Gagal memuat data contoh: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }

                mainFrame.setVisible(true);
                
                // Dispose splash screen
                splash.dispose();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Terjadi kesalahan saat menjalankan aplikasi: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static JWindow createSplashScreen() {
        JWindow splash = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.decode("#f5f2e8"));

        // Add title
        JLabel titleLabel = new JLabel("El Vecha Wedding Organizer DSS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        content.add(titleLabel, BorderLayout.NORTH);

        // Add subtitle
        JLabel subtitleLabel = new JLabel("Sistem Pendukung Keputusan", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        content.add(subtitleLabel, BorderLayout.CENTER);

        // Add loading text
        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        content.add(loadingLabel, BorderLayout.SOUTH);

        // Add border
        content.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        splash.setContentPane(content);
        splash.pack();
        splash.setLocationRelativeTo(null);
        return splash;
    }

    // Helper method to create dummy data for testing
    private static void createDummyData() {
        // TODO: Implement dummy data creation if needed
    }
}
