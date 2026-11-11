package com.example.ui.components.wallet

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.REGISTERED_SOCIETIES
import com.example.data.model.RegisteredSocietyInfo
import com.example.data.model.SocietyMaintenancePaymentRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Society Maintenance Payment & Digital Receipt Hub for Housing Societies (CHS / RWA)
 * and Commercial Market Complexes.
 * Supports itemized maintenance bills, custom bill override, Wallet Credits checkout,
 * Instant UPI / Card / NetBanking payment (+15 Cr cashback), and verified digital receipts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocietyMaintenanceDialog(
  memberName: String,
  memberBcnId: String,
  defaultLocality: String,
  walletCreditBalance: Int,
  paymentHistory: List<SocietyMaintenancePaymentRecord>,
  onOpenTopUpWallet: () -> Unit,
  onOpenQrScanner: (() -> Unit)? = null,
  onPayMaintenance: (
    societyName: String,
    registrationNumber: String,
    unitOrFlatNumber: String,
    memberName: String,
    memberBcnId: String,
    billingPeriod: String,
    amountRupees: Int,
    payWithWalletCredits: Boolean,
    directPaymentMethod: String,
    breakdownSummary: String,
    onReceiptGenerated: (SocietyMaintenancePaymentRecord?) -> Unit
  ) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Pay Bill, 1: Receipts History
  var selectedPreset by remember { mutableStateOf<RegisteredSocietyInfo?>(REGISTERED_SOCIETIES.first()) }
  var isCustomSociety by remember { mutableStateOf(false) }
  var customSocietyName by remember { mutableStateOf("") }
  var customSocietyRegNo by remember { mutableStateOf("MH/MUM/CHS/31048") }

  var unitOrFlatNo by remember { mutableStateOf("B-402") }
  var editableMemberName by remember { mutableStateOf(memberName.ifBlank { "Resident Member" }) }

  val billingPeriods = listOf(
    "November 2026 (Monthly)",
    "Q4 Oct–Dec 2026 (Quarterly)",
    "Annual FY 2026-27 (5% Rebate)"
  )
  var selectedBillingPeriod by remember { mutableStateOf(billingPeriods[0]) }

  // Vehicle Parking tier: 0 = None, 1 = Two-Wheeler (₹150), 2 = Four-Wheeler (₹400)
  var parkingTier by remember { mutableIntStateOf(1) }
  val parkingMonthlyCost = when (parkingTier) {
    1 -> 150
    2 -> 400
    else -> 0
  }

  var useCustomAmount by remember { mutableStateOf(false) }
  var customAmountInput by remember { mutableStateOf("2800") }

  val periodMultiplier = when {
    selectedBillingPeriod.startsWith("Q4") -> 3
    selectedBillingPeriod.startsWith("Annual") -> 12
    else -> 1
  }

  val basePreset = selectedPreset ?: REGISTERED_SOCIETIES.first()
  val itemizedMonthlyTotal =
    basePreset.baseMonthlyCharge +
      basePreset.waterCharge +
      basePreset.sinkingAndRepairFund +
      basePreset.securityAndLiftCharge +
      parkingMonthlyCost

  val calculatedAmount = if (useCustomAmount) {
    customAmountInput.toIntOrNull()?.coerceAtLeast(100) ?: 100
  } else {
    val gross = itemizedMonthlyTotal * periodMultiplier
    if (periodMultiplier == 12) (gross * 0.95).toInt() else gross
  }

  // Payment method: WALLET_CREDITS vs UPI / CARD / NETBANKING
  var payWithCredits by remember {
    mutableStateOf(walletCreditBalance >= calculatedAmount)
  }
  var directMethod by remember { mutableStateOf("UPI / Google Pay") }

  var latestGeneratedReceipt by remember { mutableStateOf<SocietyMaintenancePaymentRecord?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .heightIn(max = 760.dp)
        .padding(vertical = 12.dp)
        .testTag("society_maintenance_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp)
      ) {
        // ================= HEADER =================
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF0D9488))
                  ),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Apartment,
                contentDescription = "Society Maintenance",
                tint = Color(0xFF5EEAD4),
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Society Maintenance",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFFCCFBF1)
                ) {
                  Text(
                    text = "CHS / RWA",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F766E),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "Pay Housing & Commercial Premises Bills Instantly",
                fontSize = 11.sp,
                color = Color(0xFF475569)
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(34.dp)
              .background(Color(0xFFF1F5F9), CircleShape)
              .testTag("close_society_maintenance_dialog")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF334155),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ================= TABS =================
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color(0xFFF1F5F9),
          contentColor = Color(0xFF0F766E),
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = Color(0xFF0F766E)
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            selectedContentColor = Color(0xFF0F766E),
            unselectedContentColor = Color(0xFF64748B),
            text = {
              Text(
                text = "Pay Maintenance",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            },
            modifier = Modifier.testTag("society_tab_pay")
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            selectedContentColor = Color(0xFF0F766E),
            unselectedContentColor = Color(0xFF64748B),
            text = {
              Text(
                text = "Receipts (${paymentHistory.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            },
            modifier = Modifier.testTag("society_tab_receipts")
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ================= TAB CONTENT =================
        Box(modifier = Modifier.weight(1f)) {
          if (selectedTab == 0) {
            if (latestGeneratedReceipt != null) {
              // Instant Verified Receipt Screen after Payment
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
              ) {
                item {
                  SocietyReceiptCard(
                    receipt = latestGeneratedReceipt!!,
                    onShareReceipt = { record ->
                      shareSocietyReceipt(context, record)
                    }
                  )
                }
                item {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    OutlinedButton(
                      onClick = { latestGeneratedReceipt = null },
                      shape = RoundedCornerShape(12.dp),
                      modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                      Text("Pay Another Unit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                      onClick = onDismiss,
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                      modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                      Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }
                }
              }
            } else {
              // Bill Form & Breakdown
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
              ) {
                // 1. Society Selection + Scan Society QR Option
                item {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "1. Select Housing Society / Complex",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color(0xFF0F172A)
                    )
                    if (onOpenQrScanner != null) {
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier
                          .clickable {
                            onDismiss()
                            onOpenQrScanner()
                          }
                          .testTag("society_scan_qr_button")
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Society QR",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(13.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "Scan Society QR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                          )
                        }
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(REGISTERED_SOCIETIES) { soc ->
                      val isSelected = !isCustomSociety && selectedPreset?.id == soc.id
                      Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                          1.2.dp,
                          if (isSelected) Color(0xFF14B8A6) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                          .width(210.dp)
                          .clickable {
                            isCustomSociety = false
                            selectedPreset = soc
                          }
                          .testTag("society_preset_${soc.id}")
                      ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                              text = soc.name,
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold,
                              color = if (isSelected) Color.White else Color(0xFF0F172A),
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis,
                              modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                              imageVector = Icons.Default.Verified,
                              contentDescription = null,
                              tint = Color(0xFF14B8A6),
                              modifier = Modifier.size(13.dp)
                            )
                          }
                          Text(
                            text = soc.locality,
                            fontSize = 10.sp,
                            color = if (isSelected) Color(0xFF94A3B8) else Color(0xFF64748B),
                            maxLines = 1
                          )
                          Text(
                            text = "Reg: ${soc.registrationNumber}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) Color(0xFF5EEAD4) else Color(0xFF0F766E),
                            maxLines = 1
                          )
                        }
                      }
                    }

                    item {
                      Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCustomSociety) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                          1.2.dp,
                          if (isCustomSociety) Color(0xFF14B8A6) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                          .width(150.dp)
                          .clickable { isCustomSociety = true }
                          .testTag("society_custom_toggle")
                      ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                          Text(
                            text = "+ Other Society",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomSociety) Color.White else Color(0xFF0F172A)
                          )
                          Text(
                            text = "Enter custom CHS / RWA name",
                            fontSize = 10.sp,
                            color = if (isCustomSociety) Color(0xFF94A3B8) else Color(0xFF64748B)
                          )
                          Text(
                            text = defaultLocality.take(18),
                            fontSize = 9.sp,
                            color = Color(0xFF0F766E)
                          )
                        }
                      }
                    }
                  }

                  androidx.compose.animation.AnimatedVisibility(visible = isCustomSociety) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                      OutlinedTextField(
                        value = customSocietyName,
                        onValueChange = { customSocietyName = it },
                        label = { Text("Society / Complex Name", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Shanti Niketan CHS Ltd.") },
                        singleLine = true,
                        modifier = Modifier
                          .fillMaxWidth()
                          .testTag("custom_society_name_input")
                      )
                      Spacer(modifier = Modifier.height(6.dp))
                      OutlinedTextField(
                        value = customSocietyRegNo,
                        onValueChange = { customSocietyRegNo = it },
                        label = { Text("Society Registration No. (Optional)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                      )
                    }
                  }
                }

                // 2. Unit / Flat Number & Member Details
                item {
                  Text(
                    text = "2. Flat / Shop Unit & Billing Period",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = unitOrFlatNo,
                      onValueChange = { unitOrFlatNo = it },
                      label = { Text("Wing & Flat / Shop", fontSize = 11.sp) },
                      placeholder = { Text("B-402 / G-12") },
                      singleLine = true,
                      modifier = Modifier
                        .weight(0.95f)
                        .testTag("society_flat_number_input")
                    )
                    OutlinedTextField(
                      value = editableMemberName,
                      onValueChange = { editableMemberName = it },
                      label = { Text("Member Name", fontSize = 11.sp) },
                      singleLine = true,
                      modifier = Modifier
                        .weight(1.35f)
                        .testTag("society_member_name_input")
                    )
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    billingPeriods.forEach { period ->
                      val selected = selectedBillingPeriod == period
                      Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) Color(0xFF0F766E) else Color(0xFFF1F5F9),
                        border = BorderStroke(
                          1.dp,
                          if (selected) Color(0xFF0F766E) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.clickable { selectedBillingPeriod = period }
                      ) {
                        Text(
                          text = period,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (selected) Color.White else Color(0xFF334155),
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                      }
                    }
                  }
                }

                // 3. Itemized Maintenance Bill Breakdown
                item {
                  Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "3. Maintenance Bill Heads",
                          fontSize = 12.sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = Color(0xFF0F172A)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = "Custom Amount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Switch(
                            checked = useCustomAmount,
                            onCheckedChange = { useCustomAmount = it },
                            modifier = Modifier.testTag("society_custom_amount_switch")
                          )
                        }
                      }

                      if (useCustomAmount) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                          value = customAmountInput,
                          onValueChange = { val clean = it.filter { ch -> ch.isDigit() }; customAmountInput = clean },
                          label = { Text("Enter Exact Maintenance Bill Amount (₹)", fontSize = 11.sp) },
                          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                          singleLine = true,
                          modifier = Modifier
                            .fillMaxWidth()
                            .testTag("society_custom_amount_input")
                        )
                      } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        BillLineRow("General Service & Common Area", basePreset.baseMonthlyCharge * periodMultiplier)
                        BillLineRow("Municipal Water Charges", basePreset.waterCharge * periodMultiplier)
                        BillLineRow("Sinking & Major Repair Fund", basePreset.sinkingAndRepairFund * periodMultiplier)
                        BillLineRow("24x7 Security, Lift & DG Backup", basePreset.securityAndLiftCharge * periodMultiplier)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                              imageVector = Icons.Default.DirectionsCar,
                              contentDescription = null,
                              tint = Color(0xFF0F766E),
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vehicle Parking:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                          }
                          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(0 to "None", 1 to "2W ₹150", 2 to "4W ₹400").forEach { (tier, label) ->
                              val sel = parkingTier == tier
                              Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (sel) Color(0xFF0F172A) else Color.White,
                                border = BorderStroke(1.dp, if (sel) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { parkingTier = tier }
                              ) {
                                Text(
                                  text = label,
                                  fontSize = 9.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = if (sel) Color.White else Color(0xFF475569),
                                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                              }
                            }
                          }
                        }

                        if (periodMultiplier == 12) {
                          Spacer(modifier = Modifier.height(4.dp))
                          Text(
                            text = "✓ Includes 5% Annual Advance Payment Rebate",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                          )
                        }
                      }

                      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFCBD5E1))

                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "Total Maintenance Payable",
                          fontSize = 13.sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = Color(0xFF0F172A)
                        )
                        Text(
                          text = "₹$calculatedAmount",
                          fontSize = 18.sp,
                          fontWeight = FontWeight.Black,
                          color = Color(0xFF0F766E)
                        )
                      }
                    }
                  }
                }

                // 4. Payment Mode Selector: Wallet Credits vs Direct UPI/Card
                item {
                  Text(
                    text = "4. Choose Payment Method",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                  )
                  Spacer(modifier = Modifier.height(8.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    // Option A: BusyCardNet Wallet Credits
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = if (payWithCredits) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                      border = BorderStroke(
                        1.5.dp,
                        if (payWithCredits) Color(0xFF059669) else Color(0xFFCBD5E1)
                      ),
                      modifier = Modifier
                        .weight(1f)
                        .clickable { payWithCredits = true }
                        .testTag("society_pay_mode_credits")
                    ) {
                      Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = "Wallet Credits",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                          )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Balance: $walletCreditBalance Cr",
                          fontSize = 10.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = if (walletCreditBalance >= calculatedAmount) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                        if (walletCreditBalance < calculatedAmount) {
                          Text(
                            text = "Tap Top-Up or use UPI",
                            fontSize = 9.sp,
                            color = Color(0xFFD97706)
                          )
                        }
                      }
                    }

                    // Option B: Direct UPI / Card / NetBanking (+15 Cashback Credits)
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = if (!payWithCredits) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                      border = BorderStroke(
                        1.5.dp,
                        if (!payWithCredits) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                      ),
                      modifier = Modifier
                        .weight(1f)
                        .clickable { payWithCredits = false }
                        .testTag("society_pay_mode_upi")
                    ) {
                      Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = "UPI / Card / Bank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                          )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Earn +15 Bonus Credits",
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF2563EB)
                        )
                        Text(
                          text = "Zero convenience fee",
                          fontSize = 9.sp,
                          color = Color(0xFF64748B)
                        )
                      }
                    }
                  }

                  androidx.compose.animation.AnimatedVisibility(visible = !payWithCredits) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      listOf("UPI / Google Pay", "Debit / Credit Card", "NetBanking").forEach { method ->
                        val isMethodSel = directMethod == method
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = if (isMethodSel) Color(0xFF1E40AF) else Color(0xFFF1F5F9),
                          modifier = Modifier
                            .weight(1f)
                            .clickable { directMethod = method }
                        ) {
                          Text(
                            text = method,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMethodSel) Color.White else Color(0xFF334155),
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                          )
                        }
                      }
                    }
                  }

                  if (payWithCredits && walletCreditBalance < calculatedAmount) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                      onClick = onOpenTopUpWallet,
                      shape = RoundedCornerShape(10.dp),
                      border = BorderStroke(1.dp, Color(0xFF0284C7)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Recharge Wallet Credits (Need ${calculatedAmount - walletCreditBalance} more Cr)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                      )
                    }
                  }
                }
              }
            }
          } else {
            // RECEIPTS HISTORY TAB
            if (paymentHistory.isEmpty()) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(44.dp)
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "No society maintenance receipts yet",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                  )
                  Text(
                    text = "Your paid CHS / RWA digital receipts will appear here for instant download & sharing.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                  )
                }
              }
            } else {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
              ) {
                items(paymentHistory, key = { it.receiptId }) { record ->
                  SocietyReceiptCard(
                    receipt = record,
                    onShareReceipt = { shareSocietyReceipt(context, it) }
                  )
                }
              }
            }
          }
        }

        // ================= BOTTOM PAY CTA =================
        if (selectedTab == 0 && latestGeneratedReceipt == null) {
          Spacer(modifier = Modifier.height(8.dp))
          val finalSocietyName = if (isCustomSociety) {
            customSocietyName.trim().ifBlank { "Registered Housing Society" }
          } else {
            basePreset.name
          }
          val finalRegNo = if (isCustomSociety) {
            customSocietyRegNo.trim().ifBlank { "CHS/REG/VERIFIED" }
          } else {
            basePreset.registrationNumber
          }
          val breakdownSummary = if (useCustomAmount) {
            "Custom Maintenance Bill Amount (₹$calculatedAmount)"
          } else {
            "Service ₹${basePreset.baseMonthlyCharge * periodMultiplier} + Water ₹${basePreset.waterCharge * periodMultiplier} + Sinking ₹${basePreset.sinkingAndRepairFund * periodMultiplier} + Sec/Lift ₹${basePreset.securityAndLiftCharge * periodMultiplier}" +
              if (parkingMonthlyCost > 0) " + Parking ₹${parkingMonthlyCost * periodMultiplier}" else ""
          }

          Button(
            onClick = {
              if (unitOrFlatNo.isBlank()) {
                Toast.makeText(context, "Please enter your Wing & Flat / Unit number", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (payWithCredits && walletCreditBalance < calculatedAmount) {
                Toast.makeText(
                  context,
                  "Insufficient wallet credits! Switch to UPI/Card or top up your wallet.",
                  Toast.LENGTH_SHORT
                ).show()
                return@Button
              }

              onPayMaintenance(
                finalSocietyName,
                finalRegNo,
                unitOrFlatNo.trim(),
                editableMemberName.trim().ifBlank { "Member" },
                memberBcnId,
                selectedBillingPeriod,
                calculatedAmount,
                payWithCredits,
                directMethod,
                breakdownSummary
              ) { generatedReceipt ->
                if (generatedReceipt != null) {
                  latestGeneratedReceipt = generatedReceipt
                  Toast.makeText(
                    context,
                    "✅ Society Maintenance ₹$calculatedAmount Paid! Receipt ${generatedReceipt.receiptId} generated.",
                    Toast.LENGTH_LONG
                  ).show()
                } else {
                  Toast.makeText(context, "Payment could not be completed. Check wallet balance.", Toast.LENGTH_SHORT).show()
                }
              }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("confirm_pay_society_maintenance_button")
          ) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (payWithCredits) {
                "Pay $calculatedAmount Credits (₹$calculatedAmount) & Get Receipt"
              } else {
                "Pay ₹$calculatedAmount via $directMethod"
              },
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BillLineRow(label: String, amount: Int) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 11.sp, color = Color(0xFF475569))
    Text(text = "₹$amount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
  }
}

@Composable
private fun SocietyReceiptCard(
  receipt: SocietyMaintenancePaymentRecord,
  onShareReceipt: (SocietyMaintenancePaymentRecord) -> Unit
) {
  val dateStr = remember(receipt.timestamp) {
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(receipt.timestamp))
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = BorderStroke(1.5.dp, Color(0xFF14B8A6)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("society_receipt_card_${receipt.receiptId}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = receipt.status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF34D399)
          )
        }
        Text(
          text = receipt.receiptId,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF5EEAD4)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = receipt.societyName,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
      )
      if (receipt.registrationNumber.isNotBlank()) {
        Text(
          text = "Reg No: ${receipt.registrationNumber}",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = Color(0xFF94A3B8)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("UNIT / FLAT", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
          Text(receipt.unitOrFlatNumber, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Column {
          Text("MEMBER", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
          Text(receipt.memberName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text("AMOUNT PAID", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
          Text("₹${receipt.amountRupees}", fontSize = 16.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Black)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Period: ${receipt.billingPeriod} • Paid via ${receipt.paymentMethod}",
        fontSize = 10.sp,
        color = Color(0xFFCBD5E1)
      )
      if (receipt.breakdownSummary.isNotBlank()) {
        Text(
          text = receipt.breakdownSummary,
          fontSize = 9.sp,
          color = Color(0xFF94A3B8)
        )
      }
      Text(
        text = "Timestamp: $dateStr • BCN ID: ${receipt.memberBcnId}",
        fontSize = 9.sp,
        color = Color(0xFF64748B)
      )

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedButton(
        onClick = { onShareReceipt(receipt) },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF14B8A6)),
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
          .testTag("share_society_receipt_button")
      ) {
        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF5EEAD4), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Share Official Receipt to Society Secretary / WhatsApp",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF5EEAD4)
        )
      }
    }
  }
}

private fun shareSocietyReceipt(
  context: android.content.Context,
  receipt: SocietyMaintenancePaymentRecord
) {
  val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(receipt.timestamp))
  val shareText = buildString {
    appendLine("🏢 *OFFICIAL SOCIETY MAINTENANCE RECEIPT*")
    appendLine("Receipt No: ${receipt.receiptId}")
    appendLine("Society: ${receipt.societyName}")
    if (receipt.registrationNumber.isNotBlank()) {
      appendLine("Reg. No: ${receipt.registrationNumber}")
    }
    appendLine("Unit / Flat No: *${receipt.unitOrFlatNumber}*")
    appendLine("Member: ${receipt.memberName} (${receipt.memberBcnId})")
    appendLine("Billing Cycle: ${receipt.billingPeriod}")
    appendLine("Amount Paid: *₹${receipt.amountRupees}*")
    appendLine("Payment Mode: ${receipt.paymentMethod}")
    appendLine("Date: $dateStr")
    appendLine("Status: ✅ ${receipt.status}")
  }
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, "Society Maintenance Receipt - ${receipt.unitOrFlatNumber}")
    putExtra(Intent.EXTRA_TEXT, shareText)
  }
  context.startActivity(Intent.createChooser(intent, "Share Maintenance Receipt"))
}
