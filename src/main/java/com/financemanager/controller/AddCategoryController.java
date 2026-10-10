package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Category;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class AddCategoryController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtIcon;
    @FXML private ColorPicker colorPicker;

    private String editId = null;

    @FXML
    public void initialize() {
        cmbType.getItems().addAll("expense", "income");
        cmbType.getSelectionModel().selectFirst();
        colorPicker.setValue(Color.valueOf("#4CAF50"));
    }

    public void setCategory(Category cat) {
        this.editId = cat.getId();
        txtName.setText(cat.getName());
        cmbType.getSelectionModel().select(cat.getType());
        txtIcon.setText(cat.getIcon());
        colorPicker.setValue(Color.valueOf(cat.getColor()));
    }

    @FXML
    private void saveCategory() {
        try {
            String name = txtName.getText();
            if (name == null || name.trim().isEmpty()) {
                showAlert("Validation Error", "Category name is required.", javafx.scene.control.Alert.AlertType.WARNING);
                return;
            }
            
            String type = cmbType.getValue();
            String icon = txtIcon.getText();
            String color = String.format("#%02X%02X%02X",
                (int)(colorPicker.getValue().getRed() * 255),
                (int)(colorPicker.getValue().getGreen() * 255),
                (int)(colorPicker.getValue().getBlue() * 255));

            Category cat = new Category(editId, name, type, icon, color);
            if (editId != null) {
                DataStore.getInstance().updateCategory(cat);
            } else {
                DataStore.getInstance().addCategory(cat);
            }

            Navigator.goToCategories();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save category: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
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
        Navigator.goToCategories();
    }
}
