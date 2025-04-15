package com.elvecha.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CriteriaTest {
    private Criteria criteria;
    private static final String TEST_NAME = "Test Criteria";
    private static final double TEST_WEIGHT = 0.35;
    private static final String TEST_TYPE = "Benefit";

    @Before
    public void setUp() {
        criteria = new Criteria(TEST_NAME, TEST_WEIGHT, TEST_TYPE);
    }

    @Test
    public void testConstructor() {
        assertNotNull("Criteria object should be created", criteria);
        assertEquals("Name should match", TEST_NAME, criteria.getName());
        assertEquals("Weight should match", TEST_WEIGHT, criteria.getWeight(), 0.001);
        assertEquals("Type should match", TEST_TYPE, criteria.getType());
    }

    @Test
    public void testSetName() {
        String newName = "New Criteria";
        criteria.setName(newName);
        assertEquals("Name should be updated", newName, criteria.getName());
    }

    @Test
    public void testSetWeight() {
        double newWeight = 0.5;
        criteria.setWeight(newWeight);
        assertEquals("Weight should be updated", newWeight, criteria.getWeight(), 0.001);
    }

    @Test
    public void testSetType() {
        String newType = "Cost";
        criteria.setType(newType);
        assertEquals("Type should be updated", newType, criteria.getType());
    }

    @Test
    public void testToString() {
        String expected = "Criteria{" +
                "name='" + TEST_NAME + '\'' +
                ", weight=" + TEST_WEIGHT +
                ", type='" + TEST_TYPE + '\'' +
                '}';
        assertEquals("toString should match expected format", expected, criteria.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeWeight() {
        new Criteria(TEST_NAME, -0.1, TEST_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeightGreaterThanOne() {
        new Criteria(TEST_NAME, 1.1, TEST_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullName() {
        new Criteria(null, TEST_WEIGHT, TEST_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyName() {
        new Criteria("", TEST_WEIGHT, TEST_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullType() {
        new Criteria(TEST_NAME, TEST_WEIGHT, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidType() {
        new Criteria(TEST_NAME, TEST_WEIGHT, "Invalid");
    }

    @Test
    public void testValidTypes() {
        // Test "Benefit" type
        Criteria benefitCriteria = new Criteria(TEST_NAME, TEST_WEIGHT, "Benefit");
        assertEquals("Type should be Benefit", "Benefit", benefitCriteria.getType());

        // Test "Cost" type
        Criteria costCriteria = new Criteria(TEST_NAME, TEST_WEIGHT, "Cost");
        assertEquals("Type should be Cost", "Cost", costCriteria.getType());
    }

    @Test
    public void testWeightBoundaries() {
        // Test minimum weight (0)
        Criteria minWeight = new Criteria(TEST_NAME, 0.0, TEST_TYPE);
        assertEquals("Weight should be 0", 0.0, minWeight.getWeight(), 0.001);

        // Test maximum weight (1)
        Criteria maxWeight = new Criteria(TEST_NAME, 1.0, TEST_TYPE);
        assertEquals("Weight should be 1", 1.0, maxWeight.getWeight(), 0.001);
    }

    @Test
    public void testNameTrimming() {
        String nameWithSpaces = "  Test Name  ";
        Criteria trimmedCriteria = new Criteria(nameWithSpaces, TEST_WEIGHT, TEST_TYPE);
        assertEquals("Name should be trimmed", "Test Name", trimmedCriteria.getName());
    }

    @Test
    public void testTypeCaseInsensitive() {
        // Test lowercase
        Criteria lowerCriteria = new Criteria(TEST_NAME, TEST_WEIGHT, "benefit");
        assertEquals("Type should be normalized", "Benefit", lowerCriteria.getType());

        // Test uppercase
        Criteria upperCriteria = new Criteria(TEST_NAME, TEST_WEIGHT, "COST");
        assertEquals("Type should be normalized", "Cost", upperCriteria.getType());
    }

    @Test
    public void testWeightPrecision() {
        double preciseWeight = 0.333333;
        criteria.setWeight(preciseWeight);
        assertEquals("Weight should maintain precision", 
            preciseWeight, criteria.getWeight(), 0.000001);
    }
}
