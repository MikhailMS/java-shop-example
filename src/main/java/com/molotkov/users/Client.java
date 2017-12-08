package com.molotkov.users;

import com.molotkov.Basket;
import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.products.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Client extends User {

    public Client(String name, String passwd) {
        super(name, passwd);
    }

    public void addProductToBasket(Basket basket, final Product product, int amount) throws BasketException {
        basket.addProducts(product, amount);
    }

    public void removeProductFromBasket(Basket basket, final Product product, final int amount) throws BasketException {
        basket.removeProducts(product, amount);
    }

    public void saveBasket(final Connection connection, final Basket basket) {
        ArrayList<String> valuesList = new ArrayList<>();
        valuesList.add(String.format("'%s'", super.getUserName()));
        valuesList.addAll(basket.toDBFormat());

        DBUtils.insertSpecificIntoTable(connection,"baskets",
                new String[] {"basket_owner", "products_name", "products_amount"}, valuesList.toArray(new String[0]));
    }

    public Basket restoreBasket(final Connection connection) throws SQLException {
        DBCursorHolder cursor = DBUtils.filterFromTable(connection,"baskets", new String[] {"products_name", "products_amount"},
                new String[] {String.format("basket_owner = '%s'",super.getUserName()), "AND" , "processed = FALSE"});

        cursor.getResults().next();
        final String productsName = cursor.getResults().getString(1);
        final String productsAmount = cursor.getResults().getString(2);

        Basket restoredBasket = new Basket();
        restoredBasket.restoreFromDB(productsName, productsAmount);
        cursor.closeCursor();

        return restoredBasket;
    }
}
