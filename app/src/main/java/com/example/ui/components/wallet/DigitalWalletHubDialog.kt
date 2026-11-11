package com.example.ui.components.wallet

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CreditTransaction
import com.example.data.model.CreditTransactionType
import com.example.data.model.WALLET_RECHARGE_PACKS
import com.example.data.model.WalletRechargePack
import com.example.data.model.categoryName
import com.example.data.model.isPositive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full Digital Wallet Hub dialog.
 * Allows users to manage credits for advertising campaigns and marketplace purchases,
 * top up credits via simulated UPI/Card, transfer credits to other cardholders, and view the passbook ledger.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DigitalWalletHubDialog(
  creditBalance: Int,
  totalEarned: Int,
  totalSpent: Int,
  transactions: List<CreditTransaction>,
  userName: String = "Rajesh Sharma",
  bcnId: String = "IN-MH-MU-26-374BIZ691",
  onTopUpCredits: (amount: Int, method: String, packName: String) -> Unit,
  onTransferCredits: (recipientBcnId: String, amount: Int, note: String) -> Unit,
  onEarnCredits: (amount: Int, source: String) -> Unit,
  onOpenCampaignManager: () -> Unit = {},
  onOpenMarketplace: () -> Unit = {},
  onOpenSocietyMaintenance: () -> Unit = {},
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Recharge Packs, 1: Transfer, 2: Passbook Ledger, 3: Earn Free
  var selectedRechargePack by remember { mutableStateOf<WalletRechargePack?>(WALLET_RECHARGE_PACKS[1]) }
  var customRechargeAmount by remember { mutableStateOf("500") }
  var selectedPaymentMethod by remember { mutableStateOf("UPI / Google Pay") }
  var isCustomAmountMode by remember { mutableStateOf(false) }

  // Transfer state
  var transferRecipientId by remember { mutableStateOf("") }
  var transferAmountText by remember { mutableStateOf("100") }
  var transferNote by remember { mutableStateOf("") }

  // Passbook ledger filter
  var ledgerCategoryFilter by remember { mutableStateOf("All") }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 16.dp)
        .testTag("digital_wallet_hub_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // ================= HEADER =================
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E40AF))
                  ),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Digital Wallet & Credits",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
              )
              Text(
                text = "For Ad Campaigns & Marketplace Purchases",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB)
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .background(Color(0xFFF1F5F9), CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF475569),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ================= 1. EXECUTIVE WALLET CARD =================
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1528)),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("digital_wallet_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CreditCard,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "BUSYCARDNET WALLET",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp,
                  color = Color.White
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .background(Color(0xFF10B981), CircleShape)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "1 Credit = ₹1.00",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6EE7B7)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "AVAILABLE BALANCE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = Color(0xFF94A3B8)
            )

            Row(
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "$creditBalance",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
              Text(
                text = "Credits (₹$creditBalance)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                modifier = Modifier.padding(bottom = 6.dp)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cardholder Details & Spend/Earn Mini Badges
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = userName,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFF1F5F9)
                )
                Text(
                  text = bcnId,
                  fontSize = 10.sp,
                  color = Color(0xFF94A3B8)
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF1E293B)
                ) {
                  Text(
                    text = "Earned: $totalEarned",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF1E293B)
                ) {
                  Text(
                    text = "Spent: $totalSpent",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ================= 2. QUICK SHORTCUTS =================
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          WalletQuickAction(
            title = "Top-Up",
            icon = Icons.Default.Add,
            badgeColor = Color(0xFF10B981),
            isSelected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            modifier = Modifier.weight(1f)
          )
          WalletQuickAction(
            title = "Transfer",
            icon = Icons.Default.Send,
            badgeColor = Color(0xFF2563EB),
            isSelected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            modifier = Modifier.weight(1f)
          )
          WalletQuickAction(
            title = "Passbook",
            icon = Icons.Default.History,
            badgeColor = Color(0xFF0F172A),
            isSelected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            modifier = Modifier.weight(1f)
          )
          WalletQuickAction(
            title = "Earn Free",
            icon = Icons.Default.Redeem,
            badgeColor = Color(0xFFD97706),
            isSelected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ================= 3. TAB NAVIGATION =================
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color(0xFFF1F5F9),
          contentColor = Color(0xFF1E40AF),
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = Color(0xFF1E40AF)
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("Top-Up Packs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("Passbook", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("Earn Free", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ================= 4. TAB CONTENTS =================
        Box(modifier = Modifier.weight(1f)) {
          when (selectedTab) {
            0 -> {
              // TOP-UP PACKS
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
              ) {
                item {
                  Text(
                    text = "Select Credit Recharge Pack:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                  )
                  Text(
                    text = "Credits are instantly usable for Ad Campaigns and Marketplace items",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                  )
                }

                items(WALLET_RECHARGE_PACKS) { pack ->
                  val isPackSelected = !isCustomAmountMode && selectedRechargePack?.id == pack.id
                  Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (isPackSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(
                      if (isPackSelected) 2.dp else 1.dp,
                      if (isPackSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        isCustomAmountMode = false
                        selectedRechargePack = pack
                      }
                      .testTag("recharge_pack_${pack.id}")
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                          modifier = Modifier
                            .size(36.dp)
                            .background(
                              if (isPackSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                              CircleShape
                            ),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = "💳",
                            fontSize = 16.sp
                          )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                              text = pack.name,
                              fontSize = 13.sp,
                              fontWeight = FontWeight.Bold,
                              color = Color(0xFF0F172A)
                            )
                            if (pack.isPopular) {
                              Spacer(modifier = Modifier.width(6.dp))
                              Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD97706)
                              ) {
                                Text(
                                  text = "POPULAR",
                                  fontSize = 8.sp,
                                  fontWeight = FontWeight.Black,
                                  color = Color.White,
                                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                              }
                            }
                          }
                          Text(
                            text = "${pack.credits} Credits + ${pack.bonusCredits} Bonus = ${pack.totalCredits} Credits",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                          )
                          Text(
                            text = pack.subtitle,
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                          )
                        }
                      }

                      Column(horizontalAlignment = Alignment.End) {
                        Text(
                          text = "₹${pack.priceRupees}",
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Black,
                          color = Color(0xFF0F172A)
                        )
                        Text(
                          text = "+${pack.bonusCredits} Free",
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF10B981)
                        )
                      }
                    }
                  }
                }

                // Custom Amount Option
                item {
                  Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (isCustomAmountMode) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(
                      if (isCustomAmountMode) 2.dp else 1.dp,
                      if (isCustomAmountMode) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { isCustomAmountMode = true }
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "Custom Recharge Amount",
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF0F172A)
                        )
                        Text(
                          text = "1 Credit = ₹1",
                          fontSize = 10.sp,
                          color = Color(0xFF64748B)
                        )
                      }

                      Spacer(modifier = Modifier.height(6.dp))

                      OutlinedTextField(
                        value = customRechargeAmount,
                        onValueChange = {
                          customRechargeAmount = it.filter { ch -> ch.isDigit() }
                          isCustomAmountMode = true
                        },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        suffix = { Text("Credits", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                      )
                    }
                  }
                }

                // Payment Method Selector
                item {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Payment Method:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    listOf("UPI / Google Pay", "Credit/Debit Card", "Net Banking").forEach { method ->
                      val isMethodSelected = selectedPaymentMethod == method
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMethodSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isMethodSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
                        modifier = Modifier
                          .weight(1f)
                          .clickable { selectedPaymentMethod = method }
                      ) {
                        Box(
                          contentAlignment = Alignment.Center,
                          modifier = Modifier.padding(vertical = 7.dp)
                        ) {
                          Text(
                            text = method.split(" ").first(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMethodSelected) Color.White else Color(0xFF0F172A)
                          )
                        }
                      }
                    }
                  }
                }

                // Recharge Button
                item {
                  val amountToRecharge = if (isCustomAmountMode) {
                    customRechargeAmount.toIntOrNull() ?: 100
                  } else {
                    selectedRechargePack?.totalCredits ?: 575
                  }
                  val priceToPay = if (isCustomAmountMode) {
                    customRechargeAmount.toIntOrNull() ?: 100
                  } else {
                    selectedRechargePack?.priceRupees ?: 500
                  }
                  val packLabel = if (isCustomAmountMode) "Custom Top-Up" else (selectedRechargePack?.name ?: "Recharge Pack")

                  Button(
                    onClick = {
                      if (amountToRecharge > 0) {
                        onTopUpCredits(amountToRecharge, selectedPaymentMethod, packLabel)
                        Toast.makeText(
                          context,
                          "🎉 Wallet recharged with +$amountToRecharge Credits via $selectedPaymentMethod!",
                          Toast.LENGTH_SHORT
                        ).show()
                      }
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(46.dp)
                      .testTag("submit_recharge_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                  ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "RECHARGE WALLET (Pay ₹$priceToPay)",
                      fontWeight = FontWeight.Black,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            }

            1 -> {
              // TRANSFER CREDITS
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
              ) {
                item {
                  Text(
                    text = "Transfer Credits to Cardholder:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                  )
                  Text(
                    text = "Send credits instantly to any registered BusyCardNet cardholder for services or deals",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                  )
                }

                item {
                  Text(text = "Recipient BusyCardNet ID:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                  Spacer(modifier = Modifier.height(4.dp))
                  OutlinedTextField(
                    value = transferRecipientId,
                    onValueChange = { transferRecipientId = it },
                    placeholder = { Text("e.g. IN-MH-MU-26-112MECH394") },
                    modifier = Modifier.fillMaxWidth().testTag("transfer_recipient_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                  )
                }

                item {
                  Text(text = "Credits to Transfer:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                  Spacer(modifier = Modifier.height(4.dp))
                  OutlinedTextField(
                    value = transferAmountText,
                    onValueChange = { transferAmountText = it.filter { ch -> ch.isDigit() } },
                    suffix = { Text("Credits (Available: $creditBalance)", fontSize = 10.sp, color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                  )
                }

                item {
                  Text(text = "Reason / Message (Optional):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                  Spacer(modifier = Modifier.height(4.dp))
                  OutlinedTextField(
                    value = transferNote,
                    onValueChange = { transferNote = it },
                    placeholder = { Text("e.g. Payment for electrical inspection") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                  )
                }

                item {
                  val transferAmount = transferAmountText.toIntOrNull() ?: 0
                  val canTransfer = transferAmount > 0 && transferAmount <= creditBalance && transferRecipientId.isNotBlank()

                  Button(
                    onClick = {
                      if (canTransfer) {
                        onTransferCredits(transferRecipientId, transferAmount, transferNote)
                        Toast.makeText(
                          context,
                          "💸 Sent $transferAmount Credits to $transferRecipientId!",
                          Toast.LENGTH_SHORT
                        ).show()
                        transferRecipientId = ""
                        transferNote = ""
                      } else if (transferAmount > creditBalance) {
                        Toast.makeText(context, "Insufficient credit balance. Top-up first!", Toast.LENGTH_SHORT).show()
                      } else {
                        Toast.makeText(context, "Please enter recipient ID and amount", Toast.LENGTH_SHORT).show()
                      }
                    },
                    enabled = canTransfer,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(46.dp)
                      .testTag("submit_transfer_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                  ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "TRANSFER $transferAmount CREDITS",
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            }

            2 -> {
              // PASSBOOK LEDGER
              Column(modifier = Modifier.fillMaxSize()) {
                // Category filter chips
                LazyRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  listOf("All", "Recharges", "Society Maintenance", "Ad Campaigns", "Marketplace", "Transfers", "Rewards").forEach { filter ->
                    val isFiltSelected = ledgerCategoryFilter == filter
                    item {
                      Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isFiltSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isFiltSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { ledgerCategoryFilter = filter }
                      ) {
                        Text(
                          text = filter,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isFiltSelected) Color.White else Color(0xFF334155),
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val filteredTransactions = transactions.filter { tx ->
                  if (ledgerCategoryFilter == "All") true
                  else tx.categoryName.equals(ledgerCategoryFilter, ignoreCase = true)
                }

                if (filteredTransactions.isEmpty()) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                      Spacer(modifier = Modifier.height(6.dp))
                      Text("No transactions found in this category.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                  }
                } else {
                  LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                  ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                          ) {
                            Box(
                              modifier = Modifier
                                .size(32.dp)
                                .background(
                                  if (tx.isPositive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                  CircleShape
                                ),
                              contentAlignment = Alignment.Center
                            ) {
                              Icon(
                                imageVector = if (tx.isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (tx.isPositive) Color(0xFF15803D) else Color(0xFFB91C1C),
                                modifier = Modifier.size(16.dp)
                              )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                              Text(
                                text = tx.description,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1
                              )
                              val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                              Text(
                                text = "${dateFormat.format(Date(tx.timestamp))} • ${tx.categoryName}",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                              )
                            }
                          }

                          Column(horizontalAlignment = Alignment.End) {
                            Text(
                              text = if (tx.isPositive) "+${tx.amount}" else "${tx.amount}",
                              fontSize = 13.sp,
                              fontWeight = FontWeight.Black,
                              color = if (tx.isPositive) Color(0xFF15803D) else Color(0xFFB91C1C)
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
            }

            3 -> {
              // EARN FREE CREDITS
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
              ) {
                item {
                  Text(
                    text = "Ways to Earn Free Credits:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                  )
                  Text(
                    text = "Grow your community network and receive immediate reward credits",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                  )
                }

                // 1. Share App
                item {
                  RewardActivityCard(
                    title = "Share BusyCardNet with Neighbors",
                    rewardText = "+50 Credits",
                    description = "Invite nearby shops, friends, and residents on WhatsApp or SMS",
                    buttonText = "Share App",
                    icon = Icons.Default.Share,
                    onClick = {
                      val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Join BusyCardNet Local Network")
                        putExtra(Intent.EXTRA_TEXT, "Connect with local businesses and service providers near you on BusyCardNet: https://busycardnet.in/invite/$bcnId")
                      }
                      context.startActivity(Intent.createChooser(intent, "Share BusyCardNet"))
                      onEarnCredits(50, "App Share Reward")
                    }
                  )
                }

                // 2. Daily Check-in
                item {
                  RewardActivityCard(
                    title = "Daily Network Check-In",
                    rewardText = "+10 Credits",
                    description = "Open the app daily and maintain your local active status badge",
                    buttonText = "Claim +10",
                    icon = Icons.Default.Star,
                    onClick = {
                      onEarnCredits(10, "Daily Networking Check-in Bonus")
                      Toast.makeText(context, "🌟 +10 Credits added to your wallet!", Toast.LENGTH_SHORT).show()
                    }
                  )
                }

                // 3. Complete KYC
                item {
                  RewardActivityCard(
                    title = "Complete Business Verification (KYC)",
                    rewardText = "+100 Credits",
                    description = "Verify Aadhaar/PAN or GST certificate to unlock Verified Partner badge",
                    buttonText = "Verify Now",
                    icon = Icons.Default.Verified,
                    onClick = {
                      onDismiss()
                    }
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Spending Shortcuts
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF1F5F9),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Use credits for:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF334155)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Button(
                onClick = {
                  onDismiss()
                  onOpenSocietyMaintenance()
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                modifier = Modifier
                  .height(30.dp)
                  .testTag("wallet_pay_society_maintenance_button")
              ) {
                Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Society Bill", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  onDismiss()
                  onOpenCampaignManager()
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.height(30.dp)
              ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Ad Boosts", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  onDismiss()
                  onOpenMarketplace()
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                modifier = Modifier.height(30.dp)
              ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Market", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun WalletQuickAction(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  badgeColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
    border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .background(badgeColor.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = badgeColor,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF334155)
      )
    }
  }
}

@Composable
private fun RewardActivityCard(
  title: String,
  rewardText: String,
  description: String,
  buttonText: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFF8FAFC),
    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(Color(0xFFFEF3C7), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFD97706),
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFDCFCE7)
            ) {
              Text(
                text = rewardText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF15803D),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }
          Text(text = description, fontSize = 10.sp, color = Color(0xFF64748B))
        }
      }

      Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
      ) {
        Text(buttonText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
