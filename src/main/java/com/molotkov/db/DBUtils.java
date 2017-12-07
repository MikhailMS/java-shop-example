package com.molotkov.db;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.molotkov.Utils.iterateSimultaneously;

public class DBUtils {

    public static void createTable(final Connection connection, final String tableName, final String[] columns) {
        try {
            final Statement statement = connection.createStatement();
            final String columnsString = Stream.of(columns).collect(Collectors.joining(", "));
            final String query = String.format("CREATE TABLE IF NOT EXISTS %s ( %s )", tableName, columnsString);
            statement.execute(query);
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertIntoTable(final Connection connection, final String tableName, final String[] rowValues) {
        try {
            final Statement statement = connection.createStatement();
            final String columnsString = Stream.of(rowValues).collect(Collectors.joining(", "));
            final String query = String.format("INSERT INTO %s VALUES ( %s )", tableName, columnsString);
            statement.execute(query);
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertSpecificIntoTable(final Connection connection, final String tableName, final String[] insertToColumns, final String[] insertValues) {
        try {
            final Statement statement = connection.createStatement();
            final String insertToString = Stream.of(insertToColumns).collect(Collectors.joining(","));
            final String insertValuesString = Stream.of(insertValues).collect(Collectors.joining(","));
            final String query = String.format("INSERT INTO %s ( %s ) VALUES ( %s )", tableName, insertToString, insertValuesString);
            statement.execute(query);
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updateTable(final Connection connection, final String tableName, final String[] columnsToUpdate, final String[] newValues, final String[] filterArguments) {
        try {
            final Statement statement = connection.createStatement();
            final List<String> columnsList = Arrays.asList(columnsToUpdate);
            final List<String> valuesList = Arrays.asList(newValues);
            List<String> columnValueList = new ArrayList<>();
            iterateSimultaneously(columnsList, valuesList, (String column, String value) -> {
                columnValueList.add(String.format("%s = %s",column, value));
            });
            final String columnValueString = Stream.of(columnValueList.toArray(new String[0])).collect(Collectors.joining(" "));
            final String filterArgumentsString = Stream.of(filterArguments).collect(Collectors.joining(" "));
            final String query = String.format("UPDATE %s SET %s WHERE %s", tableName, columnValueString, filterArgumentsString);
            statement.execute(query);
            statement.close();
            connection.close();
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

        String whereKeyWord;
        if (filterArguments.length==0) {
            whereKeyWord = "";
        } else {
            whereKeyWord = "WHERE";
        }

        final String filterArgumentsString = Stream.of(filterArguments).collect(Collectors.joining(" "));

        String selectColumnsString;
        if (selectColumns.length==0) {
            selectColumnsString = "*";
        } else {
            selectColumnsString = Stream.of(selectColumns).collect(Collectors.joining(", "));
        }

        final String query = String.format("SELECT %s FROM %s %s %s", selectColumnsString, tableName, whereKeyWord, filterArgumentsString);
        final ResultSet resultSet = statement.executeQuery(query);

        return new DBCursorHolder(resultSet, statement);
    }

    public static DBCursorHolder innerJoinTables(final Connection connection, final String tableNameR, final String tableNameL,
                                                 final String innerJoinColumn, final String[] selectColumns, final String[] filterArguments) throws SQLException  {
        final Statement statement = connection.createStatement();

        String whereKeyWord;
        if (filterArguments.length==0) {
            whereKeyWord = "";
        } else {
            whereKeyWord = "WHERE";
        }

        final String filterArgumentsString = Stream.of(filterArguments).collect(Collectors.joining(" "));

        String selectColumnsString;
        if (selectColumns.length==0) {
            selectColumnsString = "*";
        } else {
            selectColumnsString = Stream.of(selectColumns).collect(Collectors.joining(", "));
        }

        final String query = String.format("SELECT %s FROM %s INNER JOIN %s USING (%s) %s %s", selectColumnsString,
                tableNameR, tableNameL, innerJoinColumn ,whereKeyWord, filterArgumentsString);
        System.out.println(query);
        final ResultSet resultSet = statement.executeQuery(query);

        return new DBCursorHolder(resultSet, statement);
    };

    public static void deleteTable(final Connection connection, final String tableName) {
        try {
            final Statement statement = connection.createStatement();
            final String query = String.format("DROP TABLE IF EXISTS %s", tableName);
            statement.execute(query);
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
