package model;

import javafx.beans.property.*;

public class BorrowedBook {
    private final StringProperty title, author, issueDate, dueDate, status;

    public BorrowedBook(String title, String author, String issueDate, String dueDate, String status) {
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.issueDate = new SimpleStringProperty(issueDate);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty titleProperty() { return title; }
    public StringProperty authorProperty() { return author; }
    public StringProperty issueDateProperty() { return issueDate; }
    public StringProperty dueDateProperty() { return dueDate; }
    public StringProperty statusProperty() { return status; }
}