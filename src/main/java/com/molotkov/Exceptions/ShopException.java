package com.molotkov.Exceptions;

public class ShopException  extends Exception {
    private static final long serialVersionUID = 5L;

    public ShopException(String message) {
        super(message);
    }

    public ShopException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
