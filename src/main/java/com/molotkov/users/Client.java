package com.molotkov.users;

import com.molotkov.Basket;
import com.molotkov.Order;
import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Client extends User {

    public Client(final String name, final String passwd) {
        super(name, passwd);
    }

    public void addProductToBasket(final Basket basket, final Product product, final int amount) throws BasketException {
        basket.addProducts(product, amount);
    }

    public void removeProductFromBasket(final Basket basket, final Product product, final int amount) throws BasketException {
        basket.removeProducts(product, amount);
    }

    public void saveBasket(final Connection connection, final Basket basket) {
        final ArrayList<String> valuesList = new ArrayList<>();
        final List<String> basketDetails = basket.toDBFormat();
        final String names = basketDetails.get(0);
        final String amounts = basketDetails.get(1);

        valuesList.add(String.format("'%s'", super.getUserName()));
        valuesList.add(String.format("'%s'",names));
        valuesList.add(String.format("%s", amounts));

        DBUtils.insertSpecificIntoTable(connection,"baskets",
                new String[] {"basket_owner", "products_name", "products_amount"}, valuesList.toArray(new String[0]));
    }

    public Basket restoreBasket(final Connection connection) throws SQLException {
        final DBCursorHolder cursor = DBUtils.filterFromTable(connection,"baskets", new String[] {"products_name", "products_amount"},
                new String[] {String.format("basket_owner = '%s'", getUserName()), "AND" , "processed = FALSE"});

        cursor.getResults().next();
        final String productsName = cursor.getResults().getString(1);
        final String productsAmount = cursor.getResults().getString(2);

        final Basket restoredBasket = new Basket();
        restoredBasket.restoreFromDB(productsName, productsAmount);
        cursor.closeCursor();

        return restoredBasket;
    }

    public void saveOrder(final Connection connection, final Order order) throws SQLException {
        final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "baskets", new String[]{"basket_id"},
                new String[]{String.format("basket_owner = '%s'", getUserName()), "AND", "processed = FALSE"});
        cursor.getResults().next();

        final String basketId = cursor.getResults().getString(1);
        cursor.closeCursor();

        final ArrayList<String> orderValuesList = new ArrayList<>();
        orderValuesList.add(basketId);
        orderValuesList.add(String.format("'%s'", order.getAddress()));
        orderValuesList.add(String.format("'%s'", getUserName()));
        orderValuesList.add(Double.toString(order.getBasket().calculateTotal()));
        DBUtils.insertSpecificIntoTable(connection, "orders", new String[]{"basket_id", "address", "order_owner", "total_price"},
                orderValuesList.toArray(new String[0]));
    }

    public Order restoreOrder(final Connection connection) throws SQLException {
        // Retrieve order
        DBCursorHolder cursor = DBUtils.filterFromTable(connection, "orders", new String[]{"basket_id", "address"},
                new String[]{String.format("order_owner = '%s'", getUserName()), "AND", "completed = FALSE"});
        cursor.getResults().next();

        final String orderRetrieveBasketId = cursor.getResults().getString(1);
        final String orderRetrieveAddress = cursor.getResults().getString(2);
        cursor.closeCursor();

        // Retrieve related basket
        cursor = DBUtils.filterFromTable(connection, "baskets", new String[]{"products_name","products_amount"},
                new String[]{String.format("basket_id = %s", orderRetrieveBasketId), "AND", String.format("basket_owner = '%s'",getUserName())});
        cursor.getResults().next();

        final String basketRetrievedProductNames = cursor.getResults().getString(1);
        final String basketRetrievedProductAmounts = cursor.getResults().getString(2);
        cursor.closeCursor();

        // Restore basket
        final Basket restoredBasket = new Basket();
        restoredBasket.restoreFromDB(basketRetrievedProductNames, basketRetrievedProductAmounts);

        return new Order(restoredBasket, orderRetrieveAddress);
    }

    public void completeOrder(final Connection connection) throws SQLException {
        // Retrieve order
        final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "orders", new String[]{"basket_id"},
                new String[]{String.format("order_owner = '%s'", getUserName()), "AND", "completed = FALSE"});
        cursor.getResults().next();

        final String orderRetrieveBasketId = cursor.getResults().getString(1);
        cursor.closeCursor();

        DBUtils.updateTable(connection, "orders", new String[]{"completed"}, new String[]{"TRUE"},
                new String[]{String.format("basket_id = %s", orderRetrieveBasketId), "AND", String.format("order_owner = '%s'", getUserName())});
        DBUtils.updateTable(connection, "baskets", new String[]{"processed"}, new String[]{"TRUE"},
                new String[]{String.format("basket_id = %s", orderRetrieveBasketId), "AND", String.format("basket_owner = '%s'", getUserName())});
    }

}
