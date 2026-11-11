package com.example.ui.components.reviews

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessReviewEntity
import com.example.data.model.BusinessRatingSummary
import com.example.ui.components.BcnCardHyperlink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessReviewsBottomSheet(
  businessId: String,
  businessName: String,
  busyCardNetId: String,
  ratingSummary: BusinessRatingSummary,
  onDismiss: () -> Unit,
  onOpenWriteReview: () -> Unit,
  onMarkHelpful: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var starFilter by remember { mutableIntStateOf(0) } // 0 = All, 1..5 = filter by star
  var sortBy by remember { mutableStateOf("NEWEST") } // NEWEST, HIGHEST, HELPFUL

  val filteredReviews = remember(ratingSummary.reviews, starFilter, sortBy) {
    var list = if (starFilter > 0) {
      ratingSummary.reviews.filter { it.rating == starFilter }
    } else {
      ratingSummary.reviews
    }

    when (sortBy) {
      "HIGHEST" -> list.sortedByDescending { it.rating }
      "HELPFUL" -> list.sortedByDescending { it.helpfulCount }
      else -> list.sortedByDescending { it.timestamp }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF0F172A),
    tonalElevation = 10.dp,
    modifier = modifier.testTag("business_reviews_bottom_sheet")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 36.dp)
    ) {
      // 1. Header Bar
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Client Reviews & Ratings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified Network",
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = businessName,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF94A3B8)
            )

            if (busyCardNetId.isNotBlank()) {
              Spacer(modifier = Modifier.height(3.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Card No: ",
                  fontSize = 11.sp,
                  color = Color(0xFF64748B)
                )
                BcnCardHyperlink(
                  bcnId = busyCardNetId,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(34.dp)
              .testTag("close_reviews_sheet")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // 2. Rating Breakdown Card (Histogram)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Left: Big Rating Number & Stars
              Column(
                modifier = Modifier.weight(0.42f),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = String.format("%.1f", ratingSummary.averageRating),
                  fontSize = 38.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFFFBBF24)
                )
                StarRatingDisplay(
                  rating = ratingSummary.averageRating,
                  starSize = 15.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${ratingSummary.totalReviewsCount} verified reviews",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFF94A3B8)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              // Right: Distribution Bars (5, 4, 3, 2, 1)
              Column(
                modifier = Modifier.weight(0.58f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                RatingDistributionRow(stars = 5, pct = ratingSummary.fiveStarPct, count = ratingSummary.fiveStarCount)
                RatingDistributionRow(stars = 4, pct = ratingSummary.fourStarPct, count = ratingSummary.fourStarCount)
                RatingDistributionRow(stars = 3, pct = ratingSummary.threeStarPct, count = ratingSummary.threeStarCount)
                RatingDistributionRow(stars = 2, pct = ratingSummary.twoStarPct, count = ratingSummary.twoStarCount)
                RatingDistributionRow(stars = 1, pct = ratingSummary.oneStarPct, count = ratingSummary.oneStarCount)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // 3. Write a Review Action Bar
      item {
        Button(
          onClick = onOpenWriteReview,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("open_write_review_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
        ) {
          Icon(
            imageVector = Icons.Default.RateReview,
            contentDescription = null,
            tint = Color(0xFF0F172A),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Rate This Business & Write a Review",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // 4. Filters and Sorting Chips
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            item {
              FilterChipItem(
                label = "All (${ratingSummary.totalReviewsCount})",
                isSelected = starFilter == 0,
                onClick = { starFilter = 0 }
              )
            }
            item {
              FilterChipItem(
                label = "5 ★ (${ratingSummary.fiveStarCount})",
                isSelected = starFilter == 5,
                onClick = { starFilter = 5 }
              )
            }
            item {
              FilterChipItem(
                label = "4 ★ (${ratingSummary.fourStarCount})",
                isSelected = starFilter == 4,
                onClick = { starFilter = 4 }
              )
            }
            item {
              FilterChipItem(
                label = "3 ★ (${ratingSummary.threeStarCount})",
                isSelected = starFilter == 3,
                onClick = { starFilter = 3 }
              )
            }
            item {
              FilterChipItem(
                label = "2 ★ (${ratingSummary.twoStarCount})",
                isSelected = starFilter == 2,
                onClick = { starFilter = 2 }
              )
            }
            item {
              FilterChipItem(
                label = "1 ★ (${ratingSummary.oneStarCount})",
                isSelected = starFilter == 1,
                onClick = { starFilter = 1 }
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Sort Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${filteredReviews.size} reviews shown",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              SortChipItem(
                label = "Newest",
                isSelected = sortBy == "NEWEST",
                onClick = { sortBy = "NEWEST" }
              )
              SortChipItem(
                label = "Highest",
                isSelected = sortBy == "HIGHEST",
                onClick = { sortBy = "HIGHEST" }
              )
              SortChipItem(
                label = "Helpful",
                isSelected = sortBy == "HELPFUL",
                onClick = { sortBy = "HELPFUL" }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }

      // 5. Reviews List or Empty State
      if (filteredReviews.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.RateReview,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No reviews found for this filter",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFCBD5E1)
              )
              Text(
                text = "Be the first verified customer to share feedback!",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
        }
      } else {
        items(filteredReviews, key = { it.id }) { review ->
          ReviewItemCard(
            review = review,
            onMarkHelpful = {
              onMarkHelpful(review.id)
              Toast.makeText(context, "Marked as helpful!", Toast.LENGTH_SHORT).show()
            }
          )
          Spacer(modifier = Modifier.height(10.dp))
        }
      }
    }
  }
}

@Composable
private fun RatingDistributionRow(
  stars: Int,
  pct: Float,
  count: Int
) {
  val animatedProgress by animateFloatAsState(targetValue = pct.coerceIn(0f, 1f), label = "pct_$stars")

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "$stars★",
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFFCBD5E1),
      modifier = Modifier.width(22.dp)
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(Color(0xFF334155))
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(animatedProgress)
          .background(Color(0xFFF59E0B))
      )
    }

    Text(
      text = "$count",
      fontSize = 10.sp,
      color = Color(0xFF94A3B8),
      modifier = Modifier
        .width(26.dp)
        .padding(start = 6.dp)
    )
  }
}

@Composable
private fun ReviewItemCard(
  review: BusinessReviewEntity,
  onMarkHelpful: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("review_card_${review.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Reviewer Info & Rating
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Avatar Initial Circle
          val initial = review.reviewerName.firstOrNull()?.uppercase() ?: "U"
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
              .border(1.dp, Color(0xFF38BDF8), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = initial,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = review.reviewerName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              if (review.isVerifiedClient) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified Client",
                  tint = Color(0xFF10B981),
                  modifier = Modifier.size(13.dp)
                )
              }
            }

            if (review.reviewerBusyCardNetId.isNotBlank()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "BCN: ",
                  fontSize = 10.sp,
                  color = Color(0xFF64748B)
                )
                BcnCardHyperlink(
                  bcnId = review.reviewerBusyCardNetId,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // Relative Time
        val relativeTime = remember(review.timestamp) {
          try {
            DateUtils.getRelativeTimeSpanString(
              review.timestamp,
              System.currentTimeMillis(),
              DateUtils.MINUTE_IN_MILLIS
            ).toString()
          } catch (_: Exception) {
            "Recently"
          }
        }

        Text(
          text = relativeTime,
          fontSize = 10.sp,
          color = Color(0xFF64748B)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Stars & Category Tag
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        StarRatingDisplay(
          rating = review.rating.toFloat(),
          starSize = 13.dp,
          showNumeric = true
        )

        if (review.serviceCategory.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF334155)
          ) {
            Text(
              text = review.serviceCategory,
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF93C5FD),
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Review Text
      Text(
        text = review.reviewText,
        fontSize = 12.sp,
        color = Color(0xFFE2E8F0),
        lineHeight = 17.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Bottom Row: Helpful button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0F172A),
          modifier = Modifier
            .clickable(onClick = onMarkHelpful)
            .testTag("helpful_button_${review.id}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ThumbUp,
              contentDescription = "Helpful",
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (review.helpfulCount > 0) "Helpful (${review.helpfulCount})" else "Helpful",
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFFCBD5E1)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FilterChipItem(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B),
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
    )
  }
}

@Composable
private fun SortChipItem(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) Color(0xFF334155) else Color.Transparent,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
    )
  }
}
