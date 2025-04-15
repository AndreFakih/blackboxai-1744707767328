package com.elvecha.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes test results to provide insights and recommendations
 */
public class TestReportAnalyzer {
    private final TestLogger logger;
    
    public TestReportAnalyzer() {
        this.logger = new TestLogger();
    }
    
    /**
     * Analyzes test results and generates insights
     */
    public AnalysisResult analyze(TestReportGenerator.TestResults results) {
        try {
            AnalysisBuilder builder = new AnalysisBuilder();
            
            // Analyze overall metrics
            analyzeMetrics(builder, results);
            
            // Analyze categories
            analyzeCategories(builder, results);
            
            // Analyze test patterns
            analyzeTestPatterns(builder, results);
            
            // Analyze performance
            analyzePerformance(builder, results);
            
            // Generate recommendations
            generateRecommendations(builder, results);
            
            return builder.build();
        } catch (Exception e) {
            logger.logError("Failed to analyze test results", e);
            throw new RuntimeException("Analysis failed", e);
        }
    }
    
    private void analyzeMetrics(AnalysisBuilder builder,
                              TestReportGenerator.TestResults results) {
        // Calculate pass rate
        double passRate = (double) results.getPassedTests() / results.getTotalTests();
        builder.addMetric("passRate", passRate);
        
        if (passRate < 0.9) {
            builder.addIssue(new Issue(
                "Low Pass Rate",
                String.format("Pass rate is %.2f%%, which is below the target of 90%%",
                    passRate * 100),
                "Review failed tests and address common failure patterns"
            ));
        }
        
        // Analyze skip rate
        double skipRate = (double) results.getSkippedTests() / results.getTotalTests();
        builder.addMetric("skipRate", skipRate);
        
        if (skipRate > 0.1) {
            builder.addIssue(new Issue(
                "High Skip Rate",
                String.format("Skip rate is %.2f%%, which is above the threshold of 10%%",
                    skipRate * 100),
                "Review skipped tests to ensure they are still relevant"
            ));
        }
    }
    
    private void analyzeCategories(AnalysisBuilder builder,
                                 TestReportGenerator.TestResults results) {
        Map<String, CategoryMetrics> categoryMetrics = new HashMap<>();
        
        // Calculate metrics for each category
        for (TestReportGenerator.TestCategory category : results.getCategories()) {
            double passRate = (double) category.getPassedTests() / category.getTotalTests();
            double avgDuration = (double) category.getDuration() / category.getTotalTests();
            
            categoryMetrics.put(category.getName(), new CategoryMetrics(
                passRate, avgDuration, category.getTotalTests()));
                
            // Identify problematic categories
            if (passRate < 0.8) {
                builder.addIssue(new Issue(
                    "Category Issues",
                    String.format("Category '%s' has a low pass rate of %.2f%%",
                        category.getName(), passRate * 100),
                    "Review and stabilize tests in this category"
                ));
            }
            
            // Identify slow categories
            if (avgDuration > 2000) { // 2 seconds threshold
                builder.addIssue(new Issue(
                    "Performance Issues",
                    String.format("Category '%s' has high average test duration: %.2fs",
                        category.getName(), avgDuration / 1000),
                    "Optimize tests in this category to improve performance"
                ));
            }
        }
        
        builder.addCategoryMetrics(categoryMetrics);
    }
    
    private void analyzeTestPatterns(AnalysisBuilder builder,
                                   TestReportGenerator.TestResults results) {
        Map<String, Integer> errorPatterns = new HashMap<>();
        List<String> longTests = new ArrayList<>();
        List<String> unstableTests = new ArrayList<>();
        
        for (TestReportGenerator.TestResult test : results.getTestResults()) {
            // Analyze error patterns
            if (!test.isPassed() && !test.isSkipped()) {
                String errorType = categorizeError(test.getErrorMessage());
                errorPatterns.merge(errorType, 1, Integer::sum);
            }
            
            // Identify long-running tests
            if (test.getDuration() > 5000) { // 5 seconds threshold
                longTests.add(test.getTestName());
            }
            
            // TODO: Analyze test stability (requires historical data)
        }
        
        // Report common error patterns
        if (!errorPatterns.isEmpty()) {
            String commonError = errorPatterns.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
                
            builder.addIssue(new Issue(
                "Common Error Pattern",
                String.format("Most common error type: %s (%d occurrences)",
                    commonError, errorPatterns.get(commonError)),
                "Review and address the root cause of this error pattern"
            ));
        }
        
        // Report long-running tests
        if (!longTests.isEmpty()) {
            builder.addIssue(new Issue(
                "Long Tests",
                String.format("%d tests take longer than 5 seconds to execute",
                    longTests.size()),
                "Review and optimize long-running tests"
            ));
        }
        
        builder.addErrorPatterns(errorPatterns);
        builder.addLongTests(longTests);
        builder.addUnstableTests(unstableTests);
    }
    
    private String categorizeError(String errorMessage) {
        if (errorMessage == null) return "Unknown";
        
        if (errorMessage.contains("AssertionError")) return "Assertion Failure";
        if (errorMessage.contains("NullPointerException")) return "Null Pointer";
        if (errorMessage.contains("TimeoutException")) return "Timeout";
        if (errorMessage.contains("IOException")) return "IO Error";
        
        return "Other";
    }
    
    private void analyzePerformance(AnalysisBuilder builder,
                                  TestReportGenerator.TestResults results) {
        // Calculate overall execution time
        long totalDuration = results.getTestResults().stream()
            .mapToLong(TestReportGenerator.TestResult::getDuration)
            .sum();
            
        double avgDuration = (double) totalDuration / results.getTotalTests();
        builder.addMetric("averageDuration", avgDuration);
        
        // Identify performance trends
        if (avgDuration > 1000) { // 1 second threshold
            builder.addIssue(new Issue(
                "Performance Concern",
                String.format("Average test duration is %.2fs", avgDuration / 1000),
                "Consider optimizing test execution time"
            ));
        }
        
        // Calculate performance distribution
        Map<String, Double> durationDistribution = calculateDurationDistribution(results);
        builder.addDurationDistribution(durationDistribution);
    }
    
    private Map<String, Double> calculateDurationDistribution(
            TestReportGenerator.TestResults results) {
        Map<String, Double> distribution = new HashMap<>();
        int total = results.getTotalTests();
        
        long[] durations = results.getTestResults().stream()
            .mapToLong(TestReportGenerator.TestResult::getDuration)
            .sorted()
            .toArray();
            
        distribution.put("min", (double) durations[0]);
        distribution.put("max", (double) durations[durations.length - 1]);
        distribution.put("median", calculateMedian(durations));
        distribution.put("p90", calculatePercentile(durations, 90));
        distribution.put("p95", calculatePercentile(durations, 95));
        
        return distribution;
    }
    
    private double calculateMedian(long[] sorted) {
        if (sorted.length % 2 == 0) {
            return (sorted[sorted.length/2 - 1] + sorted[sorted.length/2]) / 2.0;
        } else {
            return sorted[sorted.length/2];
        }
    }
    
    private double calculatePercentile(long[] sorted, int percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sorted.length) - 1;
        return sorted[index];
    }
    
    private void generateRecommendations(AnalysisBuilder builder,
                                       TestReportGenerator.TestResults results) {
        List<String> recommendations = new ArrayList<>();
        
        // Pass rate recommendations
        double passRate = (double) results.getPassedTests() / results.getTotalTests();
        if (passRate < 0.9) {
            recommendations.add(
                "Focus on improving test stability and fixing failing tests");
        }
        
        // Skip rate recommendations
        double skipRate = (double) results.getSkippedTests() / results.getTotalTests();
        if (skipRate > 0.1) {
            recommendations.add(
                "Review and update or remove skipped tests");
        }
        
        // Performance recommendations
        if (!builder.longTests.isEmpty()) {
            recommendations.add(
                "Optimize long-running tests to improve overall execution time");
        }
        
        // Error pattern recommendations
        if (!builder.errorPatterns.isEmpty()) {
            recommendations.add(
                "Address common error patterns to improve test reliability");
        }
        
        builder.addRecommendations(recommendations);
    }
    
    /**
     * Represents the analysis results
     */
    public static class AnalysisResult {
        private final Map<String, Double> metrics;
        private final Map<String, CategoryMetrics> categoryMetrics;
        private final Map<String, Integer> errorPatterns;
        private final List<String> longTests;
        private final List<String> unstableTests;
        private final Map<String, Double> durationDistribution;
        private final List<Issue> issues;
        private final List<String> recommendations;
        
        AnalysisResult(AnalysisBuilder builder) {
            this.metrics = builder.metrics;
            this.categoryMetrics = builder.categoryMetrics;
            this.errorPatterns = builder.errorPatterns;
            this.longTests = builder.longTests;
            this.unstableTests = builder.unstableTests;
            this.durationDistribution = builder.durationDistribution;
            this.issues = builder.issues;
            this.recommendations = builder.recommendations;
        }
        
        public Map<String, Double> getMetrics() { return metrics; }
        public Map<String, CategoryMetrics> getCategoryMetrics() { return categoryMetrics; }
        public Map<String, Integer> getErrorPatterns() { return errorPatterns; }
        public List<String> getLongTests() { return longTests; }
        public List<String> getUnstableTests() { return unstableTests; }
        public Map<String, Double> getDurationDistribution() { return durationDistribution; }
        public List<Issue> getIssues() { return issues; }
        public List<String> getRecommendations() { return recommendations; }
    }
    
    private static class AnalysisBuilder {
        private final Map<String, Double> metrics = new HashMap<>();
        private final Map<String, CategoryMetrics> categoryMetrics = new HashMap<>();
        private final Map<String, Integer> errorPatterns = new HashMap<>();
        private final List<String> longTests = new ArrayList<>();
        private final List<String> unstableTests = new ArrayList<>();
        private final Map<String, Double> durationDistribution = new HashMap<>();
        private final List<Issue> issues = new ArrayList<>();
        private final List<String> recommendations = new ArrayList<>();
        
        void addMetric(String name, double value) {
            metrics.put(name, value);
        }
        
        void addCategoryMetrics(Map<String, CategoryMetrics> metrics) {
            categoryMetrics.putAll(metrics);
        }
        
        void addErrorPatterns(Map<String, Integer> patterns) {
            errorPatterns.putAll(patterns);
        }
        
        void addLongTests(List<String> tests) {
            longTests.addAll(tests);
        }
        
        void addUnstableTests(List<String> tests) {
            unstableTests.addAll(tests);
        }
        
        void addDurationDistribution(Map<String, Double> distribution) {
            durationDistribution.putAll(distribution);
        }
        
        void addIssue(Issue issue) {
            issues.add(issue);
        }
        
        void addRecommendations(List<String> recs) {
            recommendations.addAll(recs);
        }
        
        AnalysisResult build() {
            return new AnalysisResult(this);
        }
    }
    
    public static class CategoryMetrics {
        private final double passRate;
        private final double averageDuration;
        private final int totalTests;
        
        CategoryMetrics(double passRate, double averageDuration, int totalTests) {
            this.passRate = passRate;
            this.averageDuration = averageDuration;
            this.totalTests = totalTests;
        }
        
        public double getPassRate() { return passRate; }
        public double getAverageDuration() { return averageDuration; }
        public int getTotalTests() { return totalTests; }
    }
    
    public static class Issue {
        private final String title;
        private final String description;
        private final String recommendation;
        
        Issue(String title, String description, String recommendation) {
            this.title = title;
            this.description = description;
            this.recommendation = recommendation;
        }
        
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getRecommendation() { return recommendation; }
    }
}
