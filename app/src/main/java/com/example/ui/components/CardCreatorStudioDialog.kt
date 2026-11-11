package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DigitalCardEntity

/**
 * Card Creator & AI Card Creator Studio (Prompt Sections 10-11)
 */
@Composable
fun CardCreatorStudioDialog(
  existingCard: DigitalCardEntity? = null,
  capturedPhotoUri: String? = null,
  onDismiss: () -> Unit,
  onCaptureProfilePhoto: (() -> Unit)? = null,
  onSaveCard: (
    name: String,
    designation: String,
    professionCode: String,
    businessName: String,
    services: String,
    phone: String,
    whatsapp: String,
    email: String,
    website: String,
    address: String,
    workingHours: String,
    about: String,
    templateStyle: String,
    accentColorHex: String
  ) -> Unit
) {
  var name by remember { mutableStateOf(existingCard?.name ?: "Rajesh Sharma") }
  var designation by remember { mutableStateOf(existingCard?.designation ?: "Master Electrician & Contractor") }
  var professionCode by remember { mutableStateOf(existingCard?.professionCode ?: "ELEC") }
  var businessName by remember { mutableStateOf(existingCard?.businessName ?: "Raj Electrical Services") }
  var services by remember { mutableStateOf(existingCard?.services ?: "Home Wiring, Inverter Setup, Emergency Repairs") }
  var phone by remember { mutableStateOf(existingCard?.phone ?: "+91 98765 43210") }
  var whatsapp by remember { mutableStateOf(existingCard?.whatsapp ?: "+91 98765 43210") }
  var email by remember { mutableStateOf(existingCard?.email ?: "rajesh@busycardnet.com") }
  var website by remember { mutableStateOf(existingCard?.website ?: "https://rajelectricals.in") }
  var address by remember { mutableStateOf(existingCard?.address ?: "Kurla West, Mumbai") }
  var workingHours by remember { mutableStateOf(existingCard?.workingHours ?: "8:00 AM - 9:00 PM") }
  var about by remember { mutableStateOf(existingCard?.about ?: "Licensed electrical contractor serving local community.") }
  var templateStyle by remember { mutableStateOf(existingCard?.templateStyle ?: "ELECTRIC_AMBER") }
  var photoUrl by remember(capturedPhotoUri, existingCard?.photoUrl) { mutableStateOf(capturedPhotoUri ?: existingCard?.photoUrl ?: "") }
  var aiPrompt by remember { mutableStateOf("") }
  var isAiGenerating by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("card_creator_studio_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp)
          .verticalScroll(scrollState)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (existingCard != null) "Edit Digital Card" else "Card Creator Studio",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Card Creator Box (Prompt Section 11)
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "AI Card Creator",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Text(
              text = "Describe your business (e.g. 'Create a premium card for electrical contracting'):",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = aiPrompt,
                onValueChange = { aiPrompt = it },
                placeholder = { Text("Enter prompt...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
              )
              Spacer(modifier = Modifier.width(6.dp))
              Button(
                onClick = {
                  if (aiPrompt.contains("law", ignoreCase = true)) {
                    professionCode = "LAWY"
                    designation = "Senior Advocate & Legal Counsel"
                    businessName = "Apex Chambers & Associates"
                    services = "Civil Litigation, Property Verification, Agreements"
                    templateStyle = "CORPORATE"
                  } else if (aiPrompt.contains("doctor", ignoreCase = true) || aiPrompt.contains("clinic", ignoreCase = true)) {
                    professionCode = "DOCT"
                    designation = "Consultant Physician"
                    businessName = "City Health Clinic"
                    services = "General Consultation, Diagnostics, Family Health"
                    templateStyle = "MINIMAL_LIGHT"
                  } else if (aiPrompt.contains("rest", ignoreCase = true) || aiPrompt.contains("food", ignoreCase = true)) {
                    professionCode = "REST"
                    designation = "Founder & Head Chef"
                    businessName = "Spice Route Restaurant"
                    services = "Dine-in, Party Catering, Home Delivery"
                    templateStyle = "LUXURY_GOLD"
                  } else {
                    professionCode = "ELEC"
                    designation = "Certified Electrical Contractor"
                    businessName = "Apex Electricals"
                    services = "Wiring, 24/7 Emergency Repairs, Smart Home"
                    templateStyle = "ELECTRIC_AMBER"
                  }
                  aiPrompt = ""
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
              ) {
                Text("Generate", fontSize = 11.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Template Theme Selection
        Text(text = "Card Visual Theme:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "ELECTRIC_AMBER" to "⚡ Amber",
            "MODERN_DARK" to "🌌 Obsidian",
            "LUXURY_GOLD" to "👑 Gold",
            "CREATIVE_CYAN" to "💎 Cyan",
            "MINIMAL_LIGHT" to "⚪ Minimal"
          ).forEach { (style, label) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (templateStyle == style) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { templateStyle = style }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 7.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (templateStyle == style) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Photo & Live Camera Capture (Prompt: enable profile photo capturing and media sharing features)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
              .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            if (photoUrl.isNotBlank()) {
              Text("📸", fontSize = 22.sp)
            } else {
              Text(
                text = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Profile Photo",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (photoUrl.isNotBlank()) "Photo attached" else "Capture real photo with camera",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (onCaptureProfilePhoto != null) {
            Button(
              onClick = onCaptureProfilePhoto,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Form Fields
        Text(text = "Full Name:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Designation / Title:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
          value = designation,
          onValueChange = { designation = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Business / Practice Name:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
          value = businessName,
          onValueChange = { businessName = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Services Offered (comma separated):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
          value = services,
          onValueChange = { services = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Phone:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              singleLine = true
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "WhatsApp:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
              value = whatsapp,
              onValueChange = { whatsapp = it },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              singleLine = true
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Address / Locality:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Working Hours:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
          value = workingHours,
          onValueChange = { workingHours = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            onSaveCard(
              name,
              designation,
              professionCode,
              businessName,
              services,
              phone,
              whatsapp,
              email,
              website,
              address,
              workingHours,
              about,
              templateStyle,
              if (templateStyle == "ELECTRIC_AMBER") "#F59E0B" else "#2563EB"
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("save_digital_card_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(text = if (existingCard != null) "SAVE CARD CHANGES" else "CREATE & PUBLISH CARD", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
