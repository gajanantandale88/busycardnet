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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import kotlin.random.Random

object AuctionNotificationHelper {
  private const val TAG = "AuctionNotifHelper"
  const val CHANNEL_ID = "bcn_auction_alerts"
  private const val CHANNEL_NAME = "Auction & Bidding Alerts"
  private const val CHANNEL_DESC = "Push notifications when you are outbid or your auction ends"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 300, 200, 300)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun sendOutbidNotification(
    context: Context,
    itemTitle: String,
    newBid: Double,
    outbidByName: String,
    refundAmount: Double,
    auctionItemId: String
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
        putExtra("AUCTION_ITEM_ID", auctionItemId)
      }

      val pendingIntent = PendingIntent.getActivity(
        context,
        auctionItemId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("⚠️ You've Been Outbid!")
        .setContentText("$outbidByName placed a higher bid of ₹${newBid.toInt()} on '$itemTitle'")
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(
            "⚠️ You were outbid on '$itemTitle'!\n" +
              "Current Highest Bid: ₹${newBid.toInt()} by $outbidByName.\n" +
              "Your upfront deposit of ₹${refundAmount.toInt()} is held in escrow and will be refunded automatically if you don't place a higher bid."
          )
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      val notificationManager = NotificationManagerCompat.from(context)
      val notifId = (System.currentTimeMillis() % 100000).toInt() + Random.nextInt(100)
      notificationManager.notify(notifId, notification)
      Log.d(TAG, "Outbid notification posted successfully for item $itemTitle")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send outbid notification: ${e.message}", e)
    }
  }

  fun sendAuctionEndedNotification(
    context: Context,
    itemTitle: String,
    isWinner: Boolean,
    winningBid: Double,
    refundAmount: Double
  ) {
    try {
      createNotificationChannel(context)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionCheck = ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.POST_NOTIFICATIONS
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) return
      }

      val title = if (isWinner) "🎉 Congratulations! You Won the Auction!" else "ℹ️ Auction Concluded: Deposit Refunded"
      val text = if (isWinner) {
        "You won '$itemTitle' with a bid of ₹${winningBid.toInt()}! The seller will contact you for delivery."
      } else {
        "Auction for '$itemTitle' has ended. Your upfront deposit of ₹${refundAmount.toInt()} has been refunded 100%."
      }

      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("NAV_TARGET", "MARKETPLACE")
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        Random.nextInt(10000),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(if (isWinner) android.R.drawable.star_big_on else android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send auction ended notification: ${e.message}", e)
    }
  }
}
