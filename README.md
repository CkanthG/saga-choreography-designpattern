# SAGA Choreography Design Pattern

### Tech Stack Used
```
Java 21
SpringBoot 3.3.5
Kafka
MySQL
SAGA Choreography Design Pattern
```
### Implemented SAGA design pattern in microservices.
```
1. Order Microservice
2. Stock Microservice
3. Payment Microservice
4. Delivery Microservice
```

## Microservice Workflow
***Order Creation***

**Order Microservice**:
Receives the order creation request with the payload (e.g., item, quantity, amount, address, payment mode).
Logs and validates the order details.
Initiates the order flow by forwarding it to the Stock Microservice for availability check.

***Stock Verification***

**Stock Microservice**:
Checks if there is sufficient stock for the specified item and quantity.
If stock is available, the quantity is reserved or deducted from inventory, then the process is handed off to the Payment Microservice.
If stock is unavailable, the stock service notifies the order microservice to initiate a rollback, canceling the order and updating the user.

***Payment Processing or Order Reversion***

**Payment Microservice**:
Processes the payment using the specified paymentMode (e.g., CREDIT CARD).
If the payment is successful, it confirms the payment status to the order microservice and proceeds to the Delivery Microservice.
If the payment fails, the payment service rolls back the stock update and cancels the order, notifying the user of the failure.

***Delivery Verification and Order Completion***

**Delivery Microservice**:
Validates the delivery address provided in the order payload.
If the address is valid and serviceable, it confirms successful delivery, and the order status is updated as "Completed".
If the address is invalid or undeliverable, the delivery microservice rolls back:
The stock is returned to the inventory.
The payment is reverted (if necessary).
The order is marked as canceled, and the user is notified.
