package com.financemanager.controller;

import com.financemanager.App;
import com.financemanager.db.DataStore;
import com.financemanager.model.Account;
import com.financemanager.model.Category;
import com.financemanager.model.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AddTransactionController {

    @FXML private ToggleButton btnExpense;
    @FXML private ToggleButton btnIncome;
    @FXML private ToggleButton btnTransfer;
    @FXML private ComboBox<Account> accountSelect;
    @FXML private ComboBox<Category> categorySelect;
    @FXML private ComboBox<Account> toAccountSelect;
    @FXML private javafx.scene.layout.HBox boxAccount;
    @FXML private javafx.scene.layout.HBox boxToAccount;
    @FXML private javafx.scene.layout.HBox boxCategory;
    @FXML private Label lblAccount;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtAmount;
    @FXML private TextField txtDesc;

    private ToggleGroup typeGroup;

    @FXML
    public void initialize() {
        typeGroup = new ToggleGroup();
        btnExpense.setToggleGroup(typeGroup);
        btnIncome.setToggleGroup(typeGroup);
        btnTransfer.setToggleGroup(typeGroup);
        
        btnExpense.setSelected(true); // Explicitly set it here

        DataStore store = DataStore.getInstance();
        
        List<Account> accounts = store.getAccounts();
        accountSelect.getItems().addAll(accounts);
        toAccountSelect.getItems().addAll(accounts);
        if (!accounts.isEmpty()) {
            accountSelect.getSelectionModel().selectFirst();
            if (accounts.size() > 1) toAccountSelect.getSelectionModel().select(1);
            else toAccountSelect.getSelectionModel().selectFirst();
        }

        loadCategories("expense");
        
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            } else {
                ToggleButton selected = (ToggleButton) newVal;
                String type = selected.getText().toLowerCase();
                
                if ("transfer".equals(type)) {
                    if (boxCategory != null) { boxCategory.setVisible(false); boxCategory.setManaged(false); }
                    if (boxToAccount != null) { boxToAccount.setVisible(true); boxToAccount.setManaged(true); }
                    if (lblAccount != null) lblAccount.setText("From Account");
                } else {
                    if (boxCategory != null) { boxCategory.setVisible(true); boxCategory.setManaged(true); }
                    if (boxToAccount != null) { boxToAccount.setVisible(false); boxToAccount.setManaged(false); }
                    if (lblAccount != null) lblAccount.setText("Account");
                    loadCategories(type);
                }
            }
        });

        datePicker.setValue(LocalDate.now());
    }

    private void loadCategories(String type) {
        categorySelect.getItems().clear();
        DataStore store = DataStore.getInstance();
        store.getCategories().stream()
             .filter(c -> c.getType().equals(type) || "transfer".equals(type))
             .forEach(categorySelect.getItems()::add);
        
        if (!categorySelect.getItems().isEmpty()) {
            categorySelect.getSelectionModel().selectFirst();
        }
    }

    @FXML private javafx.scene.layout.VBox keyboardContainer;
    @FXML private javafx.scene.control.Label lblKeyboardTitle;
    @FXML private javafx.scene.layout.StackPane keyboardContent;

    @FXML
    public void closeCustomKeyboard() {
        keyboardContainer.setVisible(false);
        keyboardContainer.setManaged(false);
        keyboardContent.getChildren().clear();
    }

    // --- Mirrored from Flutter add_controller.dart ---
    public void changeTab(int index) {
        if (index == 0) btnIncome.setSelected(true);
        else if (index == 1) btnExpense.setSelected(true);
        else btnTransfer.setSelected(true);
        loadCategories(((ToggleButton) typeGroup.getSelectedToggle()).getText().toLowerCase());
    }

    public void showAmountKeyboard() {
        keyboardContainer.setVisible(true);
        keyboardContainer.setManaged(true);
        lblKeyboardTitle.setText("Amount");
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(2); grid.setVgap(2);
        String[][] keys = {
            {"1", "2", "3", "DEL"},
            {"4", "5", "6", "-"},
            {"7", "8", "9", "CALC"},
            {"", "0", ".", "DONE"}
        };
        for(int r=0; r<4; r++) {
            for(int c=0; c<4; c++) {
                String k = keys[r][c];
                if(k.isEmpty()) continue;
                Button b = new Button(k);
                b.setPrefSize(80, 60);
                b.setOnAction(e -> {
                    if ("DONE".equals(k)) closeCustomKeyboard();
                    else if ("DEL".equals(k)) {
                        String txt = txtAmount.getText();
                        if (!txt.isEmpty()) txtAmount.setText(txt.substring(0, txt.length()-1));
                    }
                    else if ("CALC".equals(k)) {} // Placeholder for calculator
                    else onKeypadButtonPressed(k);
                });
                grid.add(b, c, r);
            }
        }
        keyboardContent.getChildren().setAll(grid);
    }

    public void onKeypadButtonPressed(String v) {
        txtAmount.appendText(v);
    }
    
    public void resetForNewTransaction() {
        txtAmount.clear();
        txtDesc.clear();
        datePicker.setValue(LocalDate.now());
    }

    private String editId = null;

    public void setTransaction(Transaction tx) {
        this.editId = tx.getId();
        txtAmount.setText(String.valueOf(tx.getAmount()));
        txtDesc.setText(tx.getDescription());
        try {
            String raw = tx.getDate();
            if (raw != null) {
                if (raw.contains(" ")) raw = raw.split(" ")[0];
                if (raw.contains("T")) raw = raw.split("T")[0];
                datePicker.setValue(LocalDate.parse(raw));
            }
        } catch (Exception e) {
            datePicker.setValue(LocalDate.now());
        }
        
        if ("income".equals(tx.getType())) changeTab(0);
        else if ("expense".equals(tx.getType())) changeTab(1);
        else changeTab(2);

        DataStore store = DataStore.getInstance();
        if (tx.getAccountId() != null) {
            store.getAccounts().stream().filter(a -> a.getId().equals(tx.getAccountId())).findFirst().ifPresent(accountSelect::setValue);
        }
        if (tx.getCategoryId() != null) {
            store.getCategories().stream().filter(c -> c.getId().equals(tx.getCategoryId())).findFirst().ifPresent(categorySelect::setValue);
        }
        if (tx.getToAccountId() != null) {
            store.getAccounts().stream().filter(a -> a.getId().equals(tx.getToAccountId())).findFirst().ifPresent(toAccountSelect::setValue);
        }
    }

    @FXML
    private void inserTransaction() {
        try {
            if (txtAmount.getText() == null || txtAmount.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Amount is required.", Alert.AlertType.WARNING);
                return;
            }
            
            double amt;
            try {
                String amountText = txtAmount.getText().replace(",", "").trim();
                amt = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Please enter a valid numeric amount.", Alert.AlertType.WARNING);
                return;
            }
            
            javafx.scene.control.Toggle selectedToggle = typeGroup.getSelectedToggle();
            if (selectedToggle == null) {
                showAlert("Validation Error", "Please select a transaction type.", Alert.AlertType.WARNING);
                return;
            }
            String type = ((ToggleButton) selectedToggle).getText().toLowerCase();
            
            Account acc = accountSelect.getValue();
            Category cat = categorySelect.getValue();
            Account toAcc = toAccountSelect.getValue();
            
            if (acc == null) {
                showAlert("Validation Error", "From Account is required.", Alert.AlertType.WARNING);
                return;
            }
            
            if ("transfer".equals(type)) {
                if (toAcc == null) {
                    showAlert("Validation Error", "To Account is required for transfers.", Alert.AlertType.WARNING);
                    return;
                }
                if (acc.getId().equals(toAcc.getId())) {
                    showAlert("Validation Error", "Cannot transfer to the same account.", Alert.AlertType.WARNING);
                    return;
                }
            } else {
                if (cat == null) {
                    showAlert("Validation Error", "Category is required.", Alert.AlertType.WARNING);
                    return;
                }
            }

            LocalDate selectedDate = datePicker.getValue();
            String dateStr = selectedDate != null ? selectedDate.toString() : LocalDate.now().toString();
            // Append time so it sorts ABOVE older Flutter transactions with the same date
            dateStr += " " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

            Transaction t = new Transaction(
                editId, 
                type, 
                ("transfer".equals(type)) ? null : (cat != null ? cat.getId() : null), 
                acc.getId(), 
                ("transfer".equals(type) && toAcc != null) ? toAcc.getId() : null, 
                amt, 
                dateStr, 
                null, 
                txtDesc.getText(), 
                null, 
                null
            );
            
            if (editId != null) {
                DataStore.getInstance().updateTransaction(t);
            } else {
                DataStore.getInstance().addTransaction(t);
            }
            
            // Show a visual success alert so we know 100% that the save logic completed without errors
            showAlert("Success", "Transaction saved successfully! Amount: " + amt, Alert.AlertType.INFORMATION);
            
            // Check Budget Overrun
            if ("expense".equals(type) && cat != null) {
                com.financemanager.engine.BudgetEngine.BudgetData data = com.financemanager.engine.BudgetEngine.getBudgetForMonth(
                    cat.getId(), datePicker.getValue().getYear(), datePicker.getValue().getMonthValue());
                
                if (data != null && data.limit > 0 && data.spent > data.limit) {
                    showAlert("Budget Exceeded", 
                              "Warning! You have exceeded your budget for " + cat.getName() + ".\n\n" +
                              "Limit: ₹ " + String.format("%.2f", data.limit) + "\n" +
                              "Spent: ₹ " + String.format("%.2f", data.spent), 
                              Alert.AlertType.WARNING);
                } else if (data != null && data.limit > 0 && data.percentage >= 90) {
                    showAlert("Budget Warning", 
                              "You are approaching your budget limit for " + cat.getName() + ".\n" +
                              "You have spent " + String.format("%.0f", data.percentage) + "% of your budget.", 
                              Alert.AlertType.INFORMATION);
                }
            }
            
            Navigator.goToTransactions();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred while saving: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        try {
            if (btnExpense != null && btnExpense.getScene() != null) {
                alert.initOwner(btnExpense.getScene().getWindow());
            }
        } catch (Exception e) {}
        alert.showAndWait();
    }

    @FXML
    private void cancel() {
        Navigator.goToTransactions();
    }
}
