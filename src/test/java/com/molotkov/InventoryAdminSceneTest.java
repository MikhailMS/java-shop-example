package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.InventoryScene;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
import com.molotkov.users.User;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

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
        verifyThat(".table-view", TableViewMatchersExtension.containsRow(new String[]{"apple", "0.151", "0.8", "2", "1.60", "false"}));
        verifyThat(".table-view", TableViewMatchersExtension.containsRow(new String[]{"chicken", "1.0", "2.3", "3", "6.90", "false"}));
    }

    @Test
    public void can_add_new_product_to_inventory_if_admin() {

    }

    @Test
    public void can_remove_product_from_inventory_if_admin() {

    }

}
