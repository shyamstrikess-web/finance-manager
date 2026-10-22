package com.financemanager.db;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    // MySQL / MariaDB (XAMPP localhost)
    private static final String MYSQL_HOST = "127.0.0.1";
    private static final int MYSQL_PORT = 3306;
    private static final String MYSQL_DB = "finance_manager";
    private static final String MYSQL_URL = "jdbc:mariadb://" + MYSQL_HOST + ":" + MYSQL_PORT + "/" + MYSQL_DB + 
            "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=3000&socketTimeout=10000";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";

    // SQLite fallback path
    private static final String DB_PATH = System.getProperty("user.home") + "/finance_manager_v2.db";
    private static final String SQLITE_URL = "jdbc:sqlite:" + DB_PATH;

    private static Boolean loggedConnectionMode = null;
    private static boolean xamppServicesChecked = false;

    public static boolean isMySQL(Connection conn) {
        if (conn == null) return false;
        try {
            String prod = conn.getMetaData().getDatabaseProductName().toLowerCase();
            return prod.contains("mysql") || prod.contains("mariadb");
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean isPortListening(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void ensureXamppServices() {
        if (xamppServicesChecked) return;
        xamppServicesChecked = true;

        // Auto-start MySQL if not listening
        if (!isPortListening(MYSQL_PORT)) {
            File mysqld = new File("C:/xampp/mysql/bin/mysqld.exe");
            File myIni = new File("C:/xampp/mysql/bin/my.ini");
            if (mysqld.exists()) {
                try {
                    System.out.println("[DatabaseHelper] Starting XAMPP MySQL daemon...");
                    ProcessBuilder pb = new ProcessBuilder(
                        mysqld.getAbsolutePath(),
                        "--defaults-file=" + myIni.getAbsolutePath(),
                        "--standalone"
                    );
                    pb.directory(new File("C:/xampp"));
                    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    pb.start();

                    // Wait up to 3 seconds for MySQL to listen on port 3306
                    for (int i = 0; i < 15; i++) {
                        Thread.sleep(200);
                        if (isPortListening(MYSQL_PORT)) {
                            System.out.println("[DatabaseHelper] MySQL daemon started successfully.");
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[DatabaseHelper] Could not auto-start MySQL: " + e.getMessage());
                }
            }
        }

        // Auto-start Apache if not listening (so phpMyAdmin is accessible)
        if (!isPortListening(80)) {
            File httpd = new File("C:/xampp/apache/bin/httpd.exe");
            if (httpd.exists()) {
                try {
                    System.out.println("[DatabaseHelper] Starting XAMPP Apache daemon...");
                    ProcessBuilder pb = new ProcessBuilder(
                        httpd.getAbsolutePath(),
                        "-d", "C:/xampp/apache"
                    );
                    pb.directory(new File("C:/xampp/apache"));
                    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    pb.start();
                } catch (Exception e) {
                    System.err.println("[DatabaseHelper] Could not auto-start Apache: " + e.getMessage());
                }
            }
        }
    }

    public static Connection connect() {
        if (!xamppServicesChecked) {
            ensureXamppServices();
        }

        // Attempt MySQL connection first (XAMPP localhost)
        try {
            Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
            if (loggedConnectionMode == null || !loggedConnectionMode) {
                System.out.println("[DatabaseHelper] Connected to MySQL on " + MYSQL_HOST + ":" + MYSQL_PORT + " (" + MYSQL_DB + ")");
                loggedConnectionMode = true;
            }
            return conn;
        } catch (SQLException e) {
            // If MySQL is not running, fallback to SQLite
            if (loggedConnectionMode == null || loggedConnectionMode) {
                System.err.println("[DatabaseHelper] MySQL unavailable (" + e.getMessage() + "), using SQLite fallback.");
                loggedConnectionMode = false;
            }
            try {
                return DriverManager.getConnection(SQLITE_URL);
            } catch (SQLException ex) {
                System.err.println("[DatabaseHelper] SQLite connection failed: " + ex.getMessage());
                return null;
            }
        }
    }

    public static void initializeDatabase() {
        ensureXamppServices();

        try (Connection conn = connect()) {
            if (conn == null) return;
            boolean mysql = isMySQL(conn);
            try (Statement stmt = conn.createStatement()) {
                if (mysql) {
                    initMySQL(stmt);
                } else {
                    initSQLite(stmt);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseHelper] Error initializing database: " + e.getMessage());
        }
    }

    private static void initMySQL(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS `accounts` (" +
                "`id` VARCHAR(100) NOT NULL PRIMARY KEY," +
                "`name` VARCHAR(255) NOT NULL," +
                "`type` VARCHAR(50) NOT NULL," +
                "`icon` VARCHAR(50)," +
                "`balance` DOUBLE NOT NULL," +
                "`color` VARCHAR(50)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        stmt.execute("CREATE TABLE IF NOT EXISTS `categories` (" +
                "`id` VARCHAR(100) NOT NULL PRIMARY KEY," +
                "`name` VARCHAR(255) NOT NULL," +
                "`type` VARCHAR(50) NOT NULL," +
                "`icon` VARCHAR(50)," +
                "`color` VARCHAR(50)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        stmt.execute("CREATE TABLE IF NOT EXISTS `transactions` (" +
                "`id` VARCHAR(100) NOT NULL PRIMARY KEY," +
                "`type` VARCHAR(50) NOT NULL," +
                "`category_id` VARCHAR(100)," +
                "`account_id` VARCHAR(100)," +
                "`to_account_id` VARCHAR(100)," +
                "`amount` DOUBLE NOT NULL," +
                "`date` VARCHAR(50) NOT NULL," +
                "`note` TEXT," +
                "`description` TEXT," +
                "`receipt_path` VARCHAR(500)," +
                "`recurring_rule_id` VARCHAR(100)," +
                "INDEX `idx_date` (`date`)," +
                "INDEX `idx_account` (`account_id`)," +
                "INDEX `idx_category` (`category_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        stmt.execute("CREATE TABLE IF NOT EXISTS `recurring_rules` (" +
                "`id` VARCHAR(100) NOT NULL PRIMARY KEY," +
                "`type` VARCHAR(50) NOT NULL," +
                "`category_id` VARCHAR(100)," +
                "`account_id` VARCHAR(100)," +
                "`to_account_id` VARCHAR(100)," +
                "`amount` DOUBLE NOT NULL," +
                "`description` TEXT," +
                "`frequency` VARCHAR(50) NOT NULL," +
                "`timing` VARCHAR(50) NOT NULL," +
                "`start_date` VARCHAR(50) NOT NULL," +
                "`is_active` INT NOT NULL," +
                "`last_processed` VARCHAR(50)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        stmt.execute("CREATE TABLE IF NOT EXISTS `budgets` (" +
                "`category_id` VARCHAR(100) NOT NULL PRIMARY KEY," +
                "`default_amount` DOUBLE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        stmt.execute("CREATE TABLE IF NOT EXISTS `budget_overrides` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`category_id` VARCHAR(100)," +
                "`year` INT," +
                "`month` INT," +
                "`amount` DOUBLE," +
                "UNIQUE KEY `uk_cat_year_month` (`category_id`, `year`, `month`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        stmt.execute("CREATE TABLE IF NOT EXISTS `budget_limits` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`category_id` VARCHAR(100)," +
                "`period_type` VARCHAR(50)," +
                "`period_value` VARCHAR(100)," +
                "`amount` DOUBLE," +
                "UNIQUE KEY `uk_cat_period` (`category_id`, `period_type`, `period_value`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        stmt.execute("CREATE TABLE IF NOT EXISTS `settings` (" +
                "`key` VARCHAR(100) NOT NULL PRIMARY KEY," +
                "`value` TEXT" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        insertDefaultData(stmt);
    }

    private static void initSQLite(Statement stmt) throws SQLException {
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
                "period_type TEXT," +
                "period_value TEXT," +
                "amount REAL," +
                "FOREIGN KEY(category_id) REFERENCES categories(id)," +
                "UNIQUE(category_id, period_type, period_value)" +
                ");";

        String createSettingsTable = "CREATE TABLE IF NOT EXISTS settings (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT" +
                ");";

        stmt.execute(createAccountsTable);
        stmt.execute(createCategoryTable);
        stmt.execute(createTransactionTable);
        stmt.execute(createRecurringRulesTable);
        stmt.execute(createBudgetTable);
        stmt.execute(createBudgetOverridesTable);
        stmt.execute(createBudgetLimitsTable);
        stmt.execute(createSettingsTable);
        insertDefaultData(stmt);
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
