package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

public class PDFExporterTest {
    private List<Alternative> alternatives;
    private List<Criteria> criteria;
    private String testFilePath;

    @Before
    public void setUp() {
        // Get sample data
        alternatives = DummyDataGenerator.generateSampleAlternatives();
        criteria = DummyDataGenerator.generateSampleCriteria();
        
        // Set up test file path
        testFilePath = "test_export.pdf";
    }

    @After
    public void tearDown() {
        // Clean up test file after each test
        File testFile = new File(testFilePath);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    public void testExportResults() {
        try {
            // Export to PDF
            PDFExporter.exportResults(testFilePath, alternatives, criteria);
            
            // Verify file was created
            File exportedFile = new File(testFilePath);
            assertTrue("PDF file should be created", exportedFile.exists());
            assertTrue("PDF file should not be empty", exportedFile.length() > 0);
            
        } catch (Exception e) {
            fail("PDF export should not throw exception: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportWithNullAlternatives() throws Exception {
        PDFExporter.exportResults(testFilePath, null, criteria);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportWithNullCriteria() throws Exception {
        PDFExporter.exportResults(testFilePath, alternatives, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportWithNullPath() throws Exception {
        PDFExporter.exportResults(null, alternatives, criteria);
    }

    @Test
    public void testExportWithEmptyLists() {
        try {
            PDFExporter.exportResults(testFilePath, 
                                    new java.util.ArrayList<>(), 
                                    new java.util.ArrayList<>());
            fail("Should throw exception for empty lists");
        } catch (Exception e) {
            assertTrue("Should throw appropriate exception for empty lists",
                e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testExportToInvalidLocation() {
        String invalidPath = "/invalid/location/test.pdf";
        try {
            PDFExporter.exportResults(invalidPath, alternatives, criteria);
            fail("Should throw exception for invalid file path");
        } catch (Exception e) {
            assertTrue("Should throw appropriate exception for invalid path",
                e instanceof Exception);
        }
    }

    @Test
    public void testExportWithSpecialCharacters() {
        // Add alternative with special characters
        Alternative specialAlt = new Alternative("Test & Special © Characters");
        specialAlt.setCriteriaValue("Harga Paket", 50000000.0);
        specialAlt.setCriteriaValue("Jumlah Vendor", 5.0);
        specialAlt.setCriteriaValue("Pengalaman (Tahun)", 3.0);
        specialAlt.setCriteriaValue("Rating Pelanggan", 4.0);
        specialAlt.setCriteriaValue("Jarak Lokasi (km)", 10.0);
        alternatives.add(specialAlt);

        try {
            PDFExporter.exportResults(testFilePath, alternatives, criteria);
            File exportedFile = new File(testFilePath);
            assertTrue("PDF should be created with special characters", 
                exportedFile.exists());
            assertTrue("PDF should not be empty", 
                exportedFile.length() > 0);
        } catch (Exception e) {
            fail("Should handle special characters: " + e.getMessage());
        }
    }

    @Test
    public void testExportWithLargeDataset() {
        // Create a large dataset
        for (int i = 0; i < 100; i++) {
            Alternative alt = new Alternative("Test WO " + i);
            alt.setCriteriaValue("Harga Paket", 50000000.0);
            alt.setCriteriaValue("Jumlah Vendor", 5.0);
            alt.setCriteriaValue("Pengalaman (Tahun)", 3.0);
            alt.setCriteriaValue("Rating Pelanggan", 4.0);
            alt.setCriteriaValue("Jarak Lokasi (km)", 10.0);
            alternatives.add(alt);
        }

        try {
            PDFExporter.exportResults(testFilePath, alternatives, criteria);
            File exportedFile = new File(testFilePath);
            assertTrue("PDF should be created with large dataset", 
                exportedFile.exists());
            assertTrue("PDF should not be empty", 
                exportedFile.length() > 0);
        } catch (Exception e) {
            fail("Should handle large datasets: " + e.getMessage());
        }
    }

    @Test
    public void testExportWithZeroValues() {
        // Add alternative with zero values
        Alternative zeroAlt = new Alternative("Zero Values WO");
        zeroAlt.setCriteriaValue("Harga Paket", 0.0);
        zeroAlt.setCriteriaValue("Jumlah Vendor", 0.0);
        zeroAlt.setCriteriaValue("Pengalaman (Tahun)", 0.0);
        zeroAlt.setCriteriaValue("Rating Pelanggan", 0.0);
        zeroAlt.setCriteriaValue("Jarak Lokasi (km)", 0.0);
        alternatives.add(zeroAlt);

        try {
            PDFExporter.exportResults(testFilePath, alternatives, criteria);
            File exportedFile = new File(testFilePath);
            assertTrue("PDF should be created with zero values", 
                exportedFile.exists());
            assertTrue("PDF should not be empty", 
                exportedFile.length() > 0);
        } catch (Exception e) {
            fail("Should handle zero values: " + e.getMessage());
        }
    }
}
