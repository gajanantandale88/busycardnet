package com.example.ui.components.marketing

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MARKETING_PLANS
import com.example.data.model.MarketingAdCategory
import com.example.data.model.MarketingCampaign
import com.example.data.model.MarketingPlan
import com.example.data.model.TARGET_AUDIENCE_SEGMENTS
import com.example.data.model.TargetAudienceSegment

/**
 * Facebook-Ads-Grade Local Marketing Campaign Builder.
 * Allows shops, professionals, and service businesses to launch hyper-targeted
 * campaigns with customizable plans, durations, target areas, audience categories, and CTA.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketProductCampaignDialog(
  initialLocation: String = "Kurla West, Mumbai",
  initialPhone: String = "+91 98765 43210",
  businessName: String = "My Business",
  bcnId: String = "IN-MH-MU-26-374BIZ691",
  walletCreditBalance: Int = 100,
  onOpenTopUpWallet: () -> Unit = {},
  onDismiss: () -> Unit,
  onLaunchCampaign: (MarketingCampaign, Boolean) -> Unit
) {
  var selectedCategory by remember { mutableStateOf(MarketingAdCategory.SHOPS_RETAIL) }
  var selectedSubCategory by remember { mutableStateOf(MarketingAdCategory.SHOPS_RETAIL.subCategories.first()) }
  var selectedPlan by remember { mutableStateOf(MARKETING_PLANS[1]) } // Default to Professional Growth
  var durationDays by remember { mutableIntStateOf(7) }
  var selectedArea by remember { mutableStateOf(initialLocation) }
  var showAreaDropdown by remember { mutableStateOf(false) }
  var selectedRadiusMeters by remember { mutableIntStateOf(3000) } // 3 km default
  var selectedAudience by remember { mutableStateOf(TARGET_AUDIENCE_SEGMENTS[0]) } // Local Residents & Families
  var selectedAgeGroup by remember { mutableStateOf("All Ages (18-65+)") }
  var selectedCta by remember { mutableStateOf("WHATSAPP") }
  var contactPhone by remember { mutableStateOf(initialPhone) }
  var payWithWalletCredits by remember { mutableStateOf(walletCreditBalance >= MARKETING_PLANS[1].priceRupees) }

  var campaignTitle by remember {
    mutableStateOf("Grand Festival Offer! Flat 25% Off on All Products & Services")
  }
  var campaignDescription by remember {
    mutableStateOf("Visit our store or get express same-day doorstep service within 30 minutes. 100% genuine quality guaranteed. Tap WhatsApp to claim your special voucher!")
  }

  val scrollState = rememberScrollState()

  // Dynamic calculations
  val calculatedDailyBudget = remember(selectedPlan, durationDays) {
    (selectedPlan.priceRupees / durationDays.coerceAtLeast(1)).coerceAtLeast(20)
  }
  val estimatedAudienceReach = remember(selectedRadiusMeters, selectedPlan) {
    val baseReach = when (selectedRadiusMeters) {
      500 -> 3500
      1000 -> 9200
      3000 -> 24500
      5000 -> 48000
      10000 -> 110000
      else -> 280000
    }
    (baseReach * (selectedPlan.guaranteedImpressions / 3500f)).toInt().coerceAtLeast(selectedPlan.guaranteedImpressions)
  }

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
        .testTag("market_product_campaign_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(scrollState)
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
                    colors = listOf(Color(0xFF1E40AF), Color(0xFF2563EB))
                  ),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Market the Product",
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
              )
              Text(
                text = "Facebook-Style Targeted Ad Campaign",
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

        Spacer(modifier = Modifier.height(18.dp))

        // ================= 1. AD CATEGORY (SHOPS, PROFESSIONALS, SERVICES) =================
        SectionHeader(
          stepNumber = "1",
          title = "Ad Category (Facebook-Style)",
          subtitle = "Select business type to optimize reach for local buyers"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Category Tabs
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(MarketingAdCategory.values()) { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF8FAFC),
              border = BorderStroke(
                1.5.dp,
                if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
              ),
              modifier = Modifier
                .clickable {
                  selectedCategory = cat
                  selectedSubCategory = cat.subCategories.first()
                }
                .testTag("ad_category_${cat.code}")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
              ) {
                Text(text = cat.emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = cat.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subcategory Pills
        Text(
          text = "Specialization / Subcategory:",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF475569)
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          selectedCategory.subCategories.forEach { sub ->
            val isSubSelected = selectedSubCategory == sub
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSubSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (isSubSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
              modifier = Modifier.clickable { selectedSubCategory = sub }
            ) {
              Text(
                text = sub,
                fontSize = 11.sp,
                fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSubSelected) Color.White else Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= 2. MARKETING PLANS =================
        SectionHeader(
          stepNumber = "2",
          title = "Select Marketing Plan",
          subtitle = "Choose your guaranteed views and campaign intensity"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          MARKETING_PLANS.forEach { plan ->
            val isPlanSelected = selectedPlan.id == plan.id
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isPlanSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
              ),
              border = BorderStroke(
                if (isPlanSelected) 2.dp else 1.dp,
                if (isPlanSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedPlan = plan }
                .testTag("plan_card_${plan.id}")
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
                        .size(20.dp)
                        .border(
                          2.dp,
                          if (isPlanSelected) Color(0xFF2563EB) else Color(0xFF94A3B8),
                          CircleShape
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      if (isPlanSelected) {
                        Box(
                          modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF2563EB), CircleShape)
                        )
                      }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = plan.name,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF0F172A)
                    )
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (plan.badgeText != null) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFD97706),
                        modifier = Modifier.padding(end = 8.dp)
                      ) {
                        Text(
                          text = plan.badgeText,
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Black,
                          color = Color.White,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                    Text(
                      text = "₹${plan.priceRupees}",
                      fontSize = 16.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFF1E40AF)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Visibility,
                      contentDescription = null,
                      tint = Color(0xFF2563EB),
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${plan.guaranteedImpressions} Views",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF1E293B)
                    )
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Radar,
                      contentDescription = null,
                      tint = Color(0xFF0284C7),
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = plan.radiusLabel,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF1E293B)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = plan.features.joinToString(" • "),
                  fontSize = 10.sp,
                  color = Color(0xFF64748B)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= 3. CAMPAIGN DURATION (DAYS) =================
        SectionHeader(
          stepNumber = "3",
          title = "Campaign Duration (Days)",
          subtitle = "Set how many days the campaign runs"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(1 to "1 Day", 3 to "3 Days", 7 to "7 Days", 14 to "14 Days", 30 to "30 Days").forEach { (days, label) ->
            val isDurSelected = durationDays == days
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isDurSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (isDurSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
              modifier = Modifier
                .weight(1f)
                .clickable { durationDays = days }
                .testTag("duration_${days}_days")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 9.dp)
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDurSelected) Color.White else Color(0xFF0F172A)
                  )
                  Text(
                    text = if (days == 7) "Recommended" else "Active",
                    fontSize = 8.sp,
                    color = if (isDurSelected) Color(0xFF93C5FD) else Color(0xFF64748B)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFF8FAFC),
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Daily Spend: ~₹$calculatedDailyBudget / day",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
              )
            }
            Text(
              text = "Total: ₹${selectedPlan.priceRupees}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF1E40AF)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= 4. TARGET AREA & RADIUS =================
        SectionHeader(
          stepNumber = "4",
          title = "Target Area & Geofence Radius",
          subtitle = "Where should your local customers see this ad?"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Area Selector Dropdown
        Text(text = "Target Locality / Area:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        Box {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showAreaDropdown = true }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = selectedArea, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
              }
              Text(text = "Change ▼", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
          }

          DropdownMenu(
            expanded = showAreaDropdown,
            onDismissRequest = { showAreaDropdown = false }
          ) {
            listOf(
              "Kurla West, Mumbai",
              "Kurla East, Mumbai",
              "Bandra West, Mumbai",
              "Andheri East, Mumbai",
              "Dadar Central, Mumbai",
              "Ghatkopar West, Mumbai",
              "Thane West, MMR",
              "Pune Central, MH"
            ).forEach { loc ->
              DropdownMenuItem(
                text = { Text(loc, fontWeight = FontWeight.SemiBold) },
                onClick = {
                  selectedArea = loc
                  showAreaDropdown = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Radius Chips
        Text(text = "Geofence Radius:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            500 to "500m",
            1000 to "1 km",
            3000 to "3 km",
            5000 to "5 km",
            10000 to "10 km",
            25000 to "25 km"
          ).forEach { (meters, label) ->
            val isRadSelected = selectedRadiusMeters == meters
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isRadSelected) Color(0xFF0284C7) else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (isRadSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1)),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedRadiusMeters = meters }
                .testTag("radius_${meters}m")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 7.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isRadSelected) Color.White else Color(0xFF0F172A)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Estimated Radius Audience info badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFE0F2FE),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Radar, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Estimated Reach: ~$estimatedAudienceReach cardholders & residents within ${if (selectedRadiusMeters < 1000) "${selectedRadiusMeters}m" else "${selectedRadiusMeters / 1000}km"} of $selectedArea",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF0369A1)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= 5. TARGET AUDIENCE (PEOPLE CATEGORY) =================
        SectionHeader(
          stepNumber = "5",
          title = "Target Audience (Category of People)",
          subtitle = "Which segment of people do you want to campaign for?"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TARGET_AUDIENCE_SEGMENTS.forEach { segment ->
            val isAudSelected = selectedAudience.id == segment.id
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isAudSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
              border = BorderStroke(
                if (isAudSelected) 1.5.dp else 1.dp,
                if (isAudSelected) Color(0xFF10B981) else Color(0xFFE2E8F0)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedAudience = segment }
                .testTag("audience_${segment.id}")
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = segment.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = segment.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                  )
                  Text(
                    text = segment.description,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                  )
                }
                if (isAudSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Age group selector
        Text(text = "Target Age Group:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("All Ages (18-65+)", "18-28 Young", "25-45 Working", "40+ Family").forEach { age ->
            val isAgeSelected = selectedAgeGroup == age
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isAgeSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (isAgeSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedAgeGroup = age }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              ) {
                Text(
                  text = age.split(" ").first(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isAgeSelected) Color.White else Color(0xFF0F172A)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= 6. AD CONTENT & CALL TO ACTION =================
        SectionHeader(
          stepNumber = "6",
          title = "Ad Content & Call-to-Action",
          subtitle = "What do you want people to do when they see this ad?"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Ad Headline / Product Offer:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = campaignTitle,
          onValueChange = { campaignTitle = it },
          placeholder = { Text("e.g. 50% Off On First AC Service") },
          modifier = Modifier.fillMaxWidth().testTag("campaign_title_input"),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Ad Description / Pitch:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = campaignDescription,
          onValueChange = { campaignDescription = it },
          placeholder = { Text("Describe what makes your product/service special...") },
          modifier = Modifier.fillMaxWidth().testTag("campaign_desc_input"),
          shape = RoundedCornerShape(10.dp),
          minLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CTA Buttons
        Text(text = "Direct Action Button (CTA):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "WHATSAPP" to "💬 WhatsApp",
            "CALL" to "📞 Call Now",
            "VIEW_CARD" to "📇 View Card",
            "DIRECTIONS" to "📍 Shop Map"
          ).forEach { (cta, label) ->
            val isCtaSelected = selectedCta == cta
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isCtaSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
              border = BorderStroke(1.dp, if (isCtaSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedCta = cta }
                .testTag("cta_$cta")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCtaSelected) Color.White else Color(0xFF0F172A)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Contact Phone / WhatsApp:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = contactPhone,
          onValueChange = { contactPhone = it },
          placeholder = { Text("+91 98765 43210") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ================= 7. LIVE FACEBOOK-STYLE AD PREVIEW =================
        Text(
          text = "LIVE SPONSORED AD PREVIEW",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF0F172A),
          letterSpacing = 0.5.sp
        )
        Text(
          text = "This is exactly how your ad appears in the BusyCardNet feed:",
          fontSize = 10.sp,
          color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mock Ad Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF1E40AF), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = businessName.take(2).uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = businessName,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Verified,
                      contentDescription = null,
                      tint = Color(0xFF2563EB),
                      modifier = Modifier.size(14.dp)
                    )
                  }
                  Text(
                    text = "Sponsored • ${selectedCategory.displayName} • $selectedArea",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFEF3C7)
              ) {
                Text(
                  text = "📢 SPONSORED",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFFD97706),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
              text = campaignTitle,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = campaignDescription,
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Targeting Pills Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F9)
              ) {
                Text(
                  text = "🎯 ${selectedAudience.title}",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF334155),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE0F2FE)
              ) {
                Text(
                  text = "📍 ${if (selectedRadiusMeters < 1000) "${selectedRadiusMeters}m" else "${selectedRadiusMeters / 1000}km"} Radius",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0369A1),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preview CTA Button
            Button(
              onClick = {},
              modifier = Modifier.fillMaxWidth().height(40.dp),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
              val ctaLabel = when (selectedCta) {
                "WHATSAPP" -> "💬 Send WhatsApp Message"
                "CALL" -> "📞 Call $contactPhone"
                "VIEW_CARD" -> "📇 View Digital Visiting Card"
                else -> "📍 Get Directions to Store"
              }
              Text(ctaLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= 8. LAUNCH CAMPAIGN ACTION =================
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Campaign Investment",
                  fontSize = 11.sp,
                  color = Color(0xFF94A3B8)
                )
                Text(
                  text = "₹${selectedPlan.priceRupees}",
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.White
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "${selectedPlan.guaranteedImpressions} Views Guaranteed",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF38BDF8)
                )
                Text(
                  text = "$durationDays Days • $selectedArea",
                  fontSize = 10.sp,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Method Selection: Digital Wallet Credits vs Direct UPI/Card
            Text(
              text = "Payment Method:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFCBD5E1)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val hasEnoughCredits = walletCreditBalance >= selectedPlan.priceRupees
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (payWithWalletCredits) Color(0xFF1E293B) else Color(0xFF111827),
                border = BorderStroke(
                  1.5.dp,
                  if (payWithWalletCredits) Color(0xFF38BDF8) else Color(0xFF374151)
                ),
                modifier = Modifier
                  .weight(1f)
                  .clickable { payWithWalletCredits = true }
                  .testTag("pay_with_wallet_option")
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Payment,
                      contentDescription = null,
                      tint = if (payWithWalletCredits) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Digital Wallet",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                  Text(
                    text = "Bal: $walletCreditBalance Credits",
                    fontSize = 10.sp,
                    color = if (hasEnoughCredits) Color(0xFF4ADE80) else Color(0xFFF87171),
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (!payWithWalletCredits) Color(0xFF1E293B) else Color(0xFF111827),
                border = BorderStroke(
                  1.5.dp,
                  if (!payWithWalletCredits) Color(0xFF38BDF8) else Color(0xFF374151)
                ),
                modifier = Modifier
                  .weight(1f)
                  .clickable { payWithWalletCredits = false }
                  .testTag("pay_with_upi_option")
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CreditCard,
                      contentDescription = null,
                      tint = if (!payWithWalletCredits) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "UPI / Card",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                  Text(
                    text = "Instant Pay",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                  )
                }
              }
            }

            if (payWithWalletCredits && walletCreditBalance < selectedPlan.priceRupees) {
              Spacer(modifier = Modifier.height(8.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Short by ${selectedPlan.priceRupees - walletCreditBalance} Credits",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFCA5A5)
                  )
                  Button(
                    onClick = {
                      onDismiss()
                      onOpenTopUpWallet()
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                  ) {
                    Text("Top-Up Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val canSubmitWithWallet = !payWithWalletCredits || walletCreditBalance >= selectedPlan.priceRupees

            Button(
              onClick = {
                val newCampaign = MarketingCampaign(
                  title = campaignTitle,
                  description = campaignDescription,
                  adCategory = selectedCategory.code,
                  adCategoryDisplayName = selectedCategory.displayName,
                  adSubCategory = selectedSubCategory,
                  planName = selectedPlan.name,
                  planPriceRupees = selectedPlan.priceRupees,
                  durationDays = durationDays,
                  targetArea = selectedArea,
                  targetRadiusMeters = selectedRadiusMeters,
                  targetAudienceCategory = selectedAudience.title,
                  targetAgeGroup = selectedAgeGroup,
                  ctaType = selectedCta,
                  ctaContact = contactPhone,
                  totalBudgetRupees = selectedPlan.priceRupees,
                  dailyBudgetRupees = calculatedDailyBudget,
                  status = "ACTIVE"
                )
                onLaunchCampaign(newCampaign, payWithWalletCredits)
              },
              enabled = canSubmitWithWallet,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (payWithWalletCredits) Color(0xFF10B981) else Color(0xFF2563EB)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("launch_marketing_campaign_button")
            ) {
              Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              val buttonText = if (payWithWalletCredits) {
                "🚀 PAY WITH WALLET (${selectedPlan.priceRupees} Credits)"
              } else {
                "🚀 LAUNCH CAMPAIGN (Pay ₹${selectedPlan.priceRupees})"
              }
              Text(
                text = buttonText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SectionHeader(
  stepNumber: String,
  title: String,
  subtitle: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .background(Color(0xFF0F172A), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stepNumber,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = Color(0xFF64748B)
      )
    }
  }
}
