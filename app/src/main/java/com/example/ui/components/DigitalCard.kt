package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DigitalCardEntity
import com.example.util.StylizedQRCode
import com.example.util.VisitingCardQrGenerator
import com.example.util.VisitingCardQrGenerator.QrCenterBranding
import com.example.util.VisitingCardQrGenerator.QrRenderStyle
import com.example.util.VisitingCardQrGenerator.QrUrlType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reusable DigitalCard component displaying a user's professional business details
 * (name, title, company, contacts, local distance) alongside an integrated, interactive
 * QR code generator for sharing and a local contact save affordance.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DigitalCard(
  name: String,
  title: String,
  company: String,
  modifier: Modifier = Modifier,
  phone: String = "",
  email: String = "",
  website: String = "",
  address: String = "",
  busyCardId: String = "",
  distanceLabel: String = "",
  rating: Float = 4.9f,
  reviewCount: Int = 18,
  verified: Boolean = true,
  category: String = "Professional",
  isSaved: Boolean = false,
  themeColors: List<Color> = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
  accentColor: Color = Color(0xFF38BDF8),
  initialQrType: QrUrlType = QrUrlType.FIRESTORE_WEB_PROFILE,
  onSaveContactClick: (() -> Unit)? = null,
  onShareClick: (() -> Unit)? = null,
  onCallClick: ((String) -> Unit)? = null,
  onEmailClick: ((String) -> Unit)? = null,
  onWebsiteClick: ((String) -> Unit)? = null
) {
  val context = LocalContext.current
  var selectedQrType by remember { mutableStateOf(initialQrType) }
  var showFullscreenQr by remember { mutableStateOf(false) }
  var copiedToClipboard by remember { mutableStateOf(false) }

  // Synthesize a valid DigitalCardEntity for payload generation & sharing
  val effectiveBcnId = busyCardId.ifBlank {
    val cleanName = name.replace(" ", "").take(4).uppercase()
    "IN-MH-BCN-$cleanName-2026"
  }

  val cardEntity = remember(name, title, company, phone, email, website, address, effectiveBcnId) {
    DigitalCardEntity(
      cardId = "card_$effectiveBcnId",
      internalUserId = "user_$effectiveBcnId",
      busyCardNetId = effectiveBcnId,
      name = name,
      designation = title,
      professionCode = category.take(4).uppercase(),
      businessName = company,
      services = "$title, Professional Services",
      phone = phone,
      whatsapp = phone,
      email = email,
      website = website,
      address = address,
      workingHours = "Mon - Sat: 9:00 AM - 7:00 PM",
      about = "Verified professional details for $name at $company.",
      templateStyle = "MODERN_DARK",
      accentColorHex = "#38BDF8"
    )
  }

  // Generate payload URL or vCard content
  val encodedPayload = remember(cardEntity, selectedQrType) {
    VisitingCardQrGenerator.buildEncodedPayload(cardEntity, selectedQrType)
  }

  // Asynchronously generate camera-readable ZXing QR code bitmap
  val qrBitmapState by produceState<Bitmap?>(initialValue = null, encodedPayload, accentColor) {
    value = withContext(Dispatchers.Default) {
      VisitingCardQrGenerator.generateQrBitmap(
        content = encodedPayload,
        size = 512,
        fgColor = android.graphics.Color.parseColor("#0F172A"),
        bgColor = android.graphics.Color.WHITE,
        style = QrRenderStyle.ROUNDED_SQUIRCLE,
        branding = QrCenterBranding.BCN_MONOGRAM,
        card = cardEntity
      )
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
      .clip(RoundedCornerShape(20.dp))
      .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
      .testTag("digital_card_container"),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    shape = RoundedCornerShape(20.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.linearGradient(themeColors))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        // 1. TOP HEADER: Avatar + Category & BCN ID + Save Contact & Verification Badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // User Avatar / Initials Circle
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                  Brush.radialGradient(
                    listOf(accentColor, accentColor.copy(alpha = 0.4f))
                  )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              val initials = name.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .ifBlank { "DC" }
              Text(
                text = initials,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif
              )
            }

            Column {
              Text(
                text = category.uppercase(),
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
              )
              if (effectiveBcnId.isNotBlank()) {
                Text(
                  text = effectiveBcnId,
                  color = Color.White.copy(alpha = 0.65f),
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          // Save Contact Button & Rating/Verification Badges
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (verified) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF10B981).copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(12.dp)
                  )
                  Text(
                    text = "VERIFIED",
                    color = Color(0xFF34D399),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Save Contact Toggle Button
            if (onSaveContactClick != null) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSaved) Color(0xFF0284C7) else Color.White.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSaved) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                  .clickable { onSaveContactClick() }
                  .testTag("save_contact_button")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSaved) "Saved Contact" else "Save Contact",
                    tint = if (isSaved) Color.White else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = if (isSaved) "Saved" else "Save",
                    color = if (isSaved) Color.White else Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. CORE BUSINESS DETAILS: Name, Title, Company, Rating, Local Distance
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = name,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp,
            modifier = Modifier.testTag("digital_card_name")
          )

          Spacer(modifier = Modifier.height(2.dp))

          // Title / Designation
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Work,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = title,
              color = Color.White.copy(alpha = 0.92f),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.testTag("digital_card_title")
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          // Company Name
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Business,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.7f),
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = company,
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.testTag("digital_card_company")
            )
          }

          // Distance & Rating Sub-row
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (distanceLabel.isNotBlank()) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF0284C7).copy(alpha = 0.25f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(11.dp)
                  )
                  Text(
                    text = distanceLabel,
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }

            // Star Rating
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = String.format("%.1f", rating),
                color = Color(0xFFFBBF24),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "($reviewCount)",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 10.sp
              )
            }
          }
        }

        // 3. CONTACT QUICK-ACTION CHIPS (Call, Email, Website, Location)
        if (phone.isNotBlank() || email.isNotBlank() || address.isNotBlank() || website.isNotBlank()) {
          Spacer(modifier = Modifier.height(12.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            if (phone.isNotBlank()) {
              ContactChip(
                icon = Icons.Default.Call,
                label = phone,
                tint = Color(0xFF34D399),
                onClick = {
                  if (onCallClick != null) onCallClick(phone)
                  else {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    try { context.startActivity(dialIntent) } catch (e: Exception) {}
                  }
                }
              )
            }

            if (email.isNotBlank()) {
              ContactChip(
                icon = Icons.Default.Email,
                label = email,
                tint = Color(0xFF60A5FA),
                onClick = {
                  if (onEmailClick != null) onEmailClick(email)
                  else {
                    val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                    try { context.startActivity(mailIntent) } catch (e: Exception) {}
                  }
                }
              )
            }

            if (website.isNotBlank()) {
              ContactChip(
                icon = Icons.Default.Language,
                label = website.removePrefix("https://").removePrefix("http://"),
                tint = Color(0xFFA78BFA),
                onClick = {
                  if (onWebsiteClick != null) onWebsiteClick(website)
                  else {
                    val webUrl = if (website.startsWith("http://") || website.startsWith("https://")) website else "https://$website"
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                    try { context.startActivity(webIntent) } catch (e: Exception) {}
                  }
                }
              )
            }

            if (address.isNotBlank()) {
              ContactChip(
                icon = Icons.Default.LocationOn,
                label = address,
                tint = Color(0xFFF87171),
                onClick = null
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // 4. INTEGRATED QR CODE GENERATOR FOR SHARING
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("digital_card_qr_section")
        ) {
          // QR Header Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.QrCode2,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(15.dp)
                )
              }

              Text(
                text = "QR Generator for Sharing",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            // Expand QR Button
            IconButton(
              onClick = { showFullscreenQr = true },
              modifier = Modifier
                .size(28.dp)
                .testTag("expand_qr_button")
            ) {
              Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Expand QR Code",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // QR Code Format Switcher (Cloud Web, vCard Contact, App Link)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            val qrTypes = listOf(
              QrUrlType.FIRESTORE_WEB_PROFILE to "Web Profile",
              QrUrlType.VCARD_WITH_FIRESTORE to "vCard",
              QrUrlType.UNIVERSAL_DEEPLINK to "App Link"
            )

            qrTypes.forEach { (type, label) ->
              val isSelected = selectedQrType == type
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                  .weight(1f)
                  .height(26.dp)
                  .clickable { selectedQrType = type }
                  .testTag("qr_type_${type.name.lowercase()}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF0F172A) else Color.White.copy(alpha = 0.8f)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // QR Code Preview & Action Buttons Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Camera Scannable QR Code Box
            Box(
              modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(4.dp)
                .clickable { showFullscreenQr = true }
                .testTag("digital_card_qr_preview"),
              contentAlignment = Alignment.Center
            ) {
              if (qrBitmapState != null) {
                Image(
                  bitmap = qrBitmapState!!.asImageBitmap(),
                  contentDescription = "Camera scannable QR code for $name",
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                StylizedQRCode(
                  data = encodedPayload,
                  modifier = Modifier.fillMaxSize(),
                  darkColor = Color(0xFF0F172A),
                  lightColor = Color.White
                )
              }
            }

            // Quick Sharing Action Buttons
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Primary Native Share Button
              Button(
                onClick = {
                  if (onShareClick != null) {
                    onShareClick()
                  } else {
                    val bitmapToShare = qrBitmapState ?: VisitingCardQrGenerator.generateQrBitmap(
                      content = encodedPayload,
                      card = cardEntity
                    )
                    VisitingCardQrGenerator.shareQrWithImageAndLink(
                      context = context,
                      card = cardEntity,
                      bitmap = bitmapToShare,
                      url = VisitingCardQrGenerator.buildEncodedPayload(cardEntity, QrUrlType.FIRESTORE_WEB_PROFILE)
                    )
                  }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(34.dp)
                  .testTag("digital_card_share_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = null,
                  tint = Color(0xFF0F172A),
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Share Card & QR",
                  color = Color(0xFF0F172A),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              // Copy Encoded Link Button
              OutlinedButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("BusyCardNet Link", encodedPayload)
                  clipboard.setPrimaryClip(clip)
                  copiedToClipboard = true
                  Toast.makeText(context, "Card link copied!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(30.dp)
                  .testTag("digital_card_copy_link_button")
              ) {
                Icon(
                  imageVector = if (copiedToClipboard) Icons.Default.Check else Icons.Default.ContentCopy,
                  contentDescription = null,
                  tint = if (copiedToClipboard) Color(0xFF34D399) else Color.White.copy(alpha = 0.9f),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (copiedToClipboard) "Copied!" else "Copy Link",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium
                )
              }

              // Save to Device Button
              OutlinedButton(
                onClick = {
                  val bitmapToSave = qrBitmapState ?: VisitingCardQrGenerator.generateQrBitmap(
                    content = encodedPayload,
                    card = cardEntity
                  )
                  val saved = VisitingCardQrGenerator.saveQrToGallery(context, bitmapToSave, cardEntity)
                  if (saved) {
                    Toast.makeText(context, "QR saved to Pictures/BusyCardNet!", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(context, "Failed to save QR code", Toast.LENGTH_SHORT).show()
                  }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(30.dp)
                  .testTag("digital_card_save_qr_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.8f),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Save QR Image",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Normal,
                  color = Color.White.copy(alpha = 0.9f)
                )
              }
            }
          }
        }
      }
    }
  }

  // Fullscreen High-Visibility QR Code Modal for In-Person Networking
  if (showFullscreenQr) {
    Dialog(onDismissRequest = { showFullscreenQr = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp))
          .testTag("fullscreen_qr_dialog"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
              Text(
                text = "$title • $company",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
            }

            IconButton(
              onClick = { showFullscreenQr = false },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = "Close",
                tint = Color(0xFF64748B)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Big Crisp QR Code for Camera Scanning
          Box(
            modifier = Modifier
              .size(240.dp)
              .clip(RoundedCornerShape(14.dp))
              .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
              .padding(10.dp),
            contentAlignment = Alignment.Center
          ) {
            if (qrBitmapState != null) {
              Image(
                bitmap = qrBitmapState!!.asImageBitmap(),
                contentDescription = "Fullscreen QR code for $name",
                modifier = Modifier.fillMaxSize()
              )
            } else {
              CircularProgressIndicator(color = Color(0xFF0F172A))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Scan with any phone camera to instantly view digital profile & save contact",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              val bitmapToShare = qrBitmapState ?: VisitingCardQrGenerator.generateQrBitmap(
                content = encodedPayload,
                card = cardEntity
              )
              VisitingCardQrGenerator.shareQrWithImageAndLink(
                context = context,
                card = cardEntity,
                bitmap = bitmapToShare,
                url = VisitingCardQrGenerator.buildEncodedPayload(cardEntity, QrUrlType.FIRESTORE_WEB_PROFILE)
              )
              showFullscreenQr = false
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
              .fillMaxWidth()
              .height(42.dp)
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share Card via Apps", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Convenient overload for DigitalCard accepting a [DigitalCardEntity] directly.
 */
@Composable
fun DigitalCard(
  card: DigitalCardEntity,
  modifier: Modifier = Modifier,
  isSaved: Boolean = false,
  distanceLabel: String = if (card.distanceMeters > 0) "${card.distanceMeters}m away" else "",
  initialQrType: QrUrlType = QrUrlType.FIRESTORE_WEB_PROFILE,
  themeColors: List<Color> = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
  accentColor: Color = Color(0xFF38BDF8),
  onSaveContactClick: (() -> Unit)? = null,
  onShareClick: ((DigitalCardEntity) -> Unit)? = null,
  onCallClick: ((String) -> Unit)? = null,
  onEmailClick: ((String) -> Unit)? = null,
  onWebsiteClick: ((String) -> Unit)? = null
) {
  DigitalCard(
    name = card.name,
    title = card.designation,
    company = card.businessName,
    modifier = modifier,
    phone = card.phone,
    email = card.email,
    website = card.website,
    address = card.address,
    busyCardId = card.busyCardNetId,
    distanceLabel = distanceLabel,
    rating = 4.9f,
    reviewCount = card.viewsCount.coerceAtLeast(14),
    verified = true,
    category = card.professionCode.ifBlank { "Professional" },
    isSaved = isSaved,
    themeColors = themeColors,
    accentColor = accentColor,
    initialQrType = initialQrType,
    onSaveContactClick = onSaveContactClick,
    onShareClick = if (onShareClick != null) { { onShareClick(card) } } else null,
    onCallClick = onCallClick,
    onEmailClick = onEmailClick,
    onWebsiteClick = onWebsiteClick
  )
}

/**
 * Visual pill chip for business contact details with custom tint and click handler.
 */
@Composable
private fun ContactChip(
  icon: ImageVector,
  label: String,
  tint: Color,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = Color.White.copy(alpha = 0.08f),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    modifier = modifier.then(
      if (onClick != null) Modifier.clickable { onClick() } else Modifier
    )
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(12.dp)
      )
      Text(
        text = label,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
