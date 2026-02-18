package controller;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.SceneUtil;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    // Removed addressField
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    void handleRegister(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // Updated Validation (No address check)
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all required fields.");
            return;
        }

        if (!pass.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Passwords do not match!");
            return;
        }

        // Call DAO with only 4 parameters
        boolean success = UserDAO.register(name, email, pass, phone);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
            SceneUtil.switchTo(event, "Login.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Email might already be registered.");
        }
    }

    @FXML
    void goToLogin(ActionEvent event) {
        SceneUtil.switchTo(event, "Login.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}