package com.sree.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.order.dto.CustomerOrder;
import com.sree.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OrderService orderService;
    CustomerOrder customerOrder;

    @Test
    void testCreateOrderSuccessfully() throws Exception {
        customerOrder = new CustomerOrder();
        customerOrder.setItem("books");
        customerOrder.setQuantity(10);
        customerOrder.setAmount(2000.0);
        customerOrder.setPaymentMode("CREDIT CARD");
        customerOrder.setOrderId(102);

        String expectedResponse = "Order Created Successfully";
        // Mock the orderService response
        when(orderService.createOrder(customerOrder)).thenReturn(expectedResponse);

        mockMvc.perform(
                post("/orders/create")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerOrder))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }
}