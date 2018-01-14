package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Inventory;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.interfaces.ProductStorage;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;

import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.controlsfx.tools.Utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Map;

import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

public class InventoryScene {
    private static final String PRODUCT_NAME_COLUMN = "Product Name";
    private static final String PRODUCT_WEIGHT_COLUMN = "Product Weight";
    private static final String PRODUCT_PRICE_COLUMN = "Product Price";
    private static final String PRODUCT_AMOUNT_COLUMN = "Quantity available in Inventory";
    private static final String PRODUCT_TOTAL_COLUMN = "Product Total Price";
    private static final String INVENTORY_DETAILS = "Details";

    private static final String BASKET_TOTAL_COLUMN = "Basket Total";
    private static final String ORDER_DETAILS = "Order Details";

    private static final String CLIENT_ADD_PRODUCT_BUTTON = "Add to basket";
    private static final String CLIENT_REMOVE_PRODUCT_BUTTON = "Remove from basket";
    private static final String CLIENT_ADD_PRODUCT_NOTIFICATION_SUCCESS = "Product has been added to basket";
    private static final String CLIENT_ADD_PRODUCT_NOTIFICATION_ERROR = "Something went wrong while adding product to basket";
    private static final String CLIENT_REMOVE_PRODUCT_NOTIFICATION_SUCCESS = "Product has been removed from basket";
    private static final String CLIENT_REMOVE_PRODUCT_NOTIFICATION_ERROR = "Something went wrong while removing product from basket: Possibly you tried to remove more occurrences of a product, than exist in basket";

    private static final String ADMIN_ADD_PRODUCT_BUTTON = "Add to inventory";
    private static final String ADMIN_REMOVE_PRODUCT_BUTTON = "Remove from inventory";
    private static final String ADMIN_ADD_PRODUCT_NOTIFICATION_SUCCESS = "Product has been added to inventory";
    private static final String ADMIN_ADD_PRODUCT_NOTIFICATION_ERROR = "Something went wrong while adding product to inventory";
    private static final String ADMIN_REMOVE_PRODUCT_NOTIFICATION_SUCCESS = "Product has been removed from inventory";
    private static final String ADMIN_REMOVE_PRODUCT_NOTIFICATION_ERROR = "Something went wrong while removing product from inventory: Possibly you tried to remove more occurrences of a product than exist in inventory";

    private static final String ADMIN_ADD_NEW_PRODUCT_BUTTON = "Add new product";
    private static final String ADMIN_ADD_NEW_PRODUCT_NOTIFICATION_SUCCESS = "One of the fields is empty. Make sure all product descriptors are filled in";

    private static HBox addProductBox;

    public static VBox createMainInventoryBox(final Inventory inventory, final User user, final Connection connection) {
        final VBox inventoryTableView = new VBox();
        inventoryTableView.setSpacing(5);
        inventoryTableView.setPadding(new Insets(5, 5, 5, 5));
        inventoryTableView.setAlignment(Pos.TOP_CENTER);

        inventoryTableView.getChildren().add(createTitleLabel("Inventory", Color.DARKBLUE, "Calibri", FontWeight.BOLD, 16));

        if (user instanceof Administrator) {
            inventoryTableView.getChildren().addAll(createInventoryTableView(inventory, user, connection), addProductBox);
        }
        else {
            inventoryTableView.getChildren().addAll(createInventoryTableView(inventory, user, connection), createTitleLabel("Basket", Color.DARKBLUE,
                    "Calibri", FontWeight.BOLD, 16), createBasketTableView(user.getBasket(), connection, user));
        }

        // fit content
        inventoryTableView.prefWidthProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_WIDTH));
        inventoryTableView.prefHeightProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_HEIGHT));

        return inventoryTableView;
    }

    private static void addDetailsRowExpander(final TableView table, final ProductStorage storage, final String addButtonText,
                                              final String removeButtonText, final String addNotificationTextSuccess, final String addNotificationTextError,
                                              final String removeNotificationTextSuccess, final String removeNotificationTextError,
                                              final User user, final Connection connection) {
        final TableRowExpanderColumn<Map.Entry<Product, Integer>> expander = new TableRowExpanderColumn<>(param -> {
            final HBox editor = new HBox(10);
            editor.getChildren().addAll(createAddButton(addButtonText, addNotificationTextSuccess, addNotificationTextError, storage, editor, param, user, connection),
                    createDeleteButton(removeButtonText, removeNotificationTextSuccess, removeNotificationTextError, storage, editor, param, user, connection));
            return editor;
        });
        expander.setText(INVENTORY_DETAILS);
        expander.setId(INVENTORY_DETAILS);
        expander.setSortable(false);

        table.getColumns().add(expander);
    }

    private static void addClientBasketRowExpander(final TableView table, final Connection connection, final User user) {
        final TextField address = new TextField();
        address.setPromptText("Enter delivery address");
        address.setId("delivery-address");

        final Button completeOrder = new Button();
        completeOrder.setText("Complete order");

        final TableRowExpanderColumn<Basket> expander = new TableRowExpanderColumn<>(param -> {
            HBox editor = new HBox(10);
            Label detailsLabel = new Label("");
            detailsLabel.setText(String.format("There are: %d items in the basket @ total price of %.2f", param.getValue().getProducts()
                    .entrySet().parallelStream().mapToInt(Map.Entry::getValue).sum(), param.getValue().calculateTotal()));

            completeOrder.setOnMouseClicked(mouseEvent -> {
                if(param.getValue().getProducts().size() > 0) {
                    if(!address.getText().isEmpty()) {
                        try {

                            final Client client = (Client)user;

                            if (client.retrievedBasketId() >= 0) {
                                client.completeOrder(connection, address.getText());
                            } else {
                                client.saveBasket(connection, client.getBasket());
                                client.setRetrievedBasketId(connection);
                                client.completeOrder(connection, address.getText());
                                client.setRetrievedBasketId(-1);
                            }

                            address.clear();

                            Notifications.create()
                                    .darkStyle()
                                    .title("Info")
                                    .text("Order has been made")
                                    .position(Pos.CENTER)
                                    .owner(Utils.getWindow(editor))
                                    .hideAfter(Duration.seconds(2))
                                    .showConfirm();

                        } catch (Exception e) {
                            Notifications.create()
                                    .darkStyle()
                                    .title("Error")
                                    .text("Order has not been completed. Try again")
                                    .position(Pos.CENTER)
                                    .owner(Utils.getWindow(editor))
                                    .hideAfter(Duration.seconds(2))
                                    .showError();
                            e.printStackTrace();
                        }
                    } else {
                        Notifications.create()
                                .darkStyle()
                                .title("Error")
                                .text("Cannot process the order: Enter the delivery address")
                                .position(Pos.CENTER)
                                .owner(Utils.getWindow(editor))
                                .hideAfter(Duration.seconds(2))
                                .showError();
                    }
                } else {
                    Notifications.create()
                            .darkStyle()
                            .title("Error")
                            .text("Cannot process the order: Add products to basket to complete order")
                            .position(Pos.CENTER)
                            .owner(Utils.getWindow(editor))
                            .hideAfter(Duration.seconds(2))
                            .showError();
                }
            });

            editor. getChildren().addAll(detailsLabel, address, completeOrder);

            return editor;
        });
        expander.setText(ORDER_DETAILS);
        expander.setId(ORDER_DETAILS);
        expander.setSortable(false);

        table.getColumns().add(expander);
    }

    private static Button createAddButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError,
                                          final ProductStorage storage, final HBox editor , final TableRowExpanderColumn.TableRowDataFeatures<Map.Entry<Product, Integer>> param,
                                          final User user, final Connection connection) {
        final Button addToBasket = new Button();
        addToBasket.setText(buttonText);

        addToBasket.setOnMouseClicked(mouseEvent -> {
            try {
                storage.addProducts(param.getValue().getKey(), 1);
                if (user instanceof Administrator) {
                    ((Administrator)user).increaseProductAmountInInventory(connection, param.getValue().getKey(), 1);
                } else {
                    System.out.println("Client detected - unlucky inventory");
                }
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

    private static Button createDeleteButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError,
                                             final ProductStorage storage, final HBox editor , final TableRowExpanderColumn.TableRowDataFeatures<Map.Entry<Product, Integer>> param,
                                             final User user, final Connection connection) {
        final Button deleteFromBasket = new Button();
        deleteFromBasket.setText(buttonText);
        deleteFromBasket.setOnMouseClicked(mouseEvent -> {
            try {
                storage.removeProducts(param.getValue().getKey(), 1);
                if (user instanceof Administrator) {
                    ((Administrator)user).decreaseProductAmountInInventory(connection, param.getValue().getKey(), 1);
                } else {
                    System.out.println("Client detected - lucky inventory");
                }
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

    private static Label createTitleLabel(final String title, final Color colorConst, final String fontFamily,
                                          final FontWeight fontWeight, final int fontSize) {
        final Label titleLabel = new Label(title);
        titleLabel.setTextFill(colorConst);
        titleLabel.setFont(Font.font(fontFamily, fontWeight, fontSize));

        return titleLabel;
    }

    private static TableView createInventoryTableView(final Inventory inventory, final User user, final Connection connection) {
        final ObservableList<Map.Entry<Product, Integer>> items = FXCollections.observableArrayList(inventory.getProducts().entrySet());

        final TableView<Map.Entry<Product, Integer>> table = new TableView<>(items);
        table.setEditable(true);
        table.setId("inventory-table-view");

        final TableColumn<Map.Entry<Product, Integer>, String> productNameColumn = new TableColumn<>(PRODUCT_NAME_COLUMN);
        productNameColumn.setId(PRODUCT_NAME_COLUMN);
        productNameColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getKey().getName()));

        final TableColumn<Map.Entry<Product, Integer>, Double> productWeightColumn = new TableColumn<>(PRODUCT_WEIGHT_COLUMN);
        productWeightColumn.setId(PRODUCT_WEIGHT_COLUMN);
        productWeightColumn.setCellValueFactory(item -> {
            final DecimalFormat df = new DecimalFormat("#.###");
            final double weight = Double.valueOf(df.format(item.getValue().getKey().getWeight()));

            return new SimpleObjectProperty<>(weight);
        });

        final TableColumn<Map.Entry<Product, Integer>, Double> productPriceColumn = new TableColumn<>(PRODUCT_PRICE_COLUMN);
        productPriceColumn.setId(PRODUCT_PRICE_COLUMN);
        productPriceColumn.setCellValueFactory(item -> {
            final DecimalFormat df = new DecimalFormat("#.##");
            final double price = Double.valueOf(df.format(item.getValue().getKey().getPrice()));

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
            addDetailsRowExpander(table, inventory, ADMIN_ADD_PRODUCT_BUTTON, ADMIN_REMOVE_PRODUCT_BUTTON, ADMIN_ADD_PRODUCT_NOTIFICATION_SUCCESS,
                    ADMIN_ADD_PRODUCT_NOTIFICATION_ERROR, ADMIN_REMOVE_PRODUCT_NOTIFICATION_SUCCESS, ADMIN_REMOVE_PRODUCT_NOTIFICATION_ERROR,
                    user, connection);
            addProductBox = createAddProductBox(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn, inventory, items, user, connection);
        }
        else {
            table.getColumns().setAll(productNameColumn, productWeightColumn, productPriceColumn, productAmountColumn);
            addDetailsRowExpander(table, user.getBasket(), CLIENT_ADD_PRODUCT_BUTTON, CLIENT_REMOVE_PRODUCT_BUTTON, CLIENT_ADD_PRODUCT_NOTIFICATION_SUCCESS,
                    CLIENT_ADD_PRODUCT_NOTIFICATION_ERROR, CLIENT_REMOVE_PRODUCT_NOTIFICATION_SUCCESS, CLIENT_REMOVE_PRODUCT_NOTIFICATION_ERROR,
                    user, connection);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final TableFilter<Map.Entry<Product,Integer>> filter = TableFilter.forTableView(table).lazy(false).apply();

        return table;
    }

    private static TableView createBasketTableView(final Basket basket, final Connection connection, final User user) {
        final ObservableList<Basket> items = FXCollections.observableArrayList(basket);

        final TableView<Basket> table = new TableView<>(items);
        table.setEditable(true);
        table.setId("basket-table-view");

        final TableColumn<Basket, String> basketTotalColumn = new TableColumn<>(BASKET_TOTAL_COLUMN);
        basketTotalColumn.setId(BASKET_TOTAL_COLUMN);
        basketTotalColumn.setCellValueFactory(item -> new SimpleStringProperty(String.format("%.2f", item.getValue().calculateTotal())));
        basketTotalColumn.setSortable(false);

        table.getColumns().add(basketTotalColumn);
        addClientBasketRowExpander(table, connection, user);

        return table;
    }

    private static HBox createAddProductBox(final TableColumn productNameColumn, final TableColumn productWeightColumn, final TableColumn productPriceColumn,
                                            final TableColumn productAmountColumn, final Inventory inventory, final ObservableList items,
                                            final User user, final Connection connection) {
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

        final Button addNewProductButton = new Button(ADMIN_ADD_NEW_PRODUCT_BUTTON);
        addNewProductButton.setOnMouseClicked(mouseEvent -> {
            final String newProductName = addProductName.getText();
            final String newProductWeight = addProductWeight.getText();
            final String newProductPrice = addProductPrice.getText();
            final String newProductAmount = addProductAmount.getText();
            if (!newProductName.isEmpty() && !newProductWeight.isEmpty() && !newProductPrice.isEmpty() && ! newProductAmount.isEmpty()) {
                try {

                    final Product product = new Product(newProductName, Double.parseDouble(newProductWeight), Double.parseDouble(newProductPrice));
                    // Here should also be a call to save new product to DB
                    try {
                        ((Administrator)user).addNewProductToInventory(connection, product, Integer.parseInt(newProductAmount));
                        // Next 3 lines of code is huuuge hack - but can't think of another solution.
                        // It works, but may give poor performance on big ObservableList
                        items.removeAll(inventory.getProducts().entrySet());
                        inventory.addProducts(product, Integer.parseInt(newProductAmount));
                        items.addAll(inventory.getProducts().entrySet());

                        addProductName.clear();
                        addProductWeight.clear();
                        addProductPrice.clear();
                        addProductAmount.clear();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
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
                        .text(ADMIN_ADD_NEW_PRODUCT_NOTIFICATION_SUCCESS)
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