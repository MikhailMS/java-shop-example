package com.molotkov;

import com.molotkov.Interfaces.StringFormatter;
import com.molotkov.Products.Product;
import java.util.HashMap;

public class Inventory {
    private HashMap<Product, Integer> inventory;
    private StringFormatter stringFormatter;

    public Inventory() {
        this.inventory = new HashMap<>();
    }

    public void addProduct(Product product) {
        this.inventory.put(product,this.inventory.get(product)+1);
    }

    public void removeProduct(Product product) {
        this.inventory.replace(product,this.inventory.get(product)-1);
    }

    public HashMap<Product, Integer> getStock() {
        return inventory;
    }

    public double getStockPrice() {
        return 1;
    }

    public void setStringFormatter(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return this.stringFormatter.formatToString(this);
    }
}
