package model;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty role;
    private final StringProperty phoneNumber;

    public User(int id, String name, String email, String role, String phoneNumber) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
    }

    // Property getters (Needed for TableView)
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }
    public StringProperty roleProperty() { return role; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }

    // Standard getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getRole() { return role.get(); }
    public String getPhoneNumber() { return phoneNumber.get(); }

    // --- ADD THESE SETTERS TO FIX THE ERRORS ---
    
    public void setName(String value) { 
        this.name.set(value); 
    }

    public void setEmail(String value) { 
        this.email.set(value); 
    }

    public void setPhoneNumber(String value) { 
        this.phoneNumber.set(value); 
    }
}