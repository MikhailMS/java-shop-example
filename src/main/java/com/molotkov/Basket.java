package com.molotkov;

import com.molotkov.Exceptions.BasketException;
import com.molotkov.Interfaces.StringFormatter;
import com.molotkov.Products.Product;
import java.util.HashMap;

public class Basket {
    private HashMap<Product, Integer> products;
    private StringFormatter stringFormatter;

    public Basket() {
        this.products = new HashMap<>();
        this.stringFormatter = () -> {
            int basketSize = this.products.size();
            String itemString = basketSize > 1 ? " products." : " product.";
            return "Basket has " + basketSize + itemString;
        };
    }

    public void addProducts(Product product, int amount) throws BasketException {
        if (product != null) {
            int currentAmount = this.products.getOrDefault(product,0);
            this.products.put(product, currentAmount + amount);
        } else {
            throw new BasketException("You cannot add Null objects to Basket!");
        }
    }

    public void removeProducts(Product product, int amount) throws BasketException {
        if (this.products.get(product) > amount) {
            this.products.replace(product,this.products.get(product)-amount);
        } else if (this.products.get(product) == amount) {
            this.products.remove(product);
        } else {
            throw new BasketException("Cannot remove " + amount + " instances of product as there are only " + this.products.get(product) + " instances!");
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
        return stringFormatter.formatToString();
    }

}
