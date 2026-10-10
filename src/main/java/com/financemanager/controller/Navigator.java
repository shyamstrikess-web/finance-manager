package com.financemanager.controller;

/**
 * Global navigator that keeps the MainController reference alive.
 * Edit/add screens call Navigator.goBack() instead of App.setRoot("fxml/Main"),
 * so the sidebar stays in place and the correct tab reloads with fresh data.
 */
public class Navigator {

    private static MainController mainController;

    /** Called once when Main.fxml is first initialized. */
    public static void setMainController(MainController mc) {
        mainController = mc;
    }

    public static MainController getMainController() {
        return mainController;
    }

    /** Navigate back to Transactions tab. */
    public static void goToTransactions() {
        if (mainController != null) mainController.showTransactionsPublic();
    }

    /** Navigate back to Accounts tab. */
    public static void goToAccounts() {
        if (mainController != null) mainController.showAccountsPublic();
    }

    /** Navigate back to Categories tab. */
    public static void goToCategories() {
        if (mainController != null) mainController.showCategoriesPublic();
    }

    /** Navigate back to Budget tab. */
    public static void goToBudget() {
        if (mainController != null) mainController.showBudgetPublic();
    }

    /** Navigate back to Dashboard tab. */
    public static void goToDashboard() {
        if (mainController != null) mainController.showDashboardPublic();
    }
}
