package com.financemanager.model;

import java.util.Map;
import java.util.HashMap;

public class Budget {
    private String categoryId;
    private double defaultAmount;
    private Map<String, Double> monthlyOverrides = new HashMap<>(); // Deprecated somewhat
    
    // Generic period limits: e.g. "YEAR:2026", "MONTH:2026-05", "WEEK:2026-05-W1"
    private Map<String, Double> customLimits = new HashMap<>();

    public Budget(String categoryId, double defaultAmount) {
        this.categoryId = categoryId;
        this.defaultAmount = defaultAmount;
    }

    public String getCategoryId() { return categoryId; }
    public double getDefaultAmount() { return defaultAmount; }
    
    public Map<String, Double> getMonthlyOverrides() { return monthlyOverrides; }
    public void addOverride(String yearMonth, double amount) {
        monthlyOverrides.put(yearMonth, amount);
    }
    
    public Map<String, Double> getCustomLimits() { return customLimits; }
    public void addCustomLimit(String periodType, String periodValue, double amount) {
        customLimits.put(periodType + ":" + periodValue, amount);
    }
    
    public Double getCustomLimit(String periodType, String periodValue) {
        return customLimits.get(periodType + ":" + periodValue);
    }
}
