package com.molotkov;

import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.molotkov.gui.ControlUsersScene.createControlTable;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;
import static org.testfx.api.FxAssert.verifyThat;

public class ControlUsersSceneTest extends ApplicationTest {
    private HikariDataSource dataSource;
    private static boolean setupIsDone = false;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Override
    public void start(final Stage stage) throws SQLException {
        final Client testClient1 = new Client("testClient1", "testClient1");
        final Client testClient2 = new Client("testClient2", "testClient2");
        final Administrator admin = new Administrator("admin", "admin");

        final List<User> userList = new ArrayList<>();
        userList.add(testClient1);
        userList.add(testClient2);
        userList.add(admin);

        // TestContainers bit
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        dataSource = new HikariDataSource(hikariConfig);

        if(!setupIsDone) {
            System.out.println("Hallo ee");
            final Statement statement = dataSource.getConnection().createStatement();

            statement.addBatch("CREATE TABLE IF NOT EXISTS users ( user_name text PRIMARY KEY, user_password text NOT NULL," +
                    " privileges boolean DEFAULT FALSE )");

            statement.addBatch("INSERT INTO users VALUES ( 'testClient1', 'testClient1', FALSE )");
            statement.addBatch("INSERT INTO users VALUES ( 'testClient2', 'testClient2', FALSE )");
            statement.addBatch("INSERT INTO users VALUES ( 'admin', 'admin', TRUE )");

            statement.executeBatch();
            statement.close();
            setupIsDone = true;
        }
        // TestContainers ends

        stage.setScene(new Scene(createControlTable(userList, admin, dataSource.getConnection()), WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    @Test
    public void should_contain_columns() throws SQLException {
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("User name"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("User privilege"));
    }

    @Test
    public void should_contain_rows() throws SQLException {
        verifyThat(".table-view", TableViewMatchers.containsRow("testClient1", "False"));
        verifyThat(".table-view", TableViewMatchers.containsRow("testClient2", "False"));
        verifyThat(".table-view", TableViewMatchers.containsRow("admin", "True"));
    }

    @Test
    public void can_add_new_admin_user() throws SQLException {
        ((TextField) GuiTest.find("#user-name")).setText("admin2");
        ((TextField) GuiTest.find("#user-password")).setText("admin2");
        clickOn("Administrator?").clickOn("Add new user");
        verifyThat(".table-view", TableViewMatchers.containsRow("admin2", "True"));
    }

    @Test
    public void can_add_new_client_user() throws SQLException {
        ((TextField) GuiTest.find("#user-name")).setText("client");
        ((TextField) GuiTest.find("#user-password")).setText("client");
        clickOn("Add new user");
        verifyThat(".table-view", TableViewMatchers.containsRow("client", "False"));
    }

    @Test
    public void can_remove_user() throws SQLException {
        verifyThat(".table-view", TableViewMatchers.containsRow("testClient1", "False"));
        verifyThat(".table-view", TableViewMatchers.containsRow("testClient2", "False"));
        verifyThat(".table-view", TableViewMatchers.containsRow("admin", "True"));
        clickOn("User name")
                .clickOn((Node)from(lookup(".expander-button")).nth(0).query())
                .clickOn("Remove user");
        verifyThat(".table-view", TableViewMatchersExtension.hasNoTableCell("admin"));
        verifyThat(".table-view", TableViewMatchersExtension.hasNoTableCell("True"));
        verifyThat(lookup("User has been removed successfully"), Node::isVisible);
    }
}
