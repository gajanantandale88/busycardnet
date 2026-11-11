package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.AuctionBidEntity
import com.example.data.local.AuctionItemEntity
import com.example.data.local.AuctionNotificationEntity
import com.example.data.local.MarketplaceProductEntity
import com.example.data.local.ProductQuotationEntity
import kotlinx.coroutines.delay

/**
 * Format remaining milliseconds into human-readable countdown string.
 */
fun formatRemainingTime(remainingMillis: Long): String {
  if (remainingMillis <= 0) return "AUCTION ENDED"
  val totalSeconds = remainingMillis / 1000
  val days = totalSeconds / 86400
  val hours = (totalSeconds % 86400) / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60

  return if (days > 0) {
    "${days}d ${hours}h ${minutes}m ${seconds}s"
  } else if (hours > 0) {
    String.format("%02d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format("%02d:%02d", minutes, seconds)
  }
}

/**
 * Auction Item Card displaying product photo, live real-time countdown timer,
 * current bid, upfront deposit required, refund guarantee, and bidding actions.
 */
@Composable
fun AuctionItemCard(
  item: AuctionItemEntity,
  onBidClick: () -> Unit,
  onTestOutbid: () -> Unit,
  onExpired: () -> Unit,
  onHandoverToStorage: (() -> Unit)? = null,
  onDispatchFromStorage: (() -> Unit)? = null,
  onConfirmDelivery: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

  // Real-time countdown timer tick every second
  LaunchedEffect(item.id, item.status) {
    while (item.status == "ACTIVE" && currentTime < item.endTime) {
      delay(1000L)
      currentTime = System.currentTimeMillis()
      if (currentTime >= item.endTime) {
        onExpired()
      }
    }
  }

  val remainingMillis = (item.endTime - currentTime).coerceAtLeast(0L)
  val isEnded = item.status == "ENDED" || remainingMillis <= 0L
  val isUrgent = !isEnded && remainingMillis < 3600000L // Less than 1 hour

  val upfrontDeposit = (item.currentBid * item.upfrontPercent / 100.0)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(
        width = 1.dp,
        color = if (isEnded) Color(0xFFCBD5E1) else if (isUrgent) Color(0xFFEF4444) else Color(0xFF3B82F6),
        shape = RoundedCornerShape(16.dp)
      )
      .testTag("auction_item_${item.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column {
      // 1. Photo & Real-Time Countdown Header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
      ) {
        // Product Photo
        if (item.imageUrl.isNotBlank()) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(item.imageUrl)
              .crossfade(true)
              .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(54.dp)
              )
              Text(
                text = item.category,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
        }

        // Dark gradient overlay on bottom of photo for text legibility
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                startY = 60f
              )
            )
        )

        // Top Badges: Category & Escrow %
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.65f)
          ) {
            Text(
              text = item.category.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF2563EB)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "${item.upfrontPercent}% Upfront Escrow",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }

        // Bottom on Photo: Real-time Countdown Timer Ribbon
        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isEnded) Color(0xFF475569) else if (isUrgent) Color(0xFFDC2626) else Color(0xFF059669)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = if (isEnded) Icons.Default.CheckCircle else Icons.Default.Timer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isEnded) "AUCTION CONCLUDED" else "Ends in: " + formatRemainingTime(remainingMillis),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }
          }

          Text(
            text = "Total ${item.totalBids} bids",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }

      // 2. Content Details
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = item.title,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = item.description,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Price & Escrow Summary Box
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFEFF6FF),
          border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (isEnded) "WINNING BID" else "CURRENT HIGHEST BID",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E40AF)
              )
              Text(
                text = "₹${item.currentBid.toInt()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E3A8A)
              )
              Text(
                text = "Started at ₹${item.startingPrice.toInt()} • by ${item.sellerName}",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
              )
              BcnCardHyperlink(
                bcnId = item.sellerBusyCardNetId,
                color = Color(0xFF1E40AF),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 1.dp)
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFDBEAFE)
              ) {
                Text(
                  text = "Upfront: ₹${upfrontDeposit.toInt()} (${item.upfrontPercent}%)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1D4ED8),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "100% refundable if outbid",
                fontSize = 9.sp,
                color = Color(0xFF059669),
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Highest Bidder Info
        if (item.highestBidderName != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isEnded) "Winner: ${item.highestBidderName} 🎉" else "Leading Bidder: ${item.highestBidderName}",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            BcnCardHyperlink(
              bcnId = "IN-MH-MU-26-108RES390",
              fontSize = 10.sp,
              color = Color(0xFF2563EB)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!isEnded) {
            Button(
              onClick = onBidClick,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
              ),
              modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("bid_button_${item.id}")
            ) {
              Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Place Bid (Pay ${item.upfrontPercent}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Quick Outbid Test Button (Allows immediate simulation of push notification & deposit refund)
            OutlinedButton(
              onClick = onTestOutbid,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .height(40.dp)
                .testTag("test_outbid_button_${item.id}")
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Outbid", fontSize = 11.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
            }

            // Quick Close Bid Button for immediate logistics testing
            OutlinedButton(
              onClick = onExpired,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
              modifier = Modifier
                .height(40.dp)
                .testTag("close_bid_button_${item.id}")
            ) {
              Text("Close Bid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        if (isEnded) {
          Spacer(modifier = Modifier.height(8.dp))
          // Complete BCN Storage & Buyer Delivery Logistics Cycle Tracker
          AuctionLogisticsCycleView(
            item = item,
            onHandoverToStorage = onHandoverToStorage,
            onDispatchFromStorage = onDispatchFromStorage,
            onConfirmDelivery = onConfirmDelivery
          )
        }
      }
    }
  }
}

/**
 * BCN Storage & Buyer Delivery Logistics Cycle Tracker.
 *
 * Implements the mandatory logistics workflow:
 * 1. Bid Closed: Winning bid locked in escrow.
 * 2. Seller Handover: Seller hands over physical product to BCN Central Storage Hub.
 * 3. BCN Storage: Storage custody, quality verification, and shelf slot assignment.
 * 4. Dispatch from Storage: Secure delivery from BCN Storage directly to Buyer.
 * 5. Delivered to Buyer: Buyer confirms delivery & escrow funds released to Seller.
 */
@Composable
fun AuctionLogisticsCycleView(
  item: AuctionItemEntity,
  onHandoverToStorage: (() -> Unit)? = null,
  onDispatchFromStorage: (() -> Unit)? = null,
  onConfirmDelivery: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val currentStage = when (item.logisticsStatus) {
    "IN_BCN_STORAGE" -> 2
    "OUT_FOR_DELIVERY" -> 3
    "DELIVERED_TO_BUYER" -> 4
    else -> 1 // AWAITING_SELLER_HANDOVER
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFF8FAFC),
    border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Top Logistics Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "BCN STORAGE LOGISTICS CYCLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E3A8A),
            letterSpacing = 0.5.sp
          )
        }

        val (statusText, statusBg, statusColor) = when (currentStage) {
          1 -> Triple("AWAITING STORAGE HANDOVER", Color(0xFFFEF3C7), Color(0xFFD97706))
          2 -> Triple("IN BCN STORAGE HUB", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
          3 -> Triple("OUT FOR BUYER DELIVERY", Color(0xFFEDE9FE), Color(0xFF6D28D9))
          else -> Triple("DELIVERED TO BUYER 🎉", Color(0xFFD1FAE5), Color(0xFF047857))
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = statusBg
        ) {
          Text(
            text = statusText,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4-Stage Horizontal Stepper
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val steps = listOf(
          "1. Closed",
          "2. Handover",
          "3. In Storage",
          "4. Delivered"
        )
        steps.forEachIndexed { index, stepLabel ->
          val stepNumber = index + 1
          val isCompleted = currentStage > stepNumber || (currentStage == 4 && stepNumber == 4)
          val isCurrent = currentStage == stepNumber && currentStage < 4

          val dotColor = when {
            isCompleted -> Color(0xFF10B981)
            isCurrent -> Color(0xFF2563EB)
            else -> Color(0xFFCBD5E1)
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(18.dp)
                .background(dotColor, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              if (isCompleted) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(11.dp)
                )
              } else {
                Text(
                  text = "$stepNumber",
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = stepLabel,
              fontSize = 8.5.sp,
              fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Medium,
              color = if (isCurrent) Color(0xFF1D4ED8) else if (isCompleted) Color(0xFF047857) else Color(0xFF94A3B8),
              textAlign = TextAlign.Center,
              maxLines = 1
            )
          }

          if (index < steps.size - 1) {
            Box(
              modifier = Modifier
                .weight(0.35f)
                .height(2.dp)
                .background(if (currentStage > stepNumber) Color(0xFF10B981) else Color(0xFFE2E8F0))
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Stage-specific details & Action Card
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          when (currentStage) {
            1 -> {
              Text(
                text = "Seller Action: Handover to BCN Storage Hub",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "Bid is closed. Seller must deposit the item at the designated BCN Storage Hub for inspection before buyer delivery.",
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                lineHeight = 14.sp
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "📍 Hub: ${item.bcnStorageHub}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB)
              )
              Text(
                text = "🔖 Storage Waybill: ${item.storageInwardCode}",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = { onHandoverToStorage?.invoke() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(36.dp)
                  .testTag("handover_storage_button_${item.id}")
              ) {
                Text("📦 Handover Product to BCN Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            2 -> {
              Text(
                text = "Product Verified in BCN Storage Custody",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0369A1)
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "✅ 100% Quality & Authenticity verified by BCN Vault Officer. Stored in slot ${item.storageShelfSlot}. Ready for buyer delivery dispatch.",
                fontSize = 10.sp,
                color = Color(0xFF475569),
                lineHeight = 14.sp
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "📦 Location: ${item.bcnStorageHub} • Slot: ${item.storageShelfSlot}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0369A1)
              )
              Text(
                text = "🏠 Buyer Address: ${item.buyerDeliveryAddress}",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = { onDispatchFromStorage?.invoke() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(36.dp)
                  .testTag("dispatch_storage_button_${item.id}")
              ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🚚 Dispatch from Storage to Buyer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            3 -> {
              Text(
                text = "Dispatched from Storage • Out for Buyer Delivery",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6D28D9)
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "Product is in transit from BCN Central Storage Hub to Buyer's address via BCN Express Logistics.",
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                lineHeight = 14.sp
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "🚚 Logistics: BCN Express Secure Transit",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6D28D9)
              )
              Text(
                text = "🔖 Tracking ID: ${item.dispatchTrackingId}",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
              )
              Text(
                text = "📍 Delivery To: ${item.buyerDeliveryAddress}",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = { onConfirmDelivery?.invoke() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(36.dp)
                  .testTag("confirm_delivery_button_${item.id}")
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("✅ Buyer: Confirm Delivery & Release Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            4 -> {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Logistic Cycle Completed • Delivered to Buyer 🎉",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF047857)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Product verified at BCN Storage, delivered to buyer, and escrow payout released to Seller ${item.sellerName}.",
                fontSize = 10.sp,
                color = Color(0xFF065F46),
                lineHeight = 14.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Digital Waybill: ${item.storageInwardCode} • Tracking: ${item.dispatchTrackingId}",
                fontSize = 9.sp,
                color = Color(0xFF059669),
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Dialog for placing a bid on an auction item with upfront 10-20% payment.
 */
@Composable
fun PlaceAuctionBidDialog(
  item: AuctionItemEntity,
  onDismiss: () -> Unit,
  onConfirmBid: (bidAmount: Double) -> Unit
) {
  val minNextBid = item.currentBid + 100.0
  var bidAmountStr by remember { mutableStateOf(minNextBid.toInt().toString()) }
  val bidAmount = bidAmountStr.toDoubleOrNull() ?: minNextBid
  val upfrontAmount = bidAmount * (item.upfrontPercent / 100.0)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("place_bid_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Place Auction Bid", fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Item Summary Card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFEFF6FF),
          border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUrl.isNotBlank()) {
              AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(54.dp)
                  .clip(RoundedCornerShape(8.dp))
              )
              Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
              Text("Current Bid: ₹${item.currentBid.toInt()}", fontSize = 12.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold)
              Text("Minimum next bid: ₹${minNextBid.toInt()}", fontSize = 11.sp, color = Color(0xFF64748B))
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Bid Amount (₹):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = bidAmountStr,
          onValueChange = { bidAmountStr = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("bid_amount_input"),
          shape = RoundedCornerShape(10.dp)
        )

        // Quick add buttons
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(100, 500, 1000, 2500).forEach { inc ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
              modifier = Modifier
                .weight(1f)
                .clickable {
                  val newBid = (bidAmount + inc).toInt()
                  bidAmountStr = newBid.toString()
                }
            ) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                Text("+₹$inc", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Upfront Escrow Deposit Breakdown
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0FDF4),
          border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Upfront Deposit (${item.upfrontPercent}%):", fontSize = 12.sp, color = Color(0xFF166534))
              Text("₹${upfrontAmount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF166534))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Balance if won: ₹${(bidAmount - upfrontAmount).toInt()}",
              fontSize = 11.sp,
              color = Color(0xFF15803D)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                "100% Automatic Refund if outbid by another user.",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF166534)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            if (bidAmount > item.currentBid) {
              onConfirmBid(bidAmount)
            }
          },
          enabled = bidAmount > item.currentBid,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("confirm_bid_button")
        ) {
          Text("PAY ₹${upfrontAmount.toInt()} UPFRONT & PLACE BID", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}

/**
 * Dialog for sellers to list a product in the Auction tab,
 * specifying days listed for auction and 10 to 20% upfront deposit.
 */
@Composable
fun CreateAuctionDialog(
  onDismiss: () -> Unit,
  onSubmit: (
    title: String,
    description: String,
    category: String,
    imageUrl: String,
    startingPrice: Double,
    upfrontPercent: Int,
    durationDays: Int
  ) -> Unit
) {
  var title by remember { mutableStateOf("Bosch Professional Hammer Drill 750W") }
  var description by remember { mutableStateOf("Heavy-duty impact drill with 13mm keyless chuck and full accessory kit. Pristine condition.") }
  var category by remember { mutableStateOf("TOOLS") }
  var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600") }
  var startingPriceStr by remember { mutableStateOf("2500") }
  var upfrontPercent by remember { mutableStateOf(15) }
  var durationDays by remember { mutableStateOf(3) }

  val startingPrice = startingPriceStr.toDoubleOrNull() ?: 2500.0
  val estimatedDeposit = startingPrice * (upfrontPercent / 100.0)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("create_auction_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("List Item for Auction", fontSize = 17.sp, fontWeight = FontWeight.Bold)
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Product Title:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("e.g. Sony DSLR Camera, Antique Watch...") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Product Photo (URL):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = imageUrl,
          onValueChange = { imageUrl = it },
          placeholder = { Text("https://example.com/item.jpg") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "Tools" to "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600",
            "Camera" to "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600",
            "Laptop" to "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600",
            "Watch" to "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600"
          ).forEach { (label, url) ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (imageUrl == url) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (imageUrl == url) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
              modifier = Modifier.clickable { imageUrl = url }
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Starting Bid Price (₹):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = startingPriceStr,
          onValueChange = { startingPriceStr = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Duration in Days
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Auction Duration:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Text("$durationDays Days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(1 to "1 Day", 2 to "2 Days", 3 to "3 Days", 5 to "5 Days", 7 to "7 Days").forEach { (d, label) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (durationDays == d) Color(0xFF2563EB) else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (durationDays == d) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
              modifier = Modifier
                .weight(1f)
                .clickable { durationDays = d }
            ) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 7.dp)) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (durationDays == d) Color.White else Color(0xFF334155)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Upfront Deposit Percentage (10% to 20%)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Upfront Payment Required (10-20%):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Text("$upfrontPercent%", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(10, 15, 20).forEach { pct ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (upfrontPercent == pct) Color(0xFF059669) else Color(0xFFF0FDF4),
              border = BorderStroke(1.dp, if (upfrontPercent == pct) Color(0xFF059669) else Color(0xFFBBF7D0)),
              modifier = Modifier
                .weight(1f)
                .clickable { upfrontPercent = pct }
            ) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                  text = "$pct% Upfront",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (upfrontPercent == pct) Color.White else Color(0xFF166534)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Escrow explanation card
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFEFF6FF),
          border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Escrow Guarantee", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Bidders pay $upfrontPercent% upfront (approx ₹${estimatedDeposit.toInt()}). Whoever wins pays remaining balance & receives product; all outbid bidders get 100% of their money back immediately.",
              fontSize = 10.sp,
              color = Color(0xFF1E3A8A)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Description:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          minLines = 2
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            if (title.isNotBlank() && startingPrice > 0) {
              onSubmit(title, description, category, imageUrl, startingPrice, upfrontPercent, durationDays)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("submit_auction_button")
        ) {
          Text("LAUNCH AUCTION LISTING", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Dialog allowing users to inspect the shared photo and product details,
 * then place a price offer / quotation.
 */
@Composable
fun PlaceProductQuotationDialog(
  product: MarketplaceProductEntity,
  onDismiss: () -> Unit,
  onSubmitQuotation: (offeredPrice: Double, message: String) -> Unit
) {
  var offerPriceStr by remember { mutableStateOf(product.price.toInt().toString()) }
  var message by remember { mutableStateOf("Interested in purchasing this. Can pick up today!") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("place_quotation_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Make Offer / Quotation", fontSize = 17.sp, fontWeight = FontWeight.Bold)
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Product Photo preview
        if (product.imageUrl.isNotBlank()) {
          AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxWidth()
              .height(150.dp)
              .clip(RoundedCornerShape(12.dp))
          )
          Spacer(modifier = Modifier.height(10.dp))
        }

        Text(product.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("Listed Price: ₹${product.price.toInt()} • by ${product.sellerName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        BcnCardHyperlink(
          bcnId = product.sellerBusyCardNetId,
          color = MaterialTheme.colorScheme.primary,
          fontSize = 11.sp,
          modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Your Proposed Price (₹):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = offerPriceStr,
          onValueChange = { offerPriceStr = it },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("quotation_price_input"),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Note / Message for Seller:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = message,
          onValueChange = { message = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          minLines = 2
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            val price = offerPriceStr.toDoubleOrNull() ?: product.price
            onSubmitQuotation(price, message)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("submit_quotation_button")
        ) {
          Text("SUBMIT QUOTATION TO SELLER", fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }
  }
}

/**
 * Top In-App Notification Banner displayed immediately when user is outbid,
 * detailing the competitor, new bid, and escrow deposit refund guarantee.
 */
@Composable
fun OutbidNotificationBanner(
  alert: AuctionNotificationEntity,
  onDismiss: () -> Unit,
  onRebid: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFFFEF2F2),
    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
    shadowElevation = 4.dp,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("outbid_alert_banner")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "OUTBID ALERT!",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = Color(0xFFDC2626)
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF991B1B), modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = alert.message,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF7F1D1D)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(onClick = onDismiss) {
          Text("Dismiss", fontSize = 11.sp, color = Color(0xFF991B1B))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Button(
          onClick = onRebid,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier.height(32.dp)
        ) {
          Text("Re-Bid Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }
  }
}
