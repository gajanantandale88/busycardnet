package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CardCornerStyle
import com.example.data.model.CardPrintOptions
import com.example.util.StylizedBarcode
import com.example.util.StylizedQRCode

/**
 * High-Fidelity Visiting Card Physical Print Preview.
 * Supports Front & Back views with tactile lighting, realistic textures, and live element toggles.
 */
@Composable
fun VisitingCardPrintPreview(
  options: CardPrintOptions,
  modifier: Modifier = Modifier,
  onFlipCard: (() -> Unit)? = null
) {
  var isShowingFront by remember { mutableStateOf(true) }

  val cornerRadius = when (options.cornerStyle) {
    CardCornerStyle.SQUARE -> 0.dp
    CardCornerStyle.ROUNDED_3MM -> 12.dp
    CardCornerStyle.ROUNDED_6MM -> 20.dp
  }

  val primaryBg = Color(android.graphics.Color.parseColor(options.theme.primaryBgHex))
  val secondaryBg = Color(android.graphics.Color.parseColor(options.theme.secondaryBgHex))
  val textPrimary = Color(android.graphics.Color.parseColor(options.theme.textColorHex))
  val accentColor = Color(android.graphics.Color.parseColor(options.theme.accentColorHex))

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Controls Header: Front / Back Toggle
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.clip(RoundedCornerShape(20.dp))
        ) {
          Row(modifier = Modifier.padding(2.dp)) {
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = if (isShowingFront) MaterialTheme.colorScheme.primary else Color.Transparent,
              modifier = Modifier
                .clickable { isShowingFront = true }
                .testTag("preview_side_front_tab")
            ) {
              Text(
                text = "FRONT SIDE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isShowingFront) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(18.dp),
              color = if (!isShowingFront) MaterialTheme.colorScheme.primary else Color.Transparent,
              modifier = Modifier
                .clickable { isShowingFront = false }
                .testTag("preview_side_back_tab")
            ) {
              Text(
                text = "BACK SIDE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isShowingFront) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // Tap to Flip Prompt
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable {
            isShowingFront = !isShowingFront
            onFlipCard?.invoke()
          }
          .padding(4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Flip,
          contentDescription = "Flip Card",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Tap to Flip",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 2. The Physical Card Surface (Standard Business Card 1.75:1 ratio, 3.5" x 2.0")
    Card(
      shape = RoundedCornerShape(cornerRadius),
      colors = CardDefaults.cardColors(containerColor = primaryBg),
      border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.55f)),
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.75f)
        .shadow(
          elevation = 12.dp,
          shape = RoundedCornerShape(cornerRadius),
          spotColor = accentColor.copy(alpha = 0.35f)
        )
        .clickable { isShowingFront = !isShowingFront }
        .testTag("interactive_visiting_card_canvas")
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.linearGradient(
              colors = listOf(
                primaryBg,
                secondaryBg,
                primaryBg.copy(alpha = 0.95f)
              )
            )
          )
          .padding(14.dp)
      ) {
        if (isShowingFront) {
          // ================= FRONT SIDE =================
          CardFrontContent(
            options = options,
            textPrimary = textPrimary,
            accentColor = accentColor
          )
        } else {
          // ================= BACK SIDE =================
          CardBackContent(
            options = options,
            textPrimary = textPrimary,
            accentColor = accentColor
          )
        }

        // Subtly visible watermark of BCN verified standard
        Text(
          text = "ISO 7810 / CR80 CARD SPECIFICATION",
          fontSize = 6.5.sp,
          fontFamily = FontFamily.Monospace,
          color = textPrimary.copy(alpha = 0.2f),
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(2.dp)
        )
      }
    }
  }
}

@Composable
private fun CardFrontContent(
  options: CardPrintOptions,
  textPrimary: Color,
  accentColor: Color
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Row: Photo / Logo + Company Name & Words
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        if (options.includeCompanyName && options.companyName.isNotBlank()) {
          Text(
            text = options.companyName.uppercase(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (options.includeCompanyWords && options.companyWords.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "\"${options.companyWords}\"",
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = textPrimary.copy(alpha = 0.85f),
            lineHeight = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Profile Photo / Logo (if selected)
      if (options.includePhoto) {
        Spacer(modifier = Modifier.width(10.dp))
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .border(1.5.dp, accentColor, CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          if (options.photoUrl.isNotBlank()) {
            AsyncImage(
              model = options.photoUrl,
              contentDescription = "Card Photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    }

    // Middle Row: Name & BCN ID with Security Tag
    Column(modifier = Modifier.fillMaxWidth()) {
      if (options.includeName && options.name.isNotBlank()) {
        Text(
          text = options.name,
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          color = textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (options.includeBcnId && options.bcnId.isNotBlank()) {
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = accentColor.copy(alpha = 0.2f),
            border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.5f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(10.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = options.bcnId,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor
              )
            }
          }
        }
      }
    }

    // Bottom Contact Details Matrix (Phone, WhatsApp, Email, Website)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        if (options.includePhone && options.phone.isNotBlank()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = accentColor, modifier = Modifier.size(9.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = options.phone, fontSize = 8.5.sp, color = textPrimary.copy(alpha = 0.9f), maxLines = 1)
          }
        }

        if (options.includeWhatsApp && options.whatsApp.isNotBlank()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF25D366)),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "W", fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = options.whatsApp, fontSize = 8.5.sp, color = textPrimary.copy(alpha = 0.9f), maxLines = 1)
          }
        }
      }

      Column(
        modifier = Modifier.weight(1.2f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.End
      ) {
        if (options.includeEmail && options.email.isNotBlank()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = accentColor, modifier = Modifier.size(9.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = options.email, fontSize = 8.sp, color = textPrimary.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
        }

        if (options.includeWebsite && options.website.isNotBlank()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = accentColor, modifier = Modifier.size(9.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = options.website, fontSize = 8.sp, color = textPrimary.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
        }
      }
    }
  }
}

@Composable
private fun CardBackContent(
  options: CardPrintOptions,
  textPrimary: Color,
  accentColor: Color
) {
  Row(
    modifier = Modifier.fillMaxSize(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left Column: Scannable Barcode / QR Code
    if (options.includeBarcode) {
      Column(
        modifier = Modifier.weight(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        StylizedQRCode(
          data = "https://busycardnet.in/card/${options.bcnId}",
          modifier = Modifier.size(68.dp),
          darkColor = Color(0xFF0F172A),
          lightColor = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "SCAN TO CONNECT",
          fontSize = 7.5.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          color = accentColor
        )
      }

      Spacer(modifier = Modifier.width(12.dp))
    }

    // Right Column: Social Links, Address & Barcode Strip
    Column(
      modifier = Modifier.weight(1.3f),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "CONNECT & ENGAGE",
          fontSize = 9.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.8.sp,
          color = accentColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Social handles: Facebook, Instagram
        if (options.includeInstagram && options.instagram.isNotBlank()) {
          Text(
            text = "Instagram: ${options.instagram}",
            fontSize = 8.sp,
            color = textPrimary.copy(alpha = 0.85f),
            maxLines = 1
          )
        }

        if (options.includeFacebook && options.facebook.isNotBlank()) {
          Text(
            text = "Facebook: ${options.facebook}",
            fontSize = 8.sp,
            color = textPrimary.copy(alpha = 0.85f),
            maxLines = 1
          )
        }

        if (options.includeAddress && options.address.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = options.address,
              fontSize = 7.5.sp,
              color = textPrimary.copy(alpha = 0.8f),
              maxLines = 2,
              lineHeight = 10.sp
            )
          }
        }
      }

      // Barcode strip across card bottom if BCN ID present
      if (options.includeBarcode && options.bcnId.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        StylizedBarcode(
          data = options.bcnId,
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
          darkColor = Color(0xFF0F172A),
          lightColor = Color.White,
          showText = false
        )
      }
    }
  }
}
