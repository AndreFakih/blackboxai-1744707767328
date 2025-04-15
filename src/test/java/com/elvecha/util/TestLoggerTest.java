package com.elvecha.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestLoggerTest {
    private static final String LOG_DIR = "test-logs";
    private File logDir;

    @Before
    public void setUp() {
        logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        TestLogger.log("Test setup complete");
    }

    @After
    public void tearDown() {
        TestLogger.close();
        deleteDirectory(logDir);
    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    @Test
    public void testBasicLogging() throws Exception {
        String testMessage = "Test message";
        TestLogger.log(testMessage);
        
        // Verify log file exists and contains message
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        assertNotNull("Log files should exist", logFiles);
        assertTrue("Should have at least one log file", logFiles.length > 0);
        
        // Read log file content
        List<String> lines = Files.readAllLines(logFiles[0].toPath());
        boolean messageFound = lines.stream()
            .anyMatch(line -> line.contains(testMessage));
        assertTrue("Log should contain test message", messageFound);
    }

    @Test
    public void testErrorLogging() throws Exception {
        Exception testError = new RuntimeException("Test error");
        TestLogger.logError("Error occurred", testError);
        
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        List<String> lines = Files.readAllLines(logFiles[0].toPath());
        
        boolean errorFound = lines.stream()
            .anyMatch(line -> line.contains("ERROR") && 
                            line.contains("Error occurred"));
        assertTrue("Log should contain error message", errorFound);
        
        boolean stackTraceFound = lines.stream()
            .anyMatch(line -> line.contains("RuntimeException"));
        assertTrue("Log should contain stack trace", stackTraceFound);
    }

    @Test
    public void testTimerFunctionality() throws Exception {
        TestLogger.startTimer("testTimer");
        Thread.sleep(100); // Simulate some work
        long elapsed = TestLogger.stopTimer("testTimer");
        
        assertTrue("Timer should measure at least 100ms", elapsed >= 100_000_000);
        
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        List<String> lines = Files.readAllLines(logFiles[0].toPath());
        boolean timerFound = lines.stream()
            .anyMatch(line -> line.contains("Timer 'testTimer' elapsed"));
        assertTrue("Log should contain timer information", timerFound);
    }

    @Test
    public void testMeasureTime() {
        String result = TestLogger.measureTime("operation", () -> {
            try {
                Thread.sleep(50);
                return "test result";
            } catch (InterruptedException e) {
                return null;
            }
        });
        
        assertEquals("Operation should return correct result", 
            "test result", result);
    }

    @Test
    public void testMemoryUsageLogging() throws Exception {
        TestLogger.logMemoryUsage("Test Context");
        
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        List<String> lines = Files.readAllLines(logFiles[0].toPath());
        
        boolean memoryInfoFound = lines.stream()
            .anyMatch(line -> line.contains("Memory Usage") && 
                            line.contains("Test Context"));
        assertTrue("Log should contain memory usage information", memoryInfoFound);
    }

    @Test
    public void testTestInfoLogging() throws Exception {
        TestLogger.logTestInfo("TestCase", "Test description");
        TestLogger.logTestComplete("TestCase", true);
        
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        List<String> lines = Files.readAllLines(logFiles[0].toPath());
        
        boolean testStartFound = lines.stream()
            .anyMatch(line -> line.contains("=== Test: TestCase ==="));
        assertTrue("Log should contain test start marker", testStartFound);
        
        boolean testEndFound = lines.stream()
            .anyMatch(line -> line.contains("Test 'TestCase' PASSED"));
        assertTrue("Log should contain test end marker", testEndFound);
    }

    @Test
    public void testConcurrentLogging() throws Exception {
        int threadCount = 10;
        int messagesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < messagesPerThread; j++) {
                        TestLogger.log("Thread " + threadId + " message " + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        List<String> lines = Files.readAllLines(logFiles[0].toPath());
        
        int totalMessages = lines.stream()
            .filter(line -> line.contains("Thread") && line.contains("message"))
            .count();
            
        assertEquals("Should have logged all messages", 
            threadCount * messagesPerThread, totalMessages);
    }

    @Test
    public void testLogCleanup() throws Exception {
        // Create some old log files
        Path oldLogPath = new File(logDir, "old_test.log").toPath();
        Files.write(oldLogPath, "Old log content".getBytes());
        
        // Set last modified to 7 days ago
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        oldLogPath.toFile().setLastModified(sevenDaysAgo);
        
        TestLogger.cleanupOldLogs(5); // Keep logs newer than 5 days
        
        assertFalse("Old log file should be deleted", 
            oldLogPath.toFile().exists());
    }
}
