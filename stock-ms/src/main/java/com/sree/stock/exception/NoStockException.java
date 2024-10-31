package com.sree.stock.exception;

/**
 * To handle no stock exceptions.
 */
public class NoStockException extends Exception{
    public NoStockException(String message) {
        super(message);
    }
}
