package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ProfessionCategoryEntity
import com.example.data.local.ServiceRequestEntity
import com.example.data.model.ServiceRequestCategory
import kotlinx.coroutines.delay

/**
 * Trade Category Definition with preset sample tasks for one-tap auto-fill
 */
data class BroadcastCategoryPreset(
  val code: String,
  val displayName: String,
  val emoji: String,
  val accentColor: Color,
  val estimatedNearbyPros: Int,
  val quickTasks: List<QuickTaskPreset>
)

data class QuickTaskPreset(
  val title: String,
  val description: String,
  val defaultBudget: String,
  val tag: String
)

object BroadcastPresetCatalog {
  val categories = listOf(
    BroadcastCategoryPreset(
      code = "PLUM",
      displayName = "Plumbing",
      emoji = "🔧",
      accentColor = Color(0xFF0284C7),
      estimatedNearbyPros = 14,
      quickTasks = listOf(
        QuickTaskPreset(
          title = "Kitchen Pipe Leakage Under Sink",
          description = "Water leaking under the kitchen sink connector. Need plumber with Teflon tape & standard wrench.",
          defaultBudget = "₹350 - ₹600",
          tag = "Leakage"
        ),
        QuickTaskPreset(
          title = "Bathroom Tap Replacement",
          description = "Existing brass tap is loose and dripping constantly. Need replacement installed.",
          defaultBudget = "₹250 - ₹450",
          tag = "Tap Fix"
        ),
        QuickTaskPreset(
          title = "Clogged Floor Drain / Waste Pipe",
          description = "Bathroom drainage is backed up. Requires drain cleaner tool / snake wire clear.",
          defaultBudget = "₹400 - ₹800",
          tag = "Drainage"
        ),
        QuickTaskPreset(
          title = "Geyser / Water Heater Inlet Fix",
          description = "Hot water pipe connection is dripping slowly. Need inlet valve tightened.",
          defaultBudget = "₹500 - ₹900",
          tag = "Geyser"
        )
      )
    ),
    BroadcastCategoryPreset(
      code = "ELEC",
      displayName = "Electrical",
      emoji = "⚡",
      accentColor = Color(0xFFEAB308),
      estimatedNearbyPros = 18,
      quickTasks = listOf(
        QuickTaskPreset(
          title = "Switchboard Sparking / Fuse Blown",
          description = "Main kitchen switchboard sparked when refrigerator turned on. Circuit needs urgent inspection.",
          defaultBudget = "₹300 - ₹700",
          tag = "Sparking"
        ),
        QuickTaskPreset(
          title = "MCB Tripping Constantly",
          description = "Breaker trips every 10 minutes when AC turns on. Possible neutral overload or short circuit.",
          defaultBudget = "₹450 - ₹850",
          tag = "MCB Tripping"
        ),
        QuickTaskPreset(
          title = "Ceiling Fan Regulator / Capacitor Fix",
          description = "Living room ceiling fan is running at dead slow speed. Needs capacitor replacement.",
          defaultBudget = "₹200 - ₹400",
          tag = "Fan Repair"
        ),
        QuickTaskPreset(
          title = "Emergency Power Inverter Check",
          description = "Inverter not switching over during power cut. Need battery connection and fuse checked.",
          defaultBudget = "₹500 - ₹1,200",
          tag = "Inverter"
        )
      )
    ),
    BroadcastCategoryPreset(
      code = "CARP",
      displayName = "Carpentry",
      emoji = "🪚",
      accentColor = Color(0xFFD97706),
      estimatedNearbyPros = 9,
      quickTasks = listOf(
        QuickTaskPreset(
          title = "Main Door Lock Jammed / Broken Latch",
          description = "Main door cylindrical lock is jammed from outside. Need carpenter to unlock and replace latch.",
          defaultBudget = "₹400 - ₹750",
          tag = "Door Lock"
        ),
        QuickTaskPreset(
          title = "Wardrobe Hydraulic Hinge Repair",
          description = "Two cabinet doors are sagging and won't shut. Need soft-close hinges aligned.",
          defaultBudget = "₹350 - ₹600",
          tag = "Hinges"
        ),
        QuickTaskPreset(
          title = "Wooden Bed Frame Creak / Reinforcement",
          description = "King size bed frame slat is cracked and squeaking loudly. Need bracket reinforcement.",
          defaultBudget = "₹500 - ₹1,000",
          tag = "Furniture"
        )
      )
    ),
    BroadcastCategoryPreset(
      code = "AC",
      displayName = "AC & Appliances",
      emoji = "❄️",
      accentColor = Color(0xFF06B6D4),
      estimatedNearbyPros = 12,
      quickTasks = listOf(
        QuickTaskPreset(
          title = "Split AC Not Cooling / Gas Refill",
          description = "1.5 ton split AC outdoor unit running but air isn't cold. Need pressure test and gas recharge.",
          defaultBudget = "₹800 - ₹2,200",
          tag = "AC Cooling"
        ),
        QuickTaskPreset(
          title = "Washing Machine Drum Not Spinning",
          description = "Front load washing machine powers on and fills water, but drum won't spin. Motor belt check.",
          defaultBudget = "₹500 - ₹1,400",
          tag = "Appliance"
        ),
        QuickTaskPreset(
          title = "Refrigerator Cooling Coil Overheating",
          description = "Double-door fridge compressor runs hot and lower compartment is warm. Need technician today.",
          defaultBudget = "₹600 - ₹1,500",
          tag = "Fridge"
        )
      )
    ),
    BroadcastCategoryPreset(
      code = "CLEAN",
      displayName = "Cleaning & Painting",
      emoji = "🧹",
      accentColor = Color(0xFF10B981),
      estimatedNearbyPros = 11,
      quickTasks = listOf(
        QuickTaskPreset(
          title = "Emergency Water Soak Extraction",
          description = "Water seeped into hallway during rain. Need wet vacuum machine and drying assistance.",
          defaultBudget = "₹600 - ₹1,200",
          tag = "Water Soak"
        ),
        QuickTaskPreset(
          title = "Bathroom Deep Acid Cleaning",
          description = "Two bathrooms need hard water scale removal and tile joint scrubbing.",
          defaultBudget = "₹700 - ₹1,300",
          tag = "Deep Clean"
        ),
        QuickTaskPreset(
          title = "Balcony Wall Waterproof Touchup",
          description = "Paint peeling due to moisture dampness. Need scraper, primer and one coat touchup.",
          defaultBudget = "₹1,200 - ₹2,500",
          tag = "Painting"
        )
      )
    ),
    BroadcastCategoryPreset(
      code = "TECH",
      displayName = "Tech & Freelance",
      emoji = "💻",
      accentColor = Color(0xFF8B5CF6),
      estimatedNearbyPros = 16,
      quickTasks = listOf(
        QuickTaskPreset(
          title = "Laptop Display Flickering / SSD Upgrade",
          description = "Dell laptop display blacking out intermittently. Need local technician to test ribbon cable.",
          defaultBudget = "₹500 - ₹1,500",
          tag = "Hardware"
        ),
        QuickTaskPreset(
          title = "Office WiFi Router Mesh Setup",
          description = "Need secondary router configured as bridge with Cat6 cabling to back office.",
          defaultBudget = "₹600 - ₹1,200",
          tag = "Networking"
        ),
        QuickTaskPreset(
          title = "Urgent Android / Web Bug Fix",
          description = "Looking for local freelance developer for immediate 2-hour debugging session.",
          defaultBudget = "₹1,500 - ₹3,500",
          tag = "Dev Gig"
        )
      )
    ),
    BroadcastCategoryPreset(
      code = "HANDY",
      displayName = "Handyman",
      emoji = "🛠️",
      accentColor = Color(0xFFF97316),
      estimatedNearbyPros = 15,
      quickTasks = listOf(
        QuickTaskPreset(
          title = "Curtain Rod & Wall Mirror Mounting",
          description = "Need hammer drill to mount 2 heavy mirrors and 3 curtain brackets into concrete wall.",
          defaultBudget = "₹350 - ₹650",
          tag = "Drilling"
        ),
        QuickTaskPreset(
          title = "General Fix-it Assistance",
          description = "Assortment of small tasks: door stopper, loose screws, picture frames, and shower curtain.",
          defaultBudget = "₹400 - ₹800",
          tag = "Handyman"
        )
      )
    )
  )
}

/**
 * State of the Broadcast Flow
 */
enum class BroadcastFlowStep {
  FORM,             // Filling out the request
  RADAR_DISPATCH,   // Pinging nearby businesses with animated radar
  CONFIRMED         // Active broadcast HUD with expiry countdown
}

/**
 * High-craft Service Request Proximity Broadcast Dialog.
 *
 * Directly empowers users to broadcast a task or service request to businesses
 * within their immediate 500m proximity (or expandable to 1-5km).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProximityServiceBroadcastDialog(
  initialProfessionCode: String = "PLUM",
  userLocality: String = "Andheri East, Sector 4, Mumbai",
  onDismiss: () -> Unit,
  onBroadcastSubmit: (
    title: String,
    category: String,
    categoryCode: String,
    description: String,
    budget: String,
    urgency: String,
    serviceMode: String,
    tags: String,
    radiusMeters: Int,
    photoAttached: Boolean,
    customLocality: String
  ) -> Unit
) {
  val focusManager = LocalFocusManager.current

  // State Management
  var currentStep by remember { mutableStateOf(BroadcastFlowStep.FORM) }

  // Form Fields
  var selectedCategory by remember {
    mutableStateOf(
      BroadcastPresetCatalog.categories.find { it.code.equals(initialProfessionCode, ignoreCase = true) }
        ?: BroadcastPresetCatalog.categories.first()
    )
  }
  var taskTitle by remember { mutableStateOf("") }
  var problemDescription by remember { mutableStateOf("") }
  var selectedBudget by remember { mutableStateOf("Negotiable") }
  var customBudgetInput by remember { mutableStateOf("") }
  var selectedUrgency by remember { mutableStateOf("URGENT") } // URGENT, TODAY, FLEXIBLE
  var selectedRadiusMeters by remember { mutableIntStateOf(500) } // 500m Core default!
  var localityInput by remember { mutableStateOf(userLocality) }
  var isPhotoAttached by remember { mutableStateOf(true) }
  var allowDirectCalls by remember { mutableStateOf(true) }
  var allowWhatsApp by remember { mutableStateOf(true) }

  // Dispatch Animation State
  var dispatchProgress by remember { mutableFloatStateOf(0f) }
  var pingCount by remember { mutableIntStateOf(0) }
  var remainingSeconds by remember { mutableIntStateOf(1800) } // 30 mins broadcast timer

  // Quick helper to fill task from preset
  fun applyPreset(preset: QuickTaskPreset) {
    taskTitle = preset.title
    problemDescription = preset.description
    selectedBudget = preset.defaultBudget
  }

  // Auto-fill initial template if title is empty
  LaunchedEffect(selectedCategory) {
    if (taskTitle.isBlank()) {
      selectedCategory.quickTasks.firstOrNull()?.let { applyPreset(it) }
    }
  }

  // Simulated Radar Dispatch Step Animation
  LaunchedEffect(currentStep) {
    if (currentStep == BroadcastFlowStep.RADAR_DISPATCH) {
      dispatchProgress = 0f
      pingCount = 0
      val targetPros = selectedCategory.estimatedNearbyPros
      for (i in 1..targetPros) {
        delay(120L)
        dispatchProgress = i.toFloat() / targetPros.toFloat()
        pingCount = i
      }
      delay(400L)
      // Transition to Confirmed Active Broadcast HUD
      currentStep = BroadcastFlowStep.CONFIRMED
    }
  }

  // Expiry Timer in CONFIRMED state
  LaunchedEffect(currentStep) {
    if (currentStep == BroadcastFlowStep.CONFIRMED) {
      while (remainingSeconds > 0) {
        delay(1000L)
        remainingSeconds -= 1
      }
    }
  }

  Dialog(
    onDismissRequest = {
      if (currentStep != BroadcastFlowStep.RADAR_DISPATCH) {
        onDismiss()
      }
    },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 16.dp)
        .testTag("proximity_service_broadcast_dialog")
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        when (currentStep) {
          BroadcastFlowStep.FORM -> {
            ServiceRequestFormContent(
              selectedCategory = selectedCategory,
              onCategorySelect = { cat ->
                selectedCategory = cat
                cat.quickTasks.firstOrNull()?.let { applyPreset(it) }
              },
              taskTitle = taskTitle,
              onTitleChange = { taskTitle = it },
              problemDescription = problemDescription,
              onDescriptionChange = { problemDescription = it },
              selectedBudget = selectedBudget,
              onBudgetSelect = { selectedBudget = it },
              customBudgetInput = customBudgetInput,
              onCustomBudgetChange = { customBudgetInput = it },
              selectedUrgency = selectedUrgency,
              onUrgencySelect = { selectedUrgency = it },
              selectedRadiusMeters = selectedRadiusMeters,
              onRadiusSelect = { selectedRadiusMeters = it },
              localityInput = localityInput,
              onLocalityChange = { localityInput = it },
              isPhotoAttached = isPhotoAttached,
              onTogglePhoto = { isPhotoAttached = !isPhotoAttached },
              allowDirectCalls = allowDirectCalls,
              onToggleCalls = { allowDirectCalls = !allowDirectCalls },
              allowWhatsApp = allowWhatsApp,
              onToggleWhatsApp = { allowWhatsApp = !allowWhatsApp },
              onApplyQuickPreset = { applyPreset(it) },
              onDismiss = onDismiss,
              onStartBroadcast = {
                // Trigger submission and switch to radar dispatch
                val finalBudget = if (selectedBudget == "Custom" && customBudgetInput.isNotBlank()) {
                  "₹$customBudgetInput"
                } else {
                  selectedBudget
                }
                onBroadcastSubmit(
                  taskTitle.ifBlank { "${selectedCategory.displayName} Task Request" },
                  selectedCategory.displayName,
                  selectedCategory.code,
                  problemDescription.ifBlank { "Immediate service requested in neighborhood." },
                  finalBudget,
                  selectedUrgency,
                  "ON_SITE",
                  selectedCategory.code,
                  selectedRadiusMeters,
                  isPhotoAttached,
                  localityInput
                )
                currentStep = BroadcastFlowStep.RADAR_DISPATCH
              }
            )
          }

          BroadcastFlowStep.RADAR_DISPATCH -> {
            RadarScanningDispatchContent(
              category = selectedCategory,
              radiusMeters = selectedRadiusMeters,
              locality = localityInput,
              progress = dispatchProgress,
              pingedCount = pingCount,
              totalPros = selectedCategory.estimatedNearbyPros
            )
          }

          BroadcastFlowStep.CONFIRMED -> {
            BroadcastActiveConfirmationContent(
              taskTitle = taskTitle,
              category = selectedCategory,
              radiusMeters = selectedRadiusMeters,
              locality = localityInput,
              remainingSeconds = remainingSeconds,
              respondingBusinesses = pingCount,
              allowCalls = allowDirectCalls,
              allowWhatsApp = allowWhatsApp,
              onClose = onDismiss
            )
          }
        }
      }
    }
  }
}

/**
 * Main Service Request Form View
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceRequestFormContent(
  selectedCategory: BroadcastCategoryPreset,
  onCategorySelect: (BroadcastCategoryPreset) -> Unit,
  taskTitle: String,
  onTitleChange: (String) -> Unit,
  problemDescription: String,
  onDescriptionChange: (String) -> Unit,
  selectedBudget: String,
  onBudgetSelect: (String) -> Unit,
  customBudgetInput: String,
  onCustomBudgetChange: (String) -> Unit,
  selectedUrgency: String,
  onUrgencySelect: (String) -> Unit,
  selectedRadiusMeters: Int,
  onRadiusSelect: (Int) -> Unit,
  localityInput: String,
  onLocalityChange: (String) -> Unit,
  isPhotoAttached: Boolean,
  onTogglePhoto: () -> Unit,
  allowDirectCalls: Boolean,
  onToggleCalls: () -> Unit,
  allowWhatsApp: Boolean,
  onToggleWhatsApp: () -> Unit,
  onApplyQuickPreset: (QuickTaskPreset) -> Unit,
  onDismiss: () -> Unit,
  onStartBroadcast: () -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 18.dp)
  ) {

    // 1. Header with Close Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFF2563EB).copy(alpha = 0.12f))
            .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Radar,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Broadcast Service Request",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Alert verified businesses within 500m",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2563EB)
          )
        }
      }

      IconButton(
        onClick = onDismiss,
        modifier = Modifier
          .size(32.dp)
          .testTag("dismiss_service_request_form_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. 500m Proximity Live Radar Banner
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("live_proximity_radar_banner")
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          PulsingRadarIndicator()
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "500M PROXIMITY RADAR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF38BDF8),
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF10B981).copy(alpha = 0.25f)
              ) {
                Text(
                  text = "LIVE",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFF34D399),
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
            Text(
              text = "⚡ ${selectedCategory.estimatedNearbyPros} verified ${selectedCategory.displayName.lowercase()} businesses ready in your 500m zone.",
              fontSize = 11.sp,
              color = Color(0xFFE2E8F0),
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Trade / Category Selection
    Text(
      text = "1. SELECT TRADE / CATEGORY",
      fontSize = 11.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      BroadcastPresetCatalog.categories.forEach { category ->
        val isSelected = selectedCategory.code == category.code
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) category.accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCategorySelect(category) }
            .testTag("category_chip_${category.code}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Text(text = category.emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = category.displayName,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Quick Task Suggestions Chips
    Text(
      text = "Quick Presets (Tap to Auto-fill):",
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(6.dp))

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      selectedCategory.quickTasks.forEach { preset ->
        val isCurrent = taskTitle == preset.title
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isCurrent) selectedCategory.accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          border = BorderStroke(
            1.dp,
            if (isCurrent) selectedCategory.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onApplyQuickPreset(preset) }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${if (isCurrent) "✓ " else "+ "}${preset.tag}",
              fontSize = 11.sp,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
              color = if (isCurrent) selectedCategory.accentColor else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Task Title & Description
    Text(
      text = "2. TASK DETAILS",
      fontSize = 11.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = taskTitle,
      onValueChange = onTitleChange,
      label = { Text("Task or Problem Title", fontSize = 12.sp) },
      placeholder = { Text("e.g. Urgent Kitchen Pipe Leakage", fontSize = 12.sp) },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("broadcast_task_title_input"),
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = problemDescription,
      onValueChange = onDescriptionChange,
      label = { Text("Detailed Requirements & Floor / Access Notes", fontSize = 12.sp) },
      placeholder = { Text("Describe the task specifics, what parts are broken, floor number, or urgent timing...", fontSize = 12.sp) },
      minLines = 3,
      maxLines = 5,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("broadcast_problem_desc_input"),
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 5. Proximity Broadcast Radius (500m Core Default Highlighted)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "3. PROXIMITY BROADCAST RADIUS",
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.5.sp
      )
      Text(
        text = if (selectedRadiusMeters == 500) "⭐ 500m Fastest (<20m)" else "${selectedRadiusMeters}m Zone",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = if (selectedRadiusMeters == 500) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      listOf(
        500 to "500m\nWalk",
        1000 to "1000m\nWard",
        2000 to "2000m\nTown",
        5000 to "5000m\nCity",
        10000 to "10000m\nMetro"
      ).forEach { (radius, label) ->
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
              else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            }
          ),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onRadiusSelect(radius) }
            .testTag("radius_option_$radius")
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
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected && isHero500) Color.White else if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 6. Urgency & Budget
    Text(
      text = "4. URGENCY & BUDGET",
      fontSize = 11.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Urgency chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(
        "URGENT" to "⚡ Immediate (<30m)",
        "TODAY" to "🕒 Today (<3h)",
        "FLEXIBLE" to "📅 Flexible"
      ).forEach { (code, label) ->
        val isSelected = selectedUrgency == code
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = when {
            isSelected && code == "URGENT" -> Color(0xFFDC2626)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          },
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onUrgencySelect(code) }
            .testTag("urgency_chip_$code")
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
          ) {
            Text(
              text = label,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Budget selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      listOf("Negotiable", "₹300 - ₹500", "₹500 - ₹1,200", "₹1,200 - ₹2,500", "Custom").forEach { b ->
        val isSelected = selectedBudget == b
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
          ),
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onBudgetSelect(b) }
        ) {
          Text(
            text = b,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }

    if (selectedBudget == "Custom") {
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = customBudgetInput,
        onValueChange = onCustomBudgetChange,
        label = { Text("Enter Budget Amount (₹)", fontSize = 11.sp) },
        placeholder = { Text("e.g. 750", fontSize = 11.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 7. Location & Photo Attachment
    Text(
      text = "5. LOCATION & ATTACHMENTS",
      fontSize = 11.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = localityInput,
      onValueChange = onLocalityChange,
      leadingIcon = {
        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
      },
      label = { Text("Your Current Location / Landmark", fontSize = 11.sp) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Photo attachment card
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable { onTogglePhoto() }
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isPhotoAttached) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPhotoAttached) Icons.Default.PhotoCamera else Icons.Default.AddAPhoto,
              contentDescription = null,
              tint = if (isPhotoAttached) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = if (isPhotoAttached) "Problem Photo Attached" else "Attach Photo of Issue",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isPhotoAttached) "photo_${selectedCategory.code.lowercase()}_problem.jpg (1.8 MB)" else "Helps businesses assess tools & quote quickly",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (isPhotoAttached) Color(0xFF10B981) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ) {
          Text(
            text = if (isPhotoAttached) "Attached ✓" else "+ Add",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPhotoAttached) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Contact Preferences Checkboxes
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .clickable { onToggleCalls() }
      ) {
        Checkbox(
          checked = allowDirectCalls,
          onCheckedChange = { onToggleCalls() },
          colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Direct Calls", fontSize = 11.sp, fontWeight = FontWeight.Medium)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .clickable { onToggleWhatsApp() }
      ) {
        Checkbox(
          checked = allowWhatsApp,
          onCheckedChange = { onToggleWhatsApp() },
          colors = CheckboxDefaults.colors(checkedColor = Color(0xFF25D366)),
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "WhatsApp Alert", fontSize = 11.sp, fontWeight = FontWeight.Medium)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 8. Submit Broadcast Button
    Button(
      onClick = onStartBroadcast,
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("submit_proximity_broadcast_button")
    ) {
      Icon(imageVector = Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "BROADCAST TO ${selectedRadiusMeters}M BUSINESSES",
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "All verified local businesses within ${selectedRadiusMeters}m radius will receive an instant push alert. Stored in Cloud Firestore (`service_requests`).",
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

/**
 * Animated Concentric Radar Pulse Effect
 */
@Composable
private fun PulsingRadarIndicator(modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
  val pulseAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_scale"
  )

  Box(
    modifier = modifier.size(36.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2, size.height / 2)
      val maxRadius = size.minDimension / 2

      // Outer animated ring
      drawCircle(
        color = Color(0xFF38BDF8).copy(alpha = (1f - pulseAnim) * 0.6f),
        radius = maxRadius * pulseAnim,
        center = center,
        style = Stroke(width = 2.dp.toPx())
      )

      // Fixed inner circle
      drawCircle(
        color = Color(0xFF0284C7),
        radius = maxRadius * 0.45f,
        center = center
      )
    }

    Icon(
      imageVector = Icons.Default.FlashOn,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(12.dp)
    )
  }
}

/**
 * Radar Scanning Dispatch Phase (Active Animation)
 */
@Composable
private fun RadarScanningDispatchContent(
  category: BroadcastCategoryPreset,
  radiusMeters: Int,
  locality: String,
  progress: Float,
  pingedCount: Int,
  totalPros: Int
) {
  val infiniteTransition = rememberInfiniteTransition(label = "radar_scanning")
  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_sweep"
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0B1329))
        )
      )
      .padding(28.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "📡 DISPATCHING PROXIMITY BROADCAST",
      fontSize = 11.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 1.sp,
      color = Color(0xFF38BDF8)
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Pinging businesses within ${radiusMeters}m",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )

    Text(
      text = locality,
      fontSize = 11.sp,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Radar Canvas
    Box(
      modifier = Modifier.size(160.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        // Background concentric circles
        for (i in 1..4) {
          drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.15f * i),
            radius = radius * (i / 4f),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
          )
        }

        // Crosshairs
        drawLine(
          color = Color(0xFF38BDF8).copy(alpha = 0.25f),
          start = Offset(center.x, 0f),
          end = Offset(center.x, size.height),
          strokeWidth = 1.dp.toPx()
        )
        drawLine(
          color = Color(0xFF38BDF8).copy(alpha = 0.25f),
          start = Offset(0f, center.y),
          end = Offset(size.width, center.y),
          strokeWidth = 1.dp.toPx()
        )

        // Sweeping beam
        drawArc(
          brush = Brush.sweepGradient(
            colors = listOf(
              Color.Transparent,
              Color(0xFF38BDF8).copy(alpha = 0.1f),
              Color(0xFF38BDF8).copy(alpha = 0.6f)
            )
          ),
          startAngle = sweepAngle,
          sweepAngle = 45f,
          useCenter = true
        )

        // Center dot
        drawCircle(
          color = Color(0xFF38BDF8),
          radius = 6.dp.toPx(),
          center = center
        )
      }

      Text(
        text = category.emoji,
        fontSize = 28.sp
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Progress details
    Text(
      text = "Alerting $pingedCount of $totalPros verified ${category.displayName.lowercase()} businesses...",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color(0xFFF1F5F9)
    )

    Spacer(modifier = Modifier.height(10.dp))

    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth(0.8f)
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = Color(0xFF38BDF8),
      trackColor = Color(0xFF334155)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "Encrypted broadcast packet transmitted over BusyCardNet mesh protocol.",
      fontSize = 10.sp,
      color = Color(0xFF64748B),
      textAlign = TextAlign.Center
    )
  }
}

/**
 * Confirmed Active Broadcast HUD with Countdown & Responders Counter
 */
@Composable
private fun BroadcastActiveConfirmationContent(
  taskTitle: String,
  category: BroadcastCategoryPreset,
  radiusMeters: Int,
  locality: String,
  remainingSeconds: Int,
  respondingBusinesses: Int,
  allowCalls: Boolean,
  allowWhatsApp: Boolean,
  onClose: () -> Unit
) {
  val minutes = remainingSeconds / 60
  val seconds = remainingSeconds % 60
  val formattedTime = String.format("%02d:%02d", minutes, seconds)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    // Success check circle
    Box(
      modifier = Modifier
        .size(60.dp)
        .clip(CircleShape)
        .background(Color(0xFF10B981).copy(alpha = 0.15f))
        .border(2.dp, Color(0xFF10B981), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = Color(0xFF10B981),
        modifier = Modifier.size(34.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "BROADCAST ACTIVE & LIVE",
      fontSize = 12.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 0.8.sp,
      color = Color(0xFF10B981)
    )

    Text(
      text = taskTitle,
      fontSize = 17.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Live Metrics Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = "Broadcast Radius", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "📍 ${radiusMeters}m Proximity", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(text = "Alerts Delivered", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = "$respondingBusinesses Businesses",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2563EB)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expiry Timer Pill
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0F172A),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Auto-Expires in:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
              )
            }

            Text(
              text = "$formattedTime mins",
              fontSize = 14.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFFF59E0B)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Responses will arrive directly via ${if (allowCalls) "Phone Call, " else ""}${if (allowWhatsApp) "WhatsApp & " else ""}BusyCardNet chat.",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onClose,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp)
        .testTag("dismiss_broadcast_hud_button")
    ) {
      Text(text = "DONE • MONITORING RESPONSES", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
  }
}
