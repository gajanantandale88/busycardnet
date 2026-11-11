package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * AI Shop Notification & Ad Creative Generator.
 * Powered by Gemini AI (REST API via OkHttp) and high-impact local copywriting synthesis
 * and banner image generation for DCN shopkeepers and cardholders.
 */
object ShopNotificationAiEngine {

  private val httpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .build()
  }

  data class AiNotificationSuggestion(
    val headline: String,
    val announcementBody: String,
    val callToAction: String,
    val recommendedTags: List<String>,
    val bannerTheme: String,
    val fallbackBannerBitmap: Bitmap
  )

  /**
   * Generates a complete AI-crafted shop announcement, discount deal, news flash or event
   * based on the motive/topic provided by the shopkeeper.
   */
  suspend fun generateShopAnnouncement(
    shopName: String,
    dcnId: String,
    categoryOrProfession: String,
    motivePrompt: String,
    urgencyLevel: String = "High",
    discountPercent: String = "20%"
  ): AiNotificationSuggestion = withContext(Dispatchers.IO) {
    val cleanShop = shopName.ifBlank { "Local Business & Retail" }
    val motive = motivePrompt.ifBlank { "Special festival discount and new arrivals available at our shop" }
    
    var aiHeadline = ""
    var aiBody = ""
    var aiCta = "WHATSAPP"

    // 1. Attempt Gemini 2.5 Flash if valid API key is present
    try {
      val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
      if (apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true)) {
        val aiPrompt = """
          You are an expert digital marketing assistant for Indian local shopkeepers and professionals with a Digital Card Network (DCN) identity.
          Shop Name: $cleanShop
          DCN Card ID: $dcnId
          Category: $categoryOrProfession
          Shopkeeper's Motive: $motive
          Discount/Offer: $discountPercent
          
          Write an enticing, professional, and punchy shop notification in 3 parts:
          HEADLINE: (max 8 words, exciting, with emojis)
          BODY: (2 to 3 sentences with clear offer, timing, and invitation to visit or message on WhatsApp)
          CTA: (either WHATSAPP, CALL, or DIRECTIONS)
        """.trimIndent()

        val jsonBody = JSONObject().apply {
          val contents = JSONArray().apply {
            val contentObj = JSONObject().apply {
              val parts = JSONArray().apply {
                put(JSONObject().apply { put("text", aiPrompt) })
              }
              put("parts", parts)
            }
            put(contentObj)
          }
          put("contents", contents)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
          .url(url)
          .post(requestBody)
          .build()

        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful) {
          val respBody = response.body?.string() ?: ""
          if (respBody.isNotBlank()) {
            val root = JSONObject(respBody)
            val candidates = root.optJSONArray("candidates")
            val candidate0 = candidates?.optJSONObject(0)
            val content = candidate0?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isNotBlank()) {
              val lines = text.lines()
              for (line in lines) {
                val trimmed = line.trim()
                when {
                  trimmed.startsWith("HEADLINE:", ignoreCase = true) -> {
                    aiHeadline = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                  }
                  trimmed.startsWith("BODY:", ignoreCase = true) -> {
                    aiBody = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                  }
                  trimmed.startsWith("CTA:", ignoreCase = true) -> {
                    val ctaStr = trimmed.substringAfter(":").trim().uppercase()
                    if (ctaStr.contains("CALL")) aiCta = "CALL"
                    else if (ctaStr.contains("DIR") || ctaStr.contains("MAP")) aiCta = "DIRECTIONS"
                    else aiCta = "WHATSAPP"
                  }
                }
              }
            }
          }
        }
      }
    } catch (_: Exception) {
      // Graceful fallback to deterministic high-converting copywriting templates
    }

    if (aiHeadline.isBlank()) {
      aiHeadline = when {
        motive.contains("sale", ignoreCase = true) || motive.contains("off", ignoreCase = true) || motive.contains("discount", ignoreCase = true) ->
          "🎉 Mega Festival Sale! Flat $discountPercent Off at $cleanShop!"
        motive.contains("new", ignoreCase = true) || motive.contains("arrival", ignoreCase = true) || motive.contains("stock", ignoreCase = true) ->
          "✨ Fresh Arrivals Just In! Exclusive Collection at $cleanShop"
        motive.contains("urgent", ignoreCase = true) || motive.contains("emergency", ignoreCase = true) || motive.contains("repair", ignoreCase = true) ->
          "⚡ Express On-Demand Service Available in 30 Mins — $cleanShop"
        else ->
          "📢 Important Shop Announcement from $cleanShop (DCN: ${dcnId.takeLast(10)})"
      }
    }

    if (aiBody.isBlank()) {
      aiBody = "Exciting news for all neighbors and cardholders! $motive. Visit our store today or tap below to claim your personalized discount voucher directly on WhatsApp. 100% genuine quality & express local doorstep delivery!"
    }

    val tags = listOf(
      "ShopNotification",
      categoryOrProfession.replace(" ", ""),
      "DCNVerified",
      "SpecialOffer"
    )

    val bannerBitmap = generateAiBannerArt(
      shopName = cleanShop,
      headline = aiHeadline,
      category = categoryOrProfession
    )

    AiNotificationSuggestion(
      headline = aiHeadline,
      announcementBody = aiBody,
      callToAction = aiCta,
      recommendedTags = tags,
      bannerTheme = "Vibrant Modern Poster",
      fallbackBannerBitmap = bannerBitmap
    )
  }

  /**
   * Generates a high-resolution, beautifully branded marketing poster bitmap for the notification.
   * Renders modern gradients, badges, shop typography and AI sparkles.
   */
  fun generateAiBannerArt(
    shopName: String,
    headline: String,
    category: String,
    badgeText: String = "AI SMART AD • DCN VERIFIED"
  ): Bitmap {
    val width = 800
    val height = 460
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Vibrant background gradient
    val isWarm = category.contains("food", ignoreCase = true) || headline.contains("sale", ignoreCase = true)
    val bgColor1 = if (isWarm) Color.rgb(249, 115, 22) else Color.rgb(37, 99, 235)
    val bgColor2 = if (isWarm) Color.rgb(180, 83, 9) else Color.rgb(15, 23, 42)

    val bgPaint = Paint().apply {
      isAntiAlias = true
      shader = android.graphics.LinearGradient(
        0f, 0f, width.toFloat(), height.toFloat(),
        bgColor1, bgColor2,
        android.graphics.Shader.TileMode.CLAMP
      )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Decorative geometric circles for visual depth
    val decorPaint = Paint().apply {
      isAntiAlias = true
      color = Color.WHITE
      alpha = 25
      style = Paint.Style.FILL
    }
    canvas.drawCircle(width * 0.85f, height * 0.2f, 160f, decorPaint)
    canvas.drawCircle(width * 0.1f, height * 0.8f, 130f, decorPaint)

    // Top Badge Pill
    val badgeBg = Paint().apply {
      isAntiAlias = true
      color = Color.argb(190, 0, 0, 0)
      style = Paint.Style.FILL
    }
    val badgeRect = RectF(40f, 36f, 380f, 76f)
    canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBg)

    val badgeTextPaint = Paint().apply {
      isAntiAlias = true
      color = Color.rgb(251, 191, 36)
      textSize = 18f
      typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    canvas.drawText("✦ $badgeText", 56f, 63f, badgeTextPaint)

    // Shop Name
    val shopPaint = Paint().apply {
      isAntiAlias = true
      color = Color.rgb(224, 242, 254)
      textSize = 28f
      typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    canvas.drawText(shopName.uppercase(), 40f, 130f, shopPaint)

    // Headline Text (word wrap)
    val headlinePaint = Paint().apply {
      isAntiAlias = true
      color = Color.WHITE
      textSize = 38f
      typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
      setShadowLayer(6f, 2f, 2f, Color.argb(120, 0, 0, 0))
    }

    val words = headline.split(" ")
    var line = ""
    var yPos = 185f
    for (word in words) {
      val testLine = if (line.isEmpty()) word else "$line $word"
      if (headlinePaint.measureText(testLine) > (width - 80)) {
        canvas.drawText(line, 40f, yPos, headlinePaint)
        line = word
        yPos += 46f
        if (yPos > 320f) break
      } else {
        line = testLine
      }
    }
    if (line.isNotEmpty() && yPos <= 330f) {
      canvas.drawText(line, 40f, yPos, headlinePaint)
    }

    // Bottom Action Pill
    val ctaPillPaint = Paint().apply {
      isAntiAlias = true
      color = Color.rgb(16, 185, 129)
      style = Paint.Style.FILL
    }
    val ctaRect = RectF(40f, height - 90f, 340f, height - 40f)
    canvas.drawRoundRect(ctaRect, 16f, 16f, ctaPillPaint)

    val ctaTextPaint = Paint().apply {
      isAntiAlias = true
      color = Color.WHITE
      textSize = 20f
      typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    canvas.drawText("Tap Below to Connect 💬", 56f, height - 58f, ctaTextPaint)

    // Verified DCN Guarantee stamp at bottom right
    val stampPaint = Paint().apply {
      isAntiAlias = true
      color = Color.argb(200, 255, 255, 255)
      textSize = 17f
      typeface = android.graphics.Typeface.MONOSPACE
    }
    canvas.drawText("ONLINE NOTIFICATION HUB", width - 290f, height - 58f, stampPaint)

    return bitmap
  }
}
