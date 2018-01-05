package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.InventoryScene;
import com.molotkov.products.Product;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

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

        stage.setScene(new Scene(InventoryScene.createMainInventoryBox(inventory, client), WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    @Test
    public void should_contain_specific_inventory_columns_for_user() {
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
        clickOn((Node)from(lookup(".expander-button")).nth(0).query()).clickOn("Add to basket");
        sleep(1000);
        verifyThat(lookup("Product has been added to basket"), Node::isVisible);
    }

    @Test
    public void can_remove_product_from_basket_if_user() {
        clickOn((Node)from(lookup(".expander-button")).nth(0).query()).clickOn("Add to basket").clickOn("Remove from basket");
        sleep(1000);
        verifyThat(lookup("Product has been removed from basket"), Node::isVisible);
    }

    @Test
    public void should_see_columns_basket_table_if_user() {
        verifyThat("#basket-table-view", TableViewMatchersExtension.hasColumnWithID("Basket Total"));
        verifyThat("#basket-table-view", TableViewMatchersExtension.hasColumnWithID("Order Details"));
    }

    @Test
    public void can_see_basket_total_if_user() {
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.00", false));
    }

    @Test
    public void can_see_updated_basket_total_add_product() {
        clickOn("Product Name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Add to basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.80", false));
    }

    @Test
    public void can_see_updated_basket_total_remove_product() {
        clickOn("Product Name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Add to basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.80", false));
        clickOn("Remove from basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow("0.00", false));
    }

    @Test
    public void can_complete_order() {
        clickOn((Node)from(lookup(".expander-button")).nth(2).query());
        ((TextField) GuiTest.find("#delivery-address")).setText("London");
        clickOn("Complete order");
        verifyThat(lookup("Order has been made"), Node::isVisible);
    }
}
