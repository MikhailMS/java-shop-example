package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.gui.InventoryScene;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import static org.loadui.testfx.controls.Commons.hasLabel;
import static org.testfx.api.FxAssert.verifyThat;

public class InventorySceneTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        User admin = new Administrator("t", "t");
        User client = new Client("t", "t");
        Basket userBasket = new Basket();

        Inventory inventory = new Inventory();
        try {
            inventory.addProducts(new Product("chicken", 1, 2.3),3);
            inventory.addProducts(new Product("apple", 0.151, 0.8), 2);
        } catch (InventoryException e) {
            e.printStackTrace();
        }

        client.setBasket(userBasket);

        stage.setScene(new Scene(InventoryScene.createInventoryTableView(inventory, client), 600, 400));
        stage.show();
    }

    @Test
    public void should_contain_user_specific_columns() {
        verifyThat(".table-view", TableViewMatchers.hasTableCell("apple"));
    }

    @Test
    public void should_contain_data_in_rows_for_user() {

    }

    @Test
    public void should_add_product_to_basket_if_user() {

    }

    @Test
    public void should_remove_product_from_basket_if_user() {

    }

    @Test
    public void should_contain_admin_specific_columns() {

    }

    @Test
    public void should_contain_data_in_rows_for_admin() {

    }

    @Test
    public void should_add_new_product_to_inventory_if_admin() {

    }

    @Test
    public void should_remove_product_from_inventory_if_admin() {

    }

}
