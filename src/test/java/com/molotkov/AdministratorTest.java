package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.InventoryException;
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

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class AdministratorTest {
    private HikariDataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Before
    public void setUp() throws SQLException {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(30);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);
        final Statement statement = dataSource.getConnection().createStatement();

        statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_password text NOT NULL," +
                " privileges boolean DEFAULT FALSE )");
        statement.addBatch("INSERT INTO users VALUES ( 'admin', 'admin', TRUE )");
        statement.addBatch("INSERT INTO users VALUES ( 'testUser1', 'testUser1', FALSE )");
        statement.addBatch("INSERT INTO users VALUES ( 'testUser2', 'testUser2', FALSE )");

        statement.addBatch(" CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY," +
                " basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL," +
                " products_amount text NOT NULL, processed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.addBatch("INSERT INTO baskets ( basket_owner, products_name, products_amount ) VALUES ( 'testUser1', 'apple,chicken', '1,2' )");
        statement.addBatch("INSERT INTO baskets ( basket_owner, products_name, products_amount ) VALUES ( 'testUser2', 'apple', '2' )");

        statement.addBatch("CREATE TABLE IF NOT EXISTS orders ( order_id serial, basket_id int4 REFERENCES baskets(basket_id) ON DELETE CASCADE," +
                " order_owner text REFERENCES users(user_name) ON DELETE CASCADE, address text NOT NULL, total_price numeric (8,2) NOT NULL," +
                " completed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address, total_price ) VALUES ( 1, 'testUser1', 'Manchester', 2.45 )");
        statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address, total_price ) VALUES ( 2, 'testUser2', 'London', 2.50 )");

        statement.addBatch("CREATE TABLE IF NOT EXISTS products ( product_id serial PRIMARY KEY, product_name text NOT NULL UNIQUE," +
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
    public void testAdministratorMethods() throws SQLException, InventoryException {
    // TESTING getTotalPriceOfInventory
        final Administrator admin = new Administrator("admin", "admin");
        final double totalOfInventory = admin.getTotalPriceOfInventory(dataSource.getConnection());
        assertEquals("getTotalPriceOfInventory succeeds", 11.6, totalOfInventory);

    // TESTING getTotalPriceOfAllOrders
        final double totalOfOrders = admin.getTotalPriceOfAllOrders(dataSource.getConnection());
        assertEquals("getTotalPriceOfAllOrders succeeds", 4.95, totalOfOrders);

    // TESTING addProductToInventory
        final Product newProduct = new Product("turkey", 1.5, 3);
        final int amount = 1;
        admin.addNewProductToInventory(dataSource.getConnection(),newProduct, amount);

        DBCursorHolder cursor = DBUtils.innerJoinTables(dataSource.getConnection(), "products", "inventory", "product_id",
                new String[]{"product_name", "product_price", "product_amount"}, new String[]{String.format("product_name = '%s'",newProduct.getName())});
        String newProductString = "";

        while (cursor.getResults().next()) {
            newProductString += String.format("%s ",cursor.getResults().getString(1));
            newProductString += String.format("%s ",cursor.getResults().getString(2));
            newProductString += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("addProductToInventory succeeds", "turkey 3.00 1 ", newProductString);
        cursor.closeCursor();

    // TESTING removeProductFromInventory
        admin.decreaseProductAmountInInventory(dataSource.getConnection(), newProduct, 1);

        cursor = DBUtils.innerJoinTables(dataSource.getConnection(), "products", "inventory", "product_id",
                new String[]{"product_name", "product_price", "product_amount"}, new String[]{String.format("product_name = '%s'",newProduct.getName())});
        newProductString = "";

        while (cursor.getResults().next()) {
            newProductString += String.format("%s ",cursor.getResults().getString(1));
            newProductString += String.format("%s ",cursor.getResults().getString(2));
            newProductString += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("removeProductToInventory succeeds", "turkey 3.00 0 ", newProductString);
        cursor.closeCursor();

    // TESTING createUser
        admin.createUser(dataSource.getConnection(), "testUser3", "testUser3", false);
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "users", new String[]{"user_name"}, new String[]{"user_password = 'testUser3'"});
        cursor.getResults().next();

        assertEquals("createUser succeeds", "testUser3", cursor.getResults().getString(1));
        cursor.closeCursor();

    // TESTING deleteUser
        admin.deleteUser(dataSource.getConnection(), "testUser3");
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "users", new String[]{"user_name"}, new String[]{"user_password = 'testUser3'"});
        String empty = "";
        while (cursor.getResults().next()) {
            empty += cursor.getResults().getString(1);
        }

        assertEquals("deleteUser succeeds", "", empty);
        cursor.closeCursor();
    }
}
