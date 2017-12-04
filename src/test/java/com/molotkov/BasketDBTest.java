package com.molotkov;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;
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

public class BasketDBTest {
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
        statement.execute("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_passwd text NOT NULL, privileges boolean DEFAULT FALSE )");
        statement.execute(" CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY, basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL, products_amount text NOT NULL, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
        statement.execute("INSERT INTO users VALUES ( 'testUser', 'testUser', FALSE )");
        statement.close();
    }

    @Test
    public void testSaveBasketToDB() throws SQLException {
        Basket testBasket = new Basket();
        try {
            testBasket.addProducts(new Product("apple", 0.150, 0.8),2);
        } catch (BasketException ex) {
            ex.printStackTrace();
        }
        ArrayList<String> valuesList = new ArrayList<>();
        valuesList.add("1");
        valuesList.add("testUser");
        valuesList.addAll(testBasket.toDBFormat());
        DBUtils.insertIntoTable(dataSource.getConnection(), "baskets", valuesList.toArray(new String[0]));

        DBCursorHolder cursor = DBUtils.selectFromTable(dataSource.getConnection(), "baskets", new String[] {"products_name"});
        cursor.getResults().next();
        final String resultString1 = cursor.getResults().getString(1);
        assertEquals("SaveBasketToDB succeeded", "apple",resultString1);
        cursor.closeCursor();
    }

    @Test
    public void testRetrieveBasketFromDB() throws SQLException {
        Basket testBasket = new Basket();

        Statement statement = dataSource.getConnection().createStatement();
        statement.execute("INSERT INTO baskets ( basket_owner, products_name, products_amount ) VALUES ( 'testUser', 'apple', '1' )");
        statement.close();

        DBCursorHolder cursor = DBUtils.filterFromTable(dataSource.getConnection(),"baskets", new String[] {"products_name", "products_amount"}, new String[] {"basket_owner = 'testUser'"});

        cursor.getResults().next();
        final String productsName = cursor.getResults().getString(1);
        final String productsAmount = cursor.getResults().getString(2);

        testBasket.restoreFromDB(productsName, productsAmount);
        assertEquals("RetrieveBasketFromDB succeeded", "Basket has 1 product.",testBasket.toString());
        cursor.closeCursor();
    }
}
