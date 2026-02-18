package controller;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import util.SceneUtil;
import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    public void login(ActionEvent event) {
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            showAlert("Validation Error", "Email and password are required");
            return;
        }

        User user = UserDAO.login(email, pass);

        if (user == null) {
            showAlert("Login Failed", "Invalid email or password");
            return;
        }

        try {
            String fxmlPath;
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                fxmlPath = "/resources/view/AdminDashboard.fxml";
            } else {
                fxmlPath = "/resources/view/UserDashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // --- DATA PASSING LOGIC ---
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                AdminDashboardController controller = loader.getController();
                controller.setAdminData(user); 
            } else {
                UserDashboardController controller = loader.getController();
                controller.setUserData(user); // We need to add this method to the User controller
            }

            // Switch Stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("System Error", "Could not load the Dashboard screen.");
        }
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        SceneUtil.switchTo(event, "Register.fxml");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}