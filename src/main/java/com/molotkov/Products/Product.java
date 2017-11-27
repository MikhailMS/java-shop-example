package com.molotkov.Products;

import com.molotkov.Interfaces.StringFormatter;

public class Product {
    private String name;
    private double weight;
    private double price;
    private StringFormatter stringFormatter;

    public Product(String name, double weight, double price) {
        this.name = name;
        this.weight = weight;
        this.price = price;
        this.stringFormatter = () -> "Product: " + this.name + " has price " + this.price + ".";
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStringFormatter(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return stringFormatter.formatToString();
    }
}
