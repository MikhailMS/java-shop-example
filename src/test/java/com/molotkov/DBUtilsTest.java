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
import java.sql.Statement;

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
    // CREATE TABLE
        DBUtils.createTable(dataSource.getConnection(), "test_table", new String[]{"id integer NOT NULL", "string text NOT NULL"});

    // INSERT INTO TABLE / SELECT FROM TABLE
        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", new String[]{"1", "'test text'"});
        DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", selectColumns);
        cursor.getResults().next();
        final String resultString1 = cursor.getResults().getString(1);
        assertEquals("Create/Insert/Select queries succeed", "1",resultString1);
        cursor.closeCursor();

    // SELECT SPECIFIC FIELDS UNDER CONDITION
        DBUtils.insertIntoTable(dataSource.getConnection(), "test_table", new String[]{"2", "'new text'"});
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, new String[]{"string LIKE 'new%'"});
        cursor.getResults().next();
        final String resultString2 = cursor.getResults().getString(1);
        assertEquals("Filter query succeeds", "2",resultString2);
        cursor.closeCursor();

    // INSERT SPECIFIC FIELDS INTO TABLE
        DBUtils.insertSpecificIntoTable(dataSource.getConnection(), "test_table", new String[] {"id", "string"}, new String[]{
                "3", "'once more'"});
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, new String[]{"string LIKE 'once%'"});
        cursor.getResults().next();
        final String resultString3 = cursor.getResults().getString(1);
        assertEquals("Insert into specific columns query succeeds", "3",resultString3);
        cursor.closeCursor();

    // UPDATE TABLE
        DBUtils.updateTable(dataSource.getConnection(), "test_table", new String[]{"id"}, new String[]{"4"},
                new String[]{"string LIKE 'once%'"});
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "test_table", selectColumns, new String[]{"string LIKE 'once%'"});
        cursor.getResults().next();
        final String resultString4 = cursor.getResults().getString(1);
        assertEquals("Update query succeeds", "4", resultString4);

    // NATURAL JOIN TEST
        Statement statement = dataSource.getConnection().createStatement();
        statement.addBatch("CREATE TABLE IF NOT EXISTS products ( product_id serial PRIMARY KEY, product_name text NOT NULL," +
                " product_weight numeric (6,3) NOT NULL, product_price numeric (8,2) NOT NULL )");
        statement.addBatch("INSERT INTO products ( product_name, product_weight, product_price ) VALUES ( 'apple', 0.150, 0.8 )");
        statement.addBatch("INSERT INTO products ( product_name, product_weight, product_price ) VALUES ( 'chicken', 1, 2.3 )");

        statement.addBatch("CREATE TABLE IF NOT EXISTS inventory ( entry_id serial, " +
                "product_id int4 REFERENCES products(product_id) ON DELETE RESTRICT, product_amount int4 NOT NULL )");
        statement.addBatch("INSERT INTO inventory ( product_id, product_amount ) VALUES ( 1, 3 )");
        statement.addBatch("INSERT INTO inventory ( product_id, product_amount ) VALUES ( 2, 4 )");

        statement.executeBatch();
        statement.close();

        cursor = DBUtils.naturalJoinTables(dataSource.getConnection(), "products", "inventory", new String[] {"product_id", "product_name", "product_weight", "product_price", "product_amount"},
                new String[]{});
        String resultString5 = "";
        while (cursor.getResults().next()) {
            resultString5 += String.format("%s ",cursor.getResults().getString(2));
            resultString5 += String.format("%s ",cursor.getResults().getString(3));
            resultString5 += String.format("%s ",cursor.getResults().getString(4));
            resultString5 += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("Natural join query succeeds", "apple 0.150 0.80 3 chicken 1.000 2.30 4 ", resultString5);
        cursor.closeCursor();
    }

    @Test(expected = SQLException.class)
    public void testDeleteTable() throws SQLException {
        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        DBUtils.createTable(dataSource.getConnection(), "test_table", new String[]{});
        DBUtils.deleteTable(dataSource.getConnection(), "test_table");

        final DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "test_table", new String[]{});
    }
}
