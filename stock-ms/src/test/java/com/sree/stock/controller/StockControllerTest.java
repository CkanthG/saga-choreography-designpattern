package com.sree.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.stock.dto.Stock;
import com.sree.stock.service.StockService;
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

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private StockService stockService;

    @Test
    void testAddItemsSuccessful() throws Exception {
        Stock stock = new Stock("books", 100);

        String expectedResponse = "Successfully added stock";
        // Mock the stockService response
        when(stockService.addStock(stock)).thenReturn(expectedResponse);

        mockMvc.perform(
                        post("/stocks/addItems")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stock))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }
}