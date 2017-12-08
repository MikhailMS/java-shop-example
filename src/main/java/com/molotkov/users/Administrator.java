package com.molotkov.users;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.db.DBUtils;
import com.molotkov.exceptions.InventoryException;
import com.molotkov.products.Product;

import java.sql.Connection;
import java.sql.SQLException;

public class Administrator extends User {

    public Administrator(final String name, final String passwd) {
        super(name, passwd);
    }

    public double getTotalPriceOfInventory(final Connection connection) throws SQLException {
        final DBCursorHolder cursor = DBUtils.innerJoinTables(connection, "products", "inventory", "product_id",
                new String[]{"product_price","product_amount"}, new String[]{});
        double total = 0.0;
        while (cursor.getResults().next()) {
            total += cursor.getResults().getDouble(1) * cursor.getResults().getDouble(2);
        }
        return total;
    }

    public double getTotalPriceOfAllOrders(final Connection connection) throws SQLException {
        final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "orders", new String[]{"total_price"}, new String[]{});
        double total = 0.0;
        while (cursor.getResults().next()) {
            total += cursor.getResults().getDouble(1);
        }
        return total;
    }

    public void addProductToInventory(final Connection connection, final Product product, final int amount) throws SQLException {
        DBUtils.insertSpecificIntoTable(connection, "products", new String[]{"product_name","product_weight","product_price"},
                new String[]{String.format("'%s'",product.getName()), Double.toString(product.getWeight()), Double.toString(product.getPrice())});
        final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "products", new String[]{"product_id"},
                new String[]{String.format("product_name = '%s'", product.getName())});
        cursor.getResults().next();
        final int productId = cursor.getResults().getInt(1);
        cursor.closeCursor();

        DBUtils.insertSpecificIntoTable(connection, "inventory", new String[]{"product_id", "product_amount"},
                new String[]{Integer.toString(productId), Integer.toString(amount)});
    }

    public void removeProductFromInventory(final Connection connection, final Product product, final int amount)
            throws SQLException, InventoryException {
        final DBCursorHolder cursor = DBUtils.innerJoinTables(connection, "products", "inventory", "product_id",
                new String[]{"product_id","product_amount"}, new String[]{String.format("product_name = '%s'",product.getName())});
        int productId = -1;
        int productAmount = -1;
        while(cursor.getResults().next()) {
            productId = cursor.getResults().getInt(1);
            productAmount = cursor.getResults().getInt(2);
        }
        final int newAmount = productAmount - amount;
        if (newAmount >= 0) {
            DBUtils.updateTable(connection, "inventory", new String[]{"product_amount"}, new String[]{Integer.toString(newAmount)},
                    new String[]{String.format("product_id = %d",productId)});
        } else {
            throw new InventoryException("You cannot order specified amount of product");
        }
    }

    public void createUser(final Connection connection, final String userName, final String userPasswd) {
        DBUtils.insertSpecificIntoTable(connection, "users", new String[]{"user_name","user_passwd"},
                new String[]{String.format("'%s'",userName), String.format("'%s'",userPasswd)});
    }

    public void deleteUser(final Connection connection, final String userName) {
        DBUtils.deleteFromTable(connection, "users", new String[]{String.format("user_name = '%s'",userName)});
    }
}
