package com.molotkov.gui;

import com.molotkov.Inventory;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.products.Product;

import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;

import java.text.DecimalFormat;
import java.util.Map;

public class InventoryScene extends Application {

    @Override
    public void start(Stage stage) {

        User admin = new Administrator("t", "t");
        User client = new Client("t", "t");

        Inventory inventory = new Inventory();
        try {
            inventory.addProducts(new Product("chicken", 1, 2.3),3);
            inventory.addProducts(new Product("apple", 0.151, 0.8), 2);
        } catch (InventoryException e) {
            e.printStackTrace();
        }

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

        final TableColumn<Map.Entry<Product, Integer>, Integer> productAmountColumn = new TableColumn<>("Amount");
        productAmountColumn.setCellValueFactory(item -> new SimpleObjectProperty<>(item.getValue().getValue()));

        final TableColumn<Map.Entry<Product, Integer>, String> productTotalColumn = new TableColumn<>("Product Total");
        productTotalColumn.setCellValueFactory(item -> new SimpleStringProperty(String.format("%.2f", item.getValue().getKey().getPrice() * item.getValue().getValue())));

        if (user instanceof Administrator) table.getColumns().setAll(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn, productTotalColumn);
        else table.getColumns().setAll(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn);

        TableRowExpanderColumn<Map.Entry<Product, Integer>> expander = new TableRowExpanderColumn<>(param -> {
            HBox editor = new HBox(10);
            Label detailsLabel = new Label();
            detailsLabel.setText(String.format("Weight: %.3f | Price: %.2f", param.getValue().getKey().getWeight(), param.getValue().getKey().getPrice()));
            editor.getChildren().addAll(detailsLabel);
            return editor;
        });

        table.getColumns().add(expander);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final TableFilter<Map.Entry<Product,Integer>> filter = TableFilter.forTableView(table).lazy(false).apply();

        return table;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
