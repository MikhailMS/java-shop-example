package com.molotkov.exceptions;

public class ShopException extends Exception {
    private static final long serialVersionUID = 5L;

    public ShopException(final String message) {
        super(message);
    }

    public ShopException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
