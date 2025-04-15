package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import java.util.*;

public class DummyDataGenerator {
    
    public static List<Criteria> generateSampleCriteria() {
        List<Criteria> criteriaList = new ArrayList<>();
        
        // Add sample criteria with realistic weights and types
        criteriaList.add(new Criteria("Harga Paket", 0.35, "Cost"));
        criteriaList.add(new Criteria("Jumlah Vendor", 0.25, "Benefit"));
        criteriaList.add(new Criteria("Pengalaman (Tahun)", 0.15, "Benefit"));
        criteriaList.add(new Criteria("Rating Pelanggan", 0.15, "Benefit"));
        criteriaList.add(new Criteria("Jarak Lokasi (km)", 0.10, "Cost"));
        
        return criteriaList;
    }
    
    public static List<Alternative> generateSampleAlternatives() {
        List<Alternative> alternativeList = new ArrayList<>();
        
        // WO 1: Premium service with high price
        Alternative wo1 = new Alternative("Elegant Wedding");
        wo1.setCriteriaValue("Harga Paket", 75000000.0);
        wo1.setCriteriaValue("Jumlah Vendor", 8.0);
        wo1.setCriteriaValue("Pengalaman (Tahun)", 10.0);
        wo1.setCriteriaValue("Rating Pelanggan", 4.8);
        wo1.setCriteriaValue("Jarak Lokasi (km)", 5.0);
        alternativeList.add(wo1);
        
        // WO 2: Mid-range balanced option
        Alternative wo2 = new Alternative("Happy Wedding");
        wo2.setCriteriaValue("Harga Paket", 50000000.0);
        wo2.setCriteriaValue("Jumlah Vendor", 6.0);
        wo2.setCriteriaValue("Pengalaman (Tahun)", 5.0);
        wo2.setCriteriaValue("Rating Pelanggan", 4.5);
        wo2.setCriteriaValue("Jarak Lokasi (km)", 8.0);
        alternativeList.add(wo2);
        
        // WO 3: Budget-friendly option
        Alternative wo3 = new Alternative("Smart Wedding");
        wo3.setCriteriaValue("Harga Paket", 35000000.0);
        wo3.setCriteriaValue("Jumlah Vendor", 4.0);
        wo3.setCriteriaValue("Pengalaman (Tahun)", 3.0);
        wo3.setCriteriaValue("Rating Pelanggan", 4.2);
        wo3.setCriteriaValue("Jarak Lokasi (km)", 12.0);
        alternativeList.add(wo3);
        
        // WO 4: Premium local service
        Alternative wo4 = new Alternative("Royal Wedding");
        wo4.setCriteriaValue("Harga Paket", 65000000.0);
        wo4.setCriteriaValue("Jumlah Vendor", 7.0);
        wo4.setCriteriaValue("Pengalaman (Tahun)", 8.0);
        wo4.setCriteriaValue("Rating Pelanggan", 4.7);
        wo4.setCriteriaValue("Jarak Lokasi (km)", 3.0);
        alternativeList.add(wo4);
        
        // WO 5: New but promising service
        Alternative wo5 = new Alternative("Fresh Wedding");
        wo5.setCriteriaValue("Harga Paket", 45000000.0);
        wo5.setCriteriaValue("Jumlah Vendor", 5.0);
        wo5.setCriteriaValue("Pengalaman (Tahun)", 2.0);
        wo5.setCriteriaValue("Rating Pelanggan", 4.4);
        wo5.setCriteriaValue("Jarak Lokasi (km)", 15.0);
        alternativeList.add(wo5);
        
        return alternativeList;
    }
    
    public static void loadSampleData(List<Criteria> criteriaList, List<Alternative> alternativeList) {
        // Clear existing data
        criteriaList.clear();
        alternativeList.clear();
        
        // Add sample criteria
        criteriaList.addAll(generateSampleCriteria());
        
        // Add sample alternatives
        alternativeList.addAll(generateSampleAlternatives());
    }
}
