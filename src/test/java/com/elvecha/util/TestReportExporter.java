package com.elvecha.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.JSONObject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

/**
 * Exports test reports in various formats (HTML, PDF, JSON, XML)
 */
public class TestReportExporter {
    private final TestLogger logger;
    private final String outputDir;
    
    public TestReportExporter(String outputDir) {
        this.outputDir = outputDir;
        this.logger = new TestLogger();
        createOutputDirectory();
    }
    
    private void createOutputDirectory() {
        File dir = new File(outputDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create output directory: " + outputDir);
        }
    }
    
    /**
     * Export format enumeration
     */
    public enum Format {
        HTML, PDF, JSON, XML
    }
    
    /**
     * Exports test results in the specified format
     */
    public void export(TestReportGenerator.TestResults results, Format format) {
        try {
            switch (format) {
                case HTML:
                    exportHtml(results);
                    break;
                case PDF:
                    exportPdf(results);
                    break;
                case JSON:
                    exportJson(results);
                    break;
                case XML:
                    exportXml(results);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
        } catch (Exception e) {
            logger.logError("Failed to export report", e);
            throw new RuntimeException("Export failed", e);
        }
    }
    
    /**
     * Exports test results in HTML format
     */
    private void exportHtml(TestReportGenerator.TestResults results) throws IOException {
        TestReportGenerator generator = new TestReportGenerator(outputDir);
        generator.generateReport(results);
    }
    
    /**
     * Exports test results in PDF format
     */
    private void exportPdf(TestReportGenerator.TestResults results) throws IOException, DocumentException {
        String filename = String.format("test-report-%d.pdf", results.getTimestamp());
        File outputFile = new File(outputDir, filename);
        
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();
        
        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
        Paragraph title = new Paragraph("Test Execution Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
        
        // Add summary
        addPdfSection(document, "Summary", createSummaryContent(results));
        
        // Add categories
        addPdfSection(document, "Categories", createCategoriesContent(results));
        
        // Add test details
        addPdfSection(document, "Test Details", createTestDetailsContent(results));
        
        // Add environment info
        addPdfSection(document, "Environment", createEnvironmentContent(results));
        
        document.close();
        logger.log("PDF report generated: " + outputFile.getAbsolutePath());
    }
    
    private void addPdfSection(Document document, String title, List<String> content) 
            throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        document.add(new Paragraph(title, sectionFont));
        document.add(Chunk.NEWLINE);
        
        for (String line : content) {
            document.add(new Paragraph(line));
        }
        document.add(Chunk.NEWLINE);
    }
    
    private List<String> createSummaryContent(TestReportGenerator.TestResults results) {
        List<String> content = new ArrayList<>();
        content.add(String.format("Total Tests: %d", results.getTotalTests()));
        content.add(String.format("Passed: %d", results.getPassedTests()));
        content.add(String.format("Failed: %d", results.getFailedTests()));
        content.add(String.format("Skipped: %d", results.getSkippedTests()));
        return content;
    }
    
    private List<String> createCategoriesContent(TestReportGenerator.TestResults results) {
        List<String> content = new ArrayList<>();
        for (TestReportGenerator.TestCategory category : results.getCategories()) {
            content.add(String.format("Category: %s", category.getName()));
            content.add(String.format("  Total: %d", category.getTotalTests()));
            content.add(String.format("  Passed: %d", category.getPassedTests()));
            content.add(String.format("  Failed: %d", category.getFailedTests()));
            content.add(String.format("  Duration: %.2fs", category.getDuration() / 1000.0));
            content.add("");
        }
        return content;
    }
    
    private List<String> createTestDetailsContent(TestReportGenerator.TestResults results) {
        List<String> content = new ArrayList<>();
        for (TestReportGenerator.TestResult test : results.getTestResults()) {
            content.add(String.format("Test: %s", test.getTestName()));
            content.add(String.format("  Status: %s", 
                test.isPassed() ? "Passed" : (test.isSkipped() ? "Skipped" : "Failed")));
            content.add(String.format("  Duration: %.2fs", test.getDuration() / 1000.0));
            if (!test.isPassed() && !test.isSkipped()) {
                content.add(String.format("  Error: %s", test.getErrorMessage()));
            }
            content.add("");
        }
        return content;
    }
    
    private List<String> createEnvironmentContent(TestReportGenerator.TestResults results) {
        List<String> content = new ArrayList<>();
        for (Map.Entry<String, String> entry : results.getEnvironmentInfo().entrySet()) {
            content.add(String.format("%s: %s", entry.getKey(), entry.getValue()));
        }
        return content;
    }
    
    /**
     * Exports test results in JSON format
     */
    private void exportJson(TestReportGenerator.TestResults results) throws IOException {
        String filename = String.format("test-report-%d.json", results.getTimestamp());
        File outputFile = new File(outputDir, filename);
        
        JSONObject json = new JSONObject();
        json.put("timestamp", results.getTimestamp());
        json.put("summary", createJsonSummary(results));
        json.put("categories", createJsonCategories(results));
        json.put("tests", createJsonTests(results));
        json.put("environment", results.getEnvironmentInfo());
        
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writer.write(json.toString(2));
        }
        
        logger.log("JSON report generated: " + outputFile.getAbsolutePath());
    }
    
    private JSONObject createJsonSummary(TestReportGenerator.TestResults results) {
        JSONObject summary = new JSONObject();
        summary.put("total", results.getTotalTests());
        summary.put("passed", results.getPassedTests());
        summary.put("failed", results.getFailedTests());
        summary.put("skipped", results.getSkippedTests());
        return summary;
    }
    
    private List<JSONObject> createJsonCategories(TestReportGenerator.TestResults results) {
        List<JSONObject> categories = new ArrayList<>();
        for (TestReportGenerator.TestCategory category : results.getCategories()) {
            JSONObject cat = new JSONObject();
            cat.put("name", category.getName());
            cat.put("total", category.getTotalTests());
            cat.put("passed", category.getPassedTests());
            cat.put("failed", category.getFailedTests());
            cat.put("duration", category.getDuration());
            categories.add(cat);
        }
        return categories;
    }
    
    private List<JSONObject> createJsonTests(TestReportGenerator.TestResults results) {
        List<JSONObject> tests = new ArrayList<>();
        for (TestReportGenerator.TestResult test : results.getTestResults()) {
            JSONObject t = new JSONObject();
            t.put("name", test.getTestName());
            t.put("passed", test.isPassed());
            t.put("skipped", test.isSkipped());
            t.put("duration", test.getDuration());
            if (!test.isPassed() && !test.isSkipped()) {
                t.put("error", test.getErrorMessage());
            }
            tests.add(t);
        }
        return tests;
    }
    
    /**
     * Exports test results in XML format
     */
    private void exportXml(TestReportGenerator.TestResults results) throws IOException {
        String filename = String.format("test-report-%d.xml", results.getTimestamp());
        File outputFile = new File(outputDir, filename);
        
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<testReport>\n");
            
            // Write timestamp
            writer.write(String.format("  <timestamp>%d</timestamp>\n", 
                results.getTimestamp()));
            
            // Write summary
            writeSummaryXml(writer, results);
            
            // Write categories
            writeCategoriesXml(writer, results);
            
            // Write tests
            writeTestsXml(writer, results);
            
            // Write environment
            writeEnvironmentXml(writer, results);
            
            writer.write("</testReport>");
        }
        
        logger.log("XML report generated: " + outputFile.getAbsolutePath());
    }
    
    private void writeSummaryXml(Writer writer, TestReportGenerator.TestResults results) 
            throws IOException {
        writer.write("  <summary>\n");
        writer.write(String.format("    <total>%d</total>\n", results.getTotalTests()));
        writer.write(String.format("    <passed>%d</passed>\n", results.getPassedTests()));
        writer.write(String.format("    <failed>%d</failed>\n", results.getFailedTests()));
        writer.write(String.format("    <skipped>%d</skipped>\n", results.getSkippedTests()));
        writer.write("  </summary>\n");
    }
    
    private void writeCategoriesXml(Writer writer, TestReportGenerator.TestResults results) 
            throws IOException {
        writer.write("  <categories>\n");
        for (TestReportGenerator.TestCategory category : results.getCategories()) {
            writer.write("    <category>\n");
            writer.write(String.format("      <name>%s</name>\n", escapeXml(category.getName())));
            writer.write(String.format("      <total>%d</total>\n", category.getTotalTests()));
            writer.write(String.format("      <passed>%d</passed>\n", category.getPassedTests()));
            writer.write(String.format("      <failed>%d</failed>\n", category.getFailedTests()));
            writer.write(String.format("      <duration>%d</duration>\n", category.getDuration()));
            writer.write("    </category>\n");
        }
        writer.write("  </categories>\n");
    }
    
    private void writeTestsXml(Writer writer, TestReportGenerator.TestResults results) 
            throws IOException {
        writer.write("  <tests>\n");
        for (TestReportGenerator.TestResult test : results.getTestResults()) {
            writer.write("    <test>\n");
            writer.write(String.format("      <name>%s</name>\n", escapeXml(test.getTestName())));
            writer.write(String.format("      <passed>%b</passed>\n", test.isPassed()));
            writer.write(String.format("      <skipped>%b</skipped>\n", test.isSkipped()));
            writer.write(String.format("      <duration>%d</duration>\n", test.getDuration()));
            if (!test.isPassed() && !test.isSkipped()) {
                writer.write(String.format("      <error>%s</error>\n", 
                    escapeXml(test.getErrorMessage())));
            }
            writer.write("    </test>\n");
        }
        writer.write("  </tests>\n");
    }
    
    private void writeEnvironmentXml(Writer writer, TestReportGenerator.TestResults results) 
            throws IOException {
        writer.write("  <environment>\n");
        for (Map.Entry<String, String> entry : results.getEnvironmentInfo().entrySet()) {
            writer.write(String.format("    <%s>%s</%s>\n",
                escapeXml(entry.getKey()),
                escapeXml(entry.getValue()),
                escapeXml(entry.getKey())));
        }
        writer.write("  </environment>\n");
    }
    
    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "<")
                   .replace(">", ">")
                   .replace("\"", """)
                   .replace("'", "&apos;");
    }
}
