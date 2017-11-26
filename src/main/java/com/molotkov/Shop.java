package com.molotkov;

import com.molotkov.Interfaces.StringFormatter;
import com.molotkov.Products.Product;

public class Shop {
    private Inventory inventory;
    private StringFormatter stringFormatter;

    public Shop() {
        this.inventory = new Inventory();
    }

    public void addToInventory(Product product) {
        this.inventory.addProduct(product);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setStringFormatter(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return this.stringFormatter.formatToString(this);
    }
}
