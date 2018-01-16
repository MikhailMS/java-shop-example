package com.molotkov.gui;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.controlsfx.tools.Utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

public class ControlUsersScene {
    private static final String USER_NAME_COLUMN = "User name";
    private static final String USER_PRIVILEGE_COLUMN = "User privilege";

    public static VBox createControlTable(final List<User> users, final Administrator admin, final Connection connection) {
        final ObservableList<User> observableUserList = FXCollections.observableArrayList(users);
        final TableView<User> userTableView = new TableView<>(observableUserList);
        userTableView.setId("users-table");
        userTableView.setPrefWidth(WINDOW_WIDTH);
        userTableView.setPrefHeight(WINDOW_HEIGHT);

        final VBox controlTableBox = new VBox();
        controlTableBox.setSpacing(5);
        controlTableBox.setPadding(new Insets(5, 5, 5, 5));

        final TableColumn<User, String> userNameColumn = new TableColumn<>(USER_NAME_COLUMN);
        userNameColumn.setId(USER_NAME_COLUMN);
        userNameColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getUserName()));

        final TableColumn<User, String> userPrivilegeColumn = new TableColumn<>(USER_PRIVILEGE_COLUMN);
        userPrivilegeColumn.setId(USER_PRIVILEGE_COLUMN);
        userPrivilegeColumn.setCellValueFactory(item -> {
            if (item.getValue() instanceof Administrator) return new SimpleStringProperty("True");
            else return new SimpleStringProperty("False");
        });

        userTableView.getColumns().setAll(userNameColumn, userPrivilegeColumn);
        addAdminRowExpander(userTableView, users, observableUserList, admin, connection);
        userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        controlTableBox.getChildren().setAll(userTableView, createAddUserBox(userNameColumn, users, observableUserList, admin, connection));

        final TableFilter<User> filter = TableFilter.forTableView(userTableView).lazy(false).apply();

        return controlTableBox;
    }

    private static Button createDeleteButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError,
                                             final List<User> users, final ObservableList<User> observableUsers, final HBox editor ,
                                             final TableRowExpanderColumn.TableRowDataFeatures<User> param, final Administrator admin,
                                             final Connection connection) {
        final Button deleteFromBasket = new Button();
        deleteFromBasket.setText(buttonText);
        deleteFromBasket.setOnMouseClicked(mouseEvent -> {
            try {
                users.remove(param.getValue());
                observableUsers.remove(param.getValue());

                // Here should also be call to DB to remove user
                admin.deleteUser(connection, param.getValue().getUserName());

                Notifications.create()
                        .darkStyle()
                        .title("Info")
                        .text(notificationTextSuccess)
                        .position(Pos.CENTER)
                        .owner(Utils.getWindow(editor))
                        .hideAfter(Duration.seconds(2))
                        .showConfirm();

            } catch (Exception e) {
                Notifications.create()
                        .darkStyle()
                        .title("Error")
                        .text(notificationTextError)
                        .position(Pos.CENTER)
                        .owner(Utils.getWindow(editor))
                        .hideAfter(Duration.seconds(4))
                        .showError();
                e.printStackTrace();
            }
        });
        return deleteFromBasket;
    }

    private static HBox createAddUserBox(final TableColumn userNameColumn, final List<User> users, final ObservableList items,
                                         final Administrator admin, final Connection connection) {
        final HBox addProductBox = new HBox();
        final TextField addUserName = new TextField();
        addUserName.setPromptText("Enter name");
        addUserName.setId("user-name");
        addUserName.setMaxWidth(userNameColumn.getPrefWidth());

        final TextField addUserPassword = new TextField();
        addUserPassword.setPromptText("Enter password");
        addUserPassword.setId("user-password");
        addUserPassword.setMaxWidth(userNameColumn.getPrefWidth());

        final TextField addUserPrivilege = new TextField();
        addUserPrivilege.setPromptText("Enter privilege");
        addUserPrivilege.setId("user-privilege");
        addUserPrivilege.setMaxWidth(userNameColumn.getPrefWidth());

        final CheckBox enablePrivilege = new CheckBox("Administrator?");

        final Button addNewUserButton = new Button("Add new user");
        addNewUserButton.setOnMouseClicked(mouseEvent -> {
            final String newUserName = addUserName.getText();
            final String newUserPassword = addUserPassword.getText();
            if (!newUserName.isEmpty() && !newUserPassword.isEmpty()) {
                // Ensure that user doesn't exist in DB
                try {
                    final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "users", new String[]{"user_name"},
                            new String[]{String.format("user_name='%s'", newUserName)});
                    if (!cursor.getResults().next()) {
                        // Next 4 lines of code is huuuge hack - but can't think of another solution.
                        // It works, but may give poor performance on big ObservableList
                        items.removeAll(users);
                        if(enablePrivilege.isSelected()) users.add(new Administrator(newUserName, newUserPassword));
                        else users.add(new Client(newUserName, newUserPassword));
                        items.addAll(users);

                        // Here should also be a call to DB to save new user
                        admin.createUser(connection, newUserName, newUserPassword, enablePrivilege.isSelected());

                        addUserName.clear();
                        addUserPassword.clear();
                        addUserPrivilege.clear();
                        enablePrivilege.setSelected(false);
                    } else {
                        Notifications.create()
                                .darkStyle()
                                .title("Error")
                                .text("Username is taken. Try different username")
                                .position(Pos.CENTER)
                                .owner(Utils.getWindow(addProductBox))
                                .hideAfter(Duration.seconds(2))
                                .showError();
                    }
                    cursor.closeCursor();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Notifications.create()
                        .darkStyle()
                        .title("Error")
                        .text("One of the fields is empty. Make sure user details are completed.")
                        .position(Pos.CENTER)
                        .owner(Utils.getWindow(addProductBox))
                        .hideAfter(Duration.seconds(2))
                        .showError();
            }
        });
        addProductBox.getChildren().addAll(addUserName, addUserPassword, enablePrivilege, addNewUserButton);
        addProductBox.setSpacing(3);

        return addProductBox;
    }

    private static void addAdminRowExpander(final TableView table, final List<User> users, final ObservableList<User> observableUsers,
                                            final Administrator admin, final Connection connection) {
        final TableRowExpanderColumn<User> expander =  new TableRowExpanderColumn<>(param -> {
            final HBox editor = new HBox(10);
            editor.getChildren().addAll(createDeleteButton("Remove user", "User has been removed successfully",
                    "Something went wrong while removing user", users, observableUsers, editor, param, admin, connection));
            return editor;
        });
        expander.setText("Details");
        expander.setId("Details");

        table.getColumns().add(expander);
    }
}
