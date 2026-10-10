package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Category;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class CategoriesController {

    @FXML private FlowPane categoriesGrid;

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        categoriesGrid.getChildren().clear();
        DataStore store = DataStore.getInstance();
        List<Category> categories = store.getCategories();
        
        for (Category c : categories) {
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("category-item");
            card.setPrefWidth(120);
            card.setPrefHeight(120);
            
            Label icon = new Label(c.getIcon());
            icon.setStyle("-fx-font-size: 32px;");
            
            Label name = new Label(c.getName());
            name.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-secondary;");
            
            card.getChildren().addAll(icon, name);
            
            javafx.scene.control.ContextMenu ctxMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem delItem = new javafx.scene.control.MenuItem("Delete Category");
            delItem.setOnAction(ev -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setHeaderText("Delete category " + c.getName() + "?");
                alert.setContentText("This action cannot be undone.");
                alert.showAndWait().ifPresent(res -> {
                    if (res == javafx.scene.control.ButtonType.OK) {
                        store.deleteCategory(c.getId());
                        loadCategories();
                    }
                });
            });
            ctxMenu.getItems().add(delItem);
            
            card.setOnContextMenuRequested(ev -> ctxMenu.show(card, ev.getScreenX(), ev.getScreenY()));
            
            card.setOnMouseClicked(ev -> {
                if (ev.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(com.financemanager.App.class.getResource("fxml/AddCategory.fxml"));
                        javafx.scene.Parent root = loader.load();
                        AddCategoryController controller = loader.getController();
                        controller.setCategory(c);
                        Navigator.getMainController().pushView(root);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            categoriesGrid.getChildren().add(card);
        }

        // Add Category Button
        VBox addCard = new VBox(8);
        addCard.setAlignment(Pos.CENTER);
        addCard.setPrefWidth(120);
        addCard.setPrefHeight(120);
        addCard.setStyle("-fx-border-color: -border-subtle; -fx-border-style: dashed; -fx-cursor: hand; -fx-border-radius: 12px;");
        Label plusIcon = new Label("+");
        plusIcon.setStyle("-fx-font-size: 32px; -fx-text-fill: -text-secondary;");
        Label addName = new Label("Add Category");
        addName.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary;");
        addCard.getChildren().addAll(plusIcon, addName);
        
        addCard.setOnMouseClicked(e -> {
            try {
                com.financemanager.App.setRoot("fxml/AddCategory");
            } catch (Exception ex) {}
        });
        
        categoriesGrid.getChildren().add(addCard);
    }
}
