package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Inventory;
import com.molotkov.Order;
import com.molotkov.db.DBConnector;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.molotkov.gui.GuiWindowConsts.*;

public class MainScreen extends Application {
    private static final String PRIMARY_STAGE_TITLE = "Java Super Shop";
    private static final Color PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private static User user;

    private static Inventory shopInventory = new Inventory();
    private static Basket clientBasket = new Basket();
    private static List<Order> userOrders = new ArrayList<>();
    private static List<User> userList = new ArrayList<>();

    private static DBConnector connector;

    private static Stage primaryStage;

    @Override
    public void start(final Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(PRIMARY_STAGE_TITLE);

        connector = new DBConnector("jdbc:postgresql:test_db");

        final Group root = new Group();
        final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR);
        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab loginTab = new Tab();
        loginTab.setText("Authorisation");
        final HBox loginBox = new HBox(HBOX_SPACING);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.getChildren().add(LoginButton.createLoginButton(primaryStage, connector.getConnection(), user, shopInventory,
                clientBasket, userOrders, userList));
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

    @Override
    public void stop() {
        // Handle basket save here only if it's not empty
        System.out.println("Caught application closure");
        if (clientBasket.getProducts().size() > 0) {
            if (((Client) user).retrievedBasketId() < 0) {
                ((Client) user).saveBasket(connector.getConnection(), clientBasket);
            }
        }
        try {
            connector.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
