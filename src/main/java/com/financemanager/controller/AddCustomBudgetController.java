package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Category;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.util.List;
import java.util.stream.Collectors;

public class AddCustomBudgetController {

    @FXML private ComboBox<Category> categorySelect;
    @FXML private ComboBox<String> cmbPeriodType;
    @FXML private ComboBox<String> cmbYear;
    @FXML private ComboBox<String> cmbMonth;
    @FXML private ComboBox<String> cmbWeek;
    @FXML private TextField txtAmount;

    @FXML
    public void initialize() {
        DataStore store = DataStore.getInstance();
        List<Category> expenseCats = store.getCategories().stream().filter(c -> "expense".equals(c.getType())).collect(Collectors.toList());
        categorySelect.getItems().addAll(expenseCats);
        if (!expenseCats.isEmpty()) categorySelect.getSelectionModel().selectFirst();

        cmbPeriodType.getItems().addAll("YEAR", "MONTH", "WEEK");
        cmbPeriodType.getSelectionModel().select("MONTH");

        cmbYear.getItems().addAll("2024", "2025", "2026", "2027", "2028");
        cmbYear.getSelectionModel().select("2026");

        cmbMonth.getItems().addAll("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        cmbMonth.getSelectionModel().select("01");

        cmbWeek.getItems().addAll("W1", "W2", "W3", "W4", "W5");
        cmbWeek.getSelectionModel().select("W1");

        cmbPeriodType.setOnAction(e -> updateVisibilities());
        updateVisibilities();
    }

    public void setEditData(String categoryId, double amount, int year, int month) {
        // Create a stub Category with just the ID — equals() matches by ID so selection works
        categorySelect.getSelectionModel().select(new Category(categoryId, "", "", "", ""));
        txtAmount.setText(String.valueOf(amount));
        cmbPeriodType.getSelectionModel().select("MONTH");
        cmbYear.getSelectionModel().select(String.valueOf(year));
        cmbMonth.getSelectionModel().select(String.format("%02d", month));
    }

    private void updateVisibilities() {
        String type = cmbPeriodType.getValue();
        if ("YEAR".equals(type)) {
            cmbMonth.setVisible(false);
            cmbWeek.setVisible(false);
        } else if ("MONTH".equals(type)) {
            cmbMonth.setVisible(true);
            cmbWeek.setVisible(false);
        } else if ("WEEK".equals(type)) {
            cmbMonth.setVisible(true);
            cmbWeek.setVisible(true);
        }
    }

    @FXML
    private void saveLimit() {
        try {
            Category cat = categorySelect.getValue();
            if (cat == null) {
                showAlert("Validation Error", "Please select a category.", javafx.scene.control.Alert.AlertType.WARNING);
                return;
            }

            double amount = 0.0;
            try {
                amount = Double.parseDouble(txtAmount.getText());
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Please enter a valid numeric budget amount.", javafx.scene.control.Alert.AlertType.WARNING);
                return;
            }

            String type = cmbPeriodType.getValue();
            String value = cmbYear.getValue();

            if ("MONTH".equals(type)) {
                value += "-" + cmbMonth.getValue();
            } else if ("WEEK".equals(type)) {
                value += "-" + cmbMonth.getValue() + "-" + cmbWeek.getValue();
            }

            DataStore.getInstance().setBudgetLimit(cat.getId(), type, value, amount);
            Navigator.goToBudget();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save budget limit: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void cancel() {
        Navigator.goToBudget();
    }
}
