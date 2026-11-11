package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.BcnCardHyperlink
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.AuctionItemEntity
import com.example.data.local.CardTemplateEntity
import com.example.data.local.MarketplaceProductEntity
import com.example.data.local.PhysicalSupplierEntity
import com.example.data.local.ServiceRequestEntity
import com.example.data.model.RequestUrgencyFilter
import com.example.data.model.ServiceDeliveryMode
import com.example.data.model.ServiceRequestCategory
import com.example.ui.components.AuctionItemCard
import com.example.ui.components.CreateAuctionDialog
import com.example.ui.components.OutbidNotificationBanner
import com.example.ui.components.PlaceAuctionBidDialog
import com.example.ui.components.PlaceProductQuotationDialog
import com.example.ui.components.ProximityServiceBroadcastDialog
import com.example.ui.components.SellProductDialog
import com.example.ui.components.SimpleServiceBroadcastDialog
import com.example.ui.viewmodel.BusyCardNetViewModel

@Composable
fun MarketplaceScreen(
  viewModel: BusyCardNetViewModel,
  initialTab: Int = 0,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current

  // State Flows
  val products by viewModel.allProducts.collectAsStateWithLifecycle()
  val templates by viewModel.allTemplates.collectAsStateWithLifecycle()
  val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
  val filteredRequests by viewModel.filteredServiceRequests.collectAsStateWithLifecycle()
  val filterState by viewModel.serviceRequestFilter.collectAsStateWithLifecycle()
  val categoryCounts by viewModel.serviceCategoryCounts.collectAsStateWithLifecycle()
  val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
  val auctionItems by viewModel.allAuctionItems.collectAsStateWithLifecycle()
  val latestOutbidAlert by viewModel.latestOutbidAlert.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableStateOf(initialTab) }
  var showSellDialog by remember { mutableStateOf(false) }
  var showPostServiceRequestDialog by remember { mutableStateOf(false) }
  var showSimpleBroadcastDialog by remember { mutableStateOf(false) }
  var showCreateAuctionDialog by remember { mutableStateOf(false) }
  var auctionForBidding by remember { mutableStateOf<AuctionItemEntity?>(null) }
  var productForQuotation by remember { mutableStateOf<MarketplaceProductEntity?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("marketplace_screen")
  ) {

    // Top Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "MARKETPLACE & SERVICES",
          fontSize = 17.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.8.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = when (selectedTab) {
            0 -> "Categorized Service Requests (Plumbing, Freelance, Consulting...)"
            1 -> "Local Goods & Pre-Owned Items (Photos & Quotations)"
            2 -> "Live Escrow Auctions (10-20% Upfront & Real-time Timer)"
            3 -> "Digital Business Card Templates by Verified Creators"
            4 -> "NFC Smart Card Manufacturers & Printers"
            else -> "Creator Studio & Designer Royalties"
          },
          fontSize = 11.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Contextual Action Button
      when (selectedTab) {
        0 -> {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
              onClick = { showSimpleBroadcastDialog = true },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier
                .height(36.dp)
                .testTag("broadcast_service_button")
            ) {
              Icon(imageVector = Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Broadcast", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
              onClick = { showPostServiceRequestDialog = true },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier
                .height(36.dp)
                .testTag("post_service_request_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text("Detailed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
        1 -> {
          Button(
            onClick = { showSellDialog = true },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
              .height(36.dp)
              .testTag("sell_product_button")
          ) {
            Icon(imageVector = Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sell Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
        2 -> {
          Button(
            onClick = { showCreateAuctionDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
              .height(36.dp)
              .testTag("create_auction_button")
          ) {
            Icon(imageVector = Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("+ Auction", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }

    // Outbid push notification alert banner
    latestOutbidAlert?.let { alert ->
      OutbidNotificationBanner(
        alert = alert,
        onDismiss = { viewModel.dismissOutbidAlert() },
        onRebid = {
          viewModel.dismissOutbidAlert()
          val targetItem = auctionItems.find { it.id == alert.auctionItemId }
          if (targetItem != null) {
            selectedTab = 2
            auctionForBidding = targetItem
          }
        }
      )
    }

    // Segmented Tabs (Services, Goods, Auctions, Designs, Suppliers, Studio)
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = Color(0xFF1E40AF),
      edgePadding = 12.dp
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = { Text("Services (${filteredRequests.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 0) Color(0xFF1E40AF) else Color(0xFF475569)) },
        icon = { Icon(imageVector = Icons.Default.Handyman, contentDescription = null, tint = if (selectedTab == 0) Color(0xFF1E40AF) else Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_services")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = { Text("Goods (${products.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 1) Color(0xFF1E40AF) else Color(0xFF475569)) },
        icon = { Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = if (selectedTab == 1) Color(0xFF1E40AF) else Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_goods")
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = { Text("Auctions (${auctionItems.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 2) Color(0xFF1E40AF) else Color(0xFF475569)) },
        icon = { Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = if (selectedTab == 2) Color(0xFF1E40AF) else Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_auctions")
      )
      Tab(
        selected = selectedTab == 3,
        onClick = { selectedTab = 3 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = { Text("Designs (${templates.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 3) Color(0xFF1E40AF) else Color(0xFF475569)) },
        icon = { Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = if (selectedTab == 3) Color(0xFF1E40AF) else Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_designs")
      )
      Tab(
        selected = selectedTab == 4,
        onClick = { selectedTab = 4 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = { Text("Suppliers (${suppliers.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 4) Color(0xFF1E40AF) else Color(0xFF475569)) },
        icon = { Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = if (selectedTab == 4) Color(0xFF1E40AF) else Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_suppliers")
      )
      Tab(
        selected = selectedTab == 5,
        onClick = { selectedTab = 5 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = { Text("Studio", fontSize = 11.sp, fontWeight = if (selectedTab == 5) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 5) Color(0xFF1E40AF) else Color(0xFF475569)) },
        icon = { Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = if (selectedTab == 5) Color(0xFF1E40AF) else Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("tab_studio")
      )
    }

    if (selectedTab == 5) {
      CreatorsScreen(
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize()
      )
    } else if (selectedTab == 0) {
      // ==========================================
      // TAB 0: CATEGORIZED SERVICE REQUESTS DISCOVERY
      // ==========================================
      Column(modifier = Modifier.fillMaxSize()) {

        // 1. Search Bar
        OutlinedTextField(
          value = filterState.searchQuery,
          onValueChange = { viewModel.setServiceRequestSearchQuery(it) },
          placeholder = {
            Text(
              text = "Search requests (e.g. plumbing, freelance, audit)...",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
          },
          trailingIcon = {
            if (filterState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setServiceRequestSearchQuery("") }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("service_request_search_field")
        )

        // 📡 Proximity Service Request Broadcast (500m - 10000m) Hero Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { showSimpleBroadcastDialog = true }
            .testTag("broadcast_service_hero_card")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = Color(0xFF3B82F6),
              modifier = Modifier.size(40.dp)
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
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "BROADCAST SERVICE REQUEST",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF93C5FD)
                )
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFF2563EB)
                ) {
                  Text(
                    text = "FIRESTORE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "500m • 1000m • 2000m • 5000m • 10000m",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Notify nearby verified businesses • Synced to Cloud Firestore",
                fontSize = 10.sp,
                color = Color(0xFFDBEAFE)
              )
            }
            Button(
              onClick = { showSimpleBroadcastDialog = true },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(30.dp)
            ) {
              Text("Broadcast", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
          }
        }

        // 2. Primary Category Filter Chips Row (Plumbing, Freelance, Consulting, etc.)
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .testTag("service_request_category_filter_row")
        ) {
          items(ServiceRequestCategory.entries.toTypedArray()) { category ->
            val isSelected = filterState.category == category
            val count = categoryCounts[category] ?: 0
            val accentColor = Color(category.accentColorHex)

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  viewModel.setServiceRequestCategory(category)
                }
                .testTag("category_chip_${category.code}")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
              ) {
                Text(text = category.iconEmoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = category.displayName,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(5.dp))
                Surface(
                  shape = CircleShape,
                  color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                  Text(
                    text = count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                  )
                }
              }
            }
          }
        }

        // 3. Category Spotlight Banner & Quick Tag Filters (when a specific category is active)
        if (filterState.category != ServiceRequestCategory.ALL) {
          val activeCat = filterState.category
          val catColor = Color(activeCat.accentColorHex)

          Card(
            colors = CardDefaults.cardColors(containerColor = catColor.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .border(1.dp, catColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${activeCat.iconEmoji} ${activeCat.displayName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = catColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "• Category Overview", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Text(
                text = activeCat.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
              )

              // Popular quick tags
              Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "Popular:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                activeCat.popularTags.forEach { tag ->
                  val isTagActive = filterState.selectedTag == tag
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isTagActive) catColor else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = if (isTagActive) 0.8f else 0.3f)),
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .clickable { viewModel.setServiceRequestTag(tag) }
                      .testTag("tag_chip_$tag")
                  ) {
                    Text(
                      text = tag,
                      fontSize = 10.sp,
                      fontWeight = if (isTagActive) FontWeight.Bold else FontWeight.Normal,
                      color = if (isTagActive) Color.White else catColor,
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // 4. Secondary Filter Chips (Urgency, Mode, Clear All)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Urgency filters
          RequestUrgencyFilter.entries.forEach { urgency ->
            val isSelected = filterState.urgency == urgency
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setServiceRequestUrgency(urgency) },
              label = { Text(urgency.displayName, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(urgency.badgeColorHex).copy(alpha = 0.2f),
                selectedLabelColor = Color(urgency.badgeColorHex)
              ),
              modifier = Modifier.height(28.dp).testTag("urgency_chip_${urgency.name}")
            )
          }

          // Service delivery mode filters
          ServiceDeliveryMode.entries.forEach { mode ->
            val isSelected = filterState.mode == mode
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setServiceRequestDeliveryMode(mode) },
              label = { Text(mode.displayName, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier.height(28.dp).testTag("mode_chip_${mode.name}")
            )
          }

          // Clear All Filters Button (if active)
          if (filterState.hasActiveFilters) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { viewModel.clearServiceRequestFilters() }
                .testTag("clear_filters_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Reset All", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }

        // 5. Filtered Service Requests List
        if (filteredRequests.isEmpty()) {
          ServiceRequestEmptyState(
            category = filterState.category,
            hasActiveFilters = filterState.hasActiveFilters,
            onClearFilters = { viewModel.clearServiceRequestFilters() },
            onPostRequest = { showPostServiceRequestDialog = true }
          )
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Showing ${filteredRequests.size} request${if (filteredRequests.size == 1) "" else "s"}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (filterState.selectedTag != null) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Filtered by: ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                      modifier = Modifier.clickable { viewModel.setServiceRequestTag(null) }
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)) {
                        Text(text = filterState.selectedTag!!, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Tag", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                      }
                    }
                  }
                }
              }
            }

            items(filteredRequests, key = { it.requestId }) { request ->
              ServiceRequestMarketplaceCard(
                request = request,
                onTagClick = { tag -> viewModel.setServiceRequestTag(tag) },
                onRespond = {
                  viewModel.respondToRequest(request.requestId)
                  Toast.makeText(context, "Proposal sent to ${request.requesterName}!", Toast.LENGTH_SHORT).show()
                },
                onCall = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                  context.startActivity(intent)
                },
                onWhatsApp = {
                  val msg = "Hello ${request.requesterName}, I saw your ${request.category} request on BusyCardNet: '${request.displayTitle}'"
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919876543210?text=${Uri.encode(msg)}"))
                  try {
                    context.startActivity(intent)
                  } catch (e: Exception) {
                    Toast.makeText(context, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
                  }
                }
              )
            }
          }
        }
      }
    } else {
      // Content for Tabs 1 (Goods), 2 (Auctions), 3 (Designs), 4 (Suppliers)
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        when (selectedTab) {
          1 -> {
            items(products, key = { it.id }) { product ->
              MarketplaceProductCard(
                product = product,
                onContact = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${product.contactPhone}"))
                  context.startActivity(intent)
                },
                onWhatsApp = {
                  val url = "https://wa.me/${product.contactPhone.replace("+", "").replace(" ", "")}?text=Hi%2C+I+am+interested+in+your+item+on+BusyCardNet+Marketplace%3A+${Uri.encode(product.title)}"
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                  context.startActivity(intent)
                },
                onMakeOffer = {
                  productForQuotation = product
                }
              )
            }
          }
          2 -> {
            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = CircleShape,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.size(40.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "LIVE LOCAL ESCROW AUCTIONS",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFF1E3A8A)
                    )
                    Text(
                      text = "Bid by paying 10-20% upfront deposit. Highest bidder wins the product; all outbid participants get 100% of their money back immediately.",
                      fontSize = 10.sp,
                      color = Color(0xFF1E40AF)
                    )
                  }
                }
              }
            }

            if (auctionItems.isEmpty()) {
              item {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No active auctions right now", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Be the first to list a product for auction!", fontSize = 12.sp, color = Color(0xFF94A3B8))
                  }
                }
              }
            } else {
              items(auctionItems, key = { it.id }) { item ->
                AuctionItemCard(
                  item = item,
                  onBidClick = { auctionForBidding = item },
                  onTestOutbid = {
                    viewModel.simulateOutbidOnItem(item.id)
                    Toast.makeText(context, "Simulated competing bid! Push notification triggered.", Toast.LENGTH_SHORT).show()
                  },
                  onExpired = {
                    viewModel.expireAuction(item.id)
                  },
                  onHandoverToStorage = {
                    viewModel.handoverAuctionToStorage(item.id) {
                      Toast.makeText(context, "Product handed over to BCN Storage Hub!", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onDispatchFromStorage = {
                    viewModel.dispatchAuctionFromStorage(item.id) {
                      Toast.makeText(context, "Product dispatched from BCN Storage for delivery!", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onConfirmDelivery = {
                    viewModel.confirmAuctionDelivery(item.id) {
                      Toast.makeText(context, "Delivery confirmed! Escrow funds released to seller.", Toast.LENGTH_SHORT).show()
                    }
                  }
                )
              }
            }
          }
          3 -> {
            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "🎨 CREATOR STUDIO: BUILD TEMPLATES",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFFF59E0B)
                    )
                    Text(
                      text = "Design cards with all touchpoint icons (LinkedIn, WhatsApp, etc.) & earn royalties",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Button(
                    onClick = { selectedTab = 5 },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Text("Open Studio", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            items(templates, key = { it.id }) { template ->
              TemplateMarketplaceCard(
                template = template,
                onApplyTemplate = {
                  Toast.makeText(context, "Template applied to your card draft!", Toast.LENGTH_SHORT).show()
                }
              )
            }
          }
          4 -> {
            items(suppliers, key = { it.id }) { supplier ->
              SupplierCard(
                supplier = supplier,
                onOrder = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919820011223"))
                  context.startActivity(intent)
                }
              )
            }
          }
        }
      }
    }
  }

  // Dialogs
  if (showSellDialog) {
    SellProductDialog(
      onDismiss = { showSellDialog = false },
      onSubmit = { title, desc, price, condition, category, pickup, imageUrl ->
        viewModel.sellProduct(title, desc, price, condition, category, pickup, imageUrl)
        showSellDialog = false
        Toast.makeText(context, "Item listed in local marketplace!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  if (showCreateAuctionDialog) {
    CreateAuctionDialog(
      onDismiss = { showCreateAuctionDialog = false },
      onSubmit = { title, desc, category, imageUrl, startingPrice, upfrontPercent, durationDays ->
        viewModel.createAuction(title, desc, category, imageUrl, startingPrice, upfrontPercent, durationDays) {
          showCreateAuctionDialog = false
          selectedTab = 2
          Toast.makeText(context, "🎉 Auction listing live with ${upfrontPercent}% upfront escrow!", Toast.LENGTH_SHORT).show()
        }
      }
    )
  }

  auctionForBidding?.let { item ->
    PlaceAuctionBidDialog(
      item = item,
      onDismiss = { auctionForBidding = null },
      onConfirmBid = { bidAmount ->
        viewModel.placeAuctionBid(
          itemId = item.id,
          bidAmount = bidAmount,
          onSuccess = {
            auctionForBidding = null
            Toast.makeText(context, "✅ Bid placed! Escrow deposit held securely.", Toast.LENGTH_SHORT).show()
          }
        )
      }
    )
  }

  productForQuotation?.let { prod ->
    PlaceProductQuotationDialog(
      product = prod,
      onDismiss = { productForQuotation = null },
      onSubmitQuotation = { offeredPrice, message ->
        viewModel.submitProductQuotation(prod.id, offeredPrice, message) {
          productForQuotation = null
          Toast.makeText(context, "✅ Quotation of ₹${offeredPrice.toInt()} submitted to ${prod.sellerName}!", Toast.LENGTH_LONG).show()
        }
      }
    )
  }

  if (showPostServiceRequestDialog) {
    ProximityServiceBroadcastDialog(
      initialProfessionCode = if (filterState.category != ServiceRequestCategory.ALL) filterState.category.code else "PLUM",
      userLocality = selectedLocation,
      onDismiss = { showPostServiceRequestDialog = false },
      onBroadcastSubmit = { title, category, categoryCode, desc, budget, urgency, mode, tags, radius, photo, locality ->
        viewModel.broadcastProximityServiceRequest(
          title = title,
          category = category,
          categoryCode = categoryCode,
          problemDescription = desc,
          budget = budget,
          urgency = urgency,
          serviceMode = mode,
          tags = tags,
          radiusMeters = radius,
          photoAttached = photo,
          customLocality = locality
        )
        showPostServiceRequestDialog = false
        viewModel.setServiceRequestCategory(ServiceRequestCategory.fromCode(categoryCode))
        Toast.makeText(context, "📡 Proximity broadcast active under $category (${radius}m)!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Simple Service Broadcast Dialog (500m, 1000m, 2000m, 5000m, 10000m)
  if (showSimpleBroadcastDialog) {
    SimpleServiceBroadcastDialog(
      userLocality = selectedLocation,
      onDismiss = { showSimpleBroadcastDialog = false },
      onBroadcastSubmit = { title, category, categoryCode, desc, budget, urgency, radius ->
        viewModel.broadcastProximityServiceRequest(
          title = title,
          category = category,
          categoryCode = categoryCode,
          problemDescription = desc,
          budget = budget,
          urgency = urgency,
          serviceMode = "ON_SITE",
          tags = categoryCode,
          radiusMeters = radius,
          photoAttached = false,
          customLocality = selectedLocation
        )
        showSimpleBroadcastDialog = false
        viewModel.setServiceRequestCategory(ServiceRequestCategory.fromCode(categoryCode))
        val count = try {
          com.example.ui.map.LocalPlaceDataProvider.getCuratedPlaces()
            .count { it.distanceMeters <= radius }
        } catch (_: Exception) { 5 }
        Toast.makeText(
          context,
          "📡 Broadcast sent! $count businesses within ${radius}m notified & stored in Firestore.",
          Toast.LENGTH_LONG
        ).show()
      }
    )
  }
}

/**
 * Service Request Card for Marketplace
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceRequestMarketplaceCard(
  request: ServiceRequestEntity,
  onTagClick: (String) -> Unit,
  onRespond: () -> Unit,
  onCall: () -> Unit,
  onWhatsApp: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cat = ServiceRequestCategory.fromCode(request.categoryCode)
  val catColor = Color(cat.accentColorHex)

  val isUrgent = request.urgency.equals("URGENT", ignoreCase = true)
  val isRemote = request.serviceMode.equals("REMOTE", ignoreCase = true)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, catColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
      .testTag("service_request_card_${request.requestId}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {

      // Row 1: Badges (Category + Urgency + Delivery Mode)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Category Pill
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = catColor.copy(alpha = 0.14f)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
          ) {
            Text(text = cat.iconEmoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = request.category.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.5.sp,
              color = catColor
            )
          }
        }

        // Urgency & Delivery Mode
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          // Delivery Mode Pill
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isRemote) Color(0xFF8B5CF6).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
              Text(
                text = if (isRemote) "💻 Remote" else "📍 ${request.distanceMeters}m",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isRemote) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Urgency Pill
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isUrgent) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
          ) {
            Text(
              text = if (isUrgent) "⚡ URGENT" else request.urgency,
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = if (isUrgent) Color(0xFFEF4444) else Color(0xFF10B981),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Title
      Text(
        text = request.displayTitle,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      // Problem Description
      Text(
        text = request.problemDescription,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 3.dp, bottom = 6.dp)
      )

      // Tags Row
      val tags = request.tagList
      if (tags.isNotEmpty()) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(bottom = 8.dp)
        ) {
          tags.forEach { tag ->
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { onTagClick(tag) }
            ) {
              Text(
                text = "#$tag",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      // Budget & Meta Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Budget: ${request.budget}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "Response target: ${request.expiryMinutes}m",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Requester info & Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "👤 ${request.requesterName}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          BcnCardHyperlink(
            bcnId = request.requesterBusyCardNetId,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp)
          )
          Text(
            text = "📍 ${request.requesterLocality}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = onCall,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("Call", fontSize = 10.sp)
          }

          Button(
            onClick = onWhatsApp,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }

          Button(
            onClick = onRespond,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = catColor),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp).testTag("respond_button_${request.requestId}")
          ) {
            Text("I'm Interested", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

/**
 * Empty State for Service Requests
 */
@Composable
fun ServiceRequestEmptyState(
  category: ServiceRequestCategory,
  hasActiveFilters: Boolean,
  onClearFilters: () -> Unit,
  onPostRequest: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = if (category != ServiceRequestCategory.ALL) category.iconEmoji else "🔍",
      fontSize = 44.sp
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = if (category != ServiceRequestCategory.ALL) "No ${category.displayName} Requests Found" else "No Service Requests Found",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = if (hasActiveFilters) {
        "No active requests match your current filters. Try resetting the filters or posting a new request."
      } else {
        "Be the first to post a service request in this category to get nearby professionals to respond."
      },
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      if (hasActiveFilters) {
        OutlinedButton(onClick = onClearFilters, shape = RoundedCornerShape(8.dp)) {
          Text("Clear Filters", fontSize = 12.sp)
        }
      }
      Button(
        onClick = onPostRequest,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (category != ServiceRequestCategory.ALL) Color(category.accentColorHex) else MaterialTheme.colorScheme.primary)
      ) {
        Text("+ Post Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

/**
 * Dialog to Post a Categorized Service Request
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostMarketplaceServiceRequestDialog(
  initialCategory: ServiceRequestCategory = ServiceRequestCategory.PLUMBING,
  onDismiss: () -> Unit,
  onSubmit: (
    title: String,
    category: String,
    categoryCode: String,
    description: String,
    budget: String,
    urgency: String,
    mode: String,
    tags: String,
    radiusMeters: Int
  ) -> Unit
) {
  var selectedCategory by remember { mutableStateOf(if (initialCategory == ServiceRequestCategory.ALL) ServiceRequestCategory.PLUMBING else initialCategory) }
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var budget by remember { mutableStateOf("") }
  var selectedUrgency by remember { mutableStateOf("STANDARD") }
  var selectedMode by remember { mutableStateOf("ON_SITE") }
  var selectedRadius by remember { mutableStateOf(500) }
  var selectedTags by remember { mutableStateOf(setOf<String>()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "📢 Post Service Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {

        // 1. Select Category
        Text(text = "Select Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ServiceRequestCategory.entries.filter { it != ServiceRequestCategory.ALL }.forEach { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(cat.accentColorHex) else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  selectedCategory = cat
                  selectedTags = emptySet()
                }
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                Text(text = cat.iconEmoji, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = cat.displayName,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        // 2. Title
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Request Title", fontSize = 11.sp) },
          placeholder = {
            Text(
              text = when (selectedCategory) {
                ServiceRequestCategory.PLUMBING -> "e.g. Bathroom pipe leak repair"
                ServiceRequestCategory.FREELANCE -> "e.g. Android Jetpack Compose developer"
                ServiceRequestCategory.CONSULTING -> "e.g. GST filing and audit advice"
                else -> "e.g. Need service help"
              },
              fontSize = 11.sp
            )
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("post_request_title_input")
        )

        // 3. Problem Description
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Problem Description", fontSize = 11.sp) },
          placeholder = { Text("Describe the task, timeline, and requirements in detail...", fontSize = 11.sp) },
          minLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("post_request_desc_input")
        )

        // 4. Quick Tag Suggestions for Selected Category
        Text(text = "Suggested Tags (Tap to Add):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          selectedCategory.popularTags.forEach { tag ->
            val isTagSelected = selectedTags.contains(tag)
            val catColor = Color(selectedCategory.accentColorHex)
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (isTagSelected) catColor else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                  selectedTags = if (isTagSelected) selectedTags - tag else selectedTags + tag
                }
            ) {
              Text(
                text = "${if (isTagSelected) "✓ " else "+ "}$tag",
                fontSize = 10.sp,
                color = if (isTagSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }

        // 5. Budget Estimate
        OutlinedTextField(
          value = budget,
          onValueChange = { budget = it },
          label = { Text("Budget Estimate", fontSize = 11.sp) },
          placeholder = { Text("e.g. ₹800 - ₹1,500, or Fixed ₹25,000", fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("post_request_budget_input")
        )

        // 6. Urgency Selection
        Text(text = "Urgency Level:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("URGENT" to "⚡ Urgent (<2h)", "STANDARD" to "Standard (Today)", "FLEXIBLE" to "Flexible").forEach { (code, label) ->
            val isSelected = selectedUrgency == code
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedUrgency = code }
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              )
            }
          }
        }

        // 7. Delivery Mode & Radius
        Text(text = "Service Delivery Mode:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("ON_SITE" to "📍 Local On-Site", "REMOTE" to "💻 Remote Work").forEach { (mode, label) ->
            val isSelected = selectedMode == mode
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedMode = mode }
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              )
            }
          }
        }

        if (selectedMode == "ON_SITE") {
          Text(text = "Search Radius:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
              500 to "500m",
              1000 to "1 km",
              2000 to "2 km",
              5000 to "5 km",
              10000 to "10 km"
            ).forEach { (r, label) ->
              val isSelected = selectedRadius == r
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable { selectedRadius = r }
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isBlank()) {
            title = "${selectedCategory.displayName} Service Request"
          }
          if (description.isBlank()) {
            description = "Looking for verified ${selectedCategory.displayName} service provider."
          }
          onSubmit(
            title,
            selectedCategory.displayName,
            selectedCategory.code,
            description,
            budget,
            selectedUrgency,
            selectedMode,
            selectedTags.joinToString(", "),
            selectedRadius
          )
        },
        modifier = Modifier.testTag("submit_post_service_request_button")
      ) {
        Text("Post Request", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

/**
 * Local Product Listing Item Card (Prompt Section 21)
 */
@Composable
fun MarketplaceProductCard(
  product: MarketplaceProductEntity,
  onContact: () -> Unit,
  onWhatsApp: () -> Unit,
  onMakeOffer: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .testTag("product_item_${product.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Photo of item for sale
      if (product.imageUrl.isNotBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(10.dp))
        ) {
          AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
          Text(
            text = product.category,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (product.condition == "NEW") Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = if (product.condition == "NEW") "Brand New" else "Used Item",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (product.condition == "NEW") Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = product.title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "Sold by: ${product.sellerName}",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )

      BcnCardHyperlink(
        bcnId = product.sellerBusyCardNetId,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 1.dp, bottom = 2.dp)
      )

      Text(
        text = product.description,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "₹${product.price.toInt()}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "📍 ${product.locationName} (${product.distanceMeters}m)",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Quotation / Make Offer Button
          Button(
            onClick = onMakeOffer,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("make_offer_button_${product.id}")
          ) {
            Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Quote", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }

          OutlinedButton(
            onClick = onContact,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Call", fontSize = 11.sp)
          }

          Button(
            onClick = onWhatsApp,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

/**
 * Designer Card Template Item (Prompt Section 12)
 */
@Composable
fun TemplateMarketplaceCard(
  template: CardTemplateEntity,
  onApplyTemplate: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column {
          Text(text = template.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          Text(text = "By ${template.creatorName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          BcnCardHyperlink(
            bcnId = template.creatorBusyCardNetId,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp)
          )
          Text(
            text = "Category: ${template.professionCode}",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = if (template.price == 0.0) "FREE" else "₹${template.price.toInt()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = if (template.price == 0.0) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "${template.rating} (${template.downloadsCount})", fontSize = 10.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Button(
        onClick = onApplyTemplate,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().height(36.dp)
      ) {
        Text("Use This Design in Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

/**
 * Card Supplier / NFC Manufacturer Card (Prompt Section 13 & 14)
 */
@Composable
fun SupplierCard(
  supplier: PhysicalSupplierEntity,
  onOrder: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = supplier.companyName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
          }
          BcnCardHyperlink(
            bcnId = supplier.busyCardNetId,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp)
          )
          Text(text = "Card Types: ${supplier.cardTypesOffered}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
          Text(
            text = "From ₹${supplier.pricePerUnit.toInt()}/card",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Delivery in ${supplier.deliveryDays} days", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Button(
          onClick = onOrder,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(34.dp)
        ) {
          Text("Contact & Order Cards", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
