package com.financemanager.model;

public class Account {
    private String id;
    private String name;
    private String type;
    private String icon;
    private double balance;
    private String color;

    public Account(String id, String name, String type, String icon, double balance, String color) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.balance = balance;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    @Override
    public String toString() { return icon + " " + name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account a = (Account) o;
        return id != null && id.equals(a.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
