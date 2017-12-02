package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.products.Product;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShopTest {
    private Shop shop;

    @Before
    public void setUp() {
        shop = new Shop();
    }

    @Test
    public void testShopConstructor() {
        assertTrue(shop instanceof Shop);
    }
    @Test
    public void testAddOneProductToInventory() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        shop.addToInventory(test,1);

        assertTrue(shop.getInventory().getProducts().get(test)==1);
        assertTrue(shop.getInventory().getProducts().containsKey(test));
    }
    @Test
    public void testAddTwoSameProductsToInventory() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        shop.addToInventory(test,1);
        shop.addToInventory(test,1);

        assertTrue(shop.getInventory().getProducts().get(test)==2);
        assertTrue(shop.getInventory().getProducts().containsKey(test));
    }
    @Test
    public void testAddMultipleProductsToInventory() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        final Product test1 = new Product("Chicken", 1, 2.3);
        final Product test2 = new Product("Beef", 0.5, 3.25);
        shop.addToInventory(test,2);
        shop.addToInventory(test1,3);
        shop.addToInventory(test2,4);

        assertTrue(shop.getInventory().getProducts().containsKey(test));
        assertTrue(shop.getInventory().getProducts().containsKey(test1));
        assertTrue(shop.getInventory().getProducts().containsKey(test2));
        assertTrue(shop.getInventory().getProducts().get(test)==2);
        assertTrue(shop.getInventory().getProducts().get(test1)==3);
        assertTrue(shop.getInventory().getProducts().get(test2)==4);
    }
    @Test(expected = InventoryException.class)
    public void testAddNullProductToInventory() throws InventoryException {
        shop.addToInventory(null,1);
    }
    @Test
    public void testRemoveOneInstanceOfProductFromInventory() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        shop.addToInventory(test, 2);
        shop.removeFromInventory(test,1);

        assertTrue(shop.getInventory().getProducts().get(test)==1);
    }
    @Test
    public void testRemoveProductCompletelyFromInventory() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        shop.addToInventory(test, 2);
        shop.removeFromInventory(test,2);

        assertFalse(shop.getInventory().getProducts().containsKey(test));
    }
    @Test(expected = InventoryException.class)
    public void testRemoveMoreInstancesOfProductFromInventoryThanExists() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        shop.addToInventory(test, 2);
        shop.removeFromInventory(test,3);
    }
    @Test
    public void testSetStringFormatter() {
        shop.setStringFormatter(() -> "New formatter");

        assertTrue(shop.toString().equals("New formatter"));
    }
    @Test
    public void testToStringOneProduct() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        shop.addToInventory(test,2);

        assertTrue(shop.toString().equals("Shop has inventory with 1 product and total value of 1.6"));
    }
    @Test
    public void testToStringMultipleProducts() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        final Product test1 = new Product("Chicken", 1, 2.3);
        shop.addToInventory(test,2);
        shop.addToInventory(test1, 2);

        assertTrue(shop.toString().equals("Shop has inventory with 2 products and total value of 6.2"));
    }
}
