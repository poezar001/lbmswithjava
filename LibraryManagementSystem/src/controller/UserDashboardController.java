package controller;

import dao.DBConnection;
import dao.BookDAO;
import model.Book;
import model.BorrowedBook;
import model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.event.ActionEvent;
import util.SessionManager;
import util.SceneUtil;

import java.sql.*;

public class UserDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label sectionHeader; // Make sure this FX:ID exists in your FXML
    @FXML private TextField searchField;
    @FXML private StackPane contentArea;
    
    private ObservableList<BorrowedBook> myBorrowedData = FXCollections.observableArrayList();
    private ObservableList<Book> allLibraryBooks = FXCollections.observableArrayList();
    private FilteredList<Book> filteredBooks;
    private User loggedUser;

    @FXML
    public void initialize() {
        showMyBooks();
    }

    public void setUserData(User user) {
        this.loggedUser = user;
        this.welcomeLabel.setText("Welcome back, " + user.getName() + "!");
        loadMyBooksData();
    }

    // --- NAVIGATION LOGIC ---

    @FXML
    void showMyBooks() {
        if(sectionHeader != null) sectionHeader.setText("My Currently Borrowed Books");
        searchField.setVisible(false);

        TableView<BorrowedBook> table = new TableView<>();
        setupBorrowedTableColumns(table);
        table.setItems(myBorrowedData);
        
        VBox layout = new VBox(10, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        contentArea.getChildren().setAll(layout);
        loadMyBooksData();
    }

    @FXML
    void showBrowseLibrary() {
        if(sectionHeader != null) sectionHeader.setText("Library Catalog");
        searchField.setVisible(true);

        TableView<Book> table = new TableView<>();
        setupLibraryTableColumns(table);
        
        allLibraryBooks.setAll(BookDAO.getAllBooks());
        filteredBooks = new FilteredList<>(allLibraryBooks, p -> true);
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredBooks.setPredicate(book -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return book.getTitle().toLowerCase().contains(lower) || 
                       book.getAuthor().toLowerCase().contains(lower) || 
                       book.getCategory().toLowerCase().contains(lower);
            });
        });

        table.setItems(filteredBooks);
        VBox layout = new VBox(10, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        contentArea.getChildren().setAll(layout);
    }

    @FXML
    void showProfile() {
        if(sectionHeader != null) sectionHeader.setText("User Profile");
        searchField.setVisible(false);

        VBox profileContainer = new VBox(30);
        profileContainer.setStyle("-fx-padding: 20; -fx-background-color: white;");

     // --- 1. TOP SECTION: PROFILE PICTURE ICON (No Button) ---
        VBox picSection = new VBox(15);
        picSection.setAlignment(Pos.CENTER_LEFT);

        Label picTitle = new Label("Your Profile Picture");
        picTitle.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

        // The Circular Icon Container
        StackPane circle = new StackPane();
        // Fixed size for the circle
        circle.setMaxWidth(80);
        circle.setMaxHeight(80);
        circle.setMinWidth(80);
        circle.setMinHeight(80);
        circle.setStyle("-fx-background-color: #fce4ec; -fx-background-radius: 50;");

        // The SVG Person Icon
        SVGPath userIcon = new SVGPath();
        userIcon.setContent("M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z");
        userIcon.setFill(Color.web("#d81b60")); 
        userIcon.setScaleX(2.5); // Slightly larger since the button is gone
        userIcon.setScaleY(2.5);

        circle.getChildren().add(userIcon);

        // Add only the title and the circle to the section
        picSection.getChildren().addAll(picTitle, circle);

        // --- 2. MIDDLE SECTION: FORM GRID ---
        GridPane formGrid = new GridPane();
        formGrid.setHgap(30);
        formGrid.setVgap(20);

        String inputStyle = "-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14;";

        VBox nameBox = createStyledInput("Full name", loggedUser.getName(), inputStyle);
        TextField nameField = (TextField) nameBox.getChildren().get(1);

        VBox emailBox = createStyledInput("Email address", loggedUser.getEmail(), inputStyle);
        TextField emailField = (TextField) emailBox.getChildren().get(1);

        VBox phoneBox = createStyledInput("Phone number", loggedUser.getPhoneNumber(), inputStyle);
        TextField phoneField = (TextField) phoneBox.getChildren().get(1);

        VBox roleBox = createStyledInput("Role", loggedUser.getRole(), inputStyle);
        TextField roleField = (TextField) roleBox.getChildren().get(1);
        roleField.setEditable(false);
        roleField.setOpacity(0.6);

        formGrid.add(nameBox, 0, 0);
        formGrid.add(emailBox, 1, 0);
        formGrid.add(phoneBox, 0, 1);
        formGrid.add(roleBox, 1, 1);

        // --- 3. BOTTOM SECTION: UPDATE BUTTON ---
        Button updateBtn = new Button("Update Profile");
        updateBtn.setStyle("-fx-background-color: #311b92; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 35; -fx-background-radius: 8; -fx-cursor: hand;");
        
        // This links the button to the function below
        updateBtn.setOnAction(e -> handleUpdate(nameField.getText(), emailField.getText(), phoneField.getText()));

        profileContainer.getChildren().addAll(picSection, formGrid, updateBtn);
        contentArea.getChildren().setAll(profileContainer);
    }

    // Helper for input layout
    private VBox createStyledInput(String labelText, String value, String style) {
        Label l = new Label(labelText);
        l.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold; -fx-font-size: 13;");
        TextField tf = new TextField(value == null ? "" : value);
        tf.setStyle(style);
        tf.setPrefWidth(350);
        return new VBox(8, l, tf);
    }

    // --- FUNCTIONAL METHODS ---

    private void handleUpdate(String newName, String newEmail, String newPhone) {
        String sql = "UPDATE users SET name = ?, email = ?, phone_number = ? WHERE user_id = ?";
        
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newName);
                ps.setString(2, newEmail);
                ps.setString(3, newPhone);
                ps.setInt(4, loggedUser.getId());
                
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Update the User object in memory
                    loggedUser.setName(newName);
                    loggedUser.setEmail(newEmail);
                    loggedUser.setPhoneNumber(newPhone);
                    
                    // Refresh UI
                    welcomeLabel.setText("Welcome back, " + newName + "!");
                    showAlert("Success", "Profile updated successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Could not update profile: " + e.getMessage());
        }
    }

    private void loadMyBooksData() {
        if (loggedUser == null) return;
        myBorrowedData.clear();
        String sql = "SELECT b.title, b.author, br.issue_date, br.due_date, b.status " +
                     "FROM borrow br JOIN books b ON br.book_id = b.id " +
                     "WHERE br.user_id = ? AND br.return_date IS NULL";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, loggedUser.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                myBorrowedData.add(new BorrowedBook(
                    rs.getString("title"), rs.getString("author"),
                    rs.getString("issue_date"), rs.getString("due_date"), rs.getString("status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Logout from session?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            SessionManager.cleanSession();
            SceneUtil.switchTo(event, "Login.fxml");
        }
    }

 // --- TABLE COLUMN SETUP HELPERS ---

    private void setupBorrowedTableColumns(TableView<BorrowedBook> table) {
        TableColumn<BorrowedBook, String> t = new TableColumn<>("Title");
        t.setCellValueFactory(c -> c.getValue().titleProperty());

        TableColumn<BorrowedBook, String> a = new TableColumn<>("Author");
        a.setCellValueFactory(c -> c.getValue().authorProperty());

        TableColumn<BorrowedBook, String> d = new TableColumn<>("Due Date");
        d.setCellValueFactory(c -> c.getValue().dueDateProperty());

        // Use setAll to prevent duplicate columns and clear the "ugly" filler
//        table.getColumns().setAll(t, a, d);
        table.getColumns().add(t);
        table.getColumns().add(a);
        table.getColumns().add(d);

        // MODERN FIX (JavaFX 20+): Stretches all columns to fill the table width
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }


    private void setupLibraryTableColumns(TableView<Book> table) {
        TableColumn<Book, String> t = new TableColumn<>("Title");
        t.setCellValueFactory(c -> c.getValue().titleProperty());

        TableColumn<Book, String> c = new TableColumn<>("Category");
        c.setCellValueFactory(c1 -> c1.getValue().categoryProperty());

        TableColumn<Book, String> s = new TableColumn<>("Status");
        s.setCellValueFactory(c2 -> c2.getValue().statusProperty());

        // --- Action Column with Borrow Button ---
        TableColumn<Book, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button borrowBtn = new Button("Borrow");
            {
                // Matching your theme colors
                borrowBtn.setStyle("-fx-background-color: #311b92; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                borrowBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    handleBorrowAction(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    
                    // Ensure this matches 'Available' from your ENUM
                    if ("Available".equalsIgnoreCase(book.getStatus())) {
                        setGraphic(borrowBtn);
                    } else {
                        // Show 'Issued' or 'Unavailable' for other statuses
                        Label lbl = new Label("Issued"); 
                        lbl.setStyle("-fx-text-fill: #bdc3c7;");
                        setGraphic(lbl);
                    }
                }
            }
        });

     // Add ALL columns to the table
        table.getColumns().add(t);
        table.getColumns().add(c);
        table.getColumns().add(s);
        table.getColumns().add(actionCol); 

        // Optional: Make columns auto-size to fill the space
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void handleBorrowAction(Book book) {
        if (BookDAO.borrowBook(loggedUser.getId(), book.getId())) {
            showAlert("Success", "You have successfully borrowed: " + book.getTitle());
            // Refresh the catalog table to update the status and remove the button
            showBrowseLibrary(); 
        } else {
            showAlert("Error", "Could not borrow the book. Please try again.");
        }
    }


    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}