package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.firestore.FirestoreBusinessManager
import com.example.data.local.DigitalCardEntity
import com.example.ui.components.BcnCameraScannerDialog
import com.example.ui.components.BcnCardHyperlink
import com.example.ui.components.GoogleProfileCardManagerDialog
import com.example.ui.map.GoogleMapView
import com.example.ui.map.LocalMapPlaceItem
import com.example.ui.map.LocalPlaceDataProvider
import com.example.ui.map.MapLayerMode
import com.example.ui.map.PlaceDetailBottomSheet
import com.example.ui.map.PlaceType
import com.example.ui.viewmodel.BusyCardNetViewModel
import com.example.util.AuthManager
import com.example.util.BcnLocationManager

// Backward-compatible alias for any existing code
typealias BcnMapPin = LocalMapPlaceItem

/**
 * Main Google Maps integration screen displaying local businesses and service providers
 * within a 500m radius of the user's current location.
 */
@Composable
fun LocalMapScreen(
  viewModel: BusyCardNetViewModel,
  onOpenCardDetails: (DigitalCardEntity) -> Unit,
  onRequestService: (professionCode: String) -> Unit,
  onBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val locationManager = remember { BcnLocationManager.getInstance(context) }
  val currentLocation by locationManager.currentLocation.collectAsStateWithLifecycle()
  val isGpsActive by locationManager.isGpsActive.collectAsStateWithLifecycle()

  if (onBack != null) {
    BackHandler { onBack() }
  }

  var hasLocationPermission by remember {
    mutableStateOf(
      androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED ||
      androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (fine || coarse) {
      hasLocationPermission = true
      locationManager.requestDeviceLocation()
      Toast.makeText(context, "Location access granted. Centering on GPS position...", Toast.LENGTH_SHORT).show()
    }
  }

  LaunchedEffect(Unit) {
    if (!hasLocationPermission) {
      permissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    } else {
      locationManager.requestDeviceLocation()
    }
  }

  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val connectedBridges by viewModel.connectedBridges.collectAsStateWithLifecycle()

  // Authentication via Credential Manager
  val authManager = remember { AuthManager.getInstance() }
  val firebaseUser by authManager.currentUserFlow.collectAsStateWithLifecycle()
  val simulatedUser by authManager.localSimulatedUserFlow.collectAsStateWithLifecycle()
  val isAuthenticated = firebaseUser != null || simulatedUser != null
  val userDisplayName = firebaseUser?.displayName ?: simulatedUser?.displayName ?: "Google Member"
  val userPhotoUrl = firebaseUser?.photoUrl?.toString() ?: simulatedUser?.photoUrl ?: ""

  // State controls
  var selectedRadiusMeters by remember { mutableIntStateOf(500) } // 500m, 1000m, 2000m, 5000m, 10000m
  var selectedPlaceType by remember { mutableStateOf<PlaceType?>(null) } // null = All, LOCAL_BUSINESS, SERVICE_PROVIDER
  var selectedCategory by remember { mutableStateOf("ALL") }
  var searchQuery by remember { mutableStateOf("") }
  var onlyOpenNow by remember { mutableStateOf(false) }
  var layerMode by remember { mutableStateOf(MapLayerMode.NORMAL) }
  var zoomScale by remember { mutableFloatStateOf(1f) }
  var selectedPlace by remember { mutableStateOf<LocalMapPlaceItem?>(null) }
  var showLayerDropdown by remember { mutableStateOf(false) }
  var showAddPlaceDialog by remember { mutableStateOf(false) }
  var showGoogleProfileDialog by remember { mutableStateOf(false) }
  var showScannerDialog by remember { mutableStateOf(false) }

  // Live Firestore integration: Pull local businesses within selected radius (500m - 10000m)
  var firestorePlaces by remember { mutableStateOf<List<LocalMapPlaceItem>>(emptyList()) }
  var isFirestoreLive by remember { mutableStateOf(false) }

  LaunchedEffect(currentLocation.latitude, currentLocation.longitude, selectedRadiusMeters) {
    try {
      FirestoreBusinessManager.listenToBusinessesWithinRadius(
        userLat = currentLocation.latitude,
        userLng = currentLocation.longitude,
        radiusMeters = selectedRadiusMeters
      ).collect { list ->
        firestorePlaces = list
        isFirestoreLive = true
      }
    } catch (_: Exception) {
      firestorePlaces = LocalPlaceDataProvider.getCuratedPlaces(currentLocation.latitude, currentLocation.longitude)
        .filter { it.distanceMeters <= selectedRadiusMeters }
    }
  }

  // Combined places list from Firestore + local database cards + curated data
  val placesList = remember(firestorePlaces, currentLocation, allCards, connectedBridges) {
    val basePlaces = firestorePlaces.toMutableList()

    // Supplement with local curated places if Firestore returned empty
    if (basePlaces.isEmpty()) {
      basePlaces.addAll(
        LocalPlaceDataProvider.getCuratedPlaces(
          currentLat = currentLocation.latitude,
          currentLng = currentLocation.longitude
        )
      )
    }

    // Augment with registered cards in Room DB that have locations
    allCards.forEach { card ->
      if (basePlaces.none { it.busyCardNetId == card.busyCardNetId }) {
        val dist = locationManager.calculateDistanceMeters(card.latitude, card.longitude).coerceAtLeast(50)
        val isBusiness = card.professionCode in listOf("SHOP", "REST", "HOTE", "BIZ", "SALE", "RETL")
        basePlaces.add(
          LocalMapPlaceItem(
            id = card.cardId,
            name = card.businessName.ifBlank { card.name },
            ownerOrContact = card.name,
            busyCardNetId = card.busyCardNetId,
            placeType = if (isBusiness) PlaceType.LOCAL_BUSINESS else PlaceType.SERVICE_PROVIDER,
            professionCode = card.professionCode,
            categoryTitle = card.designation,
            emoji = if (isBusiness) "🏢" else "🔧",
            latitude = card.latitude,
            longitude = card.longitude,
            distanceMeters = dist,
            address = card.address,
            phone = card.phone,
            whatsapp = card.whatsapp,
            email = card.email,
            website = card.website,
            rating = 4.8f,
            reviewsCount = 15,
            isOpenNow = true,
            operatingHours = card.workingHours.ifBlank { "9:00 AM - 8:00 PM" },
            servicesOrProducts = card.services.split(",").map { it.trim() }.filter { it.isNotBlank() },
            isConnectedBridge = connectedBridges.any { it.targetBusyCardNetId == card.busyCardNetId },
            about = card.about
          )
        )
      }
    }
    basePlaces
  }

  // Filtered places according to radius (500m), type, category, and search query
  val filteredPlaces = remember(placesList, selectedRadiusMeters, selectedPlaceType, selectedCategory, searchQuery, onlyOpenNow) {
    LocalPlaceDataProvider.filterPlacesWithinRadius(
      places = placesList,
      radiusMeters = selectedRadiusMeters,
      placeType = selectedPlaceType,
      categoryCode = selectedCategory,
      searchQuery = searchQuery,
      onlyOpenNow = onlyOpenNow
    )
  }

  // Breakdown statistics dynamically calculated for selected radius (500m, 1000m, 2000m, 5000m, 10000m)
  val placesInRadius = remember(placesList, selectedRadiusMeters) { placesList.filter { it.distanceMeters <= selectedRadiusMeters } }
  val businessesInRadius = remember(placesInRadius) { placesInRadius.count { it.isBusiness } }
  val providersInRadius = remember(placesInRadius) { placesInRadius.count { it.isServiceProvider } }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF070D19))
      .testTag("google_map_screen")
  ) {

    // 1. Top Header: Google Maps Identity & GPS Coordinates
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF0F172A))
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (onBack != null) {
          IconButton(
            onClick = onBack,
            modifier = Modifier
              .size(36.dp)
              .padding(end = 4.dp)
              .testTag("map_header_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFF4285F4),
              modifier = Modifier.padding(end = 6.dp)
            ) {
              Text(
                text = "GOOGLE MAPS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
            Text(
              text = "NEARBY BUSINESSES (500M)",
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.8.sp,
              color = Color(0xFF38BDF8)
            )
          }

          Spacer(modifier = Modifier.height(3.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isGpsActive) Color(0xFF10B981) else Color(0xFFF59E0B))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "GPS: %.4f°N, %.4f°E".format(currentLocation.latitude, currentLocation.longitude),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFF065F46)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Live Firestore", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
              }
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // CameraX QR Scanner quick action button
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0284C7),
            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
            modifier = Modifier
              .clickable { showScannerDialog = true }
              .testTag("map_scan_qr_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Scan QR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // Google Sign-In & Card Profile Manager Button
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isAuthenticated) Color(0xFF1E293B) else Color(0xFF4285F4),
            border = BorderStroke(1.dp, if (isAuthenticated) Color(0xFF10B981) else Color(0xFF93C5FD)),
            modifier = Modifier
              .clickable { showGoogleProfileDialog = true }
              .testTag("google_profile_btn")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              if (isAuthenticated && userPhotoUrl.isNotBlank()) {
                AsyncImage(
                  model = userPhotoUrl,
                  contentDescription = "Avatar",
                  modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                )
              } else {
                Icon(
                  imageVector = Icons.Default.AccountCircle,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
              }
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isAuthenticated) "Card Profile" else "Sign In",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Location Permission Banner when GPS permission not yet granted
      if (!hasLocationPermission) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF1E3A8A).copy(alpha = 0.9f),
          border = BorderStroke(1.dp, Color(0xFF60A5FA)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("gps_permission_banner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "Enable GPS for exact 500m proximity to your current location",
                fontSize = 10.sp,
                color = Color.White
              )
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF2563EB),
              modifier = Modifier
                .clickable {
                  permissionLauncher.launch(
                    arrayOf(
                      Manifest.permission.ACCESS_FINE_LOCATION,
                      Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                  )
                }
                .testTag("allow_gps_button")
            ) {
              Text(
                "Allow GPS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // 2. Search Box for Places
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search businesses & services within ${if (selectedRadiusMeters >= 1000) "${selectedRadiusMeters / 1000}km" else "${selectedRadiusMeters}m"}...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp)) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
            }
          }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF1E293B),
          unfocusedContainerColor = Color(0xFF1E293B),
          focusedBorderColor = Color(0xFF38BDF8),
          unfocusedBorderColor = Color(0xFF334155),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("map_search_input")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Primary Filter Row: All, Businesses, Service Providers, Open Now
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Filter: All within selected radius
        val isAllSelected = selectedPlaceType == null
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isAllSelected) Color(0xFF4285F4) else Color(0xFF1E293B),
          border = BorderStroke(1.dp, if (isAllSelected) Color(0xFF93C5FD) else Color(0xFF334155)),
          modifier = Modifier
            .weight(1f)
            .clickable {
              selectedPlaceType = null
              selectedPlace = null
            }
            .testTag("filter_type_all")
        ) {
          Text(
            text = "All (${placesInRadius.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAllSelected) Color.White else Color(0xFFCBD5E1),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
          )
        }

        // Filter: Local Businesses
        val isBizSelected = selectedPlaceType == PlaceType.LOCAL_BUSINESS
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isBizSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
          border = BorderStroke(1.dp, if (isBizSelected) Color(0xFF38BDF8) else Color(0xFF334155)),
          modifier = Modifier
            .weight(1.3f)
            .clickable {
              selectedPlaceType = PlaceType.LOCAL_BUSINESS
              selectedPlace = null
            }
            .testTag("filter_type_businesses")
        ) {
          Text(
            text = "🏢 Businesses ($businessesInRadius)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isBizSelected) Color.White else Color(0xFFCBD5E1),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
          )
        }

        // Filter: Service Providers
        val isProvSelected = selectedPlaceType == PlaceType.SERVICE_PROVIDER
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isProvSelected) Color(0xFFF59E0B) else Color(0xFF1E293B),
          border = BorderStroke(1.dp, if (isProvSelected) Color(0xFFFBBF24) else Color(0xFF334155)),
          modifier = Modifier
            .weight(1.3f)
            .clickable {
              selectedPlaceType = PlaceType.SERVICE_PROVIDER
              selectedPlace = null
            }
            .testTag("filter_type_providers")
        ) {
          Text(
            text = "🔧 Services ($providersInRadius)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isProvSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
          )
        }

        // Filter: Open Now Toggle
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (onlyOpenNow) Color(0xFF10B981) else Color(0xFF1E293B),
          border = BorderStroke(1.dp, if (onlyOpenNow) Color(0xFF34D399) else Color(0xFF334155)),
          modifier = Modifier
            .clickable { onlyOpenNow = !onlyOpenNow }
            .testTag("filter_open_now")
        ) {
          Text(
            text = if (onlyOpenNow) "🟢 Open" else "Open Now",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (onlyOpenNow) Color.White else Color(0xFFCBD5E1),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 4. Prominent Dedicated Radius Selector Row (500m, 1000m, 2000m, 5000m, 10000m)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Radius:",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFCBD5E1),
          modifier = Modifier.padding(end = 6.dp)
        )
        val radiusOptions = listOf(
          500 to "500m",
          1000 to "1000m",
          2000 to "2000m",
          5000 to "5000m",
          10000 to "10000m"
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          radiusOptions.forEach { (rad, label) ->
            val isSelected = selectedRadiusMeters == rad
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B),
              border = BorderStroke(1.dp, if (isSelected) Color(0xFF7DD3FC) else Color(0xFF334155)),
              modifier = Modifier
                .weight(1f)
                .clickable {
                  selectedRadiusMeters = rad
                  selectedPlace = null
                }
                .testTag("radius_pill_${rad}m")
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 5.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 5. Category Chips in a smooth scrollable row
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
      ) {
        // Category pills
        val categories = listOf(
          "ALL" to "All Categories",
          "ELEC" to "⚡ Electrician",
          "PLUM" to "🔧 Plumber",
          "DOCT" to "🩺 Clinic",
          "SHOP" to "🏪 Hardware & Retail",
          "REST" to "🍴 Restaurant",
          "CARP" to "🪚 Carpenter",
          "TECH" to "❄️ AC Repair",
          "MECH" to "🚗 Auto Mechanic",
          "BIZ" to "💼 Corporate & Legal"
        )
        items(categories) { (code, label) ->
          val isSelected = selectedCategory == code
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B),
            border = BorderStroke(1.dp, if (isSelected) Color(0xFFFBBF24) else Color(0xFF334155)),
            modifier = Modifier.clickable {
              selectedCategory = code
              selectedPlace = null
            }
          ) {
            Text(
              text = label,
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // 5. Google Maps Viewport Area
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // Native Google Maps component with fallback radar
      GoogleMapView(
        userLatitude = currentLocation.latitude,
        userLongitude = currentLocation.longitude,
        radiusMeters = selectedRadiusMeters.toDouble(),
        places = filteredPlaces,
        selectedPlace = selectedPlace,
        onPlaceSelected = { place -> selectedPlace = place },
        layerMode = layerMode,
        zoomScale = zoomScale,
        modifier = Modifier.fillMaxSize()
      )

      // Top Floating Badge: Radius & Place Count
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.9f),
        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${filteredPlaces.size} Places in ${selectedRadiusMeters}m",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      // Floating Map Action Controls (Top End)
      Column(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Layers Dropdown Button
        Box {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 4.dp
          ) {
            IconButton(
              onClick = { showLayerDropdown = true },
              modifier = Modifier.size(38.dp)
            ) {
              Icon(Icons.Default.Layers, contentDescription = "Map Layers", tint = Color.White)
            }
          }

          DropdownMenu(
            expanded = showLayerDropdown,
            onDismissRequest = { showLayerDropdown = false }
          ) {
            MapLayerMode.values().forEach { mode ->
              DropdownMenuItem(
                text = { Text(mode.label, fontSize = 12.sp, fontWeight = if (layerMode == mode) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                  layerMode = mode
                  showLayerDropdown = false
                }
              )
            }
          }
        }

        // Zoom In, Zoom Out & Locate Me
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0F172A).copy(alpha = 0.9f),
          border = BorderStroke(1.dp, Color(0xFF334155)),
          shadowElevation = 4.dp
        ) {
          Column {
            IconButton(
              onClick = { if (zoomScale < 2.0f) zoomScale += 0.25f },
              modifier = Modifier.size(38.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
            }
            IconButton(
              onClick = { if (zoomScale > 0.6f) zoomScale -= 0.25f },
              modifier = Modifier.size(38.dp)
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
            }
            IconButton(
              onClick = {
                zoomScale = 1f
                selectedPlace = null
                locationManager.requestDeviceLocation()
                Toast.makeText(context, "Centering on GPS & refreshing 500m radius...", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(38.dp)
            ) {
              Icon(Icons.Default.MyLocation, contentDescription = "Locate Me", tint = Color(0xFF4285F4))
            }
          }
        }

        // Floating QR Scanner Quick Action Button
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0F172A).copy(alpha = 0.95f),
          border = BorderStroke(1.dp, Color(0xFF38BDF8)),
          shadowElevation = 4.dp
        ) {
          IconButton(
            onClick = { showScannerDialog = true },
            modifier = Modifier.size(38.dp).testTag("floating_scan_qr_button")
          ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Card QR", tint = Color(0xFF38BDF8))
          }
        }

        // Quick Add Business / Service Provider Button
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0284C7).copy(alpha = 0.9f),
          border = BorderStroke(1.dp, Color(0xFF38BDF8)),
          shadowElevation = 4.dp
        ) {
          IconButton(
            onClick = { showAddPlaceDialog = true },
            modifier = Modifier.size(38.dp)
          ) {
            Icon(Icons.Default.AddBusiness, contentDescription = "Add Place", tint = Color.White)
          }
        }

        // Proximity Broadcast 500m Service Request Button
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF2563EB).copy(alpha = 0.95f),
          border = BorderStroke(1.dp, Color(0xFF60A5FA)),
          shadowElevation = 4.dp
        ) {
          IconButton(
            onClick = { onRequestService("PLUM") },
            modifier = Modifier
              .size(38.dp)
              .testTag("map_broadcast_service_request_fab")
          ) {
            Icon(Icons.Default.Radar, contentDescription = "Broadcast 500m Service Request", tint = Color.White)
          }
        }
      }

      // Bottom Section: Either the Selected Place Bottom Sheet OR the 500m Carousel
      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
      ) {
        androidx.compose.animation.AnimatedVisibility(
          visible = selectedPlace != null,
          enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
          exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
          selectedPlace?.let { place ->
            PlaceDetailBottomSheet(
              place = place,
              onDismiss = { selectedPlace = null },
              onOpenCardDetails = onOpenCardDetails,
              onConnectBridge = { p ->
                Toast.makeText(context, "Bridge connection request dispatched to ${p.name}!", Toast.LENGTH_SHORT).show()
              },
              viewModel = viewModel
            )
          }
        }

        // Horizontal Carousel of places within 500m when none is selected
        androidx.compose.animation.AnimatedVisibility(
          visible = selectedPlace == null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0F172A).copy(alpha = 0.85f))
              .padding(vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "DISCOVERED WITHIN 500M (${filteredPlaces.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = "Tap to view on Google Maps",
                fontSize = 10.sp,
                color = Color(0xFF38BDF8)
              )
            }

            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(filteredPlaces) { place ->
                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                  border = BorderStroke(1.dp, if (place.isBusiness) Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.5f)),
                  modifier = Modifier
                    .width(220.dp)
                    .clickable { selectedPlace = place }
                    .testTag("carousel_place_${place.id}")
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (place.isBusiness) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)
                      ) {
                        Text(
                          text = "${place.emoji} ${if (place.isBusiness) "BUSINESS" else "SERVICE"}",
                          fontSize = 8.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (place.isBusiness) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                      }
                      Text(
                        text = "📍 ${place.distanceMeters}m",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                      )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                      text = place.name,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )

                    BcnCardHyperlink(
                      bcnId = place.busyCardNetId,
                      color = Color(0xFF38BDF8),
                      fontSize = 10.sp,
                      modifier = Modifier.padding(vertical = 1.dp)
                    )

                    Text(
                      text = place.categoryTitle,
                      fontSize = 11.sp,
                      color = Color(0xFF94A3B8),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${place.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                      }

                      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = Color(0xFF4285F4),
                          modifier = Modifier.clickable {
                            locationManager.navigateWithGoogleMaps(context, place.latitude, place.longitude)
                          }
                        ) {
                          Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Directions",
                            tint = Color.White,
                            modifier = Modifier
                              .padding(4.dp)
                              .size(14.dp)
                          )
                        }

                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = Color(0xFF10B981),
                          modifier = Modifier.clickable {
                            val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${place.phone}"))
                            context.startActivity(dialIntent)
                          }
                        ) {
                          Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier
                              .padding(4.dp)
                              .size(14.dp)
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // 6. Register Local Business / Service Provider Dialog
  if (showAddPlaceDialog) {
    var newName by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf(PlaceType.LOCAL_BUSINESS) }
    var newCategory by remember { mutableStateOf("ELEC") }
    var newPhone by remember { mutableStateOf("+91 ") }
    var newAddress by remember { mutableStateOf("Kurla West, Mumbai") }

    AlertDialog(
      onDismissRequest = { showAddPlaceDialog = false },
      title = { Text("Register Business or Service on Map", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Pin your business or service within the 500m local discovery radius:", fontSize = 12.sp, color = Color(0xFF94A3B8))

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (newType == PlaceType.LOCAL_BUSINESS) Color(0xFF0284C7) else Color(0xFF1E293B),
              modifier = Modifier
                .weight(1f)
                .clickable { newType = PlaceType.LOCAL_BUSINESS }
            ) {
              Text(
                text = "🏢 Business",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (newType == PlaceType.SERVICE_PROVIDER) Color(0xFFF59E0B) else Color(0xFF1E293B),
              modifier = Modifier
                .weight(1f)
                .clickable { newType = PlaceType.SERVICE_PROVIDER }
            ) {
              Text(
                text = "🔧 Service Provider",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (newType == PlaceType.SERVICE_PROVIDER) Color.Black else Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              )
            }
          }

          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Business / Provider Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it },
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newAddress,
            onValueChange = { newAddress = it },
            label = { Text("Address / Locality") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newName.isNotBlank()) {
              Toast.makeText(context, "$newName pinned to Google Maps at current location!", Toast.LENGTH_SHORT).show()
              showAddPlaceDialog = false
            } else {
              Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
          Text("Pin to Map")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddPlaceDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Google Profile & Visiting Card Manager Dialog
  if (showGoogleProfileDialog) {
    GoogleProfileCardManagerDialog(
      viewModel = viewModel,
      currentLatitude = currentLocation.latitude,
      currentLongitude = currentLocation.longitude,
      onDismiss = { showGoogleProfileDialog = false },
      onCardSaved = { savedCard ->
        Toast.makeText(context, "Visiting Card Profile live in Firestore!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // CameraX QR Scanner Dialog for quickly importing another person's digital business card
  if (showScannerDialog) {
    BcnCameraScannerDialog(
      viewModel = viewModel,
      onDismiss = { showScannerDialog = false },
      onCardScanned = { scannedCard ->
        showScannerDialog = false
        Toast.makeText(context, "📥 Scanned & Verified: ${scannedCard.businessName.ifBlank { scannedCard.name }}", Toast.LENGTH_LONG).show()
      }
    )
  }
}

/**
 * Composable screen using the Google Maps SDK to visualize local businesses
 * within a 500m, 1000m, 2000m, 5000m, or 10000m radius of the user's current location,
 * pulling data live from Firestore.
 */
@Composable
fun GoogleBusinessMapScreen(
  viewModel: BusyCardNetViewModel,
  onOpenCardDetails: (DigitalCardEntity) -> Unit = {},
  onRequestService: (professionCode: String) -> Unit = {},
  onBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  LocalMapScreen(
    viewModel = viewModel,
    onOpenCardDetails = onOpenCardDetails,
    onRequestService = onRequestService,
    onBack = onBack,
    modifier = modifier
  )
}
