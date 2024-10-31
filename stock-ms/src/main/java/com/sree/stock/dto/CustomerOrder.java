package com.sree.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * To receive order details from customer and map to it.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOrder {
    private String item;
    private Integer quantity;
    private Double amount;
    private String paymentMode;
    private Integer orderId;
    private String address;
}
