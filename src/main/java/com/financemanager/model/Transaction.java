package com.financemanager.model;

public class Transaction {
    private String id;
    private String type;
    private String categoryId;
    private String accountId;
    private String toAccountId;
    private double amount;
    private String date; // YYYY-MM-DD
    private String note;
    private String description;
    private String receiptPath;
    private String recurringRuleId;

    public Transaction(String id, String type, String categoryId, String accountId, String toAccountId, double amount, String date, String note, String description, String receiptPath, String recurringRuleId) {
        this.id = id;
        this.type = type;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.description = description;
        this.receiptPath = receiptPath;
        this.recurringRuleId = recurringRuleId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getCategoryId() { return categoryId; }
    public String getAccountId() { return accountId; }
    public String getToAccountId() { return toAccountId; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getNote() { return note; }
    public String getDescription() { return description; }
    public String getReceiptPath() { return receiptPath; }
    public String getRecurringRuleId() { return recurringRuleId; }
}
