package com.sree.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.order.dto.CustomerOrder;
import com.sree.order.dto.OrderEvent;
import com.sree.order.entity.Order;
import com.sree.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    @Mock
    private ObjectMapper mapper;
    private CustomerOrder customerOrder;
    private Order order;
    private static final String VALID_EVENT_JSON = """
                {"customerOrder":{"item":"books","quantity":10,"amount":2000.0,"paymentMode":"CREDIT CARD",
                "orderId":102,"address":"Germany"},"type":"ORDER_REVERSED"}
                """;
    private static final String INVALID_JSON = "invalid_json";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // given
        customerOrder = new CustomerOrder();
        customerOrder.setItem("books");
        customerOrder.setQuantity(10);
        customerOrder.setAmount(2000.0);
        customerOrder.setPaymentMode("CREDIT CARD");

        order = new Order();
        order.setAmount(customerOrder.getAmount());
        order.setItem(customerOrder.getItem());
        order.setQuantity(customerOrder.getQuantity());
    }

    @Test
    void testCreateOrderSuccessfully() {
        // given
        customerOrder.setOrderId(102);
        customerOrder.setAddress("Germany");
        order.setStatus("Order Created");
        order.setOrderId(102);

        // when
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // then
        String actual = orderService.createOrder(customerOrder);
        assertEquals("Order Created Successfully", actual);
    }

    @Test
    void testCreateOrderFailed() {
        // when
        when(orderRepository.save(any(Order.class))).thenReturn(any(Order.class));

        // then
        String actual = orderService.createOrder(customerOrder);
        assertEquals("Order Creation Failed", actual);
    }

    @Test
    void testReverseOrderSuccessful() throws JsonProcessingException {
        // given
        order.setStatus("Order Created");
        order.setOrderId(102);
        OrderEvent orderEvent = new OrderEvent(customerOrder, "ORDER_REVERSED");

        // when
        when(mapper.readValue(VALID_EVENT_JSON, OrderEvent.class)).thenReturn(orderEvent);
        when(orderRepository.findById(customerOrder.getOrderId())).thenReturn(Optional.of(order));

        // then
        orderService.reverseOrder(VALID_EVENT_JSON);

        // verify
        verify(mapper, times(1)).readValue(VALID_EVENT_JSON, OrderEvent.class);
        verify(orderRepository, times(1)).findById(customerOrder.getOrderId());
    }

    @Test
    void testReverseOrderFailed() throws JsonProcessingException {
        // when
        when(mapper.readValue(INVALID_JSON, OrderEvent.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});

        // then
        orderService.reverseOrder(INVALID_JSON);

        // verify
        verify(mapper, times(1)).readValue(INVALID_JSON, OrderEvent.class);
    }
}
