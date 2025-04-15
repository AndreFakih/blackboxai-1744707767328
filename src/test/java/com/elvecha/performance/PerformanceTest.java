package com.elvecha.performance;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import com.elvecha.util.SAWCalculator;
import com.elvecha.util.PDFExporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PerformanceTest {
    private SAWCalculator calculator;
    private List<Criteria> criteriaList;
    private List<Alternative> alternativeList;
    private static final String TEST_PDF = "test_performance.pdf";
    private static final int LARGE_DATASET_SIZE = 1000;
    private static final int CRITERIA_COUNT = 10;
    private static final long MAX_CALCULATION_TIME = 1000; // 1 second
    private static final long MAX_PDF_GENERATION_TIME = 5000; // 5 seconds
    private static final int STRESS_TEST_ITERATIONS = 100;

    @Before
    public void setUp() {
        calculator = new SAWCalculator();
        setupTestData();
    }

    @After
    public void tearDown() {
        File testFile = new File(TEST_PDF);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    private void setupTestData() {
        // Create criteria
        criteriaList = new ArrayList<>();
        double weightPerCriteria = 1.0 / CRITERIA_COUNT;
        
        for (int i = 0; i < CRITERIA_COUNT; i++) {
            String type = (i % 2 == 0) ? "Benefit" : "Cost";
            criteriaList.add(new Criteria(
                "Criteria" + i,
                weightPerCriteria,
                type
            ));
        }

        // Create large dataset of alternatives
        alternativeList = new ArrayList<>();
        for (int i = 0; i < LARGE_DATASET_SIZE; i++) {
            Alternative alt = new Alternative("Alternative" + i);
            for (Criteria criteria : criteriaList) {
                // Generate random values between 1 and 100
                double value = 1 + Math.random() * 99;
                alt.setCriteriaValue(criteria.getName(), value);
            }
            alternativeList.add(alt);
        }
    }

    @Test
    public void testCalculationPerformance() {
        long startTime = System.nanoTime();
        
        List<Alternative> results = calculator.calculate(criteriaList, alternativeList);
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        assertNotNull("Results should not be null", results);
        assertEquals("All alternatives should be processed",
            LARGE_DATASET_SIZE, results.size());
        assertTrue("Calculation should complete within time limit",
            duration <= MAX_CALCULATION_TIME);
        
        System.out.println("Calculation time for " + LARGE_DATASET_SIZE + 
            " alternatives: " + duration + "ms");
    }

    @Test
    public void testPDFGenerationPerformance() {
        try {
            // Calculate rankings first
            List<Alternative> rankedAlternatives = 
                calculator.calculate(criteriaList, alternativeList);
            
            long startTime = System.nanoTime();
            
            PDFExporter.exportResults(TEST_PDF, rankedAlternatives, criteriaList);
            
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            
            File pdfFile = new File(TEST_PDF);
            assertTrue("PDF file should be created", pdfFile.exists());
            assertTrue("PDF generation should complete within time limit",
                duration <= MAX_PDF_GENERATION_TIME);
            
            System.out.println("PDF generation time for " + LARGE_DATASET_SIZE + 
                " alternatives: " + duration + "ms");
            
        } catch (Exception e) {
            fail("PDF generation failed: " + e.getMessage());
        }
    }

    @Test
    public void testMemoryUsage() {
        long initialMemory = Runtime.getRuntime().totalMemory() - 
                           Runtime.getRuntime().freeMemory();
        
        // Perform calculation
        List<Alternative> results = calculator.calculate(criteriaList, alternativeList);
        
        long finalMemory = Runtime.getRuntime().totalMemory() - 
                          Runtime.getRuntime().freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.println("Memory used: " + (memoryUsed / 1024 / 1024) + "MB");
        
        // Memory usage should be reasonable (less than 100MB for this dataset)
        assertTrue("Memory usage should be reasonable",
            memoryUsed < 100 * 1024 * 1024);
    }

    @Test
    public void testStressCalculation() {
        long totalDuration = 0;
        long maxDuration = 0;
        
        for (int i = 0; i < STRESS_TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            
            calculator.calculate(criteriaList, alternativeList);
            
            long duration = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - startTime);
            
            totalDuration += duration;
            maxDuration = Math.max(maxDuration, duration);
        }
        
        long averageDuration = totalDuration / STRESS_TEST_ITERATIONS;
        
        System.out.println("Stress test results:");
        System.out.println("Average calculation time: " + averageDuration + "ms");
        System.out.println("Maximum calculation time: " + maxDuration + "ms");
        
        assertTrue("Average calculation time should be reasonable",
            averageDuration <= MAX_CALCULATION_TIME);
    }

    @Test
    public void testConcurrentCalculations() {
        int threadCount = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[threadCount];
        boolean[] success = new boolean[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    calculator.calculate(criteriaList, alternativeList);
                    success[threadIndex] = true;
                } catch (Exception e) {
                    success[threadIndex] = false;
                }
            });
        }
        
        long startTime = System.nanoTime();
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail("Thread interrupted: " + e.getMessage());
            }
        }
        
        long duration = TimeUnit.NANOSECONDS.toMillis(
            System.nanoTime() - startTime);
        
        System.out.println("Concurrent calculation time with " + 
            threadCount + " threads: " + duration + "ms");
        
        // Verify all calculations completed successfully
        for (boolean succeeded : success) {
            assertTrue("All concurrent calculations should succeed", succeeded);
        }
    }
}
