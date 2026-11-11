package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DigitalCardEntity
import com.example.data.model.ProximityAlertNotification
import com.example.data.model.ProximityVicinityTier
import com.example.util.StylizedBarcode

/**
 * Animated Heads-Up Banner shown when a new service request is posted
 * within the user's active vicinity tier (500m, 1000m, 2000m, 5000m, 10000m, +10000m).
 */
@Composable
fun ProximityAlertHeadsUpBanner(
  alert: ProximityAlertNotification?,
  onDismiss: () -> Unit,
  onOpenChat: (requestId: String) -> Unit,
  onOpenMarketplace: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = alert != null,
    enter = slideInVertically() + fadeIn(),
    exit = slideOutVertically() + fadeOut(),
    modifier = modifier
  ) {
    if (alert != null) {
      val distanceColor = when {
        alert.distanceMeters <= 500 -> Color(0xFFDC2626) // Immediate Red/Amber
        alert.distanceMeters <= 1000 -> Color(0xFF059669) // Green
        alert.distanceMeters <= 2000 -> Color(0xFF0284C7) // Sky Blue
        alert.distanceMeters <= 5000 -> Color(0xFF4F46E5) // Indigo
        alert.distanceMeters <= 10000 -> Color(0xFF7C3AED) // Purple
        else -> Color(0xFF475569) // Slate Regional
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(16.dp))
          .testTag("proximity_alert_heads_up_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, distanceColor.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  distanceColor.copy(alpha = 0.08f),
                  MaterialTheme.colorScheme.surface
                )
              )
            )
            .padding(14.dp)
        ) {
          // Header: Alert badge + Distance Pill + Dismiss Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .background(distanceColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Radar,
                  contentDescription = null,
                  tint = distanceColor,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "NEW SERVICE REQUEST IN VICINITY",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.6.sp,
                  color = distanceColor
                )
                Text(
                  text = "Posted just now in ${alert.locality}",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = distanceColor,
              modifier = Modifier.testTag("alert_vicinity_badge")
            ) {
              Text(
                text = alert.distanceBadgeLabel,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(24.dp).testTag("dismiss_alert_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss Alert",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Request Title & Category
          Text(
            text = alert.title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(3.dp))

          Text(
            text = alert.problemDescription,
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Meta Info Row: Requester, Budget, Urgency
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "By ${alert.requesterName} (",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              BcnCardHyperlink(
                bcnId = alert.requesterBcnId,
                fontSize = 10.sp
              )
              Text(
                text = ")",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF10B981).copy(alpha = 0.15f)
            ) {
              Text(
                text = alert.budget,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF047857),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Action Buttons: Chat & Connect or View in Marketplace
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                onOpenChat(alert.requestId)
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = distanceColor),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1.2f)
                .height(34.dp)
                .testTag("alert_respond_button")
            ) {
              Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Respond & Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
              onClick = {
                onOpenMarketplace()
                onDismiss()
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .height(34.dp)
                .testTag("alert_view_details_button")
            ) {
              Text("View Request", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}

/**
 * Proximity Vicinity Radar Notification Dialog:
 * Lets user select their notification radius (500m, 1000m, 2000m, 5000m, 10000m, +10000m),
 * toggle alert sounds/push notifications, and test/simulate incoming service requests.
 */
@Composable
fun ProximityAlertSettingsDialog(
  currentVicinityMeters: Int,
  alertsEnabled: Boolean,
  alertsHistory: List<ProximityAlertNotification>,
  onUpdateVicinity: (Int) -> Unit,
  onToggleAlerts: (Boolean) -> Unit,
  onSimulateAlert: (vicinityMeters: Int) -> Unit,
  onOpenChat: (requestId: String) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.fillMaxWidth(0.95f).testTag("proximity_alert_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CellTower,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Proximity Radar Alerts",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Service requests in your vicinity",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. Master Toggle: Enable / Disable Vicinity Alerts
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Vicinity Push Notifications",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (alertsEnabled) "Active: Alerting on new requests" else "Muted: No alert pings",
                  fontSize = 10.sp,
                  color = if (alertsEnabled) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Switch(
                checked = alertsEnabled,
                onCheckedChange = { onToggleAlerts(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = Color(0xFF0284C7)
                ),
                modifier = Modifier.testTag("toggle_proximity_alerts_switch")
              )
            }
          }
        }

        // 2. Vicinity Tier Selector: 500m, 1000m, 2000m, 5000m, 10000m, +10000m
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "ALERT VICINITY RADIUS",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = ProximityVicinityTier.fromRadius(currentVicinityMeters).label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7)
              )
            }

            Text(
              text = "Choose how close a newly posted service request must be to trigger a real-time push alert on your phone:",
              fontSize = 10.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 14.sp
            )

            // Vicinity Tier Cards Grid
            val tiers = listOf(
              ProximityVicinityTier.TIER_500M,
              ProximityVicinityTier.TIER_1000M,
              ProximityVicinityTier.TIER_2000M,
              ProximityVicinityTier.TIER_5000M,
              ProximityVicinityTier.TIER_10000M,
              ProximityVicinityTier.TIER_PLUS_10000M
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              tiers.chunked(2).forEach { rowTiers ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  rowTiers.forEach { tier ->
                    val isSelected = currentVicinityMeters == tier.radiusMeters
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                      border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                      ),
                      modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateVicinity(tier.radiusMeters) }
                        .testTag("vicinity_tier_${tier.shortLabel}")
                    ) {
                      Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween,
                          modifier = Modifier.fillMaxWidth()
                        ) {
                          Text(
                            text = tier.shortLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface
                          )
                          if (isSelected) {
                            Icon(
                              imageVector = Icons.Default.CheckCircle,
                              contentDescription = null,
                              tint = Color(0xFF0284C7),
                              modifier = Modifier.size(13.dp)
                            )
                          }
                        }
                        Text(
                          text = tier.label.replace(tier.shortLabel, "").trim().removePrefix("(").removeSuffix(")"),
                          fontSize = 9.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          maxLines = 1
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // 3. Test & Simulator Section (Test all 6 Vicinity Tiers)
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.FlashOn,
                  contentDescription = null,
                  tint = Color(0xFF2563EB),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "TEST VICINITY ALERT SIMULATOR",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.5.sp,
                  color = Color(0xFF1E40AF)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tap a distance below to simulate a real inbound service request in that radius tier:",
                fontSize = 10.sp,
                color = Color(0xFF1E3A8A)
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                listOf(500 to "500m", 1000 to "1000m", 2000 to "2000m").forEach { (rad, lbl) ->
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF2563EB),
                    modifier = Modifier
                      .weight(1f)
                      .clickable {
                        onSimulateAlert(rad)
                        Toast.makeText(context, "📡 Alert fired: New request within $lbl!", Toast.LENGTH_SHORT).show()
                      }
                      .testTag("test_alert_$lbl")
                  ) {
                    Text(
                      text = "⚡ $lbl",
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.padding(vertical = 5.dp),
                      textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                listOf(5000 to "5000m", 10000 to "10000m", 999999 to "+10000m").forEach { (rad, lbl) ->
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1D4ED8),
                    modifier = Modifier
                      .weight(1f)
                      .clickable {
                        onSimulateAlert(rad)
                        Toast.makeText(context, "📡 Alert fired: New request within $lbl!", Toast.LENGTH_SHORT).show()
                      }
                      .testTag("test_alert_$lbl")
                  ) {
                    Text(
                      text = "⚡ $lbl",
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.padding(vertical = 5.dp),
                      textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                  }
                }
              }
            }
          }
        }

        // 4. Recent Proximity Alerts Feed
        item {
          Text(
            text = "RECENT VICINITY ALERTS (${alertsHistory.size})",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (alertsHistory.isEmpty()) {
          item {
            Text(
              text = "No recent proximity alerts. New requests in your vicinity will show here.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          items(alertsHistory) { alert ->
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth().testTag("alert_history_item_${alert.id}")
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = alert.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                  )
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = alert.distanceBadgeLabel,
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF0284C7),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "By ${alert.requesterName} • ${alert.locality} • ${alert.budget}",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                      onOpenChat(alert.requestId)
                      onDismiss()
                    }
                  ) {
                    Text(
                      text = "Respond / Chat →",
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Done", fontSize = 11.sp)
      }
    }
  )
}

/**
 * Single User Barcode Card for the Home Page:
 * Displays the authenticated user's personal BCN digital identity barcode.
 * ("Home page should have only 1 scanner and 1 user barcode.")
 */
@Composable
fun UserPersonalBarcodeCard(
  primaryCard: DigitalCardEntity?,
  onShareClick: () -> Unit,
  onNavigateToMyCard: () -> Unit,
  onOpenQrGenerator: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val bcnId = primaryCard?.busyCardNetId ?: "IN-MH-MU-26-581ELEC347"
  val ownerName = primaryCard?.name ?: "Rohan Sharma"
  val businessName = primaryCard?.businessName ?: "Sharma Electrical & Automation"

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(18.dp))
      .testTag("home_user_barcode_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFFF8FAFC),
              Color(0xFFFFFFFF)
            )
          )
        )
        .padding(14.dp)
    ) {
      // Header: Identity Tag + BCN Alphanumeric Code + Copy action
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF0284C7)
          ) {
            Text(
              text = "MY USER BARCODE",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "DIGITAL IDENTITY",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
          )
        }

        // Copy ID Chip
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFFF1F5F9),
          border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
          modifier = Modifier
            .clickable {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("BusyCardNet ID", bcnId))
              Toast.makeText(context, "Copied BCN ID: $bcnId", Toast.LENGTH_SHORT).show()
            }
            .testTag("home_copy_user_barcode_id")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy ID",
              tint = Color(0xFF0284C7),
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "COPY",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0284C7)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // The User's 1D Linear Barcode
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color.White)
          .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
          .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          StylizedBarcode(
            data = bcnId,
            modifier = Modifier
              .fillMaxWidth(0.92f)
              .height(58.dp)
              .testTag("home_user_barcode_view"),
            darkColor = Color(0xFF0B1120),
            lightColor = Color.White,
            showText = true
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // User details row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = businessName,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = Color(0xFF0284C7),
              modifier = Modifier.size(13.dp)
            )
          }
          Text(
            text = "Cardholder: $ownerName • Scan for instant contact exchange",
            fontSize = 9.5.sp,
            color = Color(0xFF64748B)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (onOpenQrGenerator != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF0F172A),
              modifier = Modifier
                .clickable { onOpenQrGenerator() }
                .testTag("home_user_barcode_qr_studio_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
              ) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("QR Studio", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0284C7).copy(alpha = 0.12f),
            modifier = Modifier
              .clickable { onShareClick() }
              .testTag("home_user_barcode_share_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text("Share", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2563EB),
            modifier = Modifier
              .clickable { onNavigateToMyCard() }
              .testTag("home_user_barcode_view_card_button")
          ) {
            Text(
              text = "My Card",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
          }
        }
      }
    }
  }
}
