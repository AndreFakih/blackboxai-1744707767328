package com.elvecha.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class TestReportBuilderTest {
    
    @Test
    public void testBasicBuilder() {
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .withTimestamp(1000L)
            .addCategory("model", 10, 8, 2, 1500)
            .addPassedTest("TestA", 1000)
            .addFailedTest("TestB", "Expected true", 2000)
            .addEnvironmentInfo("Java", "1.8")
            .build();
            
        assertNotNull("Results should not be null", results);
        assertEquals("Should have correct timestamp", 1000L, results.getTimestamp());
        assertEquals("Should have one category", 1, results.getCategories().size());
        assertEquals("Should have two tests", 2, results.getTestResults().size());
        assertEquals("Should have one environment entry", 1, 
            results.getEnvironmentInfo().size());
    }
    
    @Test
    public void testComplexBuilder() {
        Map<String, String> envInfo = new HashMap<>();
        envInfo.put("OS", "Linux");
        envInfo.put("Memory", "1024MB");
        
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .withTimestamp(System.currentTimeMillis())
            .addCategory("model", 10, 8, 2, 1500)
            .addCategory("util", 15, 15, 0, 2000)
            .addPassedTest("Test1", 1000)
            .addPassedTest("Test2", 1500)
            .addFailedTest("Test3", "Error", 2000)
            .addSkippedTest("Test4")
            .addEnvironmentInfo(envInfo)
            .build();
            
        assertEquals("Should have two categories", 2, results.getCategories().size());
        assertEquals("Should have four tests", 4, results.getTestResults().size());
        assertEquals("Should have two environment entries", 2, 
            results.getEnvironmentInfo().size());
        
        // Verify test counts
        assertEquals("Should have two passed tests", 2, results.getPassedTests());
        assertEquals("Should have one failed test", 1, results.getFailedTests());
        assertEquals("Should have one skipped test", 1, results.getSkippedTests());
    }
    
    @Test
    public void testRandomDataGeneration() {
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .withRandomTestData(100, 5)
            .withStandardEnvironmentInfo()
            .build();
            
        assertEquals("Should have five categories", 5, results.getCategories().size());
        assertEquals("Should have 100 tests", 100, results.getTotalTests());
        assertTrue("Should have environment info", 
            results.getEnvironmentInfo().size() > 0);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInvalidCategory() {
        TestReportBuilder.create()
            .addCategory("invalid", -1, 0, 0, 1000)
            .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInvalidTestMetrics() {
        TestReportBuilder.create()
            .addCategory("category", 10, 8, 8, 1000) // 8 + 8 > 10
            .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testDuplicateTestNames() {
        TestReportBuilder.create()
            .addPassedTest("Test", 1000)
            .addPassedTest("Test", 2000)
            .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testFailedTestWithoutError() {
        TestReportBuilder.create()
            .addFailedTest("Test", null, 1000)
            .build();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testInvalidTestDuration() {
        TestReportBuilder.create()
            .addPassedTest("Test", -1000)
            .build();
    }
    
    @Test
    public void testStandardEnvironmentInfo() {
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .withStandardEnvironmentInfo()
            .build();
            
        Map<String, String> env = results.getEnvironmentInfo();
        assertTrue("Should have Java version", 
            env.containsKey("Java Version"));
        assertTrue("Should have OS info", 
            env.containsKey("OS"));
        assertTrue("Should have memory info", 
            env.containsKey("Memory"));
        assertTrue("Should have processor info", 
            env.containsKey("Processors"));
    }
    
    @Test
    public void testBulkAdditions() {
        List<TestReportGenerator.TestResult> testResults = Arrays.asList(
            new TestReportGenerator.TestResult("Test1", true, false, null, 1000),
            new TestReportGenerator.TestResult("Test2", true, false, null, 2000)
        );
        
        List<TestReportGenerator.TestCategory> categories = Arrays.asList(
            new TestReportGenerator.TestCategory("Cat1", 10, 8, 2, 1500),
            new TestReportGenerator.TestCategory("Cat2", 15, 15, 0, 2000)
        );
        
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .addTestResults(testResults)
            .addCategories(categories)
            .build();
            
        assertEquals("Should have all test results", 2, 
            results.getTestResults().size());
        assertEquals("Should have all categories", 2, 
            results.getCategories().size());
    }
    
    @Test
    public void testEmptyReport() {
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .withTimestamp(System.currentTimeMillis())
            .build();
            
        assertEquals("Should have no categories", 0, 
            results.getCategories().size());
        assertEquals("Should have no tests", 0, 
            results.getTestResults().size());
        assertEquals("Should have no environment info", 0, 
            results.getEnvironmentInfo().size());
    }
    
    @Test
    public void testLargeReport() {
        int numTests = 1000;
        int numCategories = 50;
        
        long startTime = System.currentTimeMillis();
        TestReportGenerator.TestResults results = TestReportBuilder.create()
            .withRandomTestData(numTests, numCategories)
            .withStandardEnvironmentInfo()
            .build();
        long endTime = System.currentTimeMillis();
        
        assertEquals("Should have correct number of categories", numCategories, 
            results.getCategories().size());
        assertEquals("Should have correct number of tests", numTests, 
            results.getTotalTests());
        
        // Performance check
        assertTrue("Building large report should be reasonably fast", 
            endTime - startTime < 5000);
    }
    
    @Test
    public void testValidationRules() {
        // Test category validation
        try {
            TestReportBuilder.create()
                .addCategory("cat", 10, 5, 6, 1000) // 5 + 6 > 10
                .build();
            fail("Should throw exception for invalid metrics");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exceeds total"));
        }
        
        // Test name validation
        try {
            TestReportBuilder.create()
                .addPassedTest("", 1000)
                .build();
            fail("Should throw exception for empty test name");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("name cannot be null"));
        }
        
        // Test environment info validation
        try {
            TestReportBuilder.create()
                .addEnvironmentInfo(null, "value")
                .build();
            fail("Should throw exception for null key");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Invalid environment info"));
        }
    }
}
