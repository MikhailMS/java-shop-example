package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class OrderDBTest {
    private static HikariDataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @After
    public void closeConnection() throws SQLException {
        dataSource.getConnection().close();
    }

    @AfterClass
    public static void closeDataSource() {
        dataSource.close();
    }

    @Before
    public void setUp() throws SQLException {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);
        final Statement statement = dataSource.getConnection().createStatement();

        statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_passwd text NOT NULL," +
                " privileges boolean DEFAULT FALSE )");
        statement.addBatch("CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY," +
                " basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL," +
                " products_amount text NOT NULL, processed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.addBatch("CREATE TABLE IF NOT EXISTS orders ( order_id serial," +
                " basket_id int4 REFERENCES baskets(basket_id) ON DELETE CASCADE, order_owner text REFERENCES users(user_name) ON DELETE CASCADE," +
                " address text NOT NULL, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.addBatch("INSERT INTO users VALUES ( 'testUser', 'testUser', FALSE )");

        statement.executeBatch();
        statement.close();
    }

    @Test
    public void testOrderToFromDB() throws SQLException {
    // -------------------
        final Basket testBasket = new Basket();
        try {
            testBasket.addProducts(new Product("apple", 0.150, 0.8), 2);
        } catch (BasketException ex) {
            ex.printStackTrace();
        }
        final Order savedOrder = new Order(testBasket, "London");

        // Save basket
        final ArrayList<String> valuesList = new ArrayList<>();
        final List<String> basketDetails = testBasket.toDBFormat();
        final String names = basketDetails.get(0);
        final String amounts = basketDetails.get(1);

        valuesList.add("'testUser'");
        valuesList.add(String.format("'%s'",names));
        valuesList.add(String.format("%s", amounts));

        DBUtils.insertSpecificIntoTable(dataSource.getConnection(),"baskets",
                new String[] {"basket_owner", "products_name", "products_amount"}, valuesList.toArray(new String[0]));

        // Get basket_id
        DBCursorHolder cursor = DBUtils.filterFromTable(dataSource.getConnection(), "baskets", new String[]{"basket_id"},
                new String[]{"basket_owner = 'testUser'", "AND", "processed = FALSE"});
        cursor.getResults().next();
        final String basketId = cursor.getResults().getString(1);
        cursor.closeCursor();

        // Save order
        final ArrayList<String> orderValuesList = new ArrayList<>();
        orderValuesList.add(basketId);
        orderValuesList.add(String.format("'%s'",savedOrder.getAddress()));
        DBUtils.insertSpecificIntoTable(dataSource.getConnection(), "orders", new String[]{"basket_id","address"},
                orderValuesList.toArray(new String[0]));

        // Update basket to be processed = TRUE
        DBUtils.updateTable(dataSource.getConnection(), "baskets", new String[]{"processed"}, new String[]{"TRUE"},
                new String[]{"basket_owner = 'testUser'","basket_id = "+basketId});

        // Check if order been saved
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "orders", new String[]{"order_id","address"},
                new String[]{"basket_id = "+basketId});
        cursor.getResults().next();
        final String orderId = cursor.getResults().getString(1);
        final String orderAddress = cursor.getResults().getString(2);
        assertEquals("Save order query succeeded", "Order ID 1 @ London", String.format("Order ID %s @ %s", orderId, orderAddress));
        cursor.closeCursor();
    // -------------------
        // Retrieve order's information
        cursor = DBUtils.selectFromTable(dataSource.getConnection(), "orders", new String[]{"basket_id","address"});
        cursor.getResults().next();

        final String orderRetrieveBasketId = cursor.getResults().getString(1);
        final String orderRetrieveAddress = cursor.getResults().getString(2);
        cursor.closeCursor();

        // Restore related basket
        cursor = DBUtils.filterFromTable(dataSource.getConnection(), "baskets", new String[]{"products_name","products_amount"},
                new String[]{"basket_id = "+orderRetrieveBasketId, "AND", "basket_owner = 'testUser'"});
        cursor.getResults().next();

        final String basketRetrievedProductNames = cursor.getResults().getString(1);
        final String basketRetrievedProductAmounts = cursor.getResults().getString(2);
        cursor.closeCursor();

        final Basket restoredBasket = new Basket();
        restoredBasket.restoreFromDB(basketRetrievedProductNames, basketRetrievedProductAmounts);

        // Restore order
        final Order restoredOrder = new Order(restoredBasket, orderRetrieveAddress);

        // Check if orders match
        assertEquals("Retrieve order query succeeded", savedOrder.toString(), restoredOrder.toString());

        closeConnection();
    }
}
