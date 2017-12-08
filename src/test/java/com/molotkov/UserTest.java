package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.users.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class UserTest {
    private HikariDataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Before
    public void setUp() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(20);
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
                " order_owner text REFERENCES users(user_name) ON DELETE CASCADE, address text NOT NULL, completed boolean DEFAULT FALSE," +
                " created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 1, 'testUser1', 'Manchester' )");
        statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 2, 'testUser2', 'London' )");

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
    public void testUserSuperClassMethods() throws SQLException, InterruptedException {
    //-------------- User test cases -------------------------------
        User testUser = new User("testUser", "testUser");
        assertEquals("Constructor succeeds", true, testUser instanceof User);

        assertEquals("getUserName succeeds", "testUser", testUser.getUserName());
        assertEquals("getUserPasswd succeeds", "testUser", testUser.getUserPasswd());

        User testUser1 = new User("testUser1", "testUser1");
        User testUser2 = new User("testUser2", "testUser2");
        User admin = new User("admin", "admin");

        // Ensure testUser1 gets only his orders
        DBCursorHolder cursor = testUser1.fetchOrders(dataSource.getConnection(), new String[]{});
        String orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(1));
            orders += String.format("%s ",cursor.getResults().getString(2));
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("testUser1 has only his orders", "apple,chicken 1,2 Manchester ", orders);
        cursor.closeCursor();

        // Ensure testUser2 gets only his orders
        cursor = testUser2.fetchOrders(dataSource.getConnection(), new String[]{});
        orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(1));
            orders += String.format("%s ",cursor.getResults().getString(2));
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("testUser2 has only his orders", "apple 2 London ", orders);
        cursor.closeCursor();


        // Ensure user can filter orders by date
        String date = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        date = String.format("orders.created_at < '%s'::date",date);
        cursor = testUser1.fetchOrders(dataSource.getConnection(), new String[]{date});
        orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(1));
            orders += String.format("%s ",cursor.getResults().getString(2));
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("testUser1 can filter only his orders by date", "apple,chicken 1,2 Manchester ", orders);
        cursor.closeCursor();

        // Ensure user can see inventory
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{});
        String inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("testUser can see inventory", "apple 0.150 0.80 3 chicken 1.000 2.30 4 ", inventory);
        cursor.closeCursor();

        // Ensure user can sort inventory by name
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{"product_name LIKE 'apple'"});
        inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("testUser can sort inventory by name", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure user can sort inventory by weight
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{"product_weight = 0.150"});
        inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("testUser can sort inventory by weight", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure user can sort inventory by price
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{"product_price = 0.8"});
        inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("testUser can sort inventory by price", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

    //-------------- Admin test cases ------------------------------
        // Ensure admin gets all orders
        cursor = admin.fetchOrders(dataSource.getConnection(), new String[]{});
        orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(1));
            orders += String.format("%s ",cursor.getResults().getString(2));
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("admin has all orders", "apple,chicken 1,2 Manchester apple 2 London ", orders);
        cursor.closeCursor();

        // Ensure admin can sort all orders by date
        date = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        date = String.format("orders.created_at < '%s'::date",date);
        cursor = admin.fetchOrders(dataSource.getConnection(), new String[]{date});
        orders = "";

        while (cursor.getResults().next()) {
            orders += String.format("%s ",cursor.getResults().getString(1));
            orders += String.format("%s ",cursor.getResults().getString(2));
            orders += String.format("%s ",cursor.getResults().getString(3));
        }

        assertEquals("admin can sort all orders by date", "apple,chicken 1,2 Manchester apple 2 London ", orders);
        cursor.closeCursor();

        // Ensure admin can see inventory
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{});
        inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("admin can see inventory", "apple 0.150 0.80 3 chicken 1.000 2.30 4 ", inventory);
        cursor.closeCursor();

        // Ensure admin can sort inventory by name
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{"product_name LIKE 'apple'"});
        inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("admin can sort inventory by name", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure admin can sort inventory by weight
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{"product_weight = 0.150"});
        inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("admin can sort inventory by weight", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure admin can sort inventory by price
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{"product_price = 0.8"});
        inventory = "";

        while (cursor.getResults().next()) {
            inventory += String.format("%s ",cursor.getResults().getString(2));
            inventory += String.format("%s ",cursor.getResults().getString(3));
            inventory += String.format("%s ",cursor.getResults().getString(4));
            inventory += String.format("%s ",cursor.getResults().getString(5));
        }

        assertEquals("admin can sort inventory by price", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        dataSource.close();
    }
}
