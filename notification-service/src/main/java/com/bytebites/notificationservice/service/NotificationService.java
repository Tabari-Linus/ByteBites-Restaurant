package com.bytebites.notificationservice.service;

import com.bytebites.notificationservice.enums.NotificationType;
import com.bytebites.notificationservice.event.OrderPlacedEvent;
import com.bytebites.notificationservice.event.OrderStatusChangedEvent;
import com.bytebites.notificationservice.event.RestaurantCreatedEvent;
import com.bytebites.notificationservice.model.Notification;
import com.bytebites.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final TemplateService templateService;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository,
                               TemplateService templateService,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.templateService = templateService;
        this.emailService = emailService;
    }

    public void sendOrderPlacedNotificationToCustomer(OrderPlacedEvent event) {
        logger.info("Sending order placed notification to customer: {}", event.customerId());

        
        Optional<Notification> existing = notificationRepository.findByEventId(event.eventId() + "_customer");
        if (existing.isPresent()) {
            logger.warn("Notification already processed for event: {}", event.eventId());
            return;
        }

        try {
            String subject = templateService.generateOrderPlacedCustomerSubject(event);
            String content = templateService.generateOrderPlacedCustomerContent(event);

            Notification notification = new Notification(
                    event.customerId(),
                    event.customerEmail(),
                    NotificationType.ORDER_PLACED_CUSTOMER,
                    subject,
                    content,
                    event.eventId() + "_customer"
            );

            
            notification.addMetadata("orderId", event.orderId().toString());
            notification.addMetadata("restaurantId", event.restaurantId().toString());
            notification.addMetadata("totalAmount", event.totalAmount().toString());

            
            emailService.sendEmail(event.customerEmail(), subject, content);
            notification.markAsSent();

            
            notificationRepository.save(notification);

            logger.info("Order placed notification sent successfully to customer: {}", event.customerId());

        } catch (Exception e) {
            logger.error("Failed to send order placed notification to customer: {}, error: {}",
                    event.customerId(), e.getMessage(), e);

            
            Notification notification = new Notification(
                    event.customerId(),
                    event.customerEmail(),
                    NotificationType.ORDER_PLACED_CUSTOMER,
                    "Order Confirmation",
                    "Failed to generate content",
                    event.eventId() + "_customer"
            );
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);

            throw new RuntimeException("Failed to send customer notification", e);
        }
    }

    public void sendOrderPlacedNotificationToRestaurant(OrderPlacedEvent event) {
        logger.info("Sending order placed notification to restaurant: {}", event.restaurantId());

        
        Optional<Notification> existing = notificationRepository.findByEventId(event.eventId() + "_restaurant");
        if (existing.isPresent()) {
            logger.warn("Notification already processed for event: {}", event.eventId());
            return;
        }

        try {
            String subject = templateService.generateOrderPlacedRestaurantSubject(event);
            String content = templateService.generateOrderPlacedRestaurantContent(event);

            
            String restaurantEmail = "restaurant@example.com"; 

            Notification notification = new Notification(
                    event.restaurantId(), 
                    restaurantEmail,
                    NotificationType.ORDER_PLACED_RESTAURANT,
                    subject,
                    content,
                    event.eventId() + "_restaurant"
            );

            
            notification.addMetadata("orderId", event.orderId().toString());
            notification.addMetadata("customerId", event.customerId().toString());

            
            emailService.sendEmail(restaurantEmail, subject, content);
            notification.markAsSent();

            
            notificationRepository.save(notification);

            logger.info("Order placed notification sent successfully to restaurant: {}", event.restaurantId());

        } catch (Exception e) {
            logger.error("Failed to send order placed notification to restaurant: {}, error: {}",
                    event.restaurantId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send restaurant notification", e);
        }
    }

    public void sendOrderStatusChangedNotification(OrderStatusChangedEvent event) {
        logger.info("Sending order status changed notification to customer: {}", event.customerId());

        
        Optional<Notification> existing = notificationRepository.findByEventId(event.eventId());
        if (existing.isPresent()) {
            logger.warn("Notification already processed for event: {}", event.eventId());
            return;
        }

        try {
            String subject = templateService.generateOrderStatusChangedSubject(event);
            String content = templateService.generateOrderStatusChangedContent(event);

            Notification notification = new Notification(
                    event.customerId(),
                    event.customerEmail(),
                    NotificationType.ORDER_STATUS_CHANGED,
                    subject,
                    content,
                    event.eventId()
            );

            
            notification.addMetadata("orderId", event.orderId().toString());
            notification.addMetadata("previousStatus", event.previousStatus());
            notification.addMetadata("newStatus", event.newStatus());

            
            emailService.sendEmail(event.customerEmail(), subject, content);
            notification.markAsSent();

            
            notificationRepository.save(notification);

            logger.info("Order status changed notification sent successfully to customer: {}", event.customerId());

        } catch (Exception e) {
            logger.error("Failed to send order status changed notification to customer: {}, error: {}",
                    event.customerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send status change notification", e);
        }
    }

    public void sendRestaurantCreatedNotification(RestaurantCreatedEvent event) {
        logger.info("Sending restaurant created notification to owner: {}", event.ownerId());

        
        Optional<Notification> existing = notificationRepository.findByEventId(event.eventId());
        if (existing.isPresent()) {
            logger.warn("Notification already processed for event: {}", event.eventId());
            return;
        }

        try {
            String subject = templateService.generateRestaurantCreatedSubject(event);
            String content = templateService.generateRestaurantCreatedContent(event);

            Notification notification = new Notification(
                    event.ownerId(),
                    event.ownerEmail(),
                    NotificationType.RESTAURANT_CREATED,
                    subject,
                    content,
                    event.eventId()
            );

            
            notification.addMetadata("restaurantId", event.restaurantId().toString());
            notification.addMetadata("restaurantName", event.restaurantName());
            notification.addMetadata("status", event.status());

            
            emailService.sendEmail(event.ownerEmail(), subject, content);
            notification.markAsSent();

            
            notificationRepository.save(notification);

            logger.info("Restaurant created notification sent successfully to owner: {}", event.ownerId());

        } catch (Exception e) {
            logger.error("Failed to send restaurant created notification to owner: {}, error: {}",
                    event.ownerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send restaurant notification", e);
        }
    }
}
