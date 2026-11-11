package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DigitalCardEntity
import com.example.data.local.SavedBusinessEntity
import com.example.ui.viewmodel.BusyCardNetViewModel

/**
 * Model representing a verified contact in the local vicinity.
 */
data class LocalAreaContact(
  val id: String,
  val name: String,
  val title: String,
  val company: String,
  val professionCode: String,
  val professionTitle: String,
  val busyCardNetId: String,
  val distanceMeters: Int,
  val locality: String,
  val phone: String,
  val email: String,
  val website: String,
  val address: String,
  val rating: Float = 4.9f,
  val reviewCount: Int = 18,
  val verified: Boolean = true,
  val themeColors: List<Color> = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
  val accentColor: Color = Color(0xFF38BDF8)
) {
  fun toSavedBusinessEntity(): SavedBusinessEntity {
    return SavedBusinessEntity(
      businessId = busyCardNetId,
      userId = "local_user_$id",
      businessName = company,
      ownerName = name,
      busyCardNetId = busyCardNetId,
      professionCode = professionCode,
      categoryTitle = professionTitle,
      phone = phone,
      whatsapp = phone,
      email = email,
      website = website,
      address = "$address, $locality",
      operatingHours = "9:00 AM - 8:00 PM",
      rating = rating,
      reviewCount = reviewCount,
      distanceMeters = distanceMeters,
      isVerified = verified,
      servicesList = "$title, $professionTitle"
    )
  }

  fun toDigitalCardEntity(): DigitalCardEntity {
    return DigitalCardEntity(
      cardId = id,
      internalUserId = "user_$id",
      busyCardNetId = busyCardNetId,
      name = name,
      designation = title,
      professionCode = professionCode,
      businessName = company,
      services = "$title, $professionTitle",
      phone = phone,
      whatsapp = phone,
      email = email,
      website = website,
      address = "$address, $locality",
      workingHours = "Mon - Sat: 9:00 AM - 7:00 PM",
      about = "Trusted local provider in $locality.",
      templateStyle = "MODERN_DARK",
      accentColorHex = String.format("#%06X", (0xFFFFFF and accentColor.hashCode())),
      distanceMeters = distanceMeters
    )
  }
}

/**
 * Standard list of verified local area contacts for the district.
 */
val defaultLocalContacts = listOf(
  LocalAreaContact(
    id = "pro_elec_01",
    name = "Rajesh Sharma",
    title = "Master Electrician & Contractor",
    company = "Raj Electrical Services",
    professionCode = "ELEC",
    professionTitle = "Electrician",
    busyCardNetId = "IN-MH-MU-26-581ELEC347",
    distanceMeters = 180,
    locality = "Kurla West",
    phone = "+91 98765 43210",
    email = "rajesh.elec@busycardnet.com",
    website = "https://rajelectricals.in",
    address = "Shop 12, Station Road",
    rating = 4.9f,
    reviewCount = 38,
    verified = true,
    themeColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
    accentColor = Color(0xFFF59E0B)
  ),
  LocalAreaContact(
    id = "pro_plum_02",
    name = "Anil Kadam",
    title = "Senior Sanitary & Pipeline Engineer",
    company = "QuickFix Plumbing Solutions",
    professionCode = "PLUM",
    professionTitle = "Plumber",
    busyCardNetId = "IN-MH-MU-26-735PLUM402",
    distanceMeters = 250,
    locality = "Kurla West",
    phone = "+91 97654 32109",
    email = "anil.quickfix@busycardnet.com",
    website = "https://quickfixmumbai.in",
    address = "Near Municipal Garden",
    rating = 4.8f,
    reviewCount = 29,
    verified = true,
    themeColors = listOf(Color(0xFF0B192C), Color(0xFF1E3E62)),
    accentColor = Color(0xFF06B6D4)
  ),
  LocalAreaContact(
    id = "pro_doct_03",
    name = "Dr. Anita Deshmukh",
    title = "Consultant Physician & Family Medicine",
    company = "City Health Medicare Clinic",
    professionCode = "DOCT",
    professionTitle = "Doctor",
    busyCardNetId = "IN-MH-MU-26-264DOCT918",
    distanceMeters = 420,
    locality = "Bandra East",
    phone = "+91 99887 66554",
    email = "dr.anita@cityhealthclinic.com",
    website = "https://cityhealthclinic.org",
    address = "4th Floor, Lotus Plaza",
    rating = 4.9f,
    reviewCount = 54,
    verified = true,
    themeColors = listOf(Color(0xFF06283D), Color(0xFF1363DF)),
    accentColor = Color(0xFF10B981)
  ),
  LocalAreaContact(
    id = "pro_lawy_04",
    name = "Adv. Vikrant Shinde",
    title = "Senior Legal Advisor & Corporate Counsel",
    company = "Shinde & Associates Law Firm",
    professionCode = "LAWY",
    professionTitle = "Lawyer",
    busyCardNetId = "IN-MH-MU-26-421LAWY683",
    distanceMeters = 650,
    locality = "Bandra East",
    phone = "+91 98123 45678",
    email = "vikrant@shindelaw.com",
    website = "https://shindelaw.com",
    address = "Chamber 7, Court Annex",
    rating = 4.7f,
    reviewCount = 22,
    verified = true,
    themeColors = listOf(Color(0xFF18181B), Color(0xFF27272A)),
    accentColor = Color(0xFFF43F5E)
  ),
  LocalAreaContact(
    id = "pro_carp_05",
    name = "Master Salim Khan",
    title = "Custom Furniture & Modular Interiors",
    company = "Salim Woodcraft & Interiors",
    professionCode = "CARP",
    professionTitle = "Carpenter",
    busyCardNetId = "IN-MH-MU-26-819CARP551",
    distanceMeters = 800,
    locality = "Andheri MIDC",
    phone = "+91 98222 33445",
    email = "salimwoodcraft@busycardnet.com",
    website = "https://saliminteriors.in",
    address = "Gala 4, Timber Market",
    rating = 4.8f,
    reviewCount = 31,
    verified = true,
    themeColors = listOf(Color(0xFF1C1917), Color(0xFF292524)),
    accentColor = Color(0xFFEAB308)
  ),
  LocalAreaContact(
    id = "pro_mech_06",
    name = "Vinod Sutar",
    title = "Multi-Brand Automobile Specialist",
    company = "Speedy Auto Garage & Diagnostics",
    professionCode = "MECH",
    professionTitle = "Auto Mechanic",
    busyCardNetId = "IN-MH-MU-26-112MECH394",
    distanceMeters = 1100,
    locality = "Andheri MIDC",
    phone = "+91 98333 77889",
    email = "speedyauto@busycardnet.com",
    website = "https://speedyauto.in",
    address = "Plot 89, Link Road",
    rating = 4.6f,
    reviewCount = 19,
    verified = true,
    themeColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
    accentColor = Color(0xFF38BDF8)
  ),
  LocalAreaContact(
    id = "pro_arch_07",
    name = "Ar. Meera Sengupta",
    title = "Principal Architect & Sustainable Planner",
    company = "Studio Vistara Architects",
    professionCode = "ARCH",
    professionTitle = "Architect",
    busyCardNetId = "IN-MH-MU-26-441ARCH109",
    distanceMeters = 1450,
    locality = "Powai Tech Park",
    phone = "+91 98444 11223",
    email = "meera@studiovistara.com",
    website = "https://studiovistara.com",
    address = "Tower C, Hiranandani Complex",
    rating = 4.9f,
    reviewCount = 42,
    verified = true,
    themeColors = listOf(Color(0xFF111827), Color(0xFF1F2937)),
    accentColor = Color(0xFFA855F7)
  ),
  LocalAreaContact(
    id = "pro_tech_08",
    name = "Kunal Verma",
    title = "Cloud Solutions & Enterprise IT Consultant",
    company = "Verma Cloud Systems",
    professionCode = "TECH",
    professionTitle = "IT Consultant",
    busyCardNetId = "IN-MH-MU-26-928TECH614",
    distanceMeters = 1800,
    locality = "Powai Tech Park",
    phone = "+91 98555 66778",
    email = "kunal@vermacloud.io",
    website = "https://vermacloud.io",
    address = "Tech Hub, Central Avenue",
    rating = 4.9f,
    reviewCount = 27,
    verified = true,
    themeColors = listOf(Color(0xFF022C22), Color(0xFF064E3B)),
    accentColor = Color(0xFF34D399)
  )
)

/**
 * Grid layout to display multiple DigitalCard components,
 * enabling users to browse contacts in their local area, filter by distance/locality,
 * and save contacts to local storage with 1-tap.
 */
@Composable
fun DigitalCardGrid(
  viewModel: BusyCardNetViewModel,
  modifier: Modifier = Modifier,
  onOpenCardDetails: ((DigitalCardEntity) -> Unit)? = null,
  initialLocality: String = "All Localities",
  initialRadiusMeters: Int = 2000
) {
  val context = LocalContext.current
  val savedBusinesses by viewModel.allSavedBusinesses.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedRadius by remember { mutableIntStateOf(initialRadiusMeters) }
  var selectedLocality by remember { mutableStateOf(initialLocality) }
  var selectedCategory by remember { mutableStateOf("ALL") }
  var activeTab by remember { mutableIntStateOf(0) } // 0: Browse All, 1: Saved Contacts
  var gridColumnCount by remember { mutableIntStateOf(1) } // 1 or 2 columns

  // Locality options
  val localities = listOf(
    "All Localities",
    "Kurla West",
    "Bandra East",
    "Andheri MIDC",
    "Powai Tech Park"
  )

  // Distance Radius filters
  val radiusFilters = listOf(
    0 to "All Nearby",
    500 to "500m",
    1000 to "1 km",
    2000 to "2 km",
    5000 to "5 km"
  )

  // Category filters
  val categories = listOf(
    "ALL" to "All Categories",
    "ELEC" to "⚡ Electricians",
    "PLUM" to "🔧 Plumbers",
    "DOCT" to "👨‍⚕️ Doctors",
    "LAWY" to "⚖️ Lawyers",
    "CARP" to "🪚 Carpenters",
    "MECH" to "🚗 Mechanics",
    "ARCH" to "📐 Architects",
    "TECH" to "💻 IT Consultants"
  )

  // Filtered contacts based on search, radius, locality, and category
  val filteredContacts = remember(
    searchQuery,
    selectedRadius,
    selectedLocality,
    selectedCategory,
    activeTab,
    savedBusinesses
  ) {
    defaultLocalContacts.filter { contact ->
      val isSaved = savedBusinesses.any { it.businessId == contact.busyCardNetId }

      // Tab filter
      if (activeTab == 1 && !isSaved) return@filter false

      // Locality filter
      val matchesLocality = selectedLocality == "All Localities" ||
          contact.locality.equals(selectedLocality, ignoreCase = true)

      // Radius filter
      val matchesRadius = selectedRadius == 0 || contact.distanceMeters <= selectedRadius

      // Category filter
      val matchesCategory = selectedCategory == "ALL" ||
          contact.professionCode.equals(selectedCategory, ignoreCase = true)

      // Search query filter
      val matchesSearch = searchQuery.isBlank() ||
          contact.name.contains(searchQuery, ignoreCase = true) ||
          contact.company.contains(searchQuery, ignoreCase = true) ||
          contact.title.contains(searchQuery, ignoreCase = true) ||
          contact.locality.contains(searchQuery, ignoreCase = true) ||
          contact.busyCardNetId.contains(searchQuery, ignoreCase = true)

      matchesLocality && matchesRadius && matchesCategory && matchesSearch
    }
  }

  val totalSavedCount = savedBusinesses.size

  LazyVerticalGrid(
    columns = if (gridColumnCount == 1) GridCells.Fixed(1) else GridCells.Adaptive(minSize = 340.dp),
    modifier = modifier
      .fillMaxSize()
      .testTag("digital_card_grid"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. HEADER & CONTROLS SECTION (Full Span)
    item(span = { GridItemSpan(maxLineSpan) }) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Title Bar & View Controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "LOCAL AREA CONTACTS",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.8.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Browse & save verified digital visiting cards in your neighborhood",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Column layout switcher (1 column vs 2 column grid)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable {
              gridColumnCount = if (gridColumnCount == 1) 2 else 1
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = if (gridColumnCount == 1) Icons.Default.GridView else Icons.Default.TableRows,
                contentDescription = "Toggle Grid Columns",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (gridColumnCount == 1) "2-Col Grid" else "1-Col View",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Box
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search name, company, service or BCN ID...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("digital_card_grid_search"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Main Navigation Tabs: "Browse All Local" vs "Saved Contacts (N)"
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .clickable { activeTab = 0 }
              .testTag("tab_browse_local_contacts")
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = if (activeTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Browse Local Area (${defaultLocalContacts.size})",
                fontSize = 11.sp,
                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                color = if (activeTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (activeTab == 1) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .clickable { activeTab = 1 }
              .testTag("tab_saved_contacts")
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = if (activeTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Saved Contacts ($totalSavedCount)",
                fontSize = 11.sp,
                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                color = if (activeTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Distance Radius Filter Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          radiusFilters.forEach { (radius, label) ->
            val isSelected = selectedRadius == radius
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant,
              border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)) else null,
              modifier = Modifier
                .clickable { selectedRadius = radius }
                .testTag("radius_filter_$radius")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                if (radius > 0) {
                  Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(11.dp)
                  )
                }
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Locality Filter Row
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(localities) { locality ->
            val isSelected = selectedLocality == locality
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.clickable { selectedLocality = locality }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(11.dp)
                )
                Text(
                  text = locality,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Profession Categories Row
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(categories) { (code, title) ->
            val isSelected = selectedCategory == code
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.clickable { selectedCategory = code }
            ) {
              Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }

        // Active Status & Results Counter Bar
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SHOWING ${filteredContacts.size} DIGITAL CARDS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (selectedRadius > 0 || selectedLocality != "All Localities" || selectedCategory != "ALL") {
            Text(
              text = "Reset Filters",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.clickable {
                selectedRadius = 0
                selectedLocality = "All Localities"
                selectedCategory = "ALL"
                searchQuery = ""
              }
            )
          }
        }
      }
    }

    // 2. EMPTY STATE WHEN NO CARDS MATCH
    if (filteredContacts.isEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = if (activeTab == 1) Icons.Default.BookmarkBorder else Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = if (activeTab == 1) "No Saved Contacts Yet" else "No Local Cards Found",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (activeTab == 1) {
                "Tap 'Save' on any DigitalCard to keep contacts in your personal local address book."
              } else {
                "Try widening your distance radius or clearing category filters to find nearby professionals."
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
              onClick = {
                activeTab = 0
                selectedRadius = 0
                selectedLocality = "All Localities"
                selectedCategory = "ALL"
                searchQuery = ""
              }
            ) {
              Text(if (activeTab == 1) "Browse Local Area" else "View All Local Contacts", fontSize = 11.sp)
            }
          }
        }
      }
    }

    // 3. GRID OF DIGITAL CARD COMPONENTS
    items(filteredContacts, key = { it.busyCardNetId }) { contact ->
      val isSaved = savedBusinesses.any { it.businessId == contact.busyCardNetId }
      val distanceText = if (contact.distanceMeters > 0) "${contact.distanceMeters}m away • ${contact.locality}" else contact.locality

      DigitalCard(
        name = contact.name,
        title = contact.title,
        company = contact.company,
        phone = contact.phone,
        email = contact.email,
        website = contact.website,
        address = contact.address,
        busyCardId = contact.busyCardNetId,
        distanceLabel = distanceText,
        rating = contact.rating,
        reviewCount = contact.reviewCount,
        verified = contact.verified,
        category = contact.professionTitle,
        isSaved = isSaved,
        themeColors = contact.themeColors,
        accentColor = contact.accentColor,
        onSaveContactClick = {
          val entityToSave = contact.toSavedBusinessEntity()
          viewModel.toggleSaveBusiness(entityToSave) { saved ->
            val message = if (saved) {
              "Saved ${contact.name} to your local contacts!"
            } else {
              "Removed ${contact.name} from saved contacts."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
          }
        },
        onCallClick = { phone ->
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
          try { context.startActivity(intent) } catch (e: Exception) {}
        },
        onEmailClick = { email ->
          val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
          try { context.startActivity(mailIntent) } catch (e: Exception) {}
        },
        onWebsiteClick = { url ->
          val webUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
          val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
          try { context.startActivity(webIntent) } catch (e: Exception) {}
        }
      )
    }
  }
}
