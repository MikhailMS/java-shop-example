package com.molotkov.gui;

import com.molotkov.Basket;
import com.molotkov.Inventory;
import com.molotkov.Order;
import com.molotkov.db.DBConnector;
import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.BasketException;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.products.Product;
import com.molotkov.users.Administrator;
import com.molotkov.users.Client;
import com.molotkov.users.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class GuiDbUtils {

    public static void loadDataToInventory(final Connection connection, final Inventory inventory, final User user) {
        DBCursorHolder cursor;
        try {
            if (user instanceof Client) {
                cursor = DBUtils.innerJoinTables(connection, "products", "inventory", "product_id",
                        new String[] {"product_name", "product_weight", "product_price", "product_amount"}, new String[]{"product_amount > 0"});
            } else {
                cursor = DBUtils.innerJoinTables(connection, "products", "inventory", "product_id",
                        new String[] {"product_name", "product_weight", "product_price", "product_amount"}, new String[]{});
            }

            while (cursor.getResults().next()) {
                inventory.addProducts(new Product(cursor.getResults().getString(1),
                        cursor.getResults().getDouble(2), cursor.getResults().getDouble(3)),cursor.getResults().getInt(4));
            }
            cursor.closeCursor();
        } catch (SQLException | InventoryException e) {
            e.printStackTrace();
        }
    }

    public static void loadDataToUserList(final Connection connection, final List<User> userList) {
        DBCursorHolder cursor;
        try {
            cursor = DBUtils.filterFromTable(connection, "users", new String[]{"user_name", "user_password", "privileges"},
                    new String[]{});
            while (cursor.getResults().next()) {
                if (cursor.getResults().getBoolean(3)) userList.add(new Administrator(cursor.getResults().getString(1),
                        cursor.getResults().getString(2)));
                else userList.add(new Client(cursor.getResults().getString(1), cursor.getResults().getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadDataToOrders(final User user, final Connection connection, final List<Order> orders) {
        DBCursorHolder cursor;
        try {
            if (user instanceof Administrator) cursor = DBUtils.innerJoinTables(connection, "baskets","orders",
                    "basket_id", new String[]{"products_name", "products_amount", "address"}, new String[]{});
            else cursor = DBUtils.innerJoinTables(connection, "baskets","orders",
                    "basket_id", new String[]{"products_name", "products_amount", "address"},
                    new String[]{String.format("basket_owner='%s'", user.getUserName())});
            while (cursor.getResults().next()) {
                final Basket orderBasket = new Basket();
                constructBasketFromDB(connection, cursor.getResults(), orderBasket);
                orders.add(new Order(orderBasket, cursor.getResults().getString(3)));
            }
            cursor.closeCursor();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadSavedBasket(final Client client, final Connection connection, final Basket basket) {
        DBCursorHolder cursor;
        try {
            cursor = DBUtils.filterFromTable(connection, "baskets", new String[]{"products_name", "products_amount", "basket_id"},
                    new String[]{String.format("basket_owner='%s'", client.getUserName()), "AND", "processed='f'"});
            while (cursor.getResults().next()) {
                client.setRetrievedBasketId(cursor.getResults().getInt(3));
                constructBasketFromDB(connection, cursor.getResults(), basket);
            }
            cursor.closeCursor();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void constructBasketFromDB(final Connection connection, final ResultSet products, final Basket basketToConstruct) {
        try {
            final List<String> names = Arrays.asList(products.getString(1).split(","));
            final List<String> amounts = Arrays.asList(products.getString(2).split(","));

            Product restoredProduct;
            int counter = 0;

            for(final String productName : names) {
                final DBCursorHolder productDetails = DBUtils.filterFromTable(connection, "products",
                        new String[]{"product_weight", "product_price"}, new String[]{String.format("product_name='%s'", productName)});
                while (productDetails.getResults().next()) {
                    restoredProduct = new Product(productName, productDetails.getResults().getDouble(1),
                            productDetails.getResults().getDouble(2));
                    basketToConstruct.addProducts(restoredProduct, Integer.parseInt(amounts.get(counter)));
                }
                productDetails.closeCursor();
                counter++;
            }
        } catch (SQLException | BasketException e) {
            e.printStackTrace();
        }
    }

}
