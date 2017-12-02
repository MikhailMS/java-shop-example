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
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        final String[] params = {
                "id integer NOT NULL",
                "string text NOT NULL"
        };
        final String[] insertRow1 = {
                "1",
                "'test text'"
        };
        final String[] insertRow2 = {
                "2",
                "'new text'"
        };
        final String[] selectColumns = {
                "id"
        };
        final String[] filterArguments = {
                "string LIKE 'new%'"
        };

        DBUtils.createTable(dataSource.getConnection(), "test_table", params);

        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", insertRow1);
        DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", selectColumns);
        cursor.getResults().next();
        String resultString1 = cursor.getResults().getString(1);
        assertEquals("Create/Insert/Select queries succeed", "1",resultString1);
        cursor.closeCursor();

        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", insertRow2);
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, filterArguments);
        cursor.getResults().next();
        String resultString2 = cursor.getResults().getString(1);
        assertEquals("Filter query succeeds", "2",resultString2);
        cursor.closeCursor();
    }

    @Test(expected = SQLException.class)
    public void testDeleteTable() throws SQLException {
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        DBUtils.createTable(dataSource.getConnection(), "test_table", new String[]{});
        DBUtils.deleteTable(dataSource.getConnection(), "test_table");

        final DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", new String[]{});
    }

}
