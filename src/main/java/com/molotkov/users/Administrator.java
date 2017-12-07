package com.molotkov.users;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.products.Product;

import java.sql.Connection;
import java.sql.SQLException;

public class Administrator extends User {

    public Administrator(String name, String passwd) {
        super(name, passwd);
    }

    public double getTotalPriceOfInventory(final Connection connection) throws SQLException {
        DBCursorHolder cursor = DBUtils.innerJoinTables(connection, "products", "inventory", "product_id",
                new String[]{"product_price","product_amount"}, new String[]{});
        double total = 0.0;
        while (cursor.getResults().next()) {
            total += cursor.getResults().getDouble(1) * cursor.getResults().getDouble(2);
        }
        return total;
    }

    public void addProductToInventory(final Connection connection, final Product product, final int amount) {

    }

    public void removeProductFromInventory(final Connection connection, final Product product, final int amount) {

    }
}
