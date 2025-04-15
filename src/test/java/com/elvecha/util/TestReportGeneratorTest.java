package com.elvecha.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Tests for TestReportGenerator
 */
public class TestReportGeneratorTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private TestReportGenerator generator;
    private String sessionId;
    
    @Before
    public void setUp() {
        generator = new TestReportGenerator();
        sessionId = "test-" + System.currentTimeMillis();
    }
    
    @Test
    public void testGenerateBasicReport() throws IOException {
        TestReportGenerator.TestResults results = createBasicResults();
        generator.generateReport(results);
        
        File reportFile = new File("test-reports/test-report-" + sessionId + ".html");
        assertTrue("Report file should exist", reportFile.exists());
        
        String content = new String(Files.readAllBytes(reportFile.toPath()));
        assertBasicContent(content, results);
    }
    
    @Test
    public void testGenerateDetailedReport() throws IOException {
        TestReportGenerator.TestResults results = createDetailedResults();
        generator.generateReport(results);
        
        File reportFile = new File("test-reports/test-report-" + sessionId + ".html");
        assertTrue("Report file should exist", reportFile.exists());
        
        String content = new String(Files.readAllBytes(reportFile.toPath()));
        assertDetailedContent(content, results);
    }
    
    @Test
    public void testGenerateReportWithIssues() throws IOException {
        TestReportGenerator.TestResults results = createResultsWithIssues();
        generator.generateReport(results);
        
        File reportFile = new File("test-reports/test-report-" + sessionId + ".html");
        assertTrue("Report file should exist", reportFile.exists());
        
        String content = new String(Files.readAllBytes(reportFile.toPath()));
        assertIssueContent(content, results);
    }
    
    @Test
    public void testGenerateReportWithPerformanceMetrics() throws IOException {
        TestReportGenerator.TestResults results = createResultsWithPerformanceMetrics();
        generator.generateReport(results);
        
        File reportFile = new File("test-reports/test-report-" + sessionId + ".html");
        assertTrue("Report file should exist", reportFile.exists());
        
        String content = new String(Files.readAllBytes(reportFile.toPath()));
        assertPerformanceContent(content, results);
    }
    
    @Test
    public void testGenerateReportWithCategories() throws IOException {
        TestReportGenerator.TestResults results = createResultsWithCategories();
        generator.generateReport(results);
        
        File reportFile = new File("test-reports/test-report-" + sessionId + ".html");
        assertTrue("Report file should exist", reportFile.exists());
        
        String content = new String(Files.readAllBytes(reportFile.toPath()));
        assertCategoryContent(content, results);
    }
    
    @Test
    public void testGenerateReportWithEnvironmentInfo() throws IOException {
        TestReportGenerator.TestResults results = createBasicResults();
        generator.generateReport(results);
        
        File reportFile = new File("test-reports/test-report-" + sessionId + ".html");
        assertTrue("Report file should exist", reportFile.exists());
        
        String content = new String(Files.readAllBytes(reportFile.toPath()));
        assertEnvironmentContent(content);
    }
    
    @Test
    public void testReportFormatting() throws IOException {
        TestReportGenerator.TestResults results = createFormattingTestResults();
        generator.generateReport(results);
        
        File reportFile = new File("test-reports/test-report-" + sessionId + ".html");
        assertTrue("Report file should exist", reportFile.exists());
        
        String content = new String(Files.readAllBytes(reportFile.toPath()));
        assertFormatting(content, results);
    }
    
    private TestReportGenerator.TestResults createBasicResults() {
        List<TestReportGenerator.TestResult> testResults = Arrays.asList(
            new TestReportGenerator.TestResult(
                "testBasic", "TestClass", true, false, null, 100L)
        );
        
        return new TestReportGenerator.TestResults(
            sessionId,
            testResults,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            100L,
            1.0
        );
    }
    
    private TestReportGenerator.TestResults createDetailedResults() {
        List<TestReportGenerator.TestResult> testResults = Arrays.asList(
            new TestReportGenerator.TestResult(
                "testSuccess", "TestClass", true, false, null, 100L),
            new TestReportGenerator.TestResult(
                "testFailure", "TestClass", false, false, "Test failed", 200L),
            new TestReportGenerator.TestResult(
                "testSkipped", "TestClass", false, true, null, 0L)
        );
        
        return new TestReportGenerator.TestResults(
            sessionId,
            testResults,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            300L,
            0.8
        );
    }
    
    private TestReportGenerator.TestResults createResultsWithIssues() {
        List<TestReportGenerator.TestResult> testResults = Arrays.asList(
            new TestReportGenerator.TestResult(
                "testWithIssue", "TestClass", false, false, "Error occurred", 100L)
        );
        
        List<TestReportGenerator.TestIssue> issues = Arrays.asList(
            new TestReportGenerator.TestIssue(
                "ERROR", "Test Failure", "An error occurred during test execution"),
            new TestReportGenerator.TestIssue(
                "WARN", "Performance Issue", "Test execution took longer than expected")
        );
        
        return new TestReportGenerator.TestResults(
            sessionId,
            testResults,
            Collections.emptyList(),
            issues,
            Collections.emptyMap(),
            Collections.emptyMap(),
            100L,
            0.7
        );
    }
    
    private TestReportGenerator.TestResults createResultsWithPerformanceMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("CPU Usage", 75.0);
        metrics.put("Memory Usage", 512.0);
        metrics.put("Response Time", 100.0);
        
        Map<String, Double> thresholds = new HashMap<>();
        thresholds.put("CPU Usage", 90.0);
        thresholds.put("Memory Usage", 1024.0);
        thresholds.put("Response Time", 200.0);
        
        return new TestReportGenerator.TestResults(
            sessionId,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            metrics,
            thresholds,
            100L,
            0.9
        );
    }
    
    private TestReportGenerator.TestResults createResultsWithCategories() {
        List<TestReportGenerator.TestCategory> categories = Arrays.asList(
            new TestReportGenerator.TestCategory("Model", 10, 9, 1, 1000L),
            new TestReportGenerator.TestCategory("Controller", 8, 7, 0, 800L),
            new TestReportGenerator.TestCategory("View", 5, 4, 1, 500L)
        );
        
        return new TestReportGenerator.TestResults(
            sessionId,
            Collections.emptyList(),
            categories,
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            2300L,
            0.85
        );
    }
    
    private TestReportGenerator.TestResults createFormattingTestResults() {
        List<TestReportGenerator.TestResult> testResults = Arrays.asList(
            new TestReportGenerator.TestResult(
                "test<Script>", "Test&Class", true, false, null, 100L),
            new TestReportGenerator.TestResult(
                "test'Quote\"", "Test'Class\"", false, false, "Error & failure", 200L)
        );
        
        return new TestReportGenerator.TestResults(
            sessionId,
            testResults,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            300L,
            0.5
        );
    }
    
    private void assertBasicContent(String content, TestReportGenerator.TestResults results) {
        assertTrue("Should contain session ID", 
            content.contains(results.getSessionId()));
        assertTrue("Should contain test name", 
            content.contains("testBasic"));
        assertTrue("Should contain pass rate", 
            content.contains("100.0%"));
    }
    
    private void assertDetailedContent(String content, TestReportGenerator.TestResults results) {
        assertTrue("Should contain success test", 
            content.contains("testSuccess"));
        assertTrue("Should contain failure test", 
            content.contains("testFailure"));
        assertTrue("Should contain skipped test", 
            content.contains("testSkipped"));
        assertTrue("Should contain error message", 
            content.contains("Test failed"));
    }
    
    private void assertIssueContent(String content, TestReportGenerator.TestResults results) {
        assertTrue("Should contain error issue", 
            content.contains("Test Failure"));
        assertTrue("Should contain warning issue", 
            content.contains("Performance Issue"));
        assertTrue("Should contain issue descriptions", 
            content.contains("An error occurred during test execution"));
    }
    
    private void assertPerformanceContent(String content, TestReportGenerator.TestResults results) {
        assertTrue("Should contain CPU metric", 
            content.contains("CPU Usage"));
        assertTrue("Should contain memory metric", 
            content.contains("Memory Usage"));
        assertTrue("Should contain response time metric", 
            content.contains("Response Time"));
        assertTrue("Should contain metric values", 
            content.contains("75.0") && content.contains("512.0"));
    }
    
    private void assertCategoryContent(String content, TestReportGenerator.TestResults results) {
        assertTrue("Should contain model category", 
            content.contains("Model"));
        assertTrue("Should contain controller category", 
            content.contains("Controller"));
        assertTrue("Should contain view category", 
            content.contains("View"));
        assertTrue("Should contain category metrics", 
            content.contains("90.0%") && content.contains("87.5%"));
    }
    
    private void assertEnvironmentContent(String content) {
        assertTrue("Should contain Java version", 
            content.contains(System.getProperty("java.version")));
        assertTrue("Should contain OS info", 
            content.contains(System.getProperty("os.name")));
        assertTrue("Should contain memory info", 
            content.contains("Memory"));
        assertTrue("Should contain processor info", 
            content.contains("Processors"));
    }
    
    private void assertFormatting(String content, TestReportGenerator.TestResults results) {
        assertFalse("Should escape HTML in test names", 
            content.contains("test<Script>"));
        assertFalse("Should escape quotes in class names", 
            content.contains("Test'Class\""));
        assertTrue("Should contain escaped content", 
            content.contains("test<Script>"));
        assertTrue("Should contain escaped error message", 
            content.contains("Error &amp; failure"));
    }
}
