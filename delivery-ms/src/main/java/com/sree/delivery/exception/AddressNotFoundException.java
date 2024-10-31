package com.sree.delivery.exception;

/**
 * To handle address not found exception.
 */
public class AddressNotFoundException extends Exception{
    public AddressNotFoundException(String message) {
        super(message);
    }
}
