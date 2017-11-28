package com.molotkov;

import com.molotkov.Exceptions.BasketException;
import com.molotkov.Products.Product;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class OrderTest {
    private Order order;
    private Basket basket;

    @Before
    public void setUp() throws BasketException {
        basket = new Basket();
        final Product test1 = new Product("Apple", 0.150, 0.8);
        basket.addProducts(test1, 1);
        order = new Order(basket, "London");
    }

    @Test
    public void testOrderConstructor() {
        assertTrue(order instanceof Order);
    }
    @Test
    public void testSetBasket() {
        final Basket testBasket = new Basket();
        order.setBasket(testBasket);

        assertTrue(order.getBasket().equals(testBasket));
    }
    @Test
    public void testRemoveBasket() {
        order.removeBasket();

        assertTrue(null == order.getBasket());
    }
    @Test
    public void testChangeAddress() {
        order.changeAddress("Manchester");

        assertTrue(order.getAddress().equals("Manchester"));
    }
    @Test
    public void testSetStringFormatter() {
        order.setStringFormatter(() -> "New formatter");

        assertTrue(order.toString().equals("New formatter"));
    }
    @Test
    public void testToStringWithOneProduct() {
        assertTrue(order.toString().equals("Order includes 1 product and would be delivered to London"));
    }
    @Test
    public void testToStringWithMultipleProducts() throws BasketException {
        order.getBasket().addProducts(new Product("Chicken", 1, 2.3), 2);

        assertTrue(order.toString().equals("Order includes 2 products and would be delivered to London"));
    }
}
