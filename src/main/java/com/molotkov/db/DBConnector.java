package com.molotkov.db;

import com.molotkov.users.User;

import java.sql.*;

public class DBConnector {
    private String dbUrl;
    private User user;
    private Connection connection;

    public DBConnector(final String dbUrl, final User user) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.postgresql.Driver");
            if (user == null) {
                this.connection = DriverManager.getConnection(this.dbUrl);
            } else {
                this.user = user;
                this.connection = DriverManager.getConnection(this.dbUrl, this.user.getUserName(), this.user.getUserPasswd());
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void changeDBUrl(final String newUrl) {
        this.dbUrl = newUrl;
    }

    public void changeUser(User user) {
        this.user = user;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public User getUser() {
        return user;
    }

    public Connection getConnection() {
        return connection;
    }
}
