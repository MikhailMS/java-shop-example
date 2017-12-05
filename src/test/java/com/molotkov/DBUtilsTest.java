package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.SQLException;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class DBUtilsTest {
    private HikariConfig hikariConfig;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Before
    public void setUp() {
        hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());
    }

    @Test
    public void testDBUtilsInFullButDeleteTable() throws SQLException {
        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        final String[] selectColumns = {
                "id"
        };

        DBUtils.createTable(dataSource.getConnection(), "test_table", new String[]{"id integer NOT NULL", "string text NOT NULL"});

        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", new String[]{"1", "'test text'"});
        DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", selectColumns);
        cursor.getResults().next();
        final String resultString1 = cursor.getResults().getString(1);
        assertEquals("Create/Insert/Select queries succeed", "1",resultString1);
        cursor.closeCursor();

        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", new String[]{"2", "'new text'"});
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, new String[]{"string LIKE 'new%'"});
        cursor.getResults().next();
        final String resultString2 = cursor.getResults().getString(1);
        assertEquals("Filter query succeeds", "2",resultString2);
        cursor.closeCursor();

        DBUtils.insertSpecificIntoTable(dataSource.getConnection(), "test_table", new String[] {"id", "string"}, new String[]{
                "3", "'once more'"});
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, new String[]{"string LIKE 'once%'"});
        cursor.getResults().next();
        final String resultString3 = cursor.getResults().getString(1);
        assertEquals("Insert into specific columns query succeeds", "3",resultString3);
        cursor.closeCursor();

        DBUtils.updateTable(dataSource.getConnection(), "test_table", new String[]{"id"}, new String[]{"4"},
                new String[]{"string LIKE 'once%'"});
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, new String[]{"string LIKE 'once%'"});
        cursor.getResults().next();
        final String resultString4 = cursor.getResults().getString(1);
        assertEquals("Update query succeeds", "4", resultString4);
    }

    @Test(expected = SQLException.class)
    public void testDeleteTable() throws SQLException {
        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        DBUtils.createTable(dataSource.getConnection(), "test_table", new String[]{});
        DBUtils.deleteTable(dataSource.getConnection(), "test_table");

        final DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", new String[]{});
    }
}
