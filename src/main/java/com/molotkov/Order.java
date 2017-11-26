package com.molotkov;

import com.molotkov.Interfaces.StringFormatter;

public class Order {
    private Basket basket;
    private String address;
    private StringFormatter stringFormatter;

     public Order(Basket basket, String address) {
         this.basket = basket;
         this.address = address;
     }

    public Basket getBasket() {
        return basket;
    }

    public void addBasket(Basket basket) {
        this.basket = basket;
    }

    public void removeBasket() {
         this.basket = null;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStringFormatter(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return this.stringFormatter.formatToString(this);
    }
}
