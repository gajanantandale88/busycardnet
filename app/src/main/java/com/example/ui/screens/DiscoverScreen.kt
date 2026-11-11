package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.components.SmartSearchIntentBanner
import com.example.util.SmartSearchIntentEngine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DigitalCardEntity
import com.example.data.model.BusinessRatingSummary
import com.example.ui.components.BcnCardHyperlink
import com.example.ui.components.DigitalCardGrid
import com.example.ui.components.DigitalVisitingCardView
import com.example.ui.components.reviews.BusinessReviewsBottomSheet
import com.example.ui.components.reviews.StarRatingDisplay
import com.example.ui.components.reviews.WriteReviewDialog
import com.example.ui.viewmodel.BusyCardNetViewModel

data class NearbyProfessional(
  val name: String,
  val businessName: String,
  val professionCode: String,
  val professionTitle: String,
  val busyCardNetId: String,
  val distanceMeters: Int,
  val rating: Float,
  val availability: String, // AVAILABLE, BUSY, OFFLINE
  val phone: String,
  val whatsapp: String
)

enum class DiscoverMode {
  GRID, // Grid layout with multiple DigitalCard components
  DIRECTORY, // Classic directory list
  MAP // Google Maps & Radar
}

@Composable
fun DiscoverScreen(
  viewModel: BusyCardNetViewModel,
  initialCategoryCode: String? = null,
  onOpenCardDetails: (DigitalCardEntity) -> Unit,
  onRequestService: (professionCode: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val professions by viewModel.allProfessions.collectAsStateWithLifecycle()
  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val searchedCard by viewModel.searchedCard.collectAsStateWithLifecycle()

  var selectedProfessionFilter by remember { mutableStateOf(if (initialCategoryCode == "MAP") "ALL" else (initialCategoryCode ?: "ALL")) }
  var discoverMode by remember {
    mutableStateOf(if (initialCategoryCode == "MAP") DiscoverMode.MAP else DiscoverMode.GRID)
  }

  var selectedProForReview by remember { mutableStateOf<NearbyProfessional?>(null) }
  var writingReviewForPro by remember { mutableStateOf<NearbyProfessional?>(null) }
  val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()

  androidx.compose.runtime.LaunchedEffect(initialCategoryCode) {
    if (initialCategoryCode == "MAP") {
      discoverMode = DiscoverMode.MAP
    } else if (initialCategoryCode != null) {
      selectedProfessionFilter = initialCategoryCode
      discoverMode = DiscoverMode.GRID
    }
  }

  // Sample verified nearby professionals matching user prompt specification (Section 24)
  val nearbyPros = remember {
    listOf(
      NearbyProfessional(
        name = "Rajesh Sharma",
        businessName = "Raj Electrical Services",
        professionCode = "ELEC",
        professionTitle = "Electrician",
        busyCardNetId = "IN-MH-MU-26-581ELEC347",
        distanceMeters = 180,
        rating = 4.9f,
        availability = "AVAILABLE",
        phone = "+91 98765 43210",
        whatsapp = "+91 98765 43210"
      ),
      NearbyProfessional(
        name = "Anil Kadam",
        businessName = "QuickFix Plumbing Solutions",
        professionCode = "PLUM",
        professionTitle = "Plumber",
        busyCardNetId = "IN-MH-MU-26-735PLUM402",
        distanceMeters = 250,
        rating = 4.8f,
        availability = "AVAILABLE",
        phone = "+91 97654 32109",
        whatsapp = "+91 97654 32109"
      ),
      NearbyProfessional(
        name = "Dr. Anita Deshmukh",
        businessName = "City Health Clinic",
        professionCode = "DOCT",
        professionTitle = "Doctor",
        busyCardNetId = "IN-MH-MU-26-264DOCT918",
        distanceMeters = 420,
        rating = 4.9f,
        availability = "AVAILABLE",
        phone = "+91 99887 66554",
        whatsapp = "+91 99887 66554"
      ),
      NearbyProfessional(
        name = "Adv. Vikrant Shinde",
        businessName = "Shinde & Associates Law Firm",
        professionCode = "LAWY",
        professionTitle = "Lawyer",
        busyCardNetId = "IN-MH-MU-26-421LAWY683",
        distanceMeters = 650,
        rating = 4.7f,
        availability = "BUSY",
        phone = "+91 98123 45678",
        whatsapp = "+91 98123 45678"
      ),
      NearbyProfessional(
        name = "Master Carpenter Salim",
        businessName = "Salim Woodcraft & Interiors",
        professionCode = "CARP",
        professionTitle = "Carpenter",
        busyCardNetId = "IN-MH-MU-26-819CARP551",
        distanceMeters = 800,
        rating = 4.8f,
        availability = "AVAILABLE",
        phone = "+91 98222 33445",
        whatsapp = "+91 98222 33445"
      ),
      NearbyProfessional(
        name = "Vinod Sutar",
        businessName = "Speedy Auto Mechanic",
        professionCode = "MECH",
        professionTitle = "Mechanic",
        busyCardNetId = "IN-MH-MU-26-112MECH394",
        distanceMeters = 1100,
        rating = 4.6f,
        availability = "AVAILABLE",
        phone = "+91 98333 77889",
        whatsapp = "+91 98333 77889"
      )
    )
  }

  val recognizedIntent = remember(searchQuery) {
    SmartSearchIntentEngine.analyzeQuery(searchQuery)
  }

  val filteredPros = remember(selectedProfessionFilter, searchQuery, nearbyPros, recognizedIntent) {
    nearbyPros.filter { pro ->
      val matchesCategory = selectedProfessionFilter == "ALL" || pro.professionCode == selectedProfessionFilter
      val matchesIntent = recognizedIntent != null && pro.professionCode.equals(recognizedIntent.requiredProfessionCode, ignoreCase = true)
      val matchesQuery = searchQuery.isBlank() ||
          pro.name.contains(searchQuery, true) ||
          pro.businessName.contains(searchQuery, true) ||
          pro.professionTitle.contains(searchQuery, true) ||
          pro.busyCardNetId.contains(searchQuery, true) ||
          matchesIntent
      matchesCategory && (matchesQuery || matchesIntent)
    }
  }

  if (discoverMode == DiscoverMode.MAP) {
    Box(modifier = modifier.fillMaxSize()) {
      LocalMapScreen(
        viewModel = viewModel,
        onOpenCardDetails = onOpenCardDetails,
        onRequestService = onRequestService,
        onBack = { discoverMode = DiscoverMode.GRID }
      )
      // Switch back to Grid or Directory view
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 14.dp, end = 72.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Surface(
          modifier = Modifier
            .clickable { discoverMode = DiscoverMode.GRID }
            .testTag("switch_to_grid_view"),
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF0284C7),
          shadowElevation = 6.dp
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.GridView, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cards Grid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }

        Surface(
          modifier = Modifier
            .clickable { discoverMode = DiscoverMode.DIRECTORY }
            .testTag("switch_to_list_view"),
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF0F172A),
          shadowElevation = 6.dp
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Directory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  } else if (discoverMode == DiscoverMode.GRID) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .testTag("discover_grid_screen")
    ) {
      // Top Navigation Switcher Bar
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DISCOVER NEARBY",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          // 3-Mode Switcher
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE2E8F0)
          ) {
            Row(
              modifier = Modifier.padding(3.dp),
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              val isGrid = discoverMode == DiscoverMode.GRID
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isGrid) Color(0xFF0F172A) else Color.Transparent,
                modifier = Modifier.clickable { discoverMode = DiscoverMode.GRID }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.Default.GridView, contentDescription = null, tint = if (isGrid) Color.White else Color(0xFF64748B), modifier = Modifier.size(12.dp))
                  Text("Grid", fontSize = 10.sp, fontWeight = if (isGrid) FontWeight.Bold else FontWeight.SemiBold, color = if (isGrid) Color.White else Color(0xFF334155))
                }
              }

              val isDir = discoverMode == DiscoverMode.DIRECTORY
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDir) Color(0xFF0F172A) else Color.Transparent,
                modifier = Modifier.clickable { discoverMode = DiscoverMode.DIRECTORY }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = if (isDir) Color.White else Color(0xFF64748B), modifier = Modifier.size(12.dp))
                  Text("List", fontSize = 10.sp, fontWeight = if (isDir) FontWeight.Bold else FontWeight.SemiBold, color = if (isDir) Color.White else Color(0xFF334155))
                }
              }

              val isMap = discoverMode == DiscoverMode.MAP
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isMap) Color(0xFF0F172A) else Color.Transparent,
                modifier = Modifier.clickable { discoverMode = DiscoverMode.MAP }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isMap) Color.White else Color(0xFF64748B), modifier = Modifier.size(12.dp))
                  Text("Map", fontSize = 10.sp, fontWeight = if (isMap) FontWeight.Bold else FontWeight.SemiBold, color = if (isMap) Color.White else Color(0xFF334155))
                }
              }
            }
          }
        }
      }

      // Grid of Digital Cards with Local Area Browsing & 1-Tap Save
      DigitalCardGrid(
        viewModel = viewModel,
        onOpenCardDetails = onOpenCardDetails
      )
    }
  } else {
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .testTag("discover_screen"),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {

    // 1. Search Bar with 3-Mode Switcher
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DISCOVER NEARBY",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          // 3-Mode Switcher in Directory View
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE2E8F0)
          ) {
            Row(
              modifier = Modifier.padding(3.dp),
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              val isGrid = discoverMode == DiscoverMode.GRID
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isGrid) Color(0xFF0F172A) else Color.Transparent,
                modifier = Modifier.clickable { discoverMode = DiscoverMode.GRID }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.Default.GridView, contentDescription = null, tint = if (isGrid) Color.White else Color(0xFF64748B), modifier = Modifier.size(12.dp))
                  Text("Grid", fontSize = 10.sp, fontWeight = if (isGrid) FontWeight.Bold else FontWeight.SemiBold, color = if (isGrid) Color.White else Color(0xFF334155))
                }
              }

              val isDir = discoverMode == DiscoverMode.DIRECTORY
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDir) Color(0xFF0F172A) else Color.Transparent,
                modifier = Modifier.clickable { discoverMode = DiscoverMode.DIRECTORY }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = if (isDir) Color.White else Color(0xFF64748B), modifier = Modifier.size(12.dp))
                  Text("List", fontSize = 10.sp, fontWeight = if (isDir) FontWeight.Bold else FontWeight.SemiBold, color = if (isDir) Color.White else Color(0xFF334155))
                }
              }

              val isMap = discoverMode == DiscoverMode.MAP
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isMap) Color(0xFF0F172A) else Color.Transparent,
                modifier = Modifier.clickable { discoverMode = DiscoverMode.MAP }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isMap) Color.White else Color(0xFF64748B), modifier = Modifier.size(12.dp))
                  Text("Map", fontSize = 10.sp, fontWeight = if (isMap) FontWeight.Bold else FontWeight.SemiBold, color = if (isMap) Color.White else Color(0xFF334155))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Search by name, ID (e.g. IN-MH-MU...), service...") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("discover_search_input"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )
      }
    }

    // 1B. Smart Search Intent Detection (Natural Language Understanding & Auto-matching)
    if (recognizedIntent != null) {
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          SmartSearchIntentBanner(
            intent = recognizedIntent,
            radiusLabel = "500m",
            onSelectProfession = { profCode ->
              selectedProfessionFilter = profCode
            },
            onOpenMap = { discoverMode = DiscoverMode.MAP },
            onRequestService = { profCode ->
              onRequestService(profCode)
            },
            onDismiss = {
              viewModel.setSearchQuery("")
            }
          )
        }
      }
    }

    // 2. Exact Card Match Result (Section 43: Search "IN-MH-MU-26-581ELEC347" opens exact card)
    if (searchedCard != null) {
      item {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "EXACT BUSYCARDNET ID MATCH",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          DigitalVisitingCardView(
            card = searchedCard!!,
            onCallClick = { phone ->
              val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
              context.startActivity(intent)
            },
            onWhatsAppClick = { phone ->
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone"))
              try { context.startActivity(intent) } catch (e: Exception) {}
            }
          )
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // 2B. Google Maps 500m Local Discovery Banner
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF0284C7)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clickable { discoverMode = DiscoverMode.MAP }
          .testTag("discover_open_google_maps_banner")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF4285F4)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "GOOGLE MAPS • 500M DISCOVERY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color(0xFF38BDF8)
              )
              Text(
                text = "View businesses & service providers on Google Maps",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0284C7)
          ) {
            Text(
              text = "Open Map",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
          }
        }
      }
    }

    // 3. Category Horizontal Filter Row
    item {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          val isAllSelected = selectedProfessionFilter == "ALL"
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isAllSelected) Color(0xFF0F172A) else Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isAllSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
            modifier = Modifier.clickable { selectedProfessionFilter = "ALL" }
          ) {
            Text(
              text = "All Categories",
              fontSize = 11.sp,
              fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.SemiBold,
              color = if (isAllSelected) Color.White else Color(0xFF1E293B),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }
        }

        items(professions) { prof ->
          val isSelected = selectedProfessionFilter == prof.code
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) Color(0xFF0F172A) else Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
            modifier = Modifier.clickable { selectedProfessionFilter = prof.code }
          ) {
            Text(
              text = "${prof.iconEmoji} ${prof.title}",
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
              color = if (isSelected) Color.White else Color(0xFF1E293B),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }
        }
      }
    }

    // 4. Interactive Radar Map View (Prompt Section 44)
    if (discoverMode == DiscoverMode.MAP) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF07111E)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val center = Offset(size.width / 2, size.height / 2)
              val maxRadius = size.minDimension / 2.2f

              // Draw radar rings: 500m, 1km, 2km
              drawCircle(Color(0xFF1E3A8A).copy(alpha = 0.4f), radius = maxRadius * 0.3f, center = center, style = Stroke(width = 1.5f))
              drawCircle(Color(0xFF1E3A8A).copy(alpha = 0.4f), radius = maxRadius * 0.65f, center = center, style = Stroke(width = 1.5f))
              drawCircle(Color(0xFF1E3A8A).copy(alpha = 0.4f), radius = maxRadius, center = center, style = Stroke(width = 1.5f))

              // Axis crosshairs
              drawLine(Color(0xFF1E3A8A).copy(alpha = 0.3f), start = Offset(center.x, center.y - maxRadius), end = Offset(center.x, center.y + maxRadius), strokeWidth = 1f)
              drawLine(Color(0xFF1E3A8A).copy(alpha = 0.3f), start = Offset(center.x - maxRadius, center.y), end = Offset(center.x + maxRadius, center.y), strokeWidth = 1f)

              // Center user node
              drawCircle(Color(0xFF38BDF8), radius = 6.dp.toPx(), center = center)

              // Mock nearby nodes with glowing dots
              val nodes = listOf(
                Offset(center.x + 35.dp.toPx(), center.y - 25.dp.toPx()) to Color(0xFFF59E0B), // Electrician
                Offset(center.x - 45.dp.toPx(), center.y + 30.dp.toPx()) to Color(0xFF06B6D4), // Plumber
                Offset(center.x + 70.dp.toPx(), center.y + 50.dp.toPx()) to Color(0xFF10B981), // Doctor
                Offset(center.x - 80.dp.toPx(), center.y - 40.dp.toPx()) to Color(0xFFEF4444)  // Lawyer
              )
              nodes.forEach { (pos, color) ->
                drawCircle(color.copy(alpha = 0.3f), radius = 10.dp.toPx(), center = pos)
                drawCircle(color, radius = 4.dp.toPx(), center = pos)
              }
            }

            Column(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
            ) {
              Text(text = "📡 RADAR: 500m - 2km Local Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
              Text(text = "⚡ 180m Electrician • 🔧 250m Plumber • 👨‍⚕️ 420m Doctor", fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
          }
        }
      }
    }

    // 5. Nearby Professionals Count
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "NEARBY REGISTERED PROFESSIONALS (${filteredPros.size})",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 6. Nearby Professionals Cards (Prompt Section 24: View Card, Call, WhatsApp, Send Request)
    items(filteredPros) { pro ->
      val summary = remember(allReviews, pro) {
        viewModel.getRatingSummary(
          businessId = pro.busyCardNetId,
          businessName = pro.businessName,
          busyCardNetId = pro.busyCardNetId,
          defaultRating = pro.rating,
          defaultReviewsCount = 14
        )
      }

      NearbyProfessionalCard(
        pro = pro,
        ratingSummary = summary,
        onViewCard = {
          val matching = allCards.find { it.professionCode == pro.professionCode } ?: allCards.firstOrNull()
          if (matching != null) onOpenCardDetails(matching)
        },
        onCall = {
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${pro.phone}"))
          context.startActivity(intent)
        },
        onWhatsApp = {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${pro.whatsapp}"))
          try { context.startActivity(intent) } catch (e: Exception) {}
        },
        onSendRequest = { onRequestService(pro.professionCode) },
        onOpenReviews = { selectedProForReview = pro },
        onRate = { writingReviewForPro = pro },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }
  }

  // Reviews Bottom Sheet
  selectedProForReview?.let { pro ->
    val summary = remember(allReviews, pro) {
      viewModel.getRatingSummary(
        businessId = pro.busyCardNetId,
        businessName = pro.businessName,
        busyCardNetId = pro.busyCardNetId,
        defaultRating = pro.rating,
        defaultReviewsCount = 14
      )
    }

    BusinessReviewsBottomSheet(
      businessId = pro.busyCardNetId,
      businessName = pro.businessName,
      busyCardNetId = pro.busyCardNetId,
      ratingSummary = summary,
      onDismiss = { selectedProForReview = null },
      onOpenWriteReview = {
        writingReviewForPro = pro
        selectedProForReview = null
      },
      onMarkHelpful = { reviewId ->
        viewModel.markReviewHelpful(reviewId)
      }
    )
  }

  // Write Review Dialog
  writingReviewForPro?.let { pro ->
    WriteReviewDialog(
      businessId = pro.busyCardNetId,
      businessName = pro.businessName,
      busyCardNetId = pro.busyCardNetId,
      defaultServiceCategory = pro.professionTitle,
      onDismiss = { writingReviewForPro = null },
      onSubmitReview = { rating, comment, category ->
        viewModel.submitBusinessReview(
          businessId = pro.busyCardNetId,
          businessName = pro.businessName,
          busyCardNetId = pro.busyCardNetId,
          rating = rating,
          reviewText = comment,
          serviceCategory = category
        )
      }
    )
  }
}
}

/**
 * Nearby Professional Card (Section 24 specification)
 */
@Composable
fun NearbyProfessionalCard(
  pro: NearbyProfessional,
  ratingSummary: BusinessRatingSummary? = null,
  onViewCard: () -> Unit,
  onCall: () -> Unit,
  onWhatsApp: () -> Unit,
  onSendRequest: () -> Unit,
  onOpenReviews: () -> Unit = {},
  onRate: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .testTag("nearby_pro_${pro.busyCardNetId}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = pro.businessName,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = pro.professionTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(3.dp))

          // BusyCardNet ID in clickable hyperlink format
          BcnCardHyperlink(
            bcnId = pro.busyCardNetId,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        // Distance & Availability Pill
        Column(horizontalAlignment = Alignment.End) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
          ) {
            Text(
              text = "📍 ${pro.distanceMeters} meters away",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Rating badge - clickable to view reviews
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
            modifier = Modifier
              .clickable(onClick = onOpenReviews)
              .testTag("rating_badge_${pro.busyCardNetId}")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              val displayRating = ratingSummary?.averageRating ?: pro.rating
              val displayCount = ratingSummary?.totalReviewsCount ?: 14
              Text(
                text = String.format("%.1f", displayRating),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "($displayCount★)",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
              )

              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (pro.availability == "AVAILABLE") "🟢 Available" else "🟡 Busy",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedButton(
          onClick = onViewCard,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Text("View Card", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
          onClick = onCall,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(0.9f).height(36.dp)
        ) {
          Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Call", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
          onClick = onWhatsApp,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Button(
          onClick = onSendRequest,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1.1f).height(36.dp)
        ) {
          Text("Request", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedButton(
          onClick = onRate,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(36.dp).testTag("rate_pro_btn_${pro.busyCardNetId}")
        ) {
          Icon(
            imageVector = Icons.Default.RateReview,
            contentDescription = "Rate",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(13.dp)
          )
        }
      }
    }
  }
}
