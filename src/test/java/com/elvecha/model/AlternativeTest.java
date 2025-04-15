package com.elvecha.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class AlternativeTest {
    private Alternative alternative;
    private static final String TEST_NAME = "Test WO";
    private static final String TEST_CRITERIA = "Test Criteria";
    private static final Double TEST_VALUE = 100.0;

    @Before
    public void setUp() {
        alternative = new Alternative(TEST_NAME);
    }

    @Test
    public void testConstructor() {
        assertNotNull("Alternative object should be created", alternative);
        assertEquals("Name should match", TEST_NAME, alternative.getName());
        assertNotNull("Criteria values map should be initialized", 
            alternative.getCriteriaValues());
        assertEquals("Initial final score should be 0", 
            0.0, alternative.getFinalScore(), 0.001);
    }

    @Test
    public void testSetName() {
        String newName = "New WO";
        alternative.setName(newName);
        assertEquals("Name should be updated", newName, alternative.getName());
    }

    @Test
    public void testSetAndGetCriteriaValue() {
        alternative.setCriteriaValue(TEST_CRITERIA, TEST_VALUE);
        assertEquals("Criteria value should match", 
            TEST_VALUE, alternative.getCriteriaValue(TEST_CRITERIA));
    }

    @Test
    public void testGetNonExistentCriteriaValue() {
        assertNull("Non-existent criteria should return null",
            alternative.getCriteriaValue("NonExistent"));
    }

    @Test
    public void testSetAndGetFinalScore() {
        double newScore = 0.85;
        alternative.setFinalScore(newScore);
        assertEquals("Final score should be updated", 
            newScore, alternative.getFinalScore(), 0.001);
    }

    @Test
    public void testGetCriteriaValues() {
        alternative.setCriteriaValue("Criteria1", 1.0);
        alternative.setCriteriaValue("Criteria2", 2.0);
        
        Map<String, Double> values = alternative.getCriteriaValues();
        assertNotNull("Criteria values map should not be null", values);
        assertEquals("Map should contain correct number of entries", 
            2, values.size());
        assertEquals("Value should match", 1.0, values.get("Criteria1"), 0.001);
        assertEquals("Value should match", 2.0, values.get("Criteria2"), 0.001);
    }

    @Test
    public void testToString() {
        alternative.setCriteriaValue(TEST_CRITERIA, TEST_VALUE);
        alternative.setFinalScore(0.75);
        
        String expected = "Alternative{" +
                "name='" + TEST_NAME + '\'' +
                ", criteriaValues=" + alternative.getCriteriaValues() +
                ", finalScore=" + alternative.getFinalScore() +
                '}';
        assertEquals("toString should match expected format", 
            expected, alternative.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullName() {
        new Alternative(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyName() {
        new Alternative("");
    }

    @Test
    public void testSetNullCriteriaValue() {
        alternative.setCriteriaValue(TEST_CRITERIA, null);
        assertNull("Null value should be allowed for criteria",
            alternative.getCriteriaValue(TEST_CRITERIA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullCriteriaName() {
        alternative.setCriteriaValue(null, TEST_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyCriteriaName() {
        alternative.setCriteriaValue("", TEST_VALUE);
    }

    @Test
    public void testMultipleCriteriaValues() {
        // Add multiple criteria values
        alternative.setCriteriaValue("Price", 50000.0);
        alternative.setCriteriaValue("Quality", 4.5);
        alternative.setCriteriaValue("Distance", 10.0);
        
        Map<String, Double> values = alternative.getCriteriaValues();
        assertEquals("Should contain all criteria values", 3, values.size());
        assertEquals("Price should match", 50000.0, values.get("Price"), 0.001);
        assertEquals("Quality should match", 4.5, values.get("Quality"), 0.001);
        assertEquals("Distance should match", 10.0, values.get("Distance"), 0.001);
    }

    @Test
    public void testUpdateExistingCriteriaValue() {
        alternative.setCriteriaValue(TEST_CRITERIA, TEST_VALUE);
        Double newValue = 200.0;
        alternative.setCriteriaValue(TEST_CRITERIA, newValue);
        
        assertEquals("Criteria value should be updated",
            newValue, alternative.getCriteriaValue(TEST_CRITERIA));
    }

    @Test
    public void testNameTrimming() {
        String nameWithSpaces = "  Test Name  ";
        Alternative trimmedAlternative = new Alternative(nameWithSpaces);
        assertEquals("Name should be trimmed", "Test Name", trimmedAlternative.getName());
    }

    @Test
    public void testCriteriaNameTrimming() {
        String criteriaWithSpaces = "  Test Criteria  ";
        alternative.setCriteriaValue(criteriaWithSpaces, TEST_VALUE);
        assertEquals("Should find value with trimmed criteria name",
            TEST_VALUE, alternative.getCriteriaValue("Test Criteria"));
    }

    @Test
    public void testFinalScorePrecision() {
        double preciseScore = 0.333333;
        alternative.setFinalScore(preciseScore);
        assertEquals("Final score should maintain precision",
            preciseScore, alternative.getFinalScore(), 0.000001);
    }

    @Test
    public void testCriteriaValueModification() {
        alternative.setCriteriaValue(TEST_CRITERIA, TEST_VALUE);
        Map<String, Double> values = alternative.getCriteriaValues();
        values.put(TEST_CRITERIA, 200.0); // Modify the returned map
        
        // Original value should remain unchanged
        assertEquals("Original value should not be affected by map modification",
            TEST_VALUE, alternative.getCriteriaValue(TEST_CRITERIA));
    }
}
