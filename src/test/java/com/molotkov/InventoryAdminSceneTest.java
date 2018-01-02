package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.InventoryScene;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
import com.molotkov.users.User;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import static org.testfx.api.FxAssert.verifyThat;

public class InventoryAdminSceneTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        User admin = new Administrator("t", "t");

        Inventory inventory = new Inventory();
        try {
            inventory.addProducts(new Product("chicken", 1, 2.3),3);
            inventory.addProducts(new Product("apple", 0.151, 0.8), 2);
        } catch (InventoryException e) {
            e.printStackTrace();
        }

        stage.setScene(new Scene(InventoryScene.createInventoryTableView(inventory, admin), 600, 400));
        stage.show();
    }

    @Test
    public void should_contain_admin_specific_columns() {
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Name"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Weight"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Price"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Quantity available in Inventory"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Total Price"));
    }

    @Test
    public void should_contain_data_in_rows_for_admin() {
        verifyThat(".table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 2, "1.60", false));
        verifyThat(".table-view", TableViewMatchers.containsRow("chicken", 1.0, 2.3, 3, "6.90", false));
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
    }

    @Test
    public void can_remove_new_product_from_inventory_if_admin() {
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
        verifyThat(lookup("Something went wrong while removing product from inventory: Possibly you tried to delete more occurrences of a product than exist in inventory"), Node::isVisible);
    }
}
