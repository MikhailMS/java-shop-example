package com.molotkov.exceptions;

public class InventoryException extends Exception {
    private static final long serialVersionUID = 4L;

    public InventoryException(final String message) {
        super(message);
    }

    public InventoryException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
