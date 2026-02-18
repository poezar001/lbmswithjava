package dao;

import model.User;
import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserDAO {

    public static User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email=? AND password=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("phone_number")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static boolean register(String name, String email, String password, String phone) {
        String sql = "INSERT INTO users(name, email, password, role, phone_number) VALUES(?,?,?,'USER',?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, phone);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ObservableList<User> getAllUsers() {
        ObservableList<User> list = FXCollections.observableArrayList();
        // FIXED: Added phone_number to the SELECT statement
        String sql = "SELECT user_id, name, email, role, phone_number FROM users";
        
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                // FIXED: Now passing all 5 required parameters
                list.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("phone_number")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    public static double getUserFines(int userId) {
        String sql = "SELECT SUM(fine_amount) FROM borrow WHERE user_id = ? AND return_date IS NULL";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public static boolean payFineAndReturn(String userName, String bookTitle) {
        String sql = "UPDATE borrow br " +
                     "JOIN users u ON br.user_id = u.user_id " +
                     "JOIN books b ON br.book_id = b.id " +
                     "SET br.fine_amount = 0, br.return_date = CURRENT_DATE " +
                     "WHERE u.name = ? AND b.title = ? AND br.return_date IS NULL";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, userName);
            pst.setString(2, bookTitle);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}