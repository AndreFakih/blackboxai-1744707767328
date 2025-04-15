package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import java.util.*;

public class SAWCalculator {
    
    public List<Alternative> calculate(List<Criteria> criteria, List<Alternative> alternatives) {
        if (criteria.isEmpty() || alternatives.isEmpty()) {
            throw new IllegalArgumentException("Criteria and alternatives lists cannot be empty");
        }

        // Step 1: Create decision matrix
        double[][] matrix = createDecisionMatrix(criteria, alternatives);
        
        // Step 2: Normalize the matrix
        double[][] normalizedMatrix = normalizeMatrix(matrix, criteria);
        
        // Step 3: Calculate weighted sum and set final scores
        calculateFinalScores(normalizedMatrix, criteria, alternatives);
        
        // Step 4: Sort alternatives by final score (descending)
        alternatives.sort((a1, a2) -> Double.compare(a2.getFinalScore(), a1.getFinalScore()));
        
        return alternatives;
    }

    private double[][] createDecisionMatrix(List<Criteria> criteria, List<Alternative> alternatives) {
        double[][] matrix = new double[alternatives.size()][criteria.size()];
        
        for (int i = 0; i < alternatives.size(); i++) {
            Alternative alt = alternatives.get(i);
            for (int j = 0; j < criteria.size(); j++) {
                Criteria crit = criteria.get(j);
                Double value = alt.getCriteriaValue(crit.getName());
                matrix[i][j] = value != null ? value : 0.0;
            }
        }
        
        return matrix;
    }

    private double[][] normalizeMatrix(double[][] matrix, List<Criteria> criteria) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] normalized = new double[rows][cols];

        for (int j = 0; j < cols; j++) {
            double[] column = new double[rows];
            for (int i = 0; i < rows; i++) {
                column[i] = matrix[i][j];
            }

            double max = Arrays.stream(column).max().getAsDouble();
            double min = Arrays.stream(column).min().getAsDouble();

            for (int i = 0; i < rows; i++) {
                if (criteria.get(j).getType().equalsIgnoreCase("benefit")) {
                    normalized[i][j] = matrix[i][j] / max;
                } else { // Cost criteria
                    normalized[i][j] = min / matrix[i][j];
                }
            }
        }

        return normalized;
    }

    private void calculateFinalScores(double[][] normalizedMatrix, List<Criteria> criteria, 
                                    List<Alternative> alternatives) {
        for (int i = 0; i < alternatives.size(); i++) {
            double score = 0.0;
            for (int j = 0; j < criteria.size(); j++) {
                score += normalizedMatrix[i][j] * criteria.get(j).getWeight();
            }
            alternatives.get(i).setFinalScore(score);
        }
    }
}
