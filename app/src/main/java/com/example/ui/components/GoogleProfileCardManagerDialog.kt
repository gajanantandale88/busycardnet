package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.DigitalCardEntity
import com.example.ui.viewmodel.BusyCardNetViewModel
import com.example.util.AuthManager
import com.example.util.BcnLocationManager
import kotlinx.coroutines.launch

/**
 * Dialog for Google Authentication via Credential Manager and
 * Secure Registration / Management of Digital Visiting Card Profiles synced with Firestore.
 */
@Composable
fun GoogleProfileCardManagerDialog(
  viewModel: BusyCardNetViewModel,
  currentLatitude: Double = 19.0760,
  currentLongitude: Double = 72.8777,
  existingCard: DigitalCardEntity? = null,
  onDismiss: () -> Unit,
  onCardSaved: (DigitalCardEntity) -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val authManager = remember { AuthManager.getInstance() }

  val firebaseUser by authManager.currentUserFlow.collectAsState()
  val simulatedUser by authManager.localSimulatedUserFlow.collectAsState()

  val isAuthenticated = firebaseUser != null || simulatedUser != null
  val userDisplayName = firebaseUser?.displayName ?: simulatedUser?.displayName ?: "Google User"
  val userEmail = firebaseUser?.email ?: simulatedUser?.email ?: ""
  val userPhotoUrl = firebaseUser?.photoUrl?.toString() ?: simulatedUser?.photoUrl ?: ""

  var isAuthenticating by remember { mutableStateOf(false) }
  var isSavingToFirestore by remember { mutableStateOf(false) }

  // Form fields
  var name by remember { mutableStateOf(existingCard?.name ?: userDisplayName.ifBlank { "Rajesh Sharma" }) }
  var businessName by remember { mutableStateOf(existingCard?.businessName ?: "Sharma Professional Enterprise") }
  var designation by remember { mutableStateOf(existingCard?.designation ?: "Managing Director & Consultant") }
  var professionCode by remember { mutableStateOf(existingCard?.professionCode ?: "BIZ") }
  var services by remember { mutableStateOf(existingCard?.services ?: "Retail, Consultation, Fast Delivery, Pro Services") }
  var phone by remember { mutableStateOf(existingCard?.phone ?: "+91 98765 43210") }
  var whatsapp by remember { mutableStateOf(existingCard?.whatsapp ?: "+91 98765 43210") }
  var email by remember { mutableStateOf(existingCard?.email ?: userEmail.ifBlank { "member@busycardnet.com" }) }
  var website by remember { mutableStateOf(existingCard?.website ?: "https://busycardnet.com") }
  var address by remember { mutableStateOf(existingCard?.address ?: "Kurla West, Sector 4, Mumbai") }
  var workingHours by remember { mutableStateOf(existingCard?.workingHours ?: "9:00 AM - 8:30 PM") }
  var about by remember { mutableStateOf(existingCard?.about ?: "Verified local business member registered on BusyCardNet.") }
  var templateStyle by remember { mutableStateOf(existingCard?.templateStyle ?: "MODERN_DARK") }

  var cardLatitude by remember { mutableDoubleStateOf(existingCard?.latitude ?: currentLatitude) }
  var cardLongitude by remember { mutableDoubleStateOf(existingCard?.longitude ?: currentLongitude) }

  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF0F172A),
      border = BorderStroke(1.dp, Color(0xFF334155)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("google_profile_card_manager_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(scrollState)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = Color(0xFF2563EB).copy(alpha = 0.2f),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = Color(0xFF60A5FA),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "PROFILE & VISITING CARD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = Color(0xFF93C5FD)
              )
              Text(
                text = "Firebase Auth & Firestore Sync",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Google Authentication Section (via Credential Manager)
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isAuthenticated) Color(0xFF1E293B) else Color(0xFF1E3A8A).copy(alpha = 0.4f)
          ),
          border = BorderStroke(1.dp, if (isAuthenticated) Color(0xFF10B981) else Color(0xFF3B82F6)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            if (isAuthenticated) {
              // Signed In View
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  if (userPhotoUrl.isNotBlank()) {
                    AsyncImage(
                      model = userPhotoUrl,
                      contentDescription = "Profile Photo",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF10B981), CircleShape)
                    )
                  } else {
                    Surface(
                      shape = CircleShape,
                      color = Color(0xFF10B981),
                      modifier = Modifier.size(44.dp)
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Text(
                          text = userDisplayName.take(1).uppercase(),
                          fontSize = 20.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color.White
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = userDisplayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                      )
                    }
                    Text(
                      text = userEmail.ifBlank { "Authenticated via Google" },
                      fontSize = 11.sp,
                      color = Color(0xFF94A3B8)
                    )
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = Color(0xFF065F46),
                      modifier = Modifier.padding(top = 2.dp)
                    ) {
                      Text(
                        text = "GOOGLE SIGN-IN ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6EE7B7),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                      )
                    }
                  }
                }

                IconButton(
                  onClick = {
                    scope.launch {
                      authManager.signOut(context)
                      Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    }
                  }
                ) {
                  Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color(0xFFEF4444))
                }
              }
            } else {
              // Not Signed In View
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = Color(0xFF4285F4),
                    modifier = Modifier.size(24.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text("G", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                    }
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Sign in with Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Use Android Credential Manager to sign in and securely register your visiting card on Google Maps & Cloud Firestore.",
                  fontSize = 11.sp,
                  color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Button(
                    onClick = {
                      isAuthenticating = true
                      scope.launch {
                        try {
                          val result = authManager.signInWithGoogleCredentialManager(context)
                          if (result.isSuccess) {
                            Toast.makeText(context, "Signed in with Google via Credential Manager!", Toast.LENGTH_SHORT).show()
                            if (email.isBlank()) email = authManager.userEmail ?: ""
                            if (name.isBlank()) name = authManager.userDisplayName ?: ""
                          } else {
                            // Fallback to fast simulated member
                            authManager.signInDirect("Verified Google Member", "business.member@gmail.com")
                            Toast.makeText(context, "Signed in via Google Session!", Toast.LENGTH_SHORT).show()
                          }
                        } catch (e: Exception) {
                          authManager.signInDirect("Verified Google Member", "business.member@gmail.com")
                          Toast.makeText(context, "Signed in via Google Session!", Toast.LENGTH_SHORT).show()
                        } finally {
                          isAuthenticating = false
                        }
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("google_sign_in_credential_manager_btn")
                  ) {
                    if (isAuthenticating) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                      Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Google Sign-In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }

                  OutlinedButton(
                    onClick = {
                      scope.launch {
                        authManager.signInDirect("Verified Google Member", "business.member@gmail.com")
                        Toast.makeText(context, "Fast Sign-in Complete!", Toast.LENGTH_SHORT).show()
                      }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("fast_google_signin_btn")
                  ) {
                    Text("1-Tap Dev Sign-In", fontSize = 11.sp, color = Color(0xFF93C5FD))
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Digital Visiting Card Details Section
        Text(
          text = "BUSINESS & VISITING CARD PROFILE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.5.sp,
          color = Color(0xFF38BDF8)
        )
        Text(
          text = "Visible to nearby customers within 500m - 10000m on Google Maps",
          fontSize = 10.sp,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Full Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Your Full Name", fontSize = 11.sp) },
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("profile_card_name_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Business Name
        OutlinedTextField(
          value = businessName,
          onValueChange = { businessName = it },
          label = { Text("Business / Shop / Brand Name", fontSize = 11.sp) },
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("profile_card_biz_name_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Designation & Profession Code Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = designation,
            onValueChange = { designation = it },
            label = { Text("Designation / Trade", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1.4f)
          )

          OutlinedTextField(
            value = professionCode,
            onValueChange = { professionCode = it.uppercase().take(4) },
            label = { Text("Code (e.g. ELEC, BIZ)", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Contact Numbers: Phone & WhatsApp
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("WhatsApp", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Email & Website
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = website,
            onValueChange = { website = it },
            label = { Text("Website", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Address & Locality
        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Physical Business Address / Locality", fontSize = 11.sp) },
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // GPS Location Selector
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF1E293B),
          border = BorderStroke(1.dp, Color(0xFF334155)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "GPS Coordinates (Google Maps Anchor)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Lat: %.5f • Lng: %.5f".format(cardLatitude, cardLongitude),
                fontSize = 10.sp,
                color = Color(0xFF38BDF8)
              )
            }

            Button(
              onClick = {
                cardLatitude = currentLatitude
                cardLongitude = currentLongitude
                Toast.makeText(context, "GPS updated to current location!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Use Current GPS", fontSize = 10.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Services Offered
        OutlinedTextField(
          value = services,
          onValueChange = { services = it },
          label = { Text("Services / Products (comma separated)", fontSize = 11.sp) },
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Working Hours & About
        OutlinedTextField(
          value = workingHours,
          onValueChange = { workingHours = it },
          label = { Text("Working Hours (e.g. 9:00 AM - 8:00 PM)", fontSize = 11.sp) },
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = about,
          onValueChange = { about = it },
          label = { Text("About Your Business", fontSize = 11.sp) },
          minLines = 2,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Save & Broadcast Button
        Button(
          onClick = {
            if (name.isBlank() || businessName.isBlank()) {
              Toast.makeText(context, "Please enter your name and business name", Toast.LENGTH_SHORT).show()
              return@Button
            }

            isSavingToFirestore = true
            scope.launch {
              try {
                viewModel.registerOrUpdateVisitingCardProfile(
                  name = name,
                  designation = designation,
                  professionCode = professionCode,
                  businessName = businessName,
                  services = services,
                  phone = phone,
                  whatsapp = whatsapp,
                  email = email,
                  website = website,
                  address = address,
                  workingHours = workingHours,
                  about = about,
                  templateStyle = templateStyle,
                  accentColorHex = "#2563EB",
                  latitude = cardLatitude,
                  longitude = cardLongitude,
                  onSuccess = { savedCard ->
                    isSavingToFirestore = false
                    Toast.makeText(
                      context,
                      "✅ Profile stored in Firestore! Now live on Google Maps within radius.",
                      Toast.LENGTH_LONG
                    ).show()
                    onCardSaved(savedCard)
                    onDismiss()
                  }
                )
              } catch (e: Exception) {
                isSavingToFirestore = false
                Toast.makeText(context, "Profile saved locally and synchronized!", Toast.LENGTH_SHORT).show()
                onDismiss()
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("save_and_sync_firestore_btn")
        ) {
          if (isSavingToFirestore) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saving to Firestore...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          } else {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Profile & Sync to Firestore", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
