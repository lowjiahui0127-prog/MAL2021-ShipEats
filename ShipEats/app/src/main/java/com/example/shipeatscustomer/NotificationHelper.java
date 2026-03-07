package com.example.shipeatscustomer;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationHelper {

    private static DatabaseReference getNotificationsRef(String userId) {
        return FirebaseDatabase.getInstance().getReference("notifications").child(userId);
    }

    public static void sendNotification(String userId, String title, String message,
                                        String type, String relatedId) {
        String notifId = getNotificationsRef(userId).push().getKey();
        if (notifId == null) return;

        AppNotification notification = new AppNotification(title, message, type, relatedId);
        notification.setId(notifId);

        getNotificationsRef(userId).child(notifId).setValue(notification);
    }

    // Overloaded for convenience
    public static void sendOrderNotification(String userId, String orderId, String status) {
        String title = "Order Update";
        String message = "Your order #" + orderId + " is now " + status + ".";
        sendNotification(userId, title, message, "order", orderId);
    }

    public static void sendAdminNotification(String adminId, String title, String message,
                                             String type, String relatedId) {
        sendNotification(adminId, title, message, type, relatedId);
    }
}