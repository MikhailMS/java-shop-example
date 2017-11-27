package com.molotkov.Exceptions;

public class OrderException  extends Exception {
    private static final long serialVersionUID = 3L;

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
