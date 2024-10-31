package com.sree.stock.dto;

/**
 * To map the stock metadata to save into db.
 * @param item name of the stock.
 * @param quantity number of the stock.
 */
public record Stock(
        String item,
        Integer quantity
) {
}
