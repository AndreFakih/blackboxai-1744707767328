package com.elvecha.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;

public class TestReportComparatorTest {
    private TestReportComparator comparator;
    
    @Before
    public void setUp() {
        comparator = new TestReportComparator();
    }
    
    @Test
    public void testBasicComparison() {
        // Create previous results
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .withTimestamp(1000L)
            .addCategory("model", 10, 8, 2, 1500)
            .addPassedTest("Test1", 1000)
            .addFailedTest("Test2", "Error1", 2000)
            .build();
            
        // Create current results with changes
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .withTimestamp(2000L)
            .addCategory("model", 10, 7, 3, 1600)
            .addFailedTest("Test1", "New Error", 1200) // Status changed
            .addPassedTest("Test2", 1800) // Status changed
            .build();
            
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        assertTrue("Should detect significant changes", result.hasSignificantChanges());
        assertEquals("Should detect category changes", 1, 
            result.getCategoryChanges().size());
        assertEquals("Should detect status changes", 2, 
            result.getTestStatusChanges().size());
    }
    
    @Test
    public void testCategoryChanges() {
        // Create previous results
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .addCategory("model", 10, 8, 2, 1500)
            .addCategory("util", 15, 15, 0, 2000)
            .build();
            
        // Create current results with category changes
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .addCategory("model", 12, 10, 2, 1600)
            .addCategory("ui", 5, 4, 1, 1000) // New category
            .build();
            
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        assertEquals("Should detect new category", 1, 
            result.getNewCategories().size());
        assertEquals("Should detect removed category", 1, 
            result.getRemovedCategories().size());
        assertTrue("Should detect category metric changes", 
            result.getCategoryChanges().get(0).getTotal().getDifference() == 2);
    }
    
    @Test
    public void testTestStatusChanges() {
        // Create previous results
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .addPassedTest("Test1", 1000)
            .addFailedTest("Test2", "Error", 2000)
            .addSkippedTest("Test3")
            .build();
            
        // Create current results with status changes
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .addFailedTest("Test1", "New Error", 1200)
            .addPassedTest("Test2", 1800)
            .addPassedTest("Test3", 1000)
            .addPassedTest("Test4", 1000) // New test
            .build();
            
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        assertEquals("Should detect status changes", 3, 
            result.getTestStatusChanges().size());
        assertEquals("Should detect new tests", 1, 
            result.getNewTests().size());
    }
    
    @Test
    public void testDurationChanges() {
        // Create previous results
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .addPassedTest("Test1", 1000)
            .addPassedTest("Test2", 2000)
            .build();
            
        // Create current results with duration changes
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .addPassedTest("Test1", 1200) // Small change
            .addPassedTest("Test2", 3000) // Significant change
            .build();
            
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        assertEquals("Should detect significant duration changes", 1, 
            result.getTestDurationChanges().size());
        assertTrue("Should detect correct duration change",
            result.getTestDurationChanges().get("Test2").getPercentageChange() > 20);
    }
    
    @Test
    public void testEnvironmentChanges() {
        // Create previous results with environment info
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .addEnvironmentInfo("Java", "1.8")
            .addEnvironmentInfo("OS", "Linux")
            .build();
            
        // Create current results with environment changes
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .addEnvironmentInfo("Java", "11") // Changed
            .addEnvironmentInfo("Memory", "2G") // New
            .build();
            
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        assertEquals("Should detect environment changes", 1, 
            result.getEnvironmentChanges().size());
        assertEquals("Should detect new environment variables", 1, 
            result.getNewEnvironmentVars().size());
        assertEquals("Should detect removed environment variables", 1, 
            result.getRemovedEnvironmentVars().size());
    }
    
    @Test
    public void testLargeComparison() {
        // Create large previous results
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .withRandomTestData(1000, 50)
            .build();
            
        // Create large current results
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .withRandomTestData(1000, 50)
            .build();
            
        // Measure performance
        long startTime = System.currentTimeMillis();
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue("Large comparison should complete in reasonable time",
            duration < 5000);
    }
    
    @Test
    public void testEmptyComparison() {
        // Create empty results
        TestReportGenerator.TestResults previous = TestReportBuilder.create().build();
        TestReportGenerator.TestResults current = TestReportBuilder.create().build();
        
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        assertFalse("Empty comparison should not have significant changes",
            result.hasSignificantChanges());
    }
    
    @Test
    public void testMetricChanges() {
        // Create results with metric changes
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .withRandomTestData(100, 5)
            .build();
            
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .withRandomTestData(150, 5)
            .build();
            
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        TestReportComparator.MetricChange totalChange = 
            result.getMetricChanges().get("total");
        assertNotNull("Should have total metric change", totalChange);
        assertEquals("Should detect correct difference", 50, 
            totalChange.getDifference());
    }
    
    @Test
    public void testEdgeCases() {
        // Test with null error messages
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .addFailedTest("Test1", null, 1000)
            .build();
            
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .addFailedTest("Test1", "New Error", 1000)
            .build();
            
        // Should not throw exception
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        assertNotNull("Should handle null error messages",
            result.getTestStatusChanges().get(0).getPreviousError());
    }
    
    @Test
    public void testCategoryMetrics() {
        // Create results with category metric changes
        TestReportGenerator.TestResults previous = TestReportBuilder.create()
            .addCategory("model", 10, 8, 2, 1500)
            .build();
            
        TestReportGenerator.TestResults current = TestReportBuilder.create()
            .addCategory("model", 15, 12, 3, 2000)
            .build();
            
        TestReportComparator.ComparisonResult result = comparator.compare(current, previous);
        
        TestReportComparator.CategoryChange change = result.getCategoryChanges().get(0);
        assertEquals("Should detect total change", 5, 
            change.getTotal().getDifference());
        assertEquals("Should detect passed change", 4, 
            change.getPassed().getDifference());
        assertEquals("Should detect failed change", 1, 
            change.getFailed().getDifference());
        assertEquals("Should detect duration change", 500, 
            change.getDuration().getDifference());
    }
}
