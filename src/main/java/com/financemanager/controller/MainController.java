package com.financemanager.controller;

import com.financemanager.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnBudget;
    @FXML private Button btnAccounts;
    @FXML private Button btnCategories;

    private Button currentActiveBtn;

    @FXML
    public void initialize() {
        Navigator.setMainController(this);
        showDashboard();
    }

    private void setActiveButton(Button btn) {
        if (currentActiveBtn != null) {
            currentActiveBtn.getStyleClass().remove("nav-item-active");
        }
        currentActiveBtn = btn;
        if (!btn.getStyleClass().contains("nav-item-active")) {
            btn.getStyleClass().add("nav-item-active");
        }
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Push any already-loaded Parent node into contentArea (used by edit screens). */
    public void pushView(Parent view) {
        contentArea.getChildren().setAll(view);
    }

    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        loadView("fxml/Dashboard");
    }

    @FXML
    private void showTransactions() {
        setActiveButton(btnTransactions);
        loadView("fxml/Transactions");
    }

    @FXML
    private void showBudget() {
        setActiveButton(btnBudget);
        loadView("fxml/Budget");
    }

    @FXML
    private void showAccounts() {
        setActiveButton(btnAccounts);
        loadView("fxml/Accounts");
    }

    @FXML
    private void showCategories() {
        setActiveButton(btnCategories);
        loadView("fxml/Categories");
    }

    // Public aliases used by Navigator
    public void showDashboardPublic()    { showDashboard(); }
    public void showTransactionsPublic() { setActiveButton(btnTransactions); loadView("fxml/Transactions"); }
    public void showAccountsPublic()     { setActiveButton(btnAccounts);     loadView("fxml/Accounts"); }
    public void showCategoriesPublic()   { setActiveButton(btnCategories);   loadView("fxml/Categories"); }
    public void showBudgetPublic()       { setActiveButton(btnBudget);       loadView("fxml/Budget"); }

    @FXML
    private void goToAddTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/AddTransaction.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
