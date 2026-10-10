package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label lblDate;
    @FXML private Label lblBalance;
    @FXML private Label lblIncome;
    @FXML private Label lblExpense;
    @FXML private ListView<Transaction> recentTransactionsList;

    @FXML
    public void initialize() {
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        
        recentTransactionsList.setCellFactory(listView -> new TransactionCell(() -> initialize()));
        
        DataStore store = DataStore.getInstance();
        
        double totalBalance = store.getAccounts().stream().mapToDouble(a -> a.getBalance()).sum();
        lblBalance.setText(String.format("₹ %.2f", totalBalance));
        
        String thisMonthPrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Transaction> txns = store.getTransactions();
        
        double income = 0;
        double expense = 0;
        
        recentTransactionsList.getItems().clear();
        int count = 0;
        
        for (Transaction t : txns) {
            if (t.getDate().startsWith(thisMonthPrefix)) {
                if ("income".equals(t.getType())) income += t.getAmount();
                if ("expense".equals(t.getType())) expense += t.getAmount();
            }
            
            if (count < 5) {
                recentTransactionsList.getItems().add(t);
                count++;
            }
        }
        
        lblIncome.setText(String.format("₹ %.2f", income));
        lblExpense.setText(String.format("₹ %.2f", expense));
    }
}
