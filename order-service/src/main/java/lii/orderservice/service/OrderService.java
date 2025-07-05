package lii.orderservice.service;
import jakarta.transaction.Transactional;
import lii.orderservice.client.RestaurantClient;
import lii.orderservice.dto.CreateOrderRequest;
import lii.orderservice.dto.OrderItemRequest;
import lii.orderservice.dto.OrderResponse;
import lii.orderservice.enums.OrderStatus;
import lii.orderservice.event.OrderPlacedEvent;
import lii.orderservice.maaper.OrderMapper;
import lii.orderservice.model.Order;
import lii.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final OrderMapper orderMapper;
    private final RestaurantClient restaurantClient;

    @Value("${topic.name}")
    private String orderTopic;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request, UUID customerId) {
        restaurantClient.getRestaurantById(request.restaurantId())
                .blockOptional()
                .orElseThrow(() -> new IllegalArgumentException("Restaurant with ID " + request.restaurantId() + " not found."));


        request.items().forEach(this::validateMenuItem);

        log.info("All items validated. Creating order for customer: {}", customerId);

        Order order = orderMapper.toEntity(request);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);
        BigDecimal totalPrice = request.items().stream()
                .map(item -> item.price().multiply(new BigDecimal(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);
        publishOrderPlacedEvent(savedOrder);
        return orderMapper.toDto(savedOrder);
    }

    private void validateMenuItem(OrderItemRequest itemRequest) {
        var menuItem = restaurantClient.getMenuItemById(itemRequest.menuItemId())
                .blockOptional()
                .orElseThrow(() -> new IllegalArgumentException("Menu item with ID " + itemRequest.menuItemId() + " not found."));


        if (itemRequest.price().compareTo(menuItem.price()) != 0) {
            throw new IllegalArgumentException("Price mismatch for menu item " + menuItem.name() +
                    ". Expected: " + menuItem.price() + ", but received: " + itemRequest.price());
        }
    }

    private void publishOrderPlacedEvent(Order order) {
        var itemsData = order.getOrderItems().stream()
                .map(item -> new OrderPlacedEvent.OrderItemData(item.getMenuItemId(), item.getQuantity()))
                .collect(Collectors.toList());

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getTotalPrice(),
                itemsData
        );

        kafkaTemplate.send(orderTopic, event.orderId().toString(), event);
        log.info("Published OrderPlacedEvent for order ID: {}", event.orderId());
    }
}