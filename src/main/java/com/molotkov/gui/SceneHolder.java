package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Inventory;
import com.molotkov.Order;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.sql.Connection;
import java.util.List;

import static com.molotkov.gui.GuiWindowConsts.HBOX_SPACING;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

public class SceneHolder {

    public static Pane createClientPaneScene(final Connection connection, final Inventory shopInventory, final List<Order> userOrders,
                                             final Basket clientBasket, final User user) {
        // Need to load shopInventory
        GuiDbUtils.loadDataToInventory(connection, shopInventory, user);
        // Need to load userOrders
        GuiDbUtils.loadDataToOrders(user, connection, userOrders);
        // Need to load client basket, if it's been saved
        GuiDbUtils.loadSavedBasket((Client)user, connection, clientBasket);

        // Create Client scene
        user.setBasket(clientBasket);

        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab inventoryTab = new Tab();
        inventoryTab.setText("Inventory");
        final HBox inventoryBox = new HBox(HBOX_SPACING);
        inventoryBox.setAlignment(Pos.CENTER);
        inventoryBox.getChildren().add(InventoryScene.createMainInventoryBox(shopInventory, user, connection));
        inventoryTab.setContent(inventoryBox);

        final Tab orderHistoryTab = new Tab();
        orderHistoryTab.setText("Order History");
        final HBox orderHistoryBox = new HBox(HBOX_SPACING);
        orderHistoryBox.setAlignment(Pos.CENTER);
        orderHistoryBox.getChildren().add(HistoryScene.syncTablesIntoOneTable(HistoryScene.createOrderTableView(userOrders), HistoryScene.createTotalOrderTableView(userOrders)));
        orderHistoryTab.setContent(orderHistoryBox);

        tabPane.getTabs().addAll(inventoryTab, orderHistoryTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // bind to take available space
        borderPane.prefHeightProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_HEIGHT));
        borderPane.prefWidthProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_WIDTH));
        borderPane.setCenter(tabPane);

        return borderPane;
    }

    public static Pane createAdminPaneScene(final Connection connection, final Inventory shopInventory, final List<Order> userOrders,
                                             final List<User> userList, final User user) {
        // Need to load shopInventory
        GuiDbUtils.loadDataToInventory(connection, shopInventory, user);
        // Need to load userOrders
        GuiDbUtils.loadDataToOrders(user, connection, userOrders);
        // Need to load users
        GuiDbUtils.loadDataToUserList(connection, userList);

        // Create Admin scene
        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab inventoryTab = new Tab();
        inventoryTab.setText("Inventory");
        final HBox inventoryBox = new HBox(HBOX_SPACING);
        inventoryBox.setAlignment(Pos.CENTER);
        inventoryBox.getChildren().add(InventoryScene.createMainInventoryBox(shopInventory, user, connection));
        inventoryTab.setContent(inventoryBox);

        final Tab orderHistoryTab = new Tab();
        orderHistoryTab.setText("Order History");
        final HBox orderHistoryBox = new HBox(HBOX_SPACING);
        orderHistoryBox.setAlignment(Pos.CENTER);
        orderHistoryBox.getChildren().add(HistoryScene.syncTablesIntoOneTable(HistoryScene.createOrderTableView(userOrders), HistoryScene.createTotalOrderTableView(userOrders)));
        orderHistoryTab.setContent(orderHistoryBox);

        final Tab controlUsersTab = new Tab();
        controlUsersTab.setText("System users");
        final HBox controlUsersBox = new HBox(HBOX_SPACING);
        controlUsersBox.setAlignment(Pos.CENTER);
        controlUsersBox.getChildren().add(ControlUsersScene.createControlTable(userList, (Administrator)user, connection));
        controlUsersTab.setContent(controlUsersBox);

        tabPane.getTabs().addAll(inventoryTab, orderHistoryTab, controlUsersTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // bind to take available space
        borderPane.prefHeightProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_HEIGHT));
        borderPane.prefWidthProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_WIDTH));
        borderPane.setCenter(tabPane);

        return borderPane;
    }
}
