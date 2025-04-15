package com.elvecha.util;

import org.junit.Test;
import org.junit.Rule;
import org.junit.Before;
import org.junit.After;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TestReportRuleTest {
    private static final String TEST_REPORT_DIR = "test-reports";
    
    @Rule
    public TestReportRule reportRule = new TestReportRule();
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setUp() {
        // Set test-specific properties
        TestReportRule.setProperty("report.output.dir", TEST_REPORT_DIR);
        TestReportRule.setProperty("test.log.memory", "true");
        
        // Create test directory
        new File(TEST_REPORT_DIR).mkdirs();
    }
    
    @After
    public void tearDown() {
        // Clean up test files
        TestUtils.cleanupTestFiles(TEST_REPORT_DIR);
    }
    
    @Test
    public void testSuccessfulTest() throws Exception {
        // This test should pass and be recorded
        assertTrue("Simple assertion", true);
        
        // Force report generation
        TestReportRule.generateReport();
        
        // Verify report
        File[] reportFiles = new File(TEST_REPORT_DIR)
            .listFiles((dir, name) -> name.endsWith(".html"));
        String content = Files.readString(reportFiles[0].toPath());
        
        assertTrue("Report should contain test name",
            content.contains("testSuccessfulTest"));
        assertTrue("Report should mark test as passed",
            content.contains("PASSED"));
    }
    
    @Test
    public void testFailedTest() {
        try {
            // This test should fail
            thrown.expect(AssertionError.class);
            fail("Intentional failure");
        } finally {
            // Force report generation
            TestReportRule.generateReport();
            
            try {
                File[] reportFiles = new File(TEST_REPORT_DIR)
                    .listFiles((dir, name) -> name.endsWith(".html"));
                String content = Files.readString(reportFiles[0].toPath());
                
                assertTrue("Report should contain test name",
                    content.contains("testFailedTest"));
                assertTrue("Report should mark test as failed",
                    content.contains("FAILED"));
                assertTrue("Report should contain failure details",
                    content.contains("Intentional failure"));
            } catch (Exception e) {
                fail("Failed to verify report: " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testCustomSection() throws Exception {
        // Add a custom section
        TestReportRule.addCustomSection(
            "Custom Category",
            "Test Metrics",
            "Custom test information"
        );
        
        // Force report generation
        TestReportRule.generateReport();
        
        // Verify custom section
        File[] reportFiles = new File(TEST_REPORT_DIR)
            .listFiles((dir, name) -> name.endsWith(".html"));
        String content = Files.readString(reportFiles[0].toPath());
        
        assertTrue("Report should contain custom category",
            content.contains("Custom Category"));
        assertTrue("Report should contain custom title",
            content.contains("Test Metrics"));
    }
    
    @Test
    public void testCustomResult() throws Exception {
        // Record a custom result
        TestReportRule.recordCustomResult(
            "Custom Tests",
            "Custom Test",
            true,
            1000000000, // 1 second
            null
        );
        
        // Force report generation
        TestReportRule.generateReport();
        
        // Verify custom result
        File[] reportFiles = new File(TEST_REPORT_DIR)
            .listFiles((dir, name) -> name.endsWith(".html"));
        String content = Files.readString(reportFiles[0].toPath());
        
        assertTrue("Report should contain custom category",
            content.contains("Custom Tests"));
        assertTrue("Report should contain custom test name",
            content.contains("Custom Test"));
        assertTrue("Report should contain duration",
            content.contains("1.00"));
    }
    
    @Test
    public void testMemoryLogging() throws Exception {
        // Perform some memory-intensive operation
        byte[] data = new byte[1024 * 1024]; // 1MB
        
        // Force report generation
        TestReportRule.generateReport();
        
        // Verify memory logging
        List<String> logLines = Files.readAllLines(
            new File(TEST_REPORT_DIR, "test.log").toPath());
        
        boolean foundMemoryLog = logLines.stream()
            .anyMatch(line -> line.contains("Memory Usage") &&
                            line.contains("testMemoryLogging"));
        
        assertTrue("Should log memory usage", foundMemoryLog);
    }
    
    @Test
    public void testConfigurationProperties() {
        // Test getting properties
        assertEquals("Should get default value",
            "default",
            TestReportRule.getProperty("non.existent", "default"));
            
        // Test setting properties
        TestReportRule.setProperty("test.custom", "value");
        assertEquals("Should get set value",
            "value",
            TestReportRule.getProperty("test.custom", "default"));
    }
    
    @Test
    public void testReportGenerator() {
        // Test getting generator instance
        assertNotNull("Should get generator instance",
            TestReportRule.getGenerator());
    }
    
    @Test
    public void testLogger() {
        // Test getting logger instance
        assertNotNull("Should get logger instance",
            TestReportRule.getLogger());
    }
    
    @Test
    public void testConcurrentTests() throws Exception {
        // Simulate concurrent test execution
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                TestReportRule.recordCustomResult(
                    "Concurrent Tests",
                    "Concurrent Test " + index,
                    true,
                    100000000, // 100ms
                    null
                );
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Force report generation
        TestReportRule.generateReport();
        
        // Verify all tests were recorded
        File[] reportFiles = new File(TEST_REPORT_DIR)
            .listFiles((dir, name) -> name.endsWith(".html"));
        String content = Files.readString(reportFiles[0].toPath());
        
        for (int i = 0; i < threads.length; i++) {
            assertTrue("Report should contain concurrent test " + i,
                content.contains("Concurrent Test " + i));
        }
    }
}
