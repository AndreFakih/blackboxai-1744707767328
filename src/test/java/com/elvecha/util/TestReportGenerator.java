package com.elvecha.util;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates HTML test reports using the template
 */
public class TestReportGenerator {
    private final TestLogger logger;
    private final String templatePath;
    private final String outputDir;
    private String template;
    
    public TestReportGenerator() {
        this.logger = new TestLogger();
        this.templatePath = "src/test/resources/report-template.html";
        this.outputDir = "test-reports";
        loadTemplate();
    }
    
    /**
     * Generates a test report from test results
     */
    public void generateReport(TestResults results) {
        try {
            String report = template;
            
            // Replace placeholders with actual data
            report = replaceBasicInfo(report, results);
            report = replaceMetrics(report, results);
            report = replaceTestResults(report, results);
            report = replaceCategoryResults(report, results);
            report = replacePerformanceMetrics(report, results);
            report = replaceIssues(report, results);
            report = replaceEnvironment(report);
            
            // Write report to file
            writeReport(report, results.getSessionId());
            
        } catch (Exception e) {
            logger.logError("Failed to generate report", e);
            throw new RuntimeException("Report generation failed", e);
        }
    }
    
    private void loadTemplate() {
        try {
            template = new String(Files.readAllBytes(Paths.get(templatePath)));
        } catch (IOException e) {
            logger.logError("Failed to load report template", e);
            throw new RuntimeException("Template loading failed", e);
        }
    }
    
    private String replaceBasicInfo(String report, TestResults results) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new Date());
            
        report = report.replace("{{TIMESTAMP}}", timestamp);
        report = report.replace("{{SESSION_ID}}", results.getSessionId());
        
        String status = results.getPassRate() >= 0.9 ? "PASSED" : "FAILED";
        String statusColor = results.getPassRate() >= 0.9 ? 
            "bg-green-100 text-green-800" : "bg-red-100 text-red-800";
            
        report = report.replace("{{STATUS}}", status);
        report = report.replace("{{STATUS_COLOR}}", statusColor);
        
        return report;
    }
    
    private String replaceMetrics(String report, TestResults results) {
        report = report.replace("{{TOTAL_TESTS}}", 
            String.valueOf(results.getTotalTests()));
            
        report = report.replace("{{PASS_RATE}}", 
            String.format("%.1f", results.getPassRate() * 100));
            
        report = report.replace("{{DURATION}}", 
            formatDuration(results.getTotalDuration()));
            
        report = report.replace("{{COVERAGE}}", 
            String.format("%.1f", results.getCoverage() * 100));
            
        return report;
    }
    
    private String replaceTestResults(String report, TestResults results) {
        StringBuilder testResults = new StringBuilder();
        
        for (TestResult test : results.getTestResults()) {
            String resultHtml = generateTestResultHtml(test);
            testResults.append(resultHtml);
        }
        
        return report.replace("{{TEST_RESULTS}}", testResults.toString());
    }
    
    private String generateTestResultHtml(TestResult test) {
        String template = getTestResultTemplate();
        
        String resultColor = test.isPassed() ? 
            "bg-green-500" : (test.isSkipped() ? "bg-yellow-500" : "bg-red-500");
            
        String resultIcon = test.isPassed() ? 
            "fa-check" : (test.isSkipped() ? "fa-pause" : "fa-times");
            
        template = template.replace("{{RESULT_COLOR}}", resultColor);
        template = template.replace("{{RESULT_ICON}}", resultIcon);
        template = template.replace("{{TEST_NAME}}", test.getTestName());
        template = template.replace("{{TEST_DURATION}}", 
            formatDuration(test.getDuration()));
        template = template.replace("{{TEST_CLASS}}", test.getTestClass());
        template = template.replace("{{TEST_ID}}", 
            "test-" + test.getTestName().replaceAll("\\W+", "-"));
            
        String details = test.isPassed() ? 
            "Test passed successfully" : test.getErrorMessage();
        template = template.replace("{{TEST_DETAILS}}", details);
        
        return template;
    }
    
    private String replaceCategoryResults(String report, TestResults results) {
        StringBuilder categoryResults = new StringBuilder();
        
        for (TestCategory category : results.getCategories()) {
            String categoryHtml = generateCategoryHtml(category);
            categoryResults.append(categoryHtml);
        }
        
        return report.replace("{{CATEGORY_RESULTS}}", categoryResults.toString());
    }
    
    private String generateCategoryHtml(TestCategory category) {
        String template = getCategoryTemplate();
        
        double passRate = (double) category.getPassedTests() / category.getTotalTests() * 100;
        String color = passRate >= 90 ? "bg-green-500" : 
            (passRate >= 75 ? "bg-yellow-500" : "bg-red-500");
            
        template = template.replace("{{CATEGORY_NAME}}", category.getName());
        template = template.replace("{{CATEGORY_PASS_RATE}}", 
            String.format("%.1f", passRate));
        template = template.replace("{{CATEGORY_COLOR}}", color);
        
        return template;
    }
    
    private String replacePerformanceMetrics(String report, TestResults results) {
        StringBuilder metrics = new StringBuilder();
        
        Map<String, Double> perfMetrics = results.getPerformanceMetrics();
        for (Map.Entry<String, Double> metric : perfMetrics.entrySet()) {
            String metricHtml = generateMetricHtml(
                metric.getKey(), metric.getValue(), results.getMetricThreshold(metric.getKey()));
            metrics.append(metricHtml);
        }
        
        return report.replace("{{PERFORMANCE_METRICS}}", metrics.toString());
    }
    
    private String generateMetricHtml(String name, double value, double threshold) {
        String template = getMetricTemplate();
        
        double percentage = (value / threshold) * 100;
        String color = percentage <= 75 ? "bg-green-500" : 
            (percentage <= 90 ? "bg-yellow-500" : "bg-red-500");
            
        template = template.replace("{{METRIC_NAME}}", name);
        template = template.replace("{{METRIC_VALUE}}", 
            String.format("%.2f", value));
        template = template.replace("{{METRIC_PERCENTAGE}}", 
            String.format("%.1f", percentage));
        template = template.replace("{{METRIC_COLOR}}", color);
        
        return template;
    }
    
    private String replaceIssues(String report, TestResults results) {
        StringBuilder issues = new StringBuilder();
        
        for (TestIssue issue : results.getIssues()) {
            String issueHtml = generateIssueHtml(issue);
            issues.append(issueHtml);
        }
        
        return report.replace("{{ISSUES}}", issues.toString());
    }
    
    private String generateIssueHtml(TestIssue issue) {
        String template = getIssueTemplate();
        
        String severity = issue.getSeverity();
        String bg = severity.equals("ERROR") ? "bg-red-50" : 
            (severity.equals("WARN") ? "bg-yellow-50" : "bg-blue-50");
        String color = severity.equals("ERROR") ? "text-red-400" : 
            (severity.equals("WARN") ? "text-yellow-400" : "text-blue-400");
        String textColor = severity.equals("ERROR") ? "text-red-800" : 
            (severity.equals("WARN") ? "text-yellow-800" : "text-blue-800");
        String icon = severity.equals("ERROR") ? "fa-exclamation-circle" : 
            (severity.equals("WARN") ? "fa-exclamation-triangle" : "fa-info-circle");
            
        template = template.replace("{{ISSUE_BG}}", bg);
        template = template.replace("{{ISSUE_COLOR}}", color);
        template = template.replace("{{ISSUE_TEXT_COLOR}}", textColor);
        template = template.replace("{{ISSUE_ICON}}", icon);
        template = template.replace("{{ISSUE_TITLE}}", issue.getTitle());
        template = template.replace("{{ISSUE_DESCRIPTION}}", issue.getDescription());
        
        return template;
    }
    
    private String replaceEnvironment(String report) {
        StringBuilder env = new StringBuilder();
        
        // System properties
        env.append(generateEnvHtml("Java Version", 
            System.getProperty("java.version")));
        env.append(generateEnvHtml("OS", 
            System.getProperty("os.name")));
        env.append(generateEnvHtml("Memory", 
            Runtime.getRuntime().maxMemory() / (1024*1024) + " MB"));
        env.append(generateEnvHtml("Processors", 
            String.valueOf(Runtime.getRuntime().availableProcessors())));
        
        // Test properties
        Properties props = loadTestProperties();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("test.env.")) {
                String name = key.substring("test.env.".length())
                    .replace(".", " ")
                    .toUpperCase();
                env.append(generateEnvHtml(name, props.getProperty(key)));
            }
        }
        
        return report.replace("{{ENVIRONMENT}}", env.toString());
    }
    
    private String generateEnvHtml(String name, String value) {
        String template = getEnvTemplate();
        
        template = template.replace("{{ENV_NAME}}", name);
        template = template.replace("{{ENV_VALUE}}", value);
        
        return template;
    }
    
    private void writeReport(String report, String sessionId) throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        
        String filename = String.format("test-report-%s.html", sessionId);
        Path reportPath = outputPath.resolve(filename);
        
        Files.write(reportPath, report.getBytes());
        logger.log("Report generated: " + reportPath);
    }
    
    private Properties loadTestProperties() {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream("src/test/resources/test.properties")) {
            props.load(is);
        } catch (IOException e) {
            logger.logError("Failed to load test properties", e);
        }
        return props;
    }
    
    private String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        return String.format("%.2fs", millis / 1000.0);
    }
    
    private String getTestResultTemplate() {
        return template.substring(
            template.indexOf("const testResultTemplate = `") + 27,
            template.indexOf("`;", template.indexOf("const testResultTemplate = `"))
        );
    }
    
    private String getCategoryTemplate() {
        return template.substring(
            template.indexOf("const categoryTemplate = `") + 25,
            template.indexOf("`;", template.indexOf("const categoryTemplate = `"))
        );
    }
    
    private String getMetricTemplate() {
        return template.substring(
            template.indexOf("const metricTemplate = `") + 24,
            template.indexOf("`;", template.indexOf("const metricTemplate = `"))
        );
    }
    
    private String getIssueTemplate() {
        return template.substring(
            template.indexOf("const issueTemplate = `") + 23,
            template.indexOf("`;", template.indexOf("const issueTemplate = `"))
        );
    }
    
    private String getEnvTemplate() {
        return template.substring(
            template.indexOf("const envTemplate = `") + 21,
            template.indexOf("`;", template.indexOf("const envTemplate = `"))
        );
    }
    
    /**
     * Test results data structure
     */
    public static class TestResults {
        private final String sessionId;
        private final List<TestResult> testResults;
        private final List<TestCategory> categories;
        private final List<TestIssue> issues;
        private final Map<String, Double> performanceMetrics;
        private final Map<String, Double> metricThresholds;
        private final long totalDuration;
        private final double coverage;
        
        public TestResults(String sessionId, List<TestResult> testResults,
                List<TestCategory> categories, List<TestIssue> issues,
                Map<String, Double> performanceMetrics, Map<String, Double> metricThresholds,
                long totalDuration, double coverage) {
            this.sessionId = sessionId;
            this.testResults = testResults;
            this.categories = categories;
            this.issues = issues;
            this.performanceMetrics = performanceMetrics;
            this.metricThresholds = metricThresholds;
            this.totalDuration = totalDuration;
            this.coverage = coverage;
        }
        
        public String getSessionId() { return sessionId; }
        public List<TestResult> getTestResults() { return testResults; }
        public List<TestCategory> getCategories() { return categories; }
        public List<TestIssue> getIssues() { return issues; }
        public Map<String, Double> getPerformanceMetrics() { return performanceMetrics; }
        public double getMetricThreshold(String metric) { return metricThresholds.get(metric); }
        public long getTotalDuration() { return totalDuration; }
        public double getCoverage() { return coverage; }
        
        public int getTotalTests() { return testResults.size(); }
        
        public double getPassRate() {
            long passed = testResults.stream()
                .filter(TestResult::isPassed)
                .count();
            return (double) passed / testResults.size();
        }
    }
    
    /**
     * Individual test result
     */
    public static class TestResult {
        private final String testName;
        private final String testClass;
        private final boolean passed;
        private final boolean skipped;
        private final String errorMessage;
        private final long duration;
        
        public TestResult(String testName, String testClass, boolean passed,
                boolean skipped, String errorMessage, long duration) {
            this.testName = testName;
            this.testClass = testClass;
            this.passed = passed;
            this.skipped = skipped;
            this.errorMessage = errorMessage;
            this.duration = duration;
        }
        
        public String getTestName() { return testName; }
        public String getTestClass() { return testClass; }
        public boolean isPassed() { return passed; }
        public boolean isSkipped() { return skipped; }
        public String getErrorMessage() { return errorMessage; }
        public long getDuration() { return duration; }
    }
    
    /**
     * Test category results
     */
    public static class TestCategory {
        private final String name;
        private final int totalTests;
        private final int passedTests;
        private final int skippedTests;
        private final long duration;
        
        public TestCategory(String name, int totalTests, int passedTests,
                int skippedTests, long duration) {
            this.name = name;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.skippedTests = skippedTests;
            this.duration = duration;
        }
        
        public String getName() { return name; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getSkippedTests() { return skippedTests; }
        public long getDuration() { return duration; }
    }
    
    /**
     * Test issue
     */
    public static class TestIssue {
        private final String severity;
        private final String title;
        private final String description;
        
        public TestIssue(String severity, String title, String description) {
            this.severity = severity;
            this.title = title;
            this.description = description;
        }
        
        public String getSeverity() { return severity; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
}
