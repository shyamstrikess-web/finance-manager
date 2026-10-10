package com.financemanager.model;

public class RecurringRule {
    private String id;
    private String type;
    private String categoryId;
    private String accountId;
    private String toAccountId;
    private double amount;
    private String description;
    private String frequency;
    private String timing;
    private String startDate;
    private boolean isActive;
    private String lastProcessed;

    public RecurringRule(String id, String type, String categoryId, String accountId, String toAccountId, double amount, String description, String frequency, String timing, String startDate, boolean isActive, String lastProcessed) {
        this.id = id;
        this.type = type;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = description;
        this.frequency = frequency;
        this.timing = timing;
        this.startDate = startDate;
        this.isActive = isActive;
        this.lastProcessed = lastProcessed;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getCategoryId() { return categoryId; }
    public String getAccountId() { return accountId; }
    public String getToAccountId() { return toAccountId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getFrequency() { return frequency; }
    public String getTiming() { return timing; }
    public String getStartDate() { return startDate; }
    public boolean isActive() { return isActive; }
    public String getLastProcessed() { return lastProcessed; }
    public void setLastProcessed(String lastProcessed) { this.lastProcessed = lastProcessed; }
}
