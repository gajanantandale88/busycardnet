package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DigitalCardEntity
import com.example.util.BusyCardIdGenerator
import com.example.util.StylizedBarcode
import com.example.util.StylizedQRCode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DigitalVisitingCardView(
  card: DigitalCardEntity,
  modifier: Modifier = Modifier,
  onCallClick: (String) -> Unit = {},
  onWhatsAppClick: (String) -> Unit = {},
  onEmailClick: (String) -> Unit = {},
  onWebsiteClick: ((String) -> Unit)? = null,
  onLinkedInClick: ((String) -> Unit)? = null,
  onFacebookClick: ((String) -> Unit)? = null,
  onInstagramClick: ((String) -> Unit)? = null,
  onConnectClick: ((DigitalCardEntity) -> Unit)? = null,
  onShareClick: (DigitalCardEntity) -> Unit = {},
  onOrderPhysicalClick: (() -> Unit)? = null,
  onScanBarcodeClick: (() -> Unit)? = null
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  var showQrCode by remember { mutableStateOf(false) }
  var barcodeFormat by remember { mutableStateOf("BARCODE") } // "BARCODE" or "QR"
  var nfcTapped by remember { mutableStateOf(false) }
  var isBridgeConnected by remember(card.isConnected) { mutableStateOf(card.isConnected) }

  val defaultLaunchUrl: (String) -> Unit = { url ->
    try {
      val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
      context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(finalUrl)))
    } catch (e: Exception) {
      android.widget.Toast.makeText(context, "Opening link: $url", android.widget.Toast.LENGTH_SHORT).show()
    }
  }

  // Theme styling based on templateStyle
  val (cardBackgroundBrush, cardTextColor, accentColor, cardBorderColor) = when (card.templateStyle) {
    "ELECTRIC_AMBER" -> QuadTheme(
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFF130E05), Color(0xFF1E1708), Color(0xFF0F0B02))
      ),
      textColor = Color(0xFFFFFBEB),
      accent = Color(0xFFF59E0B),
      border = Color(0xFFD97706).copy(alpha = 0.6f)
    )
    "LUXURY_GOLD" -> QuadTheme(
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFF111111), Color(0xFF1C1917), Color(0xFF0A0A0A))
      ),
      textColor = Color(0xFFFAF5EF),
      accent = Color(0xFFEAB308),
      border = Color(0xFFCA8A04).copy(alpha = 0.7f)
    )
    "MINIMAL_LIGHT" -> QuadTheme(
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC), Color(0xFFF1F5F9))
      ),
      textColor = Color(0xFF0F172A),
      accent = Color(0xFF2563EB),
      border = Color(0xFFCBD5E1)
    )
    "CREATIVE_CYAN" -> QuadTheme(
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFF05171F), Color(0xFF0C2B39), Color(0xFF041018))
      ),
      textColor = Color(0xFFECFEFF),
      accent = Color(0xFF06B6D4),
      border = Color(0xFF0891B2).copy(alpha = 0.7f)
    )
    else -> QuadTheme( // MODERN_DARK / CORPORATE
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFF0B1325), Color(0xFF111E38), Color(0xFF060A14))
      ),
      textColor = Color(0xFFF8FAFC),
      accent = Color(0xFF38BDF8),
      border = Color(0xFF1E3A8A).copy(alpha = 0.8f)
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .border(1.5.dp, cardBorderColor, RoundedCornerShape(20.dp))
      .testTag("digital_visiting_card_${card.cardId}"),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    shape = RoundedCornerShape(20.dp)
  ) {
    Box(
      modifier = Modifier
        .background(cardBackgroundBrush)
        .padding(18.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {

        // Header Row: Business Name + Verified Badge + NFC indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = card.businessName.ifEmpty { "BUSYCARDNET IDENTITY" },
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = accentColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified Identity",
              tint = accentColor,
              modifier = Modifier.size(16.dp)
            )
          }

          // NFC & QR Quick Action Icons
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (nfcTapped) accentColor.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.25f),
              modifier = Modifier
                .clickable { nfcTapped = !nfcTapped }
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Nfc,
                  contentDescription = "NFC Enabled",
                  tint = if (nfcTapped) accentColor else cardTextColor.copy(alpha = 0.7f),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (nfcTapped) "NFC Active" else "NFC Ready",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (nfcTapped) accentColor else cardTextColor.copy(alpha = 0.7f)
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
              onClick = { showQrCode = !showQrCode },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "Toggle QR Code",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Details & Avatar
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Circular Initials Avatar
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.2f))
              .border(2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = card.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              color = accentColor
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = card.name,
              fontSize = 19.sp,
              fontWeight = FontWeight.Bold,
              color = cardTextColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = card.designation,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = accentColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            BcnCardHyperlink(
              bcnId = card.busyCardNetId,
              color = accentColor,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 3.dp)
            )
            if (card.address.isNotBlank()) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = cardTextColor.copy(alpha = 0.6f),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = card.address,
                  fontSize = 11.sp,
                  color = cardTextColor.copy(alpha = 0.7f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        // Services Tags
        if (card.services.isNotBlank()) {
          Spacer(modifier = Modifier.height(12.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            val serviceList = card.services.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            serviceList.take(4).forEach { service ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f))
              ) {
                Text(
                  text = service,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = cardTextColor,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
          }
        }

        // About / Working Hours
        if (card.workingHours.isNotBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = card.workingHours,
              fontSize = 11.sp,
              color = cardTextColor.copy(alpha = 0.8f)
            )
          }
        }

        // Interactive Barcode & QR Code Expansion (Prompt: Barcode to scan for any business card or BCN holder)
        AnimatedVisibility(visible = showQrCode) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Toggle between 1D Barcode and 2D QR Code
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(3.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (barcodeFormat == "BARCODE") accentColor else Color.Transparent,
                modifier = Modifier.clickable { barcodeFormat = "BARCODE" }
              ) {
                Text(
                  text = "|||| Barcode",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (barcodeFormat == "BARCODE") Color.Black else cardTextColor.copy(alpha = 0.8f),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (barcodeFormat == "QR") accentColor else Color.Transparent,
                modifier = Modifier.clickable { barcodeFormat = "QR" }
              ) {
                Text(
                  text = "⬛ QR Code",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (barcodeFormat == "QR") Color.Black else cardTextColor.copy(alpha = 0.8f),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (barcodeFormat == "BARCODE") {
              StylizedBarcode(
                data = card.busyCardNetId,
                modifier = Modifier.fillMaxWidth(0.92f),
                darkColor = Color(0xFF0F172A),
                lightColor = Color.White
              )
            } else {
              StylizedQRCode(
                data = BusyCardIdGenerator.getShortUrl(card.busyCardNetId),
                modifier = Modifier.size(130.dp),
                darkColor = Color(0xFF0F172A),
                lightColor = Color.White
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = BusyCardIdGenerator.getShortUrl(card.busyCardNetId),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = accentColor
              )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Scan with any camera or BCN Scanner to view or connect",
              fontSize = 10.sp,
              color = cardTextColor.copy(alpha = 0.6f)
            )

            if (onScanBarcodeClick != null) {
              Spacer(modifier = Modifier.height(8.dp))
              androidx.compose.material3.OutlinedButton(
                onClick = onScanBarcodeClick,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.QrCodeScanner,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Open Camera Scanner",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = accentColor
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- ALL-IN-ONE INTERACTIVE ICON STRIP (Website, Email, Phone, WhatsApp, LinkedIn, Facebook, Instagram, BCN Link) ---
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "CONNECT & SOCIAL TOUCHPOINTS",
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = cardTextColor.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 1. Phone Call Icon
          InteractiveTouchIcon(
            emojiOrLabel = "📞",
            tooltip = "Call",
            accentColor = accentColor,
            textColor = cardTextColor,
            onClick = { onCallClick(card.phone) }
          )

          // 2. WhatsApp Icon
          InteractiveTouchIcon(
            emojiOrLabel = "💬",
            tooltip = "WhatsApp",
            accentColor = Color(0xFF25D366),
            textColor = cardTextColor,
            onClick = { onWhatsAppClick(card.whatsapp) }
          )

          // 3. Website Icon
          InteractiveTouchIcon(
            emojiOrLabel = "🌐",
            tooltip = "Website",
            accentColor = Color(0xFF38BDF8),
            textColor = cardTextColor,
            onClick = {
              if (onWebsiteClick != null) onWebsiteClick(card.website)
              else defaultLaunchUrl(card.website.ifEmpty { "https://busycardnet.com" })
            }
          )

          // 4. Email Icon
          InteractiveTouchIcon(
            emojiOrLabel = "✉️",
            tooltip = "Email",
            accentColor = Color(0xFFEF4444),
            textColor = cardTextColor,
            onClick = {
              if (card.email.isNotBlank()) {
                val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                  data = android.net.Uri.parse("mailto:${card.email}")
                }
                try { context.startActivity(emailIntent) } catch (e: Exception) {
                  android.widget.Toast.makeText(context, "Email: ${card.email}", android.widget.Toast.LENGTH_SHORT).show()
                }
              }
            }
          )

          // 5. LinkedIn Icon
          InteractiveTouchIcon(
            emojiOrLabel = "💼",
            tooltip = "LinkedIn",
            accentColor = Color(0xFF0A66C2),
            textColor = cardTextColor,
            onClick = {
              val link = card.linkedin.ifEmpty { "https://linkedin.com/search/results/all/?keywords=${Uri.encode(card.name)}" }
              if (onLinkedInClick != null) onLinkedInClick(link) else defaultLaunchUrl(link)
            }
          )

          // 6. Facebook Icon
          InteractiveTouchIcon(
            emojiOrLabel = "📘",
            tooltip = "Facebook",
            accentColor = Color(0xFF1877F2),
            textColor = cardTextColor,
            onClick = {
              val link = card.facebook.ifEmpty { "https://facebook.com" }
              if (onFacebookClick != null) onFacebookClick(link) else defaultLaunchUrl(link)
            }
          )

          // 7. Instagram Icon
          InteractiveTouchIcon(
            emojiOrLabel = "📷",
            tooltip = "Instagram",
            accentColor = Color(0xFFE1306C),
            textColor = cardTextColor,
            onClick = {
              val link = card.instagram.ifEmpty { "https://instagram.com" }
              if (onInstagramClick != null) onInstagramClick(link) else defaultLaunchUrl(link)
            }
          )

          // 8. BCN Link Icon
          InteractiveTouchIcon(
            emojiOrLabel = "🆔",
            tooltip = "BCN Link",
            accentColor = accentColor,
            textColor = cardTextColor,
            onClick = {
              showQrCode = !showQrCode
              val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
              val clip = android.content.ClipData.newPlainText("BusyCardNet Link", BusyCardIdGenerator.getShortUrl(card.busyCardNetId))
              clipboard.setPrimaryClip(clip)
              android.widget.Toast.makeText(context, "Copied ${BusyCardIdGenerator.getShortUrl(card.busyCardNetId)}", android.widget.Toast.LENGTH_SHORT).show()
            }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connect Request / Bridge Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Connect / Build Bridge Button
          Surface(
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .clickable {
                isBridgeConnected = !isBridgeConnected
                onConnectClick?.invoke(card)
                val msg = if (isBridgeConnected) "Bridge built with ${card.name}!" else "Connection request cancelled"
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
              }
              .testTag("card_action_connect_bridge"),
            color = if (isBridgeConnected) Color(0xFF10B981).copy(alpha = 0.25f) else accentColor.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isBridgeConnected) Color(0xFF10B981).copy(alpha = 0.8f) else accentColor.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Row(
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp)
            ) {
              Text(
                text = if (isBridgeConnected) "🤝" else "🔗",
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isBridgeConnected) "Bridge Active" else "Connect (Build Bridge)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBridgeConnected) Color(0xFF34D399) else cardTextColor
              )
            }
          }

          // Share Button
          Surface(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onShareClick(card) }
              .testTag("card_action_share"),
            color = Color.White.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Card",
                tint = cardTextColor,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MANDATORY SECTION 8 REQUIREMENT:
        // "The BusyCardNet ID must automatically appear on every digital visiting card.
        // Mandatory position: BOTTOM-RIGHT CORNER. It must not be removable."
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          // Left side: Mini branding or primary indicator
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "BUSYCARDNET",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.2.sp,
              color = cardTextColor.copy(alpha = 0.45f)
            )
            if (card.isPrimary) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "• PRIMARY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
              )
            }
          }

          // BOTTOM-RIGHT CORNER: BUSYCARDNET ID (Clickable Hyperlink)
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = accentColor.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, accentColor.copy(alpha = 0.45f)),
            modifier = Modifier.testTag("card_busycardnet_id")
          ) {
            BcnCardHyperlink(
              bcnId = card.busyCardNetId,
              color = accentColor,
              prefix = "",
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
          }
        }
      }
    }
  }
}

private data class QuadTheme(
  val brush: Brush,
  val textColor: Color,
  val accent: Color,
  val border: Color
)

@Composable
fun InteractiveTouchIcon(
  emojiOrLabel: String,
  tooltip: String,
  accentColor: Color,
  textColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.clickable { onClick() }
  ) {
    Surface(
      shape = CircleShape,
      color = accentColor.copy(alpha = 0.16f),
      border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
      modifier = Modifier.size(34.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = emojiOrLabel,
          fontSize = 15.sp
        )
      }
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = tooltip,
      fontSize = 9.sp,
      fontWeight = FontWeight.Medium,
      color = textColor.copy(alpha = 0.75f),
      maxLines = 1
    )
  }
}
