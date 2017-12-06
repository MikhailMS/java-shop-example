package com.molotkov.interfaces;

import com.molotkov.db.DBCursorHolder;

import java.sql.Connection;
import java.sql.SQLException;

public interface UserInterface {

    DBCursorHolder fetchOrders(final Connection connection, String[] filterArguments) throws SQLException;
    DBCursorHolder fetchInventory(final Connection connection) throws SQLException;

}
