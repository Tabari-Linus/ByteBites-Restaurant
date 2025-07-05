package lii.orderservice.maaper;

import lii.orderservice.dto.CreateOrderRequest;
import lii.orderservice.dto.OrderItemRequest;
import lii.orderservice.dto.OrderItemResponse;
import lii.orderservice.dto.OrderResponse;
import lii.orderservice.model.Order;
import lii.orderservice.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setRestaurantId(request.restaurantId());

        List<OrderItem> orderItems = request.items().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        orderItems.forEach(item -> item.setOrder(order)); // Set bi-directional link

        return order;
    }

    public OrderItem toEntity(OrderItemRequest itemRequest) {
        OrderItem item = new OrderItem();
        item.setMenuItemId(itemRequest.menuItemId());
        item.setQuantity(itemRequest.quantity());
        item.setPrice(itemRequest.price());
        return item;
    }

    public OrderResponse toDto(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getStatus(),
                order.getTotalPrice(),
                itemResponses
        );
    }

    public OrderItemResponse toDto(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getMenuItemId(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }
}