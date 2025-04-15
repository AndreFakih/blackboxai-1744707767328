package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.DecimalFormat;

/**
 * Utility class providing helper methods for tests
 */
public class TestUtils {
    private static final Random random = new Random();
    private static final DecimalFormat df = new DecimalFormat("#.###");
    
    /**
     * Creates a list of test criteria with valid weights summing to 1.0
     */
    public static List<Criteria> createTestCriteria(int count) {
        List<Criteria> criteriaList = new ArrayList<>();
        double remainingWeight = 1.0;
        
        for (int i = 0; i < count; i++) {
            String name = "Criteria" + (i + 1);
            String type = (i % 2 == 0) ? "Benefit" : "Cost";
            
            // For last criteria, use remaining weight
            double weight;
            if (i == count - 1) {
                weight = remainingWeight;
            } else {
                // Generate random weight that doesn't exceed remaining
                weight = Math.min(0.1 + random.nextDouble() * 0.2, remainingWeight);
                remainingWeight -= weight;
            }
            
            // Format weight to 3 decimal places
            weight = Double.parseDouble(df.format(weight));
            criteriaList.add(new Criteria(name, weight, type));
        }
        
        return criteriaList;
    }
    
    /**
     * Creates a list of test alternatives with random values
     */
    public static List<Alternative> createTestAlternatives(
            int count, List<Criteria> criteriaList) {
        List<Alternative> alternativeList = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Alternative alt = new Alternative("Alternative" + (i + 1));
            
            for (Criteria criteria : criteriaList) {
                double value = generateValueForCriteria(criteria);
                alt.setCriteriaValue(criteria.getName(), value);
            }
            
            alternativeList.add(alt);
        }
        
        return alternativeList;
    }
    
    /**
     * Generates appropriate test value based on criteria type
     */
    private static double generateValueForCriteria(Criteria criteria) {
        switch (criteria.getName().toLowerCase()) {
            case "harga":
            case "price":
            case "biaya":
            case "cost":
                // Generate price between 30M and 80M
                return 30_000_000 + random.nextDouble() * 50_000_000;
                
            case "rating":
            case "nilai":
            case "score":
                // Generate rating between 1 and 5
                return 1.0 + random.nextDouble() * 4.0;
                
            case "vendor":
            case "vendors":
                // Generate vendor count between 3 and 10
                return 3.0 + random.nextDouble() * 7.0;
                
            case "pengalaman":
            case "experience":
                // Generate years of experience between 1 and 15
                return 1.0 + random.nextDouble() * 14.0;
                
            case "jarak":
            case "distance":
                // Generate distance between 1 and 30 km
                return 1.0 + random.nextDouble() * 29.0;
                
            default:
                // For unknown criteria, generate value between 1 and 100
                return 1.0 + random.nextDouble() * 99.0;
        }
    }
    
    /**
     * Verifies that criteria weights sum to 1.0
     */
    public static boolean verifyWeightSum(List<Criteria> criteriaList) {
        double sum = criteriaList.stream()
            .mapToDouble(Criteria::getWeight)
            .sum();
        return Math.abs(sum - 1.0) < 0.001; // Allow small floating-point difference
    }
    
    /**
     * Verifies that all alternatives have values for all criteria
     */
    public static boolean verifyAlternativeCompleteness(
            List<Alternative> alternatives, List<Criteria> criteria) {
        return alternatives.stream().allMatch(alt ->
            criteria.stream().allMatch(crit ->
                alt.getCriteriaValue(crit.getName()) != null
            )
        );
    }
    
    /**
     * Cleans up test files
     */
    public static void cleanupTestFiles(String... filePaths) {
        for (String path : filePaths) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    /**
     * Formats a double value to 3 decimal places
     */
    public static double formatDouble(double value) {
        return Double.parseDouble(df.format(value));
    }
    
    /**
     * Creates a deep copy of a criteria list
     */
    public static List<Criteria> copyCriteriaList(List<Criteria> original) {
        List<Criteria> copy = new ArrayList<>();
        for (Criteria criteria : original) {
            copy.add(new Criteria(
                criteria.getName(),
                criteria.getWeight(),
                criteria.getType()
            ));
        }
        return copy;
    }
    
    /**
     * Creates a deep copy of an alternative list
     */
    public static List<Alternative> copyAlternativeList(List<Alternative> original) {
        List<Alternative> copy = new ArrayList<>();
        for (Alternative alt : original) {
            Alternative newAlt = new Alternative(alt.getName());
            for (Map.Entry<String, Double> entry : alt.getCriteriaValues().entrySet()) {
                newAlt.setCriteriaValue(entry.getKey(), entry.getValue());
            }
            newAlt.setFinalScore(alt.getFinalScore());
            copy.add(newAlt);
        }
        return copy;
    }
    
    /**
     * Verifies that rankings are in descending order
     */
    public static boolean verifyRankingOrder(List<Alternative> rankedList) {
        for (int i = 0; i < rankedList.size() - 1; i++) {
            if (rankedList.get(i).getFinalScore() < 
                rankedList.get(i + 1).getFinalScore()) {
                return false;
            }
        }
        return true;
    }
}
