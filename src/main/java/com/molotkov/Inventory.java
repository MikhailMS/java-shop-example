package com.molotkov;

import com.molotkov.Exceptions.InventoryException;
import com.molotkov.Interfaces.ProductStorage;
import com.molotkov.Interfaces.StringFormatter;
import com.molotkov.Products.Product;
import java.text.DecimalFormat;
import java.util.HashMap;

public class Inventory implements ProductStorage {
    private HashMap<Product, Integer> products;
    private StringFormatter stringFormatter;

    Inventory() {
        this.products = new HashMap<>();
        this.stringFormatter = () -> {
            final int productsSize = this.products.size();
            final String itemString = productsSize > 1 ? productsSize + " products" : productsSize + " product";
            final DecimalFormat total = new DecimalFormat("####0.0");
            return String.format("Inventory has %s, total price of the stock: %s", itemString, total.format(calculateTotal()));
        };
    }

    public void addProducts(Product product, int amount) throws InventoryException {
        if (product != null) {
            final int currentAmount = this.products.getOrDefault(product,0);
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
            throw new InventoryException(String.format("Cannot remove %d instances of product as there are only %d instances!", amount, this.products.get(product)));
        }
    }

    public HashMap<Product, Integer> getProducts() {
        return products;
    }

    public double calculateTotal() {
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