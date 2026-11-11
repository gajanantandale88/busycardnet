package com.example.ui.components.marketing

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MarketingCampaign
import com.example.util.ShopNotificationAiEngine
import kotlinx.coroutines.launch

enum class ShopNotificationBroadcastTier(
  val id: String,
  val title: String,
  val costCreditsOrRupees: Int,
  val radiusMeters: Int,
  val durationDays: Int,
  val badgeText: String,
  val description: String
) {
  LOCAL_ALERT(
    id = "LOCAL_ALERT",
    title = "Neighborhood Alert",
    costCreditsOrRupees = 49,
    radiusMeters = 1000,
    durationDays = 3,
    badgeText = "QUICK FLASH",
    description = "Immediate 1km geofenced notification broadcast to residents & cardholders."
  ),
  TOWN_ANNOUNCEMENT(
    id = "TOWN_ANNOUNCEMENT",
    title = "Town News & Offer",
    costCreditsOrRupees = 149,
    radiusMeters = 5000,
    durationDays = 7,
    badgeText = "MOST POPULAR",
    description = "5km wide announcement with AI visual banner & priority feed placement."
  ),
  DISTRICT_CAMPAIGN(
    id = "DISTRICT_CAMPAIGN",
    title = "City-Wide Megaphone",
    costCreditsOrRupees = 299,
    radiusMeters = 15000,
    durationDays = 14,
    badgeText = "MAX REACH",
    description = "15km metro radius, push alert highlight, verified DCN badge & WhatsApp direct CTA."
  )
}

/**
 * Shop Notification & Announcement Activity Dialog for DCN Cardholders.
 * Empowers shopkeepers to create online news, announcements, and AI-generated image posters
 * to advertize their motives to nearby people using digital wallet credits, subscription, or instant payment.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopNotificationAnnouncementDialog(
  shopName: String,
  dcnId: String,
  categoryOrProfession: String,
  initialLocation: String,
  initialPhone: String,
  walletCreditBalance: Int,
  hasActiveSubscription: Boolean = false,
  onOpenTopUpWallet: () -> Unit = {},
  onOpenSubscription: () -> Unit = {},
  onDismiss: () -> Unit,
  onPublishNotification: (
    title: String,
    body: String,
    bannerBitmap: Bitmap?,
    radiusMeters: Int,
    durationDays: Int,
    ctaType: String,
    ctaPhone: String,
    paidWithCredits: Boolean,
    paidWithSubscription: Boolean,
    cost: Int
  ) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var motivePrompt by remember {
    mutableStateOf("Grand festive season discount! Flat 30% off on all items, fresh arrivals available now. Visit store today!")
  }
  var discountPercent by remember { mutableStateOf("30%") }
  var notificationTitle by remember { mutableStateOf("") }
  var notificationBody by remember { mutableStateOf("") }
  var selectedCta by remember { mutableStateOf("WHATSAPP") }
  var contactPhone by remember { mutableStateOf(initialPhone) }
  var selectedTier by remember { mutableStateOf(ShopNotificationBroadcastTier.TOWN_ANNOUNCEMENT) }
  var generatedBannerBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isGeneratingAi by remember { mutableStateOf(false) }

  // Payment Selection: CREDITS, SUBSCRIPTION, or INSTANT_PAY
  var paymentMode by remember {
    mutableStateOf(
      if (hasActiveSubscription) "SUBSCRIPTION"
      else if (walletCreditBalance >= ShopNotificationBroadcastTier.TOWN_ANNOUNCEMENT.costCreditsOrRupees) "CREDITS"
      else "INSTANT_PAY"
    )
  }

  // Pre-generate initial AI announcement
  LaunchedEffect(Unit) {
    isGeneratingAi = true
    val result = ShopNotificationAiEngine.generateShopAnnouncement(
      shopName = shopName,
      dcnId = dcnId,
      categoryOrProfession = categoryOrProfession,
      motivePrompt = motivePrompt,
      discountPercent = discountPercent
    )
    notificationTitle = result.headline
    notificationBody = result.announcementBody
    selectedCta = result.callToAction
    generatedBannerBitmap = result.fallbackBannerBitmap
    isGeneratingAi = false
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 14.dp)
        .testTag("shop_notification_announcement_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {

        // ================= HEADER =================
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                  ),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Shop Notification & AI Ad",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFFDBEAFE)
                ) {
                  Text(
                    text = "DCN ONLINE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1D4ED8),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "$shopName • DCN: $dcnId",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subscription / Credit status callout banner
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0F172A),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Wallet: $walletCreditBalance Credits",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = if (hasActiveSubscription) "✓ Unlimited DCN Shop Subscription Active" else "Use wallet credits or subscribe to notify daily",
                  fontSize = 10.sp,
                  color = if (hasActiveSubscription) Color(0xFF34D399) else Color(0xFF94A3B8)
                )
              }
            }

            if (!hasActiveSubscription) {
              OutlinedButton(
                onClick = onOpenSubscription,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text("Subscription", fontSize = 10.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= 1. MOTIVE & AI PROMPT =================
        Text(
          text = "1. WHAT DO YOU WANT TO ADVERTIZE / NOTIFY?",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF1E3A8A),
          letterSpacing = 0.5.sp
        )
        Text(
          text = "Enter your shop's news, arrival, discount, emergency service, or motive:",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = motivePrompt,
          onValueChange = { motivePrompt = it },
          placeholder = { Text("e.g. 50% discount on shoes, free home delivery, shop open till midnight...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_motive_input"),
          shape = RoundedCornerShape(12.dp),
          minLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = discountPercent,
            onValueChange = { discountPercent = it },
            label = { Text("Discount / Offer %") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
          )

          Button(
            onClick = {
              scope.launch {
                isGeneratingAi = true
                val result = ShopNotificationAiEngine.generateShopAnnouncement(
                  shopName = shopName,
                  dcnId = dcnId,
                  categoryOrProfession = categoryOrProfession,
                  motivePrompt = motivePrompt,
                  discountPercent = discountPercent
                )
                notificationTitle = result.headline
                notificationBody = result.announcementBody
                selectedCta = result.callToAction
                generatedBannerBitmap = result.fallbackBannerBitmap
                isGeneratingAi = false
                Toast.makeText(context, "✨ AI generated shop news & custom banner!", Toast.LENGTH_SHORT).show()
              }
            },
            enabled = !isGeneratingAi,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            modifier = Modifier
              .weight(1.3f)
              .height(54.dp)
              .testTag("generate_with_ai_button")
          ) {
            if (isGeneratingAi) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Crafting AI...", fontSize = 11.sp)
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Create via AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= 2. AI GENERATED AD BANNER & PREVIEW =================
        Text(
          text = "2. AI CREATED NOTIFICATION BANNER",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF1E3A8A),
          letterSpacing = 0.5.sp
        )
        Text(
          text = "Generated high-impact poster banner broadcasted to nearby people:",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (generatedBannerBitmap != null) {
          Card(
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("ai_generated_banner_preview")
          ) {
            Image(
              bitmap = generatedBannerBitmap!!.asImageBitmap(),
              contentDescription = "AI Generated Notification Banner",
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
            )
          }
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
              .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text("Tap 'Create via AI' to synthesize your motive poster", fontSize = 11.sp, color = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Headline & Body Editing
        OutlinedTextField(
          value = notificationTitle,
          onValueChange = { notificationTitle = it },
          label = { Text("Announcement Headline") },
          modifier = Modifier.fillMaxWidth().testTag("notification_headline_input"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = notificationBody,
          onValueChange = { notificationBody = it },
          label = { Text("News & Motive Details") },
          modifier = Modifier.fillMaxWidth().testTag("notification_body_input"),
          shape = RoundedCornerShape(10.dp),
          minLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ================= 3. BROADCAST RADIUS & TIER =================
        Text(
          text = "3. SELECT BROADCAST AREA & DAYS",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF1E3A8A),
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ShopNotificationBroadcastTier.values().forEach { tier ->
            val isTierSelected = selectedTier == tier
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isTierSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
              border = BorderStroke(
                if (isTierSelected) 2.dp else 1.dp,
                if (isTierSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedTier = tier }
                .testTag("tier_${tier.id}")
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(20.dp)
                      .background(if (isTierSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    if (isTierSelected) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(tier.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                      Spacer(modifier = Modifier.width(6.dp))
                      Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDBEAFE)) {
                        Text(tier.badgeText, fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E40AF), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                      }
                    }
                    Text(tier.description, fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                      text = "📍 ${if (tier.radiusMeters < 1000) "${tier.radiusMeters}m" else "${tier.radiusMeters / 1000}km"} Radius • ⏱️ ${tier.durationDays} Days Active",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = Color(0xFF0369A1)
                    )
                  }
                }

                Text(
                  text = "${tier.costCreditsOrRupees} Cr / ₹${tier.costCreditsOrRupees}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF0F172A)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= 4. PAYMENT & AVAIL FACILITY =================
        Text(
          text = "4. AVAIL FACILITY (PAYMENT METHOD)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF1E3A8A),
          letterSpacing = 0.5.sp
        )
        Text(
          text = "DCN cardholders can use wallet credit, take subscription, or pay directly:",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // A: Digital Wallet Credits
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (paymentMode == "CREDITS") Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
            border = BorderStroke(
              if (paymentMode == "CREDITS") 2.dp else 1.dp,
              if (paymentMode == "CREDITS") Color(0xFF2563EB) else Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { paymentMode = "CREDITS" }
              .testTag("pay_mode_credits")
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Wallet Credits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text("Bal: $walletCreditBalance Cr", fontSize = 10.sp, color = if (walletCreditBalance >= selectedTier.costCreditsOrRupees) Color(0xFF059669) else Color(0xFFDC2626), fontWeight = FontWeight.Bold)
              Text("Deduct ${selectedTier.costCreditsOrRupees} Cr", fontSize = 9.sp, color = Color(0xFF64748B))
            }
          }

          // B: Subscription
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (paymentMode == "SUBSCRIPTION") Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
            border = BorderStroke(
              if (paymentMode == "SUBSCRIPTION") 2.dp else 1.dp,
              if (paymentMode == "SUBSCRIPTION") Color(0xFFD97706) else Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { paymentMode = "SUBSCRIPTION" }
              .testTag("pay_mode_subscription")
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DCN Pass", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(if (hasActiveSubscription) "Active Plan" else "₹499 / Month", fontSize = 10.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
              Text("Unlimited news", fontSize = 9.sp, color = Color(0xFF64748B))
            }
          }

          // C: Instant UPI / Payment
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (paymentMode == "INSTANT_PAY") Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
            border = BorderStroke(
              if (paymentMode == "INSTANT_PAY") 2.dp else 1.dp,
              if (paymentMode == "INSTANT_PAY") Color(0xFF10B981) else Color(0xFFE2E8F0)
            ),
            modifier = Modifier
              .weight(1f)
              .clickable { paymentMode = "INSTANT_PAY" }
              .testTag("pay_mode_instant")
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Direct Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text("₹${selectedTier.costCreditsOrRupees}", fontSize = 10.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
              Text("UPI / Card", fontSize = 9.sp, color = Color(0xFF64748B))
            }
          }
        }

        if (paymentMode == "CREDITS" && walletCreditBalance < selectedTier.costCreditsOrRupees) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Short by ${selectedTier.costCreditsOrRupees - walletCreditBalance} Credits",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB91C1C)
              )
              Button(
                onClick = {
                  onDismiss()
                  onOpenTopUpWallet()
                },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Text("Top-Up Wallet", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Publish Button
        Button(
          onClick = {
            val titleToPublish = notificationTitle.ifBlank { "📢 Special Announcement from $shopName" }
            val bodyToPublish = notificationBody.ifBlank { motivePrompt }
            val paidWithCredits = paymentMode == "CREDITS"
            val paidWithSub = paymentMode == "SUBSCRIPTION"

            onPublishNotification(
              titleToPublish,
              bodyToPublish,
              generatedBannerBitmap,
              selectedTier.radiusMeters,
              selectedTier.durationDays,
              selectedCta,
              contactPhone,
              paidWithCredits,
              paidWithSub,
              selectedTier.costCreditsOrRupees
            )
          },
          enabled = !(paymentMode == "CREDITS" && walletCreditBalance < selectedTier.costCreditsOrRupees),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("publish_shop_notification_button")
        ) {
          Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Broadcast News to People (${selectedTier.title})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
