package com.molotkov.interfaces;

import com.molotkov.db.DBCursorHolder;

import java.sql.Connection;
import java.sql.SQLException;

public interface UserInterface {

    DBCursorHolder fetchOrders(Connection connection, String[] filterArguments) throws SQLException;

    DBCursorHolder fetchInventory(Connection connection, String[] filterArguments) throws SQLException;

}
