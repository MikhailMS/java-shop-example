package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.InventoryScene;
import com.molotkov.products.Product;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.ClassRule;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import java.sql.SQLException;
import java.sql.Statement;

import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

import static org.testfx.api.FxAssert.verifyThat;

public class InventoryClientSceneTest extends ApplicationTest {
    private HikariDataSource dataSource;
    private static boolean setupIsDone = false;


    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Override
    public void start(Stage stage) throws SQLException {
        User client = new Client("t", "t");
        Basket userBasket = new Basket();

        // TestContainers bit
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(35);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);

        if(!setupIsDone) {
            System.out.println("Hallo ee");
            final Statement statement = dataSource.getConnection().createStatement();

            statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_passwd text NOT NULL," +
                    " privileges boolean DEFAULT FALSE )");
            statement.addBatch("INSERT INTO users VALUES ( 't', 't', FALSE )");

            statement.addBatch(" CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY," +
                    " basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL," +
                    " products_amount text NOT NULL, processed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");

            statement.addBatch("CREATE TABLE IF NOT EXISTS orders ( order_id serial, basket_id int4 REFERENCES baskets(basket_id) ON DELETE CASCADE," +
                    " order_owner text REFERENCES users(user_name) ON DELETE CASCADE, address text NOT NULL," +
                    " completed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");

            statement.executeBatch();
            statement.close();
            setupIsDone = true;
        }
        // TestContainers ends

        Inventory inventory = new Inventory();
        try {
            inventory.addProducts(new Product("chicken", 1, 2.3),3);
            inventory.addProducts(new Product("apple", 0.151, 0.8), 2);
        } catch (InventoryException e) {
            e.printStackTrace();
        }

        client.setBasket(userBasket);

        try {
            stage.setScene(new Scene(InventoryScene.createMainInventoryBox(inventory, client, dataSource.getConnection()), WINDOW_WIDTH, WINDOW_HEIGHT));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stage.show();
    }

    @Test
    public void should_contain_specific_inventory_columns_for_user() {
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Name"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Weight"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Price"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Quantity available in Inventory"));
        verifyThat(".table-view", TableViewMatchersExtension.hasNoColumnWithID("Product Total Price"));
        dataSource.close();
    }

    @Test
    public void should_contain_data_in_rows_for_user() {
        verifyThat(".table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 2, false));
        verifyThat(".table-view", TableViewMatchers.containsRow("chicken", 1.0, 2.3, 3, false));
        dataSource.close();
    }

    @Test
    public void can_add_product_to_basket_if_user() {
        clickOn((Node)from(lookup(".expander-button")).nth(0).query()).clickOn("Add to basket");
        sleep(1000);
        verifyThat(lookup("Product has been added to basket"), Node::isVisible);
        dataSource.close();
    }

    @Test
    public void can_remove_product_from_basket_if_user() {
        clickOn((Node)from(lookup(".expander-button")).nth(0).query()).clickOn("Add to basket").clickOn("Remove from basket");
        sleep(1000);
        verifyThat(lookup("Product has been removed from basket"), Node::isVisible);
        dataSource.close();
    }

    @Test
    public void should_see_columns_basket_table_if_user() {
        verifyThat("#basket-table-view", TableViewMatchersExtension.hasColumnWithID("Basket Total"));
        verifyThat("#basket-table-view", TableViewMatchersExtension.hasColumnWithID("Order Details"));
        dataSource.close();
    }

    @Test
    public void can_see_basket_total_if_user() {
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.00", false));
        dataSource.close();
    }

    @Test
    public void can_see_updated_basket_total_add_product() {
        clickOn("Product Name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Add to basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.80", false));
        dataSource.close();
    }

    @Test
    public void can_see_updated_basket_total_remove_product() {
        clickOn("Product Name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Add to basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.80", false));
        clickOn("Remove from basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.00", false));
        dataSource.close();
    }

    @Test
    public void can_complete_order() {
        clickOn("Product Name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Add to basket")
                .clickOn((Node)from(lookup(".expander-button")).nth(2).query());
        ((TextField) GuiTest.find("#delivery-address")).setText("London");
        clickOn("Complete order");
        verifyThat(lookup("Order has been made"), Node::isVisible);
        dataSource.close();
    }

    @Test
    public void cannot_complete_order_wo_products() {
        clickOn((Node)from(lookup(".expander-button")).nth(2).query());
        ((TextField) GuiTest.find("#delivery-address")).setText("London");
        clickOn("Complete order");
        verifyThat(lookup("Cannot process the order: Add products to basket to complete order"), Node::isVisible);
        dataSource.close();
    }

    @Test
    public void cannot_complete_order_wo_address() {
        clickOn("Product Name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Add to basket")
                .clickOn((Node)from(lookup(".expander-button")).nth(2).query())
                .clickOn("Complete order");
        verifyThat(lookup("Cannot process the order: Enter the delivery address"), Node::isVisible);
        dataSource.close();
    }
}
