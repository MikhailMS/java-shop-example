package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class AdministratorTest {
    private HikariConfig hikariConfig;
    private HikariDataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Before
    public void setUp() throws SQLException {
        hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);
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
    }

    @Test
    public void testAdministratorMethods() throws SQLException {
    // TESTING getTotalPriceOfInventory
        Administrator admin = new Administrator("admin", "admin");
        assertEquals("getTotalPriceOfInventory succeeds", 11.6, admin.getTotalPriceOfInventory(dataSource.getConnection()));

    // TESTING addProductToInventory
        Product newProduct = new Product("turkey", 1.5, 3);
        admin.addProductToInventory(dataSource.getConnection(), newProduct, 1);

        DBCursorHolder cursor = DBUtils.filterFromTable(dataSource.getConnection(), "products", new String[]{"product_name", "product_price"},
                new String[]{String.format("product_name LIKE '%s'",newProduct.getName())});
        String newProductString = "";

        while (cursor.getResults().next()) {
            newProductString += String.format("%s ",cursor.getResults().getString(1));
            newProductString += String.format("%f ",cursor.getResults().getDouble(2));
        }

        assertEquals("addProductToInventory succeeds", "turkey 3.0 ", newProductString);
        cursor.closeCursor();

    // TESTING removeProductFromInventory
        admin.removeProductFromInventory(dataSource.getConnection(), newProduct, 1);

        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "products", new String[]{"product_name", "product_price"},
                new String[]{String.format("product_name LIKE '%s'",newProduct.getName())});
        newProductString = "";

        while (cursor.getResults().next()) {
            newProductString += String.format("%s ",cursor.getResults().getString(1));
            newProductString += String.format("%f ",cursor.getResults().getDouble(2));
        }

        assertEquals("addProductToInventory succeeds", "", newProductString);
        cursor.closeCursor();

    }
}
