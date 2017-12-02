package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.junit.Assert.assertTrue;

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
        String[] params = {
                "id integer NOT NULL",
                "string text NOT NULL"
        };
        String[] insertRow1 = {
                "1",
                "'test text'"
        };
        String[] selectColumns = {
                "id"
        };
        DBUtils.createTable(dataSource.getConnection(), "test_table", params);
        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", insertRow1);

        DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", selectColumns);

        cursor.getResults().next();
        String resultString1 = cursor.getResults().getString(1);
        assertEquals("Create/Insert/Select queries succeed", "1",resultString1);
        cursor.closeCursor();

        String[] insertRow2 = {
                "2",
                "'new text'"
        };
        String[] filterArguments = {
                "string LIKE 'new%'"
        };
        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", insertRow2);

        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, filterArguments);
        cursor.getResults().next();
        String resultString2 = cursor.getResults().getString(1);
        assertEquals("Filter query succeeds", "2",resultString2);
    }

    @Test(expected = SQLException.class)
    public void testDeleteTable() throws SQLException {
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        DBUtils.createTable(dataSource.getConnection(), "test_table", new String[]{});
        DBUtils.deleteTable(dataSource.getConnection(), "test_table");

        DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", new String[]{});
    }

}
