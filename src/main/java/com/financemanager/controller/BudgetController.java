package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.engine.BudgetEngine;
import com.financemanager.model.Budget;
import com.financemanager.model.Category;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.util.List;

public class BudgetController {

    @FXML private Label lblYear;
    @FXML private Label lblTotalBudgeted;
    @FXML private Label lblTotalSpent;
    @FXML private FlowPane budgetGrid;

    private int currentYear;
    private int currentMonth;

    @FXML
    public void initialize() {
        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();
        loadBudgets();
    }

    @FXML
    private void prevYear() {
        currentYear--;
        loadBudgets();
    }

    @FXML
    private void nextYear() {
        currentYear++;
        loadBudgets();
    }

    @FXML
    private void addCustomBudget() {
        try {
            com.financemanager.App.setRoot("fxml/AddCustomBudget");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBudgets() {
        lblYear.setText(String.valueOf(currentYear));
        budgetGrid.getChildren().clear();
        
        DataStore store = DataStore.getInstance();
        List<Category> categories = store.getCategories();
        
        double totalBudget = 0;
        double totalSpent = 0;
        
        for (Category cat : categories) {
            BudgetEngine.BudgetData data = BudgetEngine.getBudgetForMonth(cat.getId(), currentYear, currentMonth);
            if (data == null || data.limit <= 0) continue;
            
            totalBudget += data.limit;
            totalSpent += data.spent;
            
            VBox card = new VBox(8);
            card.getStyleClass().add("budget-card");
            card.setPrefWidth(300);
            
            Label title = new Label(cat.getIcon() + " " + cat.getName());
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            
            Label amounts = new Label(String.format("₹ %.2f / ₹ %.2f", data.spent, data.limit));
            amounts.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-secondary;");
            
            // Progress bar
            HBox progressBg = new HBox();
            progressBg.getStyleClass().add("budget-progress-bg");
            
            javafx.scene.layout.Region progressFill = new javafx.scene.layout.Region();
            String statusClass = data.percentage >= 100 ? "budget-progress-fill-over" : (data.percentage >= 75 ? "budget-progress-fill-warning" : "budget-progress-fill-safe");
            progressFill.getStyleClass().add(statusClass);
            
            // Calculate width percentage
            double widthPct = Math.min(data.percentage, 100) / 100.0;
            // Since HBox width isn't known yet, we can use bind or prefWidth based on parent (300px width card - 40px padding = 260px)
            progressFill.setPrefWidth(260 * widthPct);
            progressFill.setPrefHeight(8);
            
            progressBg.getChildren().add(progressFill);
            
            Label remaining = new Label();
            if (data.remaining >= 0) {
                remaining.setText(String.format("₹ %.2f remaining", data.remaining));
                remaining.setStyle("-fx-font-size: 12px; -fx-text-fill: -success;");
            } else {
                remaining.setText(String.format("₹ %.2f over budget", Math.abs(data.remaining)));
                remaining.setStyle("-fx-font-size: 12px; -fx-text-fill: -danger;");
            }
            
            card.getChildren().addAll(title, amounts, progressBg, remaining);
            
            javafx.scene.control.ContextMenu ctxMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem delItem = new javafx.scene.control.MenuItem("Remove Monthly Limit");
            delItem.setOnAction(ev -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setHeaderText("Remove budget limit for " + cat.getName() + " in " + currentYear + "-" + String.format("%02d", currentMonth) + "?");
                alert.setContentText("This action cannot be undone.");
                alert.showAndWait().ifPresent(res -> {
                    if (res == javafx.scene.control.ButtonType.OK) {
                        store.deleteBudgetLimit(cat.getId(), "MONTH", currentYear + "-" + String.format("%02d", currentMonth));
                        loadBudgets();
                    }
                });
            });
            ctxMenu.getItems().add(delItem);
            
            card.setOnContextMenuRequested(ev -> ctxMenu.show(card, ev.getScreenX(), ev.getScreenY()));
            
            card.setOnMouseClicked(ev -> {
                if (ev.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(com.financemanager.App.class.getResource("fxml/AddCustomBudget.fxml"));
                        javafx.scene.Parent root = loader.load();
                        AddCustomBudgetController controller = loader.getController();
                        controller.setEditData(cat.getId(), data.limit, currentYear, currentMonth);
                        Navigator.getMainController().pushView(root);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            budgetGrid.getChildren().add(card);
        }
        
        lblTotalBudgeted.setText(String.format("Monthly Budgeted: ₹ %.2f", totalBudget));
        lblTotalSpent.setText(String.format("Monthly Spent: ₹ %.2f", totalSpent));
    }
}
