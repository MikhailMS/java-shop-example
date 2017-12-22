package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Inventory;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.interfaces.ProductStorage;
import com.molotkov.products.Product;

import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.controlsfx.tools.Utils;

import java.text.DecimalFormat;
import java.util.Map;

public class InventoryScene extends Application {
    @Override
    public void start(Stage stage) {

        User admin = new Administrator("t", "t");
        User client = new Client("t", "t");
        Basket userBasket = new Basket();

        Inventory inventory = new Inventory();
        try {
            inventory.addProducts(new Product("chicken", 1, 2.3),3);
            inventory.addProducts(new Product("apple", 0.151, 0.8), 2);
        } catch (InventoryException e) {
            e.printStackTrace();
        }

        client.setBasket(userBasket);

        stage.setScene(new Scene(createInventoryTableView(inventory, admin), 600, 400));
        stage.show();
    }

    public TableView createInventoryTableView(final Inventory inventory, final User user) {
        ObservableList<Map.Entry<Product, Integer>> items = FXCollections.observableArrayList(inventory.getProducts().entrySet());
        final TableView<Map.Entry<Product, Integer>> table = new TableView<>(items);

        final TableColumn<Map.Entry<Product, Integer>, String> productNameColumn = new TableColumn<>("Product name");
        productNameColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getKey().getName()));

        final TableColumn<Map.Entry<Product, Integer>, Double> productWeightColumn = new TableColumn<>("Product Weight");
        productWeightColumn.setCellValueFactory(item -> {
            double weight = item.getValue().getKey().getWeight();
            DecimalFormat df = new DecimalFormat("#.###");
            weight = Double.valueOf(df.format(weight));
            return new SimpleObjectProperty<>(weight);
        });

        final TableColumn<Map.Entry<Product, Integer>, Double> productPriceColumn = new TableColumn<>("Product Price");
        productPriceColumn.setCellValueFactory(item -> {
            double price = item.getValue().getKey().getPrice();
            DecimalFormat df = new DecimalFormat("#.##");
            price = Double.valueOf(df.format(price));
            return new SimpleObjectProperty<>(price);
        });

        final TableColumn<Map.Entry<Product, Integer>, Integer> productAmountColumn = new TableColumn<>("Amount in Inventory");
        productAmountColumn.setCellValueFactory(item -> new SimpleObjectProperty<>(item.getValue().getValue()));

        final TableColumn<Map.Entry<Product, Integer>, String> productTotalColumn = new TableColumn<>("Product Total");
        productTotalColumn.setCellValueFactory(item -> new SimpleStringProperty(String.format("%.2f", item.getValue().getKey().getPrice() * item.getValue().getValue())));


        if (user instanceof Administrator) {
            table.getColumns().setAll(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn, productTotalColumn);
            addAdminRowExpander(table, inventory);
        }
        else {
            table.getColumns().setAll(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn);
            addClientRowExpander(table, user);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final TableFilter<Map.Entry<Product,Integer>> filter = TableFilter.forTableView(table).lazy(false).apply();

        return table;
    }

    private void addClientRowExpander(final TableView table, final User user) {
        TableRowExpanderColumn<Map.Entry<Product, Integer>> expander = new TableRowExpanderColumn<>(param -> {
            HBox editor = new HBox(10);
            Label detailsLabel = new Label();
            detailsLabel.setText(String.format("Weight: %.3f | Price: %.2f", param.getValue().getKey().getWeight(), param.getValue().getKey().getPrice()));


            editor.getChildren().addAll(detailsLabel, createAddButton("Add to basket","Product has been added to basket",
                    "Something went wrong while adding product to basket", user.getBasket(), editor, param),
                    createDeleteButton("Remove from basket","Product has been deleted from basket",
                            "Something went wrong while deleting product from basket: Possibly you tried to delete more occurrences of a product, than exist in basket",
                            user.getBasket(), editor, param));
            return editor;
        });

        table.getColumns().add(expander);
    }

    private void addAdminRowExpander(final TableView table, Inventory inventory) {
        TableRowExpanderColumn<Map.Entry<Product, Integer>> expander =  new TableRowExpanderColumn<>(param -> {
            HBox editor = new HBox(10);
            editor.getChildren().addAll(createAddButton("Add to inventory","Product has been added to inventory",
                    "Something went wrong while adding product to inventory", inventory, editor, param),
                    createDeleteButton("Remove from inventory", "Product has been removed from inventory",
                            "Something went wrong while removing product from inventory: Possibly you tried to delete more occurrences of a product than exist in inventory ", inventory, editor, param));
            return editor;
        });

        table.getColumns().add(expander);
    }

    private Button createAddButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError, final ProductStorage storage, final HBox editor , final TableRowExpanderColumn.TableRowDataFeatures<Map.Entry<Product, Integer>> param) {
        Button addToBasket = new Button();
        addToBasket.setText(buttonText);

        addToBasket.setOnMouseClicked(mouseEvent -> {
            try {
                storage.addProducts(param.getValue().getKey(), 1);
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
                        .hideAfter(Duration.seconds(2))
                        .showError();
                e.printStackTrace();
            }
        });
        return addToBasket;
    }

    private Button createDeleteButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError, final ProductStorage storage, final HBox editor , final TableRowExpanderColumn.TableRowDataFeatures<Map.Entry<Product, Integer>> param) {
        Button deleteFromBasket = new Button();
        deleteFromBasket.setText(buttonText);

        deleteFromBasket.setOnMouseClicked(mouseEvent -> {
            try {
                storage.removeProducts(param.getValue().getKey(), 1);
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

    public static void main(String[] args) {
        launch(args);
    }
}
