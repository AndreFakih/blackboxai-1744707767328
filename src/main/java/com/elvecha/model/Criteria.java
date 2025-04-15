package com.elvecha.model;

public class Criteria {
    private String name;
    private double weight;
    private String type;

    public Criteria(String name, double weight, String type) {
        validateName(name);
        validateWeight(weight);
        validateType(type);
        
        this.name = name.trim();
        this.weight = weight;
        this.type = normalizeType(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        validateWeight(weight);
        this.weight = weight;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        validateType(type);
        this.type = normalizeType(type);
    }

    private void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Nama kriteria tidak boleh null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama kriteria tidak boleh kosong");
        }
    }

    private void validateWeight(double weight) {
        if (weight < 0 || weight > 1) {
            throw new IllegalArgumentException(
                "Bobot harus berada di antara 0 dan 1 (inclusive)");
        }
    }

    private void validateType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Tipe kriteria tidak boleh null");
        }
        String normalizedType = type.trim().toLowerCase();
        if (!normalizedType.equals("benefit") && !normalizedType.equals("cost")) {
            throw new IllegalArgumentException(
                "Tipe kriteria harus 'Benefit' atau 'Cost'");
        }
    }

    private String normalizeType(String type) {
        String normalized = type.trim().toLowerCase();
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }

    @Override
    public String toString() {
        return "Criteria{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", type='" + type + '\'' +
                '}';
    }
}
