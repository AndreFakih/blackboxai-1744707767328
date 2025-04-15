package com.elvecha.util;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

/**
 * JUnit RunListener implementation that provides real-time test execution feedback
 * and collects detailed metrics about test execution
 */
public class TestExecutionListener extends RunListener {
    private final Map<String, TestMetrics> categoryMetrics;
    private final AtomicInteger totalTests;
    private final AtomicInteger completedTests;
    private final long suiteStartTime;
    private final TestLogger logger;
    private final TestReportRule reportRule;

    public TestExecutionListener() {
        this.categoryMetrics = new ConcurrentHashMap<>();
        this.totalTests = new AtomicInteger(0);
        this.completedTests = new AtomicInteger(0);
        this.suiteStartTime = System.currentTimeMillis();
        this.logger = new TestLogger();
        this.reportRule = new TestReportRule();
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        totalTests.set(description.testCount());
        logger.log(String.format("Starting test execution. Total tests: %d",
            totalTests.get()));
        
        // Add initial metrics to report
        reportRule.addCustomSection(
            "Test Execution",
            "Initial Setup",
            String.format("Total tests to run: %d\nStart time: %s",
                totalTests.get(),
                new java.util.Date(suiteStartTime))
        );
    }

    @Override
    public void testStarted(Description description) throws Exception {
        String category = getCategory(description);
        TestMetrics metrics = categoryMetrics.computeIfAbsent(category,
            k -> new TestMetrics());
        
        metrics.startTest();
        logger.log(String.format("Starting test: %s.%s",
            category, description.getMethodName()));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        String category = getCategory(description);
        TestMetrics metrics = categoryMetrics.get(category);
        
        if (metrics != null) {
            metrics.finishTest();
            int completed = completedTests.incrementAndGet();
            
            // Log progress
            double progress = (completed * 100.0) / totalTests.get();
            logger.log(String.format("Test completed: %s.%s (%.1f%% complete)",
                category, description.getMethodName(), progress));
            
            // Update report
            updateProgressReport(category, metrics);
        }
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        String category = getCategory(failure.getDescription());
        TestMetrics metrics = categoryMetrics.get(category);
        
        if (metrics != null) {
            metrics.recordFailure();
            logger.logError(String.format("Test failed: %s.%s",
                category, failure.getDescription().getMethodName()),
                failure.getException());
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        long duration = System.currentTimeMillis() - suiteStartTime;
        
        // Generate final metrics
        StringBuilder summary = new StringBuilder();
        summary.append("Test Execution Summary:\n\n");
        summary.append(String.format("Total Duration: %.2f seconds\n",
            duration / 1000.0));
        summary.append(String.format("Tests Run: %d\n", result.getRunCount()));
        summary.append(String.format("Tests Passed: %d\n",
            result.getRunCount() - result.getFailureCount()));
        summary.append(String.format("Tests Failed: %d\n",
            result.getFailureCount()));
        summary.append(String.format("Success Rate: %.1f%%\n",
            (result.getRunCount() - result.getFailureCount()) * 100.0 /
            result.getRunCount()));
        
        // Add category metrics
        summary.append("\nCategory Metrics:\n");
        categoryMetrics.forEach((category, metrics) -> {
            summary.append(String.format("\n%s:\n", category));
            summary.append(String.format("  Tests: %d\n", metrics.totalTests));
            summary.append(String.format("  Passed: %d\n",
                metrics.totalTests - metrics.failures));
            summary.append(String.format("  Failed: %d\n", metrics.failures));
            summary.append(String.format("  Avg Duration: %.2f ms\n",
                metrics.getTotalDuration() / (double)metrics.totalTests));
        });
        
        // Add to report
        reportRule.addCustomSection(
            "Test Execution",
            "Final Summary",
            summary.toString()
        );
        
        logger.log("Test execution completed");
    }

    private String getCategory(Description description) {
        return description.getTestClass().getSimpleName();
    }

    private void updateProgressReport(String category, TestMetrics metrics) {
        String progressInfo = String.format(
            "%s Progress:\nTests Run: %d\nPassed: %d\nFailed: %d\n" +
            "Average Duration: %.2f ms",
            category,
            metrics.totalTests,
            metrics.totalTests - metrics.failures,
            metrics.failures,
            metrics.getTotalDuration() / (double)metrics.totalTests
        );
        
        reportRule.addCustomSection(
            "Test Progress",
            category,
            progressInfo
        );
    }

    private static class TestMetrics {
        private final AtomicInteger totalTests = new AtomicInteger(0);
        private final AtomicInteger failures = new AtomicInteger(0);
        private final AtomicInteger activeTests = new AtomicInteger(0);
        private long totalDuration = 0;
        private long lastStartTime;

        void startTest() {
            totalTests.incrementAndGet();
            activeTests.incrementAndGet();
            lastStartTime = System.nanoTime();
        }

        void finishTest() {
            activeTests.decrementAndGet();
            totalDuration += (System.nanoTime() - lastStartTime) / 1_000_000; // Convert to ms
        }

        void recordFailure() {
            failures.incrementAndGet();
        }

        long getTotalDuration() {
            return totalDuration;
        }
    }
}
