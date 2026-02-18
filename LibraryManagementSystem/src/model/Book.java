package model;

import javafx.beans.property.*;

public class Book {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty isbn;
    private final StringProperty category;
    private final IntegerProperty quantity;
    private final StringProperty status;

    public Book(int id, String title, String author, String isbn, String category, int quantity, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.isbn = new SimpleStringProperty(isbn);
        this.category = new SimpleStringProperty(category);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.status = new SimpleStringProperty(status);
    }

    // --- Property Methods (Keep these for TableView) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty authorProperty() { return author; }
    public StringProperty isbnProperty() { return isbn; }
    public StringProperty categoryProperty() { return category; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty statusProperty() { return status; }

    // --- Standard Getters (Add these to fix your errors) ---
    public int getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getAuthor() { return author.get(); }
    public String getIsbn() { return isbn.get(); }
    public String getCategory() { return category.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getStatus() { return status.get(); }

    // --- Setters (Optional, but helpful for Profile/Edit) ---
    public void setTitle(String value) { title.set(value); }
    public void setAuthor(String value) { author.set(value); }
    public void setCategory(String value) { category.set(value); }
}