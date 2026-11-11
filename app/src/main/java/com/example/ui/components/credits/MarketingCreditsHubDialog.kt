package com.example.ui.components.credits

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CreditTransaction
import com.example.data.model.CreditTransactionType
import com.example.data.model.MarketingServicePackage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val AVAILABLE_MARKETING_PACKAGES = listOf(
  MarketingServicePackage(
    id = "pkg-broadcast-500m",
    title = "500m Local Push Broadcast",
    description = "Send instant promotional push notifications to all active merchants & clients within 500m radius.",
    creditCost = 100,
    durationLabel = "Instant Broadcast",
    iconEmoji = "📡",
    category = "Proximity Marketing",
    targetReach = "Estimated Reach: ~150 Local Clients"
  ),
  MarketingServicePackage(
    id = "pkg-top-spotlight",
    title = "Top Spotlight in Discover",
    description = "Pin your digital visiting card and products to the #1 position in search and category exploration.",
    creditCost = 150,
    durationLabel = "Active for 7 Days",
    iconEmoji = "🌟",
    category = "Search Boost",
    targetReach = "Estimated Reach: ~850 Searches"
  ),
  MarketingServicePackage(
    id = "pkg-video-sponsored",
    title = "Sponsored Video Feed Reel",
    description = "Showcase your trade skills and portfolio video directly in the app's trending video feed with a direct call button.",
    creditCost = 200,
    durationLabel = "Active for 14 Days",
    iconEmoji = "🎬",
    category = "Video Marketing",
    targetReach = "Estimated Reach: ~1,400 Views"
  ),
  MarketingServicePackage(
    id = "pkg-nfc-voucher",
    title = "Physical NFC Card Discount Voucher",
    description = "Redeem ₹250 instant voucher code towards your next physical smart NFC or luxury visiting card print batch.",
    creditCost = 250,
    durationLabel = "Instant Voucher Code",
    iconEmoji = "💳",
    category = "Print Rewards",
    targetReach = "Save ₹250 on 100 Cards"
  ),
  MarketingServicePackage(
    id = "pkg-fast-track-kyc",
    title = "Priority Verification Fast-Track",
    description = "Expedited KYC verification by senior compliance officer within 30 minutes with immediate Verified Badge.",
    creditCost = 80,
    durationLabel = "Under 30 Minutes",
    iconEmoji = "⚡",
    category = "Trust Badge",
    targetReach = "Instant Verified Status"
  )
)

/**
 * Marketing & Credits Hub Dialog:
 * Shows credit balance from DataStore, enables app sharing to earn +50 credits per share,
 * and allows spending credits to purchase marketing promotions and boosts.
 */
@Composable
fun MarketingCreditsHubDialog(
  creditBalance: Int,
  totalEarned: Int,
  totalSpent: Int,
  shareCount: Int,
  transactions: List<CreditTransaction>,
  onShareApp: () -> Unit,
  onSpendCredits: (MarketingServicePackage) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Marketing Packages, 1: Transaction Ledger, 2: Earn Credits
  var showSuccessSpendDialog by remember { mutableStateOf<MarketingServicePackage?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF090D16))
        .testTag("marketing_credits_hub_dialog"),
      color = Color(0xFF090D16)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        // Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "BCN Marketing & Credits Hub",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Earn credits by sharing • Spend on business marketing",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .background(Color(0xFF1E293B), CircleShape)
              .testTag("close_credits_hub_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Live Credit Balance Dashboard Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2E)),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(16.dp))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.5f), Color(0xFF0F1B2E))
                )
              )
              .padding(16.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "AVAILABLE MARKETING CREDITS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF38BDF8)
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                      text = "$creditBalance",
                      fontSize = 32.sp,
                      fontWeight = FontWeight.Black,
                      color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Credits",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFFBBF24),
                      modifier = Modifier.padding(bottom = 6.dp)
                    )
                  }
                }

                // Quick Share Button to earn credits
                Button(
                  onClick = {
                    onShareApp()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                      type = "text/plain"
                      putExtra(
                        Intent.EXTRA_TEXT,
                        "Connect with my business on BusyCardNet! Tap to view my verified digital visiting card, products, and contact info: https://busycardnet.com/app/ref"
                      )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share BusyCardNet App"))
                  },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                  modifier = Modifier.testTag("share_app_earn_credits_button")
                ) {
                  Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Share (+50)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              HorizontalDivider(color = Color(0xFF1E293B))
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                MetricItem(label = "Total Earned", value = "+$totalEarned", color = Color(0xFF34D399))
                MetricItem(label = "Total Spent", value = "-$totalSpent", color = Color(0xFFF87171))
                MetricItem(label = "Shares Done", value = "$shareCount Shares", color = Color(0xFF38BDF8))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color(0xFF0F172A),
          contentColor = Color(0xFF38BDF8),
          divider = {}
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            selectedContentColor = Color(0xFF38BDF8),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Marketing Packages", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 0) Color(0xFF38BDF8) else Color(0xFFCBD5E1)) },
            icon = { Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = if (selectedTab == 0) Color(0xFF38BDF8) else Color(0xFF94A3B8), modifier = Modifier.size(16.dp)) },
            modifier = Modifier.testTag("tab_marketing_packages")
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            selectedContentColor = Color(0xFF38BDF8),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Credit Ledger", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 1) Color(0xFF38BDF8) else Color(0xFFCBD5E1)) },
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = if (selectedTab == 1) Color(0xFF38BDF8) else Color(0xFF94A3B8), modifier = Modifier.size(16.dp)) },
            modifier = Modifier.testTag("tab_credit_ledger")
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            selectedContentColor = Color(0xFF38BDF8),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Earn Credits", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.SemiBold, color = if (selectedTab == 2) Color(0xFF38BDF8) else Color(0xFFCBD5E1)) },
            icon = { Icon(Icons.Default.Share, contentDescription = null, tint = if (selectedTab == 2) Color(0xFF38BDF8) else Color(0xFF94A3B8), modifier = Modifier.size(16.dp)) },
            modifier = Modifier.testTag("tab_earn_credits")
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
          when (selectedTab) {
            0 -> MarketingPackagesTab(
              creditBalance = creditBalance,
              packages = AVAILABLE_MARKETING_PACKAGES,
              onPurchase = { pkg ->
                if (creditBalance >= pkg.creditCost) {
                  onSpendCredits(pkg)
                  showSuccessSpendDialog = pkg
                } else {
                  Toast.makeText(
                    context,
                    "Insufficient credits! Share the app to earn +50 credits.",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              }
            )

            1 -> CreditLedgerTab(transactions = transactions)

            2 -> EarnCreditsGuideTab(
              shareCount = shareCount,
              onShare = {
                onShareApp()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "Discover trusted verified businesses and create your digital visiting card on BusyCardNet: https://busycardnet.com/app/download"
                  )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share BusyCardNet"))
              }
            )
          }
        }
      }
    }
  }

  // Marketing Campaign Activated Confirmation Dialog
  if (showSuccessSpendDialog != null) {
    val pkg = showSuccessSpendDialog!!
    Dialog(onDismissRequest = { showSuccessSpendDialog = null }) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier.padding(16.dp).border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color(0xFF064E3B)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(32.dp))
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Marketing Campaign Activated!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Successfully redeemed ${pkg.creditCost} credits for '${pkg.title}'. Your campaign is now actively reaching potential clients.",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(18.dp))
          Button(
            onClick = { showSuccessSpendDialog = null },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Done", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
  Column {
    Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
    Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
  }
}

// ---------------------------------------------------------------------------
// Tab 0: Marketing Packages to Spend Credits
// ---------------------------------------------------------------------------
@Composable
private fun MarketingPackagesTab(
  creditBalance: Int,
  packages: List<MarketingServicePackage>,
  onPurchase: (MarketingServicePackage) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(packages) { pkg ->
      val canAfford = creditBalance >= pkg.creditCost
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, if (canAfford) Color(0xFF1E293B) else Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Row(modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
              ) {
                Text(pkg.iconEmoji, fontSize = 22.sp)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = pkg.title,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = pkg.category,
                  fontSize = 10.sp,
                  color = Color(0xFF38BDF8)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF59E0B).copy(alpha = 0.2f)
            ) {
              Text(
                text = "${pkg.creditCost} Credits",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = pkg.description,
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 16.sp
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "⚡ ${pkg.targetReach} • ${pkg.durationLabel}",
              fontSize = 10.sp,
              color = Color(0xFF94A3B8)
            )

            Button(
              onClick = { onPurchase(pkg) },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (canAfford) Color(0xFF2563EB) else Color(0xFF334155)
              ),
              modifier = Modifier.height(34.dp).testTag("spend_credits_${pkg.id}_button")
            ) {
              Text(
                text = if (canAfford) "Activate" else "Need ${pkg.creditCost - creditBalance} more",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Tab 1: Credit Ledger (Real-time history from DataStore)
// ---------------------------------------------------------------------------
@Composable
private fun CreditLedgerTab(transactions: List<CreditTransaction>) {
  if (transactions.isEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
      Spacer(modifier = Modifier.height(12.dp))
      Text("No credit transactions yet", color = Color(0xFF94A3B8), fontSize = 13.sp)
      Spacer(modifier = Modifier.height(6.dp))
      Text("Share your digital card or the app to earn your first +50 credits!", color = Color(0xFF64748B), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
  } else {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(transactions) { tx ->
        val isGain = tx.amount > 0
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF131D31),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                .background(if (isGain) Color(0xFF064E3B) else Color(0xFF450A0A)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isGain) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                  contentDescription = null,
                  tint = if (isGain) Color(0xFF34D399) else Color(0xFFF87171),
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = tx.description,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = dateFormat.format(Date(tx.timestamp)),
                  fontSize = 9.sp,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = if (isGain) "+${tx.amount}" else "${tx.amount}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isGain) Color(0xFF34D399) else Color(0xFFF87171)
              )
              Text(
                text = "Bal: ${tx.balanceAfter}",
                fontSize = 9.sp,
                color = Color(0xFF64748B)
              )
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Tab 2: How to Earn Credits Guide & Instant Share
// ---------------------------------------------------------------------------
@Composable
private fun EarnCreditsGuideTab(
  shareCount: Int,
  onShare: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "HOW TO EARN MARKETING CREDITS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF38BDF8)
          )
          Spacer(modifier = Modifier.height(10.dp))

          EarnMethodRow("📲", "Share Card via WhatsApp", "+50 Credits per client shared")
          EarnMethodRow("👥", "Refer a Fellow Merchant", "+100 Credits upon sign-up")
          EarnMethodRow("🛡️", "Complete Business KYC", "+100 Credits upon team approval")
          EarnMethodRow("⭐", "Receive 5-Star Reviews", "+20 Credits per verified review")

          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = onShare,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("instant_share_for_credits_button")
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share App Now (+50 Credits)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun EarnMethodRow(emoji: String, title: String, reward: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(emoji, fontSize = 16.sp)
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = Color(0xFF10B981).copy(alpha = 0.2f)
    ) {
      Text(
        text = reward,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF34D399),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}
