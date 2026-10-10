package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Account;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class AccountsController {

    @FXML private Label lblTotal;
    @FXML private FlowPane accountsGrid;

    @FXML
    public void initialize() {
        loadAccounts();
    }

    private void loadAccounts() {
        accountsGrid.getChildren().clear();
        DataStore store = DataStore.getInstance();
        List<Account> accounts = store.getAccounts();
        
        double total = 0;
        
        for (Account a : accounts) {
            total += a.getBalance();
            
            VBox card = new VBox(8);
            card.getStyleClass().add("account-card");
            card.setPrefWidth(280);
            card.setStyle(card.getStyle() + "; -fx-border-width: 4 0 0 0; -fx-border-color: " + a.getColor() + ";");
            
            Label iconName = new Label(a.getIcon() + " " + a.getName());
            iconName.setStyle("-fx-font-size: 16px; -fx-text-fill: -text-secondary;");
            
            Label type = new Label(a.getType().toUpperCase());
            type.setStyle("-fx-font-size: 10px; -fx-text-fill: " + a.getColor() + ";");
            
            Label balance = new Label(String.format("₹ %.2f", a.getBalance()));
            balance.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            card.getChildren().addAll(type, iconName, balance);
            
            javafx.scene.control.ContextMenu ctxMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem delItem = new javafx.scene.control.MenuItem("Delete Account");
            delItem.setOnAction(ev -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setHeaderText("Delete account " + a.getName() + "?");
                alert.setContentText("This action cannot be undone.");
                alert.showAndWait().ifPresent(res -> {
                    if (res == javafx.scene.control.ButtonType.OK) {
                        store.deleteAccount(a.getId());
                        loadAccounts();
                    }
                });
            });
            ctxMenu.getItems().add(delItem);
            
            // Show ContextMenu on right click
            card.setOnContextMenuRequested(ev -> ctxMenu.show(card, ev.getScreenX(), ev.getScreenY()));
            
            card.setOnMouseClicked(ev -> {
                if (ev.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(com.financemanager.App.class.getResource("fxml/AddAccount.fxml"));
                        javafx.scene.Parent root = loader.load();
                        AddAccountController controller = loader.getController();
                        controller.setAccount(a);
                        Navigator.getMainController().pushView(root);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            accountsGrid.getChildren().add(card);
        }

        // Add Account Button
        javafx.scene.control.Button btnAdd = new javafx.scene.control.Button("+ Add Account");
        btnAdd.setPrefWidth(280);
        btnAdd.setPrefHeight(100);
        btnAdd.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-border-color: -border-subtle; -fx-border-style: dashed; -fx-cursor: hand; -fx-text-fill: -text-secondary;");
        btnAdd.setOnAction(e -> {
            try {
                com.financemanager.App.setRoot("fxml/AddAccount");
            } catch (Exception ex) {}
        });
        accountsGrid.getChildren().add(btnAdd);
        
        lblTotal.setText(String.format("Total: ₹ %.2f", total));
    }
}
