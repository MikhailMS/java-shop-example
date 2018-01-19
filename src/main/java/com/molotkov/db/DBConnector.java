package com.molotkov.db;

import com.molotkov.users.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private String dbUrl;
    private User user;
    private Connection connection;

    public DBConnector(final String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(this.dbUrl);
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public DBConnector(final String dbUrl, final User user) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.postgresql.Driver");
            this.user = user;
            this.connection = DriverManager.getConnection(this.dbUrl, this.user.getUserName(), this.user.getUserPasswd());
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void changeDBUrl(final String newUrl) {
        this.dbUrl = newUrl;
    }

    public void changeUser(final User user) {
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
