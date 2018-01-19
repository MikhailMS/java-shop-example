package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Inventory;
import com.molotkov.Order;
import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.molotkov.gui.SceneHolder.createAdminPaneScene;
import static com.molotkov.gui.SceneHolder.createClientPaneScene;

public class LoginButton extends ChangeableScene {

    public static Button createLoginButton(final Stage primaryStage, final Connection connection, final User user, final Inventory shopInventory,
                                           final Basket clientBasket, final List<Order> userOrders, final List<User> userList) {
        final Button btn = new Button();
        btn.setText("Gain access to the Shop");

        btn.setOnAction(mainEvent -> loginAction(primaryStage, connection, user, shopInventory, clientBasket, userOrders, userList));
        return btn;
    }

    private static void loginAction(final Stage primaryStage, final Connection connection, final User user, final Inventory shopInventory,
                                    final Basket clientBasket, final List<Order> userOrders, final List<User> userList) {
        // Create the custom dialog.
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login");
        alert.setHeaderText("Login Dialog");

        final Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login into Super Java Shop");
        dialog.setContentText("Enter your username and password : ");
        dialog.initModality(Modality.NONE);

        // Set login button
        final ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        final TextField userName = new TextField();
        userName.setPromptText("e.g. m03j");
        userName.setId("user-name");

        final PasswordField userPasswd = new PasswordField();
        userPasswd.setPromptText("xxxx");
        userPasswd.setId("user-passwd");

        grid.add(new Label("Username: "), 0, 0);
        grid.add(userName, 1, 0);
        grid.add(new Label("Password: "), 0, 1);
        grid.add(userPasswd, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        final Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
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
                userAuthentication(primaryStage, e, dialog, userName.getText(), userPasswd.getText(), connection, user, shopInventory,
                        clientBasket, userOrders, userList);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static void userAuthentication(final Stage primaryStage, final Event e, final Dialog dialog, final String userName,
                                           final String userPasswd, final Connection connection, User user, final Inventory shopInventory,
                                           final Basket clientBasket, final List<Order> userOrders, final List<User> userList) throws SQLException {
        if (e.getEventType().equals(ActionEvent.ACTION)) {
            e.consume();
            if (isUserAllowed(userName, userPasswd, connection)) {
                if (isUserAdmin(userName, userPasswd, connection)) {
                    user = new Administrator(userName, userPasswd);
                    changeScene(primaryStage, createAdminPaneScene(connection, shopInventory, userOrders, userList, user));
                } else {
                    user = new Client(userName, userPasswd);
                    changeScene(primaryStage, createClientPaneScene(connection, shopInventory, userOrders, clientBasket, user));
                }
                dialog.close();
            } else {
                final ShakeTransition animation = new ShakeTransition(dialog.getDialogPane(), t -> dialog.show());
                animation.playFromStart();
            }
        }
    }

    private static boolean isUserAllowed(final String userName, final String userPasswd, final Connection connection) throws SQLException {
        final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "users", new String[]{"user_name"},
                new String[]{String.format("user_name = '%s'", userName), "AND", String.format("user_password = '%s'", userPasswd)});
        while (cursor.getResults().next()) {
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

    private static boolean isUserAdmin(final String userName, final String userPasswd, final Connection connection) throws SQLException {
        final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "users", new String[]{"privileges"},
                new String[]{String.format("user_name = '%s'", userName), "AND", String.format("user_password = '%s'", userPasswd)});
        cursor.getResults().next();
        if (cursor.getResults().getBoolean(1)) {
            cursor.closeCursor();
            return true;
        } else {
            cursor.closeCursor();
            return false;
        }
    }

}
