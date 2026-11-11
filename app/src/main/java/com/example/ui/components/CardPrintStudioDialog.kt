package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.DigitalCardEntity
import com.example.data.model.CardCornerStyle
import com.example.data.model.CardDeliveryAddress
import com.example.data.model.CardManufacturer
import com.example.data.model.CardPrintOptions
import com.example.data.model.CardPrintOrder
import com.example.data.model.CardPrintTheme
import com.example.data.model.CardQualityOption
import com.example.util.CardPrintHelper
import com.example.util.VisitingCardAiGenerator
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * AI Visiting Card Print Studio Dialog.
 * Allows user to:
 * 1. Automatically generate visiting card via AI (slogans, themes, layout).
 * 2. Select exactly what elements appear on the card (Photo, Name, BCN ID, Barcode,
 *    Company Name, Email, Website, Facebook, Instagram, WhatsApp, Company Words, etc.)
 *    and customize their content.
 * 3. Choose certified Card Manufacturer & Card Quality (Paper weight, GSM, finish, corners).
 * 4. Configure delivery address for physical card dispatch or print directly via Android Print.
 */
@Composable
fun CardPrintStudioDialog(
  card: DigitalCardEntity,
  userCity: String = "Mumbai",
  userLocality: String = "Andheri East",
  onDismiss: () -> Unit,
  onOrderPlaced: (CardPrintOrder) -> Unit = {}
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Studio Step Tabs
  var currentStep by remember { mutableIntStateOf(0) } // 0: AI & Elements, 1: Manufacturer & Quality, 2: Delivery Address, 3: Review & Print

  // Card Print Options State
  var options by remember { mutableStateOf(CardPrintOptions.fromCard(card)) }

  // AI Prompt & Suggestion State
  var aiPrompt by remember { mutableStateOf("") }
  var isAiGenerating by remember { mutableStateOf(false) }
  var aiSlogansList by remember { mutableStateOf<List<String>>(emptyList()) }
  var aiStatusNote by remember { mutableStateOf("AI analyzed business profile: Electrical & Tech Services") }

  // Manufacturers List
  val manufacturers = remember {
    listOf(
      CardManufacturer(
        id = "mfg-metro",
        name = "Metro Print & Card Works",
        rating = 4.8f,
        deliveryDays = 3,
        location = "Lower Parel, Mumbai",
        basePrice100 = 250.0,
        description = "Official BCN Certified Partner with high-definition litho press.",
        isOfficialBcn = true,
        specialty = "350-450 GSM Offset Press"
      ),
      CardManufacturer(
        id = "mfg-smarttap",
        name = "SmartTap NFC Technologies",
        rating = 4.9f,
        deliveryDays = 2,
        location = "Andheri East, Mumbai",
        basePrice100 = 499.0,
        description = "Specialists in Smart Contactless NFC Cards and rigid PVC.",
        isOfficialBcn = true,
        specialty = "NFC Tap & Hybrid PVC"
      ),
      CardManufacturer(
        id = "mfg-express",
        name = "Express Visiting Card Factory",
        rating = 4.6f,
        deliveryDays = 2,
        location = "Dadar, Mumbai",
        basePrice100 = 190.0,
        description = "Fast turnaround 48-hour dispatch for standard corporate batches.",
        specialty = "Gloss UV & Matte Express"
      ),
      CardManufacturer(
        id = "mfg-vistaprint",
        name = "VistaPrint Pro Print Network",
        rating = 4.9f,
        deliveryDays = 3,
        location = "Bandra Kurla Complex, Mumbai",
        basePrice100 = 320.0,
        description = "Global standard premium card stock with anti-scratch coating.",
        specialty = "Soft-Touch Velvet & Foils"
      )
    )
  }
  var selectedManufacturer by remember { mutableStateOf(manufacturers.first()) }

  // Card Quality & Finish Options
  val qualityOptions = remember {
    listOf(
      CardQualityOption(
        id = "qual-matte-350",
        name = "Standard Matte",
        paperWeight = "350 GSM",
        finishDescription = "Silky smooth non-glare finish, classic business elegance.",
        priceMultiplier = 1.0,
        badgeText = "POPULAR"
      ),
      CardQualityOption(
        id = "qual-gloss-400",
        name = "Gloss UV Coated",
        paperWeight = "400 GSM",
        finishDescription = "Ultra-vibrant glossy UV coat with high photo contrast.",
        priceMultiplier = 1.2
      ),
      CardQualityOption(
        id = "qual-velvet-450",
        name = "Velvet Soft-Touch",
        paperWeight = "450 GSM",
        finishDescription = "Suede-like rubberized luxury texture. Fingerprint resistant.",
        priceMultiplier = 1.5,
        badgeText = "PREMIUM"
      ),
      CardQualityOption(
        id = "qual-linen-400",
        name = "Textured Cotton Linen",
        paperWeight = "400 GSM",
        finishDescription = "Woven textile grid pattern, eco-friendly luxury cotton.",
        priceMultiplier = 1.6
      ),
      CardQualityOption(
        id = "qual-pvc-500",
        name = "Waterproof Smart PVC",
        paperWeight = "500 GSM",
        finishDescription = "Rigid credit-card polymer, 100% waterproof and tear-proof.",
        priceMultiplier = 2.0
      ),
      CardQualityOption(
        id = "qual-nfc-tap",
        name = "Contactless Smart NFC",
        paperWeight = "Hybrid Card",
        finishDescription = "Embedded NTAG216 chip. Tap on any smartphone to open card.",
        priceMultiplier = 3.2,
        badgeText = "NEXT-GEN"
      )
    )
  }
  var selectedQuality by remember { mutableStateOf(qualityOptions.first()) }

  // Pack Quantity
  val quantityPacks = remember { listOf(100, 250, 500, 1000) }
  var selectedQuantity by remember { mutableIntStateOf(100) }

  // Delivery Address Form State
  var recipientName by remember { mutableStateOf(card.name) }
  var deliveryPhone by remember { mutableStateOf(card.phone) }
  var streetAddress by remember { mutableStateOf(if (card.address.isNotBlank()) card.address else "Flat 402, Sai Heritage, MG Road") }
  var locality by remember { mutableStateOf(userLocality) }
  var city by remember { mutableStateOf(userCity) }
  var state by remember { mutableStateOf("Maharashtra") }
  var pinCode by remember { mutableStateOf("400069") }
  var deliveryNotes by remember { mutableStateOf("Leave with security desk if unavailable.") }
  var isExpressDelivery by remember { mutableStateOf(false) }

  // Order Confirmation State
  var confirmedOrder by remember { mutableStateOf<CardPrintOrder?>(null) }

  // Calculate pricing dynamically
  val bulkDiscountFactor = when (selectedQuantity) {
    100 -> 1.0
    250 -> 0.85
    500 -> 0.75
    1000 -> 0.60
    else -> 1.0
  }
  val quantityRatio = selectedQuantity / 100.0
  val calculatedBasePrice = (selectedManufacturer.basePrice100 * selectedQuality.priceMultiplier * quantityRatio * bulkDiscountFactor).toInt().toDouble()
  val shippingFee = if (isExpressDelivery) 99.0 else 0.0
  val calculatedTotal = calculatedBasePrice + shippingFee

  // Initial AI Generation on Mount
  LaunchedEffect(card.cardId) {
    coroutineScope.launch {
      isAiGenerating = true
      val suggestion = VisitingCardAiGenerator.generateCardDesign(
        businessName = card.businessName,
        ownerName = card.name,
        professionOrCategory = card.professionCode
      )
      aiSlogansList = suggestion.alternativeSlogans
      options = VisitingCardAiGenerator.applySuggestion(options, suggestion)
      aiStatusNote = suggestion.designSummary
      isAiGenerating = false
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 28.dp)
        .testTag("card_print_studio_dialog"),
      color = MaterialTheme.colorScheme.background,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {

        // ================= STUDIO HEADER =================
        Surface(
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 4.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Print,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "AI VISITING CARD PRINT STUDIO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = "AI SMART",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFF059669),
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = "Custom Elements • Certified Manufacturers • Address Delivery",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier.testTag("close_print_studio_button")
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
          }
        }

        // ================= STEP NAVIGATION TABS =================
        TabRow(
          selectedTabIndex = currentStep,
          containerColor = Color(0xFFF1F5F9),
          contentColor = Color(0xFF1E40AF)
        ) {
          Tab(
            selected = currentStep == 0,
            onClick = { currentStep = 0 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("1. AI & Content", fontSize = 11.sp, fontWeight = if (currentStep == 0) FontWeight.Bold else FontWeight.SemiBold, color = if (currentStep == 0) Color(0xFF1E40AF) else Color(0xFF475569)) },
            modifier = Modifier.testTag("step_tab_elements")
          )
          Tab(
            selected = currentStep == 1,
            onClick = { currentStep = 1 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("2. Quality & Maker", fontSize = 11.sp, fontWeight = if (currentStep == 1) FontWeight.Bold else FontWeight.SemiBold, color = if (currentStep == 1) Color(0xFF1E40AF) else Color(0xFF475569)) },
            modifier = Modifier.testTag("step_tab_manufacturer")
          )
          Tab(
            selected = currentStep == 2,
            onClick = { currentStep = 2 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("3. Delivery", fontSize = 11.sp, fontWeight = if (currentStep == 2) FontWeight.Bold else FontWeight.SemiBold, color = if (currentStep == 2) Color(0xFF1E40AF) else Color(0xFF475569)) },
            modifier = Modifier.testTag("step_tab_delivery")
          )
          Tab(
            selected = currentStep == 3,
            onClick = { currentStep = 3 },
            selectedContentColor = Color(0xFF1E40AF),
            unselectedContentColor = Color(0xFF475569),
            text = { Text("4. Print / Order", fontSize = 11.sp, fontWeight = if (currentStep == 3) FontWeight.Bold else FontWeight.SemiBold, color = if (currentStep == 3) Color(0xFF1E40AF) else Color(0xFF475569)) },
            modifier = Modifier.testTag("step_tab_print")
          )
        }

        // ================= SCROLLABLE CONTENT =================
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

          // ================= PERSISTENT LIVE PREVIEW =================
          item {
            VisitingCardPrintPreview(
              options = options,
              modifier = Modifier.fillMaxWidth()
            )
          }

          // ================= STEP 0: AI DESIGN & ELEMENT CUSTOMIZATION =================
          if (currentStep == 0) {
            // AI Generative Assistance Banner
            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = "AI Auto-Generate Card & Company Words",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }

                    if (isAiGenerating) {
                      Text(
                        text = "Synthesizing...",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  // AI Prompt Input
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    OutlinedTextField(
                      value = aiPrompt,
                      onValueChange = { aiPrompt = it },
                      placeholder = { Text("e.g., Luxury gold tone with memorable slogan", fontSize = 11.sp) },
                      singleLine = true,
                      modifier = Modifier
                        .weight(1f)
                        .testTag("ai_card_prompt_input"),
                      shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                      onClick = {
                        coroutineScope.launch {
                          isAiGenerating = true
                          val suggestion = VisitingCardAiGenerator.generateCardDesign(
                            businessName = options.companyName,
                            ownerName = options.name,
                            professionOrCategory = card.professionCode,
                            customPrompt = aiPrompt
                          )
                          aiSlogansList = suggestion.alternativeSlogans
                          options = VisitingCardAiGenerator.applySuggestion(options, suggestion)
                          aiStatusNote = suggestion.designSummary
                          isAiGenerating = false
                          Toast.makeText(context, "AI updated visiting card design!", Toast.LENGTH_SHORT).show()
                        }
                      },
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.testTag("ai_regenerate_button")
                    ) {
                      Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Generate", fontSize = 11.sp)
                    }
                  }

                  // AI Slogan Chips
                  if (aiSlogansList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                      text = "Tap to pick AI Company Words / Tagline:",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      items(aiSlogansList) { slogan ->
                        val isCurrent = options.companyWords == slogan
                        Surface(
                          shape = RoundedCornerShape(12.dp),
                          color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                          border = BorderStroke(1.dp, if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                          modifier = Modifier.clickable {
                            options = options.copy(companyWords = slogan, includeCompanyWords = true)
                          }
                        ) {
                          Text(
                            text = slogan.take(45) + "...",
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            // Theme Selection
            item {
              Column {
                Text(
                  text = "CARD FINISH & THEME PALETTE",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  items(CardPrintTheme.values()) { theme ->
                    val isSelected = options.theme == theme
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = Color(android.graphics.Color.parseColor(theme.primaryBgHex)),
                      border = BorderStroke(
                        if (isSelected) 2.5.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                      ),
                      modifier = Modifier
                        .clickable { options = options.copy(theme = theme) }
                        .testTag("theme_chip_${theme.name}")
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(theme.accentColorHex)))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = theme.displayName,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(android.graphics.Color.parseColor(theme.textColorHex))
                        )
                      }
                    }
                  }
                }
              }
            }

            // Element Selection & Customization List
            item {
              Text(
                text = "SELECT WHAT USER WANTS ON THE VISITING CARD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Check the options you want printed and edit the content directly below.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // 1. Photo
            item {
              ElementOptionCard(
                title = "Photo / Brand Logo",
                isChecked = options.includePhoto,
                onCheckedChange = { options = options.copy(includePhoto = it) },
                value = options.photoUrl,
                onValueChange = { options = options.copy(photoUrl = it) },
                placeholder = "https://... (Image or logo URL)",
                testTagPrefix = "opt_photo"
              )
            }

            // 2. Name
            item {
              ElementOptionCard(
                title = "Cardholder Name",
                isChecked = options.includeName,
                onCheckedChange = { options = options.copy(includeName = it) },
                value = options.name,
                onValueChange = { options = options.copy(name = it) },
                placeholder = "Full Name",
                testTagPrefix = "opt_name"
              )
            }

            // 3. BCN ID
            item {
              ElementOptionCard(
                title = "BusyCardNet ID (BCN ID)",
                isChecked = options.includeBcnId,
                onCheckedChange = { options = options.copy(includeBcnId = it) },
                value = options.bcnId,
                onValueChange = { options = options.copy(bcnId = it) },
                placeholder = "IN-MH-MU-26-XXXX",
                testTagPrefix = "opt_bcn_id"
              )
            }

            // 4. Barcode / QR Code
            item {
              ElementOptionCard(
                title = "Barcode / QR Code",
                isChecked = options.includeBarcode,
                onCheckedChange = { options = options.copy(includeBarcode = it) },
                value = options.barcodeData,
                onValueChange = { options = options.copy(barcodeData = it) },
                placeholder = "Barcode string or URL",
                testTagPrefix = "opt_barcode"
              )
            }

            // 5. Company Name
            item {
              ElementOptionCard(
                title = "Company Name",
                isChecked = options.includeCompanyName,
                onCheckedChange = { options = options.copy(includeCompanyName = it) },
                value = options.companyName,
                onValueChange = { options = options.copy(companyName = it) },
                placeholder = "Business / Trade Name",
                testTagPrefix = "opt_company_name"
              )
            }

            // 6. Company Words (Slogan / Tagline)
            item {
              ElementOptionCard(
                title = "Company Words / Slogan / Tagline",
                isChecked = options.includeCompanyWords,
                onCheckedChange = { options = options.copy(includeCompanyWords = it) },
                value = options.companyWords,
                onValueChange = { options = options.copy(companyWords = it) },
                placeholder = "Inspiring company motto or value proposition",
                singleLine = false,
                testTagPrefix = "opt_company_words"
              )
            }

            // 7. Email ID
            item {
              ElementOptionCard(
                title = "Email ID",
                isChecked = options.includeEmail,
                onCheckedChange = { options = options.copy(includeEmail = it) },
                value = options.email,
                onValueChange = { options = options.copy(email = it) },
                placeholder = "youremail@domain.com",
                testTagPrefix = "opt_email"
              )
            }

            // 8. Website
            item {
              ElementOptionCard(
                title = "Website",
                isChecked = options.includeWebsite,
                onCheckedChange = { options = options.copy(includeWebsite = it) },
                value = options.website,
                onValueChange = { options = options.copy(website = it) },
                placeholder = "www.yourwebsite.com",
                testTagPrefix = "opt_website"
              )
            }

            // 9. Facebook
            item {
              ElementOptionCard(
                title = "Facebook",
                isChecked = options.includeFacebook,
                onCheckedChange = { options = options.copy(includeFacebook = it) },
                value = options.facebook,
                onValueChange = { options = options.copy(facebook = it) },
                placeholder = "facebook.com/yourhandle",
                testTagPrefix = "opt_facebook"
              )
            }

            // 10. Instagram
            item {
              ElementOptionCard(
                title = "Instagram",
                isChecked = options.includeInstagram,
                onCheckedChange = { options = options.copy(includeInstagram = it) },
                value = options.instagram,
                onValueChange = { options = options.copy(instagram = it) },
                placeholder = "@yourhandle",
                testTagPrefix = "opt_instagram"
              )
            }

            // 11. WhatsApp Number
            item {
              ElementOptionCard(
                title = "WhatsApp Number",
                isChecked = options.includeWhatsApp,
                onCheckedChange = { options = options.copy(includeWhatsApp = it) },
                value = options.whatsApp,
                onValueChange = { options = options.copy(whatsApp = it) },
                placeholder = "+91 98200 XXXXX",
                testTagPrefix = "opt_whatsapp"
              )
            }

            // 12. Phone Number
            item {
              ElementOptionCard(
                title = "Phone Call Number",
                isChecked = options.includePhone,
                onCheckedChange = { options = options.copy(includePhone = it) },
                value = options.phone,
                onValueChange = { options = options.copy(phone = it) },
                placeholder = "+91 98200 XXXXX",
                testTagPrefix = "opt_phone"
              )
            }

            // 13. Physical Address
            item {
              ElementOptionCard(
                title = "Business Address / Location",
                isChecked = options.includeAddress,
                onCheckedChange = { options = options.copy(includeAddress = it) },
                value = options.address,
                onValueChange = { options = options.copy(address = it) },
                placeholder = "Shop 12, Main Market, Mumbai",
                singleLine = false,
                testTagPrefix = "opt_address"
              )
            }
          }

          // ================= STEP 1: CARD MANUFACTURER & QUALITY =================
          if (currentStep == 1) {
            // Manufacturer Selection
            item {
              Text(
                text = "SELECT CARD MANUFACTURER / PRINT LAB",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                manufacturers.forEach { mfg ->
                  val isSelected = selectedManufacturer.id == mfg.id
                  Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                      if (isSelected) 2.dp else 1.dp,
                      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { selectedManufacturer = mfg }
                      .testTag("mfg_card_${mfg.id}")
                  ) {
                    Row(
                      modifier = Modifier.padding(14.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .size(20.dp)
                          .clip(CircleShape)
                          .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                          ),
                        contentAlignment = Alignment.Center
                      ) {
                        if (isSelected) {
                          Box(
                            modifier = Modifier
                              .size(10.dp)
                              .clip(CircleShape)
                              .background(MaterialTheme.colorScheme.primary)
                          )
                        }
                      }

                      Spacer(modifier = Modifier.width(12.dp))

                      Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = mfg.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                          if (mfg.isOfficialBcn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                              shape = RoundedCornerShape(4.dp),
                              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                              Text(
                                text = "VERIFIED LAB",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                              )
                            }
                          }
                        }

                        Text(
                          text = mfg.description,
                          fontSize = 11.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                          horizontalArrangement = Arrangement.spacedBy(10.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "${mfg.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                          }
                          Text(text = "• ${mfg.deliveryDays} Days Delivery", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                          Text(text = "• ${mfg.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                      }

                      Text(
                        text = "₹${mfg.basePrice100.toInt()}/100",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            }

            // Card Quality & Paper Stock
            item {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "SELECT CARD QUALITY & FINISH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                qualityOptions.forEach { qual ->
                  val isSelected = selectedQuality.id == qual.id
                  Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                      if (isSelected) 2.dp else 1.dp,
                      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { selectedQuality = qual }
                      .testTag("quality_card_${qual.id}")
                  ) {
                    Row(
                      modifier = Modifier.padding(14.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .size(20.dp)
                          .clip(CircleShape)
                          .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                          ),
                        contentAlignment = Alignment.Center
                      ) {
                        if (isSelected) {
                          Box(
                            modifier = Modifier
                              .size(10.dp)
                              .clip(CircleShape)
                              .background(MaterialTheme.colorScheme.primary)
                          )
                        }
                      }

                      Spacer(modifier = Modifier.width(12.dp))

                      Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = qual.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                          ) {
                            Text(
                              text = qual.paperWeight,
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Bold,
                              color = MaterialTheme.colorScheme.onSecondaryContainer,
                              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                          }
                          if (qual.badgeText.isNotBlank()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                              shape = RoundedCornerShape(4.dp),
                              color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                              Text(
                                text = qual.badgeText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                              )
                            }
                          }
                        }

                        Text(
                          text = qual.finishDescription,
                          fontSize = 11.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }

                      Text(
                        text = "${qual.priceMultiplier}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            }

            // Corner Style
            item {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "CARD CORNER CUT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                CardCornerStyle.values().forEach { style ->
                  val isSelected = options.cornerStyle == style
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                      if (isSelected) 2.dp else 1.dp,
                      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                      .weight(1f)
                      .clickable { options = options.copy(cornerStyle = style) }
                      .testTag("corner_style_${style.name}")
                  ) {
                    Column(
                      modifier = Modifier.padding(10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(
                        text = style.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            }

            // Quantity Selection
            item {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "CARD QUANTITY PACK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                quantityPacks.forEach { qty ->
                  val isSelected = selectedQuantity == qty
                  val discountLabel = when (qty) {
                    250 -> "15% OFF"
                    500 -> "25% OFF"
                    1000 -> "40% OFF"
                    else -> "STD"
                  }
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                      if (isSelected) 2.dp else 1.dp,
                      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                      .weight(1f)
                      .clickable { selectedQuantity = qty }
                      .testTag("quantity_pack_$qty")
                  ) {
                    Column(
                      modifier = Modifier.padding(vertical = 10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(
                        text = "$qty",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = "Cards",
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = discountLabel,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            }
          }

          // ================= STEP 2: DELIVERY ADDRESS =================
          if (currentStep == 2) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "DELIVER TO ADDRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "Physical cards will be shipped directly to this destination.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                // Autofill button
                OutlinedButton(
                  onClick = {
                    recipientName = card.name
                    deliveryPhone = card.phone
                    streetAddress = if (card.address.isNotBlank()) card.address else "Flat 402, Sai Heritage, MG Road"
                    city = userCity
                    locality = userLocality
                    pinCode = "400069"
                    Toast.makeText(context, "Address autofilled from Profile!", Toast.LENGTH_SHORT).show()
                  },
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("autofill_address_button")
                ) {
                  Text("Autofill", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                  value = recipientName,
                  onValueChange = { recipientName = it },
                  label = { Text("Recipient Full Name") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("input_recipient_name"),
                  shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                  value = deliveryPhone,
                  onValueChange = { deliveryPhone = it },
                  label = { Text("Delivery Mobile Number") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("input_delivery_phone"),
                  shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                  value = streetAddress,
                  onValueChange = { streetAddress = it },
                  label = { Text("Flat / House / Building / Street") },
                  singleLine = false,
                  modifier = Modifier.fillMaxWidth().testTag("input_delivery_street"),
                  shape = RoundedCornerShape(10.dp)
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedTextField(
                    value = locality,
                    onValueChange = { locality = it },
                    label = { Text("Locality / Area") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_delivery_locality"),
                    shape = RoundedCornerShape(10.dp)
                  )
                  OutlinedTextField(
                    value = pinCode,
                    onValueChange = { pinCode = it },
                    label = { Text("PIN Code") },
                    singleLine = true,
                    modifier = Modifier.weight(0.8f).testTag("input_delivery_pincode"),
                    shape = RoundedCornerShape(10.dp)
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_delivery_city"),
                    shape = RoundedCornerShape(10.dp)
                  )
                  OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_delivery_state"),
                    shape = RoundedCornerShape(10.dp)
                  )
                }

                OutlinedTextField(
                  value = deliveryNotes,
                  onValueChange = { deliveryNotes = it },
                  label = { Text("Delivery Instructions / Landmark") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("input_delivery_notes"),
                  shape = RoundedCornerShape(10.dp)
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Shipping Speed Selection
              Text(
                text = "SHIPPING METHOD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(6.dp))

              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (!isExpressDelivery) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, if (!isExpressDelivery) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { isExpressDelivery = false }
                  .testTag("shipping_standard")
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Standard Shipping (${selectedManufacturer.deliveryDays + 1} Days)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Doorstep tracking via Blue Dart / DTDC", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  Text(text = "FREE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isExpressDelivery) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, if (isExpressDelivery) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { isExpressDelivery = true }
                  .testTag("shipping_express")
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = Color(0xFFF59E0B))
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Express Priority Courier (24-48 Hours)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Expedited dispatch direct from printing lab", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  Text(text = "₹99", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }

          // ================= STEP 3: REVIEW & PRINT OPTIONS =================
          if (currentStep == 3) {
            item {
              Text(
                text = "ORDER SUMMARY & PRINT OPTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))

              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Selected Manufacturer:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(selectedManufacturer.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Card Quality & Finish:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${selectedQuality.name} (${selectedQuality.paperWeight})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Quantity Pack:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$selectedQuantity Visiting Cards", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Corner Finishing:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(options.cornerStyle.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Shipping Destination:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$locality, $city", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  Divider(modifier = Modifier.padding(vertical = 4.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Printing Base Price:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${calculatedBasePrice.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Courier Delivery Fee:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (shippingFee == 0.0) "FREE" else "₹${shippingFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Estimated Delivery:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${if (isExpressDelivery) 2 else selectedManufacturer.deliveryDays} Business Days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                  }

                  Divider(modifier = Modifier.padding(vertical = 4.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Total Amount Payable:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("₹${calculatedTotal.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                  }
                }
              }
            }

            // The Two Available Print Options
            item {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "CHOOSE PRINT ACTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(10.dp))

              // Print Option 1: Order Physical Cards and Deliver to Address
              Button(
                onClick = {
                  val newOrderId = "BCN-PRNT-2026-${(1000..9999).random()}"
                  val deliveryAddr = CardDeliveryAddress(
                    recipientName = recipientName,
                    phone = deliveryPhone,
                    streetAddress = streetAddress,
                    locality = locality,
                    city = city,
                    state = state,
                    pinCode = pinCode,
                    deliveryNotes = deliveryNotes,
                    isExpressCourier = isExpressDelivery
                  )

                  val order = CardPrintOrder(
                    orderId = newOrderId,
                    cardId = card.cardId,
                    options = options,
                    manufacturer = selectedManufacturer,
                    quality = selectedQuality,
                    cornerStyle = options.cornerStyle,
                    quantity = selectedQuantity,
                    basePrice = calculatedBasePrice,
                    deliveryFee = shippingFee,
                    totalPrice = calculatedTotal,
                    deliveryAddress = deliveryAddr,
                    estimatedDeliveryDays = if (isExpressDelivery) 2 else selectedManufacturer.deliveryDays
                  )

                  confirmedOrder = order
                  onOrderPlaced(order)
                  Toast.makeText(context, "Visiting card order placed for $recipientName!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("submit_print_order_delivery_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Order & Deliver to Address (₹${calculatedTotal.toInt()})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Print Option 2: Print Now via Android PrintManager / Save as PDF
              OutlinedButton(
                onClick = {
                  CardPrintHelper.printVisitingCard(
                    context = context,
                    options = options,
                    onFinished = {
                      Toast.makeText(context, "Sent to Android Print Spooler!", Toast.LENGTH_SHORT).show()
                    }
                  )
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("direct_android_print_button"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print Directly (WiFi/Bluetooth Printer or Save PDF)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }

        // ================= BOTTOM STEP BAR =================
        Surface(
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (currentStep > 0) {
              OutlinedButton(
                onClick = { currentStep-- },
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Back", fontSize = 12.sp)
              }
            } else {
              Spacer(modifier = Modifier.width(1.dp))
            }

            if (currentStep < 3) {
              Button(
                onClick = { currentStep++ },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("wizard_next_button")
              ) {
                Text(
                  text = when (currentStep) {
                    0 -> "Next: Maker & Quality"
                    1 -> "Next: Delivery Address"
                    else -> "Next: Review & Print"
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }

  // ================= ORDER CONFIRMATION CELEBRATION MODAL =================
  if (confirmedOrder != null) {
    val order = confirmedOrder!!
    Dialog(
      onDismissRequest = {
        confirmedOrder = null
        onDismiss()
      }
    ) {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
        modifier = Modifier.padding(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color(0xFF10B981).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Visiting Card Order Placed!",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Order ID: ${order.orderId}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "• Pack: ${order.quantity} x ${order.quality.name} (${order.quality.paperWeight})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "• Manufacturer: ${order.manufacturer.name}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "• Shipping to: ${order.deliveryAddress.fullAddressLine}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "• Delivery Estimate: ${order.estimatedDeliveryDays} Business Days",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF059669)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              confirmedOrder = null
              onDismiss()
            },
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

/**
 * Reusable Element Selection & Editing Item.
 * Includes a checkbox to include/exclude the item on the visiting card,
 * and an inline editable text field to customize content.
 */
@Composable
private fun ElementOptionCard(
  title: String,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  singleLine: Boolean = true,
  testTagPrefix: String
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isChecked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ),
    border = BorderStroke(
      1.dp,
      if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = isChecked,
          onCheckedChange = onCheckedChange,
          colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("${testTagPrefix}_checkbox")
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = title,
          fontSize = 12.sp,
          fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
          color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      AnimatedVisibility(visible = isChecked) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 6.dp)) {
          OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 11.sp) },
            singleLine = singleLine,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("${testTagPrefix}_input"),
            shape = RoundedCornerShape(8.dp)
          )
        }
      }
    }
  }
}
