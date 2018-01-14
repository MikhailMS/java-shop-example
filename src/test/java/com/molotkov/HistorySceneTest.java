package com.molotkov;

import com.molotkov.exceptions.BasketException;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.products.Product;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;
import static com.molotkov.gui.HistoryScene.*;

import static org.testfx.api.FxAssert.verifyThat;

public class HistorySceneTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        final Scene scene = new Scene(new Group());

        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);

        final Basket testBasket = new Basket();
        try {
            testBasket.addProducts(new Product("apple", 0.150, 0.8),3);
        } catch (BasketException e) {
            e.printStackTrace();
        }
        testBasket.setStringFormatter(()-> {
            final int basketSize = testBasket.getProducts().size();
            final String allProductsString = testBasket.getProducts().entrySet().stream()
                    .map(product -> {
                        final DecimalFormat df = new DecimalFormat("#.##");
                        final double price = Double.valueOf(df.format(product.getKey().getPrice()));
                        return String.format("%d %s @ %.2f£", product.getValue(), product.getKey().getName(), price);
                    })
                    .collect(Collectors.joining(", "));
            final String itemString = basketSize > 1 ? basketSize + " products" : basketSize + " product";
            return String.format("Order contains %s: %s", itemString, allProductsString);
        });

        final Order testOrder1 = new Order(testBasket, "London");
        final Order testOrder2 = new Order(testBasket, "Manchester");

        final List<Order> testOrders = new ArrayList<>();
        testOrders.add(testOrder1);
        testOrders.add(testOrder2);

        final TableView orderTable = createOrderTableView(testOrders);
        final TableView totalTable = createTotalOrderTableView(testOrders);
        totalTable.setPrefHeight(100);

        ((Group) scene.getRoot()).getChildren().addAll(syncTablesIntoOneTable(orderTable, totalTable));
        stage.setScene(scene);
        stage.show();
        syncScrollbars(orderTable, totalTable);
    }

    @Test
    public void should_contain_columns() {
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Delivery address"));
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Total order price"));
        verifyThat("#total-table", TableViewMatchersExtension.hasColumnWithID("Total of all orders"));
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void should_contain_rows_in_order_table() {
        verifyThat("#order-table", TableViewMatchers.containsRow("London", 2.4, false));
        verifyThat("#order-table", TableViewMatchers.containsRow("Manchester", 2.4, false));
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void should_contain_row_in_total_table() {
        verifyThat("#total-table", TableViewMatchersExtension.containsRow(
                TableViewMatchersExtension.REPLACEMENT_VALUE, 4.8, TableViewMatchersExtension.REPLACEMENT_VALUE)); //make sure row in total table has only one value
        verifyThat("#total-table", TableViewMatchers.hasTableCell(4.8));
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void should_contain_order_details_in_order_table() {
        clickOn((Node)from(lookup(".expander-button")).nth(0).query());
        verifyThat(lookup("Order contains 1 product: 3 apple @ 0.80£"), Node::isVisible);
        clickOn((Node)from(lookup(".expander-button")).nth(0).query());

        clickOn((Node)from(lookup(".expander-button")).nth(1).query());
        verifyThat(lookup("Order contains 1 product: 3 apple @ 0.80£"), Node::isVisible);
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
