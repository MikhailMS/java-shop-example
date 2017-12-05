package com.molotkov.interfaces;

import com.molotkov.db.DBCursorHolder;

import java.sql.ResultSet;

public interface UserInterface {

    DBCursorHolder fetchOrders();
    DBCursorHolder fetchInventory();

}
