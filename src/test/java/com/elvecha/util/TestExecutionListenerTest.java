package com.elvecha.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TestExecutionListenerTest {
    private TestExecutionListener listener;
    private static final String TEST_REPORTS_DIR = "test-reports";
    private static final String TEST_CLASS = "TestClass";
    private static final String TEST_METHOD = "testMethod";
    
    @Before
    public void setUp() {
        listener = new TestExecutionListener();
        new File(TEST_REPORTS_DIR).mkdirs();
    }
    
    @After
    public void tearDown() {
        TestUtils.cleanupTestFiles(TEST_REPORTS_DIR);
    }
    
    @Test
    public void testTestRunStarted() throws Exception {
        Description description = Description.createSuiteDescription(TEST_CLASS);
        description.addChild(Description.createTestDescription(
            getClass(), TEST_METHOD));
        
        listener.testRunStarted(description);
        
        // Verify log file contains start message
        File[] logFiles = new File(TEST_REPORTS_DIR)
            .listFiles((dir, name) -> name.endsWith(".log"));
        String content = Files.readString(logFiles[0].toPath());
        
        assertTrue("Log should contain start message",
            content.contains("Starting test execution"));
    }
    
    @Test
    public void testTestStarted() throws Exception {
        Description description = Description.createTestDescription(
            getClass(), TEST_METHOD);
        
        listener.testStarted(description);
        
        // Verify log contains test start message
        File[] logFiles = new File(TEST_REPORTS_DIR)
            .listFiles((dir, name) -> name.endsWith(".log"));
        String content = Files.readString(logFiles[0].toPath());
        
        assertTrue("Log should contain test start message",
            content.contains("Starting test:"));
        assertTrue("Log should contain test name",
            content.contains(TEST_METHOD));
    }
    
    @Test
    public void testTestFinished() throws Exception {
        Description description = Description.createTestDescription(
            getClass(), TEST_METHOD);
        
        listener.testStarted(description);
        listener.testFinished(description);
        
        // Verify log contains completion message
        File[] logFiles = new File(TEST_REPORTS_DIR)
            .listFiles((dir, name) -> name.endsWith(".log"));
        String content = Files.readString(logFiles[0].toPath());
        
        assertTrue("Log should contain completion message",
            content.contains("Test completed"));
        assertTrue("Log should contain progress percentage",
            content.contains("%"));
    }
    
    @Test
    public void testTestFailure() throws Exception {
        Description description = Description.createTestDescription(
            getClass(), TEST_METHOD);
        Exception testException = new RuntimeException("Test failure");
        Failure failure = new Failure(description, testException);
        
        listener.testStarted(description);
        listener.testFailure(failure);
        listener.testFinished(description);
        
        // Verify log contains failure information
        File[] logFiles = new File(TEST_REPORTS_DIR)
            .listFiles((dir, name) -> name.endsWith(".log"));
        String content = Files.readString(logFiles[0].toPath());
        
        assertTrue("Log should contain failure message",
            content.contains("Test failed"));
        assertTrue("Log should contain exception message",
            content.contains("Test failure"));
    }
    
    @Test
    public void testTestRunFinished() throws Exception {
        // Run a series of tests
        Description description = Description.createTestDescription(
            getClass(), TEST_METHOD);
        
        listener.testRunStarted(description);
        
        // Successful test
        listener.testStarted(description);
        listener.testFinished(description);
        
        // Failed test
        Description failedDesc = Description.createTestDescription(
            getClass(), "failedTest");
        listener.testStarted(failedDesc);
        listener.testFailure(new Failure(failedDesc, 
            new RuntimeException("Test failure")));
        listener.testFinished(failedDesc);
        
        // Complete test run
        Result result = new Result();
        listener.testRunFinished(result);
        
        // Verify final summary
        File[] logFiles = new File(TEST_REPORTS_DIR)
            .listFiles((dir, name) -> name.endsWith(".log"));
        String content = Files.readString(logFiles[0].toPath());
        
        assertTrue("Log should contain summary",
            content.contains("Test Execution Summary"));
        assertTrue("Log should contain duration",
            content.contains("Total Duration"));
        assertTrue("Log should contain test counts",
            content.contains("Tests Run:"));
    }
    
    @Test
    public void testConcurrentExecution() throws Exception {
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    Description description = Description.createTestDescription(
                        getClass(), "concurrentTest" + index);
                    listener.testStarted(description);
                    Thread.sleep(100); // Simulate test execution
                    listener.testFinished(description);
                } catch (Exception e) {
                    fail("Concurrent execution failed: " + e.getMessage());
                }
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
        
        // Verify all tests were recorded
        File[] logFiles = new File(TEST_REPORTS_DIR)
            .listFiles((dir, name) -> name.endsWith(".log"));
        String content = Files.readString(logFiles[0].toPath());
        
        for (int i = 0; i < threadCount; i++) {
            assertTrue("Log should contain concurrent test " + i,
                content.contains("concurrentTest" + i));
        }
    }
    
    @Test
    public void testMetricsAccuracy() throws Exception {
        Description description = Description.createTestDescription(
            getClass(), TEST_METHOD);
        
        // Start test run
        listener.testRunStarted(description);
        
        // Run multiple tests with known timing
        for (int i = 0; i < 3; i++) {
            Description testDesc = Description.createTestDescription(
                getClass(), "test" + i);
            listener.testStarted(testDesc);
            Thread.sleep(100); // Known delay
            listener.testFinished(testDesc);
        }
        
        // Complete test run
        Result result = new Result();
        listener.testRunFinished(result);
        
        // Verify metrics
        File[] logFiles = new File(TEST_REPORTS_DIR)
            .listFiles((dir, name) -> name.endsWith(".log"));
        String content = Files.readString(logFiles[0].toPath());
        
        assertTrue("Average duration should be around 100ms",
            content.contains("Avg Duration") && 
            content.contains("100"));
    }
}
