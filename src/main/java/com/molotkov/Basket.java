package com.molotkov;

import com.molotkov.exceptions.BasketException;
import com.molotkov.interfaces.ProductStorage;
import com.molotkov.interfaces.StringFormatter;
import com.molotkov.products.Product;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Basket implements ProductStorage {
    private HashMap<Product, Integer> products;
    private StringFormatter stringFormatter;

    Basket() {
        this.products = new HashMap<>();
        this.stringFormatter = () -> {
            final int basketSize = this.products.size();
            final String itemString = basketSize > 1 ? basketSize + " products." : basketSize + " product.";
            return String.format("Basket has %s", itemString);
        };
    }

    public void addProducts(final Product product, final int amount) throws BasketException {
        if (product != null) {
            final int currentAmount = this.products.getOrDefault(product,0);
            this.products.put(product, currentAmount + amount);
        } else {
            throw new BasketException("You cannot add Null objects to Basket!");
        }
    }

    public void removeProducts(final Product product, final int amount) throws BasketException {
        if (this.products.get(product) > amount) {
            this.products.replace(product,this.products.get(product)-amount);
        } else if (this.products.get(product) == amount) {
            this.products.remove(product);
        } else {
            throw new BasketException(String.format("Cannot remove %d instances of product as there are only %d instances!", amount, this.products.get(product)));
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

    public void setStringFormatter(final StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    public ArrayList<String> toDBFormat() {
        /*
            On bigger sets this implementation would be a bottleneck. Creation of 'names' and 'amounts' strings could be
            improved by either using threads or combine those two together
         */
        ArrayList<String> result = new ArrayList<>();
        String names = this.products.entrySet()
                .parallelStream()
                .map(p -> String.format("'%s'",p.getKey().getName()))
                .collect(Collectors.joining(","));
        String amounts = this.products.entrySet()
                .parallelStream()
                .map(p -> p.getValue().toString())
                .collect(Collectors.joining(","));
        result.add(names);
        result.add(amounts);

        return result;
    }

    public void restoreFromDB(String productsName, String productsAmount) {
        List<String> names = Arrays.asList(productsName.split(","));
        List<String> amounts = Arrays.asList(productsAmount.split(","));
        System.out.println(names);
        System.out.println(amounts);
        iterateSimultaneously(names, amounts, (String name, String amount) -> {
            try {
                addProducts(new Product(name, 0.150, 0.8), Integer.parseInt(amount));
            } catch (BasketException e) {
                e.printStackTrace();
            }
        });
    }

    private static <T1, T2> void iterateSimultaneously(Iterable<T1> c1, Iterable<T2> c2, BiConsumer<T1, T2> consumer) {
        Iterator<T1> i1 = c1.iterator();
        Iterator<T2> i2 = c2.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            consumer.accept(i1.next(), i2.next());
        }
    }

    @Override
    public String toString() {
        return stringFormatter.formatToString();
    }
}