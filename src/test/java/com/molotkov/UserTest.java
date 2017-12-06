package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;
import com.molotkov.users.User;
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

public class UserTest {
    private HikariConfig hikariConfig;
    private HikariDataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Before
    public void setUp() throws SQLException {
        hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);
        Statement statement = dataSource.getConnection().createStatement();

        statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_passwd text NOT NULL," +
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
                " order_owner text REFERENCES users(user_name) ON DELETE CASCADE, address text NOT NULL, completed boolean DEFAULT FALSE" +
                " created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 1, 'testUser1', 'Manchester' )");
        statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 2, 'testUser2', 'London' )");

        statement.addBatch("CREATE TABLE IF NOT EXISTS products ( product_id serial PRIMARY KEY, product_name text NOT NULL," +
                " product_weight numeric (3,3) NOT NULL, product_price numeric (6,2) NOT NULL )");
        statement.addBatch("INSERT INTO products ( product_name, product_weight, product_price ) VALUES ( 'apple', 0.150, 0.8 )");
        statement.addBatch("INSERT INTO products ( product_name, product_weight, product_price ) VALUES ( 'chicken', 1, 2.3 )");

        statement.addBatch("CREATE TABLE IF NOT EXISTS inventory ( entry_id serial, " +
                "product_id int4 REFERENCES products(product_id) ON DELETE RESTRICT, product_amount int4 NOT NULL )");
        statement.addBatch("INSERT INTO inventory ( product_id, product_amount ) VALUES ( 1, 2 )");
        statement.addBatch("INSERT INTO inventory ( product_id, product_amount ) VALUES ( 2, 1 )");

        statement.executeBatch();
        statement.close();
    }

    @Test
    public void testUserSuperClass() {
        User testUser = new User("testUser", "testUser");
        assertEquals("Constructor succeeds", true, testUser instanceof User);

        assertEquals("getUserName succeeds", "testUser", testUser.getUserName());
        assertEquals("getUserPasswd succeeds", "testUser", testUser.getUserPasswd());
    }

    @Test
    public void testUserSuperClassDBMethods() throws SQLException {
        User testUser1 = new User("testUser1", "testUser1");
        User testUser2 = new User("testUser2", "testUser2");
        User admin = new User("admin", "admin");

        // Ensure testUser1 gets only his orders
        DBCursorHolder cursor = testUser1.fetchOrders(dataSource.getConnection(), new String[]{});
        String orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("testUser1 has only his orders", true, orders.contains("testUser1") && !orders.contains("testUser2"));
        cursor.closeCursor();

        // Ensure testUser2 gets only his orders
        cursor = testUser2.fetchOrders(dataSource.getConnection(), new String[]{});
        orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("testUser2 has only his orders", true, orders.contains("testUser2") && !orders.contains("testUser1"));
        cursor.closeCursor();

        // Ensure admin gets all orders
        cursor = testUser1.fetchOrders(dataSource.getConnection(), new String[]{});
        orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("admin has all orders", true, orders.contains("testUser1") && orders.contains("testUser2"));
        cursor.closeCursor();
    }
}
