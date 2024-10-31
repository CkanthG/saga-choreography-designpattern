package com.sree.delivery.repository;

import com.sree.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * To do CRUD operations on Delivery table.
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
}
