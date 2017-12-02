package com.molotkov.db;

import java.sql.*;

public class DBConnector {
    private String dbUrl;
    private String dbUserName;
    private String dbUserPasswd;
    private Connection connection;

    public DBConnector(final String dbUrl, final String dbUserName, final String dbUserPasswd) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.postgresql.Driver");
            if (dbUserName.isEmpty() || dbUserPasswd.isEmpty()) {
                this.connection = DriverManager.getConnection(this.dbUrl);
            } else {
                this.dbUserName = dbUserName;
                this.dbUserPasswd = dbUserPasswd;
                this.connection = DriverManager.getConnection(this.dbUrl, this.dbUserName, this.dbUserPasswd);
            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void changeDBUrl(final String newUrl) {
        this.dbUrl = newUrl;
    }

    public void changeUserNameAndPasswd(final String userName, final String userPasswd) {
        this.dbUserName = userName;
        this.dbUserPasswd = userPasswd;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public Connection getConnection() {
        return connection;
    }
}
