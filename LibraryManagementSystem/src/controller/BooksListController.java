package controller;

import dao.BookDAO;
import model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.geometry.Pos;

public class BooksListController {
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Integer> colId, colQuantity;
    @FXML private TableColumn<Book, String> colTitle, colAuthor, colIsbn, colCategory, colStatus;
    @FXML private TableColumn<Book, Void> colAction;
    @FXML private TextField searchField;

    private ObservableList<Book> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Mapping Table Columns to Model Properties
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colTitle.setCellValueFactory(cell -> cell.getValue().titleProperty());
        colAuthor.setCellValueFactory(cell -> cell.getValue().authorProperty());
        colIsbn.setCellValueFactory(cell -> cell.getValue().isbnProperty());
        colCategory.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        colQuantity.setCellValueFactory(cell -> cell.getValue().quantityProperty().asObject());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());

        // Load initial data
        refreshTable();
        
        // Set up the Search Logic
        setupSearch();
        
        // Set up the Edit/Delete Buttons
        setupActionButtons();
    }

    private void setupSearch() {
        FilteredList<Book> filteredData = new FilteredList<>(masterData, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(book.getId()).contains(lowerCaseFilter)) return true;
                if (book.getTitle().toLowerCase().contains(lowerCaseFilter)) return true;
                if (book.getAuthor().toLowerCase().contains(lowerCaseFilter)) return true;
                if (book.getIsbn().toLowerCase().contains(lowerCaseFilter)) return true;
                if (book.getCategory().toLowerCase().contains(lowerCaseFilter)) return true;

                return false;
            });
        });

        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(booksTable.comparatorProperty());
        booksTable.setItems(sortedData);
    }

    public void refreshTable() {
        masterData.setAll(BookDAO.getAllBooks());
    }

    @FXML
    void openAddBookPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/view/AddBook.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Book");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            refreshTable(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Getting the specific book for this row
                    Book book = getTableView().getItems().get(getIndex());
                    
                    deleteBtn.setOnAction(e -> handleDeleteBook(book));
                    editBtn.setOnAction(e -> handleUpdateBook(book));
                    
                    setGraphic(container);
                }
            }
        });
    }

    // These methods are used by the buttons in setupActionButtons
    private void handleDeleteBook(Book book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Book");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText("Are you sure you want to delete: " + book.getTitle() + "?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (BookDAO.deleteBook(book.getId())) {
                refreshTable();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete the book.").show();
            }
        }
    }

    private void handleUpdateBook(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/view/AddBook.fxml"));
            Parent root = loader.load();
            
            AddBookController controller = loader.getController();
            controller.setBookData(book);
            
            Stage stage = new Stage();
            stage.setTitle("Update Book");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            refreshTable();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}