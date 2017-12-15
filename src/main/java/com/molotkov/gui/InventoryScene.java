package com.molotkov.gui;

import com.molotkov.Inventory;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.products.Product;

import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

public class InventoryScene  extends Application {

    @Override
    public void start(Stage stage) {

        User admin = new Administrator("t", "t");
        User client = new Client("t", "t");

        Inventory inventory = new Inventory();
        try {
            inventory.addProducts(new Product("chicken", 1, 2.3),3);
            inventory.addProducts(new Product("apple", 0.150, 0.8), 2);
        } catch (InventoryException e) {
            e.printStackTrace();
        }

        stage.setScene(new Scene(createInventoryTableView(inventory, client), 300, 400));
        stage.show();
    }

    public TableView createInventoryTableView(final Inventory inventory, final User user) {
        TableColumn<Map.Entry<Product, Integer>, String> column1 = new TableColumn<>("Product name");
        column1.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getKey().getName()));
        TableColumn<Map.Entry<Product, Integer>, Integer> column2 = new TableColumn<>("Amount");
        column2.setCellValueFactory(item -> new SimpleObjectProperty<>(item.getValue().getValue()));
        TableColumn<Map.Entry<Product, Integer>, String> column3 = new TableColumn<>("Total");
        column3.setCellValueFactory(item -> new SimpleStringProperty(String.format("%.2f", item.getValue().getKey().getPrice() * item.getValue().getValue())));

        ObservableList<Map.Entry<Product, Integer>> items = FXCollections.observableArrayList(inventory.getProducts().entrySet());
        final TableView<Map.Entry<Product, Integer>> table = new TableView<>(items);

        if (user instanceof Administrator) {
            table.getColumns().setAll(column1, column2, column3);
        } else {
            table.getColumns().setAll(column1, column2);
        }

        table.setRowFactory(row -> new TableRow<Map.Entry<Product, Integer>>() {
            Node detailsPane ;
            {
                selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        getChildren().add(detailsPane);
                    } else {
                        getChildren().remove(detailsPane);
                    }
                    this.requestLayout();
                });
                detailsPane = createDetailsPane(itemProperty());
            }

            @Override
            protected double computePrefHeight(double width) {
                if (isSelected()) {
                    return super.computePrefHeight(width)+detailsPane.prefHeight(getWidth());
                } else {
                    return super.computePrefHeight(width);
                }
            }

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                if (isSelected()) {
                    double width = getWidth();
                    double paneHeight = detailsPane.prefHeight(width);
                    detailsPane.resizeRelocate(0, getHeight()-paneHeight, width, paneHeight);
                }
            }
        });
        return table;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Node createDetailsPane(ObjectProperty<Map.Entry<Product, Integer>> item) {
        BorderPane detailsPane = new BorderPane();
        Label detailsLabel = new Label();
        VBox labels = new VBox(5, detailsLabel);
        labels.setAlignment(Pos.CENTER_LEFT);
        labels.setPadding(new Insets(2, 2, 2, 16));
        detailsPane.setCenter(labels);

        detailsPane.setStyle("-fx-background-color: -fx-background; -fx-background: skyblue;");

        item.addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
                detailsLabel.setText("");
            } else {
                detailsLabel.setText(String.format("Weight: %.3f | Price: %.2f", newItem.getKey().getWeight(), newItem.getKey().getPrice()));
            }
        });


        return detailsPane ;
    }

}
