package com.molotkov;

import com.molotkov.Exceptions.InventoryException;
import com.molotkov.Interfaces.StringFormatter;
import com.molotkov.Products.Product;

import java.text.DecimalFormat;
import java.util.HashMap;

public class Inventory {
    private HashMap<Product, Integer> products;
    private StringFormatter stringFormatter;

    public Inventory() {
        this.products = new HashMap<>();
        this.stringFormatter = () -> {
            int productsSize = this.products.size();
            String itemString = productsSize > 1 ? productsSize + " products" : productsSize + " product";
            DecimalFormat total = new DecimalFormat("####0.0");
            return String.format("Inventory has %s, total price of the stock: %s", itemString, total.format(calculateInventoryPrice()));
        };
    }

    public void addProducts(Product product, int amount) throws InventoryException {
        if (product != null) {
            int currentAmount = this.products.getOrDefault(product,0);
            this.products.put(product, currentAmount + amount);
        } else {
            throw new InventoryException("You cannot add Null objects to products!");
        }
    }

    public void removeProducts(Product product, int amount) throws InventoryException {
        if (this.products.get(product) > amount) {
            this.products.replace(product,this.products.get(product)-amount);
        } else if (this.products.get(product) == amount) {
            this.products.remove(product);
        } else {
            throw new InventoryException("Cannot remove " + amount + " instances of product as there are only " + this.products.get(product) + " instances!");
        }
    }

    public HashMap<Product, Integer> getInventory() {
        return products;
    }

    public double calculateInventoryPrice() {
        return this.products.entrySet()
                .parallelStream()
                .mapToDouble((product) -> product.getKey().getPrice()*product.getValue())
                .sum();
    }

    public void setStringFormatter(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return this.stringFormatter.formatToString();
    }
}
