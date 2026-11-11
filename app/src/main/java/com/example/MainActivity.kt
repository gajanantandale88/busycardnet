package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.ui.components.kyc.KycVerificationDialog
import com.example.ui.components.credits.MarketingCreditsHubDialog
import com.example.data.model.KycVerificationStatus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.DigitalCardEntity
import com.example.ui.components.BcnCameraScannerDialog
import com.example.ui.components.CameraScannerMode
import com.example.ui.components.CardShareDialog
import com.example.ui.components.CreateAdDialog
import com.example.ui.components.CreateServiceRequestDialog
import com.example.ui.components.DigitalVisitingCardView
import com.example.ui.components.LocalBcnCardOpener
import com.example.ui.components.ProximityServiceBroadcastDialog
import com.example.ui.screens.BridgesNetworkScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreatorsScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.MyCardScreen
import com.example.ui.screens.MyRequestsScreen
import com.example.ui.screens.ProfileDashboardScreen
import com.example.ui.screens.VideoFeedScreen
import com.example.ui.screens.VisitingCardQrGeneratorScreen
import com.example.ui.theme.BusyCardNetTheme
import com.example.ui.viewmodel.BusyCardNetViewModel
import androidx.compose.ui.window.Dialog

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BusyCardNetTheme {
        BusyCardNetApp()
      }
    }
  }
}

@Composable
fun BusyCardNetApp(viewModel: BusyCardNetViewModel = viewModel()) {
  val context = LocalContext.current
  var currentTab by remember { mutableIntStateOf(0) }
  var discoverInitialCategory by remember { mutableStateOf<String?>(null) }
  var marketplaceInitialTab by remember { mutableIntStateOf(0) }

  // Dialog & Full-Screen Overlays
  var showServiceRequestDialog by remember { mutableStateOf(false) }
  var initialServiceRequestProfession by remember { mutableStateOf("ELEC") }
  var showCreateAdDialog by remember { mutableStateOf(false) }
  var inspectingCard by remember { mutableStateOf<DigitalCardEntity?>(null) }
  var cardToShare by remember { mutableStateOf<DigitalCardEntity?>(null) }
  var showCameraScannerDialog by remember { mutableStateOf(false) }
  var cameraScannerInitialMode by remember { mutableStateOf(CameraScannerMode.SCAN_BARCODE) }
  var showQrGeneratorScreen by remember { mutableStateOf(false) }
  var qrGeneratorInitialCardId by remember { mutableStateOf<String?>(null) }
  var showLoginScreen by remember { mutableStateOf(false) }
  var showKycVerificationDialog by remember { mutableStateOf(false) }
  var showMarketingCreditsHubDialog by remember { mutableStateOf(false) }

  val kycStatus by viewModel.kycStatus.collectAsStateWithLifecycle()
  val kycData by viewModel.kycData.collectAsStateWithLifecycle()
  val creditBalance by viewModel.creditBalance.collectAsStateWithLifecycle()
  val totalEarnedCredits by viewModel.totalEarnedCredits.collectAsStateWithLifecycle()
  val totalSpentCredits by viewModel.totalSpentCredits.collectAsStateWithLifecycle()
  val shareCount by viewModel.shareCount.collectAsStateWithLifecycle()
  val creditTransactions by viewModel.creditTransactions.collectAsStateWithLifecycle()

  val activeChatRequestId by viewModel.activeChatRequestId.collectAsStateWithLifecycle()
  val professions by viewModel.allProfessions.collectAsStateWithLifecycle()
  val serviceRequests by viewModel.allServiceRequests.collectAsStateWithLifecycle()
  val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()

  // Active service request count for badge
  val pendingRequestsCount = serviceRequests.count { it.status == "SENT" }
  val viewingCardFromVm by viewModel.viewingCard.collectAsStateWithLifecycle()
  val activeInspectCard = inspectingCard ?: viewingCardFromVm

  CompositionLocalProvider(
    LocalBcnCardOpener provides { bcnId -> viewModel.openCardByBusyCardNetId(bcnId) }
  ) {
    if (activeChatRequestId != null) {
      ChatScreen(
        requestId = activeChatRequestId!!,
        viewModel = viewModel,
        onBack = { viewModel.closeChat() }
      )
    } else if (showLoginScreen) {
      LoginScreen(
        viewModel = viewModel,
        onNavigateBack = { showLoginScreen = false }
      )
    } else if (showQrGeneratorScreen) {
      VisitingCardQrGeneratorScreen(
        viewModel = viewModel,
        initialCardId = qrGeneratorInitialCardId,
        onNavigateBack = { showQrGeneratorScreen = false },
        onOpenScanner = {
          cameraScannerInitialMode = CameraScannerMode.SCAN_BARCODE
          showCameraScannerDialog = true
        }
      )
    } else {
      Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        NavigationBar(
          containerColor = Color.White,
          tonalElevation = 6.dp,
          modifier = Modifier
            .border(
              BorderStroke(1.dp, Color(0xFFE2E8F0)),
              shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .testTag("busycardnet_bottom_nav")
        ) {
          // 1. Home
          NavigationBarItem(
            selected = currentTab == 0,
            onClick = { currentTab = 0 },
            icon = {
              Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier.size(20.dp)
              )
            },
            label = { Text("Home", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFF1E40AF),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFFDBEAFE)
            ),
            modifier = Modifier.testTag("nav_item_home")
          )

          // 2. Discover (Map & Directory)
          NavigationBarItem(
            selected = currentTab == 1,
            onClick = {
              discoverInitialCategory = null
              currentTab = 1
            },
            icon = {
              Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Discover",
                modifier = Modifier.size(20.dp)
              )
            },
            label = { Text("Discover", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFF0284C7),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFFE0F2FE)
            ),
            modifier = Modifier.testTag("nav_item_discover")
          )

          // 3. My Requests (Firestore Synced Proximity Requests)
          NavigationBarItem(
            selected = currentTab == 2,
            onClick = { currentTab = 2 },
            icon = {
              BadgedBox(
                badge = {
                  if (pendingRequestsCount > 0) {
                    Badge(containerColor = Color(0xFF2563EB)) {
                      Text("$pendingRequestsCount", color = Color.White)
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Assignment,
                  contentDescription = "My Requests",
                  modifier = Modifier.size(20.dp)
                )
              }
            },
            label = { Text("My Requests", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFF2563EB),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFFDBEAFE)
            ),
            modifier = Modifier.testTag("nav_item_my_requests")
          )

          // 4. My Card (Centerpiece)
          NavigationBarItem(
            selected = currentTab == 3,
            onClick = { currentTab = 3 },
            icon = {
              Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "My Card",
                modifier = Modifier.size(22.dp)
              )
            },
            label = { Text("My Card", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFF1E40AF),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFFDBEAFE)
            ),
            modifier = Modifier.testTag("nav_item_my_card")
          )

          // 5. Bridges & 24h Statuses (Prompt: "Connect request to every cardholder... build a bridge... share statuses")
          NavigationBarItem(
            selected = currentTab == 4,
            onClick = { currentTab = 4 },
            icon = {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Bridges",
                modifier = Modifier.size(20.dp)
              )
            },
            label = { Text("Bridges", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFF10B981),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFF10B981).copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_item_bridges")
          )

          // 6. Market & Creators
          NavigationBarItem(
            selected = currentTab == 5,
            onClick = {
              marketplaceInitialTab = 0
              currentTab = 5
            },
            icon = {
              Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Market",
                modifier = Modifier.size(20.dp)
              )
            },
            label = { Text("Market", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFFD97706),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFFFEF3C7)
            ),
            modifier = Modifier.testTag("nav_item_market")
          )

          // 7. Videos & Broadcasts
          NavigationBarItem(
            selected = currentTab == 6,
            onClick = { currentTab = 6 },
            icon = {
              Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Videos",
                modifier = Modifier.size(20.dp)
              )
            },
            label = { Text("Videos", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFFEF4444),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFFFEE2E2)
            ),
            modifier = Modifier.testTag("nav_item_videos")
          )

          // 8. Settings & Business Hub
          NavigationBarItem(
            selected = currentTab == 7,
            onClick = { currentTab = 7 },
            icon = {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(20.dp)
              )
            },
            label = { Text("Settings", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFF1E40AF),
              selectedTextColor = Color(0xFF0F172A),
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF475569),
              indicatorColor = Color(0xFFDBEAFE)
            ),
            modifier = Modifier.testTag("nav_item_settings")
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0xFFE8EEF5),
                Color(0xFFF1F5F9),
                Color(0xFFF8FAFC)
              )
            )
          )
          .padding(innerPadding)
      ) {
        when (currentTab) {
          0 -> HomeScreen(
            viewModel = viewModel,
            onNavigateToDiscover = { cat ->
              discoverInitialCategory = cat
              currentTab = 1
            },
            onNavigateToMyCard = { currentTab = 3 },
            onOpenServiceRequestDialog = { profCode ->
              initialServiceRequestProfession = profCode
              showServiceRequestDialog = true
            },
            onOpenCreateAdDialog = { showCreateAdDialog = true },
            onOpenChat = { reqId -> viewModel.openChatForRequest(reqId) },
            onOpenScanner = {
              cameraScannerInitialMode = CameraScannerMode.SCAN_BARCODE
              showCameraScannerDialog = true
            },
            onOpenQrGenerator = {
              qrGeneratorInitialCardId = null
              showQrGeneratorScreen = true
            },
            onOpenLoginScreen = { showLoginScreen = true },
            onNavigateToSettings = { currentTab = 7 }
          )
          1 -> DiscoverScreen(
            viewModel = viewModel,
            initialCategoryCode = discoverInitialCategory,
            onOpenCardDetails = { card -> inspectingCard = card },
            onRequestService = { profCode ->
              initialServiceRequestProfession = profCode
              showServiceRequestDialog = true
            }
          )
          2 -> MyRequestsScreen(
            viewModel = viewModel,
            onOpenServiceRequestDialog = {
              initialServiceRequestProfession = "ELEC"
              showServiceRequestDialog = true
            },
            onOpenChat = { reqId -> viewModel.openChatForRequest(reqId) }
          )
          3 -> MyCardScreen(
            viewModel = viewModel,
            onNavigateToSuppliers = {
              marketplaceInitialTab = 3
              currentTab = 5
            },
            onOpenScanner = {
              cameraScannerInitialMode = CameraScannerMode.SCAN_BARCODE
              showCameraScannerDialog = true
            },
            onOpenQrGenerator = { cardId ->
              qrGeneratorInitialCardId = cardId
              showQrGeneratorScreen = true
            },
            onOpenLoginScreen = { showLoginScreen = true }
          )
          4 -> BridgesNetworkScreen(
            viewModel = viewModel,
            onOpenScanner = {
              cameraScannerInitialMode = CameraScannerMode.SCAN_BARCODE
              showCameraScannerDialog = true
            }
          )
          5 -> MarketplaceScreen(
            viewModel = viewModel,
            initialTab = marketplaceInitialTab
          )
          6 -> VideoFeedScreen(
            viewModel = viewModel
          )
          7 -> ProfileDashboardScreen(
            viewModel = viewModel,
            onOpenServiceRequestsHub = {
              currentTab = 2
            },
            onOpenLoginScreen = { showLoginScreen = true },
            onOpenScanner = {
              cameraScannerInitialMode = CameraScannerMode.SCAN_BARCODE
              showCameraScannerDialog = true
            }
          )
        }
      }
    }
  }

  // 500m Service Request Proximity Broadcast Dialog
  if (showServiceRequestDialog) {
    ProximityServiceBroadcastDialog(
      initialProfessionCode = initialServiceRequestProfession,
      userLocality = selectedLocation,
      onDismiss = { showServiceRequestDialog = false },
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
        showServiceRequestDialog = false
        currentTab = 2
        Toast.makeText(context, "📡 Proximity broadcast active (${radius}m)! Stored in Firestore & notified nearby businesses.", Toast.LENGTH_LONG).show()
      }
    )
  }

  // Create Ad Dialog
  if (showCreateAdDialog) {
    CreateAdDialog(
      onDismiss = { showCreateAdDialog = false },
      onSubmit = { content, radius, duration, budget, cta ->
        viewModel.createAdvertisement(content, radius, duration, budget, cta)
        showCreateAdDialog = false
        Toast.makeText(context, "Local Ad Campaign published (${radius / 1000}km radius)!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Inspecting another user's Card
  if (activeInspectCard != null) {
    Dialog(onDismissRequest = {
      inspectingCard = null
      viewModel.closeViewingCard()
    }) {
      DigitalVisitingCardView(
        card = activeInspectCard,
        onCallClick = { phone ->
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
          context.startActivity(intent)
        },
        onWhatsAppClick = { phone ->
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone"))
          try { context.startActivity(intent) } catch (e: Exception) {}
        },
        onShareClick = {
          cardToShare = activeInspectCard
        },
        onScanBarcodeClick = {
          inspectingCard = null
          viewModel.closeViewingCard()
          cameraScannerInitialMode = CameraScannerMode.SCAN_BARCODE
          showCameraScannerDialog = true
        }
      )
    }
  }

  // Share Dialog
  if (cardToShare != null) {
    CardShareDialog(
      card = cardToShare!!,
      onDismiss = { cardToShare = null },
      onOpenScanner = {
        cameraScannerInitialMode = CameraScannerMode.SCAN_BARCODE
        showCameraScannerDialog = true
      },
      onOpenQrGenerator = { cardId ->
        cardToShare = null
        qrGeneratorInitialCardId = cardId
        showQrGeneratorScreen = true
      },
      onCardShared = { platform ->
        viewModel.rewardCreditForShare(platform)
      }
    )
  }

  // Multi-step KYC Verification Dialog
  if (showKycVerificationDialog) {
    KycVerificationDialog(
      currentStatus = kycStatus,
      existingData = kycData,
      onDismiss = { showKycVerificationDialog = false },
      onSubmitKyc = { submission ->
        viewModel.submitKycVerification(submission)
      },
      onSimulateApprove = {
        viewModel.simulateApproveKycVerification()
      }
    )
  }

  // Marketing Credits Hub Dialog
  if (showMarketingCreditsHubDialog) {
    MarketingCreditsHubDialog(
      creditBalance = creditBalance,
      totalEarned = totalEarnedCredits,
      totalSpent = totalSpentCredits,
      shareCount = shareCount,
      transactions = creditTransactions,
      onShareApp = {
        viewModel.rewardCreditForShare("App Share")
      },
      onSpendCredits = { pkg ->
        viewModel.spendCreditsForMarketing(pkg)
      },
      onDismiss = { showMarketingCreditsHubDialog = false }
    )
  }

  // Camera & Barcode Scanner Dialog (Dual-Mode: Barcode Scan & Profile Photo Capture)
  if (showCameraScannerDialog) {
    BcnCameraScannerDialog(
      viewModel = viewModel,
      initialMode = cameraScannerInitialMode,
      onDismiss = { showCameraScannerDialog = false },
      onCardScanned = { card ->
        viewModel.addCardToNetworkViaQr(card) {
          Toast.makeText(
            context,
            "🤝 ${card.businessName} added to your Digital Visiting Card Network!",
            Toast.LENGTH_SHORT
          ).show()
        }
        inspectingCard = card
        showCameraScannerDialog = false
      },
      onPhotoCaptured = { photoUri ->
        Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
      }
    )
  }
  } // End CompositionLocalProvider
}
