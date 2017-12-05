package com.molotkov.users;

import com.molotkov.db.DBCursorHolder;
import com.molotkov.interfaces.UserInterface;

public class User implements UserInterface {
    private String userName;
    private String userPasswd;

    public User(String name, String passwd) {
        this.userName = name;
        this.userPasswd = passwd;
    }

    @Override
    public DBCursorHolder fetchOrders() {
        return null;
    }

    @Override
    public DBCursorHolder fetchInventory() {
        return null;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPasswd() {
        return userPasswd;
    }
}
