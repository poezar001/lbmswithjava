package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // UPDATE THIS STRING
    private static final String URL = "jdbc:mysql://localhost:3308/library_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root"; // Your MySQL username
    private static final String PASS = "root"; // Your MySQL password

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("Connection Failed! Check URL, Username, or Password.");
            e.printStackTrace();
            return null;
        }
    }
}
