package controller;

import dao.DBConnection;
import dao.UserDAO;
import model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;

public class UsersListController {
    // Table components
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName, colEmail, colRole;
    @FXML private TableColumn<User, Void> colAction;

    // Search and UI components
    @FXML private TextField searchField; // MUST match fx:id="searchField" in FXML
    @FXML private VBox editPane;
    @FXML private TextField editNameField, editEmailField, editPhoneField;
    @FXML private ComboBox<String> roleComboBox;
    
    // Data lists
    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private User selectedUser; // Null = Adding, Not Null = Editing

    @FXML
    public void initialize() {
        // 1. Setup UI
        roleComboBox.getItems().addAll("ADMIN", "USER");
        editPane.setVisible(false);
        
        // 2. Column Bindings
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());
        colRole.setCellValueFactory(cell -> cell.getValue().roleProperty());
        
        
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // 3. Setup Action Buttons (Edit/Delete)
        setupActionButtons();

        // 4. Setup Search & Filtering logic
        setupSearchLogic();

        // 5. Initial Data Load
        refreshTable();
    }

    private void setupSearchLogic() {
        // Wrap masterData in FilteredList
        filteredData = new FilteredList<>(masterData, p -> true);

        // Bind filter to search field text
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getRole().toLowerCase().contains(lowerCaseFilter)) return true;
                
                return false;
            });
        });

        // Wrap in SortedList so table can still be sorted by clicking headers
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);
    }

    @FXML
    public void refreshTable() {
        // Automatically updates filteredData and the TableView
        masterData.setAll(UserDAO.getAllUsers());
    }

    @FXML
    void showAddPanel() {
        selectedUser = null; 
        editPane.setVisible(true);
        editNameField.clear();
        editEmailField.clear();
        editPhoneField.clear();
        roleComboBox.setValue("USER");
    }

    private void showEditDetails(User user) {
        selectedUser = user;
        editPane.setVisible(true);
        editNameField.setText(user.getName());
        editEmailField.setText(user.getEmail());
        editPhoneField.setText(user.getPhoneNumber());
        roleComboBox.setValue(user.getRole());
    }

    @FXML
    void handleSave() {
        String name = editNameField.getText();
        String email = editEmailField.getText();
        String phone = editPhoneField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty()) {
            showToast("Validation Error", "Name and Email are required.");
            return;
        }

        String sql;
        if (selectedUser == null) {
            sql = "INSERT INTO users (name, email, phone_number, role, password) VALUES (?, ?, ?, ?, '123456')";
        } else {
            sql = "UPDATE users SET name=?, email=?, phone_number=?, role=? WHERE user_id=?";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, role);

            if (selectedUser != null) ps.setInt(5, selectedUser.getId());

            if (ps.executeUpdate() > 0) {
                editPane.setVisible(false);
                refreshTable(); 
                showToast("Success", selectedUser == null ? "Added successfully!" : "Updated successfully!");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            showToast("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML void cancelEdit() { editPane.setVisible(false); }

    private void setupActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final HBox container = new HBox(15);
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();

            {
                container.setStyle("-fx-alignment: CENTER;");
                
                // Edit Icon
                javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
                editIcon.setContent("M12.89,3L14.85,4.95L5.07,14.73L3.12,12.78L12.89,3M17.33,2.54C17.18,2.39 16.92,2.39 16.77,2.54L15.42,3.89L17.37,5.84L18.72,4.49C18.87,4.34 18.87,4.08 18.72,3.93L17.33,2.54M2,16.25V19H4.75L12.53,11.22L9.78,8.47L2,16.25Z");
                editIcon.setFill(javafx.scene.paint.Color.web("#576574"));
                editBtn.setGraphic(editIcon);
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                // Delete Icon
                javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                deleteIcon.setContent("M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19V4M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z");
                deleteIcon.setFill(javafx.scene.paint.Color.web("#576574"));
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                // Hover Effects
                editBtn.setOnMouseEntered(e -> editIcon.setFill(javafx.scene.paint.Color.web("#3498db")));
                editBtn.setOnMouseExited(e -> editIcon.setFill(javafx.scene.paint.Color.web("#576574")));
                deleteBtn.setOnMouseEntered(e -> deleteIcon.setFill(javafx.scene.paint.Color.web("#e74c3c")));
                deleteBtn.setOnMouseExited(e -> deleteIcon.setFill(javafx.scene.paint.Color.web("#576574")));

                editBtn.setOnAction(e -> showEditDetails(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));

                container.getChildren().addAll(editBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void confirmDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + user.getName() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                ps.setInt(1, user.getId());
                ps.executeUpdate();
                refreshTable();
                showToast("Deleted", "User has been removed.");
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void showToast(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}