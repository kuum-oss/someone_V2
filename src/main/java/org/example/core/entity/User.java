package org.example.core.entity;

public class User {
    private Integer id;
    private String email;
    private String password;
    private boolean isAdmin;
    private int points;

    public User(Integer id, String email, String password) {
        this(id, email, password, false, 5);
    }

    public User(Integer id, String email, String password, boolean isAdmin, int points) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.points = points;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
