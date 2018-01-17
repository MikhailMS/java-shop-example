package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.users.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class UserTest {
    private static HikariDataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @AfterClass
    public static void closeDataSource() {
        dataSource.close();
    }

    @Before
    public void setUp() throws SQLException {
        final HikariConfig hikariConfig = new HikariConfig();
        //hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);
        final Statement statement = dataSource.getConnection().createStatement();

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
    public void testUserClassMethods() throws SQLException {
    //-------------- User test cases -------------------------------
        final User testUser = new User("testUser", "testUser");
        assertEquals("Constructor succeeds", true, testUser instanceof User);

        assertEquals("getUserName succeeds", "testUser", testUser.getUserName());
        assertEquals("getUserPasswd succeeds", "testUser", testUser.getUserPasswd());

        final Basket testBasket = new Basket();
        testUser.setBasket(testBasket);

        assertEquals("set/get User Basket succeeds", testBasket, testUser.getBasket());

        final User testUser1 = new User("testUser1", "testUser1");
        final User testUser2 = new User("testUser2", "testUser2");
        final User admin = new User("admin", "admin");

        // Ensure testUser1 gets only his orders
        DBCursorHolder cursor = testUser1.fetchOrders(dataSource.getConnection(), new String[]{});

        final StringBuilder ordersBuilder = new StringBuilder();
        while (cursor.getResults().next()) {
            ordersBuilder.append(String.format("%s ", cursor.getResults().getString(1)));
            ordersBuilder.append(String.format("%s ", cursor.getResults().getString(2)));
            ordersBuilder.append(String.format("%s ", cursor.getResults().getString(3)));
        }
        String orders = ordersBuilder.toString();

        assertEquals("testUser1 has only his orders", "apple,chicken 1,2 Manchester ", orders);
        cursor.closeCursor();

        // Ensure testUser2 gets only his orders
        cursor = testUser2.fetchOrders(dataSource.getConnection(), new String[]{});

        final StringBuilder ordersBuilder1 = new StringBuilder();
        while (cursor.getResults().next()) {
            ordersBuilder1.append(String.format("%s ", cursor.getResults().getString(1)));
            ordersBuilder1.append(String.format("%s ", cursor.getResults().getString(2)));
            ordersBuilder1.append(String.format("%s ", cursor.getResults().getString(3)));
        }
        orders = ordersBuilder1.toString();

        assertEquals("testUser2 has only his orders", "apple 2 London ", orders);
        cursor.closeCursor();


        // Ensure user can filter orders by date
        String date = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        date = String.format("orders.created_at < '%s'::date",date);
        cursor = testUser1.fetchOrders(dataSource.getConnection(), new String[]{date});

        final StringBuilder ordersBuilder2 = new StringBuilder();
        while (cursor.getResults().next()) {
            ordersBuilder2.append(String.format("%s ", cursor.getResults().getString(1)));
            ordersBuilder2.append(String.format("%s ", cursor.getResults().getString(2)));
            ordersBuilder2.append(String.format("%s ", cursor.getResults().getString(3)));
        }
        orders = ordersBuilder2.toString();

        assertEquals("testUser1 can filter only his orders by date", "apple,chicken 1,2 Manchester ", orders);
        cursor.closeCursor();

        // Ensure user can see inventory
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{});

        final StringBuilder inventoryBuilder = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        String inventory = inventoryBuilder.toString();

        assertEquals("testUser can see inventory", "apple 0.150 0.80 3 chicken 1.000 2.30 4 ", inventory);
        cursor.closeCursor();

        // Ensure user can sort inventory by name
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{"product_name LIKE 'apple'"});

        final StringBuilder inventoryBuilder1 = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder1.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder1.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder1.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder1.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        inventory = inventoryBuilder1.toString();

        assertEquals("testUser can sort inventory by name", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure user can sort inventory by weight
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{"product_weight = 0.150"});

        final StringBuilder inventoryBuilder2 = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder2.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder2.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder2.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder2.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        inventory = inventoryBuilder2.toString();

        assertEquals("testUser can sort inventory by weight", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure user can sort inventory by price
        cursor = testUser1.fetchInventory(dataSource.getConnection(), new String[]{"product_price = 0.8"});

        final StringBuilder inventoryBuilder3 = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder3.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder3.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder3.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder3.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        inventory = inventoryBuilder3.toString();

        assertEquals("testUser can sort inventory by price", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

    //-------------- Admin test cases ------------------------------
        // Ensure admin gets all orders
        cursor = admin.fetchOrders(dataSource.getConnection(), new String[]{});

        final StringBuilder ordersBuilder3 = new StringBuilder();
        while (cursor.getResults().next()) {
            ordersBuilder3.append(String.format("%s ", cursor.getResults().getString(1)));
            ordersBuilder3.append(String.format("%s ", cursor.getResults().getString(2)));
            ordersBuilder3.append(String.format("%s ", cursor.getResults().getString(3)));
        }
        orders = ordersBuilder3.toString();

        assertEquals("admin has all orders", "apple,chicken 1,2 Manchester apple 2 London ", orders);
        cursor.closeCursor();

        // Ensure admin can sort all orders by date
        date = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        date = String.format("orders.created_at < '%s'::date",date);
        cursor = admin.fetchOrders(dataSource.getConnection(), new String[]{date});

        final StringBuilder ordersBuilder4 = new StringBuilder();
        while (cursor.getResults().next()) {
            ordersBuilder4.append(String.format("%s ", cursor.getResults().getString(1)));
            ordersBuilder4.append(String.format("%s ", cursor.getResults().getString(2)));
            ordersBuilder4.append(String.format("%s ", cursor.getResults().getString(3)));
        }
        orders = ordersBuilder4.toString();

        assertEquals("admin can sort all orders by date", "apple,chicken 1,2 Manchester apple 2 London ", orders);
        cursor.closeCursor();

        // Ensure admin can see inventory
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{});

        final StringBuilder inventoryBuilder4 = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder4.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder4.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder4.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder4.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        inventory = inventoryBuilder4.toString();

        assertEquals("admin can see inventory", "apple 0.150 0.80 3 chicken 1.000 2.30 4 ", inventory);
        cursor.closeCursor();

        // Ensure admin can sort inventory by name
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{"product_name LIKE 'apple'"});

        final StringBuilder inventoryBuilder5 = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder5.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder5.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder5.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder5.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        inventory = inventoryBuilder5.toString();

        assertEquals("admin can sort inventory by name", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure admin can sort inventory by weight
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{"product_weight = 0.150"});

        final StringBuilder inventoryBuilder6 = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder6.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder6.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder6.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder6.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        inventory = inventoryBuilder6.toString();

        assertEquals("admin can sort inventory by weight", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();

        // Ensure admin can sort inventory by price
        cursor = admin.fetchInventory(dataSource.getConnection(), new String[]{"product_price = 0.8"});

        final StringBuilder inventoryBuilder7 = new StringBuilder();
        while (cursor.getResults().next()) {
            inventoryBuilder7.append(String.format("%s ", cursor.getResults().getString(2)));
            inventoryBuilder7.append(String.format("%s ", cursor.getResults().getString(3)));
            inventoryBuilder7.append(String.format("%s ", cursor.getResults().getString(4)));
            inventoryBuilder7.append(String.format("%s ", cursor.getResults().getString(5)));
        }
        inventory = inventoryBuilder7.toString();

        assertEquals("admin can sort inventory by price", "apple 0.150 0.80 3 ", inventory);
        cursor.closeCursor();
    }
}
