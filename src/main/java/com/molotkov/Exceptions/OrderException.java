package com.molotkov.Exceptions;

public class OrderException  extends Exception {
    private static final long serialVersionUID = 3L;

    public OrderException(final String message) {
        super(message);
    }

    public OrderException(final String message, Throwable throwable) {
        super(message, throwable);
    }

}
