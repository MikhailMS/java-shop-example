package com.molotkov.gui;

import com.molotkov.Inventory;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.interfaces.ProductStorage;
import com.molotkov.products.Product;

import com.molotkov.users.Administrator;
import com.molotkov.users.User;
import javafx.beans.property.SimpleObjectProperty;
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

import java.text.DecimalFormat;
import java.util.Map;

public class InventoryScene {
    private static final String PRODUCT_NAME_COLUMN = "Product Name";
    private static final String PRODUCT_WEIGHT_COLUMN = "Product Weight";
    private static final String PRODUCT_PRICE_COLUMN = "Product Price";
    private static final String PRODUCT_AMOUNT_COLUMN = "Quantity available in Inventory";
    private static final String PRODUCT_TOTAL_COLUMN = "Product Total Price";

    public static VBox createInventoryTableView(final Inventory inventory, final User user) {
        final ObservableList<Map.Entry<Product, Integer>> items = FXCollections.observableArrayList(inventory.getProducts().entrySet());

        final TableView<Map.Entry<Product, Integer>> table = new TableView<>(items);
        table.setEditable(true);

        final VBox inventoryTableView = new VBox();
        inventoryTableView.setSpacing(5);
        inventoryTableView.setPadding(new Insets(5, 5, 5, 5));

        final TableColumn<Map.Entry<Product, Integer>, String> productNameColumn = new TableColumn<>(PRODUCT_NAME_COLUMN);
        productNameColumn.setId(PRODUCT_NAME_COLUMN);
        productNameColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getKey().getName()));

        final TableColumn<Map.Entry<Product, Integer>, Double> productWeightColumn = new TableColumn<>(PRODUCT_WEIGHT_COLUMN);
        productWeightColumn.setId(PRODUCT_WEIGHT_COLUMN);
        productWeightColumn.setCellValueFactory(item -> {
            double weight = item.getValue().getKey().getWeight();
            DecimalFormat df = new DecimalFormat("#.###");
            weight = Double.valueOf(df.format(weight));
            return new SimpleObjectProperty<>(weight);
        });

        final TableColumn<Map.Entry<Product, Integer>, Double> productPriceColumn = new TableColumn<>(PRODUCT_PRICE_COLUMN);
        productPriceColumn.setId(PRODUCT_PRICE_COLUMN);
        productPriceColumn.setCellValueFactory(item -> {
            double price = item.getValue().getKey().getPrice();
            DecimalFormat df = new DecimalFormat("#.##");
            price = Double.valueOf(df.format(price));
            return new SimpleObjectProperty<>(price);
        });

        final TableColumn<Map.Entry<Product, Integer>, Integer> productAmountColumn = new TableColumn<>(PRODUCT_AMOUNT_COLUMN);
        productAmountColumn.setId(PRODUCT_AMOUNT_COLUMN);
        productAmountColumn.setCellValueFactory(item -> new SimpleObjectProperty<>(item.getValue().getValue()));

        final TableColumn<Map.Entry<Product, Integer>, String> productTotalColumn = new TableColumn<>(PRODUCT_TOTAL_COLUMN);
        productTotalColumn.setId(PRODUCT_TOTAL_COLUMN);
        productTotalColumn.setCellValueFactory(item -> new SimpleStringProperty(String.format("%.2f", item.getValue().getKey().getPrice() * item.getValue().getValue())));


        if (user instanceof Administrator) {
            table.getColumns().setAll(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn, productTotalColumn);
            addAdminRowExpander(table, inventory);
            final HBox addProductBox = createAddProductBox(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn, inventory, items);

            inventoryTableView.getChildren().addAll(table, addProductBox);
        }
        else {
            table.getColumns().setAll(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn);
            addClientRowExpander(table, user);
            inventoryTableView.getChildren().addAll(table);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final TableFilter<Map.Entry<Product,Integer>> filter = TableFilter.forTableView(table).lazy(false).apply();

        return inventoryTableView;
    }

    private static void addClientRowExpander(final TableView table, final User user) {
        TableRowExpanderColumn<Map.Entry<Product, Integer>> expander = new TableRowExpanderColumn<>(param -> {
            HBox editor = new HBox(10);
            Label detailsLabel = new Label("");
            detailsLabel.setText(String.format("Weight: %.3f | Price: %.2f", param.getValue().getKey().getWeight(), param.getValue().getKey().getPrice()));


            editor.getChildren().addAll(detailsLabel, createAddButton("Add to basket","Product has been added to basket",
                    "Something went wro ng while adding product to basket", user.getBasket(), editor, param),
                    createDeleteButton("Remove from basket","Product has been deleted from basket",
                            "Something went wrong while deleting product from basket: Possibly you tried to delete more occurrences of a product, than exist in basket",
                            user.getBasket(), editor, param));
            return editor;
        });
        expander.setId("client-expander");

        table.getColumns().add(expander);
    }

    private static void addAdminRowExpander(final TableView table, Inventory inventory) {
        TableRowExpanderColumn<Map.Entry<Product, Integer>> expander =  new TableRowExpanderColumn<>(param -> {
            final HBox editor = new HBox(10);
            editor.getChildren().addAll(createAddButton("Add to inventory","Product has been added to inventory",
                    "Something went wrong while adding product to inventory", inventory, editor, param),
                    createDeleteButton("Remove from inventory", "Product has been removed from inventory",
                            "Something went wrong while removing product from inventory: Possibly you tried to delete more occurrences of a product than exist in inventory", inventory, editor, param));
            return editor;
        });
        expander.setId("admin-expander");

        table.getColumns().add(expander);
    }

    private static Button createAddButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError, final ProductStorage storage, final HBox editor , final TableRowExpanderColumn.TableRowDataFeatures<Map.Entry<Product, Integer>> param) {
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

    private static Button createDeleteButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError, final ProductStorage storage, final HBox editor , final TableRowExpanderColumn.TableRowDataFeatures<Map.Entry<Product, Integer>> param) {
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

    private static HBox createAddProductBox(final TableColumn productNameColumn, final TableColumn productWeightColumn, final TableColumn productPriceColumn,
                                     final TableColumn productAmountColumn, final Inventory inventory, final ObservableList items) {
        final HBox addProductBox = new HBox();
        final TextField addProductName = new TextField();
        addProductName.setPromptText("Enter name");
        addProductName.setId("name");
        addProductName.setMaxWidth(productNameColumn.getPrefWidth());

        final TextField addProductWeight = new TextField();
        addProductWeight.setPromptText("Enter weight");
        addProductWeight.setId("weight");
        addProductWeight.setMaxWidth(productWeightColumn.getPrefWidth());

        final TextField addProductPrice = new TextField();
        addProductPrice.setPromptText("Enter price");
        addProductPrice.setId("price");
        addProductPrice.setMaxWidth(productPriceColumn.getPrefWidth());

        final TextField addProductAmount = new TextField();
        addProductAmount.setPromptText("Enter quantity");
        addProductAmount.setId("amount");
        addProductAmount.setMaxWidth(productAmountColumn.getPrefWidth());

        final Button addNewProductButton = new Button("Add new product");
        addNewProductButton.setOnMouseClicked(mouseEvent -> {
            final String newProductName = addProductName.getText();
            final String newProductWeight = addProductWeight.getText();
            final String newProductPrice = addProductPrice.getText();
            final String newProductAmount = addProductAmount.getText();
            if (!newProductName.isEmpty() && !newProductWeight.isEmpty() && !newProductPrice.isEmpty() && ! newProductAmount.isEmpty()) {
                try {
                    // Next 3 lines of code is huuuge hack - but can't think of another solution.
                    // It works, but may give poor performance on big ObservableList
                    items.removeAll(inventory.getProducts().entrySet());
                    inventory.addProducts(new Product(newProductName, Double.valueOf(newProductWeight), Double.valueOf(newProductPrice)), Integer.valueOf(newProductAmount));
                    items.addAll(inventory.getProducts().entrySet());

                    addProductName.clear();
                    addProductWeight.clear();
                    addProductPrice.clear();
                    addProductAmount.clear();
                } catch (InventoryException e) {
                    e.printStackTrace();
                    Notifications.create()
                            .darkStyle()
                            .title("Error")
                            .text(e.getMessage())
                            .position(Pos.CENTER)
                            .owner(Utils.getWindow(addProductBox))
                            .hideAfter(Duration.seconds(2))
                            .showConfirm();
                }
            } else {
                Notifications.create()
                        .darkStyle()
                        .title("Error")
                        .text("One of the fields is empty. Make sure all product descriptors are filled in")
                        .position(Pos.CENTER)
                        .owner(Utils.getWindow(addProductBox))
                        .hideAfter(Duration.seconds(2))
                        .showConfirm();
            }
        });
        addProductBox.getChildren().addAll(addProductName, addProductWeight, addProductPrice, addProductAmount, addNewProductButton);
        addProductBox.setSpacing(3);

        return addProductBox;
    }
}
