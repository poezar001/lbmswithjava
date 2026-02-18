package controller;

import dao.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;

public class IssueBookController {

    @FXML private TextField bookIdField, userIdField, returnBookIdField;
    @FXML private DatePicker dueDatePicker;

    @FXML
    void handleIssueBook() {
        String bookId = bookIdField.getText();
        String userId = userIdField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        if (bookId.isEmpty() || userId.isEmpty() || dueDate == null) {
            showAlert("Error", "All fields are required to issue a book.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // Start transaction

            // 1. Check if book is available
            String checkSql = "SELECT status FROM books WHERE id = ?";
            PreparedStatement psCheck = con.prepareStatement(checkSql);
            psCheck.setString(1, bookId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next() && rs.getString("status").equalsIgnoreCase("Available")) {
                
                // 2. Insert into borrow table
                String issueSql = "INSERT INTO borrow (book_id, user_id, due_date) VALUES (?, ?, ?)";
                PreparedStatement psIssue = con.prepareStatement(issueSql);
                psIssue.setString(1, bookId);
                psIssue.setString(2, userId);
                psIssue.setDate(3, java.sql.Date.valueOf(dueDate));
                psIssue.executeUpdate();

                // 3. Update book status
                String updateSql = "UPDATE books SET status = 'Issued' WHERE id = ?";
                PreparedStatement psUpdate = con.prepareStatement(updateSql);
                psUpdate.setString(1, bookId);
                psUpdate.executeUpdate();

                con.commit();
                showAlert("Success", "Book Issued Successfully!");
                clearFields();
            } else {
                showAlert("Error", "Book is not available or does not exist.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleReturnBook() {
        String bookId = returnBookIdField.getText();

        if (bookId.isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // 1. Update borrow record with return date
            String returnSql = "UPDATE borrow SET return_date = CURRENT_DATE WHERE book_id = ? AND return_date IS NULL";
            PreparedStatement psReturn = con.prepareStatement(returnSql);
            psReturn.setString(1, bookId);
            int rows = psReturn.executeUpdate();

            if (rows > 0) {
                // 2. Update book status back to Available
                String updateSql = "UPDATE books SET status = 'Available' WHERE id = ?";
                PreparedStatement psUpdate = con.prepareStatement(updateSql);
                psUpdate.setString(1, bookId);
                psUpdate.executeUpdate();

                con.commit();
                showAlert("Success", "Book Returned Successfully!");
                returnBookIdField.clear();
            } else {
                showAlert("Error", "This book was not marked as issued.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        bookIdField.clear();
        userIdField.clear();
        dueDatePicker.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}