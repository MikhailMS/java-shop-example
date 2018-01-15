package com.molotkov;

import com.molotkov.db.DBConnector;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.MainScreen;
import com.molotkov.users.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.ClassRule;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testfx.framework.junit.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.molotkov.gui.GuiWindowConsts.HBOX_SPACING;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

public class MainSceneAdminTest extends ApplicationTest {
    private HikariDataSource dataSource;
    private static boolean setupIsDone = false;

    private static final String PRIMARY_STAGE_TITLE = "Java Super Shop";
    private static final Color PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private static User user;

    private static Inventory shopInventory = new Inventory();
    private static Basket clientBasket = new Basket();
    private static List<Order> userOrders = new ArrayList<>();
    private static List<User> userList = new ArrayList<>();

    private static DBConnector connector;

    private static Stage primaryStage;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Override
    public void start(final Stage primaryStage) throws SQLException {
        // TestContainers bit
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(35);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);

        if(!setupIsDone) {
            final Statement statement = dataSource.getConnection().createStatement();

            statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_password text NOT NULL," +
                    " privileges boolean DEFAULT FALSE )");
            statement.addBatch("INSERT INTO users VALUES ( 'admin', 'admin', TRUE )");
            statement.addBatch("INSERT INTO users VALUES ( 'testUser1', 'testUser1', FALSE )");
            statement.addBatch("INSERT INTO users VALUES ( 'testUser2', 'testUser2', FALSE )");

            statement.addBatch(" CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY," +
                    " basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL," +
                    " products_amount text NOT NULL, processed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
            statement.addBatch("INSERT INTO baskets ( basket_owner, products_name, products_amount ) VALUES ( 'testUser1', 'apple,chicken', '1,2' )");
            statement.addBatch("INSERT INTO baskets ( basket_owner, products_name, products_amount ) VALUES ( 'testUser2', 'apple', '2' )");

            statement.addBatch("CREATE TABLE IF NOT EXISTS orders ( order_id serial, basket_id int4 REFERENCES baskets(basket_id) ON DELETE CASCADE," +
                    " order_owner text REFERENCES users(user_name) ON DELETE CASCADE, address text NOT NULL, total_price numeric (8,2) NOT NULL," +
                    " completed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
            statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address, total_price ) VALUES ( 1, 'testUser1', 'Manchester', 2.45 )");
            statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address, total_price ) VALUES ( 2, 'testUser2', 'London', 2.50 )");

            statement.addBatch("CREATE TABLE IF NOT EXISTS products ( product_id serial PRIMARY KEY, product_name text NOT NULL UNIQUE," +
                    " product_weight numeric (6,3) NOT NULL, product_price numeric (8,2) NOT NULL )");
            statement.addBatch("INSERT INTO products ( product_name, product_weight, product_price ) VALUES ( 'apple', 0.150, 0.8 )");
            statement.addBatch("INSERT INTO products ( product_name, product_weight, product_price ) VALUES ( 'chicken', 1, 2.3 )");

            statement.addBatch("CREATE TABLE IF NOT EXISTS inventory ( entry_id serial, " +
                    "product_id int4 REFERENCES products(product_id) ON DELETE RESTRICT, product_amount int4 NOT NULL )");
            statement.addBatch("INSERT INTO inventory ( product_id, product_amount ) VALUES ( 1, 3 )");
            statement.addBatch("INSERT INTO inventory ( product_id, product_amount ) VALUES ( 2, 4 )");

            statement.executeBatch();
            statement.close();
            setupIsDone = true;
        }
        // MainScreen setup
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(PRIMARY_STAGE_TITLE);

        connector = new DBConnector(postgres.getJdbcUrl(), new User(postgres.getUsername(), postgres.getPassword()));

        final Group root = new Group();
        final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR);
        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab loginTab = new Tab();
        loginTab.setText("Authorisation");
        final HBox loginBox = new HBox(HBOX_SPACING);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.getChildren().add(MainScreen.loginButton(connector.getConnection()));
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

    @Test
    public void admin_can_login_n_see_inventory() {
        clickOn("Gain access to the Shop");
        ((TextField) GuiTest.find("#user-name")).setText("admin");
        ((PasswordField) GuiTest.find("#user-passwd")).setText("admin");
        clickOn("Login");
        sleep(3000);
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Name"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Weight"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Price"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Quantity available in Inventory"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Product Total Price"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("Details"));
    }
}
