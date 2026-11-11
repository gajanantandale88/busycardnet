package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.local.DigitalCardEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Utility for generating, styling, rendering, and sharing unique QR codes
 * for user digital visiting cards that encode their Firebase Firestore profile URL.
 */
object VisitingCardQrGenerator {

  private const val TAG = "VisitingCardQrGen"
  const val DEFAULT_FIRESTORE_HOST = "https://busycardnet.web.app"

  enum class QrUrlType(val title: String, val subtitle: String, val badge: String) {
    FIRESTORE_WEB_PROFILE(
      title = "Cloud Web Profile",
      subtitle = "Direct web profile hosted with Firebase Firestore data",
      badge = "RECOMMENDED"
    ),
    FIRESTORE_REST_ENDPOINT(
      title = "Firestore Cloud REST",
      subtitle = "Direct JSON document resource on Google Cloud Firestore",
      badge = "DEVELOPER"
    ),
    UNIVERSAL_DEEPLINK(
      title = "Universal App Link",
      subtitle = "Instant app open or smart web fallback for visiting card",
      badge = "FAST"
    ),
    VCARD_WITH_FIRESTORE(
      title = "vCard + Cloud URL",
      subtitle = "One-tap phone contacts save embedded with Firestore URL",
      badge = "CONTACTS"
    )
  }

  enum class QrRenderStyle(val label: String) {
    CLASSIC_SQUARE("Classic Sharp"),
    ROUNDED_SQUIRCLE("Rounded Squircle"),
    CIRCULAR_DOTS("Circular Dots")
  }

  enum class QrCenterBranding(val label: String) {
    BCN_MONOGRAM("BCN Emblem"),
    USER_INITIAL("User Initial"),
    VERIFIED_BADGE("Verified Shield"),
    NONE("Clean / None")
  }

  /**
   * Generates the encoded content string based on the chosen URL type and card entity.
   */
  fun buildEncodedPayload(card: DigitalCardEntity, type: QrUrlType): String {
    val bcnId = card.busyCardNetId.ifBlank { "IN-MH-MU-26-581ELEC347" }
    return when (type) {
      QrUrlType.FIRESTORE_WEB_PROFILE -> {
        "$DEFAULT_FIRESTORE_HOST/profile/$bcnId"
      }
      QrUrlType.FIRESTORE_REST_ENDPOINT -> {
        "https://firestore.googleapis.com/v1/projects/busycardnet-app/databases/(default)/documents/local_businesses/$bcnId"
      }
      QrUrlType.UNIVERSAL_DEEPLINK -> {
        "https://busycard.net/c/$bcnId?cloud=firestore&v=26"
      }
      QrUrlType.VCARD_WITH_FIRESTORE -> {
        buildString {
          appendLine("BEGIN:VCARD")
          appendLine("VERSION:3.0")
          appendLine("FN:${card.name}")
          if (card.businessName.isNotBlank()) appendLine("ORG:${card.businessName}")
          if (card.designation.isNotBlank()) appendLine("TITLE:${card.designation}")
          if (card.phone.isNotBlank()) appendLine("TEL;TYPE=CELL:${card.phone}")
          if (card.whatsapp.isNotBlank()) appendLine("TEL;TYPE=WORK:${card.whatsapp}")
          if (card.email.isNotBlank()) appendLine("EMAIL;TYPE=INTERNET:${card.email}")
          appendLine("URL:$DEFAULT_FIRESTORE_HOST/profile/$bcnId")
          if (card.website.isNotBlank()) appendLine("X-WEBSITE:${card.website}")
          if (card.address.isNotBlank()) appendLine("ADR;TYPE=WORK:;;${card.address};;;;")
          appendLine("NOTE:BusyCardNet ID: $bcnId | Cloud Firestore Profile")
          appendLine("END:VCARD")
        }
      }
    }
  }

  /**
   * Generates a high-resolution, camera-readable QR Code Bitmap using ZXing
   * with custom dot styles, foreground color, and optional center logo branding.
   */
  fun generateQrBitmap(
    content: String,
    size: Int = 600,
    fgColor: Int = android.graphics.Color.parseColor("#0F172A"),
    bgColor: Int = android.graphics.Color.WHITE,
    style: QrRenderStyle = QrRenderStyle.ROUNDED_SQUIRCLE,
    branding: QrCenterBranding = QrCenterBranding.BCN_MONOGRAM,
    card: DigitalCardEntity? = null
  ): Bitmap {
    val hints = hashMapOf<EncodeHintType, Any>(
      EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
      EncodeHintType.MARGIN to 1,
      EncodeHintType.CHARACTER_SET to "UTF-8"
    )

    val bitMatrix = try {
      QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    } catch (e: Exception) {
      Log.e(TAG, "Error encoding QR matrix: ${e.message}")
      return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        eraseColor(bgColor)
      }
    }

    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(bgColor)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = fgColor
      this.style = Paint.Style.FILL
    }

    val cellW = width.toFloat() / bitMatrix.width
    val cellH = height.toFloat() / bitMatrix.height

    // Calculate center area to leave room if center branding is enabled
    val centerModuleRadius = when (branding) {
      QrCenterBranding.NONE -> 0
      else -> (bitMatrix.width * 0.14f).toInt()
    }
    val midX = bitMatrix.width / 2
    val midY = bitMatrix.height / 2

    for (x in 0 until bitMatrix.width) {
      for (y in 0 until bitMatrix.height) {
        if (bitMatrix.get(x, y)) {
          // If inside center logo safe area, skip module drawing
          if (branding != QrCenterBranding.NONE &&
            kotlin.math.abs(x - midX) <= centerModuleRadius &&
            kotlin.math.abs(y - midY) <= centerModuleRadius
          ) {
            continue
          }

          // Finder patterns corners
          val isFinder = (x < 7 && y < 7) ||
              (x < 7 && y >= bitMatrix.height - 7) ||
              (x >= bitMatrix.width - 7 && y < 7)

          val left = x * cellW
          val top = y * cellH
          val right = left + cellW
          val bottom = top + cellH

          when {
            isFinder -> {
              // Finder patterns are kept crisp with slight corner softening
              canvas.drawRoundRect(
                RectF(left + 0.5f, top + 0.5f, right - 0.5f, bottom - 0.5f),
                cellW * 0.25f,
                cellH * 0.25f,
                paint
              )
            }
            style == QrRenderStyle.CIRCULAR_DOTS -> {
              val centerX = left + cellW / 2f
              val centerY = top + cellH / 2f
              val radius = (cellW / 2f) * 0.88f
              canvas.drawCircle(centerX, centerY, radius, paint)
            }
            style == QrRenderStyle.ROUNDED_SQUIRCLE -> {
              canvas.drawRoundRect(
                RectF(left + 0.6f, top + 0.6f, right - 0.6f, bottom - 0.6f),
                cellW * 0.35f,
                cellH * 0.35f,
                paint
              )
            }
            else -> { // CLASSIC_SQUARE
              canvas.drawRect(left, top, right, bottom, paint)
            }
          }
        }
      }
    }

    // Draw center branding emblem overlay if requested
    if (branding != QrCenterBranding.NONE) {
      val centerPixelX = width / 2f
      val centerPixelY = height / 2f
      val badgeSize = (centerModuleRadius * 2 + 1) * cellW * 1.05f
      val badgeRect = RectF(
        centerPixelX - badgeSize / 2f,
        centerPixelY - badgeSize / 2f,
        centerPixelX + badgeSize / 2f,
        centerPixelY + badgeSize / 2f
      )

      // Badge background
      val bgBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        this.style = Paint.Style.FILL
      }
      canvas.drawRoundRect(badgeRect, badgeSize * 0.28f, badgeSize * 0.28f, bgBadgePaint)

      // Badge inner colored pill
      val innerBadgeRect = RectF(
        badgeRect.left + 5f,
        badgeRect.top + 5f,
        badgeRect.right - 5f,
        badgeRect.bottom - 5f
      )
      val innerBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fgColor
        this.style = Paint.Style.FILL
      }
      canvas.drawRoundRect(innerBadgeRect, badgeSize * 0.24f, badgeSize * 0.24f, innerBadgePaint)

      // Text/Icon inside badge
      val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
      }

      when (branding) {
        QrCenterBranding.BCN_MONOGRAM -> {
          textPaint.textSize = badgeSize * 0.34f
          val yPos = centerPixelY - ((textPaint.descent() + textPaint.ascent()) / 2f)
          canvas.drawText("BCN", centerPixelX, yPos, textPaint)
        }
        QrCenterBranding.USER_INITIAL -> {
          val initial = card?.name?.trim()?.firstOrNull()?.uppercase() ?: "B"
          textPaint.textSize = badgeSize * 0.45f
          val yPos = centerPixelY - ((textPaint.descent() + textPaint.ascent()) / 2f)
          canvas.drawText(initial, centerPixelX, yPos, textPaint)
        }
        QrCenterBranding.VERIFIED_BADGE -> {
          textPaint.textSize = badgeSize * 0.38f
          val yPos = centerPixelY - ((textPaint.descent() + textPaint.ascent()) / 2f)
          canvas.drawText("✓", centerPixelX, yPos, textPaint)
        }
        QrCenterBranding.NONE -> {}
      }
    }

    return bitmap
  }

  /**
   * Writes the QR code Bitmap to application cache and returns its content URI via FileProvider.
   */
  fun saveQrToCache(context: Context, bitmap: Bitmap, bcnId: String): Uri? {
    return try {
      val qrDir = File(context.cacheDir, "qr_codes")
      if (!qrDir.exists()) qrDir.mkdirs()

      val cleanId = bcnId.replace(Regex("[^a-zA-Z0-9-]"), "_")
      val file = File(qrDir, "BCN_QR_${cleanId}.png")

      FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
      }

      val authority = "${context.packageName}.fileprovider"
      FileProvider.getUriForFile(context, authority, file)
    } catch (e: Exception) {
      Log.e(TAG, "Failed saving QR to cache: ${e.message}", e)
      null
    }
  }

  /**
   * Saves the generated QR Bitmap directly to the device MediaStore / Pictures directory.
   */
  fun saveQrToGallery(context: Context, bitmap: Bitmap, card: DigitalCardEntity): Boolean {
    val bcnId = card.busyCardNetId.ifBlank { "IN-MH-MU-26-581ELEC347" }
    val filename = "BCN_VisitingCard_${bcnId.replace("-", "_")}.png"

    return try {
      val outputStream: OutputStream?
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
          put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BusyCardNet")
        }
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        outputStream = imageUri?.let { resolver.openOutputStream(it) }
      } else {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val bcnDir = File(picturesDir, "BusyCardNet")
        if (!bcnDir.exists()) bcnDir.mkdirs()
        val file = File(bcnDir, filename)
        outputStream = FileOutputStream(file)
      }

      outputStream?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
      }
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error saving QR code to gallery: ${e.message}", e)
      false
    }
  }

  /**
   * Triggers Android's native Share Sheet with both the QR image file and the Firestore Profile link.
   */
  fun shareQrWithImageAndLink(
    context: Context,
    card: DigitalCardEntity,
    bitmap: Bitmap,
    url: String
  ) {
    val bcnId = card.busyCardNetId.ifBlank { "IN-MH-MU-26-581ELEC347" }
    val imageUri = saveQrToCache(context, bitmap, bcnId)

    val shareText = buildString {
      appendLine("🤝 Connect with ${card.name} on BusyCardNet!")
      if (card.businessName.isNotBlank()) appendLine("🏢 ${card.businessName} (${card.designation})")
      appendLine("📍 ${card.address}")
      appendLine("🆔 BusyCardNet ID: $bcnId")
      appendLine()
      appendLine("🌐 View Cloud Firestore Profile & Digital Visiting Card:")
      appendLine(url)
      appendLine()
      append("Scan the attached QR code to instantly import contact details & verify on Google Cloud.")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      if (imageUri != null) {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      } else {
        type = "text/plain"
      }
      putExtra(Intent.EXTRA_SUBJECT, "BusyCardNet Visiting Card: ${card.name} ($bcnId)")
      putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, "Share Firestore QR Visiting Card"))
  }

  /**
   * Opens WhatsApp with a personalized networking invite containing the Firestore profile link.
   */
  fun shareViaWhatsApp(
    context: Context,
    card: DigitalCardEntity,
    url: String,
    targetPhone: String? = null
  ) {
    val bcnId = card.busyCardNetId.ifBlank { "IN-MH-MU-26-581ELEC347" }
    val msg = buildString {
      appendLine("Hello! 👋 Connect with me on BusyCardNet.")
      appendLine("*${card.name}* | *${card.businessName}*")
      if (card.designation.isNotBlank()) appendLine("_${card.designation}_")
      appendLine()
      appendLine("📲 *Live Cloud Firestore Profile & Digital Card:*")
      appendLine(url)
      appendLine()
      appendLine("🆔 *BCN ID:* `$bcnId`")
      append("Verified local service provider on BusyCardNet network.")
    }

    val uriString = if (!targetPhone.isNullOrBlank()) {
      val cleanPhone = targetPhone.replace("+", "").replace(" ", "").replace("-", "")
      "https://wa.me/$cleanPhone?text=${Uri.encode(msg)}"
    } else {
      "https://api.whatsapp.com/send?text=${Uri.encode(msg)}"
    }

    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "WhatsApp not available: ${e.message}")
    }
  }
}
