package com.sree.stock.dto;

/**
 * To send payment information to kafka.
 * @param customerOrder for customer order details.
 * @param type for type of event.
 */
public record PaymentEvent(
        CustomerOrder customerOrder,
        String type
) {
}
