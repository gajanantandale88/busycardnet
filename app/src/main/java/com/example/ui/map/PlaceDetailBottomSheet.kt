package com.example.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.BcnCardHyperlink
import com.example.ui.components.reviews.BusinessReviewsBottomSheet
import com.example.ui.components.reviews.StarRatingDisplay
import com.example.ui.components.reviews.WriteReviewDialog
import com.example.ui.viewmodel.BusyCardNetViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DigitalCardEntity
import com.example.util.BcnLocationManager

/**
 * Bottom Sheet preview and detail card for a selected Business or Service Provider
 * found on Google Maps within the 500m radius.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaceDetailBottomSheet(
  place: LocalMapPlaceItem,
  onDismiss: () -> Unit,
  onOpenCardDetails: (DigitalCardEntity) -> Unit,
  onConnectBridge: (LocalMapPlaceItem) -> Unit,
  viewModel: BusyCardNetViewModel? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val locationManager = BcnLocationManager.getInstance(context)

  var showReviewsSheet by remember { mutableStateOf(false) }
  var showWriteReviewDialog by remember { mutableStateOf(false) }

  val allReviews by viewModel?.allReviews?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
  val ratingSummary = remember(allReviews, place) {
    viewModel?.getRatingSummary(
      businessId = place.id,
      businessName = place.name,
      busyCardNetId = place.busyCardNetId,
      defaultRating = place.rating,
      defaultReviewsCount = place.reviewsCount
    ) ?: com.example.data.model.BusinessRatingSummary(
      businessId = place.id,
      businessName = place.name,
      busyCardNetId = place.busyCardNetId,
      averageRating = place.rating,
      totalReviewsCount = place.reviewsCount,
      fiveStarCount = (place.reviewsCount * 0.8f).toInt().coerceAtLeast(1),
      fourStarCount = (place.reviewsCount * 0.2f).toInt().coerceAtLeast(1),
      threeStarCount = 0,
      twoStarCount = 0,
      oneStarCount = 0,
      reviews = emptyList()
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(12.dp)
      .testTag("place_detail_bottom_sheet"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = BorderStroke(1.5.dp, if (place.isBusiness) Color(0xFF0284C7) else Color(0xFFF59E0B)),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {

      // Row 1: Type Badge, Distance Pill, and Close Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Business or Service Provider Tag
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (place.isBusiness) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFFF59E0B).copy(alpha = 0.25f),
            border = BorderStroke(1.dp, if (place.isBusiness) Color(0xFF38BDF8) else Color(0xFFFBBF24))
          ) {
            Text(
              text = "${place.placeType.emoji} ${place.placeType.label.uppercase()}",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.5.sp,
              color = if (place.isBusiness) Color(0xFF38BDF8) else Color(0xFFFBBF24),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // 500m Radius Distance Pill
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (place.isWithin500m) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f),
            border = BorderStroke(1.dp, if (place.isWithin500m) Color(0xFF34D399) else Color(0xFF94A3B8))
          ) {
            Text(
              text = "📍 ${place.distanceMeters}m away • Within 500m",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (place.isWithin500m) Color(0xFF34D399) else Color(0xFFCBD5E1),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 2: Title, Category & Verified Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (place.isBusiness) Color(0xFF0369A1) else Color(0xFFD97706)),
          contentAlignment = Alignment.Center
        ) {
          Text(text = place.emoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = place.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified",
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(16.dp)
            )
          }

          Text(
            text = "${place.categoryTitle} • ${place.ownerOrContact}",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 3: Rating Banner, Open Status, and BCN ID
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showReviewsSheet = true }
          .testTag("open_reviews_from_place_sheet")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 7.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            StarRatingDisplay(
              rating = ratingSummary.averageRating,
              starSize = 13.dp,
              showNumeric = true
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "(${ratingSummary.totalReviewsCount} reviews)",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (place.isOpenNow) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
            ) {
              Text(
                text = if (place.isOpenNow) "🟢 OPEN NOW" else "🔴 CLOSED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (place.isOpenNow) Color(0xFF34D399) else Color(0xFFF87171),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = "★ Rate & Reviews >",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // BCN Card ID Row with Hyperlink
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "Card No: ", fontSize = 11.sp, color = Color(0xFF64748B))
          BcnCardHyperlink(
            bcnId = place.busyCardNetId,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Address & Hours
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = place.address, fontSize = 11.sp, color = Color(0xFFCBD5E1), maxLines = 1, overflow = TextOverflow.Ellipsis)
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = place.operatingHours, fontSize = 11.sp, color = Color(0xFF94A3B8))
      }

      // Services / Products chips
      if (place.servicesOrProducts.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          place.servicesOrProducts.forEach { item ->
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFF1E293B)
            ) {
              Text(
                text = item,
                fontSize = 10.sp,
                color = Color(0xFFE2E8F0),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons: Directions (Google Maps), Call, WhatsApp, Digital Card
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 1. Directions via Google Maps Navigation
        Button(
          onClick = {
            locationManager.navigateWithGoogleMaps(context, place.latitude, place.longitude)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1.2f)
            .height(42.dp)
            .testTag("action_google_maps_directions")
        ) {
          Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // 2. Call Now
        Button(
          onClick = {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
            context.startActivity(dialIntent)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("action_call_place")
        ) {
          Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // 3. WhatsApp Chat
        Button(
          onClick = {
            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${place.whatsapp}&text=Hello%20${place.name},%20I%20found%20you%20on%20BusyCardNet%20Google%20Maps!"))
            try {
              context.startActivity(waIntent)
            } catch (_: Exception) {
              Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("action_whatsapp_place")
        ) {
          Text("💬 Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Secondary Row: View BCN Digital Card & Build Bridge Alliance
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = {
            val cardEntity = DigitalCardEntity(
              cardId = place.id,
              internalUserId = place.id,
              busyCardNetId = place.busyCardNetId,
              name = place.ownerOrContact,
              designation = place.categoryTitle,
              professionCode = place.professionCode,
              businessName = place.name,
              services = place.servicesOrProducts.joinToString(", "),
              phone = place.phone,
              whatsapp = place.whatsapp,
              email = place.email,
              website = place.website,
              address = place.address,
              workingHours = place.operatingHours,
              about = place.about,
              templateStyle = if (place.isBusiness) "MODERN_DARK" else "ELECTRIC_AMBER",
              accentColorHex = if (place.isBusiness) "#0284C7" else "#F59E0B",
              isPrimary = false,
              latitude = place.latitude,
              longitude = place.longitude,
              distanceMeters = place.distanceMeters,
              isConnected = place.isConnectedBridge
            )
            onOpenCardDetails(cardEntity)
          },
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Color(0xFF38BDF8)),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("action_view_digital_card")
        ) {
          Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("View Card", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
          onClick = { onConnectBridge(place) },
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, if (place.isConnectedBridge) Color(0xFF10B981) else Color(0xFF94A3B8)),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("action_connect_bridge")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = if (place.isConnectedBridge) Color(0xFF10B981) else Color(0xFFCBD5E1),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (place.isConnectedBridge) "Connected" else "Bridge",
            fontSize = 11.sp,
            color = if (place.isConnectedBridge) Color(0xFF10B981) else Color(0xFFCBD5E1),
            fontWeight = FontWeight.SemiBold
          )
        }

        OutlinedButton(
          onClick = { showWriteReviewDialog = true },
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Color(0xFFF59E0B)),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("action_review_place")
        ) {
          Icon(
            imageVector = Icons.Default.RateReview,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Review",
            fontSize = 11.sp,
            color = Color(0xFFF59E0B),
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }

  // Dialogs & Sheets for Star Rating & Review System
  if (showReviewsSheet) {
    BusinessReviewsBottomSheet(
      businessId = place.id,
      businessName = place.name,
      busyCardNetId = place.busyCardNetId,
      ratingSummary = ratingSummary,
      onDismiss = { showReviewsSheet = false },
      onOpenWriteReview = {
        showReviewsSheet = false
        showWriteReviewDialog = true
      },
      onMarkHelpful = { reviewId ->
        viewModel?.markReviewHelpful(reviewId)
      }
    )
  }

  if (showWriteReviewDialog) {
    WriteReviewDialog(
      businessId = place.id,
      businessName = place.name,
      busyCardNetId = place.busyCardNetId,
      defaultServiceCategory = place.categoryTitle,
      onDismiss = { showWriteReviewDialog = false },
      onSubmitReview = { rating, comment, category ->
        viewModel?.submitBusinessReview(
          businessId = place.id,
          businessName = place.name,
          busyCardNetId = place.busyCardNetId,
          rating = rating,
          reviewText = comment,
          serviceCategory = category
        )
      }
    )
  }
}
