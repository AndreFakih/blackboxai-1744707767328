package com.elvecha.integration;

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

public class SystemIntegrationTest {
    private List<Criteria> criteriaList;
    private List<Alternative> alternativeList;
    private SAWCalculator calculator;
    private static final String TEST_PDF = "test_integration.pdf";

    @Before
    public void setUp() {
        calculator = new SAWCalculator();
        setupTestData();
    }

    @After
    public void tearDown() {
        // Clean up test files
        File testFile = new File(TEST_PDF);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    private void setupTestData() {
        // Create criteria
        criteriaList = new ArrayList<>();
        criteriaList.add(new Criteria("Harga", 0.35, "Cost"));
        criteriaList.add(new Criteria("Vendor", 0.25, "Benefit"));
        criteriaList.add(new Criteria("Rating", 0.40, "Benefit"));

        // Verify criteria weights sum to 1.0
        double weightSum = criteriaList.stream()
            .mapToDouble(Criteria::getWeight)
            .sum();
        assertEquals("Criteria weights must sum to 1.0", 1.0, weightSum, 0.001);

        // Create alternatives
        alternativeList = new ArrayList<>();

        Alternative alt1 = new Alternative("WO Premium");
        alt1.setCriteriaValue("Harga", 75000000.0);
        alt1.setCriteriaValue("Vendor", 8.0);
        alt1.setCriteriaValue("Rating", 4.8);
        alternativeList.add(alt1);

        Alternative alt2 = new Alternative("WO Standard");
        alt2.setCriteriaValue("Harga", 50000000.0);
        alt2.setCriteriaValue("Vendor", 6.0);
        alt2.setCriteriaValue("Rating", 4.2);
        alternativeList.add(alt2);

        Alternative alt3 = new Alternative("WO Budget");
        alt3.setCriteriaValue("Harga", 35000000.0);
        alt3.setCriteriaValue("Vendor", 4.0);
        alt3.setCriteriaValue("Rating", 3.8);
        alternativeList.add(alt3);
    }

    @Test
    public void testCompleteWorkflow() {
        try {
            // Step 1: Calculate rankings
            List<Alternative> rankedAlternatives = calculator.calculate(criteriaList, alternativeList);
            
            // Verify calculation results
            assertNotNull("Ranked alternatives should not be null", rankedAlternatives);
            assertEquals("Should have same number of alternatives", 
                alternativeList.size(), rankedAlternatives.size());
            
            // Verify alternatives are sorted by score
            for (int i = 0; i < rankedAlternatives.size() - 1; i++) {
                assertTrue("Alternatives should be sorted by score",
                    rankedAlternatives.get(i).getFinalScore() >= 
                    rankedAlternatives.get(i + 1).getFinalScore());
            }

            // Step 2: Export results to PDF
            PDFExporter.exportResults(TEST_PDF, rankedAlternatives, criteriaList);
            
            // Verify PDF was created
            File pdfFile = new File(TEST_PDF);
            assertTrue("PDF file should be created", pdfFile.exists());
            assertTrue("PDF file should not be empty", pdfFile.length() > 0);

            // Step 3: Verify best alternative meets expectations
            Alternative bestAlternative = rankedAlternatives.get(0);
            assertNotNull("Best alternative should not be null", bestAlternative);
            
            // Verify best alternative has appropriate values
            assertTrue("Best alternative should have valid score",
                bestAlternative.getFinalScore() > 0 && 
                bestAlternative.getFinalScore() <= 1);

            // Step 4: Verify worst alternative
            Alternative worstAlternative = rankedAlternatives.get(rankedAlternatives.size() - 1);
            assertTrue("Worst alternative should have lower score than best",
                worstAlternative.getFinalScore() < bestAlternative.getFinalScore());

            // Step 5: Verify all alternatives have valid scores
            for (Alternative alt : rankedAlternatives) {
                assertTrue("All scores should be between 0 and 1",
                    alt.getFinalScore() >= 0 && alt.getFinalScore() <= 1);
                
                // Verify all criteria values are present
                for (Criteria criteria : criteriaList) {
                    assertNotNull("All criteria should have values",
                        alt.getCriteriaValue(criteria.getName()));
                }
            }

        } catch (Exception e) {
            fail("Integration test failed: " + e.getMessage());
        }
    }

    @Test
    public void testDataConsistency() {
        // Calculate rankings multiple times
        List<Alternative> firstRun = calculator.calculate(criteriaList, alternativeList);
        List<Alternative> secondRun = calculator.calculate(criteriaList, alternativeList);

        // Verify results are consistent
        assertEquals("Number of alternatives should be consistent",
            firstRun.size(), secondRun.size());

        for (int i = 0; i < firstRun.size(); i++) {
            assertEquals("Scores should be consistent",
                firstRun.get(i).getFinalScore(),
                secondRun.get(i).getFinalScore(),
                0.001);
        }
    }

    @Test
    public void testEdgeCases() {
        // Test with extreme values
        Alternative extremeCase = new Alternative("Extreme WO");
        extremeCase.setCriteriaValue("Harga", 1000000000.0); // Very high price
        extremeCase.setCriteriaValue("Vendor", 20.0);        // Very high vendor count
        extremeCase.setCriteriaValue("Rating", 5.0);         // Perfect rating
        alternativeList.add(extremeCase);

        List<Alternative> results = calculator.calculate(criteriaList, alternativeList);
        
        // Verify system handles extreme values
        assertTrue("System should handle extreme values",
            results.stream()
                  .allMatch(alt -> alt.getFinalScore() >= 0 && 
                                 alt.getFinalScore() <= 1));
    }

    @Test
    public void testCriteriaWeightSensitivity() {
        // Store original results
        List<Alternative> originalResults = calculator.calculate(criteriaList, alternativeList);
        
        // Modify weights slightly
        criteriaList.get(0).setWeight(0.34); // Changed from 0.35
        criteriaList.get(1).setWeight(0.26); // Changed from 0.25
        criteriaList.get(2).setWeight(0.40); // Unchanged
        
        List<Alternative> newResults = calculator.calculate(criteriaList, alternativeList);
        
        // Verify small weight changes don't cause dramatic ranking changes
        for (int i = 0; i < originalResults.size(); i++) {
            double scoreDiff = Math.abs(
                originalResults.get(i).getFinalScore() - 
                newResults.get(i).getFinalScore()
            );
            assertTrue("Score change should be proportional to weight change",
                scoreDiff < 0.1);
        }
    }
}
