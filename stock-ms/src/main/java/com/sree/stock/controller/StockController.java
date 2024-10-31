package com.sree.stock.controller;

import com.sree.stock.dto.Stock;
import com.sree.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * To handle all stock related user requests.
 */
@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * To add stock
     * @param stock to save stock information in db.
     * @return updated status
     */
    @PostMapping("/addItems")
    public ResponseEntity<String> addItems(@RequestBody Stock stock) {
        return ResponseEntity.ok(stockService.addStock(stock));
    }
}
