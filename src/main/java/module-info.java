module com.financemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires atlantafx.base;

    opens com.financemanager to javafx.fxml;
    opens com.financemanager.controller to javafx.fxml;
    
    exports com.financemanager;
    exports com.financemanager.controller;
    exports com.financemanager.model;
    exports com.financemanager.db;
}
