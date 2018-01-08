package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Inventory;
import com.molotkov.Order;
import com.molotkov.db.DBConnector;
import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

public class MainScreen extends Application {
    private static final String PRIMARY_STAGE_TITLE = "Java Super Shop";
    private static final Color PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private User user;
    private Inventory shopInventory = new Inventory();
    private Basket clientBasket = new Basket();
    private List<Order> userOrders = new ArrayList<>();
    private DBConnector connector;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(PRIMARY_STAGE_TITLE);

        connector = new DBConnector("jdbc:postgresql:test_db");

        final Group root = new Group();
        final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR);
        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab loginTab = new Tab();
        loginTab.setText("Login");
        final HBox loginBox = new HBox(10);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.getChildren().add(loginButton(connector.getConnection()));
        loginTab.setContent(loginBox);

        tabPane.getTabs().add(loginTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // bind to take available space
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());
        borderPane.setCenter(tabPane);

        scene.setRoot(borderPane);

        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }

    private Button loginButton(final Connection connection) {
        Button btn = new Button();
        btn.setText("Gain access to the Shop");

        btn.setOnAction(mainEvent -> loginAction(connection));
        return btn;
    }

    private void loginAction(final Connection connection) {
        // Create the custom dialog.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login");
        alert.setHeaderText("Login Dialog");
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login into Super Java Shop");
        dialog.setContentText("Enter your username and password : ");
        dialog.initModality(Modality.NONE);

        // Set login button
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField userName = new TextField();
        userName.setPromptText("e.g. m03j");
        PasswordField userPasswd = new PasswordField();
        userPasswd.setPromptText("xxxx");


        grid.add(new Label("Usermame: "), 0, 0);
        grid.add(userName, 1, 0);
        grid.add(new Label("Password: "), 0, 1);
        grid.add(userPasswd, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        userName.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        // Request focus on the player name field by default.
        Platform.runLater(() -> userName.requestFocus());

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.show();

        loginButton.addEventFilter(EventType.ROOT, e -> {
            try {
                userAuthentication(e, dialog, userName.getText(), userPasswd.getText(), connection);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void userAuthentication(Event e, Dialog dialog, String userName, String userPasswd, Connection connection) throws SQLException {
        if(e.getEventType().equals(ActionEvent.ACTION)){
            e.consume();
            if (isUserAllowed(userName, userPasswd, connection)) {
                if (isUserAdmin(userName, userPasswd, connection)) {
                    user = new Administrator(userName,userPasswd);
                    changeScene(createAdminPaneScene());
                } else {
                    user = new Client(userName, userPasswd);
                    changeScene(createClientPaneScene());
                }
                dialog.close();
            }
            else {
                ShakeTransition animation = new ShakeTransition(dialog.getDialogPane(), t->dialog.show());
                animation.playFromStart();
            }
        }
    }

    private boolean isUserAllowed(String userName, String userPasswd, Connection connection) throws SQLException {
        DBCursorHolder cursor = DBUtils.filterFromTable(connection, "users", new String[]{"user_name"},
                new String[]{String.format("user_name = '%s'", userName), "AND", String.format("user_password = '%s'",userPasswd)});
        while(cursor.getResults().next()) {
            if (cursor.getResults().getString(1).equals(userName)) {
                cursor.closeCursor();
                return true;
            } else {
                cursor.closeCursor();
                return false;
            }
        }
        return false;
    }

    private boolean isUserAdmin(String userName, String userPasswd, Connection connection) throws SQLException {
        DBCursorHolder cursor = DBUtils.filterFromTable(connection, "users", new String[]{"privileges"},
                new String[]{String.format("user_name = '%s'", userName), "AND", String.format("user_password = '%s'",userPasswd)});
        cursor.getResults().next();
        if (cursor.getResults().getBoolean(1)) {
            cursor.closeCursor();
            return true;
        } else {
            cursor.closeCursor();
            return false;
        }
    }

    private void changeScene(Pane pane) {
        primaryStage.getScene().setRoot(pane);
    }

    private Pane createClientPaneScene() {
        // Need to load shopInventory
        loadDataToInventory(connector, shopInventory);
        // Need to load userOrders
        loadDataToOrders(connector, userOrders);
        // Need to load client basket, if it's been saved
        loadSavedBasket(connector, clientBasket);

        // Create Client scene
        user.setBasket(clientBasket);

        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab inventoryTab = new Tab();
        inventoryTab.setText("Inventory");
        final HBox inventoryBox = new HBox(10);
        inventoryBox.setAlignment(Pos.CENTER);
        inventoryBox.getChildren().add(InventoryScene.createMainInventoryBox(shopInventory, user));
        inventoryTab.setContent(inventoryBox);

        final Tab orderHistoryTab = new Tab();
        orderHistoryTab.setText("Order History");
        final HBox orderHistoryBox = new HBox(10);
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

    private Pane createAdminPaneScene() {
        // Need to load shopInventory
        loadDataToInventory(connector, shopInventory);
        // Need to load userOrders
        loadDataToOrders(connector, userOrders);

        // Create Admin scene
        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab inventoryTab = new Tab();
        inventoryTab.setText("Inventory");
        final HBox inventoryBox = new HBox(10);
        inventoryBox.setAlignment(Pos.CENTER);
        inventoryBox.getChildren().add(InventoryScene.createMainInventoryBox(shopInventory, user));
        inventoryTab.setContent(inventoryBox);

        final Tab orderHistoryTab = new Tab();
        orderHistoryTab.setText("Order History");
        final HBox orderHistoryBox = new HBox(10);
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

    private void loadDataToInventory(final DBConnector connector, final Inventory inventory) {
        DBCursorHolder cursor;
        try {
            cursor = DBUtils.innerJoinTables(connector.getConnection(), "products", "inventory", "product_id",
                    new String[] {"product_name", "product_weight", "product_price", "product_amount"}, new String[]{});
            while (cursor.getResults().next()) {
                inventory.addProducts(new Product(cursor.getResults().getString(1),
                        cursor.getResults().getDouble(2), cursor.getResults().getDouble(3)),cursor.getResults().getInt(4));
            }
            cursor.closeCursor();
        } catch (SQLException | InventoryException e) {
            e.printStackTrace();
        }
    }

    private void loadDataToOrders(final DBConnector connector, final List<Order> orders) {
        DBCursorHolder cursor;
        try {
            if (user instanceof Administrator) cursor = DBUtils.innerJoinTables(connector.getConnection(), "baskets","orders",
                    "basket_id", new String[]{"products_name", "products_amount", "address"}, new String[]{});
            else cursor = DBUtils.innerJoinTables(connector.getConnection(), "baskets","orders",
                    "basket_id", new String[]{"products_name", "products_amount", "address"},
                    new String[]{String.format("basket_owner='%s'", user.getUserName())});
            while (cursor.getResults().next()) {
                final Basket orderBasket = new Basket();
                constructBasketFromDB(cursor.getResults(), orderBasket);
                orders.add(new Order(orderBasket, cursor.getResults().getString(3)));
            }
            cursor.closeCursor();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSavedBasket(final DBConnector connector, final Basket basket) {
        DBCursorHolder cursor;
        try {
            cursor = DBUtils.filterFromTable(connector.getConnection(), "baskets", new String[]{"products_name", "products_amount"},
                    new String[]{String.format("basket_owner='%s'", user.getUserName()), "AND", "processed='f'"});
            while (cursor.getResults().next()) {
               constructBasketFromDB(cursor.getResults(), basket);
            }
            cursor.closeCursor();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void constructBasketFromDB(final ResultSet products, final Basket basketToConstruct) {
        try {
            final List<String> names = Arrays.asList(products.getString(1).split(","));
            final List<String> amounts = Arrays.asList(products.getString(2).split(","));
            Product restoredProduct;
            int counter = 0;
            for(String productName : names) {
                final DBCursorHolder productDetails = DBUtils.filterFromTable(connector.getConnection(), "products",
                        new String[]{"product_weight", "product_price"}, new String[]{String.format("product_name='%s'", productName)});
                while (productDetails.getResults().next()) {
                    restoredProduct = new Product(productName, productDetails.getResults().getDouble(1),
                            productDetails.getResults().getDouble(2));
                    basketToConstruct.addProducts(restoredProduct, Integer.parseInt(amounts.get(counter)));
                }
                productDetails.closeCursor();
            }
        } catch (SQLException | BasketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
