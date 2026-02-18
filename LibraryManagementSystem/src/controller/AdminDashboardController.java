package controller;

import dao.DBConnection;
import dao.UserDAO;
import dao.BookDAO;
import model.Fine;
import model.Transaction;
import model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import util.SceneUtil;
import java.sql.*;

public class AdminDashboardController {

    @FXML private BorderPane mainLayout;
    @FXML private VBox contentArea;
    @FXML private Label adminNameLabel, totalBooksCount, issuedBooksCount, usersCount;
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Integer> statusBarChart;
    @FXML private TableView<Transaction> recentTable;
    @FXML private TableColumn<Transaction, String> colTitle, colUser, colDate;
    @FXML private TableView<Fine> finesTable;
    @FXML private TableColumn<Fine, String> colFineUser, colFineBook, colFineDate;
    @FXML private TableColumn<Fine, Double> colFineAmount;
    // Linked to FXML TextFields
    @FXML private TextField userIdField; 
    @FXML private TextField bookId;
    private User loggedInUser; 
    @FXML private Label totalFinesLabel;

    public void setAdminData(User user) {
        if (user != null) {
            this.loggedInUser = user;
        }
        // Safety check to prevent crash if user is still null
        if (loggedInUser != null) {
            adminNameLabel.setText("Welcome, " + loggedInUser.getName());
        }
        
        updateOverdueFines(); 
        refreshStats();
        setupTable();
        loadRecentTransactions();
        loadAnalytics();
        loadFineDetails();
    }
    // Background Task: Updates fine_amount for all overdue books
    private void updateOverdueFines() {
        String sql = "UPDATE borrow SET fine_amount = DATEDIFF(CURRENT_DATE, due_date) * 0.50 " +
                     "WHERE return_date IS NULL AND CURRENT_DATE > due_date";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void loadFineDetails() {
        ObservableList<Fine> fineList = FXCollections.observableArrayList();
        String sql = "SELECT u.name, b.title, br.fine_amount, br.due_date " +
                     "FROM borrow br " +
                     "JOIN users u ON br.user_id = u.user_id " +
                     "JOIN books b ON br.book_id = b.id " +
                     "WHERE br.fine_amount > 0 AND br.return_date IS NULL";

        try (Connection con = DBConnection.getConnection();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                fineList.add(new Fine(
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getDouble("fine_amount"),
                    rs.getString("due_date")
                ));
            }
            finesTable.setItems(fineList);
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
	    @FXML
	    void handlePayFine(ActionEvent event) {
	        Fine selectedFine = finesTable.getSelectionModel().getSelectedItem();
	        
	        if (selectedFine == null) {
	            new Alert(Alert.AlertType.WARNING, "Please select a user from the fines table first.").show();
	            return;
	        }
	
	        if (dao.UserDAO.payFineAndReturn(selectedFine.getUserName(), selectedFine.getBookTitle())) {
	            new Alert(Alert.AlertType.INFORMATION, "Success! Fine cleared and book returned.").show();
	            // Refresh all UI elements
	            refreshStats();
	            loadFineDetails();
	            loadRecentTransactions();
	            loadAnalytics();
	        } else {
	            new Alert(Alert.AlertType.ERROR, "Could not process payment.").show();
	        }
	    }

    @FXML
    void handleIssueBook(ActionEvent event) {
        try {
            int uId = Integer.parseInt(userIdField.getText());
            int bId = Integer.parseInt(bookId.getText());

            // Step 2: Check fine status before issuing
            double currentFine = UserDAO.getUserFines(uId);

            if (currentFine > 5.00) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Borrowing Blocked");
                alert.setHeaderText("User has unpaid fines: $" + String.format("%.2f", currentFine));
                alert.setContentText("Please clear fines before issuing more books.");
                alert.showAndWait();
                return; // BLOCK ACTION
            }

            // Step 3: Proceed with borrow if clear
            if (BookDAO.borrowBook(uId, bId)) {
                new Alert(Alert.AlertType.INFORMATION, "Book Issued Successfully!").show();
                refreshStats();
                loadRecentTransactions();
                loadAnalytics();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Invalid input. Please enter numeric IDs.").show();
        }
    }

    // --- Stats and Charts Logic ---
    private void refreshStats() {
        totalBooksCount.setText(String.valueOf(getCount("SELECT COUNT(*) FROM books")));
        usersCount.setText(String.valueOf(getCount("SELECT COUNT(*) FROM users")));
        issuedBooksCount.setText(String.valueOf(getCount("SELECT COUNT(*) FROM borrow WHERE return_date IS NULL")));
        
        // NEW: Fetch and format the total fines
        double totalFines = getTotalFines();
        totalFinesLabel.setText(String.format("$%.2f", totalFines));
    }

    // Helper method to get the total fine sum
    private double getTotalFines() {
        String sql = "SELECT SUM(fine_amount) FROM borrow WHERE return_date IS NULL";
        try (Connection con = DBConnection.getConnection();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    public void loadAnalytics() {
        categoryPieChart.getData().clear();
        BookDAO.getBookCountByCategory().forEach((cat, count) -> 
            categoryPieChart.getData().add(new PieChart.Data(cat, count)));

        statusBarChart.getData().clear();
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Available", BookDAO.getCountByStatus("Available")));
        series.getData().add(new XYChart.Data<>("Issued", BookDAO.getCountByStatus("Issued")));
        statusBarChart.getData().add(series);
    }

    private void loadRecentTransactions() {
        ObservableList<Transaction> list = FXCollections.observableArrayList();
        String sql = "SELECT b.title, u.name, br.due_date FROM borrow br " +
                     "JOIN books b ON br.book_id = b.id JOIN users u ON br.user_id = u.user_id " +
                     "WHERE br.return_date IS NULL ORDER BY br.issue_date DESC LIMIT 10";
        try (Connection con = DBConnection.getConnection();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Transaction(rs.getString("title"), rs.getString("name"), rs.getString("due_date")));
            }
            recentTable.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int getCount(String sql) {
        try (Connection con = DBConnection.getConnection();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private void setupTable() {
        // Recent Activity columns
        colTitle.setCellValueFactory(cd -> cd.getValue().bookTitleProperty());
        colUser.setCellValueFactory(cd -> cd.getValue().userNameProperty());
        colDate.setCellValueFactory(cd -> cd.getValue().dueDateProperty());

        colFineUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colFineBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colFineAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colFineDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
    }

    @FXML 
    void showDashboard(ActionEvent event) { 
        mainLayout.setCenter(contentArea); 
        setAdminData(this.loggedInUser); // Pass the saved user instead of null
    }
    @FXML void showBooks(ActionEvent event) { loadView("BooksList.fxml"); }
    @FXML void showUsers(ActionEvent event) { loadView("UsersList.fxml"); }
    @FXML void showIssueBook(ActionEvent event) { loadView("IssueBook.fxml"); }
    @FXML void handleLogout(ActionEvent event) { SceneUtil.switchTo(event, "Login.fxml"); }

    private void loadView(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/resources/view/" + fxml));
            mainLayout.setCenter(view);
        } catch (Exception e) { e.printStackTrace(); }
    }
}