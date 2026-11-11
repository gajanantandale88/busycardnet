package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.SearchIntentResult

/**
 * Intelligent Smart Search Intent Card.
 *
 * When a user types a problem (e.g. "lights are of", "power issue", "pipe leaking"),
 * this component explains the detected issue and explicitly tells the user:
 * "You require: Electrician ⚡" with one-touch filtering for professionals in the radius.
 */
@Composable
fun SmartSearchIntentBanner(
  intent: SearchIntentResult,
  radiusLabel: String = "500m",
  onSelectProfession: (professionCode: String) -> Unit,
  onOpenMap: (() -> Unit)? = null,
  onRequestService: ((professionCode: String) -> Unit)? = null,
  onDismiss: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("smart_search_intent_banner"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF0F172A)
    ),
    border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF0F172A),
              Color(0xFF1E293B)
            )
          )
        )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {

        // 1. Top Badges & Dismiss Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFF38BDF8).copy(alpha = 0.2f),
              border = BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "SMART SEARCH ASSISTANT",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.5.sp,
                  color = Color(0xFF38BDF8)
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFF10B981).copy(alpha = 0.18f)
            ) {
              Text(
                text = "📍 $radiusLabel radius",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34D399),
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          if (onDismiss != null) {
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Main Diagnosis: "You require: Electrician ⚡"
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
              .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = intent.iconEmoji,
              fontSize = 22.sp
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "You require an ${intent.requiredProfessionTitle}",
              fontSize = 17.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
            Text(
              text = intent.detectedIssue,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF38BDF8)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Natural Language Explanation
        Text(
          text = intent.explanation,
          fontSize = 12.sp,
          color = Color(0xFFCBD5E1),
          lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Action Buttons (Find in Radius, View on Map, Post Request)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Primary Action: Lock category & Find in configured radius
          Button(
            onClick = { onSelectProfession(intent.requiredProfessionCode) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            modifier = Modifier
              .weight(1.3f)
              .height(38.dp)
              .testTag("intent_find_profession_btn")
          ) {
            Icon(
              imageVector = Icons.Default.NearMe,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Find ${intent.requiredProfessionTitle} ($radiusLabel)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
          }

          if (onOpenMap != null) {
            OutlinedButton(
              onClick = onOpenMap,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, Color(0xFF38BDF8)),
              modifier = Modifier
                .weight(0.9f)
                .height(38.dp)
                .testTag("intent_view_map_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Map",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
              )
            }
          }

          if (onRequestService != null) {
            Button(
              onClick = { onRequestService(intent.requiredProfessionCode) },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .testTag("intent_request_service_btn")
            ) {
              Icon(
                imageVector = Icons.Default.PostAdd,
                contentDescription = null,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Post Task",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
