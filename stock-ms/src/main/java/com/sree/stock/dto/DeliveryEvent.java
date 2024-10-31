package com.sree.stock.dto;

/**
 * To send delivery information to kafka.
 * @param customerOrder for customer order details.
 * @param type for type of event.
 */
public record DeliveryEvent(
        CustomerOrder customerOrder,
        String type
) {
}
