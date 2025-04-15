package com.elvecha.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.JSONObject;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import com.itextpdf.text.pdf.*;

public class TestReportExporterTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private TestReportExporter exporter;
    private TestReportGenerator.TestResults testResults;
    private String outputDir;
    
    @Before
    public void setUp() throws IOException {
        outputDir = tempFolder.newFolder("test-reports").getAbsolutePath();
        exporter = new TestReportExporter(outputDir);
        testResults = createTestResults();
    }
    
    private TestReportGenerator.TestResults createTestResults() {
        return TestReportBuilder.create()
            .withTimestamp(1000L)
            .addCategory("model", 10, 8, 2, 1500)
            .addCategory("util", 15, 15, 0, 2000)
            .addPassedTest("Test1", 1000)
            .addFailedTest("Test2", "Expected true", 2000)
            .addSkippedTest("Test3")
            .withStandardEnvironmentInfo()
            .build();
    }
    
    @Test
    public void testHtmlExport() throws IOException {
        exporter.export(testResults, TestReportExporter.Format.HTML);
        
        // Verify HTML file exists
        File[] files = new File(outputDir).listFiles((dir, name) -> 
            name.endsWith(".html"));
        assertNotNull("Should have HTML files", files);
        assertEquals("Should have one HTML file", 1, files.length);
        
        // Verify content
        String content = Files.readString(files[0].toPath());
        assertTrue("Should contain test name", content.contains("Test1"));
        assertTrue("Should contain category", content.contains("model"));
        assertTrue("Should contain error message", content.contains("Expected true"));
    }
    
    @Test
    public void testPdfExport() throws IOException {
        exporter.export(testResults, TestReportExporter.Format.PDF);
        
        // Verify PDF file exists
        File[] files = new File(outputDir).listFiles((dir, name) -> 
            name.endsWith(".pdf"));
        assertNotNull("Should have PDF files", files);
        assertEquals("Should have one PDF file", 1, files.length);
        
        // Verify PDF is valid
        try (PdfReader reader = new PdfReader(files[0].getAbsolutePath())) {
            assertNotNull("Should have valid PDF", reader);
            assertTrue("Should have pages", reader.getNumberOfPages() > 0);
            
            // Extract text to verify content
            String content = extractPdfText(reader);
            assertTrue("Should contain test name", content.contains("Test1"));
            assertTrue("Should contain category", content.contains("model"));
            assertTrue("Should contain error message", content.contains("Expected true"));
        }
    }
    
    private String extractPdfText(PdfReader reader) throws IOException {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            text.append(PdfTextExtractor.getTextFromPage(reader, i));
        }
        return text.toString();
    }
    
    @Test
    public void testJsonExport() throws IOException {
        exporter.export(testResults, TestReportExporter.Format.JSON);
        
        // Verify JSON file exists
        File[] files = new File(outputDir).listFiles((dir, name) -> 
            name.endsWith(".json"));
        assertNotNull("Should have JSON files", files);
        assertEquals("Should have one JSON file", 1, files.length);
        
        // Verify JSON content
        String content = Files.readString(files[0].toPath());
        JSONObject json = new JSONObject(content);
        
        assertEquals("Should have correct timestamp", 1000L, json.getLong("timestamp"));
        
        JSONObject summary = json.getJSONObject("summary");
        assertEquals("Should have correct total", 3, summary.getInt("total"));
        assertEquals("Should have correct passed", 1, summary.getInt("passed"));
        assertEquals("Should have correct failed", 1, summary.getInt("failed"));
        assertEquals("Should have correct skipped", 1, summary.getInt("skipped"));
    }
    
    @Test
    public void testXmlExport() throws Exception {
        exporter.export(testResults, TestReportExporter.Format.XML);
        
        // Verify XML file exists
        File[] files = new File(outputDir).listFiles((dir, name) -> 
            name.endsWith(".xml"));
        assertNotNull("Should have XML files", files);
        assertEquals("Should have one XML file", 1, files.length);
        
        // Parse and verify XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(files[0]);
        
        // Verify basic structure
        NodeList tests = doc.getElementsByTagName("test");
        assertEquals("Should have three tests", 3, tests.getLength());
        
        NodeList categories = doc.getElementsByTagName("category");
        assertEquals("Should have two categories", 2, categories.getLength());
        
        // Verify XML is well-formed
        assertTrue("Should be valid XML", isValidXml(files[0]));
    }
    
    private boolean isValidXml(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Test
    public void testMultipleExports() throws IOException {
        // Export in all formats
        for (TestReportExporter.Format format : TestReportExporter.Format.values()) {
            exporter.export(testResults, format);
        }
        
        // Verify all files exist
        File[] files = new File(outputDir).listFiles();
        assertNotNull("Should have files", files);
        assertEquals("Should have one file per format", 
            TestReportExporter.Format.values().length, files.length);
    }
    
    @Test(expected = RuntimeException.class)
    public void testInvalidOutputDirectory() {
        new TestReportExporter("/invalid/directory/path");
    }
    
    @Test
    public void testLargeReport() throws IOException {
        // Create large test results
        TestReportGenerator.TestResults largeResults = TestReportBuilder.create()
            .withRandomTestData(1000, 50)
            .withStandardEnvironmentInfo()
            .build();
            
        // Export in all formats
        long startTime = System.currentTimeMillis();
        for (TestReportExporter.Format format : TestReportExporter.Format.values()) {
            exporter.export(largeResults, format);
        }
        long duration = System.currentTimeMillis() - startTime;
        
        // Verify performance
        assertTrue("Export should complete in reasonable time", duration < 10000);
    }
    
    @Test
    public void testSpecialCharacters() throws IOException {
        // Create test results with special characters
        TestReportGenerator.TestResults specialResults = TestReportBuilder.create()
            .addCategory("test<category>", 1, 0, 1, 1000)
            .addFailedTest("test&name", "error'message\"here", 1000)
            .build();
            
        // Export in all formats
        for (TestReportExporter.Format format : TestReportExporter.Format.values()) {
            exporter.export(specialResults, format);
        }
        
        // Verify files exist and are valid
        for (File file : new File(outputDir).listFiles()) {
            assertTrue("File should not be empty", file.length() > 0);
            
            if (file.getName().endsWith(".xml")) {
                assertTrue("XML should be valid", isValidXml(file));
            }
        }
    }
    
    @Test
    public void testEmptyResults() throws IOException {
        // Create empty test results
        TestReportGenerator.TestResults emptyResults = TestReportBuilder.create()
            .build();
            
        // Export in all formats
        for (TestReportExporter.Format format : TestReportExporter.Format.values()) {
            exporter.export(emptyResults, format);
        }
        
        // Verify files exist
        File[] files = new File(outputDir).listFiles();
        assertEquals("Should have one file per format",
            TestReportExporter.Format.values().length, files.length);
    }
}
