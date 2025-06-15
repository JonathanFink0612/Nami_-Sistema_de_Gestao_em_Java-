package com.exemplo.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserProfile {
    private final StringProperty id;
    private final StringProperty email;
    private final StringProperty role;

    public UserProfile(String id, String email, String role) {
        this.id = new SimpleStringProperty(id);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
    }

    // Getters
    public String getId() { return id.get(); }
    public String getEmail() { return email.get(); }
    public String getRole() { return role.get(); }

    // Setters
    public void setRole(String role) { this.role.set(role); }

    // Property Getters (para o JavaFX)
    public StringProperty idProperty() { return id; }
    public StringProperty emailProperty() { return email; }
    public StringProperty roleProperty() { return role; }
}
