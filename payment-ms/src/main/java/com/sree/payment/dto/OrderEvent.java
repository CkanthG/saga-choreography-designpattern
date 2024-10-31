package com.sree.payment.dto;

/**
 * To send order information to kafka.
 * @param customerOrder for customer order details.
 * @param type for type of event.
 */
public record OrderEvent(
        CustomerOrder customerOrder,
        String type
) {
}
