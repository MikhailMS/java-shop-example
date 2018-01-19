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
                new String[]{"product_price", "product_amount"}, new String[]{});
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

    public void increaseProductAmountInInventory(final Connection connection, final Product product, final int amount)
            throws SQLException {
        final DBCursorHolder cursor = DBUtils.innerJoinTables(connection, "products", "inventory", "product_id",
                new String[]{"product_id", "product_amount"}, new String[]{String.format("product_name = '%s'", product.getName())});

        while (cursor.getResults().next()) {
            final int productId = cursor.getResults().getInt(1);
            final int productAmount = cursor.getResults().getInt(2);
            final int newAmount = productAmount + amount;
            DBUtils.updateTable(connection, "inventory", new String[]{"product_amount"}, new String[]{Integer.toString(newAmount)},
                    new String[]{String.format("product_id = %d", productId)});

        }
    }

    public void addNewProductToInventory(final Connection connection, final Product product, final int amount) throws SQLException {
        DBUtils.insertSpecificIntoTable(connection, "products", new String[]{"product_name", "product_weight", "product_price"},
                new String[]{String.format("'%s'", product.getName()), Double.toString(product.getWeight()), Double.toString(product.getPrice())});

        final DBCursorHolder cursor = DBUtils.filterFromTable(connection, "products", new String[]{"product_id"},
                new String[]{String.format("product_name = '%s'", product.getName())});
        while (cursor.getResults().next()) {
            final int productId = cursor.getResults().getInt(1);
            DBUtils.insertSpecificIntoTable(connection, "inventory", new String[]{"product_id", "product_amount"},
                    new String[]{Integer.toString(productId), Integer.toString(amount)});
        }
        cursor.closeCursor();
    }

    public void decreaseProductAmountInInventory(final Connection connection, final Product product, final int amount)
            throws SQLException, InventoryException {
        final DBCursorHolder cursor = DBUtils.innerJoinTables(connection, "products", "inventory", "product_id",
                new String[]{"product_id", "product_amount"}, new String[]{String.format("product_name = '%s'", product.getName())});

        while (cursor.getResults().next()) {
            final int productId = cursor.getResults().getInt(1);
            final int productAmount = cursor.getResults().getInt(2);
            final int newAmount = productAmount - amount;

            if (newAmount >= 0) {
                DBUtils.updateTable(connection, "inventory", new String[]{"product_amount"}, new String[]{Integer.toString(newAmount)},
                        new String[]{String.format("product_id = %d", productId)});
            } else {
                throw new InventoryException("You cannot removed specified amount of product");
            }
        }
    }

    public void createUser(final Connection connection, final String userName, final String userPasswd, final boolean privilege) {
        DBUtils.insertSpecificIntoTable(connection, "users", new String[]{"user_name", "user_password", "privileges"},
                new String[]{String.format("'%s'", userName), String.format("'%s'", userPasswd), privilege ? "'t'" : "'f'"});
    }

    public void deleteUser(final Connection connection, final String userName) {
        DBUtils.deleteFromTable(connection, "users", new String[]{String.format("user_name = '%s'", userName)});
    }
}
