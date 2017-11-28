package com.molotkov.Exceptions;

public class InventoryException  extends Exception {
    private static final long serialVersionUID = 4L;

    public InventoryException(final String message) {
        super(message);
    }

    public InventoryException(final String message, Throwable throwable) {
        super(message, throwable);
    }

}
