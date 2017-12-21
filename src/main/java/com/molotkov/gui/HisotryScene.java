package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Order;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HisotryScene extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());

        stage.setWidth(400);
        stage.setHeight(600);

        Basket testBasket = new Basket();
        try {
            testBasket.addProducts(new Product("apple", 0.150, 0.8),3);
        } catch (BasketException e) {
            e.printStackTrace();
        }
        testBasket.setStringFormatter(()-> {
            final int basketSize = testBasket.getProducts().size();
            final String allProductsString = testBasket.getProducts().entrySet().stream()
                    .map(product -> {
                        final DecimalFormat df = new DecimalFormat("#.##");
                        final double price = Double.valueOf(df.format(product.getKey().getPrice()));
                        return String.format("%d %s @ %.2f£", product.getValue(), product.getKey().getName(), price);
                    })
                    .collect(Collectors.joining(", "));
            final String itemString = basketSize > 1 ? basketSize + " products" : basketSize + " product";
            return String.format("Order contains %s: %s", itemString, allProductsString);
        });
        Order testOrder1 = new Order(testBasket, "London");
        Order testOrder2 = new Order(testBasket, "Manchester");

        List<Order> testOrders = new ArrayList<>();
        testOrders.add(testOrder1);
        testOrders.add(testOrder2);

        createFullOrderTableView(stage, scene, testOrders);
    }

    public void createFullOrderTableView(final Stage stage, final Scene scene, final List<Order> orders) {
        TableView orderTable = createOrderTableView(orders);
        TableView totalTable = createTotalOrderTableView(orders);
        totalTable.setPrefHeight(100);

        BorderPane pane = new BorderPane();

        // bind/sync tables
        for( int i=0; i < orderTable.getColumns().size(); i++) {

            TableColumn<Order,?> mainColumn = (TableColumn) orderTable.getColumns().get(i);
            TableColumn<Double,?> sumColumn = (TableColumn) totalTable.getColumns().get(i);

            // sync column widths
            sumColumn.prefWidthProperty().bind( mainColumn.widthProperty());

            // sync visibility
            sumColumn.visibleProperty().bind( mainColumn.visibleProperty());

        }

        pane.setCenter(orderTable);
        pane.setBottom(totalTable);

        // fit content
        pane.prefWidthProperty().bind(scene.widthProperty());
        pane.prefHeightProperty().bind(scene.heightProperty());

        ((Group) scene.getRoot()).getChildren().addAll(pane);

        stage.setScene(scene);
        stage.show();

        // synchronize scrollbars (must happen after table was made visible)
        ScrollBar mainTableHorizontalScrollBar = findScrollBar( orderTable, Orientation.HORIZONTAL);
        ScrollBar sumTableHorizontalScrollBar = findScrollBar( totalTable, Orientation.HORIZONTAL);
        mainTableHorizontalScrollBar.valueProperty().bindBidirectional( sumTableHorizontalScrollBar.valueProperty());
    }

    public TableView createOrderTableView(final List<Order> orders) {
        ObservableList<Order> items = FXCollections.observableList(orders);
        final TableView<Order> table = new TableView<>(items);

        addCommonColumns(table);

        TableRowExpanderColumn<Order> expander = new TableRowExpanderColumn<>(param -> {
            HBox editor = new HBox(10);
            Label detailsLabel = new Label();
            detailsLabel.setText(String.format("%s", param.getValue().getBasket().toString()));
            editor.getChildren().addAll(detailsLabel);
            return editor;
        });

        table.getColumns().add(expander);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final TableFilter<Order> filter = TableFilter.forTableView(table).lazy(false).apply();

        return table;
    }

    public TableView createTotalOrderTableView(final List<Order> orders) {
        Double totalCost = orders.parallelStream()
                .mapToDouble(price -> price.getBasket().calculateTotal())
                .sum();

        ObservableList<Double> totalCostList = FXCollections.observableArrayList();
        totalCostList.add(totalCost);

        final TableView<Double> table = new TableView<>(totalCostList);

        final TableColumn<Double, String> orderLeftBlank = new TableColumn<>("");
        final TableColumn<Double, String> orderRightBlank = new TableColumn<>("");

        final TableColumn<Double, Double> orderTotalPriceColumn = new TableColumn<>("Total of all orders");
        orderTotalPriceColumn.setCellValueFactory(price -> {
            final DecimalFormat df = new DecimalFormat("#.##");
            final double totalPrice = Double.valueOf(df.format(price.getValue()));
            return new SimpleObjectProperty<>(totalPrice);
        });

        table.getColumns().setAll(orderLeftBlank, orderTotalPriceColumn, orderRightBlank);

        return table;
    }

    private void addCommonColumns(TableView table) {
        final TableColumn<Order, String> orderAddressColumn = new TableColumn<>("Delivery Address");
        orderAddressColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getAddress()));

        final TableColumn<Order, Double> orderTotalColumn = new TableColumn<>("Total price");
        orderTotalColumn.setCellValueFactory(item -> {
            final DecimalFormat df = new DecimalFormat("#.##");
            final double totalPrice = Double.valueOf(df.format(item.getValue().getBasket().calculateTotal()));
            return new SimpleObjectProperty<>(totalPrice);
        });

        table.getColumns().setAll(orderAddressColumn, orderTotalColumn);
    }

    private ScrollBar findScrollBar(TableView table, Orientation orientation) {

        // this would be the preferred solution, but it doesn't work. it always gives back the vertical scrollbar
        //		return (ScrollBar) table.lookup(".scroll-bar:horizontal");
        //
        // => we have to search all scrollbars and return the one with the proper orientation

        Set<Node> set = table.lookupAll(".scroll-bar");
        for( Node node: set) {
            ScrollBar bar = (ScrollBar) node;
            if( bar.getOrientation() == orientation) {
                return bar;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
