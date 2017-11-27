package com.molotkov.Exceptions;

public class ProductException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProductException(String message) {
        super(message);
    }

    public ProductException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
