package com.sree.order.repository;

import com.sree.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * To do CRUD operations on Order table.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
