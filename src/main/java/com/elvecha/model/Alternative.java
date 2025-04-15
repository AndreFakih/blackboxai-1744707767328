package com.elvecha.model;

import java.util.HashMap;
import java.util.Map;

public class Alternative {
    private String name;
    private Map<String, Double> criteriaValues;
    private double finalScore;

    public Alternative(String name) {
        validateName(name);
        this.name = name.trim();
        this.criteriaValues = new HashMap<>();
        this.finalScore = 0.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public Map<String, Double> getCriteriaValues() {
        // Return a copy to prevent external modification
        return new HashMap<>(criteriaValues);
    }

    public void setCriteriaValue(String criteriaName, Double value) {
        validateCriteriaName(criteriaName);
        // Allow null values for criteria (represents unset values)
        criteriaValues.put(criteriaName.trim(), value);
    }

    public Double getCriteriaValue(String criteriaName) {
        validateCriteriaName(criteriaName);
        return criteriaValues.get(criteriaName.trim());
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    private void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Nama alternatif tidak boleh null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama alternatif tidak boleh kosong");
        }
    }

    private void validateCriteriaName(String criteriaName) {
        if (criteriaName == null) {
            throw new IllegalArgumentException("Nama kriteria tidak boleh null");
        }
        if (criteriaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama kriteria tidak boleh kosong");
        }
    }

    @Override
    public String toString() {
        return "Alternative{" +
                "name='" + name + '\'' +
                ", criteriaValues=" + criteriaValues +
                ", finalScore=" + finalScore +
                '}';
    }
}
