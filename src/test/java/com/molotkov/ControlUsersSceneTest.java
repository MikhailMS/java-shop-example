package com.molotkov;

import com.molotkov.extras.TableViewMatchersExtension;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.molotkov.gui.ControlUsersScene.createControlTable;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;
import static org.testfx.api.FxAssert.verifyThat;

public class ControlUsersSceneTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        Client testClient1 = new Client("testClient1", "testClient1");
        Client testClient2 = new Client("testClient2", "testClient2");
        Administrator admin = new Administrator("admin", "admin");

        List<User> userList = new ArrayList<>();
        userList.add(testClient1);
        userList.add(testClient2);
        userList.add(admin);

        stage.setScene(new Scene(createControlTable(userList), WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    @Test
    public void should_contain_columns() {
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("User name"));
        verifyThat(".table-view", TableViewMatchersExtension.hasColumnWithID("User privilege"));
    }

    @Test
    public void should_contain_rows() {
        verifyThat(".table-view", TableViewMatchers.containsRow("testClient1", "False"));
        verifyThat(".table-view", TableViewMatchers.containsRow("testClient2", "False"));
        verifyThat(".table-view", TableViewMatchers.containsRow("admin", "True"));
    }

    @Test
    public void can_add_new_admin_user() {
        ((TextField) GuiTest.find("#user-name")).setText("admin2");
        ((TextField) GuiTest.find("#user-password")).setText("admin2");
        clickOn("Administrator?").clickOn("Add new user");
        verifyThat(".table-view", TableViewMatchers.containsRow("admin2", "True"));
    }

    @Test
    public void can_add_new_client_user() {
        ((TextField) GuiTest.find("#user-name")).setText("client");
        ((TextField) GuiTest.find("#user-password")).setText("client");
        clickOn("Add new user");
        verifyThat(".table-view", TableViewMatchers.containsRow("client", "False"));
    }

    @Test
    public void can_remove_user() {
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
