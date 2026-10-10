package com.financemanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_PATH = System.getProperty("user.home") + "/finance_manager_v2.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "icon TEXT," +
                "balance REAL NOT NULL," +
                "color TEXT" +
                ");";

        String createCategoryTable = "CREATE TABLE IF NOT EXISTS categories (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "icon TEXT," +
                "color TEXT" +
                ");";

        String createTransactionTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id TEXT PRIMARY KEY," +
                "type TEXT NOT NULL," +
                "category_id TEXT," +
                "account_id TEXT," +
                "to_account_id TEXT," +
                "amount REAL NOT NULL," +
                "date TEXT NOT NULL," +
                "note TEXT," +
                "description TEXT," +
                "receipt_path TEXT," +
                "recurring_rule_id TEXT," +
                "FOREIGN KEY(category_id) REFERENCES categories(id)," +
                "FOREIGN KEY(account_id) REFERENCES accounts(id)," +
                "FOREIGN KEY(to_account_id) REFERENCES accounts(id)" +
                ");";
                
        String createRecurringRulesTable = "CREATE TABLE IF NOT EXISTS recurring_rules (" +
                "id TEXT PRIMARY KEY," +
                "type TEXT NOT NULL," +
                "category_id TEXT," +
                "account_id TEXT," +
                "to_account_id TEXT," +
                "amount REAL NOT NULL," +
                "description TEXT," +
                "frequency TEXT NOT NULL," +
                "timing TEXT NOT NULL," +
                "start_date TEXT NOT NULL," +
                "is_active INTEGER NOT NULL," +
                "last_processed TEXT" +
                ");";

        String createBudgetTable = "CREATE TABLE IF NOT EXISTS budgets (" +
                "category_id TEXT PRIMARY KEY," +
                "default_amount REAL," +
                "FOREIGN KEY(category_id) REFERENCES categories(id)" +
                ");";
                
        String createBudgetOverridesTable = "CREATE TABLE IF NOT EXISTS budget_overrides (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category_id TEXT," +
                "year INTEGER," +
                "month INTEGER," +
                "amount REAL," +
                "FOREIGN KEY(category_id) REFERENCES categories(id)," +
                "UNIQUE(category_id, year, month)" +
                ");";

        String createBudgetLimitsTable = "CREATE TABLE IF NOT EXISTS budget_limits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category_id TEXT," +
                "period_type TEXT," + // 'YEAR', 'MONTH', 'WEEK'
                "period_value TEXT," + // e.g. '2026', '2026-05', '2026-05-W1'
                "amount REAL," +
                "FOREIGN KEY(category_id) REFERENCES categories(id)," +
                "UNIQUE(category_id, period_type, period_value)" +
                ");";

        String createSettingsTable = "CREATE TABLE IF NOT EXISTS settings (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createAccountsTable);
            stmt.execute(createCategoryTable);
            stmt.execute(createTransactionTable);
            stmt.execute(createRecurringRulesTable);
            stmt.execute(createBudgetTable);
            stmt.execute(createBudgetOverridesTable);
            stmt.execute(createBudgetLimitsTable);
            stmt.execute(createSettingsTable);
            insertDefaultData(stmt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static void insertDefaultData(Statement stmt) throws SQLException {
        // Accounts
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO accounts (id, name, type, icon, balance, color) VALUES ('acc-1', 'Cash', 'cash', '💵', 15000, '#4CAF50')");
            stmt.execute("INSERT INTO accounts (id, name, type, icon, balance, color) VALUES ('acc-2', 'Bank Account', 'bank', '🏦', 250000, '#1976D2')");
        }
        
        // Categories
        rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("INSERT INTO categories (id, name, type, icon, color) VALUES ('cat-food', 'Food', 'expense', '🍜', '#FF7043')");
            stmt.execute("INSERT INTO categories (id, name, type, icon, color) VALUES ('cat-transport', 'Transport', 'expense', '🚗', '#29B6F6')");
            stmt.execute("INSERT INTO categories (id, name, type, icon, color) VALUES ('cat-salary', 'Salary', 'income', '💰', '#66BB6A')");
        }
    }
}
