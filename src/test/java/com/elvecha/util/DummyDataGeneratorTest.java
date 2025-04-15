package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class DummyDataGeneratorTest {

    @Test
    public void testGenerateSampleCriteria() {
        List<Criteria> criteria = DummyDataGenerator.generateSampleCriteria();
        
        // Verify criteria list is not null and contains expected number of items
        assertNotNull("Criteria list should not be null", criteria);
        assertEquals("Should generate 5 criteria", 5, criteria.size());
        
        // Verify weights sum to 1.0
        double weightSum = criteria.stream()
            .mapToDouble(Criteria::getWeight)
            .sum();
        assertEquals("Criteria weights should sum to 1.0", 1.0, weightSum, 0.001);
        
        // Verify each criteria has valid properties
        for (Criteria c : criteria) {
            assertNotNull("Criteria name should not be null", c.getName());
            assertFalse("Criteria name should not be empty", c.getName().trim().isEmpty());
            
            assertTrue("Weight should be between 0 and 1",
                c.getWeight() > 0 && c.getWeight() <= 1);
            
            assertNotNull("Type should not be null", c.getType());
            assertTrue("Type should be either 'Benefit' or 'Cost'",
                c.getType().equals("Benefit") || c.getType().equals("Cost"));
        }
    }

    @Test
    public void testGenerateSampleAlternatives() {
        List<Alternative> alternatives = DummyDataGenerator.generateSampleAlternatives();
        
        // Verify alternatives list is not null and contains expected number of items
        assertNotNull("Alternatives list should not be null", alternatives);
        assertEquals("Should generate 5 alternatives", 5, alternatives.size());
        
        // Get criteria for reference
        List<Criteria> criteria = DummyDataGenerator.generateSampleCriteria();
        
        // Verify each alternative has valid properties
        for (Alternative alt : alternatives) {
            assertNotNull("Alternative name should not be null", alt.getName());
            assertFalse("Alternative name should not be empty", alt.getName().trim().isEmpty());
            
            // Verify all criteria values are present and valid
            for (Criteria c : criteria) {
                Double value = alt.getCriteriaValue(c.getName());
                assertNotNull("Criteria value should not be null", value);
                
                // Verify specific value ranges based on criteria
                switch (c.getName()) {
                    case "Harga Paket":
                        assertTrue("Harga should be between 30M and 80M",
                            value >= 30000000 && value <= 80000000);
                        break;
                    case "Jumlah Vendor":
                        assertTrue("Vendor count should be between 3 and 10",
                            value >= 3 && value <= 10);
                        break;
                    case "Pengalaman (Tahun)":
                        assertTrue("Experience should be between 1 and 15 years",
                            value >= 1 && value <= 15);
                        break;
                    case "Rating Pelanggan":
                        assertTrue("Rating should be between 1 and 5",
                            value >= 1 && value <= 5);
                        break;
                    case "Jarak Lokasi (km)":
                        assertTrue("Distance should be between 1 and 50 km",
                            value >= 1 && value <= 50);
                        break;
                }
            }
        }
    }

    @Test
    public void testLoadSampleData() {
        List<Criteria> criteriaList = DummyDataGenerator.generateSampleCriteria();
        List<Alternative> alternativeList = DummyDataGenerator.generateSampleAlternatives();
        
        // Test loading data into existing lists
        DummyDataGenerator.loadSampleData(criteriaList, alternativeList);
        
        // Verify data was loaded correctly
        assertNotNull("Criteria list should not be null", criteriaList);
        assertNotNull("Alternatives list should not be null", alternativeList);
        assertFalse("Criteria list should not be empty", criteriaList.isEmpty());
        assertFalse("Alternatives list should not be empty", alternativeList.isEmpty());
        
        // Verify consistency between alternatives and criteria
        for (Alternative alt : alternativeList) {
            for (Criteria c : criteriaList) {
                assertNotNull(
                    String.format("Alternative '%s' should have value for criteria '%s'",
                        alt.getName(), c.getName()),
                    alt.getCriteriaValue(c.getName())
                );
            }
        }
    }

    @Test
    public void testDataConsistency() {
        List<Criteria> criteria1 = DummyDataGenerator.generateSampleCriteria();
        List<Criteria> criteria2 = DummyDataGenerator.generateSampleCriteria();
        
        // Verify multiple calls produce consistent data
        assertEquals("Multiple calls should produce same number of criteria",
            criteria1.size(), criteria2.size());
            
        for (int i = 0; i < criteria1.size(); i++) {
            Criteria c1 = criteria1.get(i);
            Criteria c2 = criteria2.get(i);
            
            assertEquals("Criteria names should be consistent", 
                c1.getName(), c2.getName());
            assertEquals("Criteria weights should be consistent",
                c1.getWeight(), c2.getWeight(), 0.001);
            assertEquals("Criteria types should be consistent",
                c1.getType(), c2.getType());
        }
    }
}
