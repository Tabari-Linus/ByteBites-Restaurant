package com.bytebites.orderservice.repository;

import com.bytebites.orderservice.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.menuItemId = :menuItemId")
    List<OrderItem> findByMenuItemId(@Param("menuItemId") UUID menuItemId);

    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o WHERE o.restaurantId = :restaurantId")
    List<OrderItem> findByRestaurantId(@Param("restaurantId") UUID restaurantId);
}