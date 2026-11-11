package com.example.ui.components.reviews

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val StarGold = Color(0xFFF59E0B)
val StarEmpty = Color(0xFF475569)

/**
 * Compact read-only star rating display (e.g. for cards or summaries).
 */
@Composable
fun StarRatingDisplay(
  rating: Float,
  maxStars: Int = 5,
  starSize: Dp = 14.dp,
  showNumeric: Boolean = false,
  textColor: Color = Color.White,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    for (i in 1..maxStars) {
      val isFull = rating >= i
      val isHalf = !isFull && rating >= (i - 0.5f)

      when {
        isFull -> Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = StarGold,
          modifier = Modifier.size(starSize)
        )
        isHalf -> Icon(
          imageVector = Icons.Default.StarHalf,
          contentDescription = null,
          tint = StarGold,
          modifier = Modifier.size(starSize)
        )
        else -> Icon(
          imageVector = Icons.Outlined.Star,
          contentDescription = null,
          tint = StarEmpty,
          modifier = Modifier.size(starSize)
        )
      }
    }

    if (showNumeric) {
      Text(
        text = String.format("%.1f", rating),
        fontSize = (starSize.value * 0.9f).sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier.padding(start = 4.dp)
      )
    }
  }
}

/**
 * Interactive star rating selector (1-5 stars) for writing reviews.
 * Minimum touch target of 48dp for accessibility compliance.
 */
@Composable
fun InteractiveStarRatingBar(
  rating: Int,
  onRatingChanged: (Int) -> Unit,
  modifier: Modifier = Modifier,
  starSize: Dp = 38.dp,
  maxStars: Int = 5
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (starIndex in 1..maxStars) {
      val isSelected = starIndex <= rating
      val starColor by animateColorAsState(
        targetValue = if (isSelected) StarGold else Color(0xFF64748B),
        label = "star_color_$starIndex"
      )

      Box(
        modifier = Modifier
          .size(48.dp)
          .clickable(
            role = Role.RadioButton,
            onClick = { onRatingChanged(starIndex) }
          )
          .testTag("star_rate_button_$starIndex"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.Star,
          contentDescription = "Rate $starIndex stars out of $maxStars",
          tint = starColor,
          modifier = Modifier.size(starSize)
        )
      }
    }
  }
}
