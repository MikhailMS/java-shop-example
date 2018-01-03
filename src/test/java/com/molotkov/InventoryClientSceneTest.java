package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.InventoryScene;
import com.molotkov.products.Product;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import static org.testfx.api.FxAssert.verifyThat;

public class InventoryClientSceneTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
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
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Name"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Weight"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Price"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Quantity available in Inventory"));
        verifyThat(".table-view", TableViewMatchersExtension.hasNoColumnWithID("Product Total Price"));
    }

    @Test
    public void should_contain_data_in_rows_for_user() {
        verifyThat(".table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 2, false));
        verifyThat(".table-view", TableViewMatchers.containsRow("chicken", 1.0, 2.3, 3, false));
    }

    @Test
    public void can_add_product_to_basket_if_user() {
        clickOn(".table-view .table-cell .button").clickOn("Add to basket");
        sleep(2000);
        verifyThat(lookup("Product has been added to basket"), Node::isVisible);
    }

    @Test
    public void can_remove_product_from_basket_if_user() {
        clickOn(".table-view .table-cell .button").clickOn("Add to basket").clickOn("Remove from basket");
        sleep(2000);
        verifyThat(lookup("Product has been deleted from basket"), Node::isVisible);
    }
}
