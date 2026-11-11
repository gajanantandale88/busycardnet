package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ProfessionCategoryEntity
import com.example.data.local.ServiceRequestEntity
import com.example.ui.components.BcnCardHyperlink

/**
 * 500-Meter Service Request Banner (Prompt Sections 25-29)
 */
@Composable
fun UrgentServiceRequestBanner(
  modifier: Modifier = Modifier,
  onRequestClick: () -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onRequestClick() }
      .testTag("urgent_service_request_banner"),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Box(
      modifier = Modifier
        .background(
          Brush.horizontalGradient(
            colors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE), Color(0xFFFFFFFF))
          )
        )
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color(0xFFDBEAFE))
              .border(1.5.dp, Color(0xFF2563EB), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FlashOn,
              contentDescription = "Urgent Request",
              tint = Color(0xFF2563EB),
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "500-METER INSTANT SERVICE",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1D4ED8),
                letterSpacing = 0.8.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "⚡ FAST",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706)
              )
            }
            Text(
              text = "Need an Electrician or Plumber?",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF172554)
            )
            Text(
              text = "Dispatch alert to professionals nearby (30 min response)",
              fontSize = 11.sp,
              color = Color(0xFF1E3A8A)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF2563EB),
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Text(
            text = "Request Now",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
          )
        }
      }
    }
  }
}

/**
 * Incoming Request Notification for Professionals (Prompt Section 26)
 */
@Composable
fun ProfessionalNotificationBanner(
  request: ServiceRequestEntity,
  onInterestedClick: () -> Unit,
  onDeclineClick: () -> Unit,
  onViewDetailsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.2.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
      .testTag("professional_notification_banner"),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = null,
            tint = Color(0xFFD97706),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "⚡ NEW SERVICE REQUEST",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF92400E)
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFFEF3C7),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D))
        ) {
          Text(
            text = "📍 ${request.distanceMeters}m away",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB45309),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "\"${request.problemDescription}\"",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF172554)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Requester: ${request.requesterName}",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF1E3A8A)
        )
      }
      BcnCardHyperlink(
        bcnId = request.requesterBusyCardNetId,
        color = Color(0xFF1D4ED8),
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 1.dp, bottom = 4.dp)
      )

      if (request.photoAttached) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AddAPhoto,
            contentDescription = null,
            tint = Color(0xFF0284C7),
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Photo of issue attached by ${request.requesterName}",
            fontSize = 10.sp,
            color = Color(0xFF1E3A8A)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onInterestedClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Text("I'M INTERESTED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedButton(
          onClick = onDeclineClick,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Text("NOT AVAILABLE", fontSize = 11.sp, color = Color(0xFF1E3A8A))
        }
      }
    }
  }
}

/**
 * Create Service Request Modal Dialog (Proximity Broadcast to 500m businesses)
 */
@Composable
fun CreateServiceRequestDialog(
  professions: List<ProfessionCategoryEntity>,
  initialProfessionCode: String = "ELEC",
  onDismiss: () -> Unit,
  onSubmit: (professionCode: String, description: String, photoAttached: Boolean, radius: Int) -> Unit
) {
  ProximityServiceBroadcastDialog(
    initialProfessionCode = initialProfessionCode,
    onDismiss = onDismiss,
    onBroadcastSubmit = { _, _, categoryCode, desc, _, _, _, _, radius, photo, _ ->
      onSubmit(categoryCode, desc, photo, radius)
    }
  )
}
