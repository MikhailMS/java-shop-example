package com.molotkov.db;

import java.sql.*;

public class DBConnector {
    private String dbUrl;
    private Connection connection;

    public DBConnector(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(this.dbUrl);
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void changeDBUrl(String newUrl) {
        this.dbUrl = newUrl;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public Connection getConnection() {
        return connection;
    }
}
