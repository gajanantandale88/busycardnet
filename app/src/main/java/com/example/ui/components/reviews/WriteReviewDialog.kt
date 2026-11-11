package com.example.ui.components.reviews

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.BcnCardHyperlink

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteReviewDialog(
  businessId: String,
  businessName: String,
  busyCardNetId: String,
  defaultServiceCategory: String = "General Service",
  onDismiss: () -> Unit,
  onSubmitReview: (rating: Int, comment: String, serviceCategory: String) -> Unit
) {
  val context = LocalContext.current
  var selectedRating by remember { mutableIntStateOf(5) }
  var reviewText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(defaultServiceCategory.ifBlank { "General Service" }) }

  val serviceCategories = listOf(
    "General Service",
    "Emergency Repair",
    "Installation",
    "On-site Inspection",
    "Consultation",
    "Maintenance AMC",
    "Fair Pricing"
  )

  val ratingSentiment = when (selectedRating) {
    5 -> "⭐⭐⭐⭐⭐ Outstanding (Highly Recommended)"
    4 -> "⭐⭐⭐⭐ Very Good (Satisfied)"
    3 -> "⭐⭐⭐ Average (Acceptable)"
    2 -> "⭐⭐ Below Expectations"
    else -> "⭐ Disappointed (Poor Experience)"
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(vertical = 24.dp)
        .testTag("write_review_dialog"),
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF0F172A),
      shadowElevation = 16.dp,
      tonalElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.RateReview,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Rate & Review",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = businessName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF38BDF8)
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("close_review_dialog")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8)
            )
          }
        }

        if (busyCardNetId.isNotBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "BCN Number: ",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
            BcnCardHyperlink(
              bcnId = busyCardNetId,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Star Rating Selection
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Tap to Rate Your Experience",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            InteractiveStarRatingBar(
              rating = selectedRating,
              onRatingChanged = { selectedRating = it },
              starSize = 36.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = ratingSentiment,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFFBBF24),
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Service Category Chips
        Text(
          text = "Service or Engagement Type",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          serviceCategories.forEach { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
              modifier = Modifier
                .clickable { selectedCategory = cat }
                .testTag("service_category_chip_$cat")
            ) {
              Text(
                text = cat,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Review Text Field
        Text(
          text = "Share Your Honest Review",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = reviewText,
          onValueChange = { if (it.length <= 500) reviewText = it },
          placeholder = {
            Text(
              "Describe service quality, prompt arrival, technical expertise, transparency in pricing, etc.",
              fontSize = 12.sp,
              color = Color(0xFF64748B)
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .testTag("review_input_field"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color(0xFF1E293B),
            unfocusedContainerColor = Color(0xFF1E293B),
            focusedBorderColor = Color(0xFF38BDF8),
            unfocusedBorderColor = Color(0xFF334155)
          )
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = "${reviewText.length}/500",
            fontSize = 10.sp,
            color = Color(0xFF64748B)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trust & Verification Note
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Verified Reviewer: Published with your verified BCN network identity to ensure high trust for local discovery.",
              fontSize = 10.sp,
              color = Color(0xFF94A3B8),
              lineHeight = 14.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("cancel_review_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
          ) {
            Text("Cancel", fontSize = 13.sp)
          }

          Button(
            onClick = {
              if (reviewText.isBlank()) {
                Toast.makeText(context, "Please write a brief comment about your experience.", Toast.LENGTH_SHORT).show()
                return@Button
              }
              onSubmitReview(selectedRating, reviewText.trim(), selectedCategory)
              Toast.makeText(context, "Thank you! Your verified review has been published.", Toast.LENGTH_SHORT).show()
              onDismiss()
            },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("submit_review_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
          ) {
            Text(
              "Post Review",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
          }
        }
      }
    }
  }
}
