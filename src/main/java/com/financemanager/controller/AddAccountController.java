package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Account;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class AddAccountController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtIcon;
    @FXML private ColorPicker colorPicker;
    @FXML private TextField txtBalance;

    private String editId = null;

    @FXML
    public void initialize() {
        cmbType.getItems().addAll("bank", "cash", "credit", "investment");
        cmbType.getSelectionModel().selectFirst();
        colorPicker.setValue(Color.valueOf("#4A90E2"));
    }

    public void setAccount(Account account) {
        this.editId = account.getId();
        txtName.setText(account.getName());
        cmbType.getSelectionModel().select(account.getType());
        txtIcon.setText(account.getIcon());
        colorPicker.setValue(Color.valueOf(account.getColor()));
        txtBalance.setText(String.valueOf(account.getBalance()));
    }

    @FXML
    private void saveAccount() {
        try {
            String name = txtName.getText();
            if (name == null || name.trim().isEmpty()) {
                showAlert("Validation Error", "Account name is required.", javafx.scene.control.Alert.AlertType.WARNING);
                return;
            }

            String type = cmbType.getValue();
            String icon = txtIcon.getText();
            String color = String.format("#%02X%02X%02X",
                (int)(colorPicker.getValue().getRed() * 255),
                (int)(colorPicker.getValue().getGreen() * 255),
                (int)(colorPicker.getValue().getBlue() * 255));
            
            double balance = 0.0;
            try {
                balance = Double.parseDouble(txtBalance.getText());
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Please enter a valid numeric balance.", javafx.scene.control.Alert.AlertType.WARNING);
                return;
            }

            Account account = new Account(editId, name, type, icon, balance, color);
            if (editId != null) {
                DataStore.getInstance().updateAccount(account);
            } else {
                DataStore.getInstance().addAccount(account);
            }

            Navigator.goToAccounts();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save account: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
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
        Navigator.goToAccounts();
    }
}
