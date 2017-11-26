package com.molotkov;

import com.molotkov.Interfaces.StringFormatter;
import com.molotkov.Products.Product;

import java.util.ArrayList;
import java.util.List;

public class Basket {
    private List<Product> products;
    private StringFormatter stringFormatter;

    public Basket() {
        this.products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
    }

    public List<Product> getProducts() {
        return products;
    }

    public double calculateTotal() {
        return this.products.parallelStream()
                .mapToDouble(Product::getPrice)
                .sum();
    }

    public void setStringFormatter(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return stringFormatter.formatToString(this);
    }

}
