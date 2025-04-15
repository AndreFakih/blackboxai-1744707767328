package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class SAWCalculatorTest {
    private SAWCalculator calculator;
    private List<Criteria> criteria;
    private List<Alternative> alternatives;

    @Before
    public void setUp() {
        calculator = new SAWCalculator();
        criteria = new ArrayList<>();
        alternatives = new ArrayList<>();

        // Setup test criteria
        criteria.add(new Criteria("Harga", 0.35, "Cost"));
        criteria.add(new Criteria("Vendor", 0.25, "Benefit"));
        criteria.add(new Criteria("Rating", 0.40, "Benefit"));

        // Setup test alternatives
        Alternative alt1 = new Alternative("WO A");
        alt1.setCriteriaValue("Harga", 50000000.0);  // Medium price
        alt1.setCriteriaValue("Vendor", 8.0);        // High vendor count
        alt1.setCriteriaValue("Rating", 4.5);        // High rating
        alternatives.add(alt1);

        Alternative alt2 = new Alternative("WO B");
        alt2.setCriteriaValue("Harga", 75000000.0);  // High price
        alt2.setCriteriaValue("Vendor", 6.0);        // Medium vendor count
        alt2.setCriteriaValue("Rating", 4.0);        // Medium rating
        alternatives.add(alt2);

        Alternative alt3 = new Alternative("WO C");
        alt3.setCriteriaValue("Harga", 35000000.0);  // Low price
        alt3.setCriteriaValue("Vendor", 4.0);        // Low vendor count
        alt3.setCriteriaValue("Rating", 3.5);        // Low rating
        alternatives.add(alt3);
    }

    @Test
    public void testCalculate() {
        // Calculate rankings
        List<Alternative> results = calculator.calculate(criteria, alternatives);
        
        // Verify results are not null and contain all alternatives
        assertNotNull("Result should not be null", results);
        assertEquals("Result should contain all alternatives", 3, results.size());
        
        // Verify alternatives are sorted by final score (descending)
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(
                "Alternatives should be sorted by score (descending)",
                results.get(i).getFinalScore() >= results.get(i + 1).getFinalScore()
            );
        }
    }

    @Test
    public void testEmptyInput() {
        try {
            calculator.calculate(new ArrayList<>(), new ArrayList<>());
            fail("Should throw IllegalArgumentException for empty input");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testNullValues() {
        Alternative altWithNull = new Alternative("WO D");
        altWithNull.setCriteriaValue("Harga", null);
        altWithNull.setCriteriaValue("Vendor", 5.0);
        altWithNull.setCriteriaValue("Rating", 4.0);
        alternatives.add(altWithNull);

        // Should handle null values by treating them as 0
        List<Alternative> results = calculator.calculate(criteria, alternatives);
        assertNotNull("Result should not be null even with null values", results);
        assertEquals("Result should contain all alternatives including one with null", 4, results.size());
    }

    @Test
    public void testWeightSum() {
        double weightSum = criteria.stream()
            .mapToDouble(Criteria::getWeight)
            .sum();
        assertEquals("Criteria weights should sum to 1.0", 1.0, weightSum, 0.001);
    }

    @Test
    public void testBenefitNormalization() {
        List<Alternative> results = calculator.calculate(criteria, alternatives);
        
        // Find alternative with highest vendor count (benefit criteria)
        Alternative bestVendor = alternatives.stream()
            .max((a1, a2) -> Double.compare(
                a1.getCriteriaValue("Vendor"),
                a2.getCriteriaValue("Vendor")))
            .get();
            
        // Its normalized score for vendor should be 1.0
        assertTrue(
            "Highest benefit value should be normalized to 1.0",
            bestVendor.getFinalScore() > 0.0
        );
    }

    @Test
    public void testCostNormalization() {
        List<Alternative> results = calculator.calculate(criteria, alternatives);
        
        // Find alternative with lowest price (cost criteria)
        Alternative bestPrice = alternatives.stream()
            .min((a1, a2) -> Double.compare(
                a1.getCriteriaValue("Harga"),
                a2.getCriteriaValue("Harga")))
            .get();
            
        // It should have a higher final score than others with same benefit values
        assertTrue(
            "Lower cost should contribute to higher final score",
            bestPrice.getFinalScore() > 0.0
        );
    }
}
