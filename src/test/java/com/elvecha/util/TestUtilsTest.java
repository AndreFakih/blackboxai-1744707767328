package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TestUtilsTest {
    private static final int TEST_CRITERIA_COUNT = 5;
    private static final int TEST_ALTERNATIVE_COUNT = 3;
    private static final String TEST_FILE = "test_utils_test.txt";
    private List<Criteria> criteriaList;
    private List<Alternative> alternativeList;

    @Before
    public void setUp() {
        criteriaList = TestUtils.createTestCriteria(TEST_CRITERIA_COUNT);
        alternativeList = TestUtils.createTestAlternatives(
            TEST_ALTERNATIVE_COUNT, criteriaList);
    }

    @After
    public void tearDown() {
        TestUtils.cleanupTestFiles(TEST_FILE);
    }

    @Test
    public void testCreateTestCriteria() {
        assertNotNull("Criteria list should not be null", criteriaList);
        assertEquals("Should create requested number of criteria",
            TEST_CRITERIA_COUNT, criteriaList.size());
        
        // Verify weights sum to 1.0
        double weightSum = criteriaList.stream()
            .mapToDouble(Criteria::getWeight)
            .sum();
        assertEquals("Weights should sum to 1.0", 1.0, weightSum, 0.001);
        
        // Verify alternating benefit/cost types
        for (int i = 0; i < criteriaList.size(); i++) {
            String expectedType = (i % 2 == 0) ? "Benefit" : "Cost";
            assertEquals("Type should alternate between Benefit and Cost",
                expectedType, criteriaList.get(i).getType());
        }
    }

    @Test
    public void testCreateTestAlternatives() {
        assertNotNull("Alternative list should not be null", alternativeList);
        assertEquals("Should create requested number of alternatives",
            TEST_ALTERNATIVE_COUNT, alternativeList.size());
        
        // Verify each alternative has values for all criteria
        for (Alternative alt : alternativeList) {
            Map<String, Double> values = alt.getCriteriaValues();
            assertEquals("Should have values for all criteria",
                TEST_CRITERIA_COUNT, values.size());
            
            // Verify all values are within expected ranges
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                Double value = entry.getValue();
                assertNotNull("Value should not be null", value);
                assertTrue("Value should be positive", value > 0);
            }
        }
    }

    @Test
    public void testVerifyWeightSum() {
        assertTrue("Weight sum verification should pass for valid weights",
            TestUtils.verifyWeightSum(criteriaList));
        
        // Test with invalid weights
        List<Criteria> invalidList = TestUtils.createTestCriteria(3);
        invalidList.get(0).setWeight(0.5);
        invalidList.get(1).setWeight(0.6); // Sum will be > 1.0
        assertFalse("Weight sum verification should fail for invalid weights",
            TestUtils.verifyWeightSum(invalidList));
    }

    @Test
    public void testVerifyAlternativeCompleteness() {
        assertTrue("Completeness check should pass for valid data",
            TestUtils.verifyAlternativeCompleteness(alternativeList, criteriaList));
        
        // Test with incomplete data
        Alternative incomplete = new Alternative("Incomplete");
        incomplete.setCriteriaValue(criteriaList.get(0).getName(), 1.0);
        // Don't set other criteria values
        
        List<Alternative> incompleteList = new ArrayList<>(alternativeList);
        incompleteList.add(incomplete);
        
        assertFalse("Completeness check should fail for incomplete data",
            TestUtils.verifyAlternativeCompleteness(incompleteList, criteriaList));
    }

    @Test
    public void testCleanupTestFiles() {
        // Create test file
        try {
            new File(TEST_FILE).createNewFile();
            assertTrue("Test file should exist", new File(TEST_FILE).exists());
            
            // Clean up
            TestUtils.cleanupTestFiles(TEST_FILE);
            assertFalse("Test file should be deleted", new File(TEST_FILE).exists());
        } catch (Exception e) {
            fail("File operations failed: " + e.getMessage());
        }
    }

    @Test
    public void testFormatDouble() {
        double testValue = 1.23456789;
        double formatted = TestUtils.formatDouble(testValue);
        assertEquals("Should format to 3 decimal places",
            1.235, formatted, 0.0001);
    }

    @Test
    public void testCopyCriteriaList() {
        List<Criteria> copy = TestUtils.copyCriteriaList(criteriaList);
        
        assertEquals("Copy should have same size", 
            criteriaList.size(), copy.size());
        
        // Verify deep copy
        for (int i = 0; i < criteriaList.size(); i++) {
            Criteria original = criteriaList.get(i);
            Criteria copied = copy.get(i);
            
            assertEquals("Names should match", 
                original.getName(), copied.getName());
            assertEquals("Weights should match",
                original.getWeight(), copied.getWeight(), 0.001);
            assertEquals("Types should match",
                original.getType(), copied.getType());
            
            // Verify it's a deep copy
            assertNotSame("Should be different objects", original, copied);
        }
    }

    @Test
    public void testCopyAlternativeList() {
        List<Alternative> copy = TestUtils.copyAlternativeList(alternativeList);
        
        assertEquals("Copy should have same size",
            alternativeList.size(), copy.size());
        
        // Verify deep copy
        for (int i = 0; i < alternativeList.size(); i++) {
            Alternative original = alternativeList.get(i);
            Alternative copied = copy.get(i);
            
            assertEquals("Names should match",
                original.getName(), copied.getName());
            assertEquals("Final scores should match",
                original.getFinalScore(), copied.getFinalScore(), 0.001);
            
            // Verify criteria values
            Map<String, Double> originalValues = original.getCriteriaValues();
            Map<String, Double> copiedValues = copied.getCriteriaValues();
            
            assertEquals("Should have same number of criteria values",
                originalValues.size(), copiedValues.size());
                
            for (String key : originalValues.keySet()) {
                assertEquals("Criteria values should match",
                    originalValues.get(key), copiedValues.get(key), 0.001);
            }
            
            // Verify it's a deep copy
            assertNotSame("Should be different objects", original, copied);
        }
    }

    @Test
    public void testVerifyRankingOrder() {
        // Create sorted list
        List<Alternative> sorted = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Alternative alt = new Alternative("Alt" + i);
            alt.setFinalScore(1.0 - (i * 0.1)); // Descending scores
            sorted.add(alt);
        }
        
        assertTrue("Should verify correct ranking order",
            TestUtils.verifyRankingOrder(sorted));
            
        // Create unsorted list
        List<Alternative> unsorted = new ArrayList<>();
        unsorted.add(sorted.get(2)); // Lowest score first
        unsorted.add(sorted.get(0)); // Highest score second
        unsorted.add(sorted.get(1)); // Medium score last
        
        assertFalse("Should fail for incorrect ranking order",
            TestUtils.verifyRankingOrder(unsorted));
    }
}
