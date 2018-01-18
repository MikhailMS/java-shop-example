package com.molotkov.exceptions;

public class BasketException extends Exception {
    private static final long serialVersionUID = 2L;

    public BasketException(final String message) {
        super(message);
    }

    public BasketException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
