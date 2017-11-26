package com.molotkov;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class OrderTest extends TestCase {
    private Order order;

    @Before
    public void setup() {
        order = new Order(new Basket(), "");
    }

    @Test
    public void testOrderConstructor() {
        assertTrue(true);
    }
    @Test
    public void testGetBasket() {
        assertTrue(true);
    }
    @Test
    public void testAddBasket() {
        assertTrue(true);
    }
    @Test
    public void testRemoveBasket() {

    }
    @Test
    public void testGetAddress() {
        assertTrue(true);
    }
    @Test
    public void testSetAddress() {
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
