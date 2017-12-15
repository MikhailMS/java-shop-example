package com.molotkov.gui;

import com.molotkov.Inventory;
import com.molotkov.products.Product;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.Map;

public class InventoryScene {

    public TableView createInventoryTableView(final Inventory inventory) {
        TableColumn<Map.Entry<Product, Integer>, String> column1 = new TableColumn<>("Product name");
        column1.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey().getName()));
        TableColumn<Map.Entry<Product, Integer>, Integer> column2 = new TableColumn<>("Amount");
        column2.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));

        ObservableList<Map.Entry<Product, Integer>> items = FXCollections.observableArrayList(inventory.getProducts().entrySet());
        final TableView<Map.Entry<Product, Integer>> table = new TableView<>(items);

        table.getColumns().setAll(column1, column2);

        return table;
    }

}
