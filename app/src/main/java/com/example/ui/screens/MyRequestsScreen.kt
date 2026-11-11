package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.BcnCardHyperlink
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ServiceRequestEntity
import com.example.ui.theme.FontElectricBlue
import com.example.ui.theme.FontMutedBlue
import com.example.ui.theme.FontNavyHeadings
import com.example.ui.theme.FontRoyalBlue
import com.example.ui.viewmodel.BusyCardNetViewModel

enum class MyRequestFilter {
  ALL,
  ACTIVE,
  IN_DISCUSSION,
  COMPLETED
}

enum class MyRequestSortOption(val displayName: String) {
  DATE_DESC("Newest"),
  DATE_ASC("Oldest"),
  CATEGORY("Category A-Z"),
  STATUS("Status")
}

/**
 * 'My Requests' tab allowing users to view, edit, or delete active service requests stored in Firestore.
 * Features a bright light mode design with royal and electric blue fonts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyRequestsScreen(
  viewModel: BusyCardNetViewModel,
  onOpenServiceRequestDialog: () -> Unit = {},
  onOpenChat: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val requests by viewModel.myServiceRequests.collectAsStateWithLifecycle()
  var currentFilter by remember { mutableStateOf(MyRequestFilter.ALL) }
  var sortOption by remember { mutableStateOf(MyRequestSortOption.DATE_DESC) }
  var selectedCategory by remember { mutableStateOf("ALL") }
  var searchQuery by remember { mutableStateOf("") }

  // Dialog states for Edit and Delete
  var requestToEdit by remember { mutableStateOf<ServiceRequestEntity?>(null) }
  var requestToDelete by remember { mutableStateOf<ServiceRequestEntity?>(null) }

  val availableCategories = remember(requests) {
    listOf("ALL") + requests.map { it.category.trim() }.filter { it.isNotBlank() }.distinct().sorted()
  }

  val filteredAndSortedRequests = remember(requests, currentFilter, selectedCategory, searchQuery, sortOption) {
    val filtered = requests.filter { req ->
      val matchesStatus = when (currentFilter) {
        MyRequestFilter.ALL -> true
        MyRequestFilter.ACTIVE -> req.status == "SENT" || req.status == "CREATED"
        MyRequestFilter.IN_DISCUSSION -> req.status == "INTERESTED" || req.status == "ACCEPTED"
        MyRequestFilter.COMPLETED -> req.status == "COMPLETED" || req.status == "CANCELLED"
      }
      val matchesCategory = selectedCategory == "ALL" || req.category.equals(selectedCategory, ignoreCase = true)
      val matchesSearch = searchQuery.isBlank() ||
        req.title.contains(searchQuery, ignoreCase = true) ||
        req.problemDescription.contains(searchQuery, ignoreCase = true) ||
        req.category.contains(searchQuery, ignoreCase = true) ||
        req.tags.contains(searchQuery, ignoreCase = true)

      matchesStatus && matchesCategory && matchesSearch
    }

    when (sortOption) {
      MyRequestSortOption.DATE_DESC -> filtered.sortedByDescending { it.createdAt }
      MyRequestSortOption.DATE_ASC -> filtered.sortedBy { it.createdAt }
      MyRequestSortOption.CATEGORY -> filtered.sortedBy { it.category.lowercase() }
      MyRequestSortOption.STATUS -> filtered.sortedWith(
        compareBy<ServiceRequestEntity> {
          when (it.status.uppercase()) {
            "SENT", "CREATED" -> 0
            "INTERESTED", "ACCEPTED" -> 1
            "COMPLETED" -> 2
            else -> 3
          }
        }.thenByDescending { it.createdAt }
      )
    }
  }

  val activeCount = requests.count { it.status == "SENT" || it.status == "CREATED" }
  val inDiscussionCount = requests.count { it.status == "INTERESTED" || it.status == "ACCEPTED" }
  val completedCount = requests.count { it.status == "COMPLETED" }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC)) // Crisp Light Mode Background
      .testTag("my_requests_screen")
  ) {
    // Top Bar Header
    Surface(
      color = Color.White,
      shadowElevation = 2.dp,
      border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "MY SERVICE REQUESTS",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.5.sp,
              color = FontNavyHeadings
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Firestore Cloud Sync",
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Firestore Cloud Synced • Collection 'service_requests'",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = FontElectricBlue
              )
            }
          }

          // + Broadcast Request Button
          Button(
            onClick = onOpenServiceRequestDialog,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2563EB),
              contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
              .height(38.dp)
              .testTag("my_requests_new_broadcast_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Broadcast", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              "Search my requests by title, problem, or trade...",
              fontSize = 12.sp,
              color = FontMutedBlue
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = FontElectricBlue,
              modifier = Modifier.size(18.dp)
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF0F7FF),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedBorderColor = Color(0xFF2563EB),
            unfocusedBorderColor = Color(0xFFDBEAFE),
            focusedTextColor = FontNavyHeadings,
            unfocusedTextColor = FontRoyalBlue
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("my_requests_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            FilterChip(
              selected = currentFilter == MyRequestFilter.ALL,
              onClick = { currentFilter = MyRequestFilter.ALL },
              label = {
                Text(
                  "All (${requests.size})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFDBEAFE),
                selectedLabelColor = FontNavyHeadings,
                containerColor = Color.White,
                labelColor = FontRoyalBlue
              ),
              border = BorderStroke(
                1.dp,
                if (currentFilter == MyRequestFilter.ALL) Color(0xFF2563EB) else Color(0xFFDBEAFE)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("filter_chip_all")
            )
          }
          item {
            FilterChip(
              selected = currentFilter == MyRequestFilter.ACTIVE,
              onClick = { currentFilter = MyRequestFilter.ACTIVE },
              label = {
                Text(
                  "Active / Live ($activeCount)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFDBEAFE),
                selectedLabelColor = FontNavyHeadings,
                containerColor = Color.White,
                labelColor = FontRoyalBlue
              ),
              border = BorderStroke(
                1.dp,
                if (currentFilter == MyRequestFilter.ACTIVE) Color(0xFF2563EB) else Color(0xFFDBEAFE)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("filter_chip_active")
            )
          }
          item {
            FilterChip(
              selected = currentFilter == MyRequestFilter.IN_DISCUSSION,
              onClick = { currentFilter = MyRequestFilter.IN_DISCUSSION },
              label = {
                Text(
                  "Responded ($inDiscussionCount)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFDBEAFE),
                selectedLabelColor = FontNavyHeadings,
                containerColor = Color.White,
                labelColor = FontRoyalBlue
              ),
              border = BorderStroke(
                1.dp,
                if (currentFilter == MyRequestFilter.IN_DISCUSSION) Color(0xFF2563EB) else Color(0xFFDBEAFE)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("filter_chip_in_discussion")
            )
          }
          item {
            FilterChip(
              selected = currentFilter == MyRequestFilter.COMPLETED,
              onClick = { currentFilter = MyRequestFilter.COMPLETED },
              label = {
                Text(
                  "Completed ($completedCount)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFDBEAFE),
                selectedLabelColor = FontNavyHeadings,
                containerColor = Color.White,
                labelColor = FontRoyalBlue
              ),
              border = BorderStroke(
                1.dp,
                if (currentFilter == MyRequestFilter.COMPLETED) Color(0xFF2563EB) else Color(0xFFDBEAFE)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("filter_chip_completed")
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sorting options row (Date, Category, Status)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Sort,
              contentDescription = "Sort By",
              tint = FontElectricBlue,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              "Sort:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = FontNavyHeadings
            )
          }

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(MyRequestSortOption.entries) { option ->
              val isSelected = sortOption == option
              FilterChip(
                selected = isSelected,
                onClick = { sortOption = option },
                label = {
                  Text(
                    option.displayName,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                },
                leadingIcon = {
                  val icon = when (option) {
                    MyRequestSortOption.DATE_DESC, MyRequestSortOption.DATE_ASC -> Icons.Default.Schedule
                    MyRequestSortOption.CATEGORY -> Icons.Default.Category
                    MyRequestSortOption.STATUS -> Icons.Default.CheckCircle
                  }
                  Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = Color(0xFF2563EB),
                  selectedLabelColor = Color.White,
                  selectedLeadingIconColor = Color.White,
                  containerColor = Color(0xFFF1F5F9),
                  labelColor = FontRoyalBlue,
                  iconColor = FontMutedBlue
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                  1.dp,
                  if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier.testTag("sort_chip_${option.name.lowercase()}")
              )
            }
          }
        }

        // Category Filter Chips (if multiple categories are present)
        if (availableCategories.size > 2) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "Category:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = FontMutedBlue,
              modifier = Modifier.padding(end = 6.dp)
            )
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(availableCategories) { cat ->
                val isSel = selectedCategory == cat
                FilterChip(
                  selected = isSel,
                  onClick = { selectedCategory = cat },
                  label = {
                    Text(
                      cat,
                      fontSize = 10.sp,
                      fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFDBEAFE),
                    selectedLabelColor = Color(0xFF1E40AF),
                    containerColor = Color.White,
                    labelColor = FontRoyalBlue
                  ),
                  shape = RoundedCornerShape(14.dp),
                  border = BorderStroke(
                    1.dp,
                    if (isSel) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                  )
                )
              }
            }
          }
        }
      }
    }

    // Requests List or Empty State
    if (filteredAndSortedRequests.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.5.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.size(76.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Handyman,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(38.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "No Service Requests Found",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = FontNavyHeadings
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Need quick repairs, freelance help, or local services? Broadcast a task directly to verified professionals within 500 meters!",
            fontSize = 13.sp,
            color = FontRoyalBlue,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = onOpenServiceRequestDialog,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2563EB),
              contentColor = Color.White
            ),
            modifier = Modifier.testTag("empty_state_broadcast_button")
          ) {
            Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Broadcast 500m Service Request", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("my_requests_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(filteredAndSortedRequests, key = { it.requestId }) { req ->
          MyServiceRequestCard(
            request = req,
            onEdit = { requestToEdit = req },
            onDelete = { requestToDelete = req },
            onExpandRadius = { newRadius ->
              viewModel.expandRequestRadius(req.requestId, newRadius)
              Toast.makeText(context, "Radius expanded to ${newRadius}m in Firestore", Toast.LENGTH_SHORT).show()
            },
            onOpenChat = { onOpenChat(req.requestId) },
            onMarkCompleted = {
              viewModel.updateRequestStatus(req.requestId, "COMPLETED")
              Toast.makeText(context, "Marked as completed in Firestore", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }
  }

  // Edit Service Request Dialog
  requestToEdit?.let { req ->
    EditServiceRequestDialog(
      request = req,
      onDismiss = { requestToEdit = null },
      onConfirmEdit = { updatedTitle, updatedProblem, updatedBudget, updatedUrgency, updatedRadius ->
        viewModel.editServiceRequest(
          requestId = req.requestId,
          title = updatedTitle,
          problemDescription = updatedProblem,
          budget = updatedBudget,
          urgency = updatedUrgency,
          radiusMeters = updatedRadius
        )
        Toast.makeText(context, "Request updated in Firestore", Toast.LENGTH_SHORT).show()
        requestToEdit = null
      }
    )
  }

  // Delete Confirmation Dialog
  requestToDelete?.let { req ->
    AlertDialog(
      onDismissRequest = { requestToDelete = null },
      shape = RoundedCornerShape(16.dp),
      containerColor = Color.White,
      icon = {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete Request",
          tint = Color(0xFFEF4444),
          modifier = Modifier.size(28.dp)
        )
      },
      title = {
        Text(
          text = "Delete Service Request?",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = FontNavyHeadings
        )
      },
      text = {
        Column {
          Text(
            text = "Are you sure you want to delete '${req.displayTitle}'?",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FontRoyalBlue
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "This will permanently delete this document from Firebase Firestore ('service_requests') and close active proximity broadcasts for this task.",
            fontSize = 12.sp,
            color = FontMutedBlue
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteServiceRequest(req.requestId)
            Toast.makeText(context, "Service request deleted from Firestore", Toast.LENGTH_SHORT).show()
            requestToDelete = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF4444),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("confirm_delete_request_button")
        ) {
          Text("Delete from Firestore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { requestToDelete = null },
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
          Text("Cancel", fontSize = 12.sp, color = FontRoyalBlue)
        }
      },
      modifier = Modifier.testTag("delete_service_request_dialog")
    )
  }
}

/**
 * Individual Service Request Card in Light Mode with Blue Fonts and actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyServiceRequestCard(
  request: ServiceRequestEntity,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onExpandRadius: (Int) -> Unit,
  onOpenChat: () -> Unit,
  onMarkCompleted: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("my_request_item_${request.requestId}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Top Status Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Category Pill
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFFEFF6FF),
          border = BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Handyman,
              contentDescription = null,
              tint = Color(0xFF2563EB),
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = request.category.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = FontNavyHeadings
            )
          }
        }

        // Urgency & Status Chips
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Urgency chip
          val urgencyBg = when (request.urgency.uppercase()) {
            "URGENT" -> Color(0xFFFEF2F2)
            "FLEXIBLE" -> Color(0xFFECFDF5)
            else -> Color(0xFFF0F9FF)
          }
          val urgencyBorder = when (request.urgency.uppercase()) {
            "URGENT" -> Color(0xFFFCA5A5)
            "FLEXIBLE" -> Color(0xFF6EE7B7)
            else -> Color(0xFFBAE6FD)
          }
          val urgencyText = when (request.urgency.uppercase()) {
            "URGENT" -> Color(0xFFDC2626)
            "FLEXIBLE" -> Color(0xFF059669)
            else -> Color(0xFF0284C7)
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = urgencyBg,
            border = BorderStroke(1.dp, urgencyBorder)
          ) {
            Text(
              text = request.urgency,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = urgencyText,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          // Status Chip
          val (statusText, statusBg, statusColor) = when (request.status) {
            "SENT" -> Triple("LIVE BROADCAST", Color(0xFFEFF6FF), Color(0xFF2563EB))
            "INTERESTED" -> Triple("PRO RESPONDED", Color(0xFFFEF3C7), Color(0xFFD97706))
            "ACCEPTED" -> Triple("ACCEPTED", Color(0xFFECFDF5), Color(0xFF059669))
            "COMPLETED" -> Triple("COMPLETED", Color(0xFFF1F5F9), Color(0xFF475569))
            else -> Triple(request.status, Color(0xFFEFF6FF), Color(0xFF2563EB))
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusBg,
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
          ) {
            Text(
              text = statusText,
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              color = statusColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Title in Blue
      Text(
        text = request.displayTitle,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = FontNavyHeadings,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Description in Blue
      Text(
        text = request.problemDescription,
        fontSize = 13.sp,
        color = FontRoyalBlue,
        lineHeight = 18.sp,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Requester: ${request.requesterName}",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = FontRoyalBlue
        )
      }
      BcnCardHyperlink(
        bcnId = request.requesterBusyCardNetId,
        color = Color(0xFF2563EB),
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 1.dp, bottom = 2.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Metadata Row
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // Budget
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
          Text(text = request.budget, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FontNavyHeadings)
        }

        // Radius
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Radar, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = "${request.radiusMeters}m proximity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FontElectricBlue)
        }

        // Locality
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = FontMutedBlue, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = request.requesterLocality, fontSize = 11.sp, color = FontRoyalBlue, maxLines = 1)
        }

        // Photo attached indicator
        if (request.photoAttached) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "Photo Attached", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0284C7))
          }
        }
      }

      // Responding Professional Section (if someone has responded)
      if (!request.respondingProfessionalName.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFF0FDF4),
          border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Professional Response",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF166534)
              )
              Text(
                text = "${request.respondingProfessionalName} (★ ${request.respondingProfessionalRating})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FontNavyHeadings
              )
              if (!request.respondingProfessionalBusyCardNetId.isNullOrBlank()) {
                BcnCardHyperlink(
                  bcnId = request.respondingProfessionalBusyCardNetId,
                  color = Color(0xFF166534),
                  fontSize = 10.sp,
                  modifier = Modifier.padding(top = 1.dp)
                )
              }
            }
            Button(
              onClick = onOpenChat,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons (Edit, Delete, Expand Radius, Mark Done)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Expand radius button if <= 1000m
        if (request.radiusMeters < 2000 && request.status != "COMPLETED") {
          OutlinedButton(
            onClick = { onExpandRadius(request.radiusMeters + 500) },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Icon(Icons.Default.Radar, contentDescription = null, tint = FontElectricBlue, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("+500m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FontElectricBlue)
          }
        } else {
          Spacer(modifier = Modifier.width(4.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Edit Button
          OutlinedButton(
            onClick = onEdit,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF2563EB)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB)),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier
              .height(32.dp)
              .testTag("edit_request_${request.requestId}")
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Request", modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          // Delete Button
          OutlinedButton(
            onClick = onDelete,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFEF4444)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier
              .height(32.dp)
              .testTag("delete_request_${request.requestId}")
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Request", modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Dialog to edit an active Service Request and save changes to Firestore and Room.
 */
@Composable
fun EditServiceRequestDialog(
  request: ServiceRequestEntity,
  onDismiss: () -> Unit,
  onConfirmEdit: (title: String, problem: String, budget: String, urgency: String, radius: Int) -> Unit
) {
  var title by remember { mutableStateOf(request.title) }
  var problemDescription by remember { mutableStateOf(request.problemDescription) }
  var budget by remember { mutableStateOf(request.budget) }
  var urgency by remember { mutableStateOf(request.urgency) }
  var radiusMeters by remember { mutableIntStateOf(request.radiusMeters) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Edit Service Request",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = FontNavyHeadings
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Update request details. Changes will immediately sync to Firebase Firestore.",
          fontSize = 12.sp,
          color = FontRoyalBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title field
        Text(text = "Task / Request Title", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FontNavyHeadings)
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = FontNavyHeadings,
            unfocusedTextColor = FontRoyalBlue,
            focusedBorderColor = Color(0xFF2563EB),
            unfocusedBorderColor = Color(0xFFDBEAFE)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_request_title_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Problem description
        Text(text = "Problem Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FontNavyHeadings)
        OutlinedTextField(
          value = problemDescription,
          onValueChange = { problemDescription = it },
          maxLines = 4,
          shape = RoundedCornerShape(8.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = FontNavyHeadings,
            unfocusedTextColor = FontRoyalBlue,
            focusedBorderColor = Color(0xFF2563EB),
            unfocusedBorderColor = Color(0xFFDBEAFE)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_request_problem_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Budget & Urgency
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Budget", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FontNavyHeadings)
            OutlinedTextField(
              value = budget,
              onValueChange = { budget = it },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = FontNavyHeadings,
                unfocusedTextColor = FontRoyalBlue,
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFDBEAFE)
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Urgency", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FontNavyHeadings)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              listOf("URGENT", "STANDARD").forEach { u ->
                FilterChip(
                  selected = urgency.equals(u, ignoreCase = true),
                  onClick = { urgency = u },
                  label = { Text(if (u == "URGENT") "Urgent" else "Normal", fontSize = 10.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFDBEAFE),
                    selectedLabelColor = FontNavyHeadings
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Radius Picker
        Text(
          text = "Proximity Broadcast Radius: ${radiusMeters}m",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = FontNavyHeadings
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.padding(top = 4.dp)
        ) {
          listOf(500, 1000, 2000).forEach { r ->
            FilterChip(
              selected = radiusMeters == r,
              onClick = { radiusMeters = r },
              label = { Text("${r}m", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFDBEAFE),
                selectedLabelColor = FontNavyHeadings
              )
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirmEdit(title, problemDescription, budget, urgency, radiusMeters)
        },
        enabled = title.isNotBlank() && problemDescription.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF2563EB),
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("save_edit_request_button")
      ) {
        Text("Save to Firestore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
      ) {
        Text("Cancel", fontSize = 12.sp, color = FontRoyalBlue)
      }
    },
    modifier = Modifier.testTag("edit_service_request_dialog")
  )
}
