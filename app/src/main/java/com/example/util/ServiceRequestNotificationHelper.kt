package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object ServiceRequestNotificationHelper {
  private const val TAG = "ServiceRequestNotif"
  const val CHANNEL_ID = "bcn_service_broadcasts"
  private const val CHANNEL_NAME = "Service Request Broadcasts & Business Alerts"
  private const val CHANNEL_DESC = "Push notifications when a proximity service request is broadcast to nearby businesses"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 250, 150, 250)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun sendBroadcastNotification(
    context: Context,
    requestId: String,
    title: String,
    category: String,
    radiusMeters: Int,
    notifiedBusinessCount: Int,
    locality: String,
    firestoreDocId: String
  ) {
    try {
      createNotificationChannel(context)

      // On Android 13+, check POST_NOTIFICATIONS permission
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionCheck = ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.POST_NOTIFICATIONS
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
          Log.w(TAG, "POST_NOTIFICATIONS permission not granted, skipping system push notification")
          return
        }
      }

      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("NAV_TARGET", "MARKETPLACE")
        putExtra("REQUEST_ID", requestId)
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        requestId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val radiusText = when {
        radiusMeters >= 1000 -> "${radiusMeters / 1000} km"
        else -> "${radiusMeters}m"
      }

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("📡 Service Broadcast Active ($radiusText Proximity)")
        .setContentText("Notified $notifiedBusinessCount nearby businesses for '$title'")
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(
            "📡 Service Request: $title\n" +
              "📍 Target Radius: $radiusText Proximity in $locality\n" +
              "🏢 Businesses Notified: $notifiedBusinessCount local members pinged\n" +
              "☁️ Firestore: Saved to collection 'service_requests' ($firestoreDocId)\n" +
              "Responses will appear in real time."
          )
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.notify(requestId.hashCode(), notification)
      Log.d(TAG, "Service request broadcast notification dispatched for $requestId")
    } catch (e: Exception) {
      Log.e(TAG, "Error posting broadcast notification: ${e.message}", e)
    }
  }

  /**
   * Dispatches an Android push notification when a new service request is posted
   * within the user's selected vicinity (500m, 1000m, 2000m, 5000m, 10000m, +10000m).
   */
  fun sendProximityAlertNotification(
    context: Context,
    requestId: String,
    title: String,
    category: String,
    requesterName: String,
    requesterBcnId: String,
    distanceMeters: Int,
    locality: String,
    budget: String,
    urgency: String = "URGENT"
  ) {
    try {
      createNotificationChannel(context)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionCheck = ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.POST_NOTIFICATIONS
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
          Log.w(TAG, "POST_NOTIFICATIONS permission not granted, skipping proximity alert notification")
          return
        }
      }

      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("NAV_TARGET", "SERVICE_REQUEST")
        putExtra("REQUEST_ID", requestId)
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        ("prox_$requestId").hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val distanceLabel = when {
        distanceMeters <= 500 -> "${distanceMeters}m (Immediate Vicinity)"
        distanceMeters <= 1000 -> "${distanceMeters}m (1000m Vicinity)"
        distanceMeters <= 2000 -> "${distanceMeters}m (2000m Vicinity)"
        distanceMeters <= 5000 -> "${distanceMeters / 1000f}km (5000m Vicinity)"
        distanceMeters <= 10000 -> "${distanceMeters / 1000f}km (10000m Vicinity)"
        else -> "${distanceMeters / 1000f}km (+10000m Vicinity)"
      }

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("🚨 New $category Request Nearby ($distanceLabel)")
        .setContentText("$requesterName posted '$title' • $budget")
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(
            "📍 Vicinity Alert: $distanceLabel\n" +
              "👤 Requester: $requesterName ($requesterBcnId)\n" +
              "⚡ Urgency: $urgency • Budget: $budget\n" +
              "📝 Task: $title\n" +
              "📍 Locality: $locality\n" +
              "Tap to open BusyCardNet, view details, or submit quote!"
          )
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.notify(("prox_$requestId").hashCode(), notification)
      Log.d(TAG, "Proximity alert notification dispatched for $requestId at distance $distanceMeters")
    } catch (e: Exception) {
      Log.e(TAG, "Error posting proximity alert notification: ${e.message}", e)
    }
  }
}
