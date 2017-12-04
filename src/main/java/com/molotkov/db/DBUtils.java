package com.molotkov.db;

import java.sql.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DBUtils {

    public static void createTable(final Connection connection, final String tableName, final String[] columns) {
        Statement statement;
        try {
            statement = connection.createStatement();
            final String columnsString = Stream.of(columns).collect(Collectors.joining(", "));
            final String query = String.format("CREATE TABLE IF NOT EXISTS %s ( %s )", tableName, columnsString);
            final ResultSet resultSet = statement.executeQuery(query);
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertIntoTable(final Connection connection, final String tableName, final String[] rowValues) {
        Statement statement;
        try {
            statement = connection.createStatement();
            final String columnsString = Stream.of(rowValues).collect(Collectors.joining(", "));
            final String query = String.format("INSERT INTO %s VALUES ( %s )", tableName, columnsString);
            final ResultSet resultSet = statement.executeQuery(query);
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static DBCursorHolder selectFromTable(final Connection connection, final String tableName, final String[] selectColumns) throws SQLException {
        final Statement statement = connection.createStatement();
        String selectColumnsString;
        if (selectColumns.length==0) {
            selectColumnsString = "*";
        } else {
            selectColumnsString = Stream.of(selectColumns).collect(Collectors.joining(", "));
        }
        final String query = String.format("SELECT %s FROM %s", selectColumnsString, tableName);
        final ResultSet resultSet = statement.executeQuery(query);

        return new DBCursorHolder(resultSet, statement);
    }

    public static DBCursorHolder filterFromTable(final Connection connection, final String tableName, final String[] selectColumns, final String[] filterArguments) throws SQLException {
        final Statement statement = connection.createStatement();
        final String filterArgumentsString = Stream.of(filterArguments).collect(Collectors.joining(", "));
        String selectColumnsString;
        if (selectColumns.length==0) {
            selectColumnsString = "*";
        } else {
            selectColumnsString = Stream.of(selectColumns).collect(Collectors.joining(", "));
        }
        final String query = String.format("SELECT %s FROM %s WHERE %s", selectColumnsString, tableName, filterArgumentsString);
        final ResultSet resultSet = statement.executeQuery(query);

        return new DBCursorHolder(resultSet, statement);
    }

    public static void deleteTable(final Connection connection, final String tableName) {
        Statement statement;
        try {
            statement = connection.createStatement();
            final String query = String.format("DROP TABLE IF EXISTS %s", tableName);
            final ResultSet resultSet = statement.executeQuery(query);
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
