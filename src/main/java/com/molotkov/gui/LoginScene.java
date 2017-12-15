package com.molotkov.gui;

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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.SQLException;

public class LoginScene {

    public Button loginButton(User user, Connection connection) {
        Button btn = new Button();
        btn.setText("Gain access to the Shop");

        btn.setOnAction(mainEvent -> loginAction(user, connection));
        return btn;
    }

    private void loginAction(User user, Connection connection) {
        // Create the custom dialog.
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Login");
        alert.setHeaderText("Login Dialog");
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login into Super Java Shop");
        dialog.setContentText("Enter your username and password : ");
        dialog.initModality(Modality.NONE);

        // Set login button
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
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
                userAuthentication(e, dialog, userName.getText(), userPasswd.getText(), user, connection);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void userAuthentication(Event e, Dialog dialog, String userName, String userPasswd, User user, Connection connection) throws SQLException {
        if(e.getEventType().equals(ActionEvent.ACTION)){
            e.consume();
            if (isUserAllowed(userName, userPasswd, connection)) {
                if (isUserAdmin(userName, userPasswd, connection)) {
                    user = new Administrator(userName,userPasswd);
                } else {
                    user = new Client(userName, userPasswd);
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
        cursor.getResults().next();
        if (cursor.getResults().getString(1).equals(userName)) {
            cursor.closeCursor();
            return true;
        } else {
            cursor.closeCursor();
            return false;
        }
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
}
