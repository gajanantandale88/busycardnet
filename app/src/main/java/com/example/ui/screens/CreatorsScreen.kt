package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CardTemplateEntity
import com.example.ui.components.BcnCardHyperlink
import com.example.ui.components.InteractiveTouchIcon
import com.example.ui.viewmodel.BusyCardNetViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatorsScreen(
  viewModel: BusyCardNetViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val templates by viewModel.allTemplates.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Creator Studio, 1: Community Templates

  // Creator Studio Form State
  var templateTitle by remember { mutableStateOf("Volt Master Deluxe") }
  var targetProfession by remember { mutableStateOf("ELEC") }
  var selectedThemeStyle by remember { mutableStateOf("ELECTRIC_AMBER") } // ELECTRIC_AMBER, LUXURY_GOLD, MINIMAL_LIGHT, CREATIVE_CYAN
  var templatePrice by remember { mutableDoubleStateOf(99.0) }

  // Interactive Touch Icon toggles (Website, Email, Phone, Photo, BCN Link, LinkedIn, Facebook, WhatsApp, Instagram)
  var hasWebsiteIcon by remember { mutableStateOf(true) }
  var hasEmailIcon by remember { mutableStateOf(true) }
  var hasPhoneIcon by remember { mutableStateOf(true) }
  var hasPhotoSlot by remember { mutableStateOf(true) }
  var hasBusyCardNetLink by remember { mutableStateOf(true) }
  var hasLinkedInIcon by remember { mutableStateOf(true) }
  var hasFacebookIcon by remember { mutableStateOf(true) }
  var hasWhatsAppIcon by remember { mutableStateOf(true) }
  var hasInstagramIcon by remember { mutableStateOf(true) }

  var publishSuccessDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090E17))
      .testTag("creators_screen")
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
              text = "BCN CREATOR STUDIO",
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.5.sp,
              color = Color(0xFFF59E0B)
            )
            Text(
              text = "Design & Publish Visiting Card Templates",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
          ) {
            Text(
              text = "Earn 80% Royalty",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF59E0B),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Navigation: [Creator Studio] | [Community Gallery]
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          contentColor = Color(0xFFF59E0B),
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = Color(0xFFF59E0B)
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            selectedContentColor = Color(0xFFF59E0B),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Template Studio", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 0) Color(0xFFF59E0B) else Color(0xFFCBD5E1)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            selectedContentColor = Color(0xFFF59E0B),
            unselectedContentColor = Color(0xFFCBD5E1),
            text = { Text("Community Templates (${templates.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, color = if (selectedTab == 1) Color(0xFFF59E0B) else Color(0xFFCBD5E1)) }
          )
        }
      }
    }

    // 2. Tab Content
    if (selectedTab == 0) {
      // --- TAB 0: TEMPLATE CREATOR STUDIO ---
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Section: Live Interactive Preview Sandbox
        item {
          Text(
            text = "LIVE INTERACTIVE PREVIEW (TAP ANY ICON)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF38BDF8)
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Interactive Sandbox Card based on selected options
          CreatorCardSandboxPreview(
            title = templateTitle,
            themeStyle = selectedThemeStyle,
            professionCode = targetProfession,
            hasPhotoSlot = hasPhotoSlot,
            hasPhoneIcon = hasPhoneIcon,
            hasWhatsAppIcon = hasWhatsAppIcon,
            hasWebsiteIcon = hasWebsiteIcon,
            hasEmailIcon = hasEmailIcon,
            hasLinkedInIcon = hasLinkedInIcon,
            hasFacebookIcon = hasFacebookIcon,
            hasInstagramIcon = hasInstagramIcon,
            hasBusyCardNetLink = hasBusyCardNetLink,
            onIconTap = { name ->
              Toast.makeText(context, "Clicked Icon: $name (Interactive)", Toast.LENGTH_SHORT).show()
            }
          )
        }

        // Section: Template Identity & Trade
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Template Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = templateTitle,
                onValueChange = { templateTitle = it },
                label = { Text("Template Name") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("creator_template_name_input"),
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = Color(0xFF1E293B),
                  unfocusedContainerColor = Color(0xFF1E293B),
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = "Target Profession / Trade:",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
              Spacer(modifier = Modifier.height(6.dp))

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                val trades = listOf(
                  "ELEC" to "⚡ Electrician",
                  "PLUM" to "🔧 Plumber",
                  "DOCT" to "👨‍⚕️ Doctor",
                  "LAWY" to "⚖️ Lawyer",
                  "SHOP" to "🏪 Retail Shop",
                  "GENE" to "🌐 All Trades"
                )
                trades.forEach { (code, label) ->
                  val selected = targetProfession == code
                  FilterChip(
                    selected = selected,
                    onClick = { targetProfession = code },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = Color(0xFF0284C7),
                      selectedLabelColor = Color.White
                    )
                  )
                }
              }
            }
          }
        }

        // Section: Color Theme Style
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Card Visual Theme",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(8.dp))

              val themes = listOf(
                "ELECTRIC_AMBER" to ("Amber Glow" to Color(0xFFF59E0B)),
                "LUXURY_GOLD" to ("Luxury Gold" to Color(0xFFEAB308)),
                "CREATIVE_CYAN" to ("Cyan Tech" to Color(0xFF06B6D4)),
                "MINIMAL_LIGHT" to ("Clean Modern" to Color(0xFF38BDF8))
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                themes.forEach { (themeKey, pair) ->
                  val isSelected = selectedThemeStyle == themeKey
                  Surface(
                    modifier = Modifier
                      .weight(1f)
                      .height(50.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { selectedThemeStyle = themeKey }
                      .border(
                        2.dp,
                        if (isSelected) Color.White else Color.Transparent,
                        RoundedCornerShape(8.dp)
                      ),
                    color = pair.second.copy(alpha = 0.25f)
                  ) {
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Box(
                        modifier = Modifier
                          .size(14.dp)
                          .clip(CircleShape)
                          .background(pair.second)
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = pair.first,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Section: Mandatory Icon Touchpoint Controls (Prompt explicit requirement)
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Interactive Icon Configuration",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Toggle the clickable touchpoint icons built into this template",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
              Spacer(modifier = Modifier.height(10.dp))

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                IconToggleChip(label = "🌐 Website Icon", active = hasWebsiteIcon, onToggle = { hasWebsiteIcon = !hasWebsiteIcon })
                IconToggleChip(label = "✉️ Email ID Icon", active = hasEmailIcon, onToggle = { hasEmailIcon = !hasEmailIcon })
                IconToggleChip(label = "📞 Contact Number", active = hasPhoneIcon, onToggle = { hasPhoneIcon = !hasPhoneIcon })
                IconToggleChip(label = "📷 Photo Avatar", active = hasPhotoSlot, onToggle = { hasPhotoSlot = !hasPhotoSlot })
                IconToggleChip(label = "🆔 BusyCardNet Link", active = hasBusyCardNetLink, onToggle = { hasBusyCardNetLink = !hasBusyCardNetLink })
                IconToggleChip(label = "💼 LinkedIn Icon", active = hasLinkedInIcon, onToggle = { hasLinkedInIcon = !hasLinkedInIcon })
                IconToggleChip(label = "📘 Facebook Icon", active = hasFacebookIcon, onToggle = { hasFacebookIcon = !hasFacebookIcon })
                IconToggleChip(label = "💬 WhatsApp Icon", active = hasWhatsAppIcon, onToggle = { hasWhatsAppIcon = !hasWhatsAppIcon })
                IconToggleChip(label = "📷 Instagram Icon", active = hasInstagramIcon, onToggle = { hasInstagramIcon = !hasInstagramIcon })
              }
            }
          }
        }

        // Section: Pricing & Monetization
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Template Pricing (Royalty Model)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(8.dp))

              val prices = listOf(0.0 to "Free", 49.0 to "₹49", 99.0 to "₹99", 199.0 to "₹199")
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                prices.forEach { (amt, label) ->
                  val isSelected = templatePrice == amt
                  Surface(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { templatePrice = amt }
                      .border(
                        1.dp,
                        if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155),
                        RoundedCornerShape(8.dp)
                      ),
                    color = if (isSelected) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF1E293B)
                  ) {
                    Box(
                      modifier = Modifier.padding(vertical = 10.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFFF59E0B) else Color.White
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Section: Publish Button
        item {
          Button(
            onClick = {
              viewModel.publishCreatorTemplate(
                title = templateTitle.ifEmpty { "Community Master Template" },
                professionCode = targetProfession,
                price = templatePrice,
                previewStyle = selectedThemeStyle
              )
              publishSuccessDialog = true
              Toast.makeText(context, "🎉 Template '$templateTitle' Published Successfully!", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("publish_creator_template_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Publish Template to BizCardNet",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )
          }
        }
      }
    } else {
      // --- TAB 1: COMMUNITY TEMPLATES GALLERY ---
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(templates) { tmpl ->
          CommunityTemplateCardItem(
            template = tmpl,
            onApply = {
              Toast.makeText(context, "Applied '${tmpl.title}' to your primary card!", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }
  }
}

@Composable
fun IconToggleChip(
  label: String,
  active: Boolean,
  onToggle: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (active) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFF1E293B),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (active) Color(0xFF38BDF8) else Color(0xFF334155)
    ),
    modifier = Modifier.clickable { onToggle() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (active) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = Color(0xFF38BDF8),
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        color = if (active) Color.White else Color(0xFF94A3B8)
      )
    }
  }
}

@Composable
fun CreatorCardSandboxPreview(
  title: String,
  themeStyle: String,
  professionCode: String,
  hasPhotoSlot: Boolean,
  hasPhoneIcon: Boolean,
  hasWhatsAppIcon: Boolean,
  hasWebsiteIcon: Boolean,
  hasEmailIcon: Boolean,
  hasLinkedInIcon: Boolean,
  hasFacebookIcon: Boolean,
  hasInstagramIcon: Boolean,
  hasBusyCardNetLink: Boolean,
  onIconTap: (String) -> Unit
) {
  val (gradient, accent) = when (themeStyle) {
    "ELECTRIC_AMBER" -> listOf(Color(0xFF191206), Color(0xFF261B07)) to Color(0xFFF59E0B)
    "LUXURY_GOLD" -> listOf(Color(0xFF17140B), Color(0xFF2B240B)) to Color(0xFFEAB308)
    "CREATIVE_CYAN" -> listOf(Color(0xFF061E26), Color(0xFF082F3D)) to Color(0xFF06B6D4)
    else -> listOf(Color(0xFF0B132B), Color(0xFF1C2541)) to Color(0xFF38BDF8)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("creator_sandbox_card"),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, accent.copy(alpha = 0.8f))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.linearGradient(gradient))
        .padding(14.dp)
    ) {
      Column {
        // Top row: Avatar + Name + Verified Badge
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (hasPhotoSlot) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.2f))
                .border(1.5.dp, accent, CircleShape)
                .clickable { onIconTap("Photo Slot") },
              contentAlignment = Alignment.Center
            ) {
              Text("⚡", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = "Trade: $professionCode • Certified BCN Holder",
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.7f)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Icons Row (Every single icon clickable!)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (hasPhoneIcon) {
            InteractiveTouchIcon("📞", "Call", accent, Color.White, { onIconTap("Call") })
          }
          if (hasWhatsAppIcon) {
            InteractiveTouchIcon("💬", "WhatsApp", Color(0xFF25D366), Color.White, { onIconTap("WhatsApp") })
          }
          if (hasWebsiteIcon) {
            InteractiveTouchIcon("🌐", "Website", Color(0xFF38BDF8), Color.White, { onIconTap("Website") })
          }
          if (hasEmailIcon) {
            InteractiveTouchIcon("✉️", "Email", Color(0xFFEF4444), Color.White, { onIconTap("Email") })
          }
          if (hasLinkedInIcon) {
            InteractiveTouchIcon("💼", "LinkedIn", Color(0xFF0A66C2), Color.White, { onIconTap("LinkedIn") })
          }
          if (hasFacebookIcon) {
            InteractiveTouchIcon("📘", "Facebook", Color(0xFF1877F2), Color.White, { onIconTap("Facebook") })
          }
          if (hasInstagramIcon) {
            InteractiveTouchIcon("📷", "Insta", Color(0xFFE1306C), Color.White, { onIconTap("Instagram") })
          }
          if (hasBusyCardNetLink) {
            InteractiveTouchIcon("🆔", "BCN", accent, Color.White, { onIconTap("BCN Link") })
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom-Right: BusyCardNet ID Display (Mandatory Section 8 position)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "🤝 BRIDGE READY",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981)
          )

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = accent.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.6f))
          ) {
            BcnCardHyperlink(
              bcnId = "IN-MH-MU-26-581ELEC347",
              color = accent,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun CommunityTemplateCardItem(
  template: CardTemplateEntity,
  onApply: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = template.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "By ${template.creatorName} • ${template.professionCode}",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
          )
          BcnCardHyperlink(
            bcnId = template.creatorBusyCardNetId,
            color = Color(0xFF38BDF8),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFF59E0B).copy(alpha = 0.15f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
        ) {
          Text(
            text = if (template.price == 0.0) "FREE" else "₹${template.price.toInt()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFF59E0B),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Supported Icons badges
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Active Icons:", fontSize = 10.sp, color = Color(0xFF64748B))
        Text("🌐 ✉️ 📞 📷 🆔 💼 📘 💬", fontSize = 12.sp)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(text = "${template.rating} (${template.downloadsCount} uses)", fontSize = 11.sp, color = Color(0xFFCBD5E1))
        }

        Button(
          onClick = onApply,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text("Apply Template", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
