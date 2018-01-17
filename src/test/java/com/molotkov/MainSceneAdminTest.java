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
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

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

    private User user;

    private Inventory shopInventory = new Inventory();
    private Basket clientBasket = new Basket();
    private List<Order> userOrders = new ArrayList<>();
    private List<User> userList = new ArrayList<>();

    private static Stage primaryStage;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Override
    public void start(final Stage primaryStage) throws SQLException {
        // TestContainers bit
        final HikariConfig hikariConfig = new HikariConfig();
        //hikariConfig.setMaximumPoolSize(100);
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
                    " order_owner text REFERENCES users(user_name) ON DELETE CASCADE, address text NOT NULL, created_at timestamp DEFAULT CURRENT_TIMESTAMP )");
            statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 1, 'testUser1', 'Manchester' )");
            statement.addBatch("INSERT INTO orders ( basket_id, order_owner, address ) VALUES ( 2, 'testUser2', 'London' )");

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
    public void admin_can_login_n_see_inventory_table() throws SQLException {
        login();

        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Product Name"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Product Weight"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Product Price"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Quantity available in Inventory"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Product Total Price"));
        verifyThat("#inventory-table-view", TableViewMatchersExtension.hasColumnWithID("Details"));
    }

    @Test
    public void admin_can_see_inventory_entries() throws SQLException {
        login();

        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 3, "2.40", false));
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("chicken", 1.0, 2.3, 4, "9.20", false));
    }

    @Test
    public void admin_can_create_new_product() throws SQLException {
        login();

        ((TextField) GuiTest.find("#name")).setText("milk");
        ((TextField) GuiTest.find("#weight")).setText("1.0");
        ((TextField) GuiTest.find("#price")).setText("1.0");
        ((TextField) GuiTest.find("#amount")).setText("5");
        clickOn("Add new product");
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("milk", 1.0, 1.0, 5, "5.00", false));
        sleep(2000);

        DBUtils.deleteFromTable(dataSource.getConnection(), "inventory", new String[]{String.format("product_id=%d", 3)});
        DBUtils.deleteFromTable(dataSource.getConnection(), "products", new String[]{String.format("product_name='%s'", "milk")});
    }

    @Test
    public void admin_can_increase_n_decrease_new_product_amount() throws SQLException {
        login();

        ((TextField) GuiTest.find("#name")).setText("milk");
        ((TextField) GuiTest.find("#weight")).setText("1.0");
        ((TextField) GuiTest.find("#price")).setText("1.0");
        ((TextField) GuiTest.find("#amount")).setText("5");
        clickOn("Add new product");
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("milk", 1.0, 1.0, 5, "5.00", false));

        clickOn("Product Name")
                .clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(2).query())
                .clickOn("Add to inventory");
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("milk", 1.0, 1.0, 6, "6.00", false));
        sleep(2000);

        clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(2).query())
                .clickOn("Remove from inventory");
        sleep(2000);

        clickOn("Remove from inventory");
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("milk", 1.0, 1.0, 4, "4.00", false));

        DBUtils.deleteFromTable(dataSource.getConnection(), "inventory", new String[]{String.format("product_id=%d", 3)});
        DBUtils.deleteFromTable(dataSource.getConnection(), "products", new String[]{String.format("product_name='%s'", "milk")});
        sleep(2000);
    }

    @Test
    public void admin_can_increase_n_decrease_product_amount() throws SQLException {
        login();

        clickOn("Product Name")
                .clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(0).query())
                .clickOn("Add to inventory");
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 4, "3.20", false));
        sleep(500);
        clickOn((Node)from(lookup("#inventory-table-view .expander-button")).nth(0).query())
                .clickOn("Remove from inventory");
        verifyThat("#inventory-table-view", TableViewMatchers.containsRow("apple", 0.151, 0.8, 3, "2.40"));
    }

    @Test
    public void admin_can_see_order_history_table() throws SQLException {
        login();

        clickOn("Order History");
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Delivery address"));
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Total order price"));
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Order Details"));
    }

    @Test
    public void admin_can_see_full_order_history_n_total() throws SQLException {
        login();

        clickOn("Order History");
        verifyThat("#order-table", TableViewMatchers.containsRow("Manchester", 5.40));
        verifyThat("#order-table", TableViewMatchers.containsRow("London", 1.60));

        verifyThat("#total-table", TableViewMatchers.containsRow(4.95));
    }

    @Test
    public void admin_can_see_order_details() throws SQLException {
        login();
        clickOn("Order History")
                .clickOn("Delivery address")
                .clickOn((Node)from(lookup("#order-table .expander-button")).nth(1).query());
        verifyThat(lookup("Basket has 2 products."), Node::isVisible);
    }

    @Test
    public void admin_can_see_system_users_table() throws SQLException {
        login();

        clickOn("System users");
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("User name"));
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("User privilege"));
        verifyThat("#order-table", TableViewMatchersExtension.hasColumnWithID("Details"));
    }

    @Test
    public void admin_can_see_all_system_users() throws SQLException {
        login();

        clickOn("System users");
        verifyThat("#users-table", TableViewMatchers.containsRow("testUser1", "False"));
        verifyThat("#users-table", TableViewMatchers.containsRow("testUser2", "False"));
        verifyThat("#users-table", TableViewMatchers.containsRow("admin", "True"));
    }

    @Test
    public void admin_can_create_new_admin() throws SQLException {
        login();

        clickOn("System users");

        ((TextField)from(lookup("#user-name")).query()).setText("admin2");
        ((TextField)from(lookup("#user-password")).query()).setText("admin2");
        clickOn("Administrator?").clickOn("Add new user");

        verifyThat("#users-table", TableViewMatchers.containsRow("admin2", "True"));

        DBUtils.deleteFromTable(dataSource.getConnection(), "users", new String[]{String.format("user_name='%s'", "admin2")});
    }

    @Test
    public void admin_can_create_new_client() throws SQLException {
        login();

        clickOn("System users");

        ((TextField)from(lookup("#user-name")).query()).setText("testUser3");
        ((TextField)from(lookup("#user-password")).query()).setText("testUser3");
        clickOn("Add new user");

        verifyThat("#users-table", TableViewMatchers.containsRow("testUser3", "False"));

        DBUtils.deleteFromTable(dataSource.getConnection(), "users", new String[]{String.format("user_name='%s'", "testUser3")});
    }

    @Test
    public void admin_can_delete_new_admin() throws SQLException {
        login();

        clickOn("System users");
        ((TextField)from(lookup("#user-name")).query()).setText("admin2");
        ((TextField)from(lookup("#user-password")).query()).setText("admin2");

        clickOn("Administrator?").clickOn("Add new user");
        verifyThat("#users-table", TableViewMatchers.containsRow("admin2", "True"));

        clickOn("User name")
                .clickOn((Node)from(lookup("#users-table .expander-button")).nth(1).query())
                .clickOn("Remove user");
        verifyThat(lookup("User has been removed successfully"), Node::isVisible);
        sleep(2000);
    }

    @Test
    public void admin_can_delete_existing_user() throws SQLException {
        login();

        clickOn("System users")
                .clickOn("User name")
                .clickOn((Node)from(lookup("#users-table .expander-button")).nth(1).query())
                .clickOn("Remove user");
        verifyThat(lookup("User has been removed successfully"), Node::isVisible);

        DBUtils.insertSpecificIntoTable(dataSource.getConnection(), "users", new String[]{"user_name", "user_password"},
                new String[]{"testUser1", "testUser1"});
        sleep(2000);
    }

    private void login() {
        clickOn("Gain access to the Shop");
        ((TextField)from(lookup("#user-name")).query()).setText("admin");
        ((PasswordField)from(lookup("#user-passwd")).query()).setText("admin");
        clickOn("Login");
        sleep(2500);
    }
}
