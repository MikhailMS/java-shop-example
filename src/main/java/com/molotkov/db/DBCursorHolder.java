package com.molotkov.db;

import javax.swing.plaf.nimbus.State;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCursorHolder {
    private ResultSet results;
    private Statement statement;

    public DBCursorHolder(ResultSet resultSet, Statement statement) {
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
