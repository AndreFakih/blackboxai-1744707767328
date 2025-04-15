package com.elvecha.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares test results to analyze changes and trends
 */
public class TestReportComparator {
    private final TestLogger logger;
    
    public TestReportComparator() {
        this.logger = new TestLogger();
    }
    
    /**
     * Compares two test results and generates a comparison report
     */
    public ComparisonResult compare(TestReportGenerator.TestResults current,
                                  TestReportGenerator.TestResults previous) {
        try {
            ComparisonBuilder builder = new ComparisonBuilder();
            
            // Compare basic metrics
            compareMetrics(builder, current, previous);
            
            // Compare categories
            compareCategories(builder, current, previous);
            
            // Compare test results
            compareTests(builder, current, previous);
            
            // Compare environment
            compareEnvironment(builder, current, previous);
            
            return builder.build();
        } catch (Exception e) {
            logger.logError("Failed to compare test results", e);
            throw new RuntimeException("Comparison failed", e);
        }
    }
    
    private void compareMetrics(ComparisonBuilder builder,
                              TestReportGenerator.TestResults current,
                              TestReportGenerator.TestResults previous) {
        builder.addMetricChange("total", previous.getTotalTests(), current.getTotalTests());
        builder.addMetricChange("passed", previous.getPassedTests(), current.getPassedTests());
        builder.addMetricChange("failed", previous.getFailedTests(), current.getFailedTests());
        builder.addMetricChange("skipped", previous.getSkippedTests(), current.getSkippedTests());
    }
    
    private void compareCategories(ComparisonBuilder builder,
                                 TestReportGenerator.TestResults current,
                                 TestReportGenerator.TestResults previous) {
        Map<String, TestReportGenerator.TestCategory> currentMap = mapCategories(current);
        Map<String, TestReportGenerator.TestCategory> previousMap = mapCategories(previous);
        
        // Find new categories
        Set<String> newCategories = new HashSet<>(currentMap.keySet());
        newCategories.removeAll(previousMap.keySet());
        builder.addNewCategories(newCategories);
        
        // Find removed categories
        Set<String> removedCategories = new HashSet<>(previousMap.keySet());
        removedCategories.removeAll(currentMap.keySet());
        builder.addRemovedCategories(removedCategories);
        
        // Compare existing categories
        Set<String> commonCategories = new HashSet<>(currentMap.keySet());
        commonCategories.retainAll(previousMap.keySet());
        
        for (String category : commonCategories) {
            TestReportGenerator.TestCategory currentCat = currentMap.get(category);
            TestReportGenerator.TestCategory previousCat = previousMap.get(category);
            
            CategoryChange change = new CategoryChange(
                category,
                previousCat.getTotalTests(),
                currentCat.getTotalTests(),
                previousCat.getPassedTests(),
                currentCat.getPassedTests(),
                previousCat.getFailedTests(),
                currentCat.getFailedTests(),
                previousCat.getDuration(),
                currentCat.getDuration()
            );
            
            builder.addCategoryChange(change);
        }
    }
    
    private Map<String, TestReportGenerator.TestCategory> mapCategories(
            TestReportGenerator.TestResults results) {
        return results.getCategories().stream()
            .collect(Collectors.toMap(
                TestReportGenerator.TestCategory::getName,
                category -> category
            ));
    }
    
    private void compareTests(ComparisonBuilder builder,
                            TestReportGenerator.TestResults current,
                            TestReportGenerator.TestResults previous) {
        Map<String, TestReportGenerator.TestResult> currentMap = mapTests(current);
        Map<String, TestReportGenerator.TestResult> previousMap = mapTests(previous);
        
        // Find new tests
        Set<String> newTests = new HashSet<>(currentMap.keySet());
        newTests.removeAll(previousMap.keySet());
        builder.addNewTests(newTests);
        
        // Find removed tests
        Set<String> removedTests = new HashSet<>(previousMap.keySet());
        removedTests.removeAll(currentMap.keySet());
        builder.addRemovedTests(removedTests);
        
        // Compare existing tests
        Set<String> commonTests = new HashSet<>(currentMap.keySet());
        commonTests.retainAll(previousMap.keySet());
        
        for (String test : commonTests) {
            TestReportGenerator.TestResult currentTest = currentMap.get(test);
            TestReportGenerator.TestResult previousTest = previousMap.get(test);
            
            if (currentTest.isPassed() != previousTest.isPassed() ||
                currentTest.isSkipped() != previousTest.isSkipped()) {
                TestStatusChange change = new TestStatusChange(
                    test,
                    previousTest.isPassed(),
                    currentTest.isPassed(),
                    previousTest.isSkipped(),
                    currentTest.isSkipped(),
                    previousTest.getErrorMessage(),
                    currentTest.getErrorMessage()
                );
                builder.addTestStatusChange(change);
            }
            
            // Check for significant duration changes (>20%)
            long durationDiff = Math.abs(currentTest.getDuration() - previousTest.getDuration());
            if (durationDiff > previousTest.getDuration() * 0.2) {
                builder.addTestDurationChange(test, 
                    previousTest.getDuration(), currentTest.getDuration());
            }
        }
    }
    
    private Map<String, TestReportGenerator.TestResult> mapTests(
            TestReportGenerator.TestResults results) {
        return results.getTestResults().stream()
            .collect(Collectors.toMap(
                TestReportGenerator.TestResult::getTestName,
                test -> test
            ));
    }
    
    private void compareEnvironment(ComparisonBuilder builder,
                                  TestReportGenerator.TestResults current,
                                  TestReportGenerator.TestResults previous) {
        Map<String, String> currentEnv = current.getEnvironmentInfo();
        Map<String, String> previousEnv = previous.getEnvironmentInfo();
        
        // Find new environment variables
        Set<String> newVars = new HashSet<>(currentEnv.keySet());
        newVars.removeAll(previousEnv.keySet());
        builder.addNewEnvironmentVars(newVars);
        
        // Find removed environment variables
        Set<String> removedVars = new HashSet<>(previousEnv.keySet());
        removedVars.removeAll(currentEnv.keySet());
        builder.addRemovedEnvironmentVars(removedVars);
        
        // Compare existing variables
        Set<String> commonVars = new HashSet<>(currentEnv.keySet());
        commonVars.retainAll(previousEnv.keySet());
        
        for (String var : commonVars) {
            String currentValue = currentEnv.get(var);
            String previousValue = previousEnv.get(var);
            
            if (!Objects.equals(currentValue, previousValue)) {
                builder.addEnvironmentChange(var, previousValue, currentValue);
            }
        }
    }
    
    /**
     * Represents a comparison between two test results
     */
    public static class ComparisonResult {
        private final Map<String, MetricChange> metricChanges;
        private final Set<String> newCategories;
        private final Set<String> removedCategories;
        private final List<CategoryChange> categoryChanges;
        private final Set<String> newTests;
        private final Set<String> removedTests;
        private final List<TestStatusChange> testStatusChanges;
        private final Map<String, DurationChange> testDurationChanges;
        private final Set<String> newEnvironmentVars;
        private final Set<String> removedEnvironmentVars;
        private final Map<String, EnvironmentChange> environmentChanges;
        
        ComparisonResult(ComparisonBuilder builder) {
            this.metricChanges = builder.metricChanges;
            this.newCategories = builder.newCategories;
            this.removedCategories = builder.removedCategories;
            this.categoryChanges = builder.categoryChanges;
            this.newTests = builder.newTests;
            this.removedTests = builder.removedTests;
            this.testStatusChanges = builder.testStatusChanges;
            this.testDurationChanges = builder.testDurationChanges;
            this.newEnvironmentVars = builder.newEnvironmentVars;
            this.removedEnvironmentVars = builder.removedEnvironmentVars;
            this.environmentChanges = builder.environmentChanges;
        }
        
        public Map<String, MetricChange> getMetricChanges() { return metricChanges; }
        public Set<String> getNewCategories() { return newCategories; }
        public Set<String> getRemovedCategories() { return removedCategories; }
        public List<CategoryChange> getCategoryChanges() { return categoryChanges; }
        public Set<String> getNewTests() { return newTests; }
        public Set<String> getRemovedTests() { return removedTests; }
        public List<TestStatusChange> getTestStatusChanges() { return testStatusChanges; }
        public Map<String, DurationChange> getTestDurationChanges() { return testDurationChanges; }
        public Set<String> getNewEnvironmentVars() { return newEnvironmentVars; }
        public Set<String> getRemovedEnvironmentVars() { return removedEnvironmentVars; }
        public Map<String, EnvironmentChange> getEnvironmentChanges() { return environmentChanges; }
        
        public boolean hasSignificantChanges() {
            return !newCategories.isEmpty() ||
                   !removedCategories.isEmpty() ||
                   !categoryChanges.isEmpty() ||
                   !testStatusChanges.isEmpty() ||
                   !testDurationChanges.isEmpty();
        }
    }
    
    private static class ComparisonBuilder {
        private final Map<String, MetricChange> metricChanges = new HashMap<>();
        private final Set<String> newCategories = new HashSet<>();
        private final Set<String> removedCategories = new HashSet<>();
        private final List<CategoryChange> categoryChanges = new ArrayList<>();
        private final Set<String> newTests = new HashSet<>();
        private final Set<String> removedTests = new HashSet<>();
        private final List<TestStatusChange> testStatusChanges = new ArrayList<>();
        private final Map<String, DurationChange> testDurationChanges = new HashMap<>();
        private final Set<String> newEnvironmentVars = new HashSet<>();
        private final Set<String> removedEnvironmentVars = new HashSet<>();
        private final Map<String, EnvironmentChange> environmentChanges = new HashMap<>();
        
        void addMetricChange(String metric, int previous, int current) {
            metricChanges.put(metric, new MetricChange(previous, current));
        }
        
        void addNewCategories(Set<String> categories) {
            newCategories.addAll(categories);
        }
        
        void addRemovedCategories(Set<String> categories) {
            removedCategories.addAll(categories);
        }
        
        void addCategoryChange(CategoryChange change) {
            categoryChanges.add(change);
        }
        
        void addNewTests(Set<String> tests) {
            newTests.addAll(tests);
        }
        
        void addRemovedTests(Set<String> tests) {
            removedTests.addAll(tests);
        }
        
        void addTestStatusChange(TestStatusChange change) {
            testStatusChanges.add(change);
        }
        
        void addTestDurationChange(String test, long previous, long current) {
            testDurationChanges.put(test, new DurationChange(previous, current));
        }
        
        void addNewEnvironmentVars(Set<String> vars) {
            newEnvironmentVars.addAll(vars);
        }
        
        void addRemovedEnvironmentVars(Set<String> vars) {
            removedEnvironmentVars.addAll(vars);
        }
        
        void addEnvironmentChange(String var, String previous, String current) {
            environmentChanges.put(var, new EnvironmentChange(previous, current));
        }
        
        ComparisonResult build() {
            return new ComparisonResult(this);
        }
    }
    
    public static class MetricChange {
        private final int previous;
        private final int current;
        private final int difference;
        
        MetricChange(int previous, int current) {
            this.previous = previous;
            this.current = current;
            this.difference = current - previous;
        }
        
        public int getPrevious() { return previous; }
        public int getCurrent() { return current; }
        public int getDifference() { return difference; }
    }
    
    public static class CategoryChange {
        private final String name;
        private final MetricChange total;
        private final MetricChange passed;
        private final MetricChange failed;
        private final DurationChange duration;
        
        CategoryChange(String name, int prevTotal, int currTotal,
                      int prevPassed, int currPassed,
                      int prevFailed, int currFailed,
                      long prevDuration, long currDuration) {
            this.name = name;
            this.total = new MetricChange(prevTotal, currTotal);
            this.passed = new MetricChange(prevPassed, currPassed);
            this.failed = new MetricChange(prevFailed, currFailed);
            this.duration = new DurationChange(prevDuration, currDuration);
        }
        
        public String getName() { return name; }
        public MetricChange getTotal() { return total; }
        public MetricChange getPassed() { return passed; }
        public MetricChange getFailed() { return failed; }
        public DurationChange getDuration() { return duration; }
    }
    
    public static class TestStatusChange {
        private final String testName;
        private final boolean previousPassed;
        private final boolean currentPassed;
        private final boolean previousSkipped;
        private final boolean currentSkipped;
        private final String previousError;
        private final String currentError;
        
        TestStatusChange(String testName,
                        boolean previousPassed, boolean currentPassed,
                        boolean previousSkipped, boolean currentSkipped,
                        String previousError, String currentError) {
            this.testName = testName;
            this.previousPassed = previousPassed;
            this.currentPassed = currentPassed;
            this.previousSkipped = previousSkipped;
            this.currentSkipped = currentSkipped;
            this.previousError = previousError;
            this.currentError = currentError;
        }
        
        public String getTestName() { return testName; }
        public boolean getPreviousPassed() { return previousPassed; }
        public boolean getCurrentPassed() { return currentPassed; }
        public boolean getPreviousSkipped() { return previousSkipped; }
        public boolean getCurrentSkipped() { return currentSkipped; }
        public String getPreviousError() { return previousError; }
        public String getCurrentError() { return currentError; }
    }
    
    public static class DurationChange {
        private final long previous;
        private final long current;
        private final long difference;
        private final double percentageChange;
        
        DurationChange(long previous, long current) {
            this.previous = previous;
            this.current = current;
            this.difference = current - previous;
            this.percentageChange = previous == 0 ? 0 :
                ((double) difference / previous) * 100;
        }
        
        public long getPrevious() { return previous; }
        public long getCurrent() { return current; }
        public long getDifference() { return difference; }
        public double getPercentageChange() { return percentageChange; }
    }
    
    public static class EnvironmentChange {
        private final String previous;
        private final String current;
        
        EnvironmentChange(String previous, String current) {
            this.previous = previous;
            this.current = current;
        }
        
        public String getPrevious() { return previous; }
        public String getCurrent() { return current; }
    }
}
