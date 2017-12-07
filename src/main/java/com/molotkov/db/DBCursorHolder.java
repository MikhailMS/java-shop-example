package com.molotkov.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCursorHolder {
    private ResultSet results;
    private Statement statement;
    private Connection connection;

    public DBCursorHolder(final ResultSet resultSet, final Statement statement, final Connection connection) {
        this.results = resultSet;
        this.statement = statement;
        this.connection = connection;
    }

    public ResultSet getResults() {
        return results;
    }

    public void closeCursor() {
        try {
            this.results.close();
            this.statement.close();
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
