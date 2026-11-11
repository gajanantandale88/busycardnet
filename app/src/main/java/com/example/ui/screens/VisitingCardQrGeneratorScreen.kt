package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firestore.FirestoreBusinessManager
import com.example.data.local.DigitalCardEntity
import com.example.ui.viewmodel.BusyCardNetViewModel
import com.example.util.VisitingCardQrGenerator
import com.example.util.VisitingCardQrGenerator.QrCenterBranding
import com.example.util.VisitingCardQrGenerator.QrRenderStyle
import com.example.util.VisitingCardQrGenerator.QrUrlType
import kotlinx.coroutines.launch

/**
 * Dedicated screen to generate unique QR codes for user digital visiting cards
 * that encode their Firebase Firestore profile URL, enabling quick sharing and networking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitingCardQrGeneratorScreen(
  viewModel: BusyCardNetViewModel,
  initialCardId: String? = null,
  onNavigateBack: () -> Unit,
  onOpenScanner: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val primaryCard by viewModel.primaryCard.collectAsStateWithLifecycle()

  var selectedCardId by remember {
    mutableStateOf(initialCardId ?: primaryCard?.cardId ?: allCards.firstOrNull()?.cardId)
  }

  val activeCard = remember(selectedCardId, allCards, primaryCard) {
    allCards.find { it.cardId == selectedCardId }
      ?: primaryCard
      ?: allCards.firstOrNull()
      ?: createFallbackCard()
  }

  // QR Code generator configuration states
  var selectedUrlType by remember { mutableStateOf(QrUrlType.FIRESTORE_WEB_PROFILE) }
  var selectedStyle by remember { mutableStateOf(QrRenderStyle.ROUNDED_SQUIRCLE) }
  var selectedBranding by remember { mutableStateOf(QrCenterBranding.BCN_MONOGRAM) }
  var selectedColorHex by remember { mutableStateOf("#0F172A") } // Slate Noir

  // Cloud Firestore sync state
  var isSyncingToFirestore by remember { mutableStateOf(false) }
  var isSyncedSuccessfully by remember { mutableStateOf(false) }

  // Fullscreen Expo Kiosk mode dialog
  var isKioskModeOpen by remember { mutableStateOf(false) }

  // NFC tap simulation dialog
  var showNfcBeamDialog by remember { mutableStateOf(false) }

  // Generate encoded payload string
  val encodedPayload = remember(activeCard, selectedUrlType) {
    VisitingCardQrGenerator.buildEncodedPayload(activeCard, selectedUrlType)
  }

  // Generate high-resolution QR Bitmap
  var qrBitmap by remember {
    mutableStateOf<Bitmap?>(null)
  }

  LaunchedEffect(encodedPayload, selectedStyle, selectedBranding, selectedColorHex, activeCard) {
    val fgColorInt = try {
      android.graphics.Color.parseColor(selectedColorHex)
    } catch (e: Exception) {
      android.graphics.Color.parseColor("#0F172A")
    }

    qrBitmap = VisitingCardQrGenerator.generateQrBitmap(
      content = encodedPayload,
      size = 650,
      fgColor = fgColorInt,
      bgColor = android.graphics.Color.WHITE,
      style = selectedStyle,
      branding = selectedBranding,
      card = activeCard
    )
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Visiting Card QR Studio",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Firebase Firestore Encoded",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0284C7)
              )
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("qr_generator_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          IconButton(
            onClick = { isKioskModeOpen = true },
            modifier = Modifier.testTag("qr_kiosk_mode_button")
          ) {
            Icon(
              imageVector = Icons.Default.Fullscreen,
              contentDescription = "Expo Kiosk Mode",
              tint = MaterialTheme.colorScheme.primary
            )
          }
          IconButton(
            onClick = onOpenScanner,
            modifier = Modifier.testTag("qr_open_scanner_button")
          ) {
            Icon(
              imageVector = Icons.Default.QrCodeScanner,
              contentDescription = "Verify with Scanner",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize().testTag("visiting_card_qr_generator_screen")
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // 1. Multi-Card Switcher (if user holds more than 1 card)
      if (allCards.size > 1) {
        item {
          Column {
            Text(
              text = "SELECT VISITING CARD",
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.8.sp,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(allCards) { card ->
                val isSelected = card.cardId == activeCard.cardId
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                  border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                  ),
                  modifier = Modifier.clickable {
                    selectedCardId = card.cardId
                  }
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(24.dp)
                        .background(
                          if (isSelected) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                          CircleShape
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = card.name.take(1).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                      )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                      Text(
                        text = card.businessName.ifBlank { card.name },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = card.busyCardNetId,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 2. Active Card Profile Banner
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.25f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("active_card_profile_summary")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(
                    listOf(
                      Color(0xFF0284C7),
                      Color(0xFF1D4ED8)
                    )
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = activeCard.name.take(1).uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = activeCard.name,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified Card",
                  tint = Color(0xFF0284C7),
                  modifier = Modifier.size(15.dp)
                )
              }
              Text(
                text = "${activeCard.designation} • ${activeCard.businessName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = activeCard.busyCardNetId,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            // Sync to Firestore status button
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSyncedSuccessfully) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF0284C7).copy(alpha = 0.12f),
              border = BorderStroke(
                1.dp,
                if (isSyncedSuccessfully) Color(0xFF10B981) else Color(0xFF0284C7).copy(alpha = 0.35f)
              ),
              modifier = Modifier
                .clickable(enabled = !isSyncingToFirestore) {
                  isSyncingToFirestore = true
                  coroutineScope.launch {
                    val ok = FirestoreBusinessManager.saveBusinessProfile(activeCard)
                    isSyncingToFirestore = false
                    isSyncedSuccessfully = true
                    Toast.makeText(
                      context,
                      if (ok) "Profile synced to Cloud Firestore!" else "Saved locally & queued for Firestore sync",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
                .testTag("qr_sync_firestore_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                if (isSyncingToFirestore) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF0284C7)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                } else {
                  Icon(
                    imageVector = if (isSyncedSuccessfully) Icons.Default.CloudDone else Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = if (isSyncedSuccessfully) Color(0xFF10B981) else Color(0xFF0284C7),
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = if (isSyncedSuccessfully) "SYNCED" else "SYNC",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSyncedSuccessfully) Color(0xFF10B981) else Color(0xFF0284C7)
                )
              }
            }
          }
        }
      }

      // 3. Central Interactive Unique QR Code Presentation Card
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
          border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("qr_code_display_card")
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            // Header tag inside card
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFF0284C7).copy(alpha = 0.12f),
              border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF10B981), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "FIRESTORE CLOUD PROFILE QR",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.6.sp,
                  color = Color(0xFF0284C7)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Rendered QR Code Image
            Box(
              modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clickable { isKioskModeOpen = true }
                .padding(12.dp),
              contentAlignment = Alignment.Center
            ) {
              if (qrBitmap != null) {
                Image(
                  bitmap = qrBitmap!!.asImageBitmap(),
                  contentDescription = "Generated QR Code encoding Firestore Profile URL",
                  modifier = Modifier
                    .fillMaxSize()
                    .testTag("generated_qr_code_image")
                )
              } else {
                CircularProgressIndicator(
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(36.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BCN Identification Barcode / ID Tag
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable {
                  val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  cm.setPrimaryClip(ClipData.newPlainText("BCN ID", activeCard.busyCardNetId))
                  Toast.makeText(context, "Copied BCN ID: ${activeCard.busyCardNetId}", Toast.LENGTH_SHORT).show()
                }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy ID",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = activeCard.busyCardNetId,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Tap QR to open Fullscreen Kiosk Expo mode",
              fontSize = 10.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 4. Quick Sharing & Networking Action Buttons
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "QUICK SHARING & NETWORKING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Main 2 Action Buttons: Native Share Sheet & WhatsApp
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                if (qrBitmap != null) {
                  VisitingCardQrGenerator.shareQrWithImageAndLink(
                    context = context,
                    card = activeCard,
                    bitmap = qrBitmap!!,
                    url = encodedPayload
                  )
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("share_qr_image_and_link_button")
            ) {
              Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Share Card & QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                VisitingCardQrGenerator.shareViaWhatsApp(
                  context = context,
                  card = activeCard,
                  url = encodedPayload
                )
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("share_whatsapp_networking_button")
            ) {
              Text("💬 WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Secondary Quick Actions: Save PNG to Gallery, Copy URL, NFC Tap
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                if (qrBitmap != null) {
                  val success = VisitingCardQrGenerator.saveQrToGallery(context, qrBitmap!!, activeCard)
                  Toast.makeText(
                    context,
                    if (success) "Saved QR Code image to Pictures/BusyCardNet!" else "Unable to save image",
                    Toast.LENGTH_LONG
                  ).show()
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(40.dp).testTag("save_qr_gallery_button")
            ) {
              Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save PNG", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Firestore Profile URL", encodedPayload))
                Toast.makeText(context, "Copied profile URL to clipboard!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(40.dp).testTag("copy_firestore_url_button")
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Copy Link", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
              onClick = { showNfcBeamDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(40.dp).testTag("nfc_tap_networking_button")
            ) {
              Icon(imageVector = Icons.Default.Nfc, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("NFC Tap", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0284C7))
            }
          }
        }
      }

      // 5. Firebase Firestore Profile URL Configuration & Inspector
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth().testTag("firestore_url_config_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = Color(0xFF0284C7),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ENCODED FIRESTORE PROFILE URL",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0284C7).copy(alpha = 0.15f)
              ) {
                Text(
                  text = "${encodedPayload.length} chars",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0284C7),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // URL Type Selector Options
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              QrUrlType.values().forEach { type ->
                val isSelected = selectedUrlType == type
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                  border = BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF0284C7) else Color.Transparent
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedUrlType = type }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = type.title,
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                          shape = RoundedCornerShape(4.dp),
                          color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ) {
                          Text(
                            text = type.badge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                          )
                        }
                      }
                      Text(
                        text = type.subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Selected",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Monospace Payload Box
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFF0F172A),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = encodedPayload,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = Color(0xFF38BDF8),
                  lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Test link in browser
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.clickable {
                      val testUrl = if (selectedUrlType == QrUrlType.VCARD_WITH_FIRESTORE) {
                        "${VisitingCardQrGenerator.DEFAULT_FIRESTORE_HOST}/profile/${activeCard.busyCardNetId}"
                      } else {
                        encodedPayload
                      }
                      try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(testUrl))
                        context.startActivity(browserIntent)
                      } catch (e: Exception) {
                        Toast.makeText(context, "Opening: $testUrl", Toast.LENGTH_SHORT).show()
                      }
                    }
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Open",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "Test Link",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(8.dp))

                  // Copy URL Button
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0284C7),
                    modifier = Modifier.clickable {
                      val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      cm.setPrimaryClip(ClipData.newPlainText("Firestore URL", encodedPayload))
                      Toast.makeText(context, "Copied URL to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "Copy",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 6. QR Code Styling & Customization Studio
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth().testTag("qr_customization_studio_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "QR DESIGN & BRANDING STUDIO",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // A. Color Palette Options
            Text(
              text = "COLOR PALETTE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            val colorPresets = listOf(
              Triple("#0F172A", "Onyx", Color(0xFF0F172A)),
              Triple("#0284C7", "Sapphire", Color(0xFF0284C7)),
              Triple("#059669", "Emerald", Color(0xFF059669)),
              Triple("#4F46E5", "Indigo", Color(0xFF4F46E5)),
              Triple("#D97706", "Amber", Color(0xFFD97706)),
              Triple("#DC2626", "Crimson", Color(0xFFDC2626)),
              Triple(activeCard.accentColorHex, "Card Color", Color(0xFF1E3A8A))
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              colorPresets.forEach { (hex, label, swatchColor) ->
                val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  border = BorderStroke(
                    1.5.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                  ),
                  modifier = Modifier.clickable { selectedColorHex = hex }
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(16.dp)
                        .background(swatchColor, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = label,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // B. Module Render Style
            Text(
              text = "MODULE SHAPE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              QrRenderStyle.values().forEach { style ->
                val isSelected = selectedStyle == style
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { selectedStyle = style }
                ) {
                  Text(
                    text = style.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // C. Center Overlay Branding
            Text(
              text = "CENTER LOGO / EMBLEM",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              QrCenterBranding.values().forEach { branding ->
                val isSelected = selectedBranding == branding
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { selectedBranding = branding }
                ) {
                  Text(
                    text = branding.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(vertical = 7.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 7. Security & Cloud Architecture Information Footer
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0284C7).copy(alpha = 0.08f),
          border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Google Cloud Firestore Verified Architecture",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Each generated QR code encodes an immutable Firestore endpoint with Error Correction Level H (30% redundancy). Anyone scanning with standard phone cameras can view your verified credentials without needing the app installed.",
              fontSize = 10.5.sp,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 14.sp
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // DIALOG 1: Fullscreen Expo / Kiosk Standee Mode
  if (isKioskModeOpen && qrBitmap != null) {
    Dialog(
      onDismissRequest = { isKioskModeOpen = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFF0B1120))
          .padding(24.dp)
          .testTag("qr_kiosk_fullscreen_dialog"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Top bar in kiosk mode
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFF10B981).copy(alpha = 0.2f),
              border = BorderStroke(1.dp, Color(0xFF10B981))
            ) {
              Text(
                text = "LIVE NETWORKING STAND",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }

            IconButton(
              onClick = { isKioskModeOpen = false },
              modifier = Modifier
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                .size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = "Exit Kiosk",
                tint = Color.White
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          Text(
            text = activeCard.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
          )
          Text(
            text = "${activeCard.designation} • ${activeCard.businessName}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Large QR Presentation Container
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
              .fillMaxWidth(0.92f)
              .aspectRatio(1f)
              .padding(12.dp)
          ) {
            Box(
              modifier = Modifier.fillMaxSize().padding(14.dp),
              contentAlignment = Alignment.Center
            ) {
              Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "Fullscreen QR",
                modifier = Modifier.fillMaxSize()
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = activeCard.busyCardNetId,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Point any phone camera to instantly view and save Cloud Profile",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }

  // DIALOG 2: NFC Contactless Tap Beam Simulator
  if (showNfcBeamDialog) {
    Dialog(onDismissRequest = { showNfcBeamDialog = false }) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .testTag("nfc_beam_simulator_dialog")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Nfc,
              contentDescription = null,
              tint = Color(0xFF0284C7),
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Ready to Tap (NFC Networking)",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Hold your phone near an NFC-enabled Android or iPhone to beam your Cloud Firestore visiting card profile (${activeCard.busyCardNetId}).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = encodedPayload,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = Color(0xFF0284C7),
              modifier = Modifier.padding(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Button(
            onClick = { showNfcBeamDialog = false },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Done", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

private fun createFallbackCard(): DigitalCardEntity {
  return DigitalCardEntity(
    cardId = "card-primary-default",
    internalUserId = "user_default_01",
    busyCardNetId = "IN-MH-MU-26-581ELEC347",
    name = "Rajesh Sharma",
    designation = "Licensed Master Electrician",
    professionCode = "ELEC",
    businessName = "Raj Electrical Works & Automation",
    services = "3-Phase Wiring, Inverter Repairs, Industrial Panels, Home Rewiring",
    phone = "+91 98201 23456",
    whatsapp = "+91 98201 23456",
    email = "rajesh@rajelectricals.in",
    website = "https://rajelectricals.in",
    address = "Shop 4, Linking Road, Bandra West, Mumbai 400050",
    workingHours = "Mon - Sat: 9:00 AM - 8:00 PM",
    about = "Licensed electrical engineering contractor & automation specialist serving commercial and residential projects across Mumbai.",
    templateStyle = "CORPORATE_SLATE",
    accentColorHex = "#1D4ED8",
    latitude = 19.0596,
    longitude = 72.8295,
    isPrimary = true,
    viewsCount = 1420,
    scansCount = 680,
    sharesCount = 310
  )
}
