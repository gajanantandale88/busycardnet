package com.example.ui.components.kyc

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.BusinessCertificateType
import com.example.data.model.GovernmentIdType
import com.example.data.model.KycVerificationData
import com.example.data.model.KycVerificationStatus

/**
 * Multi-step KYC Identity & Business Verification Dialog using CameraX and Compose.
 * Guides users through Government ID, Face front/left/right liveness photos,
 * and Certificate of Business capture to prevent fake cards and unlock business operations.
 */
@Composable
fun KycVerificationDialog(
  currentStatus: KycVerificationStatus,
  existingData: KycVerificationData,
  onDismiss: () -> Unit,
  onSubmitKyc: (KycVerificationData) -> Unit,
  onSimulateApprove: () -> Unit
) {
  val context = LocalContext.current

  // 0: Overview/Status, 1: Gov ID, 2: Front Face, 3: Left Face, 4: Right Face, 5: Business Cert, 6: Final Review
  var currentStep by remember { mutableIntStateOf(if (currentStatus == KycVerificationStatus.UNVERIFIED) 1 else 0) }

  // Form states
  var idType by remember { mutableStateOf(existingData.idType) }
  var idNumber by remember { mutableStateOf(existingData.idNumber) }
  var applicantName by remember { mutableStateOf(existingData.applicantFullName) }
  var govIdPhotoUri by remember { mutableStateOf(existingData.governmentIdPhotoUri) }

  var frontFacePhotoUri by remember { mutableStateOf(existingData.frontFacePhotoUri) }
  var leftProfilePhotoUri by remember { mutableStateOf(existingData.leftProfilePhotoUri) }
  var rightProfilePhotoUri by remember { mutableStateOf(existingData.rightProfilePhotoUri) }

  var businessCertType by remember { mutableStateOf(existingData.businessCertificateType) }
  var businessName by remember { mutableStateOf(existingData.businessName) }
  var businessCertNumber by remember { mutableStateOf(existingData.businessCertificateNumber) }
  var businessCertPhotoUri by remember { mutableStateOf(existingData.businessCertificatePhotoUri) }

  var isDeclarationAccepted by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF090D16))
        .testTag("kyc_verification_dialog"),
      color = Color(0xFF090D16)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        // Top App Bar
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
                .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "BCN Identity & KYC Verification",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Anti-Fraud & Genuine Business Protection",
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
              .testTag("close_kyc_dialog_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Stepper Progress Bar (when inside wizard 1 to 6)
        if (currentStep in 1..6) {
          Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "STEP $currentStep OF 6: ${getStepTitle(currentStep)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF38BDF8)
              )
              Text(
                text = "${(currentStep * 100) / 6}% Completed",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { currentStep / 6f },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = Color(0xFF2563EB),
              trackColor = Color(0xFF1E293B)
            )
          }
        }

        // Main Step Content
        Box(modifier = Modifier.weight(1f)) {
          when (currentStep) {
            0 -> KycOverviewScreen(
              status = currentStatus,
              data = existingData,
              onStartVerification = { currentStep = 1 },
              onSimulateApproval = {
                onSimulateApprove()
                Toast.makeText(context, "KYC Verified! Business Privileges Unlocked.", Toast.LENGTH_LONG).show()
              }
            )

            1 -> Step1GovernmentId(
              idType = idType,
              onIdTypeChange = { idType = it },
              idNumber = idNumber,
              onIdNumberChange = { idNumber = it },
              applicantName = applicantName,
              onApplicantNameChange = { applicantName = it },
              photoUri = govIdPhotoUri,
              onPhotoCaptured = { govIdPhotoUri = it },
              onRetake = { govIdPhotoUri = "" }
            )

            2 -> Step2FrontFaceLiveness(
              photoUri = frontFacePhotoUri,
              onPhotoCaptured = { frontFacePhotoUri = it },
              onRetake = { frontFacePhotoUri = "" }
            )

            3 -> Step3LeftProfileLiveness(
              photoUri = leftProfilePhotoUri,
              onPhotoCaptured = { leftProfilePhotoUri = it },
              onRetake = { leftProfilePhotoUri = "" }
            )

            4 -> Step4RightProfileLiveness(
              photoUri = rightProfilePhotoUri,
              onPhotoCaptured = { rightProfilePhotoUri = it },
              onRetake = { rightProfilePhotoUri = "" }
            )

            5 -> Step5BusinessCertificate(
              certType = businessCertType,
              onCertTypeChange = { businessCertType = it },
              businessName = businessName,
              onBusinessNameChange = { businessName = it },
              certNumber = businessCertNumber,
              onCertNumberChange = { businessCertNumber = it },
              photoUri = businessCertPhotoUri,
              onPhotoCaptured = { businessCertPhotoUri = it },
              onRetake = { businessCertPhotoUri = "" }
            )

            6 -> Step6FinalReviewAndSubmit(
              idType = idType,
              idNumber = idNumber,
              applicantName = applicantName,
              govIdPhotoUri = govIdPhotoUri,
              frontFacePhotoUri = frontFacePhotoUri,
              leftProfilePhotoUri = leftProfilePhotoUri,
              rightProfilePhotoUri = rightProfilePhotoUri,
              certType = businessCertType,
              businessName = businessName,
              certNumber = businessCertNumber,
              certPhotoUri = businessCertPhotoUri,
              isDeclarationAccepted = isDeclarationAccepted,
              onDeclarationChange = { isDeclarationAccepted = it },
              onSubmit = {
                val submission = KycVerificationData(
                  idType = idType,
                  idNumber = idNumber,
                  governmentIdPhotoUri = govIdPhotoUri,
                  frontFacePhotoUri = frontFacePhotoUri,
                  leftProfilePhotoUri = leftProfilePhotoUri,
                  rightProfilePhotoUri = rightProfilePhotoUri,
                  businessCertificateType = businessCertType,
                  businessCertificateNumber = businessCertNumber,
                  businessCertificatePhotoUri = businessCertPhotoUri,
                  applicantFullName = applicantName,
                  businessName = businessName,
                  submittedTimestamp = System.currentTimeMillis(),
                  status = KycVerificationStatus.PENDING_REVIEW,
                  complianceNotes = "Submitted for verification. Documents in review queue."
                )
                onSubmitKyc(submission)
                Toast.makeText(context, "Verification Submitted! Review in progress.", Toast.LENGTH_LONG).show()
                currentStep = 0
              }
            )
          }
        }

        // Bottom Navigation Buttons (Next / Prev)
        if (currentStep in 1..5) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                if (currentStep > 1) currentStep-- else currentStep = 0
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("kyc_prev_button")
            ) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Back", fontSize = 12.sp)
            }

            val canProceed = when (currentStep) {
              1 -> idNumber.isNotBlank() && applicantName.isNotBlank() && govIdPhotoUri.isNotBlank()
              2 -> frontFacePhotoUri.isNotBlank()
              3 -> leftProfilePhotoUri.isNotBlank()
              4 -> rightProfilePhotoUri.isNotBlank()
              5 -> businessCertNumber.isNotBlank() && businessName.isNotBlank() && businessCertPhotoUri.isNotBlank()
              else -> true
            }

            Button(
              onClick = { currentStep++ },
              enabled = canProceed,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
              modifier = Modifier.testTag("kyc_next_button")
            ) {
              Text("Next Step", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}

private fun getStepTitle(step: Int): String {
  return when (step) {
    1 -> "GOVERNMENT ID PHOTO"
    2 -> "FRONT-FACING PHOTO"
    3 -> "FACE LEFT PROFILE"
    4 -> "FACE RIGHT PROFILE"
    5 -> "BUSINESS CERTIFICATE"
    6 -> "REVIEW & SUBMIT"
    else -> "OVERVIEW"
  }
}

// ---------------------------------------------------------------------------
// Screen 0: Overview & Current Approval Status
// ---------------------------------------------------------------------------
@Composable
private fun KycOverviewScreen(
  status: KycVerificationStatus,
  data: KycVerificationData,
  onStartVerification: () -> Unit,
  onSimulateApproval: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      // Status Banner
      val badgeColor = when (status) {
        KycVerificationStatus.APPROVED -> Color(0xFF10B981)
        KycVerificationStatus.PENDING_REVIEW -> Color(0xFFF59E0B)
        KycVerificationStatus.REJECTED -> Color(0xFFEF4444)
        KycVerificationStatus.UNVERIFIED -> Color(0xFF64748B)
      }

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        modifier = Modifier.fillMaxWidth().border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = badgeColor.copy(alpha = 0.2f),
              border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (status == KycVerificationStatus.APPROVED) Icons.Default.Verified else Icons.Default.Security,
                  contentDescription = null,
                  tint = badgeColor,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = status.displayName,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = badgeColor
                )
              }
            }

            Text(
              text = "BusyCardNet Compliance",
              fontSize = 10.sp,
              color = Color(0xFF94A3B8)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = when (status) {
              KycVerificationStatus.APPROVED -> "Your business identity is fully verified! You can publish products, offer services, and connect securely with clients."
              KycVerificationStatus.PENDING_REVIEW -> "Your KYC documents are currently in the compliance review queue. Approvals typically take 2-4 hours."
              KycVerificationStatus.REJECTED -> "Verification failed. Please review the notes below and resubmit clear photographs."
              KycVerificationStatus.UNVERIFIED -> "To protect clients from fake cards and scams, every merchant must identify themselves through Government ID, Face photos, and Business Certificate."
            },
            color = Color(0xFFE2E8F0),
            fontSize = 13.sp,
            lineHeight = 19.sp
          )

          if (data.complianceNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF0F172A),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Compliance Notes: ${data.complianceNotes}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      }
    }

    // Requirements Checklist
    item {
      Text(
        text = "REQUIRED VERIFICATION STEPS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF38BDF8)
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VerificationItemRow(
          number = "1",
          title = "Government ID Card",
          subtitle = "Aadhaar / PAN / Passport / Voter ID front photo",
          isDone = data.governmentIdPhotoUri.isNotBlank()
        )
        VerificationItemRow(
          number = "2",
          title = "Front-Facing Face Photo",
          subtitle = "Biometric selfie to confirm account holder match",
          isDone = data.frontFacePhotoUri.isNotBlank()
        )
        VerificationItemRow(
          number = "3",
          title = "Face Left & Right Profile",
          subtitle = "Liveness head-turn check preventing static photo fraud",
          isDone = data.leftProfilePhotoUri.isNotBlank() && data.rightProfilePhotoUri.isNotBlank()
        )
        VerificationItemRow(
          number = "4",
          title = "Certificate of Business",
          subtitle = "GSTIN / Udyam MSME / Shop Act / Trade License",
          isDone = data.businessCertificatePhotoUri.isNotBlank()
        )
      }
    }

    // Reward Callout
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Text("🎁", fontSize = 20.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "+100 Marketing Credits Reward",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFCD34D)
            )
            Text(
              text = "Get 100 free credits upon verification to promote your digital visiting card and products.",
              fontSize = 11.sp,
              color = Color(0xFFCBD5E1)
            )
          }
        }
      }
    }

    // Action Buttons
    item {
      Spacer(modifier = Modifier.height(4.dp))
      if (status != KycVerificationStatus.APPROVED) {
        Button(
          onClick = onStartVerification,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_kyc_verification_button")
        ) {
          Icon(imageVector = Icons.Default.Security, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (status == KycVerificationStatus.PENDING_REVIEW) "Update / Resubmit KYC" else "Start KYC Verification Now",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }

      // Compliance Test Simulation Button (to immediately test approved state)
      if (status != KycVerificationStatus.APPROVED) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = onSimulateApproval,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(44.dp).testTag("simulate_kyc_approve_button")
        ) {
          Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Simulate BusyCardNet Team Approval (Demo)", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
      } else {
        OutlinedButton(
          onClick = onStartVerification,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
          Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Update Registered Documents", fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
private fun VerificationItemRow(
  number: String,
  title: String,
  subtitle: String,
  isDone: Boolean
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF131D31),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(if (isDone) Color(0xFF10B981) else Color(0xFF1E293B)),
        contentAlignment = Alignment.Center
      ) {
        if (isDone) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        } else {
          Text(number, color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp)
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Step 1: Government Identity Document
// ---------------------------------------------------------------------------
@Composable
private fun Step1GovernmentId(
  idType: GovernmentIdType,
  onIdTypeChange: (GovernmentIdType) -> Unit,
  idNumber: String,
  onIdNumberChange: (String) -> Unit,
  applicantName: String,
  onApplicantNameChange: (String) -> Unit,
  photoUri: String,
  onPhotoCaptured: (String) -> Unit,
  onRetake: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Select Government Document Type",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF38BDF8)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        GovernmentIdType.entries.take(3).forEach { type ->
          val selected = type == idType
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selected) Color(0xFF2563EB) else Color(0xFF1E293B),
            modifier = Modifier
              .weight(1f)
              .clickable { onIdTypeChange(type) }
          ) {
            Text(
              text = when (type) {
                GovernmentIdType.AADHAAR -> "Aadhaar"
                GovernmentIdType.PAN -> "PAN Card"
                GovernmentIdType.PASSPORT -> "Passport"
                else -> type.displayName
              },
              fontSize = 11.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
              color = Color.White,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        GovernmentIdType.entries.drop(3).forEach { type ->
          val selected = type == idType
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selected) Color(0xFF2563EB) else Color(0xFF1E293B),
            modifier = Modifier
              .weight(1f)
              .clickable { onIdTypeChange(type) }
          ) {
            Text(
              text = type.displayName,
              fontSize = 11.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
              color = Color.White,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
          }
        }
      }
    }

    item {
      OutlinedTextField(
        value = applicantName,
        onValueChange = onApplicantNameChange,
        label = { Text("Full Legal Name (as on ID)") },
        placeholder = { Text("e.g. Rajesh Kumar Sharma") },
        modifier = Modifier.fillMaxWidth().testTag("kyc_applicant_name_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = Color(0xFF38BDF8),
          unfocusedBorderColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(10.dp)
      )
    }

    item {
      OutlinedTextField(
        value = idNumber,
        onValueChange = onIdNumberChange,
        label = { Text("${idType.displayName} Number") },
        placeholder = { Text(idType.idPlaceholder) },
        modifier = Modifier.fillMaxWidth().testTag("kyc_id_number_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = Color(0xFF38BDF8),
          unfocusedBorderColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(10.dp)
      )
    }

    item {
      Text(
        text = "Capture Front of Government ID",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Place ID inside the card boundary. Ensure all text and photo are sharp and glare-free.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8)
      )
      Spacer(modifier = Modifier.height(8.dp))

      CameraXCaptureView(
        stepTitle = "Government ID Front",
        guideInstruction = "Align ID card inside rectangle frame",
        overlayType = CaptureOverlayType.ID_CARD,
        preferFrontCamera = false,
        capturedPhotoUri = photoUri.ifBlank { null },
        onPhotoCaptured = onPhotoCaptured,
        onRetake = onRetake
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Step 2: Front-Facing Face Liveness Photo
// ---------------------------------------------------------------------------
@Composable
private fun Step2FrontFaceLiveness(
  photoUri: String,
  onPhotoCaptured: (String) -> Unit,
  onRetake: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        text = "Front Face Photo (Selfie)",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Position your face inside the oval. Keep eyes open and look directly into the camera.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8)
      )
      Spacer(modifier = Modifier.height(10.dp))

      CameraXCaptureView(
        stepTitle = "Front Face Capture",
        guideInstruction = "Look straight into camera • Keep neutral expression",
        overlayType = CaptureOverlayType.FACE_OVAL,
        preferFrontCamera = true,
        capturedPhotoUri = photoUri.ifBlank { null },
        onPhotoCaptured = onPhotoCaptured,
        onRetake = onRetake
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Step 3: Face Left Profile
// ---------------------------------------------------------------------------
@Composable
private fun Step3LeftProfileLiveness(
  photoUri: String,
  onPhotoCaptured: (String) -> Unit,
  onRetake: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Turn Head: Left Profile (45°)",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
      Text(
        text = "Liveness check: Turn your head gently to the LEFT so your left cheek and ear are visible.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8)
      )
      Spacer(modifier = Modifier.height(10.dp))

      CameraXCaptureView(
        stepTitle = "Left Profile Check",
        guideInstruction = "Turn head 45° to the LEFT ⬅️",
        overlayType = CaptureOverlayType.FACE_OVAL,
        preferFrontCamera = true,
        capturedPhotoUri = photoUri.ifBlank { null },
        onPhotoCaptured = onPhotoCaptured,
        onRetake = onRetake
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Step 4: Face Right Profile
// ---------------------------------------------------------------------------
@Composable
private fun Step4RightProfileLiveness(
  photoUri: String,
  onPhotoCaptured: (String) -> Unit,
  onRetake: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Turn Head: Right Profile (45°)",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
      Text(
        text = "Liveness check: Turn your head gently to the RIGHT so your right cheek and ear are visible.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8)
      )
      Spacer(modifier = Modifier.height(10.dp))

      CameraXCaptureView(
        stepTitle = "Right Profile Check",
        guideInstruction = "Turn head 45° to the RIGHT ➡️",
        overlayType = CaptureOverlayType.FACE_OVAL,
        preferFrontCamera = true,
        capturedPhotoUri = photoUri.ifBlank { null },
        onPhotoCaptured = onPhotoCaptured,
        onRetake = onRetake
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Step 5: Certificate of Business & Trade License
// ---------------------------------------------------------------------------
@Composable
private fun Step5BusinessCertificate(
  certType: BusinessCertificateType,
  onCertTypeChange: (BusinessCertificateType) -> Unit,
  businessName: String,
  onBusinessNameChange: (String) -> Unit,
  certNumber: String,
  onCertNumberChange: (String) -> Unit,
  photoUri: String,
  onPhotoCaptured: (String) -> Unit,
  onRetake: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Business Certificate / Registration Proof",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Verify your legitimate business entity to prevent impersonation.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8)
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BusinessCertificateType.entries.take(4).forEach { type ->
          val selected = type == certType
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selected) Color(0xFF2563EB).copy(alpha = 0.3f) else Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFF38BDF8) else Color.Transparent),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onCertTypeChange(type) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = if (selected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = type.displayName,
                color = if (selected) Color.White else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    }

    item {
      OutlinedTextField(
        value = businessName,
        onValueChange = onBusinessNameChange,
        label = { Text("Registered Business / Trade Name") },
        placeholder = { Text("e.g. Apex Electrical Solutions Pvt Ltd") },
        modifier = Modifier.fillMaxWidth().testTag("kyc_business_name_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = Color(0xFF38BDF8),
          unfocusedBorderColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(10.dp)
      )
    }

    item {
      OutlinedTextField(
        value = certNumber,
        onValueChange = onCertNumberChange,
        label = { Text("Registration / GST / License Number") },
        placeholder = { Text("e.g. 27ABCDE1234F1Z5 or UDYAM-MH-01-0012345") },
        modifier = Modifier.fillMaxWidth().testTag("kyc_cert_number_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = Color(0xFF38BDF8),
          unfocusedBorderColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(10.dp)
      )
    }

    item {
      Text(
        text = "Capture Certificate Document",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Capture clear full-page photo of certificate or upload PDF/JPG export.",
        fontSize = 11.sp,
        color = Color(0xFF94A3B8)
      )
      Spacer(modifier = Modifier.height(8.dp))

      CameraXCaptureView(
        stepTitle = "Business Certificate",
        guideInstruction = "Align document sheet within border",
        overlayType = CaptureOverlayType.DOCUMENT_SHEET,
        preferFrontCamera = false,
        capturedPhotoUri = photoUri.ifBlank { null },
        onPhotoCaptured = onPhotoCaptured,
        onRetake = onRetake
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Step 6: Review & Final Submission
// ---------------------------------------------------------------------------
@Composable
private fun Step6FinalReviewAndSubmit(
  idType: GovernmentIdType,
  idNumber: String,
  applicantName: String,
  govIdPhotoUri: String,
  frontFacePhotoUri: String,
  leftProfilePhotoUri: String,
  rightProfilePhotoUri: String,
  certType: BusinessCertificateType,
  businessName: String,
  certNumber: String,
  certPhotoUri: String,
  isDeclarationAccepted: Boolean,
  onDeclarationChange: (Boolean) -> Unit,
  onSubmit: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "VERIFICATION DOSSIER SUMMARY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF38BDF8)
          )
          Spacer(modifier = Modifier.height(10.dp))

          DetailRow("Applicant Name", applicantName)
          DetailRow("Government ID", "${idType.displayName} ($idNumber)")
          DetailRow("Business Name", businessName)
          DetailRow("Business Proof", "${certType.displayName} ($certNumber)")
        }
      }
    }

    // Photo thumbnails row
    item {
      Text(
        text = "Captured Biometric & Document Verification Files",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFCBD5E1)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ThumbnailBox(label = "ID Doc", uri = govIdPhotoUri, modifier = Modifier.weight(1f))
        ThumbnailBox(label = "Front Face", uri = frontFacePhotoUri, modifier = Modifier.weight(1f))
        ThumbnailBox(label = "Left Profile", uri = leftProfilePhotoUri, modifier = Modifier.weight(1f))
        ThumbnailBox(label = "Right Profile", uri = rightProfilePhotoUri, modifier = Modifier.weight(1f))
        ThumbnailBox(label = "Certificate", uri = certPhotoUri, modifier = Modifier.weight(1f))
      }
    }

    // Anti-fraud legal declaration
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF131D31))
          .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = isDeclarationAccepted,
          onCheckedChange = onDeclarationChange,
          colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB)),
          modifier = Modifier.testTag("kyc_declaration_checkbox")
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "I solemnly affirm that the submitted government identity and trade certificate belong to me and my registered business. I understand any counterfeit documents will result in immediate permanent account termination.",
          fontSize = 10.sp,
          color = Color(0xFFE2E8F0),
          lineHeight = 14.sp
        )
      }
    }

    item {
      Button(
        onClick = onSubmit,
        enabled = isDeclarationAccepted,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_kyc_dossier_button")
      ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Submit Dossier for BusyCardNet Approval",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
  }
}

@Composable
private fun ThumbnailBox(label: String, uri: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(60.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFF1E293B))
        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
    ) {
      if (uri.isNotBlank()) {
        AsyncImage(
          model = uri,
          contentDescription = label,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Icon(
          Icons.Default.Info,
          contentDescription = null,
          tint = Color(0xFF64748B),
          modifier = Modifier.align(Alignment.Center).size(20.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = label, fontSize = 9.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
  }
}
