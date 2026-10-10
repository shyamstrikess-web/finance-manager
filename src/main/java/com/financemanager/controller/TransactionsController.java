package com.financemanager.controller;

import com.financemanager.db.DataStore;
import com.financemanager.model.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TransactionsController {

    @FXML private ComboBox<String> yearFilter;
    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> weekFilter;
    @FXML private ComboBox<String> typeFilter;
    
    @FXML private ListView<Transaction> transactionsList;
    @FXML private javafx.scene.control.DatePicker calendarPicker;
    @FXML private ListView<Transaction> calendarList;
    @FXML private ListView<String> monthlyList;
    @FXML private javafx.scene.control.Label lblTotalIncome;
    @FXML private javafx.scene.control.Label lblTotalExpense;
    @FXML private ListView<String> categoryBreakdownList;
    @FXML private ListView<Transaction> noteList;

    @FXML
    public void initialize() {
        yearFilter.getItems().addAll("All", "2024", "2025", "2026", "2027", "2028");
        yearFilter.getSelectionModel().select("All");
        
        monthFilter.getItems().addAll("All", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        monthFilter.getSelectionModel().select("All");
        
        weekFilter.getItems().addAll("All", "Week 1", "Week 2", "Week 3", "Week 4", "Week 5");
        weekFilter.getSelectionModel().select("All");

        typeFilter.getItems().addAll("All", "Income", "Expense", "Transfer");
        typeFilter.getSelectionModel().selectFirst();
        
        yearFilter.setOnAction(e -> loadData());
        monthFilter.setOnAction(e -> loadData());
        weekFilter.setOnAction(e -> loadData());
        typeFilter.setOnAction(e -> loadData());
        
        if (calendarPicker != null) {
            calendarPicker.setValue(LocalDate.now());
            calendarPicker.setOnAction(e -> loadData());
        }
        
        transactionsList.setCellFactory(listView -> new TransactionCell(() -> loadData()));
        if (calendarList != null) calendarList.setCellFactory(listView -> new TransactionCell(() -> loadData()));
        if (noteList != null) noteList.setCellFactory(listView -> new TransactionCell(() -> loadData()));
        
        loadData();
    }
    
    // --- Mirrored from Flutter trans_controller.dart ---
    public void loadData() {
        transactionsList.getItems().clear();
        if (calendarList != null) calendarList.getItems().clear();
        if (monthlyList != null) monthlyList.getItems().clear();
        if (categoryBreakdownList != null) categoryBreakdownList.getItems().clear();
        if (noteList != null) noteList.getItems().clear();
        
        DataStore store = DataStore.getInstance();
        List<Transaction> txns = store.getTransactions();
        
        String type = typeFilter.getValue().toLowerCase();
        String year = yearFilter.getValue();
        String month = monthFilter.getValue();
        String week = weekFilter.getValue();
        
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        java.util.Map<String, Double> categoryTotals = new java.util.HashMap<>();
        java.util.Map<String, double[]> monthlyTotals = new java.util.TreeMap<>(); // format: yyyy-MM -> [income, expense]

        for (Transaction t : txns) {
            LocalDate txDate;
            try {
                String rawDate = t.getDate();
                if (rawDate == null || rawDate.trim().isEmpty()) {
                    txDate = LocalDate.now();
                } else {
                    // Extract just the YYYY-MM-DD part if there's a time component
                    if (rawDate.contains(" ")) rawDate = rawDate.split(" ")[0];
                    if (rawDate.contains("T")) rawDate = rawDate.split("T")[0];
                    txDate = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                System.out.println("Falling back to today for transaction with invalid date: " + t.getDate());
                txDate = LocalDate.now(); // Fallback so we still render it
            }
            String monthKey = txDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            double amt = t.getAmount();
            monthlyTotals.putIfAbsent(monthKey, new double[2]);
            if ("income".equals(t.getType())) monthlyTotals.get(monthKey)[0] += amt;
            else if ("expense".equals(t.getType())) monthlyTotals.get(monthKey)[1] += amt;
            
            if (!"all".equals(type) && !t.getType().equals(type)) continue;
            
            if (!"All".equals(year) && txDate.getYear() != Integer.parseInt(year)) continue;
            if (!"All".equals(month) && txDate.getMonthValue() != Integer.parseInt(month)) continue;
            
            if (!"All".equals(week)) {
                int weekOfMonth = txDate.get(weekFields.weekOfMonth());
                int selectedWeek = Integer.parseInt(week.replace("Week ", ""));
                if (weekOfMonth != selectedWeek) continue;
            }

            // Add to Daily List
            transactionsList.getItems().add(t);
            
            // Add to Calendar if matches picked date
            if (calendarPicker != null && txDate.equals(calendarPicker.getValue())) {
                calendarList.getItems().add(t);
            }
            
            // Add to Note List if there's a note
            if (t.getNote() != null && !t.getNote().trim().isEmpty()) {
                if (noteList != null) noteList.getItems().add(t);
            }
            
            // Category breakdowns for Total View
            if ("expense".equals(t.getType())) {
                categoryTotals.put(t.getCategoryId(), categoryTotals.getOrDefault(t.getCategoryId(), 0.0) + amt);
            }
        }
        
        if (monthlyList != null) {
            for (java.util.Map.Entry<String, double[]> entry : monthlyTotals.entrySet()) {
                monthlyList.getItems().add(entry.getKey() + " -> Income: ₹ " + String.format("%.2f", entry.getValue()[0]) + " | Expense: ₹ " + String.format("%.2f", entry.getValue()[1]));
            }
        }
        
        if (categoryBreakdownList != null) {
            for (java.util.Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                com.financemanager.model.Category c = store.getCategory(entry.getKey());
                String name = c != null ? c.getIcon() + " " + c.getName() : entry.getKey();
                categoryBreakdownList.getItems().add(name + ": ₹ " + String.format("%.2f", entry.getValue()));
            }
        }
        
        calculateTotals(transactionsList.getItems());
    }

    public void calculateTotals(List<Transaction> list) {
        double income = 0;
        double expense = 0;
        for (Transaction item : list) {
            String typeStr = item.getType().toLowerCase();
            if (typeStr.equals("income")) {
                income += item.getAmount();
            }
            if (typeStr.equals("expense")) {
                expense += item.getAmount();
            }
        }
        if (lblTotalIncome != null) lblTotalIncome.setText(String.format("₹ %.2f", income));
        if (lblTotalExpense != null) lblTotalExpense.setText(String.format("₹ %.2f", expense));
        
        if (transactionsList != null) transactionsList.refresh();
        if (calendarList != null) calendarList.refresh();
        if (monthlyList != null) monthlyList.refresh();
        if (categoryBreakdownList != null) categoryBreakdownList.refresh();
        if (noteList != null) noteList.refresh();
    }

    public void deleteTransaction(String id) {
        // Mirrored function signature (using String instead of int because Java UUIDs are strings)
        loadData();
    }

    public void goToPrevious() {
        // Mirrored method for calendar navigation
    }

    public void goToNext() {
        // Mirrored method for calendar navigation
    }
}
