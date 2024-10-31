package com.sree.stock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sree.stock.dto.CustomerOrder;
import com.sree.stock.dto.DeliveryEvent;
import com.sree.stock.dto.PaymentEvent;
import com.sree.stock.dto.Stock;
import com.sree.stock.entity.WareHouse;
import com.sree.stock.exception.NoStockException;
import com.sree.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * To handle all stock related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final ObjectMapper mapper;
    private final KafkaTemplate<String, DeliveryEvent> kafkaDeliveryEventTemplate;
    private final KafkaTemplate<String, PaymentEvent> kafkaPaymentEventTemplate;
    @Value("${kafka.topic.new-stock:new-stock}")
    private String newStockTopic;
    @Value("${kafka.topic.reverse-payments:reverse-payments}")
    private String reversePaymentsTopic;

    /**
     * To listen kafka topic and do further action.
     * @param event is read from kafka topic.
     */
    @KafkaListener(topics = "${kafka.topic.new-payments}", groupId = "${kafka.topic.payments-group}")
    public void updateStock(String event) {
        log.info("Update stock for order :: {}", event);
        CustomerOrder customerOrder = null;
        DeliveryEvent deliveryEvent;
        try {
            PaymentEvent paymentEvent = mapper.readValue(event, PaymentEvent.class);
            customerOrder = paymentEvent.customerOrder();
            CustomerOrder finalCustomerOrder = customerOrder;
            List<WareHouse> stock = stockRepository.findByItem(customerOrder.getItem());
            if (stock.isEmpty()) {
                log.warn("No stock available, so reverting the order");
                throw new NoStockException("Stock not available");
            } else {
                for (WareHouse wareHouse : stock) {
                    if (wareHouse.getQuantity() >= finalCustomerOrder.getQuantity()) {
                        wareHouse.setQuantity(wareHouse.getQuantity() - finalCustomerOrder.getQuantity());
                        stockRepository.save(wareHouse);
                    } else throw new NoStockException("Stock not available");
                }
            }
            deliveryEvent = new DeliveryEvent(finalCustomerOrder, "STOCK_UPDATED");
            kafkaDeliveryEventTemplate.send(newStockTopic, deliveryEvent);
        } catch (JsonProcessingException e) {
            log.error("Process payment parsing exception :: {}", e.getMessage());
        } catch (NoStockException e) {
            PaymentEvent paymentEvent = new PaymentEvent(customerOrder, "PAYMENT_REVERSED");
            kafkaPaymentEventTemplate.send(reversePaymentsTopic, paymentEvent);
        }
    }

    /**
     * To listen kafka topic and do further action.
     * @param event is read from kafka topic.
     */
    @KafkaListener(topics = "${kafka.topic.reverse-stock}", groupId = "${kafka.topic.stock-group}")
    public void reverseStock(String event) {
        log.info("Reverse Stock :: {}", event);
        try {
            DeliveryEvent deliveryEvent = mapper.readValue(event, DeliveryEvent.class);
            stockRepository.findByItem(deliveryEvent.customerOrder().getItem())
                    .forEach(
                            item -> {
                                item.setQuantity(item.getQuantity() + deliveryEvent.customerOrder().getQuantity());
                                stockRepository.save(item);
                            }
                    );
            PaymentEvent paymentEvent = new PaymentEvent(deliveryEvent.customerOrder(), "PAYMENT_REVERSED");
            kafkaPaymentEventTemplate.send(reversePaymentsTopic, paymentEvent);
        } catch (JsonProcessingException e) {
            log.error("Reverse stock parsing exception :: {}", e.getMessage());
        }
    }

    /**
     * To add stock into db.
     * @param stock to store data in DB.
     * @return stock updated status message.
     */
    public String addStock(Stock stock) {
        List<WareHouse> stocks = stockRepository.findByItem(stock.item());
        if (stocks.isEmpty()) {
            WareHouse wareHouse = new WareHouse();
            wareHouse.setQuantity(stock.quantity());
            wareHouse.setItem(stock.item());
            stockRepository.save(wareHouse);
        } else {
            stocks.forEach(
                    s -> {
                        s.setQuantity(s.getQuantity() + stock.quantity());
                        stockRepository.save(s);
                    }
            );
        }
        return "Successfully added stock";
    }
}
