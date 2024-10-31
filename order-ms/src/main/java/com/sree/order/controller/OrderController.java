package com.sree.order.controller;

import com.sree.order.dto.CustomerOrder;
import com.sree.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * To handle all order related user requests.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * To create an order.
     * @param customerOrder to create a customer order.
     * @return status message
     */
    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody CustomerOrder customerOrder) {
        return ResponseEntity.ok(orderService.createOrder(customerOrder));
    }
}
