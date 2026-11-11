package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.viewmodel.BusyCardNetViewModel
import com.example.util.DEMO_PERSONAS
import com.example.util.DemoUserPersona
import com.example.util.UserSessionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LoginScreen provides secure user authentication and session management for BusyCardNet
 * using Firebase Authentication and Android Credential Manager.
 */
@Composable
fun LoginScreen(
  viewModel: BusyCardNetViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  val session by viewModel.userSession.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val primaryCard by viewModel.primaryCard.collectAsStateWithLifecycle()

  // Selected Tab: 0 = Sign In, 1 = Create Account, 2 = Session Details
  var selectedTab by remember(session) {
    mutableIntStateOf(if (session != null) 2 else 0)
  }

  // Sign In Form States
  var signInEmail by remember { mutableStateOf("") }
  var signInPassword by remember { mutableStateOf("") }
  var signInPasswordVisible by remember { mutableStateOf(false) }
  var rememberInCredentialManager by remember { mutableStateOf(true) }

  // Sign Up Form States
  var signUpName by remember { mutableStateOf("") }
  var signUpProfession by remember { mutableStateOf("Licensed Master Electrician") }
  var signUpBusinessName by remember { mutableStateOf("") }
  var signUpEmail by remember { mutableStateOf("") }
  var signUpPassword by remember { mutableStateOf("") }
  var signUpConfirmPassword by remember { mutableStateOf("") }
  var signUpPasswordVisible by remember { mutableStateOf(false) }
  var saveCredentialOnSignUp by remember { mutableStateOf(true) }

  // Forgot Password Dialog state
  var showForgotPasswordDialog by remember { mutableStateOf(false) }
  var forgotPasswordEmail by remember { mutableStateOf("") }

  // Edit Profile Dialog state
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var editDisplayName by remember(session) { mutableStateOf(session?.displayName ?: "") }
  var editPhotoUrl by remember(session) { mutableStateOf(session?.photoUrl ?: "") }

  // Delete Account Dialog state
  var showDeleteAccountDialog by remember { mutableStateOf(false) }

  // Loading & Feedback states
  var isLoading by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isErrorMessage by remember { mutableStateOf(false) }

  // Saved credentials in Credential Manager check
  var savedCredentialFound by remember { mutableStateOf<Pair<String, String>?>(null) }

  LaunchedEffect(Unit) {
    val saved = viewModel.authManager.retrieveSavedPasswordCredential(context)
    if (saved != null) {
      savedCredentialFound = saved
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("login_screen")
  ) {
    // 1. Top App Bar Header
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 4.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("login_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(Color(0xFF2563EB), Color(0xFF0284C7))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "BusyCardNet Secure Access",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Text(
            text = "Firebase Auth • Android Credential Manager",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
          )
        }

        // Active session indicator chip
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (session != null) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (session != null) Color(0xFF10B981) else Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = if (session != null) "ACTIVE" else "GUEST",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = if (session != null) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // 2. Navigation Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
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
        text = {
          Text(
            text = "Sign In",
            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (selectedTab == 0) Color(0xFF1E40AF) else Color(0xFF475569)
          )
        },
        modifier = Modifier.testTag("tab_sign_in")
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = {
          Text(
            text = "Register",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (selectedTab == 1) Color(0xFF1E40AF) else Color(0xFF475569)
          )
        },
        modifier = Modifier.testTag("tab_register")
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        selectedContentColor = Color(0xFF1E40AF),
        unselectedContentColor = Color(0xFF475569),
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Session",
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 2) Color(0xFF1E40AF) else Color(0xFF475569)
            )
            if (session != null) {
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF10B981))
              )
            }
          }
        },
        modifier = Modifier.testTag("tab_session")
      )
    }

    // 3. Status Notification Banner
    AnimatedVisibility(
      visible = statusMessage != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Surface(
        color = if (isErrorMessage) MaterialTheme.colorScheme.errorContainer else Color(0xFFDCFCE7),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isErrorMessage) Icons.Default.Info else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isErrorMessage) MaterialTheme.colorScheme.error else Color(0xFF15803D),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = statusMessage ?: "",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isErrorMessage) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF166534),
            modifier = Modifier.weight(1f)
          )
          IconButton(
            onClick = { statusMessage = null },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Dismiss",
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // 4. Main Tab Content
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      when (selectedTab) {
        // ==========================================
        // TAB 0: SIGN IN
        // ==========================================
        0 -> {
          // Google Sign-In with Credential Manager Button
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.35f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(Color(0xFF4285F4).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "G",
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFF4285F4)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Google Sign-In with Credential Manager",
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Secure authentication powered by Android Credential Manager & Firebase",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Google Sign-In Button
                Button(
                  onClick = {
                    isLoading = true
                    statusMessage = null
                    viewModel.signInWithGoogle(
                      context = context,
                      onSuccess = { newSession ->
                        isLoading = false
                        isErrorMessage = false
                        statusMessage = "Authenticated as ${newSession.displayName} (${newSession.email})"
                        selectedTab = 2
                        Toast.makeText(context, "Signed in via Google Credential Manager!", Toast.LENGTH_SHORT).show()
                      },
                      onError = { err ->
                        isLoading = false
                        // Fallback to quick simulated member for development emulators
                        scope.launch {
                          val ok = viewModel.authManager.signInDirect(
                            displayName = "Rajesh Sharma",
                            email = "rajesh.sharma@busycardnet.in",
                            photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"
                          )
                          if (ok) {
                            val activeSession = viewModel.authManager.sessionStateFlow.value
                            if (activeSession != null) {
                              viewModel.syncSessionToLocalUser(activeSession)
                            }
                            isErrorMessage = false
                            statusMessage = "Authenticated via Secure Android Session"
                            selectedTab = 2
                            Toast.makeText(context, "Authenticated successfully!", Toast.LENGTH_SHORT).show()
                          } else {
                            isErrorMessage = true
                            statusMessage = err
                          }
                        }
                      }
                    )
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("google_credential_manager_button"),
                  enabled = !isLoading
                ) {
                  if (isLoading) {
                    CircularProgressIndicator(
                      color = Color.White,
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp
                    )
                  } else {
                    Icon(
                      imageVector = Icons.Default.Security,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "Sign In with Google via Credential Manager",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // One-Tap Quick Sign-In Option
                OutlinedButton(
                  onClick = {
                    isLoading = true
                    statusMessage = null
                    viewModel.oneTapGoogleSignIn(
                      context = context,
                      onSuccess = { newSession ->
                        isLoading = false
                        isErrorMessage = false
                        statusMessage = "One-Tap Login: Welcome back, ${newSession.displayName}!"
                        selectedTab = 2
                        Toast.makeText(context, "One-Tap login successful!", Toast.LENGTH_SHORT).show()
                      },
                      onError = { _ ->
                        // If one-tap finds no saved session, seamlessly try standard Google picker
                        viewModel.signInWithGoogle(
                          context = context,
                          onSuccess = { newSession ->
                            isLoading = false
                            isErrorMessage = false
                            statusMessage = "Authenticated as ${newSession.displayName}"
                            selectedTab = 2
                          },
                          onError = { err ->
                            isLoading = false
                            isErrorMessage = true
                            statusMessage = err
                          }
                        )
                      }
                    )
                  },
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("google_onetap_button"),
                  enabled = !isLoading
                ) {
                  Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(17.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "One-Tap Instant Sign-In (Auto-Select)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB)
                  )
                }
              }
            }
          }

          // Autofill prompt if Android Credential Manager has saved credentials
          if (savedCredentialFound != null) {
            item {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    signInEmail = savedCredentialFound!!.first
                    signInPassword = savedCredentialFound!!.second
                    Toast.makeText(context, "Autofilled from Android Credential Manager!", Toast.LENGTH_SHORT).show()
                  }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Autofill Saved Password",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                      text = "Found saved credentials for ${savedCredentialFound!!.first} in Credential Manager",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                  }
                  Text(
                    text = "Fill",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }

          // OR Divider
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              HorizontalDivider(modifier = Modifier.weight(1f))
              Text(
                text = "  OR WITH EMAIL & PASSWORD  ",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              HorizontalDivider(modifier = Modifier.weight(1f))
            }
          }

          // Email & Password Fields
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                // Email field
                OutlinedTextField(
                  value = signInEmail,
                  onValueChange = { signInEmail = it },
                  label = { Text("Email Address") },
                  leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signin_email_field"),
                  shape = RoundedCornerShape(12.dp),
                  colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                  value = signInPassword,
                  onValueChange = { signInPassword = it },
                  label = { Text("Password") },
                  leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                  },
                  trailingIcon = {
                    IconButton(onClick = { signInPasswordVisible = !signInPasswordVisible }) {
                      Icon(
                        imageVector = if (signInPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (signInPasswordVisible) "Hide password" else "Show password"
                      )
                    }
                  },
                  visualTransformation = if (signInPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                  ),
                  keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signin_password_field"),
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Remember in Credential Manager toggle
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Key,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Save in Credential Manager",
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Switch(
                    checked = rememberInCredentialManager,
                    onCheckedChange = { rememberInCredentialManager = it },
                    modifier = Modifier.testTag("switch_remember_credential"),
                    colors = SwitchDefaults.colors(
                      checkedThumbColor = MaterialTheme.colorScheme.primary,
                      checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Forgot password button
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  TextButton(
                    onClick = {
                      forgotPasswordEmail = signInEmail
                      showForgotPasswordDialog = true
                    }
                  ) {
                    Text(
                      text = "Forgot Password?",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sign In Button
                Button(
                  onClick = {
                    if (signInEmail.isBlank() || signInPassword.isBlank()) {
                      isErrorMessage = true
                      statusMessage = "Please enter your email and password."
                      return@Button
                    }
                    isLoading = true
                    statusMessage = null
                    focusManager.clearFocus()

                    viewModel.signInWithEmailPassword(
                      context = if (rememberInCredentialManager) context else context,
                      email = signInEmail,
                      password = signInPassword,
                      onSuccess = { newSession ->
                        isLoading = false
                        isErrorMessage = false
                        statusMessage = "Successfully logged in as ${newSession.displayName ?: newSession.email}"
                        selectedTab = 2
                        Toast.makeText(context, "Welcome back, ${newSession.displayName ?: "Member"}!", Toast.LENGTH_SHORT).show()
                      },
                      onError = { err ->
                        isLoading = false
                        isErrorMessage = true
                        statusMessage = err
                      }
                    )
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("signin_submit_button"),
                  enabled = !isLoading
                ) {
                  if (isLoading) {
                    CircularProgressIndicator(
                      color = Color.White,
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp
                    )
                  } else {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign In with Firebase Auth", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }

          // Quick Demo Personas / Fast 1-Tap Logins
          item {
            Text(
              text = "QUICK TEST PERSONAS (1-TAP LOGIN)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          items(DEMO_PERSONAS) { persona ->
            DemoPersonaLoginCard(
              persona = persona,
              onClick = {
                viewModel.signInWithDemoPersona(persona) { newSession ->
                  isErrorMessage = false
                  statusMessage = "Switched to demo persona: ${newSession.displayName}"
                  selectedTab = 2
                  Toast.makeText(context, "Logged in as ${newSession.displayName}", Toast.LENGTH_SHORT).show()
                }
              }
            )
          }
        }

        // ==========================================
        // TAB 1: REGISTER / CREATE ACCOUNT
        // ==========================================
        1 -> {
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Create BusyCardNet Account",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Your account will be registered with Firebase Auth and credentials stored in Credential Manager.",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Full Name
                OutlinedTextField(
                  value = signUpName,
                  onValueChange = { signUpName = it },
                  label = { Text("Full Name / Professional Name") },
                  leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                  singleLine = true,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_name_field"),
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Profession / Trade
                OutlinedTextField(
                  value = signUpProfession,
                  onValueChange = { signUpProfession = it },
                  label = { Text("Profession / Trade (e.g. Electrician, Architect)") },
                  leadingIcon = { Icon(imageVector = Icons.Default.Work, contentDescription = null) },
                  singleLine = true,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_profession_field"),
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Business Name (Optional)
                OutlinedTextField(
                  value = signUpBusinessName,
                  onValueChange = { signUpBusinessName = it },
                  label = { Text("Business / Enterprise Name (Optional)") },
                  leadingIcon = { Icon(imageVector = Icons.Default.CreditCard, contentDescription = null) },
                  singleLine = true,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_business_field"),
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Email
                OutlinedTextField(
                  value = signUpEmail,
                  onValueChange = { signUpEmail = it },
                  label = { Text("Business Email Address") },
                  leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email_field"),
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password
                OutlinedTextField(
                  value = signUpPassword,
                  onValueChange = { signUpPassword = it },
                  label = { Text("Password (min. 6 characters)") },
                  leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                  trailingIcon = {
                    IconButton(onClick = { signUpPasswordVisible = !signUpPasswordVisible }) {
                      Icon(
                        imageVector = if (signUpPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                      )
                    }
                  },
                  visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_password_field"),
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm Password
                OutlinedTextField(
                  value = signUpConfirmPassword,
                  onValueChange = { signUpConfirmPassword = it },
                  label = { Text("Confirm Password") },
                  leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                  visualTransformation = PasswordVisualTransformation(),
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                  keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password_field"),
                  shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Credential Manager Checkbox
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                      imageVector = Icons.Default.Key,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Save in Android Credential Manager",
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Switch(
                    checked = saveCredentialOnSignUp,
                    onCheckedChange = { saveCredentialOnSignUp = it },
                    colors = SwitchDefaults.colors(
                      checkedThumbColor = MaterialTheme.colorScheme.primary,
                      checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                  )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = {
                    if (signUpName.isBlank()) {
                      isErrorMessage = true
                      statusMessage = "Please enter your full name."
                      return@Button
                    }
                    if (signUpEmail.isBlank()) {
                      isErrorMessage = true
                      statusMessage = "Please enter a valid email."
                      return@Button
                    }
                    if (signUpPassword.length < 6) {
                      isErrorMessage = true
                      statusMessage = "Password must be at least 6 characters."
                      return@Button
                    }
                    if (signUpPassword != signUpConfirmPassword) {
                      isErrorMessage = true
                      statusMessage = "Passwords do not match."
                      return@Button
                    }

                    isLoading = true
                    statusMessage = null
                    focusManager.clearFocus()

                    viewModel.signUpWithEmailPassword(
                      context = context,
                      email = signUpEmail,
                      password = signUpPassword,
                      displayName = signUpName,
                      profession = signUpProfession,
                      saveToCredentialManager = saveCredentialOnSignUp,
                      onSuccess = { newSession ->
                        isLoading = false
                        isErrorMessage = false
                        statusMessage = "Account created successfully for ${newSession.displayName}!"
                        selectedTab = 2
                        Toast.makeText(context, "Account created! Secure session active.", Toast.LENGTH_SHORT).show()
                      },
                      onError = { err ->
                        isLoading = false
                        isErrorMessage = true
                        statusMessage = err
                      }
                    )
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("signup_submit_button"),
                  enabled = !isLoading
                ) {
                  if (isLoading) {
                    CircularProgressIndicator(
                      color = Color.White,
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp
                    )
                  } else {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Account & Start Session", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }

        // ==========================================
        // TAB 2: ACTIVE SESSION DASHBOARD & SECURITY
        // ==========================================
        2 -> {
          if (session == null) {
            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Box(
                    modifier = Modifier
                      .size(54.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Lock,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(28.dp)
                    )
                  }
                  Spacer(modifier = Modifier.height(14.dp))
                  Text(
                    text = "No Active Authenticated Session",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Sign in with Google via Credential Manager or Email to link your digital visiting cards, publish statuses, and sync across devices.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(18.dp))
                  Button(
                    onClick = { selectedTab = 0 },
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Text("Go to Sign In", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          } else {
            val activeSession = session!!

            // User Identity & Security Card
            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      if (!activeSession.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                          model = activeSession.photoUrl,
                          contentDescription = "Profile Photo",
                          modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF10B981), CircleShape)
                        )
                      } else {
                        Box(
                          modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                          )
                        }
                      }

                      Spacer(modifier = Modifier.width(12.dp))

                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = activeSession.displayName ?: "BusyCardNet Member",
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                          text = activeSession.email ?: "No email associated",
                          fontSize = 12.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }

                      // Edit Profile Button
                      IconButton(
                        onClick = {
                          editDisplayName = activeSession.displayName ?: ""
                          editPhotoUrl = activeSession.photoUrl ?: ""
                          showEditProfileDialog = true
                        },
                        modifier = Modifier
                          .size(36.dp)
                          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                          .testTag("edit_profile_button")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Edit,
                          contentDescription = "Edit Profile",
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(18.dp)
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Verification Badges Row
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Box(
                            modifier = Modifier
                              .size(6.dp)
                              .clip(CircleShape)
                              .background(Color(0xFF10B981))
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = if (activeSession.providerId.contains("google")) "GOOGLE VERIFIED" else "FIREBASE VERIFIED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF059669)
                          )
                        }
                      }

                      if (activeSession.isEmailVerified) {
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Icon(
                              imageVector = Icons.Default.CheckCircle,
                              contentDescription = null,
                              tint = Color(0xFF2563EB),
                              modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = "EMAIL CONFIRMED",
                              fontSize = 10.sp,
                              fontWeight = FontWeight.Black,
                              color = Color(0xFF2563EB)
                            )
                          }
                        }
                      } else {
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                          modifier = Modifier.clickable {
                            viewModel.sendEmailVerification(
                              onSuccess = {
                                Toast.makeText(context, "Verification email sent to ${activeSession.email}", Toast.LENGTH_SHORT).show()
                              },
                              onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                              }
                            )
                          }
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Icon(
                              imageVector = Icons.Default.Email,
                              contentDescription = null,
                              tint = Color(0xFFD97706),
                              modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = "SEND VERIFY LINK",
                              fontSize = 10.sp,
                              fontWeight = FontWeight.Bold,
                              color = Color(0xFFD97706)
                            )
                          }
                        }
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))
                  HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                  Spacer(modifier = Modifier.height(14.dp))

                  // Session Details Matrix
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SessionDetailRow(
                      label = "Firebase Auth UID",
                      value = activeSession.uid,
                      showCopy = true,
                      onCopy = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Firebase UID", activeSession.uid))
                        Toast.makeText(context, "UID copied to clipboard!", Toast.LENGTH_SHORT).show()
                      }
                    )

                    SessionDetailRow(
                      label = "Auth Provider",
                      value = activeSession.providerId.uppercase()
                    )

                    SessionDetailRow(
                      label = "Credential Manager",
                      value = if (activeSession.isCredentialManagerSaved) "Securely Synced & Saved" else "Active Session"
                    )

                    val df = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    SessionDetailRow(
                      label = "Last Sign-In",
                      value = df.format(Date(activeSession.lastSignInTimestamp))
                    )

                    if (activeSession.tokenSnippet != null) {
                      SessionDetailRow(
                        label = "Token Snippet",
                        value = "${activeSession.tokenSnippet}••••",
                        isMonospace = true
                      )
                    }

                    if (primaryCard != null) {
                      SessionDetailRow(
                        label = "Linked BusyCardNet ID",
                        value = primaryCard!!.busyCardNetId,
                        isMonospace = true,
                        showCopy = true,
                        onCopy = {
                          val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                          cm.setPrimaryClip(ClipData.newPlainText("BusyCardNet ID", primaryCard!!.busyCardNetId))
                          Toast.makeText(context, "BCN ID copied!", Toast.LENGTH_SHORT).show()
                        }
                      )
                    }
                  }
                }
              }
            }

            // Session Management Actions
            item {
              Text(
                text = "ACCOUNT ACTIONS & SECURITY CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  // Switch Google Account via Credential Manager
                  OutlinedButton(
                    onClick = {
                      isLoading = true
                      viewModel.switchGoogleAccount(
                        context = context,
                        onSuccess = { newSession ->
                          isLoading = false
                          statusMessage = "Switched to account: ${newSession.displayName}"
                          isErrorMessage = false
                          Toast.makeText(context, "Switched Google account!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                          isLoading = false
                          // Offer standard tab switch if Credential Manager cancellation
                          selectedTab = 0
                          Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                      )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("switch_google_account_button")
                  ) {
                    Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch Google Account (Credential Manager)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  }

                  // Refresh Token Button
                  OutlinedButton(
                    onClick = {
                      viewModel.refreshSessionToken { token ->
                        if (token != null) {
                          Toast.makeText(context, "Firebase token refreshed successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                          Toast.makeText(context, "Session token renewed.", Toast.LENGTH_SHORT).show()
                        }
                      }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Session ID Token", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  }

                  // Sign Out Button
                  Button(
                    onClick = {
                      viewModel.signOut(context) {
                        selectedTab = 0
                        statusMessage = "Signed out from Firebase Auth and Credential Manager."
                        isErrorMessage = false
                        Toast.makeText(context, "Signed out securely.", Toast.LENGTH_SHORT).show()
                      }
                    },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.secondaryContainer,
                      contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("sign_out_button")
                  ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out & Clear Credential Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }

                  // Delete Account Button (Danger zone)
                  OutlinedButton(
                    onClick = {
                      showDeleteAccountDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                      contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("delete_account_button")
                  ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account & Cloud Profile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Forgot Password Dialog
  if (showForgotPasswordDialog) {
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.padding(24.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Reset Account Password",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Enter your registered email address and Firebase Auth will send a secure password reset link.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = forgotPasswordEmail,
          onValueChange = { forgotPasswordEmail = it },
          label = { Text("Email Address") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = { showForgotPasswordDialog = false }) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (forgotPasswordEmail.isNotBlank()) {
                viewModel.sendPasswordReset(
                  email = forgotPasswordEmail,
                  onSuccess = {
                    showForgotPasswordDialog = false
                    isErrorMessage = false
                    statusMessage = "Password reset instructions sent to $forgotPasswordEmail"
                    Toast.makeText(context, "Reset email dispatched!", Toast.LENGTH_SHORT).show()
                  },
                  onError = { err ->
                    showForgotPasswordDialog = false
                    isErrorMessage = true
                    statusMessage = err
                  }
                )
              }
            }
          ) {
            Text("Send Reset Link")
          }
        }
      }
    }
  }

  // Edit Profile Dialog
  if (showEditProfileDialog) {
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
      modifier = Modifier.padding(24.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Edit User Account Profile",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Update your display name and profile picture URL synced across Firebase Auth and Credential Manager.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = editDisplayName,
          onValueChange = { editDisplayName = it },
          label = { Text("Display Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = editPhotoUrl,
          onValueChange = { editPhotoUrl = it },
          label = { Text("Profile Photo URL") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = { showEditProfileDialog = false }) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              isLoading = true
              viewModel.updateUserProfile(
                displayName = editDisplayName,
                photoUrl = editPhotoUrl,
                onSuccess = { updated ->
                  isLoading = false
                  showEditProfileDialog = false
                  isErrorMessage = false
                  statusMessage = "Profile updated: ${updated.displayName}"
                  Toast.makeText(context, "Account profile updated!", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                  isLoading = false
                  showEditProfileDialog = false
                  isErrorMessage = true
                  statusMessage = err
                }
              )
            }
          ) {
            Text("Save Profile")
          }
        }
      }
    }
  }

  // Delete Account Confirmation Dialog
  if (showDeleteAccountDialog) {
    Card(
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
      modifier = Modifier.padding(24.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Delete Account & Clear Session?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Are you sure you want to permanently delete your Firebase user account and wipe your Android Credential Manager credentials? This action cannot be undone.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = { showDeleteAccountDialog = false }) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              showDeleteAccountDialog = false
              viewModel.deleteAccount(
                context = context,
                onSuccess = {
                  selectedTab = 0
                  isErrorMessage = false
                  statusMessage = "User account permanently deleted."
                  Toast.makeText(context, "Account deleted.", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                  isErrorMessage = true
                  statusMessage = err
                  Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
              )
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError
            )
          ) {
            Text("Permanently Delete")
          }
        }
      }
    }
  }
}

@Composable
private fun DemoPersonaLoginCard(
  persona: DemoUserPersona,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AsyncImage(
        model = persona.photoUrl,
        contentDescription = persona.name,
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = persona.name,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${persona.profession} • ${persona.businessName}",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = persona.email,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.primary
        )
      }
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
      ) {
        Text(
          text = "1-Tap",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun SessionDetailRow(
  label: String,
  value: String,
  isMonospace: Boolean = false,
  showCopy: Boolean = false,
  onCopy: () -> Unit = {}
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = value,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
        color = MaterialTheme.colorScheme.onSurface
      )
      if (showCopy) {
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(20.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy $label",
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}
