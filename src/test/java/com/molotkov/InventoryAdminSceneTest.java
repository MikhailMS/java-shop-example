package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.InventoryScene;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
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

public class InventoryAdminSceneTest extends ApplicationTest {
    private HikariDataSource dataSource;
    private static boolean setupIsDone = false;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Override
    public void start(Stage stage) throws SQLException {
        User admin = new Administrator("t", "t");

        Inventory inventory = new Inventory();
        try {
            inventory.addProducts(new Product("chicken", 1, 2.3),3);
            inventory.addProducts(new Product("apple", 0.151, 0.8), 2);
        } catch (InventoryException e) {
            e.printStackTrace();
        }

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

            statement.addBatch("CREATE TABLE IF NOT EXISTS products ( product_id serial PRIMARY KEY, product_name text NOT NULL," +
                    " product_weight numeric (6,3) NOT NULL, product_price numeric (8,2) NOT NULL )");
            statement.addBatch("CREATE TABLE IF NOT EXISTS inventory ( entry_id serial, " +
                    "product_id int4 REFERENCES products(product_id) ON DELETE RESTRICT, product_amount int4 NOT NULL )");

            statement.executeBatch();
            statement.close();
            setupIsDone = true;
        }
        // TestContainers ends
        stage.setScene(new Scene(InventoryScene.createMainInventoryBox(inventory, admin, dataSource.getConnection()), WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    @Test
    public void should_contain_specific_inventory_columns_for_admin() {
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Name"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Weight"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Price"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Quantity available in Inventory"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Total Price"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Details"));
        dataSource.close();
    }

    @Test
    public void should_contain_data_in_rows_for_admin() {
        verifyThat(".table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 2, "1.60", false));
        verifyThat(".table-view", TableViewMatchers.containsRow("chicken", 1.0, 2.3, 3, "6.90", false));
        dataSource.close();
    }

    @Test
    public void can_create_new_product_to_inventory_if_admin() {
        ((TextField) GuiTest.find("#name")).setText("milk");
        ((TextField) GuiTest.find("#weight")).setText("1.0");
        ((TextField) GuiTest.find("#price")).setText("1.0");
        ((TextField) GuiTest.find("#amount")).setText("5");
        clickOn("Add new product");
        sleep(2000);
        verifyThat(".table-view", TableViewMatchers.containsRow("milk", 1.0, 1.0, 5, "5.00", false));
        dataSource.close();
    }

    @Test
    public void can_increase_amount_new_product_if_admin() {
        ((TextField) GuiTest.find("#name")).setText("milk");
        ((TextField) GuiTest.find("#weight")).setText("1.0");
        ((TextField) GuiTest.find("#price")).setText("1.0");
        ((TextField) GuiTest.find("#amount")).setText("5");
        clickOn("Add new product");
        sleep(1000);
        clickOn("Product Name");
        clickOn((Node)from(lookup(".expander-button")).nth(2).query());
        clickOn("Add to inventory");
        sleep(1000);
        verifyThat(".table-view", TableViewMatchers.containsRow("milk", 1.0, 1.0, 6, "6.00", false));
        dataSource.close();
    }

    @Test
    public void can_decrease_new_product_if_admin() {
        ((TextField) GuiTest.find("#name")).setText("milk");
        ((TextField) GuiTest.find("#weight")).setText("1.0");
        ((TextField) GuiTest.find("#price")).setText("1.0");
        ((TextField) GuiTest.find("#amount")).setText("5");
        clickOn("Add new product");
        sleep(1000);
        clickOn("Product Name");
        clickOn((Node)from(lookup(".expander-button")).nth(2).query());
        clickOn("Remove from inventory");
        sleep(1000);
        verifyThat(".table-view", TableViewMatchers.containsRow("milk", 1.0, 1.0, 4, "4.00", false));
        dataSource.close();
    }

    @Test
    public void cannot_decrease_product_amount_below_zero_if_admin() {
        clickOn("Product Name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Remove from inventory")
                .sleep(2200)
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Remove from inventory")
                .sleep(2200)
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Remove from inventory");
        verifyThat(lookup("Something went wrong while removing product from inventory: Possibly you tried to remove more occurrences of a product than exist in inventory"), Node::isVisible);
        dataSource.close();
    }

    @Test
    public void cannot_create_new_product_if_details_incomplete() {
        ((TextField) GuiTest.find("#name")).setText("milk");
        clickOn("Add new product");
        verifyThat(lookup("One of the fields is empty. Make sure all product descriptors are filled in"), Node::isVisible);
        sleep(3000);
        ((TextField) GuiTest.find("#weight")).setText("1.0");
        clickOn("Add new product");
        verifyThat(lookup("One of the fields is empty. Make sure all product descriptors are filled in"), Node::isVisible);
        sleep(3000);
        ((TextField) GuiTest.find("#price")).setText("1.0");
        clickOn("Add new product");
        verifyThat(lookup("One of the fields is empty. Make sure all product descriptors are filled in"), Node::isVisible);
        sleep(3000);
        ((TextField) GuiTest.find("#amount")).setText("5");
        clickOn("Add new product");
        verifyThat(lookup("One of the fields is empty. Make sure all product descriptors are filled in"), Node::isVisible);
        sleep(1000);
        dataSource.close();
    }
}
