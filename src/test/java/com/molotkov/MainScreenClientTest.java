package com.molotkov;

import com.molotkov.db.DBUtils;
import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.gui.LoginButton;
import com.molotkov.users.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import javax.xml.soap.Text;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.molotkov.gui.GuiWindowConsts.HBOX_SPACING;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;
import static org.testfx.api.FxAssert.verifyThat;

public class MainScreenClientTest extends ApplicationTest {
    private HikariDataSource dataSource;
    private static boolean setupIsDone = false;

    private static final String PRIMARY_STAGE_TITLE = "Java Super Shop";
    private static final Color PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private User user;

    private Inventory shopInventory = new Inventory();
    private Basket clientBasket = new Basket();
    private List<Order> userOrders = new ArrayList<>();
    private List<User> userList = new ArrayList<>();

    private static Stage primaryStage;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @After
    public void closeDataSource() {
        dataSource.close();
    }

    @Override
    public void start(final Stage primaryStage) throws SQLException {
        // TestContainers bit
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);

        if(!setupIsDone) {
            final Statement statement = dataSource.getConnection().createStatement();

            statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_password text NOT NULL," +
                    " privileges boolean DEFAULT FALSE )");
            statement.addBatch("INSERT INTO users VALUES ( 'testUser', 'testUser', FALSE )");
            statement.addBatch("INSERT INTO users VALUES ( 'testUser1', 'testUser1', FALSE )");

            statement.addBatch(" CREATE TABLE IF NOT EXISTS baskets ( basket_id serial PRIMARY KEY," +
                    " basket_owner text REFERENCES users(user_name) ON DELETE CASCADE, products_name text NOT NULL," +
                    " products_amount text NOT NULL, processed boolean DEFAULT FALSE, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
            statement.addBatch("INSERT INTO baskets ( basket_owner, products_name, products_amount ) VALUES ( 'testUser', 'apple,chicken', '1,2' )");
            statement.addBatch("INSERT INTO baskets ( basket_owner, products_name, products_amount ) VALUES ( 'testUser1', 'apple', '2' )");


            statement.addBatch("CREATE TABLE IF NOT EXISTS orders ( order_id serial, basket_id int4 REFERENCES baskets(basket_id) ON DELETE CASCADE," +
                    " order_owner text REFERENCES users(user_name) ON DELETE CASCADE, address text NOT NULL, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
            statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 1, 'testUser', 'Manchester' )");
            statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 2, 'testUser1', 'London' )");

            statement.addBatch("CREATE TABLE IF NOT EXISTS products ( product_id serial PRIMARY KEY, product_name text NOT NULL UNIQUE," +
                    " product_weight numeric (6,3) NOT NULL, product_price numeric (8,2) NOT NULL )");
            statement.addBatch("INSERT INTO products ( product_name, product_weight, product_price ) VALUES ( 'apple', 0.151, 0.8 )");
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

        final Group root = new Group();
        final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, PRIMARY_STAGE_DEFAULT_BACKGROUND_COLOR);
        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab loginTab = new Tab();
        loginTab.setText("Authorisation");
        final HBox loginBox = new HBox(HBOX_SPACING);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.getChildren().add(LoginButton.createLoginButton(primaryStage, dataSource.getConnection(), user, shopInventory,
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

    @Test
    public void client_can_see_inventory_table() {
        login();

        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Product Name"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Product Weight"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Product Price"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Quantity available in Inventory"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Details"));
    }

    @Test
    public void client_can_see_inventory_products() {
        login();

        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 3, "2.40", false));
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("chicken", 1.0, 2.3, 4, "9.20", false));
    }

    @Test
    public void client_can_see_basket_table() {
        login();

        verifyThat("#basket-table-view", TableViewMatchersExtension.hasColumnWithID("Basket Total"));
        verifyThat("#basket-table-view", TableViewMatchersExtension.hasColumnWithID("Order Details"));
    }

    @Test
    public void client_can_see_empty_basket() {
        login();

        verifyThat("#basket-table-view", TableViewMatchers.containsRow(0.00, false));
    }

    @Test
    public void client_can_add_products_to_basket() {
        login();

        clickOn("Product Name")
                .clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(0).query())
                .clickOn("Add to basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow(0.80, false));
        verifyThat(lookup("Product has been added to basket"), Node::isVisible);
        sleep(2000);
    }

    @Test
    public void client_can_remove_product_from_basket() {
        login();

        clickOn("Product Name")
                .clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(0).query())
                .clickOn("Add to basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow(0.80, false));
        clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(0 ).query())
                .clickOn("Remove from basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow(0.00, false));
        verifyThat(lookup("Product has been removed from basket"), Node::isVisible);
        sleep(2000);
    }

    @Test
    public void client_can_complete_order() throws SQLException {
        login();

        clickOn("Product Name")
                .clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(0).query())
                .clickOn("Add to basket");
        verifyThat("#basket-table-view", TableViewMatchers.containsRow(0.80, false));
        clickOn((Node)from(lookup("#basket-table-view .expander-button")).nth(0).query());
        ((TextField)from(lookup("#delivery-address")).query()).setText("Ipswich");
        clickOn("Complete order");
        verifyThat(lookup("Order has been made"), Node::isVisible);

        DBUtils.deleteFromTable(dataSource.getConnection(), "orders", new String[]{"address='Ipswich'"});
        sleep(2000);
    }

    @Test
    public void client_cannot_complete_order_wo_products() {
        login();

        clickOn((Node)from(lookup("#basket-table-view .expander-button")).nth(0 ).query())
                .clickOn("Complete order");
        verifyThat(lookup("Cannot process the order: Add products to basket to complete order"), Node::isVisible);
        sleep(2000);
    }

    @Test
    public void client_cannot_complete_order_wo_address() {
        login();

        clickOn((Node)from(lookup("#basket-table-view .expander-button")).nth(0 ).query())
                .clickOn("Complete order");
        verifyThat(lookup("Cannot process the order: Enter the delivery address"), Node::isVisible);
        sleep(2000);
    }

    @Test
    public void client_can_see_order_history_table() {
        login();

        clickOn("Order History");
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Delivery address"));
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Total order price"));
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Order Details"));
    }

    @Test
    public void client_can_see_order_details() {
        login();

        clickOn("Order History")
                .clickOn("Delivery address")
                .clickOn((Node)from(lookup("#order-table .expander-button")).nth(0).query());
        verifyThat(lookup("Basket has 2 products."), Node::isVisible);
    }

    @Test
    public void client_can_see_full_order_history_n_total() {
        login();

        clickOn("Order History");
        verifyThat("#order-table", TableViewMatchers.containsRow("Manchester", 5.40));

        verifyThat("#total-table", TableViewMatchersExtension.hasTableCell(5.40));
    }

    private void login() {
        clickOn("Gain access to the Shop");
        ((TextField)from(lookup("#user-name")).query()).setText("testUser");
        ((PasswordField)from(lookup("#user-passwd")).query()).setText("testUser");
        clickOn("Login");
        sleep(2500);
    }
}
