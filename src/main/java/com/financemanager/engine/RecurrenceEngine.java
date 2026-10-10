package com.financemanager.engine;

import com.financemanager.db.DataStore;
import com.financemanager.model.RecurringRule;
import com.financemanager.model.Transaction;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecurrenceEngine {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void processRecurring() {
        DataStore store = DataStore.getInstance();
        List<RecurringRule> rules = store.getRecurringRules();
        LocalDate today = LocalDate.now();

        for (RecurringRule rule : rules) {
            if (!rule.isActive()) continue;

            LocalDate processDate = rule.getLastProcessed() != null 
                ? LocalDate.parse(rule.getLastProcessed(), formatter) 
                : LocalDate.parse(rule.getStartDate(), formatter);

            // If never processed and start date is due
            if (rule.getLastProcessed() == null && !processDate.isAfter(today)) {
                createTransaction(store, rule, processDate);
                rule.setLastProcessed(processDate.format(formatter));
            }

            while (true) {
                LocalDate nextOccurrence = getNextOccurrence(rule, processDate);
                if (!nextOccurrence.isAfter(today)) {
                    createTransaction(store, rule, nextOccurrence);
                    processDate = nextOccurrence;
                    rule.setLastProcessed(processDate.format(formatter));
                } else {
                    break;
                }
            }
            
            if (rule.getLastProcessed() != null) {
                store.updateRuleLastProcessed(rule.getId(), rule.getLastProcessed());
            }
        }
    }

    private static void createTransaction(DataStore store, RecurringRule rule, LocalDate date) {
        Transaction t = new Transaction(
            null, 
            rule.getType(), 
            rule.getCategoryId(), 
            rule.getAccountId(), 
            rule.getToAccountId(), 
            rule.getAmount(), 
            date.format(formatter), 
            null, 
            rule.getDescription(), 
            null, 
            rule.getId()
        );
        store.addTransaction(t);
    }

    private static LocalDate getNextOccurrence(RecurringRule rule, LocalDate fromDate) {
        LocalDate next = fromDate;
        
        if ("first-of-month".equals(rule.getTiming())) {
            next = next.withDayOfMonth(1);
        }

        switch (rule.getFrequency()) {
            case "daily": next = next.plusDays(1); break;
            case "weekdays": 
                do { next = next.plusDays(1); } while (next.getDayOfWeek().getValue() > 5);
                break;
            case "weekends":
                do { next = next.plusDays(1); } while (next.getDayOfWeek().getValue() < 6);
                break;
            case "weekly": next = next.plusWeeks(1); break;
            case "every-2-weeks": next = next.plusWeeks(2); break;
            case "every-3-weeks": next = next.plusWeeks(3); break;
            case "every-4-weeks": next = next.plusWeeks(4); break;
            case "monthly": next = next.plusMonths(1); break;
            case "annually": next = next.plusYears(1); break;
            default: next = next.plusDays(1);
        }
        return next;
    }
}
