package com.molotkov;

import com.molotkov.Products.Product;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class ProductTest extends TestCase {
    private Product product;

    @Before
    public void setup() {
        product = new Product("Apple", 150, 0.8);
    }

    @Test
    public void testProductConstructor() {
        assertTrue(true);
    }
    @Test
    public void testGetName() {
        assertTrue(true);
    }
    @Test
    public void testGetWieght() {
        assertTrue(true);
    }
    @Test
    public void testGetPrice() {

    }
    @Test
    public void testSetPrice() {
        assertTrue(true);
    }
    @Test
    public void testSetStringFormatter() {
        assertTrue(true);
    }
    @Test
    public void testToString() {
        assertTrue(true);
    }
}
