package com.molotkov.gui;

import com.molotkov.Order;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import static com.molotkov.gui.GuiWindowConsts.WINDOW_HEIGHT;
import static com.molotkov.gui.GuiWindowConsts.WINDOW_WIDTH;

public class HistoryScene {
    private static final String ORDER_ADDRESS_COLUMN = "Delivery address";
    private static final String ORDER_TOTAL_PRICE_COLUMN = "Total order price";
    private static final String TOTAL_ALL_ORDERS_COLUMN = "Total of all orders";
    private static final String EMPTY_COLUMN = "empty";

    public static BorderPane syncTablesIntoOneTable(final TableView orderTable, final TableView totalTable) {
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
        pane.prefWidthProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_WIDTH));
        pane.prefHeightProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_HEIGHT));

        return pane;
    }

    public static void syncScrollbars(final TableView orderTable, final TableView totalTable) {
        // synchronize scrollbars (must happen after table was made visible)
        final ScrollBar mainTableHorizontalScrollBar = findScrollBar( orderTable, Orientation.HORIZONTAL);
        final ScrollBar sumTableHorizontalScrollBar = findScrollBar( totalTable, Orientation.HORIZONTAL);
        try {
            mainTableHorizontalScrollBar.valueProperty().bindBidirectional(sumTableHorizontalScrollBar.valueProperty());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public static TableView createOrderTableView(final List<Order> orders) {
        ObservableList<Order> items = FXCollections.observableList(orders);
        final TableView<Order> table = new TableView<>(items);
        table.setId("order-table");
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

    public static TableView createTotalOrderTableView(final List<Order> orders) {
        Double totalCost = orders.parallelStream()
                .mapToDouble(price -> price.getBasket().calculateTotal())
                .sum();

        ObservableList<Double> totalCostList = FXCollections.observableArrayList();
        totalCostList.add(totalCost);

        final TableView<Double> table = new TableView<>(totalCostList);
        table.setId("total-table");

        final TableColumn<Double, String> orderLeftBlank = new TableColumn<>("");
        orderLeftBlank.setId(EMPTY_COLUMN);
        final TableColumn<Double, String> orderRightBlank = new TableColumn<>("");
        orderRightBlank.setId(EMPTY_COLUMN);

        final TableColumn<Double, Double> orderTotalPriceColumn = new TableColumn<>(TOTAL_ALL_ORDERS_COLUMN);
        orderTotalPriceColumn.setId(TOTAL_ALL_ORDERS_COLUMN);
        orderTotalPriceColumn.setCellValueFactory(price -> {
            final DecimalFormat df = new DecimalFormat("#.##");
            final double totalPrice = Double.valueOf(df.format(price.getValue()));
            return new SimpleObjectProperty<>(totalPrice);
        });

        table.getColumns().setAll(orderLeftBlank, orderTotalPriceColumn, orderRightBlank);

        return table;
    }

    private static void addCommonColumns(TableView table) {
        final TableColumn<Order, String> orderAddressColumn = new TableColumn<>(ORDER_ADDRESS_COLUMN);
        orderAddressColumn.setId(ORDER_ADDRESS_COLUMN);
        orderAddressColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getAddress()));

        final TableColumn<Order, Double> orderTotalColumn = new TableColumn<>(ORDER_TOTAL_PRICE_COLUMN);
        orderTotalColumn.setId(ORDER_TOTAL_PRICE_COLUMN);
        orderTotalColumn.setCellValueFactory(item -> {
            final DecimalFormat df = new DecimalFormat("#.##");
            final double totalPrice = Double.valueOf(df.format(item.getValue().getBasket().calculateTotal()));
            return new SimpleObjectProperty<>(totalPrice);
        });

        table.getColumns().setAll(orderAddressColumn, orderTotalColumn);
    }

    private static ScrollBar findScrollBar(TableView table, Orientation orientation) {

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
}
