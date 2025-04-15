package com.elvecha.util;

import java.util.*;

/**
 * Builder pattern implementation for creating test reports
 * Makes it easy to construct TestResults objects with fluent API
 */
public class TestReportBuilder {
    private long timestamp;
    private final List<TestReportGenerator.TestCategory> categories;
    private final List<TestReportGenerator.TestResult> testResults;
    private final Map<String, String> environmentInfo;
    
    private TestReportBuilder() {
        this.timestamp = System.currentTimeMillis();
        this.categories = new ArrayList<>();
        this.testResults = new ArrayList<>();
        this.environmentInfo = new HashMap<>();
    }
    
    public static TestReportBuilder create() {
        return new TestReportBuilder();
    }
    
    /**
     * Sets the timestamp for the report
     */
    public TestReportBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
    
    /**
     * Adds a category to the report
     */
    public TestReportBuilder addCategory(String name, int total, int passed, 
                                       int failed, long duration) {
        categories.add(new TestReportGenerator.TestCategory(
            name, total, passed, failed, duration));
        return this;
    }
    
    /**
     * Adds a successful test result
     */
    public TestReportBuilder addPassedTest(String testName, long duration) {
        testResults.add(new TestReportGenerator.TestResult(
            testName, true, false, null, duration));
        return this;
    }
    
    /**
     * Adds a failed test result
     */
    public TestReportBuilder addFailedTest(String testName, String errorMessage, 
                                         long duration) {
        testResults.add(new TestReportGenerator.TestResult(
            testName, false, false, errorMessage, duration));
        return this;
    }
    
    /**
     * Adds a skipped test result
     */
    public TestReportBuilder addSkippedTest(String testName) {
        testResults.add(new TestReportGenerator.TestResult(
            testName, false, true, null, 0));
        return this;
    }
    
    /**
     * Adds multiple test results from a collection
     */
    public TestReportBuilder addTestResults(
            Collection<TestReportGenerator.TestResult> results) {
        testResults.addAll(results);
        return this;
    }
    
    /**
     * Adds multiple categories from a collection
     */
    public TestReportBuilder addCategories(
            Collection<TestReportGenerator.TestCategory> cats) {
        categories.addAll(cats);
        return this;
    }
    
    /**
     * Adds an environment information entry
     */
    public TestReportBuilder addEnvironmentInfo(String key, String value) {
        environmentInfo.put(key, value);
        return this;
    }
    
    /**
     * Adds multiple environment information entries
     */
    public TestReportBuilder addEnvironmentInfo(Map<String, String> info) {
        environmentInfo.putAll(info);
        return this;
    }
    
    /**
     * Adds standard environment information
     */
    public TestReportBuilder withStandardEnvironmentInfo() {
        addEnvironmentInfo("Java Version", System.getProperty("java.version"));
        addEnvironmentInfo("OS", System.getProperty("os.name"));
        addEnvironmentInfo("Memory", Runtime.getRuntime().maxMemory() / (1024*1024) + "MB");
        addEnvironmentInfo("Processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        addEnvironmentInfo("User", System.getProperty("user.name"));
        addEnvironmentInfo("Working Directory", System.getProperty("user.dir"));
        return this;
    }
    
    /**
     * Adds random test data for testing purposes
     */
    public TestReportBuilder withRandomTestData(int numTests, int numCategories) {
        Random random = new Random();
        
        // Add categories
        for (int i = 0; i < numCategories; i++) {
            int total = 10 + random.nextInt(40);
            int failed = random.nextInt(5);
            addCategory(
                "Category" + i,
                total,
                total - failed,
                failed,
                1000 + random.nextInt(4000)
            );
        }
        
        // Add test results
        for (int i = 0; i < numTests; i++) {
            if (random.nextFloat() < 0.1f) { // 10% skipped
                addSkippedTest("Test" + i);
            } else if (random.nextFloat() < 0.8f) { // 70% passed
                addPassedTest("Test" + i, 500 + random.nextInt(2000));
            } else { // 20% failed
                addFailedTest(
                    "Test" + i,
                    "Assertion failed: expected " + random.nextInt(100),
                    500 + random.nextInt(2000)
                );
            }
        }
        
        return this;
    }
    
    /**
     * Builds the TestResults object
     */
    public TestReportGenerator.TestResults build() {
        validateReport();
        return new TestReportGenerator.TestResults(
            timestamp,
            categories,
            testResults,
            environmentInfo
        );
    }
    
    /**
     * Validates the report data before building
     */
    private void validateReport() {
        // Validate timestamp
        if (timestamp <= 0) {
            throw new IllegalStateException("Invalid timestamp");
        }
        
        // Validate categories
        for (TestReportGenerator.TestCategory category : categories) {
            if (category.getTotalTests() < 0 ||
                category.getPassedTests() < 0 ||
                category.getFailedTests() < 0 ||
                category.getDuration() < 0) {
                throw new IllegalStateException(
                    "Invalid metrics in category: " + category.getName());
            }
            
            if (category.getPassedTests() + category.getFailedTests() > 
                category.getTotalTests()) {
                throw new IllegalStateException(
                    "Sum of passed and failed tests exceeds total in category: " + 
                    category.getName());
            }
        }
        
        // Validate test results
        Set<String> testNames = new HashSet<>();
        for (TestReportGenerator.TestResult result : testResults) {
            if (result.getTestName() == null || result.getTestName().isEmpty()) {
                throw new IllegalStateException("Test name cannot be null or empty");
            }
            
            if (!testNames.add(result.getTestName())) {
                throw new IllegalStateException(
                    "Duplicate test name: " + result.getTestName());
            }
            
            if (!result.isSkipped() && result.getDuration() < 0) {
                throw new IllegalStateException(
                    "Invalid duration for test: " + result.getTestName());
            }
            
            if (!result.isPassed() && !result.isSkipped() && 
                (result.getErrorMessage() == null || 
                 result.getErrorMessage().isEmpty())) {
                throw new IllegalStateException(
                    "Failed test must have error message: " + result.getTestName());
            }
        }
        
        // Validate environment info
        for (Map.Entry<String, String> entry : environmentInfo.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty() ||
                entry.getValue() == null || entry.getValue().isEmpty()) {
                throw new IllegalStateException("Invalid environment info entry");
            }
        }
    }
}
