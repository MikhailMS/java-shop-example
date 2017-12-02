package com.molotkov.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCursorHolder {
    private ResultSet results;
    private Statement statement;

    public DBCursorHolder(final ResultSet resultSet, final Statement statement) {
        this.results = resultSet;
        this.statement = statement;
    }

    public ResultSet getResults() {
        return results;
    }

    public void closeCursor() {
        try {
            this.results.close();
            this.statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
