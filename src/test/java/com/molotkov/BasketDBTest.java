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

public class BasketDBTest {
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
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);
        final Statement statement = dataSource.getConnection().createStatement();

        statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_passwd text NOT NULL," +
                " privileges boolean DEFAULT FALSE )");
        statement.addBatch(" CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY," +
                " basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL," +
                " products_amount text NOT NULL, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.addBatch("INSERT INTO users VALUES ( 'testUser', 'testUser', FALSE )");

        statement.executeBatch();
        statement.close();
    }

    @Test
    public void testBasketToFromDB() throws SQLException {
        final Basket savedBasket = new Basket();
        try {
            savedBasket.addProducts(new Product("apple", 0.150, 0.8),2);
        } catch (BasketException ex) {
            ex.printStackTrace();
        }
        final ArrayList<String> valuesList = new ArrayList<>();
        final List<String> basketDetails = savedBasket.toDBFormat();
        final String names = basketDetails.get(0);
        final String amounts = basketDetails.get(1);

        valuesList.add("'testUser'");
        valuesList.add(String.format("'%s'",names));
        valuesList.add(String.format("%s", amounts));

        DBUtils.insertSpecificIntoTable(dataSource.getConnection(),"baskets",
                new String[] {"basket_owner", "products_name", "products_amount"}, valuesList.toArray(new String[0]));

        DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "baskets", new String[] {"products_name"});
        cursor.getResults().next();
        final String productName = cursor.getResults().getString(1);
        assertEquals("SaveBasketToDB succeeded", "apple",productName);
        cursor.closeCursor();

        cursor = DBUtils.filterFromTable(dataSource.getConnection(),"baskets", new String[] {"products_name", "products_amount"},
                new String[] {"basket_owner = 'testUser'"});

        cursor.getResults().next();
        final String productsName = cursor.getResults().getString(1);
        final String productsAmount = cursor.getResults().getString(2);

        final Basket restoredBasket = new Basket();
        restoredBasket.restoreFromDB(productsName, productsAmount);
        assertEquals("RetrieveBasketFromDB succeeded", "Basket has 1 product.",restoredBasket.toString());
        cursor.closeCursor();
    }
}
