package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.CardPrintStudioDialog
import com.example.ui.components.kyc.KycVerificationDialog
import com.example.ui.components.credits.MarketingCreditsHubDialog
import com.example.ui.components.marketing.ShopNotificationAnnouncementDialog
import com.example.data.model.KycVerificationStatus
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DigitalCardEntity
import com.example.data.local.CachedBusinessCardEntity
import com.example.ui.components.BcnCardHyperlink
import com.example.ui.components.CardCreatorStudioDialog
import com.example.ui.components.CardShareDialog
import com.example.ui.components.DigitalCard
import com.example.ui.components.DigitalVisitingCardView
import com.example.ui.components.GoogleProfileCardManagerDialog
import com.example.ui.viewmodel.BusyCardNetViewModel

@Composable
fun MyCardScreen(
  viewModel: BusyCardNetViewModel,
  onNavigateToSuppliers: () -> Unit,
  onOpenScanner: () -> Unit = {},
  onOpenStatisticsDashboard: () -> Unit = {},
  onOpenQrGenerator: (cardId: String?) -> Unit = {},
  onOpenLoginScreen: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val primaryCard by viewModel.primaryCard.collectAsStateWithLifecycle()
  val cachedCards by viewModel.allCachedBusinessCards.collectAsStateWithLifecycle()

  var selectedCardId by remember { mutableStateOf<String?>(null) }
  var showShareDialog by remember { mutableStateOf(false) }
  var showEditStudioDialog by remember { mutableStateOf(false) }
  var showCreateCardDialog by remember { mutableStateOf(false) }
  var showGoogleProfileDialog by remember { mutableStateOf(false) }
  var showAiPrintStudioDialog by remember { mutableStateOf(false) }
  var showKycDialog by remember { mutableStateOf(false) }
  var showCreditsHubDialog by remember { mutableStateOf(false) }
  var showShopNotificationDialog by remember { mutableStateOf(false) }
  var cardDisplayMode by remember { mutableStateOf("MODERN") } // MODERN or CLASSIC

  val hasActiveShopSubscription by viewModel.hasActiveShopSubscription.collectAsStateWithLifecycle()

  val kycStatus by viewModel.kycStatus.collectAsStateWithLifecycle()
  val kycData by viewModel.kycData.collectAsStateWithLifecycle()
  val creditBalance by viewModel.creditBalance.collectAsStateWithLifecycle()
  val totalEarnedCredits by viewModel.totalEarnedCredits.collectAsStateWithLifecycle()
  val totalSpentCredits by viewModel.totalSpentCredits.collectAsStateWithLifecycle()
  val shareCount by viewModel.shareCount.collectAsStateWithLifecycle()
  val creditTransactions by viewModel.creditTransactions.collectAsStateWithLifecycle()

  val activeCard = remember(selectedCardId, allCards, primaryCard) {
    allCards.find { it.cardId == selectedCardId } ?: primaryCard ?: allCards.firstOrNull()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("my_card_screen"),
    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp)
  ) {

    // 1. Header & Multi-Card Switcher (Prompt Section 55: MULTIPLE CARDS)
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "MY DIGITAL CARDS",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.8.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Multi-Card Network Identity Hub",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val userSession by viewModel.userSession.collectAsStateWithLifecycle()
            OutlinedButton(
              onClick = onOpenLoginScreen,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp).testTag("header_auth_session_button")
            ) {
              Icon(
                imageVector = if (userSession != null) Icons.Default.Shield else Icons.Default.Lock,
                contentDescription = "Session",
                modifier = Modifier.size(15.dp),
                tint = if (userSession != null) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (userSession != null) "Session" else "Login",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (userSession != null) Color(0xFF059669) else MaterialTheme.colorScheme.primary
              )
            }

            OutlinedButton(
              onClick = { showGoogleProfileDialog = true },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp).testTag("google_profile_manage_button")
            ) {
              Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Google Profile",
                modifier = Modifier.size(15.dp),
                tint = Color(0xFF4285F4)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Google", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
            }

            OutlinedButton(
              onClick = { onOpenQrGenerator(activeCard?.cardId) },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp).testTag("header_qr_generator_button")
            ) {
              Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "QR Studio",
                modifier = Modifier.size(15.dp),
                tint = Color(0xFF0284C7)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("QR Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
            }

            OutlinedButton(
              onClick = onOpenScanner,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp).testTag("scan_qr_import_button")
            ) {
              Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR",
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Button(
              onClick = { showCreateCardDialog = true },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp).testTag("add_new_card_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("+ New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-card tabs
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(allCards) { card ->
            val isSelected = card.cardId == activeCard?.cardId
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.clickable { selectedCardId = card.cardId }
            ) {
              Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(
                  text = card.businessName,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = card.busyCardNetId,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 2. The Digital Card View with Modern / Classic Toggle
    if (activeCard != null) {
      item {
        Spacer(modifier = Modifier.height(14.dp))

        // Display Mode Switcher (Modern DigitalCard vs Classic Card)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
          ) {
            Row(
              modifier = Modifier.padding(3.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (cardDisplayMode == "MODERN") MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.clickable { cardDisplayMode = "MODERN" }
              ) {
                Text(
                  text = "Modern DigitalCard",
                  fontSize = 10.sp,
                  fontWeight = if (cardDisplayMode == "MODERN") FontWeight.Bold else FontWeight.Normal,
                  color = if (cardDisplayMode == "MODERN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
              }

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (cardDisplayMode == "CLASSIC") MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.clickable { cardDisplayMode = "CLASSIC" }
              ) {
                Text(
                  text = "Classic Card",
                  fontSize = 10.sp,
                  fontWeight = if (cardDisplayMode == "CLASSIC") FontWeight.Bold else FontWeight.Normal,
                  color = if (cardDisplayMode == "CLASSIC") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
              }
            }
          }
        }

        if (cardDisplayMode == "MODERN") {
          DigitalCard(
            card = activeCard,
            isSaved = false,
            onShareClick = { showShareDialog = true },
            onCallClick = { phone ->
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
              context.startActivity(intent)
            },
            onEmailClick = { email ->
              val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
              try { context.startActivity(mailIntent) } catch (e: Exception) {}
            },
            onWebsiteClick = { url ->
              val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(if (url.startsWith("http")) url else "https://$url"))
              try { context.startActivity(webIntent) } catch (e: Exception) {}
            }
          )
        } else {
          DigitalVisitingCardView(
            card = activeCard,
            onCallClick = { phone ->
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
              context.startActivity(intent)
            },
            onWhatsAppClick = { phone ->
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone"))
              try { context.startActivity(intent) } catch (e: Exception) {}
            },
            onShareClick = {
              showShareDialog = true
            }
          )
        }
      }

      // 3. Card Quick Action Grid
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Edit Design Studio Button
          OutlinedButton(
            onClick = { showEditStudioDialog = true },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(0.9f).height(42.dp).testTag("edit_card_design_button")
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          // AI Print Studio Button (AI Visiting Card Generator & Print Workflow)
          Button(
            onClick = { showAiPrintStudioDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            modifier = Modifier.weight(1.25f).height(42.dp).testTag("ai_card_print_studio_button")
          ) {
            Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("AI Print", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
          }

          // QR Code Studio Button
          Button(
            onClick = { onOpenQrGenerator(activeCard.cardId) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.weight(1.0f).height(42.dp).testTag("card_action_qr_studio_button")
          ) {
            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }

          // Share Card
          Button(
            onClick = { showShareDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.weight(0.95f).height(42.dp).testTag("share_card_button")
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // DCN Online Shop Notification & Motive Advertizer Button
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0F172A),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showShopNotificationDialog = true }
            .testTag("my_card_shop_notification_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .background(Color(0xFF2563EB), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Notify People Online (DCN News & Motive)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0284C7)) {
                    Text("AI AD", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                  }
                }
                Text(
                  text = "Create announcement & synthesize poster image via AI",
                  fontSize = 10.sp,
                  color = Color(0xFF93C5FD)
                )
              }
            }

            Text(
              text = "Broadcast 📢",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
          }
        }
      }

      // 3B. Multi-step Anti-Fraud KYC Verification Card (Government ID, Face Liveness, Business Certificate)
      item {
        Spacer(modifier = Modifier.height(14.dp))
        val kycBorderColor = when (kycStatus) {
          KycVerificationStatus.APPROVED -> Color(0xFF10B981)
          KycVerificationStatus.PENDING_REVIEW -> Color(0xFFF59E0B)
          KycVerificationStatus.REJECTED -> Color(0xFFEF4444)
          KycVerificationStatus.UNVERIFIED -> Color(0xFF3B82F6)
        }

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, kycBorderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { showKycDialog = true }
            .testTag("kyc_verification_status_card")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(kycBorderColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (kycStatus == KycVerificationStatus.APPROVED) Icons.Default.Verified else Icons.Default.Shield,
                  contentDescription = null,
                  tint = kycBorderColor,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "BCN IDENTITY VERIFICATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = kycBorderColor.copy(alpha = 0.2f)
                  ) {
                    Text(
                      text = kycStatus.displayName.uppercase(),
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      color = kycBorderColor,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = when (kycStatus) {
                    KycVerificationStatus.APPROVED -> "Identity & Trade Certificate verified! Legitimate merchant badge active."
                    KycVerificationStatus.PENDING_REVIEW -> "Documents under review by BusyCardNet compliance team. Tap to check."
                    KycVerificationStatus.REJECTED -> "Verification failed. Tap to re-capture clear photos and resubmit."
                    KycVerificationStatus.UNVERIFIED -> "Verify Gov ID, Face & Business Certificate to prevent fake cards & earn +100 credits."
                  },
                  fontSize = 11.sp,
                  color = Color(0xFF94A3B8),
                  lineHeight = 15.sp
                )
              }
            }

            Button(
              onClick = { showKycDialog = true },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (kycStatus == KycVerificationStatus.APPROVED) Color(0xFF064E3B) else Color(0xFF2563EB)
              ),
              modifier = Modifier.height(34.dp).padding(start = 8.dp).testTag("open_kyc_dialog_button")
            ) {
              Text(
                text = if (kycStatus == KycVerificationStatus.APPROVED) "Verified" else "Verify",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (kycStatus == KycVerificationStatus.APPROVED) Color(0xFF34D399) else Color.White
              )
            }
          }
        }
      }

      // 3C. DataStore Marketing Credits Balance & Quick Hub Card
      item {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { showCreditsHubDialog = true }
            .testTag("marketing_credits_balance_card")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Campaign,
                  contentDescription = null,
                  tint = Color(0xFFFBBF24),
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "MARKETING CREDITS: $creditBalance",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFBBF24)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                  ) {
                    Text(
                      text = "+50/SHARE",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF34D399),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = "Share app to earn credits. Spend on 500m proximity broadcasts & category spotlights.",
                  fontSize = 11.sp,
                  color = Color(0xFFCBD5E1),
                  lineHeight = 15.sp
                )
              }
            }

            Button(
              onClick = { showCreditsHubDialog = true },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
              modifier = Modifier.height(34.dp).padding(start = 8.dp).testTag("open_credits_hub_button")
            ) {
              Text("Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }

      // 4. Order Physical / NFC Cards Banner (Prompt Section 13 & 14)
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2E)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showAiPrintStudioDialog = true }
            .testTag("order_physical_card_banner")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Print,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "AI CARD PRINT & PHYSICAL CARDS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF38BDF8)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                  ) {
                    Text(
                      text = "NEW",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF34D399),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = "AI-designed visiting cards with custom fields, manufacturer selection & doorstep delivery.",
                  fontSize = 10.sp,
                  color = Color(0xFFE2E8F0)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF2563EB)
            ) {
              Text(
                text = "Print",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // 5. Digital Card Analytics (Prompt Section 45: DIGITAL CARD ANALYTICS)
      item {
        Spacer(modifier = Modifier.height(18.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.BarChart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "CARD ENGAGEMENT ANALYTICS",
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            AnalyticsMetricBox(
              title = "Card Views",
              value = "${activeCard.viewsCount}",
              iconEmoji = "👁️",
              modifier = Modifier.weight(1f)
            )
            AnalyticsMetricBox(
              title = "QR Scans",
              value = "${activeCard.scansCount}",
              iconEmoji = "📱",
              modifier = Modifier.weight(1f)
            )
            AnalyticsMetricBox(
              title = "Shares",
              value = "${activeCard.sharesCount}",
              iconEmoji = "📤",
              modifier = Modifier.weight(1f)
            )
            AnalyticsMetricBox(
              title = "Leads",
              value = "${activeCard.leadsCount}",
              iconEmoji = "⚡",
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 6. Offline Cached Business Profiles (Room Database Offline Mode)
    if (cachedCards.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(18.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "OFFLINE CACHED PROFILES (${cachedCards.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF10B981).copy(alpha = 0.15f)
            ) {
              Text(
                text = "⚡ 100% Offline Ready",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF047857),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Previously visited business profiles cached in local Room database. Viewable even without internet.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          cachedCards.forEach { cachedCard ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                  viewModel.openCard(cachedCard.toDigitalCard())
                }
                .testTag("cached_card_${cachedCard.busyCardNetId}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = cachedCard.businessName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${cachedCard.ownerName} • ${cachedCard.professionTitle}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  BcnCardHyperlink(
                    bcnId = cachedCard.busyCardNetId,
                    fontSize = 11.sp
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                  ) {
                    Text(
                      text = "⭐ ${cachedCard.rating}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "View Offline",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Share Dialog
  if (showShareDialog && activeCard != null) {
    CardShareDialog(
      card = activeCard,
      onDismiss = { showShareDialog = false },
      onOpenScanner = onOpenScanner,
      onOpenQrGenerator = { cardId ->
        showShareDialog = false
        onOpenQrGenerator(cardId)
      },
      onCardShared = { platform ->
        viewModel.rewardCreditForShare(platform)
      }
    )
  }

  // Multi-Step KYC Verification Dialog (CameraX + Compose)
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

  // Marketing & Credits Hub Dialog (DataStore)
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

  // Edit Studio Dialog
  if (showEditStudioDialog && activeCard != null) {
    CardCreatorStudioDialog(
      existingCard = activeCard,
      onDismiss = { showEditStudioDialog = false },
      onSaveCard = { name, desig, prof, biz, serv, phone, wa, email, web, addr, hours, about, style, hex ->
        viewModel.createOrUpdateCard(
          name = name,
          designation = desig,
          professionCode = prof,
          businessName = biz,
          services = serv,
          phone = phone,
          whatsapp = wa,
          email = email,
          website = web,
          address = addr,
          workingHours = hours,
          about = about,
          templateStyle = style,
          accentColorHex = hex
        )
        showEditStudioDialog = false
        Toast.makeText(context, "Card updated successfully!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Create New Card Dialog
  if (showCreateCardDialog) {
    CardCreatorStudioDialog(
      existingCard = null,
      onDismiss = { showCreateCardDialog = false },
      onSaveCard = { name, desig, prof, biz, serv, phone, wa, email, web, addr, hours, about, style, hex ->
        viewModel.createOrUpdateCard(
          name = name,
          designation = desig,
          professionCode = prof,
          businessName = biz,
          services = serv,
          phone = phone,
          whatsapp = wa,
          email = email,
          website = web,
          address = addr,
          workingHours = hours,
          about = about,
          templateStyle = style,
          accentColorHex = hex
        )
        showCreateCardDialog = false
        Toast.makeText(context, "New BusyCardNet identity created!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Google Profile and Visiting Card Manager
  if (showGoogleProfileDialog) {
    GoogleProfileCardManagerDialog(
      viewModel = viewModel,
      currentLatitude = activeCard?.latitude ?: 19.0760,
      currentLongitude = activeCard?.longitude ?: 72.8777,
      existingCard = activeCard,
      onDismiss = { showGoogleProfileDialog = false },
      onCardSaved = { savedCard ->
        Toast.makeText(context, "Visiting Card Profile updated in Firestore!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // AI Visiting Card Print Studio Dialog
  if (showAiPrintStudioDialog && activeCard != null) {
    CardPrintStudioDialog(
      card = activeCard,
      userCity = activeCard.address.split(",").getOrNull(1)?.trim() ?: "Mumbai",
      userLocality = activeCard.address.split(",").firstOrNull()?.trim() ?: "Andheri East",
      onDismiss = { showAiPrintStudioDialog = false },
      onOrderPlaced = { order ->
        viewModel.placePrintOrder(order)
      }
    )
  }

  // Shop Notification & AI Announcement Dialog for Cardholder
  if (showShopNotificationDialog && activeCard != null) {
    ShopNotificationAnnouncementDialog(
      shopName = activeCard.businessName.ifBlank { activeCard.name },
      dcnId = activeCard.busyCardNetId,
      categoryOrProfession = activeCard.designation.ifBlank { "Local Business" },
      initialLocation = activeCard.address.ifBlank { "Mumbai, MH" },
      initialPhone = activeCard.phone.ifBlank { "+91 98765 43210" },
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
}

@Composable
private fun AnalyticsMetricBox(
  title: String,
  value: String,
  iconEmoji: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = iconEmoji, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontSize = 16.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = title,
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}
