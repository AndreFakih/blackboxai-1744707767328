package com.elvecha.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;

public class TestReportAnalyzerTest {
    private TestReportAnalyzer analyzer;
    
    @Before
    public void setUp() {
        analyzer = new TestReportAnalyzer();
    }
    
    @Test
    public void testBasicAnalysis() {
        // Create test results with known metrics
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addCategory("model", 10, 8, 2, 1500)
            .addPassedTest("Test1", 1000)
            .addFailedTest("Test2", "AssertionError: expected true", 2000)
            .addSkippedTest("Test3")
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        
        // Verify metrics
        assertEquals("Should calculate correct pass rate", 0.333,
            analysis.getMetrics().get("passRate"), 0.001);
        assertEquals("Should calculate correct skip rate", 0.333,
            analysis.getMetrics().get("skipRate"), 0.001);
        
        // Verify issues
        assertTrue("Should detect low pass rate",
            analysis.getIssues().stream()
                .anyMatch(i -> i.getTitle().contains("Low Pass Rate")));
    }
    
    @Test
    public void testCategoryAnalysis() {
        // Create test results with category patterns
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addCategory("stable", 10, 9, 1, 1500) // 90% pass rate
            .addCategory("unstable", 10, 6, 4, 1500) // 60% pass rate
            .addCategory("slow", 10, 8, 2, 25000) // High duration
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        
        // Verify category metrics
        Map<String, TestReportAnalyzer.CategoryMetrics> metrics = 
            analysis.getCategoryMetrics();
            
        assertEquals("Should detect stable category pass rate", 0.9,
            metrics.get("stable").getPassRate(), 0.001);
        assertEquals("Should detect unstable category pass rate", 0.6,
            metrics.get("unstable").getPassRate(), 0.001);
        
        // Verify issues
        assertTrue("Should detect unstable category",
            analysis.getIssues().stream()
                .anyMatch(i -> i.getTitle().equals("Category Issues") &&
                             i.getDescription().contains("unstable")));
        assertTrue("Should detect slow category",
            analysis.getIssues().stream()
                .anyMatch(i -> i.getTitle().equals("Performance Issues") &&
                             i.getDescription().contains("slow")));
    }
    
    @Test
    public void testErrorPatternAnalysis() {
        // Create test results with error patterns
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addFailedTest("Test1", "AssertionError: expected true", 1000)
            .addFailedTest("Test2", "AssertionError: expected false", 1000)
            .addFailedTest("Test3", "NullPointerException", 1000)
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        
        // Verify error patterns
        Map<String, Integer> patterns = analysis.getErrorPatterns();
        assertEquals("Should detect assertion errors", 2,
            (int) patterns.get("Assertion Failure"));
        assertEquals("Should detect null pointer", 1,
            (int) patterns.get("Null Pointer"));
        
        // Verify recommendations
        assertTrue("Should recommend addressing error patterns",
            analysis.getRecommendations().stream()
                .anyMatch(r -> r.contains("error patterns")));
    }
    
    @Test
    public void testPerformanceAnalysis() {
        // Create test results with performance patterns
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addPassedTest("Fast1", 500)
            .addPassedTest("Fast2", 600)
            .addPassedTest("Slow1", 6000)
            .addPassedTest("Slow2", 7000)
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        
        // Verify long tests
        List<String> longTests = analysis.getLongTests();
        assertEquals("Should detect two long tests", 2, longTests.size());
        assertTrue("Should identify correct long tests",
            longTests.contains("Slow1") && longTests.contains("Slow2"));
        
        // Verify duration distribution
        Map<String, Double> distribution = analysis.getDurationDistribution();
        assertEquals("Should calculate correct median", 3300.0,
            distribution.get("median"), 0.1);
        assertEquals("Should calculate correct p90", 6000.0,
            distribution.get("p90"), 0.1);
    }
    
    @Test
    public void testRecommendations() {
        // Create test results that trigger multiple recommendations
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addCategory("unstable", 10, 5, 5, 1500)
            .addPassedTest("Fast", 500)
            .addFailedTest("Slow", "Timeout", 6000)
            .addSkippedTest("Skip1")
            .addSkippedTest("Skip2")
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        
        List<String> recommendations = analysis.getRecommendations();
        assertTrue("Should recommend improving stability",
            recommendations.stream()
                .anyMatch(r -> r.contains("stability")));
        assertTrue("Should recommend reviewing skipped tests",
            recommendations.stream()
                .anyMatch(r -> r.contains("skipped tests")));
        assertTrue("Should recommend optimizing performance",
            recommendations.stream()
                .anyMatch(r -> r.contains("long-running tests")));
    }
    
    @Test
    public void testLargeResultsAnalysis() {
        // Create large test results
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .withRandomTestData(1000, 50)
            .build();
            
        // Measure performance
        long startTime = System.currentTimeMillis();
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue("Analysis should complete in reasonable time",
            duration < 5000);
        assertNotNull("Should generate analysis for large results",
            analysis);
    }
    
    @Test
    public void testEdgeCases() {
        // Test with empty results
        TestReportGenerator.TestResults emptyResults = TestReportBuilder.create()
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(emptyResults);
        assertTrue("Should handle empty results",
            analysis.getIssues().isEmpty());
            
        // Test with null error messages
        TestReportGenerator.TestResults nullErrors = TestReportBuilder.create()
            .addFailedTest("Test1", null, 1000)
            .build();
            
        analysis = analyzer.analyze(nullErrors);
        assertEquals("Should categorize null errors as Unknown", 1,
            (int) analysis.getErrorPatterns().get("Unknown"));
    }
    
    @Test
    public void testStatisticalAnalysis() {
        // Create results with known statistical properties
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addPassedTest("Test1", 1000)
            .addPassedTest("Test2", 2000)
            .addPassedTest("Test3", 3000)
            .addPassedTest("Test4", 4000)
            .addPassedTest("Test5", 5000)
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        
        Map<String, Double> distribution = analysis.getDurationDistribution();
        assertEquals("Should calculate correct min", 1000.0,
            distribution.get("min"), 0.1);
        assertEquals("Should calculate correct max", 5000.0,
            distribution.get("max"), 0.1);
        assertEquals("Should calculate correct median", 3000.0,
            distribution.get("median"), 0.1);
    }
    
    @Test
    public void testCategoryTrends() {
        // Create results with category trends
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addCategory("improving", 10, 9, 1, 1500)
            .addCategory("degrading", 10, 6, 4, 2500)
            .addCategory("stable", 10, 8, 2, 1500)
            .build();
            
        TestReportAnalyzer.AnalysisResult analysis = analyzer.analyze(results);
        
        // Verify category analysis
        Map<String, TestReportAnalyzer.CategoryMetrics> metrics = 
            analysis.getCategoryMetrics();
            
        assertTrue("Should identify good categories",
            metrics.get("improving").getPassRate() >= 0.9);
        assertTrue("Should identify problematic categories",
            metrics.get("degrading").getPassRate() < 0.8);
    }
}
