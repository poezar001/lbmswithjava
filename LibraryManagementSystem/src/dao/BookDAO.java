package dao;

import model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BookDAO {
    public static ObservableList<Book> getAllBooks() {
        ObservableList<Book> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM books";
        
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Book(
                    rs.getInt("id"), 
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    
    public static boolean borrowBook(int userId, int bookId) {
        // Change 'Borrowed' to 'Issued' to match your ENUM
        String insertBorrow = "INSERT INTO borrow (book_id, user_id, issue_date, due_date) VALUES (?, ?, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY))";
        String updateBookStatus = "UPDATE books SET status = 'Issued' WHERE id = ?"; // Fixed here

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return false;
            con.setAutoCommit(false); 

            try (PreparedStatement psBorrow = con.prepareStatement(insertBorrow);
                 PreparedStatement psBook = con.prepareStatement(updateBookStatus)) {

                psBorrow.setInt(1, bookId);
                psBorrow.setInt(2, userId);
                psBorrow.executeUpdate();

                psBook.setInt(1, bookId);
                psBook.executeUpdate();

                con.commit(); 
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static Map<String, Integer> getBookCountByCategory() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT category, COUNT(*) as count FROM books GROUP BY category";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getInt("count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // NEW METHOD: Used for the Bar Chart
    public static int getCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM books WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    
    public void updateFines() {
        String sql = "UPDATE borrow SET fine_amount = DATEDIFF(CURRENT_DATE, due_date) * 0.50 " +
                     "WHERE return_date IS NULL AND CURRENT_DATE > due_date";
        
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("Fines updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}