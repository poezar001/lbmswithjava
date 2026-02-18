package controller;

import dao.DBConnection;
import model.Book;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.*;

public class AddBookController {
    @FXML private TextField titleField, authorField, isbnField, categoryField, quantityField;

    private Book selectedBook; // Stores the book being edited
    private boolean isEditMode = false;

    // This method is called by BooksListController when clicking "Edit"
    public void setBookData(Book book) {
        this.selectedBook = book;
        this.isEditMode = true;

        // Pre-fill the fields with existing data
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        isbnField.setText(book.getIsbn());
        categoryField.setText(book.getCategory());
        quantityField.setText(String.valueOf(book.getQuantity()));
    }

    @FXML
    void saveBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String category = categoryField.getText().trim();
        String qty = quantityField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) return;

        // Choose SQL based on mode
        String sql;
        if (isEditMode) {
            sql = "UPDATE books SET title=?, author=?, isbn=?, category=?, quantity=? WHERE id=?";
        } else {
            sql = "INSERT INTO books (title, author, isbn, category, quantity, status) VALUES (?, ?, ?, ?, ?, 'Available')";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setString(4, category);
            ps.setInt(5, Integer.parseInt(qty.isEmpty() ? "1" : qty));

            if (isEditMode) {
                ps.setInt(6, selectedBook.getId()); // Set the ID for the WHERE clause
            }

            ps.executeUpdate();
            cancel(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cancel() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}