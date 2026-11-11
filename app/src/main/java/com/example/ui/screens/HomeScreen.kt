package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import com.example.ui.theme.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.FeedPostEntity
import com.example.data.model.ProximityAlertNotification
import com.example.data.model.ProximityVicinityTier
import com.example.ui.components.BcnCardHyperlink
import com.example.ui.components.LocalBcnCardOpener
import com.example.ui.components.ProfessionalNotificationBanner
import com.example.ui.components.ProximityAlertHeadsUpBanner
import com.example.ui.components.ProximityAlertSettingsDialog
import com.example.ui.components.UrgentServiceRequestBanner
import com.example.ui.components.UserPersonalBarcodeCard
import com.example.ui.components.kyc.KycVerificationDialog
import com.example.ui.components.credits.MarketingCreditsHubDialog
import com.example.ui.components.marketing.ShopNotificationAnnouncementDialog
import com.example.ui.components.wallet.SocietyMaintenanceDialog
import com.example.data.model.KycVerificationStatus
import com.example.ui.viewmodel.BusyCardNetViewModel
import com.example.ui.viewmodel.FeedFilter

@Composable
fun HomeScreen(
  viewModel: BusyCardNetViewModel,
  onNavigateToDiscover: (categoryCode: String?) -> Unit,
  onNavigateToMyCard: () -> Unit,
  onOpenServiceRequestDialog: (professionCode: String) -> Unit,
  onOpenCreateAdDialog: () -> Unit,
  onOpenChat: (requestId: String) -> Unit,
  onOpenScanner: () -> Unit = {},
  onOpenQrGenerator: () -> Unit = {},
  onOpenLoginScreen: () -> Unit = {},
  onNavigateToSettings: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val posts by viewModel.allPosts.collectAsStateWithLifecycle()
  val professions by viewModel.allProfessions.collectAsStateWithLifecycle()
  val serviceRequests by viewModel.allServiceRequests.collectAsStateWithLifecycle()
  val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
  val selectedRadius by viewModel.selectedRadiusMeters.collectAsStateWithLifecycle()
  val currentFilter by viewModel.feedFilter.collectAsStateWithLifecycle()
  val primaryCard by viewModel.primaryCard.collectAsStateWithLifecycle()

  // Proximity Alert Vicinity state (500m, 1000m, 2000m, 5000m, 10000m, +10000m)
  val activeProximityAlert by viewModel.activeProximityAlert.collectAsStateWithLifecycle()
  val proximityVicinity by viewModel.proximityAlertVicinityMeters.collectAsStateWithLifecycle()
  val proximityAlertsEnabled by viewModel.proximityAlertsEnabled.collectAsStateWithLifecycle()
  val proximityAlertsHistory by viewModel.proximityAlertsList.collectAsStateWithLifecycle()
  val unreadAlertsCount by viewModel.unreadProximityAlertsCount.collectAsStateWithLifecycle()

  var showLocationDropdown by remember { mutableStateOf(false) }
  var showRadiusDropdown by remember { mutableStateOf(false) }
  var showProximitySettingsDialog by remember { mutableStateOf(false) }
  var showKycDialog by remember { mutableStateOf(false) }
  var showCreditsHubDialog by remember { mutableStateOf(false) }
  var showShopNotificationDialog by remember { mutableStateOf(false) }
  var showSocietyMaintenanceDialog by remember { mutableStateOf(false) }

  val hasActiveShopSubscription by viewModel.hasActiveShopSubscription.collectAsStateWithLifecycle()
  val societyPayments by viewModel.societyMaintenancePayments.collectAsStateWithLifecycle()

  val kycStatus by viewModel.kycStatus.collectAsStateWithLifecycle()
  val kycData by viewModel.kycData.collectAsStateWithLifecycle()
  val creditBalance by viewModel.creditBalance.collectAsStateWithLifecycle()
  val totalEarnedCredits by viewModel.totalEarnedCredits.collectAsStateWithLifecycle()
  val totalSpentCredits by viewModel.totalSpentCredits.collectAsStateWithLifecycle()
  val shareCount by viewModel.shareCount.collectAsStateWithLifecycle()
  val creditTransactions by viewModel.creditTransactions.collectAsStateWithLifecycle()

  // Check if there is an incoming urgent request nearby (Prompt Section 26)
  val incomingRequest = serviceRequests.find { it.status == "SENT" || it.status == "INTERESTED" }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen_feed"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {

    // 0. Real-time Vicinity Proximity Alert Banner (Heads-up notification for 500m - +10000m)
    item {
      ProximityAlertHeadsUpBanner(
        alert = activeProximityAlert,
        onDismiss = { viewModel.dismissActiveProximityAlert() },
        onOpenChat = onOpenChat,
        onOpenMarketplace = { onNavigateToDiscover(null) }
      )
    }

    // 1. Top Bar: BusyCardNet Logo + Location / Radius Selector
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "BUSYCARDNET",
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Your Digital Identity • Local Network",
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Location & Radius Chips
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.clickable { showLocationDropdown = true }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = selectedLocation.split(",").firstOrNull() ?: selectedLocation,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }

              DropdownMenu(
                expanded = showLocationDropdown,
                onDismissRequest = { showLocationDropdown = false }
              ) {
                listOf("Kurla West, Mumbai", "Kurla East, Mumbai", "Bandra West, Mumbai", "Andheri East, Mumbai", "Dadar, Mumbai", "Pune Central, MH").forEach { loc ->
                  DropdownMenuItem(
                    text = { Text(loc) },
                    onClick = {
                      viewModel.setLocation(loc)
                      showLocationDropdown = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Radius Selector
            Box {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { showRadiusDropdown = true }
              ) {
                Text(
                  text = if (selectedRadius < 1000) "${selectedRadius}m" else "${selectedRadius / 1000}km",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
              }

              DropdownMenu(
                expanded = showRadiusDropdown,
                onDismissRequest = { showRadiusDropdown = false }
              ) {
                listOf(500 to "500m (Immediate)", 1000 to "1 km", 3000 to "3 km", 5000 to "5 km", 10000 to "10 km").forEach { (rad, lbl) ->
                  DropdownMenuItem(
                    text = { Text(lbl) },
                    onClick = {
                      viewModel.setRadius(rad)
                      showRadiusDropdown = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Direct 500m Google Map Button
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFF0284C7),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
              modifier = Modifier
                .clickable { onNavigateToDiscover("MAP") }
                .testTag("home_top_map_500m_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = "500m Map",
                  tint = Color.White,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "500m Map",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Proximity Vicinity Radar Alert Notification Button
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (proximityAlertsEnabled) Color(0xFF0284C7).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (proximityAlertsEnabled) Color(0xFF0284C7).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
              ),
              modifier = Modifier
                .clickable { showProximitySettingsDialog = true }
                .testTag("home_vicinity_radar_alert_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Radar,
                  contentDescription = "Proximity Vicinity Alerts",
                  tint = if (proximityAlertsEnabled) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val tier = ProximityVicinityTier.fromRadius(proximityVicinity)
                Text(
                  text = tier.shortLabel,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Black,
                  color = if (proximityAlertsEnabled) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (unreadAlertsCount > 0) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .size(15.dp)
                      .background(Color(0xFFDC2626), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "$unreadAlertsCount",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Firebase Auth & Credential Manager Session Button
            val activeUserSession by viewModel.userSession.collectAsStateWithLifecycle()
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (activeUserSession != null) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (activeUserSession != null) Color(0xFF10B981).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
              ),
              modifier = Modifier
                .clickable { onOpenLoginScreen() }
                .testTag("home_auth_session_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              ) {
                Icon(
                  imageVector = if (activeUserSession != null) Icons.Default.Shield else Icons.Default.Lock,
                  contentDescription = "Authentication & Session",
                  tint = if (activeUserSession != null) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (activeUserSession != null) "Session" else "Login",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (activeUserSession != null) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Settings & Marketing Hub Top Action Button
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
              ),
              modifier = Modifier
                .clickable { onNavigateToSettings() }
                .testTag("home_top_settings_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = "Settings & Marketing",
                  tint = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Settings",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }

    // 2. Global Search Affordance + Single QR Scanner on Home Page
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("home_search_bar")
      ) {
        Row(
          modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToDiscover(null) },
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Search people, cards (e.g. IN-MH-MU...)",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Single CameraX QR Scanner on Home Page (Prompt: "Home page should have only 1 scanner")
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            modifier = Modifier
              .clickable { onOpenScanner() }
              .testTag("home_search_qr_scanner_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan Card QR",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "SCAN QR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }

    // 2B. Single User Barcode on Home Page (Prompt: "Home page should have only 1 scanner and 1 user barcode.")
    item {
      UserPersonalBarcodeCard(
        primaryCard = primaryCard,
        onShareClick = {
          val bcn = primaryCard?.busyCardNetId ?: "IN-MH-MU-26-581ELEC347"
          val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Connect with me on BusyCardNet")
            putExtra(
              Intent.EXTRA_TEXT,
              "Scan my BusyCardNet Barcode or visit: https://busycardnet.app/card/$bcn"
            )
          }
          context.startActivity(Intent.createChooser(shareIntent, "Share BCN Barcode"))
        },
        onNavigateToMyCard = onNavigateToMyCard,
        onOpenQrGenerator = onOpenQrGenerator
      )
    }

    // 2C. Anti-Fraud Identity Verification (KYC) & Marketing Credits Quick Pill Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val kycColor = when (kycStatus) {
          KycVerificationStatus.APPROVED -> Color(0xFF10B981)
          KycVerificationStatus.PENDING_REVIEW -> Color(0xFFF59E0B)
          KycVerificationStatus.REJECTED -> Color(0xFFEF4444)
          KycVerificationStatus.UNVERIFIED -> Color(0xFF38BDF8)
        }

        // KYC Verification Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0F172A),
          border = androidx.compose.foundation.BorderStroke(1.dp, kycColor.copy(alpha = 0.5f)),
          modifier = Modifier
            .weight(1f)
            .clickable { showKycDialog = true }
            .testTag("home_kyc_pill_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (kycStatus == KycVerificationStatus.APPROVED) Icons.Default.Verified else Icons.Default.Shield,
              contentDescription = null,
              tint = kycColor,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "ID & KYC STATUS",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
              )
              Text(
                text = kycStatus.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = kycColor
              )
            }
          }
        }

        // Marketing Credits Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF131D31),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
          modifier = Modifier
            .weight(1f)
            .clickable { showCreditsHubDialog = true }
            .testTag("home_credits_pill_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = null,
              tint = Color(0xFFFBBF24),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "MARKETING CREDITS",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
              )
              Text(
                text = "$creditBalance Credits",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFBBF24)
              )
            }
          }
        }
      }
    }

    // 2B. Online Shopkeeper Notification & AI Motive Banner Broadcaster Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(18.dp))
          .testTag("shopkeeper_notification_broadcast_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(Color(0xFF2563EB), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Shopkeeper Notification Hub",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0284C7)) {
                    Text(
                      text = "DCN ONLINE",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Black,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = "Notify people with AI news, announcements & posters",
                  fontSize = 11.sp,
                  color = Color(0xFF93C5FD)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF10B981)
            ) {
              Text(
                text = "AI POWERED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Whatever you want to notify people nearby: simply create news, announcements, and synthesize an image via AI to advertize your motive. Pay with account credits, subscription, or direct pay!",
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showShopNotificationDialog = true },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
              modifier = Modifier
                .weight(1.3f)
                .height(40.dp)
                .testTag("open_shop_notification_button")
            ) {
              Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Create AI News / Motive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { showCreditsHubDialog = true },
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
              modifier = Modifier
                .weight(0.9f)
                .height(40.dp)
                .testTag("shop_wallet_balance_btn")
            ) {
              Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("$creditBalance Cr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
            }
          }
        }
      }
    }

    // 2B. Society Maintenance Payment Hub (CHS / RWA / Commercial Premises Bills)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(18.dp))
          .testTag("home_society_maintenance_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF34D399).copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF10B981).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Apartment,
                  contentDescription = "Society Maintenance",
                  tint = Color(0xFF6EE7B7),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "SOCIETY MAINTENANCE PAY",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.6.sp,
                  color = Color.White
                )
                Text(
                  text = "Housing CHS • RWA • Commercial Premises",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFFA7F3D0)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF10B981)
            ) {
              Text(
                text = if (societyPayments.isEmpty()) "+15 CR CASHBACK" else "${societyPayments.size} RECEIPTS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Pay monthly, quarterly, or annual society bills (Sinking Fund, Water, Security, Parking) using DCN Wallet Credits (1 Cr = ₹1) or Direct UPI / Card with instant digital receipt.",
            fontSize = 11.sp,
            color = Color(0xFFD1FAE5),
            lineHeight = 16.sp
          )

          if (societyPayments.isNotEmpty()) {
            val latestReceipt = societyPayments.first()
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.Black.copy(alpha = 0.25f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Latest Receipt",
                    tint = Color(0xFF6EE7B7),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Last Paid: ${latestReceipt.unitOrFlatNumber} • ₹${latestReceipt.amountRupees}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Text(
                  text = latestReceipt.receiptId,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  color = Color(0xFF6EE7B7)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { showSocietyMaintenanceDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("open_home_society_maintenance_button")
          ) {
            Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay Society Maintenance / View Receipts", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
          }
        }
      }
    }

    // 3. Welcome Hero Banner (Prompt Section 70: FINAL HOME SCREEN MESSAGE)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(18.dp))
          .testTag("welcome_hero_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color(0xFFEFF6FF), Color(0xFFFFFFFF))
              )
            )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "WELCOME TO BUSYCARDNET",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = FontNavyHeadings
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "• Create your digital identity\n• Find people, professionals, and services near you\n• Advertise your business where you want\n• Sell anything you want to your local community",
              fontSize = 11.sp,
              color = FontRoyalBlue,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = onNavigateToMyCard,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier
                  .weight(1f)
                  .height(38.dp)
                  .testTag("hero_create_card_button")
              ) {
                Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CREATE MY CARD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              Button(
                onClick = { onNavigateToDiscover(null) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier
                  .weight(1f)
                  .height(38.dp)
                  .testTag("hero_explore_button")
              ) {
                Icon(imageVector = Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("EXPLORE NEARBY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }
    }

    // 3B. Google Maps: 500m Local Discovery Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(18.dp))
          .testTag("home_google_maps_500m_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF2563EB)
              ) {
                Text(
                  text = "GOOGLE MAPS",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "NEARBY BUSINESSES",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FontNavyHeadings
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFECFDF5),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34D399))
            ) {
              Text(
                text = "📍 500m - 10km GPS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF059669),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Google Maps SDK & Location services: Filter verified local businesses and service providers across 500m, 1000m, 2000m, 5000m, and 10000m radii.",
            fontSize = 11.sp,
            color = FontRoyalBlue,
            lineHeight = 15.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Quick distance preview chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf("500m Radius", "1000m (1km)", "2000m (2km)", "5000m (5km)", "10000m (10km)").forEach { chip ->
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE0E7FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE))
              ) {
                Text(
                  text = chip,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1E3A8A),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { onNavigateToDiscover("MAP") },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            modifier = Modifier
              .fillMaxWidth()
              .height(38.dp)
              .testTag("open_google_maps_500m_button")
          ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("OPEN NEARBY BUSINESSES MAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }

    // 4. "WHAT ARE YOU LOOKING FOR?" Category Grid (Prompt Section 23)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "WHAT ARE YOU LOOKING FOR?",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Text(
            text = "View All →",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onNavigateToDiscover(null) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Horizontal Scrollable Row
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(end = 16.dp)
        ) {
          items(professions.take(10)) { prof ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
              modifier = Modifier
                .width(82.dp)
                .clickable { onNavigateToDiscover(prof.code) }
                .testTag("category_chip_${prof.code}")
            ) {
              Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(text = prof.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = prof.title.split("/").first().trim(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }

    // 5. 500-Meter Instant Service Request Banner (Prompt Section 25)
    item {
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        UrgentServiceRequestBanner(
          onRequestClick = { onOpenServiceRequestDialog("ELEC") }
        )
      }
    }

    // 6. Professional Notification Banner (If active request exists)
    if (incomingRequest != null) {
      item {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          ProfessionalNotificationBanner(
            request = incomingRequest,
            onInterestedClick = {
              viewModel.respondToRequest(incomingRequest.requestId)
              onOpenChat(incomingRequest.requestId)
            },
            onDeclineClick = {
              viewModel.updateRequestStatus(incomingRequest.requestId, "CANCELLED")
            },
            onViewDetailsClick = { onOpenChat(incomingRequest.requestId) }
          )
        }
      }
    }

    // 7. Feed Filter Chips (Prompt Section 17: Combined Home Feed)
    item {
      Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(FeedFilter.values()) { filter ->
            val isSelected = currentFilter == filter
            val label = when (filter) {
              FeedFilter.ALL -> "All"
              FeedFilter.NEARBY -> "📍 Nearby"
              FeedFilter.BUSINESSES -> "🏢 Businesses"
              FeedFilter.SERVICES -> "⚡ Services"
              FeedFilter.PRODUCTS -> "🛍️ Products"
              FeedFilter.VIDEO_ADS -> "▶️ Video Ads"
              FeedFilter.ADVERTISEMENTS -> "📢 Sponsored"
            }
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) Color(0xFF0F172A) else Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
              modifier = Modifier.clickable { viewModel.setFeedFilter(filter) }
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }

    // 8. Feed Posts List
    items(posts, key = { it.id }) { post ->
      FeedPostCard(
        post = post,
        onLikeClick = { viewModel.likePost(post.id) },
        onCallClick = { phone ->
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
          context.startActivity(intent)
        },
        onWhatsAppClick = { phone ->
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone"))
          try {
            context.startActivity(intent)
          } catch (e: Exception) {
            // fallback
          }
        },
        onViewCardClick = { onNavigateToDiscover(null) },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }
  }

  // Vicinity Radar Alert Notification Settings & Simulator Dialog
  if (showProximitySettingsDialog) {
    ProximityAlertSettingsDialog(
      currentVicinityMeters = proximityVicinity,
      alertsEnabled = proximityAlertsEnabled,
      alertsHistory = proximityAlertsHistory,
      onUpdateVicinity = { viewModel.setProximityAlertVicinity(it) },
      onToggleAlerts = { viewModel.setProximityAlertsEnabled(it) },
      onSimulateAlert = { viewModel.simulateProximityAlert(it) },
      onOpenChat = onOpenChat,
      onDismiss = { showProximitySettingsDialog = false }
    )
  }

  // Multi-Step Anti-Fraud KYC Verification Dialog (CameraX + Compose)
  if (showKycDialog) {
    KycVerificationDialog(
      currentStatus = kycStatus,
      existingData = kycData,
      onDismiss = { showKycDialog = false },
      onSubmitKyc = { submission ->
        viewModel.submitKycVerification(submission)
      },
      onSimulateApprove = {
        viewModel.simulateApproveKycVerification()
      }
    )
  }

  // Marketing & Growth Credits Hub Dialog (DataStore)
  if (showCreditsHubDialog) {
    MarketingCreditsHubDialog(
      creditBalance = creditBalance,
      totalEarned = totalEarnedCredits,
      totalSpent = totalSpentCredits,
      shareCount = shareCount,
      transactions = creditTransactions,
      onShareApp = {
        viewModel.rewardCreditForShare("App Share")
      },
      onSpendCredits = { pkg ->
        viewModel.spendCreditsForMarketing(pkg)
      },
      onDismiss = { showCreditsHubDialog = false }
    )
  }

  // Shopkeeper Online Notification & AI Motive Ad Dialog
  if (showShopNotificationDialog) {
    ShopNotificationAnnouncementDialog(
      shopName = primaryCard?.businessName ?: primaryCard?.name ?: "Local Shop & Services",
      dcnId = primaryCard?.busyCardNetId ?: "IN-MH-MU-26-374BIZ691",
      categoryOrProfession = primaryCard?.designation ?: "Shopkeeper & Retail",
      initialLocation = selectedLocation,
      initialPhone = primaryCard?.phone ?: "+91 98765 43210",
      walletCreditBalance = creditBalance,
      hasActiveSubscription = hasActiveShopSubscription,
      onOpenTopUpWallet = {
        showShopNotificationDialog = false
        showCreditsHubDialog = true
      },
      onOpenSubscription = {
        viewModel.activateShopSubscription {
          Toast.makeText(context, "🎉 DCN Shop Subscription activated! Unlimited announcements active.", Toast.LENGTH_LONG).show()
        }
      },
      onDismiss = { showShopNotificationDialog = false },
      onPublishNotification = { title, body, bannerBitmap, radiusMeters, durationDays, ctaType, ctaPhone, paidWithCredits, paidWithSub, cost ->
        viewModel.publishShopNotification(
          title = title,
          body = body,
          bannerBitmap = bannerBitmap,
          radiusMeters = radiusMeters,
          durationDays = durationDays,
          ctaType = ctaType,
          ctaPhone = ctaPhone,
          paidWithCredits = paidWithCredits,
          paidWithSubscription = paidWithSub,
          cost = cost
        ) { success ->
          if (success) {
            showShopNotificationDialog = false
            Toast.makeText(
              context,
              "📢 Shop announcement broadcasted across ${radiusMeters / 1000}km! AI banner live on Home feed.",
              Toast.LENGTH_LONG
            ).show()
          } else {
            Toast.makeText(context, "Insufficient wallet credits!", Toast.LENGTH_SHORT).show()
          }
        }
      }
    )
  }

  // Society Maintenance Bill Payment & Receipt Hub Dialog
  if (showSocietyMaintenanceDialog) {
    SocietyMaintenanceDialog(
      memberName = primaryCard?.name ?: "Cardholder Member",
      memberBcnId = primaryCard?.busyCardNetId ?: "IN-MH-MU-26-374BIZ691",
      defaultLocality = selectedLocation,
      walletCreditBalance = creditBalance,
      paymentHistory = societyPayments,
      onOpenTopUpWallet = {
        showSocietyMaintenanceDialog = false
        showCreditsHubDialog = true
      },
      onOpenQrScanner = null, // Home screen preserves single QR scanner constraint
      onPayMaintenance = { societyName, regNo, flatNo, member, bcnId, period, amount, useWallet, directMethod, summary, onReceiptGenerated ->
        viewModel.paySocietyMaintenance(
          societyName = societyName,
          registrationNumber = regNo,
          unitOrFlatNumber = flatNo,
          memberName = member,
          memberBcnId = bcnId,
          billingPeriod = period,
          amountRupees = amount,
          payWithWalletCredits = useWallet,
          directPaymentMethod = directMethod,
          breakdownSummary = summary
        ) { receipt ->
          onReceiptGenerated(receipt)
          if (receipt != null) {
            Toast.makeText(
              context,
              "✅ Paid ₹$amount to $societyName! Receipt ${receipt.receiptId} generated.",
              Toast.LENGTH_LONG
            ).show()
          } else {
            Toast.makeText(context, "Insufficient wallet credits for ₹$amount bill!", Toast.LENGTH_SHORT).show()
          }
        }
      },
      onDismiss = { showSocietyMaintenanceDialog = false }
    )
  }
}

/**
 * Feed Item Component with video badge, advertising radius tag, CTA buttons
 */
@Composable
fun FeedPostCard(
  post: FeedPostEntity,
  onLikeClick: () -> Unit,
  onCallClick: (String) -> Unit,
  onWhatsAppClick: (String) -> Unit,
  onViewCardClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isAd = post.postScope == "ADVERTISEMENT" || post.mediaType == "AD"
  val isVideo = post.mediaType == "VIDEO"

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(
        width = if (isAd) 1.5.dp else 1.dp,
        color = if (isAd) Color(0xFFF59E0B).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp)
      )
      .testTag("feed_card_${post.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {

      // Header: Author & BusyCardNet ID & Target Radius
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(if (isAd) Color(0xFFF59E0B).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = post.authorName.take(1),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = if (isAd) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = post.authorName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
              )
            }

            BcnCardHyperlink(
              bcnId = post.authorBusyCardNetId,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 10.sp,
              modifier = Modifier.padding(top = 1.dp)
            )
          }
        }

        // Radius / Sponsored Badge
        Column(horizontalAlignment = Alignment.End) {
          if (isAd) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFF59E0B).copy(alpha = 0.2f)
            ) {
              Text(
                text = "SPONSORED • ${post.targetRadiusMeters / 1000}km",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF59E0B),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          } else {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = "📍 ${post.locationName}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Content Text
      Text(
        text = post.content,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 18.sp
      )

      // Video Placeholder Player Simulation
      if (isVideo) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F7FF))
            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.PlayCircle,
              contentDescription = "Play Video Ad",
              tint = Color(0xFF2563EB),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "BusyCardNet Video Ad • 20 sec",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = FontRoyalBlue
            )
          }
        }
      } else if (post.mediaUrl.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        AsyncImage(
          model = post.mediaUrl,
          contentDescription = "Notification Announcement Banner",
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
        )
      }

      // Call-to-Action Bar for Ads or Businesses (Prompt Section 19)
      if (post.ctaType != "NONE") {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          when (post.ctaType) {
            "WHATSAPP" -> {
              Button(
                onClick = { onWhatsAppClick(post.ctaPhone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
              ) {
                Text("💬 Connect on WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
            "CALL" -> {
              Button(
                onClick = { onCallClick(post.ctaPhone) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
              ) {
                Text("📞 Call Professional", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
            else -> {
              val bcnOpener = LocalBcnCardOpener.current
              Button(
                onClick = { bcnOpener?.invoke(post.authorBusyCardNetId) ?: onViewCardClick() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
              ) {
                Text("📇 View Digital Card", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Footer: Like, Comment, Share
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clickable { onLikeClick() }
              .padding(4.dp)
          ) {
            Icon(
              imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Like",
              tint = if (post.isLikedByMe) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${post.likesCount}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ChatBubbleOutline,
              contentDescription = "Comments",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${post.commentsCount}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onViewCardClick, modifier = Modifier.size(24.dp)) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }
  }
}
