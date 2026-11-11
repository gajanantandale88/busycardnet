package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.map.LocalPlaceDataProvider

/**
 * Simple Input Form to broadcast a service request with explicit
 * 500m, 1000m, 2000m, 5000m, 10000m radius options, storing details
 * in Firebase Firestore to notify nearby business members.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleServiceBroadcastDialog(
  userLocality: String = "Kurla West, Mumbai",
  onDismiss: () -> Unit,
  onBroadcastSubmit: (
    title: String,
    category: String,
    categoryCode: String,
    description: String,
    budget: String,
    urgency: String,
    radiusMeters: Int
  ) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Plumbing & Sanitary") }
  var selectedCategoryCode by remember { mutableStateOf("PLUM") }
  var selectedRadiusMeters by remember { mutableIntStateOf(500) }
  var budget by remember { mutableStateOf("Negotiable") }
  var urgency by remember { mutableStateOf("URGENT") }
  var isSubmitting by remember { mutableStateOf(false) }

  // Available radii: 500m, 1000m, 2000m, 5000m, 10000m
  val radiusOptions = listOf(
    500 to "500m",
    1000 to "1000m",
    2000 to "2000m",
    5000 to "5000m",
    10000 to "10000m"
  )

  // Categories list
  val categories = listOf(
    Triple("Plumbing & Sanitary", "PLUM", "🔧"),
    Triple("Electrician & Wiring", "ELEC", "⚡"),
    Triple("Carpentry & Furniture", "CARP", "🔨"),
    Triple("AC & Appliances", "APPL", "❄️"),
    Triple("Deep Cleaning & Maid", "CLEN", "🧹"),
    Triple("Courier & Local Delivery", "DELV", "📦"),
    Triple("IT, Computer & Tech", "TECH", "💻"),
    Triple("Painting & Waterproofing", "PNTG", "🎨"),
    Triple("General Handyman", "HAND", "🛠️")
  )

  // Quick title suggestion presets
  val quickPresets = listOf(
    "Urgent Pipe Leak Repair" to "PLUM",
    "Power Trip & MCB Fix" to "ELEC",
    "AC Gas & Cooling Service" to "APPL",
    "Door Lock & Hinge Repair" to "CARP",
    "Express Parcel Delivery" to "DELV",
    "Full House Deep Cleaning" to "CLEN"
  )

  // Query curated nearby businesses in chosen radius
  val allPlaces = remember { LocalPlaceDataProvider.getCuratedPlaces() }
  val nearbyPlaces = remember(selectedRadiusMeters) {
    LocalPlaceDataProvider.filterPlacesWithinRadius(allPlaces, selectedRadiusMeters)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .clip(RoundedCornerShape(24.dp))
        .testTag("simple_service_broadcast_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = Color(0xFF2563EB),
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Radar,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            Column {
              Text(
                text = "Broadcast Service Request",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Nearby Radius • Cloud Firestore Alerts",
                fontSize = 11.sp,
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_simple_broadcast_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Firestore Storage Banner
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
          border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CloudDone,
              contentDescription = null,
              tint = Color(0xFF1D4ED8),
              modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Stored in Firebase Firestore (`service_requests`)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
              )
              Text(
                text = "Details will sync to cloud to notify verified business members in ${userLocality}.",
                fontSize = 10.sp,
                color = Color(0xFF1E40AF)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Service Title Input
        Text(
          text = "WHAT SERVICE DO YOU NEED?",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("e.g. Kitchen tap leaking, AC gas refill needed", fontSize = 13.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("simple_broadcast_title_input"),
          shape = RoundedCornerShape(12.dp),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        // Quick Suggestion Chips
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          quickPresets.forEach { (presetTitle, catCode) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  title = presetTitle
                  val match = categories.find { it.second == catCode }
                  if (match != null) {
                    selectedCategory = match.first
                    selectedCategoryCode = match.second
                  }
                }
            ) {
              Text(
                text = presetTitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Category Selector
        Text(
          text = "CATEGORY / TRADE",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.forEach { (catName, catCode, emoji) ->
            val isSelected = selectedCategoryCode == catCode
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedCategory = catName
                selectedCategoryCode = catCode
              },
              label = {
                Text(
                  text = "$emoji $catName",
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              shape = RoundedCornerShape(8.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.testTag("category_chip_$catCode")
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Proximity Radius (500m, 1000m, 2000m, 5000m, 10000m)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "BROADCAST RADIUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Target: ${selectedRadiusMeters}m",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))

        // 5 Buttons in a clean grid/row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          radiusOptions.forEach { (radius, label) ->
            val isSelected = selectedRadiusMeters == radius
            val isHero500 = radius == 500

            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(
                containerColor = when {
                  isSelected && isHero500 -> Color(0xFF1E3A8A)
                  isSelected -> MaterialTheme.colorScheme.primaryContainer
                  else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
              ),
              border = BorderStroke(
                1.5.dp,
                when {
                  isSelected && isHero500 -> Color(0xFF60A5FA)
                  isSelected -> MaterialTheme.colorScheme.primary
                  else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }
              ),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedRadiusMeters = radius }
                .testTag("radius_chip_$radius")
            ) {
              Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                if (isHero500) {
                  Text(
                    text = "★ FAST",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color(0xFFFDE047) else Color(0xFFF59E0B)
                  )
                }
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                  color = if (isSelected && isHero500) Color.White else if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Nearby Business Member Alert Preview
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "${nearbyPlaces.size} nearby business members within ${selectedRadiusMeters}m",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
            }

            if (nearbyPlaces.isNotEmpty()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Businesses receiving notification: " + nearbyPlaces.take(3).joinToString { "${it.name} (${it.distanceMeters}m)" } + if (nearbyPlaces.size > 3) " +${nearbyPlaces.size - 3} more" else "",
                fontSize = 10.sp,
                color = Color(0xFF475569),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Problem Description Input
        Text(
          text = "PROBLEM DETAILS & REQUIREMENTS",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          placeholder = { Text("Describe the work, timing, or materials required...", fontSize = 12.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .testTag("simple_broadcast_description_input"),
          shape = RoundedCornerShape(12.dp),
          maxLines = 4
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Budget & Urgency Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Budget Column
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "BUDGET (EST.)",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
              value = budget,
              onValueChange = { budget = it },
              singleLine = true,
              placeholder = { Text("e.g. ₹500", fontSize = 12.sp) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )
          }

          // Urgency Column
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "URGENCY",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              listOf("URGENT" to "⚡ Urgent", "STANDARD" to "⏱ Today").forEach { (u, label) ->
                val isSelected = urgency == u
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { urgency = u }
                ) {
                  Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                  )
                }
              }
            }
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
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              if (title.isBlank()) {
                title = "$selectedCategory Service Request"
              }
              if (description.isBlank()) {
                description = "Need verified $selectedCategory provider within ${selectedRadiusMeters}m."
              }
              isSubmitting = true
              onBroadcastSubmit(
                title,
                selectedCategory,
                selectedCategoryCode,
                description,
                budget,
                urgency,
                selectedRadiusMeters
              )
            },
            modifier = Modifier
              .weight(2f)
              .testTag("submit_simple_broadcast_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            enabled = !isSubmitting
          ) {
            if (isSubmitting) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Broadcasting...", color = Color.White)
            } else {
              Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "Broadcast ${selectedRadiusMeters}m",
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
