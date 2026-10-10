package com.financemanager.db;

import com.financemanager.model.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataStore {
    private static DataStore instance;

    private DataStore() {}

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
    
    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // --- ACCOUNTS ---
    public List<Account> getAccounts() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Account(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getDouble("balance"), rs.getString("color")));
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return list;
    }

    public Account getAccount(String id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new Account(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getDouble("balance"), rs.getString("color"));
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return null;
    }

    public void updateAccountBalance(String id, double change) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, change);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public void addAccount(Account a) {
        if (a.getId() == null) a.setId(generateId("acc"));
        String sql = "INSERT INTO accounts(id, name, type, icon, balance, color) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getId());
            pstmt.setString(2, a.getName());
            pstmt.setString(3, a.getType());
            pstmt.setString(4, a.getIcon());
            pstmt.setDouble(5, a.getBalance());
            pstmt.setString(6, a.getColor());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    // --- CATEGORIES ---
    public List<Category> getCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Category(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getString("color")));
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return list;
    }
    
    public Category getCategory(String id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new Category(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getString("color"));
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return null;
    }

    public void addCategory(Category c) {
        if (c.getId() == null) c.setId(generateId("cat"));
        String sql = "INSERT INTO categories(id, name, type, icon, color) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getId());
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getType());
            pstmt.setString(4, c.getIcon());
            pstmt.setString(5, c.getColor());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public void updateAccount(Account a) {
        String sql = "UPDATE accounts SET name=?, type=?, icon=?, balance=?, color=? WHERE id=?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getName());
            pstmt.setString(2, a.getType());
            pstmt.setString(3, a.getIcon());
            pstmt.setDouble(4, a.getBalance());
            pstmt.setString(5, a.getColor());
            pstmt.setString(6, a.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public void updateCategory(Category c) {
        String sql = "UPDATE categories SET name=?, type=?, icon=?, color=? WHERE id=?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getType());
            pstmt.setString(3, c.getIcon());
            pstmt.setString(4, c.getColor());
            pstmt.setString(5, c.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    // --- TRANSACTIONS ---
    public List<Transaction> getTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Transaction(rs.getString("id"), rs.getString("type"), rs.getString("category_id"), rs.getString("account_id"), rs.getString("to_account_id"), rs.getDouble("amount"), rs.getString("date"), rs.getString("note"), rs.getString("description"), rs.getString("receipt_path"), rs.getString("recurring_rule_id")));
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return list;
    }

    public Transaction getTransaction(String id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Transaction(rs.getString("id"), rs.getString("type"), rs.getString("category_id"), rs.getString("account_id"), rs.getString("to_account_id"), rs.getDouble("amount"), rs.getString("date"), rs.getString("note"), rs.getString("description"), rs.getString("receipt_path"), rs.getString("recurring_rule_id"));
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return null;
    }

    public void deleteTransaction(String id) {
        Transaction tx = getTransaction(id);
        if (tx != null) {
            // Revert balances
            if ("expense".equals(tx.getType())) {
                updateAccountBalance(tx.getAccountId(), tx.getAmount());
            } else if ("income".equals(tx.getType())) {
                updateAccountBalance(tx.getAccountId(), -tx.getAmount());
            } else if ("transfer".equals(tx.getType())) {
                updateAccountBalance(tx.getAccountId(), tx.getAmount());
                updateAccountBalance(tx.getToAccountId(), -tx.getAmount());
            }
            
            String sql = "DELETE FROM transactions WHERE id = ?";
            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        }
    }

    public void updateTransaction(Transaction newTx) {
        deleteTransaction(newTx.getId());
        addTransaction(newTx);
    }

    public void deleteAccount(String id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public void deleteCategory(String id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public void deleteBudgetLimit(String categoryId, String periodType, String periodValue) {
        String sql = "DELETE FROM budget_limits WHERE category_id = ? AND period_type = ? AND period_value = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            pstmt.setString(2, periodType);
            pstmt.setString(3, periodValue);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public void addTransaction(Transaction t) {
        if (t.getId() == null) t = new Transaction(generateId("txn"), t.getType(), t.getCategoryId(), t.getAccountId(), t.getToAccountId(), t.getAmount(), t.getDate(), t.getNote(), t.getDescription(), t.getReceiptPath(), t.getRecurringRuleId());
        
        String sql = "INSERT INTO transactions(id, type, category_id, account_id, to_account_id, amount, date, note, description, receipt_path, recurring_rule_id) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getId());
            pstmt.setString(2, t.getType());
            pstmt.setString(3, t.getCategoryId());
            pstmt.setString(4, t.getAccountId());
            pstmt.setString(5, t.getToAccountId());
            pstmt.setDouble(6, t.getAmount());
            pstmt.setString(7, t.getDate());
            pstmt.setString(8, t.getNote());
            pstmt.setString(9, t.getDescription());
            pstmt.setString(10, t.getReceiptPath());
            pstmt.setString(11, t.getRecurringRuleId());
            pstmt.executeUpdate();

            // Update balances
            if ("expense".equals(t.getType())) {
                updateAccountBalance(t.getAccountId(), -t.getAmount());
            } else if ("income".equals(t.getType())) {
                updateAccountBalance(t.getAccountId(), t.getAmount());
            } else if ("transfer".equals(t.getType())) {
                updateAccountBalance(t.getAccountId(), -t.getAmount());
                updateAccountBalance(t.getToAccountId(), t.getAmount());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    // --- RECURRING RULES ---
    public List<RecurringRule> getRecurringRules() {
        List<RecurringRule> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_rules";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new RecurringRule(rs.getString("id"), rs.getString("type"), rs.getString("category_id"), rs.getString("account_id"), rs.getString("to_account_id"), rs.getDouble("amount"), rs.getString("description"), rs.getString("frequency"), rs.getString("timing"), rs.getString("start_date"), rs.getInt("is_active") == 1, rs.getString("last_processed")));
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return list;
    }

    public void updateRuleLastProcessed(String id, String lastProcessed) {
        String sql = "UPDATE recurring_rules SET last_processed = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lastProcessed);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    // --- BUDGETS ---
    public List<Budget> getBudgets() {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Budget b = new Budget(rs.getString("category_id"), rs.getDouble("default_amount"));
                // Load legacy overrides
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM budget_overrides WHERE category_id = ?")) {
                    pstmt.setString(1, b.getCategoryId());
                    ResultSet rs2 = pstmt.executeQuery();
                    while(rs2.next()) {
                        String key = rs2.getInt("year") + "-" + String.format("%02d", rs2.getInt("month"));
                        b.addOverride(key, rs2.getDouble("amount"));
                    }
                }
                // Load new custom limits
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM budget_limits WHERE category_id = ?")) {
                    pstmt.setString(1, b.getCategoryId());
                    ResultSet rs3 = pstmt.executeQuery();
                    while(rs3.next()) {
                        b.addCustomLimit(rs3.getString("period_type"), rs3.getString("period_value"), rs3.getDouble("amount"));
                    }
                }
                list.add(b);
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return list;
    }

    public void setBudgetLimit(String categoryId, String periodType, String periodValue, double amount) {
        String ensureBudget = "INSERT INTO budgets(category_id, default_amount) VALUES(?, 0) ON CONFLICT(category_id) DO NOTHING";
        String sql = "INSERT INTO budget_limits(category_id, period_type, period_value, amount) VALUES(?,?,?,?) " +
                     "ON CONFLICT(category_id, period_type, period_value) DO UPDATE SET amount=excluded.amount";
        try (Connection conn = DatabaseHelper.connect()) {
            try (PreparedStatement p1 = conn.prepareStatement(ensureBudget)) {
                p1.setString(1, categoryId);
                p1.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categoryId);
                pstmt.setString(2, periodType);
                pstmt.setString(3, periodValue);
                pstmt.setDouble(4, amount);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }
}
