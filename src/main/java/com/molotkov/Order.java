package com.molotkov;

import com.molotkov.Interfaces.StringFormatter;

public class Order {
    private Basket basket;
    private String address;
    private StringFormatter stringFormatter;

     public Order(Basket basket, String address) {
         this.basket = basket;
         this.address = address;
         this.stringFormatter = () -> {
             final int basketSize = basket.getProducts().size();
             final String itemString = basketSize > 1 ? basketSize + " products" : basketSize + " product";
             return String.format("Order includes %s and would be delivered to %s", itemString, address);
         };
     }

    public Basket getBasket() {
        return basket;
    }

    public void setBasket(Basket basket) {
        this.basket = basket;
    }

    public void removeBasket() {
         this.basket = null;
    }

    public String getAddress() {
        return address;
    }

    public void changeAddress(String address) {
        this.address = address;
    }

    public void setStringFormatter(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return this.stringFormatter.formatToString();
    }
}
