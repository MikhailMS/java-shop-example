package com.molotkov;

import com.molotkov.Products.Product;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ProductTest {
    private Product product;

    @Before public void setup() {
        product = new Product("Apple", 150, 0.8);
    }

    @Test
    public void testProductConstructor() {
        assertTrue(product instanceof Product);
    }
    @Test
    public void testGetName() {
        assertTrue(product.getName().equals("Apple"));
    }
    @Test
    public void testGetWeight() {
        assertTrue(product.getWeight()==150);
    }
    @Test
    public void testGetPrice() {
        assertTrue(product.getPrice()==0.8);
    }
    @Test
    public void testSetPrice() {
        product.setPrice(1.1);

        assertTrue(product.getPrice()==1.1);
    }
    @Test
    public void testSetStringFormatter() {
        product.setStringFormatter(() -> "New formatter");

        assertTrue(product.toString().equals("New formatter"));
    }
    @Test
    public void testToString() {
        assertTrue(product.toString().equals("Product: Apple has price 0.8."));
    }
}
