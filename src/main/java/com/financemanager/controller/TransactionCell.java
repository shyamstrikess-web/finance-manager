package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Account;
import com.financemanager.model.Category;
import com.financemanager.model.Transaction;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TransactionCell extends ListCell<Transaction> {
    private HBox content;
    private Label lblIcon;
    private Label lblName;
    private Label lblDate;
    private Label lblAmount;
    private Runnable onDelete;

    public TransactionCell(Runnable onDelete) {
        super();
        this.onDelete = onDelete;
        
        lblIcon = new Label();
        lblIcon.getStyleClass().add("tx-icon");
        lblIcon.setMinSize(44, 44);
        lblIcon.setPrefSize(44, 44);
        lblIcon.setMaxSize(44, 44);
        lblIcon.setAlignment(Pos.CENTER);
        
        lblName = new Label();
        lblName.getStyleClass().add("tx-name");
        
        lblDate = new Label();
        lblDate.getStyleClass().add("tx-date");
        
        VBox details = new VBox(2, lblName, lblDate);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        lblAmount = new Label();
        lblAmount.getStyleClass().add("tx-amount");
        
        content = new HBox(16, lblIcon, details, lblAmount);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getStyleClass().add("tx-item");
        
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("Delete Transaction");
        deleteItem.setOnAction(event -> {
            Transaction tx = getItem();
            if (tx != null) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setHeaderText("Delete this transaction?");
                alert.setContentText("Amount: ₹ " + tx.getAmount() + "\nThis action cannot be undone.");
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        DataStore.getInstance().deleteTransaction(tx.getId());
                        if (this.onDelete != null) this.onDelete.run();
                    }
                });
            }
        });
        contextMenu.getItems().add(deleteItem);
        setContextMenu(contextMenu);
        
        setOnMouseClicked(ev -> {
            if (ev.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                Transaction tx = getItem();
                if (tx != null) {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(com.financemanager.App.class.getResource("fxml/AddTransaction.fxml"));
                        javafx.scene.Parent root = loader.load();
                        AddTransactionController controller = loader.getController();
                        controller.setTransaction(tx);
                        Navigator.getMainController().pushView(root);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void updateItem(Transaction tx, boolean empty) {
        super.updateItem(tx, empty);
        
        if (empty || tx == null) {
            setGraphic(null);
            setText(null);
        } else {
            DataStore store = DataStore.getInstance();
            Category cat = store.getCategory(tx.getCategoryId());
            Account acc = store.getAccount(tx.getAccountId());
            
            String icon = cat != null ? cat.getIcon() : ("transfer".equals(tx.getType()) ? "🔄" : "❓");
            String catName = cat != null ? cat.getName() : ("transfer".equals(tx.getType()) ? "Transfer" : "Unknown");
            String accName = acc != null ? acc.getName() : "";
            
            lblIcon.setText(icon);
            
            String desc = tx.getDescription() != null && !tx.getDescription().isEmpty() ? " — " + tx.getDescription() : "";
            lblName.setText(catName + desc);
            
            String subtext = tx.getDate() + (accName.isEmpty() ? "" : " · " + accName);
            if ("transfer".equals(tx.getType())) {
                Account toAcc = store.getAccount(tx.getToAccountId());
                if (toAcc != null) subtext += " \u2192 " + toAcc.getName();
            }
            lblDate.setText(subtext);
            
            String amountPrefix = "income".equals(tx.getType()) ? "+" : ("expense".equals(tx.getType()) ? "-" : "");
            lblAmount.setText(amountPrefix + String.format("₹ %.2f", tx.getAmount()));
            
            lblAmount.getStyleClass().removeAll("tx-amount-income", "tx-amount-expense", "tx-amount-transfer");
            lblAmount.getStyleClass().add("tx-amount-" + tx.getType());
            
            setGraphic(content);
        }
    }
}
