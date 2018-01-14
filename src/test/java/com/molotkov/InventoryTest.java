package com.molotkov;

import com.molotkov.exceptions.InventoryException;
import com.molotkov.products.Product;
import org.junit.Before;
import org.junit.Test;
import java.text.DecimalFormat;

import static org.junit.Assert.assertTrue;

public class InventoryTest {
    private Inventory inventory;

    @Before
    public void setUp() {
        inventory = new Inventory();
    }

    @Test
    public void testInventoryConstructor() {
        assertTrue(inventory instanceof Inventory);
    }

    @Test
    public void testAddOneProduct() throws InventoryException {
        final Product test = new Product("Apple",0.150, 0.8);
        inventory.addProducts(test, 1);

        assertTrue(inventory.getProducts().containsKey(test));
        assertTrue(inventory.getProducts().get(test)==1);
    }

    @Test
    public void testAddSameProducts() throws InventoryException {
        final Product test = new Product("Apple",0.150, 0.8);
        inventory.addProducts(test, 1);
        inventory.addProducts(test, 1);

        assertTrue(inventory.getProducts().containsKey(test));
        assertTrue(inventory.getProducts().get(test)==2);
    }

    @Test
    public void testAddMultipleProducts() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        final Product test1 = new Product("Chicken", 1, 2.3);
        final Product test2 = new Product("Beef", 0.5, 3.25);
        inventory.addProducts(test,2);
        inventory.addProducts(test1,3);
        inventory.addProducts(test2,4);

        assertTrue(inventory.getProducts().containsKey(test));
        assertTrue(inventory.getProducts().containsKey(test1));
        assertTrue(inventory.getProducts().containsKey(test2));
        assertTrue(inventory.getProducts().get(test)==2);
        assertTrue(inventory.getProducts().get(test1)==3);
        assertTrue(inventory.getProducts().get(test2)==4);
    }

    @Test(expected = InventoryException.class)
    public void testAddNullProduct() throws InventoryException {
        inventory.addProducts(null, 1);
    }

    @Test
    public void testRemoveOneProduct() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        inventory.addProducts(test,2);
        inventory.removeProducts(test, 1);

        assertTrue(inventory.getProducts().get(test)==1);
    }

    @Test
    public void testRemoveProductCompletely() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        inventory.addProducts(test,2);
        inventory.removeProducts(test, 2);

        assertTrue(inventory.getProducts().get(test) == 0);
    }

    @Test(expected = InventoryException.class)
    public void testRemoveProductMoreThanExistsInBasket() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        inventory.addProducts(test,2);
        inventory.removeProducts(test, 3);
    }

    @Test
    public void testCalculateInventoryPrice() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        final Product test1 = new Product("Chicken", 1, 2.3);
        final DecimalFormat total = new DecimalFormat("####0.0");
        inventory.addProducts(test,2);
        inventory.addProducts(test1, 2);

        assertTrue(total.format(inventory.calculateTotal()).equals("6.2"));
    }

    @Test
    public void testSetStringFormatter() {
        inventory.setStringFormatter(() -> "New formatter");

        assertTrue(inventory.toString().equals("New formatter"));
    }

    @Test
    public void testToStringOneProduct() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        inventory.addProducts(test,2);

        assertTrue(inventory.toString().equals("Inventory has 1 product, total price of the stock: 1.6"));
    }

    @Test
    public void testToStringMultipleProducts() throws InventoryException {
        final Product test = new Product("Apple", 0.150, 0.8);
        final Product test1 = new Product("Chicken", 1, 2.3);
        inventory.addProducts(test,2);
        inventory.addProducts(test1, 2);

        assertTrue(inventory.toString().equals("Inventory has 2 products, total price of the stock: 6.2"));
    }
}
