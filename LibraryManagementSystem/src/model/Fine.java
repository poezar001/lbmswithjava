package model;

public class Fine {
    private String userName;
    private String bookTitle;
    private double amount;
    private String dueDate;

    public Fine(String userName, String bookTitle, double amount, String dueDate) {
        this.userName = userName;
        this.bookTitle = bookTitle;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    // Add Getters for JavaFX TableView to use
    public String getUserName() { return userName; }
    public String getBookTitle() { return bookTitle; }
    public double getAmount() { return amount; }
    public String getDueDate() { return dueDate; }
}
