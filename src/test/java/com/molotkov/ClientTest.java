package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;
import com.molotkov.users.Client;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.SQLException;
import java.sql.Statement;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;

public class ClientTest {
    private HikariDataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Before
    public void setUp() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(25);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);
        Statement statement = dataSource.getConnection().createStatement();

        statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_passwd text NOT NULL," +
                " privileges boolean DEFAULT FALSE )");
        statement.addBatch("INSERT INTO users VALUES ( 'client', 'client', FALSE )");

        statement.addBatch(" CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY," +
                " basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL," +
                " products_amount text NOT NULL, processed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");

        statement.executeBatch();
        statement.close();
    }

    @Test
    public void testClientMethods() throws BasketException, SQLException {
        Client client = new Client("client", "client");
        Basket basket = new Basket();
        Product apple = new Product("apple", 0.150, 0.8);
    // TESTING addProductToBasket
        client.addProductToBasket(basket, apple, 2);
        assertEquals("addProductToBasket succeeds", true, basket.getProducts().containsKey(apple));

    // TESTING removeProductFromBasket
        client.removeProductFromBasket(basket, apple, 2);
        assertEquals("removeProductFromBasket succeeds", false, basket.getProducts().containsKey(apple));

    // TESTING saveBasket
        client.addProductToBasket(basket, apple, 2);
        client.saveBasket(dataSource.getConnection(), basket);
        DBCursorHolder cursor = DBUtils.filterFromTable(dataSource.getConnection(), "baskets", new String[]{"basket_id"},
                new String[]{String.format("basket_owner = '%s'", client.getUserName())});
        cursor.getResults().next();

        String result = cursor.getResults().getString(1);
        assertEquals("saveBasket succeeds", "1", result);

    // TESTING restoreBasket
        Basket restoredBasket = client.restoreBasket(dataSource.getConnection());
        assertEquals("restoreBasket succeeds", basket.toString(), restoredBasket.toString());
    }
}
