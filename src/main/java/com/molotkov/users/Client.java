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
    private int idRetrievedBasket = -1;

    public Client(final String name, final String passwd) {
        super(name, passwd);
    }

    public void addProductToBasket(final Basket basket, final Product product, final int amount) throws BasketException {
        basket.addProducts(product, amount);
    }

    public void removeProductFromBasket(final Basket basket, final Product product, final int amount) throws BasketException {
        basket.removeProducts(product, amount);
    }

    public int retrievedBasketId() {
        return this.idRetrievedBasket;
    }

    public void setRetrievedBasketId(int retrievedBasketId) {
        this.idRetrievedBasket = retrievedBasketId;
    }

    public void saveBasket(final Connection connection, final Basket basket) {
        final List<String> basketDetails = basket.toDBFormat();
        final String names = basketDetails.get(0);
        final String amounts = basketDetails.get(1);

        DBUtils.insertSpecificIntoTable(connection,"baskets",
                new String[] {"basket_owner", "products_name", "products_amount"}, new String[]{String.format("'%s'", super.getUserName()),
                        String.format("'%s'",names), String.format("%s", amounts)});
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

    public void completeOrder(final Connection connection, final String address) {
        DBUtils.insertSpecificIntoTable(connection, "orders", new String[]{"basket_id", "order_owner",
        "address"}, new String[]{String.valueOf(idRetrievedBasket), String.format("'%s'", super.getUserName()),
        String.format("'%s'", address)});

        DBUtils.updateTable(connection, "baskets", new String[]{"processed"}, new String[]{"'t'"},
            new String[]{"processed='f'", "AND", String.format("basket_owner='%s'", super.getUserName()), "AND",
            String.format("basket_id=%d", idRetrievedBasket)});

    }
}
