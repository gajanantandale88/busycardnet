package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.BcnCardHyperlink
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CardConnectionEntity
import com.example.data.local.UserStatusEntity
import com.example.ui.viewmodel.BusyCardNetViewModel

@Composable
fun BridgesNetworkScreen(
  viewModel: BusyCardNetViewModel,
  onOpenScanner: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val connectedBridges by viewModel.connectedBridges.collectAsStateWithLifecycle()
  val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()
  val allStatuses by viewModel.allStatuses.collectAsStateWithLifecycle()
  val adPosts by viewModel.videoPosts.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Bridges, 1: Requests, 2: Shared Ads
  var showAddStatusDialog by remember { mutableStateOf(false) }
  var activeStatusPreview by remember { mutableStateOf<UserStatusEntity?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF070D19))
      .testTag("bridges_network_screen")
  ) {
    // 1. Header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF0F172A),
      shadowElevation = 4.dp
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "BUSYCARDNET BRIDGES",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.2.sp,
              color = Color(0xFF10B981)
            )
            Text(
              text = "Connected Cardholders & Status Network",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFF0284C7),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onOpenScanner() }
                .testTag("bridges_scan_qr_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.QrCodeScanner,
                  contentDescription = "Scan Contact QR Card",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Scan QR",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFF10B981).copy(alpha = 0.2f),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
            ) {
              Text(
                text = "${connectedBridges.size} Bridges",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34D399),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 24H STATUSES STRIP (Prompt: "Once connected, they can build a bridge. They can share their statuses.") ---
        Text(
          text = "24-HOUR BRIDGE STATUSES",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          // Current User's "Add Status" bubble
          item {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.clickable { showAddStatusDialog = true }
            ) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF1E293B))
                  .border(2.dp, Color(0xFF38BDF8), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Add, contentDescription = "Add Status", tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Your Status", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
          }

          // Connected Cardholder Statuses
          items(allStatuses) { status ->
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.clickable { activeStatusPreview = status }
            ) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF10B981)))
                  )
                  .border(2.5.dp, Color(0xFF34D399), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(status.statusEmoji, fontSize = 24.sp)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = status.authorName.split(" ").firstOrNull() ?: status.authorName,
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Navigation: [Active Bridges] | [Pending Requests] | [Bridge Ads]
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          contentColor = Color(0xFF10B981),
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = Color(0xFF10B981)
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            selectedContentColor = Color(0xFF10B981),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Active Bridges (${connectedBridges.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 0) Color(0xFF10B981) else Color(0xFFCBD5E1)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            selectedContentColor = Color(0xFF10B981),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Pending Requests (${pendingRequests.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 1) Color(0xFF10B981) else Color(0xFFCBD5E1)) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            selectedContentColor = Color(0xFF10B981),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Bridge Ads", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 2) Color(0xFF10B981) else Color(0xFFCBD5E1)) }
          )
        }
      }
    }

    // 2. Tab Content
    when (selectedTab) {
      0 -> {
        // Active Bridges List
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // CameraX QR Contact Card Quick-Add Banner
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("bridges_qr_quick_add_card"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
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
                      .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.QrCodeScanner,
                      contentDescription = "CameraX QR Scanner",
                      tint = Color(0xFF38BDF8),
                      modifier = Modifier.size(22.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = "Add Visiting Card via QR",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color.White
                    )
                    Text(
                      text = "Scan a contact's QR card using CameraX or gallery to connect instantly",
                      fontSize = 11.sp,
                      color = Color(0xFF94A3B8),
                      lineHeight = 15.sp
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                  onClick = onOpenScanner,
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                  modifier = Modifier
                    .height(42.dp)
                    .testTag("bridges_scan_contact_cta_button")
                ) {
                  Text("Scan Card", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
              }
            }
          }

          if (connectedBridges.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No active bridges yet. Scan a contact's QR visiting card above or connect from Discover!",
                  color = Color(0xFF94A3B8),
                  fontSize = 13.sp
                )
              }
            }
          } else {
            items(connectedBridges) { bridge ->
              BridgeCardItem(
                bridge = bridge,
                onCall = { phone ->
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                  context.startActivity(intent)
                },
                onWhatsApp = { phone ->
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone"))
                  try { context.startActivity(intent) } catch (e: Exception) {}
                }
              )
            }
          }
        }
      }
      1 -> {
        // Pending Requests
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (pendingRequests.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No pending connection requests.",
                  color = Color(0xFF94A3B8),
                  fontSize = 13.sp
                )
              }
            }
          } else {
            items(pendingRequests) { req ->
              PendingRequestCardItem(
                request = req,
                onAccept = {
                  viewModel.acceptConnectRequest(req.id)
                  Toast.makeText(context, "🤝 Bridge established with ${req.requesterName}!", Toast.LENGTH_SHORT).show()
                },
                onDecline = {
                  viewModel.rejectConnectRequest(req.id)
                  Toast.makeText(context, "Request declined.", Toast.LENGTH_SHORT).show()
                }
              )
            }
          }
        }
      }
      2 -> {
        // Shared Bridge Ads & Pages
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(adPosts) { ad ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ad.authorName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                  }
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                  ) {
                    Text("🤝 BRIDGE BROADCAST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))
                BcnCardHyperlink(
                  bcnId = ad.authorBusyCardNetId,
                  color = Color(0xFF38BDF8),
                  fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(ad.content, fontSize = 13.sp, color = Color(0xFFCBD5E1))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Button(
                    onClick = {
                      val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.ctaPhone}"))
                      context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                  ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Cardholder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }

                  Button(
                    onClick = {
                      val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${ad.ctaPhone}"))
                      try { context.startActivity(intent) } catch (e: Exception) {}
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                  ) {
                    Text("💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialog: Share 24h User Status
  if (showAddStatusDialog) {
    ShareStatusDialog(
      onDismiss = { showAddStatusDialog = false },
      onSubmit = { statusText, emoji, link ->
        viewModel.shareUserStatus(statusText, emoji, link)
        showAddStatusDialog = false
        Toast.makeText(context, "Status shared to all your connected bridges!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Dialog: View Full Status Preview
  activeStatusPreview?.let { status ->
    AlertDialog(
      onDismissRequest = { activeStatusPreview = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(status.statusEmoji, fontSize = 26.sp)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(status.authorName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            BcnCardHyperlink(
              bcnId = status.busyCardNetId,
              color = Color(0xFF38BDF8),
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 1.dp)
            )
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = status.statusText,
              fontSize = 14.sp,
              color = Color.White,
              lineHeight = 20.sp,
              modifier = Modifier.padding(14.dp)
            )
          }

          if (status.pageLink.isNotBlank()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                .clickable {
                  try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(status.pageLink))
                    context.startActivity(intent)
                  } catch (e: Exception) {}
                }
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Visit Connected Business Page", fontSize = 12.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { activeStatusPreview = null },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
          Text("Close")
        }
      },
      containerColor = Color(0xFF0F172A)
    )
  }
}

@Composable
fun BridgeCardItem(
  bridge: CardConnectionEntity,
  onCall: (String) -> Unit,
  onWhatsApp: (String) -> Unit
) {
  val partnerName = if (bridge.requesterName == "Rajesh Sharma") bridge.targetName else bridge.requesterName
  val partnerBcnId = if (bridge.requesterName == "Rajesh Sharma") bridge.targetBusyCardNetId else bridge.requesterBusyCardNetId
  val partnerTrade = if (bridge.requesterName == "Rajesh Sharma") bridge.targetProfession else bridge.requesterProfession

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color(0xFF10B981).copy(alpha = 0.2f))
              .border(1.5.dp, Color(0xFF10B981), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (partnerTrade.contains("Plum", true)) "🔧" else if (partnerTrade.contains("Shop", true)) "🏪" else "⚡",
              fontSize = 20.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(partnerName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
            }
            Text(partnerTrade, fontSize = 12.sp, color = Color(0xFF94A3B8))
            BcnCardHyperlink(
              bcnId = partnerBcnId,
              color = Color(0xFF38BDF8),
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF10B981).copy(alpha = 0.2f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
        ) {
          Text(
            text = "🤝 ACTIVE BRIDGE",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF34D399),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = bridge.note,
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
        BcnCardHyperlink(
          bcnId = partnerBcnId,
          color = Color(0xFF38BDF8),
          fontSize = 11.sp
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action strip: Call, WhatsApp, View Shared Updates
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { onCall("+91 98765 43210") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
          Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = { onWhatsApp("+91 98765 43210") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
          Text("💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun PendingRequestCardItem(
  request: CardConnectionEntity,
  onAccept: () -> Unit,
  onDecline: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(request.requesterName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
          }
          Text("${request.requesterProfession} • Connect Request", fontSize = 12.sp, color = Color(0xFFF59E0B))
          BcnCardHyperlink(
            bcnId = request.requesterBusyCardNetId,
            color = Color(0xFF38BDF8),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(request.note.ifEmpty { "Wants to build a cross-cardholder bridge with you." }, fontSize = 12.sp, color = Color(0xFFCBD5E1))
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onAccept,
          modifier = Modifier.weight(1.2f),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Accept & Build Bridge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onDecline,
          modifier = Modifier.weight(0.8f),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
        ) {
          Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Decline", fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
fun ShareStatusDialog(
  onDismiss: () -> Unit,
  onSubmit: (statusText: String, emoji: String, pageLink: String) -> Unit
) {
  var statusText by remember { mutableStateOf("") }
  var selectedEmoji by remember { mutableStateOf("⚡") }
  var pageLink by remember { mutableStateOf("") }

  val emojis = listOf("⚡", "🔧", "💧", "🏪", "🏷️", "💼", "🚨", "✅")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Share 24-Hour Bridge Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          "Broadcast short live updates, emergency availability, or special offers to your connected cardholder bridges.",
          fontSize = 11.sp,
          color = Color(0xFF94A3B8)
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          emojis.forEach { em ->
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (selectedEmoji == em) Color(0xFF0284C7) else Color(0xFF1E293B))
                .clickable { selectedEmoji = em },
              contentAlignment = Alignment.Center
            ) {
              Text(em, fontSize = 16.sp)
            }
          }
        }

        OutlinedTextField(
          value = statusText,
          onValueChange = { statusText = it },
          label = { Text("What's your current status?") },
          placeholder = { Text("e.g., Available for urgent service calls in Dadar...") },
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E293B),
            unfocusedContainerColor = Color(0xFF1E293B),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        OutlinedTextField(
          value = pageLink,
          onValueChange = { pageLink = it },
          label = { Text("Attach Business Page Link (Optional)") },
          placeholder = { Text("https://busycardnet.com/my-page") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E293B),
            unfocusedContainerColor = Color(0xFF1E293B),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSubmit(
            statusText.ifEmpty { "Active & on call for services." },
            selectedEmoji,
            pageLink
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
      ) {
        Text("Share Status")
      }
    },
    dismissButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))) {
        Text("Cancel")
      }
    },
    containerColor = Color(0xFF0F172A)
  )
}
