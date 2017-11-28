package com.molotkov.Exceptions;

public class BasketException  extends Exception {
    private static final long serialVersionUID = 2L;

    public BasketException(final String message) {
        super(message);
    }

    public BasketException(final String message, Throwable throwable) {
        super(message, throwable);
    }

}
