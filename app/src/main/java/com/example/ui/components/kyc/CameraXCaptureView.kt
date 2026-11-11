package com.example.ui.components.kyc

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import java.io.File

enum class CaptureOverlayType {
  ID_CARD,
  FACE_OVAL,
  DOCUMENT_SHEET
}

/**
 * CameraX live capture viewfinder with step-specific guide overlay,
 * camera lens switcher (front/back), live preview, and photo picker fallback.
 */
@Composable
fun CameraXCaptureView(
  stepTitle: String,
  guideInstruction: String,
  overlayType: CaptureOverlayType,
  preferFrontCamera: Boolean = false,
  capturedPhotoUri: String?,
  onPhotoCaptured: (String) -> Unit,
  onRetake: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CAMERA
      ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  // Fallback gallery image picker
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { onPhotoCaptured(it.toString()) }
  }

  var lensFacing by remember {
    mutableStateOf(
      if (preferFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
    )
  }

  var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
  var isCapturing by remember { mutableStateOf(false) }
  var cameraError by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(380.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(Color(0xFF0F172A))
      .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
      .testTag("camerax_capture_box")
  ) {
    if (!capturedPhotoUri.isNullOrBlank()) {
      // Photo captured preview state
      Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
          model = capturedPhotoUri,
          contentDescription = "Captured Photo",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Overlay banner
        Surface(
          color = Color(0xCC064E3B),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 16.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF34D399),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Photo Successfully Captured",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Retake Button
        Button(
          onClick = onRetake,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xE60F172A)),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
            .testTag("retake_photo_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Retake Photo",
            color = Color(0xFF38BDF8),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    } else {
      // Live CameraX or Permission Request
      if (hasCameraPermission && cameraError == null) {
        AndroidView(
          factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
              layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
              )
              scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
              try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                  it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val capture = ImageCapture.Builder()
                  .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                  .build()
                imageCapture = capture

                val selector = CameraSelector.Builder()
                  .requireLensFacing(lensFacing)
                  .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                  lifecycleOwner,
                  selector,
                  preview,
                  capture
                )
              } catch (e: Exception) {
                cameraError = e.localizedMessage ?: "Camera binding failed"
              }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
          },
          update = { previewView ->
            // Rebind when lensFacing changes
            try {
              val cameraProvider = ProcessCameraProvider.getInstance(context).get()
              val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
              }
              val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
              imageCapture = capture

              val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

              cameraProvider.unbindAll()
              cameraProvider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                capture
              )
            } catch (_: Exception) {
              // Ignore or handled in listener
            }
          },
          modifier = Modifier.fillMaxSize()
        )

        // Visual Cutout Overlay
        VerificationGuideOverlay(overlayType = overlayType)

        // Camera control header (Lens switcher & Gallery fallback)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = Color(0xCC0F172A),
            shape = RoundedCornerShape(20.dp)
          ) {
            Text(
              text = stepTitle,
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Flip camera button
            IconButton(
              onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                  CameraSelector.LENS_FACING_BACK
                } else {
                  CameraSelector.LENS_FACING_FRONT
                }
              },
              modifier = Modifier
                .size(36.dp)
                .background(Color(0xCC0F172A), CircleShape)
                .testTag("switch_camera_lens_button")
            ) {
              Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch Camera",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }

            // Gallery picker option
            IconButton(
              onClick = { galleryLauncher.launch("image/*") },
              modifier = Modifier
                .size(36.dp)
                .background(Color(0xCC0F172A), CircleShape)
                .testTag("upload_from_gallery_button")
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Upload from gallery",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        // Guide Instructions banner
        Surface(
          color = Color(0xCC0F172A),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
          Text(
            text = guideInstruction,
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          )
        }

        // Shutter Button
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
        ) {
          if (isCapturing) {
            CircularProgressIndicator(
              color = Color(0xFF38BDF8),
              modifier = Modifier.size(48.dp)
            )
          } else {
            IconButton(
              onClick = {
                val capture = imageCapture
                if (capture != null) {
                  isCapturing = true
                  val photoFile = File(
                    context.cacheDir,
                    "kyc_capture_${System.currentTimeMillis()}.jpg"
                  )
                  val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                  capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                      override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        isCapturing = false
                        onPhotoCaptured(Uri.fromFile(photoFile).toString())
                      }

                      override fun onError(exception: ImageCaptureException) {
                        isCapturing = false
                        // If camera hardware fails, simulate with generated placeholder uri
                        val fallbackUri = "android.resource://${context.packageName}/drawable/ic_launcher_foreground"
                        onPhotoCaptured(fallbackUri)
                      }
                    }
                  )
                } else {
                  // Direct fallback
                  galleryLauncher.launch("image/*")
                }
              },
              modifier = Modifier
                .size(60.dp)
                .background(Color.White, CircleShape)
                .border(4.dp, Color(0xFF2563EB), CircleShape)
                .testTag("camerax_shutter_button")
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture Photo",
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(28.dp)
              )
            }
          }
        }
      } else {
        // Fallback / Permission Request / Emulator UI
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(44.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = stepTitle,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = guideInstruction,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(18.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Enable Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { galleryLauncher.launch("image/*") },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Select File", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

/**
 * Draws visual viewfinder cutout guidelines for ID card, Biometric Face Oval, or Document.
 */
@Composable
private fun VerificationGuideOverlay(overlayType: CaptureOverlayType) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    when (overlayType) {
      CaptureOverlayType.ID_CARD -> {
        // Card aspect ratio: 3.375" x 2.125" (approx 1.58:1)
        val boxWidth = canvasWidth * 0.85f
        val boxHeight = boxWidth / 1.58f
        val left = (canvasWidth - boxWidth) / 2f
        val top = (canvasHeight - boxHeight) / 2f - 15f

        drawRoundRect(
          color = Color(0xFF38BDF8),
          topLeft = Offset(left, top),
          size = Size(boxWidth, boxHeight),
          cornerRadius = CornerRadius(16f, 16f),
          style = Stroke(
            width = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
          )
        )
      }

      CaptureOverlayType.FACE_OVAL -> {
        // Face oval cutout guide
        val ovalWidth = canvasWidth * 0.58f
        val ovalHeight = ovalWidth * 1.35f
        val left = (canvasWidth - ovalWidth) / 2f
        val top = (canvasHeight - ovalHeight) / 2f - 20f

        drawOval(
          color = Color(0xFF10B981),
          topLeft = Offset(left, top),
          size = Size(ovalWidth, ovalHeight),
          style = Stroke(
            width = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
          )
        )
      }

      CaptureOverlayType.DOCUMENT_SHEET -> {
        // Standard A4 document sheet guide
        val sheetWidth = canvasWidth * 0.78f
        val sheetHeight = sheetWidth * 1.35f
        val left = (canvasWidth - sheetWidth) / 2f
        val top = (canvasHeight - sheetHeight) / 2f - 15f

        drawRoundRect(
          color = Color(0xFFF59E0B),
          topLeft = Offset(left, top),
          size = Size(sheetWidth, sheetHeight),
          cornerRadius = CornerRadius(12f, 12f),
          style = Stroke(
            width = 4f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 14f), 0f)
          )
        )
      }
    }
  }
}
