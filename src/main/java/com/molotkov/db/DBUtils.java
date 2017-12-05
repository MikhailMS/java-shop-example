package com.molotkov.db;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DBUtils {

    public static void createTable(final Connection connection, final String tableName, final String[] columns) {
        try {
            final Statement statement = connection.createStatement();
            final String columnsString = Stream.of(columns).collect(Collectors.joining(", "));
            final String query = String.format("CREATE TABLE IF NOT EXISTS %s ( %s )", tableName, columnsString);
            statement.execute(query);
            statement.close();
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updateTable(final Connection connection, final String tableName, final String[] columnsToUpdate, final String[] newValues, final String[] filterArguments) {
        try {
            final Statement statement = connection.createStatement();
            List<String> columnsList = Arrays.asList(columnsToUpdate);
            List<String> valuesList = Arrays.asList(newValues);
            List<String> columnValueList = new ArrayList<>();
            iterateSimultaneously(columnsList, valuesList, (String column, String value) -> {
                columnValueList.add(String.format("%s = %s",column, value));
            });
            final String columnValueString = Stream.of(columnValueList.toArray(new String[0])).collect(Collectors.joining(" "));
            final String filterArgumentsString = Stream.of(filterArguments).collect(Collectors.joining(" "));
            final String query = String.format("UPDATE %s SET %s WHERE %s", tableName, columnValueString, filterArgumentsString);
            statement.execute(query);
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
        final String filterArgumentsString = Stream.of(filterArguments).collect(Collectors.joining(" "));
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
        try {
            final Statement statement = connection.createStatement();
            final String query = String.format("DROP TABLE IF EXISTS %s", tableName);
            statement.execute(query);
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static <T1, T2> void iterateSimultaneously(final Iterable<T1> c1, final Iterable<T2> c2, final BiConsumer<T1, T2> consumer) {
        final Iterator<T1> i1 = c1.iterator();
        final Iterator<T2> i2 = c2.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            consumer.accept(i1.next(), i2.next());
        }
    }
}
