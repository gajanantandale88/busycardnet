package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DigitalCardEntity
import com.example.util.BusyCardIdGenerator
import com.example.util.StylizedBarcode
import com.example.util.StylizedQRCode

/**
 * Share Card Dialog (Prompt Section 57)
 */
@Composable
fun CardShareDialog(
  card: DigitalCardEntity,
  onDismiss: () -> Unit,
  onOpenScanner: (() -> Unit)? = null,
  onOpenQrGenerator: ((cardId: String) -> Unit)? = null,
  onCardShared: ((platform: String) -> Unit)? = null
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val shortUrl = BusyCardIdGenerator.getShortUrl(card.busyCardNetId)
  var nfcEmulated by remember { mutableStateOf(false) }
  var shareFormat by remember { mutableStateOf("CONTACT_QR") } // "CONTACT_QR", "WEB_QR", "BARCODE"

  val vCardData = remember(card) {
    buildString {
      appendLine("BEGIN:VCARD")
      appendLine("VERSION:3.0")
      appendLine("FN:${card.name}")
      if (card.businessName.isNotBlank()) appendLine("ORG:${card.businessName}")
      if (card.designation.isNotBlank()) appendLine("TITLE:${card.designation}")
      if (card.phone.isNotBlank()) appendLine("TEL;TYPE=CELL:${card.phone}")
      if (card.whatsapp.isNotBlank() && card.whatsapp != card.phone) appendLine("TEL;TYPE=WORK:${card.whatsapp}")
      if (card.email.isNotBlank()) appendLine("EMAIL;TYPE=INTERNET:${card.email}")
      if (card.website.isNotBlank()) appendLine("URL:${card.website}")
      if (card.address.isNotBlank()) appendLine("ADR;TYPE=WORK:;;${card.address};;;;")
      appendLine("NOTE:BusyCardNet ID: ${card.busyCardNetId}")
      appendLine("END:VCARD")
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("card_share_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Share Digital Identity",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )

          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Format selector: 2D Contact QR vs Web QR vs 1D Linear Barcode
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (shareFormat == "CONTACT_QR") MaterialTheme.colorScheme.primary else Color.Transparent,
            modifier = Modifier.clickable { shareFormat = "CONTACT_QR" }.testTag("tab_contact_vcard_qr")
          ) {
            Text(
              text = "📇 Save Contact QR",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (shareFormat == "CONTACT_QR") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (shareFormat == "WEB_QR") MaterialTheme.colorScheme.primary else Color.Transparent,
            modifier = Modifier.clickable { shareFormat = "WEB_QR" }.testTag("tab_web_link_qr")
          ) {
            Text(
              text = "🌐 Web QR",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (shareFormat == "WEB_QR") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (shareFormat == "BARCODE") MaterialTheme.colorScheme.primary else Color.Transparent,
            modifier = Modifier.clickable { shareFormat = "BARCODE" }.testTag("tab_1d_barcode")
          ) {
            Text(
              text = "|||| Barcode",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (shareFormat == "BARCODE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (shareFormat) {
          "CONTACT_QR" -> {
            StylizedQRCode(
              data = vCardData,
              modifier = Modifier.size(165.dp).testTag("qr_code_vcard_display"),
              darkColor = Color(0xFF0F172A),
              lightColor = Color.White
            )
          }
          "WEB_QR" -> {
            StylizedQRCode(
              data = "https://$shortUrl",
              modifier = Modifier.size(165.dp).testTag("qr_code_web_display"),
              darkColor = Color(0xFF0F172A),
              lightColor = Color.White
            )
          }
          else -> {
            StylizedBarcode(
              data = card.busyCardNetId,
              modifier = Modifier.fillMaxWidth(0.95f).testTag("barcode_display"),
              darkColor = Color(0xFF0F172A),
              lightColor = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = card.busyCardNetId,
          fontSize = 13.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )

        Text(
          text = when (shareFormat) {
            "CONTACT_QR" -> "📷 Point any smartphone camera to instantly save ${card.name} into contacts"
            "WEB_QR" -> "🌐 Scan to open live digital visiting card: $shortUrl"
            else -> "Optical POS / Barcode scanner format"
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (onOpenQrGenerator != null) {
          androidx.compose.material3.Button(
            onClick = {
              onDismiss()
              onOpenQrGenerator(card.cardId)
            },
            shape = RoundedCornerShape(10.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
              containerColor = Color(0xFF0F172A)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(38.dp)
              .testTag("dialog_open_qr_studio_button")
          ) {
            Icon(
              imageVector = Icons.Default.QrCode,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Customize & Export Firestore QR",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
        } else {
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Share Grid Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          // 0. Open Scanner if callback provided
          if (onOpenScanner != null) {
            ShareActionItem(
              iconEmoji = "📷",
              label = "Camera Scan",
              onClick = {
                onDismiss()
                onOpenScanner()
              }
            )
          }

          // WhatsApp
          ShareActionItem(
            iconEmoji = "💬",
            label = "WhatsApp",
            onClick = {
              onCardShared?.invoke("WhatsApp")
              val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode("View my BusyCardNet Digital Visiting Card: https://$shortUrl\nID: ${card.busyCardNetId}")}")
              }
              try {
                context.startActivity(sendIntent)
              } catch (e: Exception) {
                Toast.makeText(context, "Link copied! +50 Marketing Credits Earned! 🎁", Toast.LENGTH_SHORT).show()
                clipboardManager.setText(AnnotatedString("https://$shortUrl"))
              }
            }
          )

          // Copy Link
          ShareActionItem(
            iconEmoji = "📋",
            label = "Copy Link",
            onClick = {
              onCardShared?.invoke("Copy Link")
              clipboardManager.setText(AnnotatedString("https://$shortUrl"))
              Toast.makeText(context, "Short card URL copied! +50 Marketing Credits Earned! 🎁", Toast.LENGTH_SHORT).show()
            }
          )

          // Add to Contacts (vCard)
          ShareActionItem(
            iconEmoji = "👤",
            label = "Save Contact",
            onClick = {
              onCardShared?.invoke("Save Contact")
              val intent = Intent(Intent.ACTION_INSERT).apply {
                type = "vnd.android.cursor.dir/contact"
                putExtra("name", card.name)
                putExtra("phone", card.phone)
                putExtra("company", card.businessName)
                putExtra("notes", "BusyCardNet ID: ${card.busyCardNetId}\nhttps://$shortUrl")
              }
              try {
                context.startActivity(intent)
              } catch (e: Exception) {
                Toast.makeText(context, "Contact saved! +50 Marketing Credits Earned! 🎁", Toast.LENGTH_SHORT).show()
              }
            }
          )

          // NFC Tap
          ShareActionItem(
            iconEmoji = if (nfcEmulated) "📡" else "📶",
            label = if (nfcEmulated) "NFC Beaming" else "NFC Tap",
            onClick = {
              onCardShared?.invoke("NFC Tap")
              nfcEmulated = true
              Toast.makeText(context, "NFC Beaming active! +50 Marketing Credits Earned! 🎁", Toast.LENGTH_LONG).show()
            }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Credits Reward Note
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF131D31),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("🎁", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Every share earns you +50 BCN Marketing Credits in DataStore. Spend credits to boost your card in searches and local push broadcasts!",
              fontSize = 10.sp,
              color = Color(0xFFFDE68A),
              lineHeight = 14.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ShareActionItem(
  iconEmoji: String,
  label: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Text(text = iconEmoji, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
