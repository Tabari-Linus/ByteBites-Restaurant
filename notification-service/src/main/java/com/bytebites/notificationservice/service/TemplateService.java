package com.bytebites.notificationservice.service;

import com.bytebites.notificationservice.event.OrderPlacedEvent;
import com.bytebites.notificationservice.event.OrderStatusChangedEvent;
import com.bytebites.notificationservice.event.RestaurantCreatedEvent;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class TemplateService {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public String generateOrderPlacedCustomerSubject(OrderPlacedEvent event) {
        return String.format("Order Confirmation - Order #%s",
                event.orderId().toString().substring(0, 8).toUpperCase());
    }

    public String generateOrderPlacedCustomerContent(OrderPlacedEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Dear Customer,\n\n");
        content.append("Thank you for your order! Your order has been successfully placed.\n\n");
        content.append("Order Details:\n");
        content.append("- Order ID: ").append(event.orderId()).append("\n");
        content.append("- Restaurant: ").append(event.restaurantName()).append("\n");
        content.append("- Total Amount: ").append(currencyFormat.format(event.totalAmount())).append("\n");
        content.append("- Delivery Address: ").append(event.deliveryAddress()).append("\n\n");

        content.append("Items Ordered:\n");
        event.items().forEach(item ->
                content.append("- ").append(item.menuItemName())
                        .append(" x").append(item.quantity())
                        .append(" - ").append(currencyFormat.format(item.subtotal())).append("\n")
        );

        content.append("\nYour order is being processed and you will receive updates on its status.\n\n");
        content.append("Thank you for choosing ByteBites!\n");
        content.append("The ByteBites Team");

        return content.toString();
    }

    public String generateOrderPlacedRestaurantSubject(OrderPlacedEvent event) {
        return String.format("New Order Received - Order #%s",
                event.orderId().toString().substring(0, 8).toUpperCase());
    }

    public String generateOrderPlacedRestaurantContent(OrderPlacedEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Dear Restaurant Partner,\n\n");
        content.append("You have received a new order!\n\n");
        content.append("Order Details:\n");
        content.append("- Order ID: ").append(event.orderId()).append("\n");
        content.append("- Customer ID: ").append(event.customerId()).append("\n");
        content.append("- Total Amount: ").append(currencyFormat.format(event.totalAmount())).append("\n");
        content.append("- Delivery Address: ").append(event.deliveryAddress()).append("\n\n");

        content.append("Items to Prepare:\n");
        event.items().forEach(item ->
                content.append("- ").append(item.menuItemName())
                        .append(" x").append(item.quantity()).append("\n")
        );

        content.append("\nPlease log into your restaurant dashboard to confirm and manage this order.\n\n");
        content.append("Best regards,\n");
        content.append("The ByteBites Team");

        return content.toString();
    }

    public String generateOrderStatusChangedSubject(OrderStatusChangedEvent event) {
        return String.format("Order Status Update - Order #%s is now %s",
                event.orderId().toString().substring(0, 8).toUpperCase(),
                event.newStatus().toLowerCase());
    }

    public String generateOrderStatusChangedContent(OrderStatusChangedEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Dear Customer,\n\n");
        content.append("Your order status has been updated!\n\n");
        content.append("Order Details:\n");
        content.append("- Order ID: ").append(event.orderId()).append("\n");
        content.append("- Restaurant: ").append(event.restaurantName()).append("\n");
        content.append("- Previous Status: ").append(event.previousStatus()).append("\n");
        content.append("- New Status: ").append(event.newStatus()).append("\n\n");

        content.append(getStatusMessage(event.newStatus()));

        content.append("\n\nThank you for choosing ByteBites!\n");
        content.append("The ByteBites Team");

        return content.toString();
    }

    public String generateRestaurantCreatedSubject(RestaurantCreatedEvent event) {
        return String.format("Welcome to ByteBites - %s Registration Successful!", event.restaurantName());
    }

    public String generateRestaurantCreatedContent(RestaurantCreatedEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Dear Restaurant Owner,\n\n");
        content.append("Congratulations! Your restaurant has been successfully registered on ByteBites.\n\n");
        content.append("Restaurant Details:\n");
        content.append("- Restaurant Name: ").append(event.restaurantName()).append("\n");
        content.append("- Restaurant ID: ").append(event.restaurantId()).append("\n");
        content.append("- Address: ").append(event.address()).append("\n");
        content.append("- Current Status: ").append(event.status()).append("\n\n");

        if ("PENDING_APPROVAL".equals(event.status())) {
            content.append("Your restaurant is currently under review. ");
            content.append("Our team will review your application and approve it within 24-48 hours.\n\n");
            content.append("Once approved, you'll be able to:\n");
            content.append("- Manage your menu items\n");
            content.append("- Receive and process orders\n");
            content.append("- Access analytics and reports\n\n");
        }

        content.append("Thank you for partnering with ByteBites!\n\n");
        content.append("Best regards,\n");
        content.append("The ByteBites Partnership Team");

        return content.toString();
    }

    private String getStatusMessage(String status) {
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> "Great news! Your order has been confirmed by the restaurant and is being prepared.";
            case "PREPARING" -> "Your order is currently being prepared. It should be ready soon!";
            case "READY" -> "Your order is ready for pickup/delivery!";
            case "DELIVERED" -> "Your order has been delivered. We hope you enjoy your meal!";
            case "CANCELLED" -> "Unfortunately, your order has been cancelled. If you have any questions, please contact our support team.";
            default -> "Your order status has been updated.";
        };
    }
}
