package com.financemanager.engine;

import com.financemanager.db.DataStore;
import com.financemanager.model.Budget;
import com.financemanager.model.Transaction;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class BudgetEngine {
    
    public static class BudgetData {
        public double limit;
        public boolean isCustom;
        public double spent;
        public double remaining;
        public double percentage;
    }

    public static BudgetData getBudgetForPeriod(String categoryId, String periodType, String periodValue) {
        DataStore store = DataStore.getInstance();
        List<Budget> budgets = store.getBudgets();
        Budget budget = budgets.stream().filter(b -> b.getCategoryId().equals(categoryId)).findFirst().orElse(null);
        if (budget == null) return null;

        Double customLimit = budget.getCustomLimit(periodType, periodValue);
        
        // Fallback to legacy month override if type is MONTH
        if (customLimit == null && "MONTH".equals(periodType)) {
            // Ensure format matches YYYY-MM
            String[] parts = periodValue.split("-");
            if (parts.length == 2) {
                String legacyKey = parts[0] + "-" + String.format("%02d", Integer.parseInt(parts[1]));
                if (budget.getMonthlyOverrides().containsKey(legacyKey)) {
                    customLimit = budget.getMonthlyOverrides().get(legacyKey);
                }
            }
        }
        
        double limit = customLimit != null ? customLimit : budget.getDefaultAmount();
        boolean isCustom = customLimit != null;
        
        double spent = getCategorySpending(categoryId, periodType, periodValue);
        double remaining = limit - spent;
        double percentage = limit > 0 ? (spent / limit) * 100 : 0;
        if (percentage > 100) percentage = 100;

        BudgetData data = new BudgetData();
        data.limit = limit;
        data.isCustom = isCustom;
        data.spent = spent;
        data.remaining = remaining;
        data.percentage = percentage;
        return data;
    }

    public static BudgetData getBudgetForMonth(String categoryId, int year, int month) {
        return getBudgetForPeriod(categoryId, "MONTH", year + "-" + String.format("%02d", month));
    }

    private static double getCategorySpending(String categoryId, String periodType, String periodValue) {
        DataStore store = DataStore.getInstance();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        return store.getTransactions().stream()
            .filter(t -> "expense".equals(t.getType()) && categoryId.equals(t.getCategoryId()))
            .filter(t -> {
                LocalDate txDate;
                try {
                    String raw = t.getDate();
                    if (raw == null) return false;
                    if (raw.contains(" ")) raw = raw.split(" ")[0];
                    if (raw.contains("T")) raw = raw.split("T")[0];
                    txDate = LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    return false;
                }
                
                if ("YEAR".equals(periodType)) {
                    return String.valueOf(txDate.getYear()).equals(periodValue);
                } else if ("MONTH".equals(periodType)) {
                    String[] parts = periodValue.split("-");
                    return txDate.getYear() == Integer.parseInt(parts[0]) && txDate.getMonthValue() == Integer.parseInt(parts[1]);
                } else if ("WEEK".equals(periodType)) {
                    // format expected: "2026-05-W1"
                    String[] parts = periodValue.split("-W");
                    if (parts.length == 2) {
                        String[] ym = parts[0].split("-");
                        int year = Integer.parseInt(ym[0]);
                        int month = Integer.parseInt(ym[1]);
                        int week = Integer.parseInt(parts[1]);
                        
                        return txDate.getYear() == year 
                            && txDate.getMonthValue() == month 
                            && txDate.get(weekFields.weekOfMonth()) == week;
                    }
                }
                return false;
            })
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
}
