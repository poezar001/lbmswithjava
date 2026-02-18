package model;

import javafx.beans.property.*;

public class Transaction {
    private final StringProperty bookTitle;
    private final StringProperty userName;
    private final StringProperty dueDate;

    public Transaction(String bookTitle, String userName, String dueDate) {
        this.bookTitle = new SimpleStringProperty(bookTitle);
        this.userName = new SimpleStringProperty(userName);
        this.dueDate = new SimpleStringProperty(dueDate);
    }

    public StringProperty bookTitleProperty() { return bookTitle; }
    public StringProperty userNameProperty() { return userName; }
    public StringProperty dueDateProperty() { return dueDate; }
}