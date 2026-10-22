package com.financemanager.db;

import com.financemanager.model.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static DataStore instance;

    // In-memory cache for ultra-fast UI rendering
    private Map<String, Account> accountCache = null;
    private Map<String, Category> categoryCache = null;

    private DataStore() {}

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
    
    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public synchronized void invalidateCache() {
        accountCache = null;
        categoryCache = null;
    }

    // --- ACCOUNTS ---
    public synchronized List<Account> getAccounts() {
        List<Account> list = new ArrayList<>();
        Map<String, Account> newCache = new HashMap<>();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account a = new Account(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getDouble("balance"), rs.getString("color"));
                list.add(a);
                newCache.put(a.getId(), a);
            }
            this.accountCache = newCache;
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return list;
    }

    public synchronized Account getAccount(String id) {
        if (id == null) return null;
        if (accountCache == null) {
            getAccounts();
        }
        if (accountCache != null && accountCache.containsKey(id)) {
            return accountCache.get(id);
        }
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Account a = new Account(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getDouble("balance"), rs.getString("color"));
                if (accountCache != null) accountCache.put(a.getId(), a);
                return a;
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return null;
    }

    public synchronized void updateAccountBalance(String id, double change) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, change);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            if (accountCache != null && accountCache.containsKey(id)) {
                Account cached = accountCache.get(id);
                cached.setBalance(cached.getBalance() + change);
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public synchronized void addAccount(Account a) {
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
            accountCache = null;
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    // --- CATEGORIES ---
    public synchronized List<Category> getCategories() {
        List<Category> list = new ArrayList<>();
        Map<String, Category> newCache = new HashMap<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category c = new Category(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getString("color"));
                list.add(c);
                newCache.put(c.getId(), c);
            }
            this.categoryCache = newCache;
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return list;
    }
    
    public synchronized Category getCategory(String id) {
        if (id == null) return null;
        if (categoryCache == null) {
            getCategories();
        }
        if (categoryCache != null && categoryCache.containsKey(id)) {
            return categoryCache.get(id);
        }
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Category c = new Category(rs.getString("id"), rs.getString("name"), rs.getString("type"), rs.getString("icon"), rs.getString("color"));
                if (categoryCache != null) categoryCache.put(c.getId(), c);
                return c;
            }
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
        return null;
    }

    public synchronized void addCategory(Category c) {
        if (c.getId() == null) c.setId(generateId("cat"));
        String sql = "INSERT INTO categories(id, name, type, icon, color) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getId());
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getType());
            pstmt.setString(4, c.getIcon());
            pstmt.setString(5, c.getColor());
            pstmt.executeUpdate();
            categoryCache = null;
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public synchronized void updateAccount(Account a) {
        String sql = "UPDATE accounts SET name=?, type=?, icon=?, balance=?, color=? WHERE id=?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getName());
            pstmt.setString(2, a.getType());
            pstmt.setString(3, a.getIcon());
            pstmt.setDouble(4, a.getBalance());
            pstmt.setString(5, a.getColor());
            pstmt.setString(6, a.getId());
            pstmt.executeUpdate();
            accountCache = null;
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public synchronized void updateCategory(Category c) {
        String sql = "UPDATE categories SET name=?, type=?, icon=?, color=? WHERE id=?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getType());
            pstmt.setString(3, c.getIcon());
            pstmt.setString(4, c.getColor());
            pstmt.setString(5, c.getId());
            pstmt.executeUpdate();
            categoryCache = null;
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

    public synchronized void deleteTransaction(String id) {
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

    public synchronized void updateTransaction(Transaction newTx) {
        deleteTransaction(newTx.getId());
        addTransaction(newTx);
    }

    public synchronized void deleteAccount(String id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            accountCache = null;
        } catch (SQLException e) { throw new RuntimeException("Database error: " + e.getMessage(), e); }
    }

    public synchronized void deleteCategory(String id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            categoryCache = null;
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

    public synchronized void addTransaction(Transaction t) {
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
        try (Connection conn = DatabaseHelper.connect()) {
            boolean mysql = DatabaseHelper.isMySQL(conn);
            String ensureBudget;
            String sql;
            if (mysql) {
                ensureBudget = "INSERT INTO budgets(category_id, default_amount) VALUES(?, 0) ON DUPLICATE KEY UPDATE default_amount=default_amount";
                sql = "INSERT INTO budget_limits(category_id, period_type, period_value, amount) VALUES(?,?,?,?) " +
                      "ON DUPLICATE KEY UPDATE amount=VALUES(amount)";
            } else {
                ensureBudget = "INSERT INTO budgets(category_id, default_amount) VALUES(?, 0) ON CONFLICT(category_id) DO NOTHING";
                sql = "INSERT INTO budget_limits(category_id, period_type, period_value, amount) VALUES(?,?,?,?) " +
                      "ON CONFLICT(category_id, period_type, period_value) DO UPDATE SET amount=excluded.amount";
            }

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
