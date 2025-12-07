package com.example.opp.service;

import com.example.opp.repository.BookingRepository;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {
    
    private static NotificationService instance;
    private final BookingRepository bookingRepository;
    private final ScheduledExecutorService scheduler;
    private final IntegerProperty newWebBookingsCount = new SimpleIntegerProperty(0);
    private LocalDateTime lastCheck = LocalDateTime.now();
    
    private NotificationService() {
        this.bookingRepository = new BookingRepository();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NotificationService");
            t.setDaemon(true);
            return t;
        });
    }
    
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    public void startPolling() {
        scheduler.scheduleAtFixedRate(this::checkForNewBookings, 0, 5, TimeUnit.SECONDS);
    }
    
    public void stopPolling() {
        scheduler.shutdown();
    }
    
    private void checkForNewBookings() {
        try {
            int count = bookingRepository.countNewWebBookingsSince(lastCheck);
            Platform.runLater(() -> {
                if (count > 0) {
                    newWebBookingsCount.set(newWebBookingsCount.get() + count);
                }
            });
            lastCheck = LocalDateTime.now();
        } catch (SQLException e) {
            // Log error silently
            System.err.println("Error checking new bookings: " + e.getMessage());
        }
    }
    
    public IntegerProperty newWebBookingsCountProperty() {
        return newWebBookingsCount;
    }
    
    public int getNewWebBookingsCount() {
        return newWebBookingsCount.get();
    }
    
    public void markBookingsAsRead() {
        Platform.runLater(() -> newWebBookingsCount.set(0));
    }
}