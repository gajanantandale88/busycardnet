package com.example.data.model

import com.example.data.local.DigitalCardEntity

/**
 * Visual themes for visiting card printing and physical rendering.
 */
enum class CardPrintTheme(
  val displayName: String,
  val primaryBgHex: String,
  val secondaryBgHex: String,
  val textColorHex: String,
  val accentColorHex: String,
  val isDark: Boolean
) {
  EXECUTIVE_MATTE(
    displayName = "Executive Matte",
    primaryBgHex = "#0F172A",
    secondaryBgHex = "#1E293B",
    textColorHex = "#F8FAFC",
    accentColorHex = "#38BDF8",
    isDark = true
  ),
  LUXURY_GOLD(
    displayName = "Luxury Gold Foil",
    primaryBgHex = "#18181B",
    secondaryBgHex = "#27272A",
    textColorHex = "#FEF08A",
    accentColorHex = "#EAB308",
    isDark = true
  ),
  MINIMAL_LIGHT(
    displayName = "Minimalist Clean",
    primaryBgHex = "#FFFFFF",
    secondaryBgHex = "#F1F5F9",
    textColorHex = "#0F172A",
    accentColorHex = "#2563EB",
    isDark = false
  ),
  CYBER_TECH(
    displayName = "Cyber Tech Navy",
    primaryBgHex = "#030712",
    secondaryBgHex = "#0F172A",
    textColorHex = "#F9FAFB",
    accentColorHex = "#06B6D4",
    isDark = true
  ),
  EMERALD_CORP(
    displayName = "Emerald Prestige",
    primaryBgHex = "#064E3B",
    secondaryBgHex = "#047857",
    textColorHex = "#ECFDF5",
    accentColorHex = "#10B981",
    isDark = true
  ),
  ROYAL_PURPLE(
    displayName = "Royal Velvet",
    primaryBgHex = "#2E1065",
    secondaryBgHex = "#3B0764",
    textColorHex = "#FAF5FF",
    accentColorHex = "#A855F7",
    isDark = true
  )
}

/**
 * Corner cutting styles for the physical visiting card.
 */
enum class CardCornerStyle(val displayName: String, val radiusDp: Int) {
  SQUARE("Classic Square (90°)", 0),
  ROUNDED_3MM("Rounded (3mm Credit Card)", 8),
  ROUNDED_6MM("Soft Curved (6mm)", 14)
}

/**
 * Card quality and paper finish options.
 */
data class CardQualityOption(
  val id: String,
  val name: String,
  val paperWeight: String,
  val finishDescription: String,
  val priceMultiplier: Double,
  val badgeText: String = ""
)

/**
 * Certified visiting card printer / manufacturer.
 */
data class CardManufacturer(
  val id: String,
  val name: String,
  val rating: Float,
  val deliveryDays: Int,
  val location: String,
  val basePrice100: Double,
  val description: String,
  val isOfficialBcn: Boolean = false,
  val specialty: String = "Offset Litho & UV"
)

/**
 * User customization options and toggles for elements to appear on the visiting card.
 */
data class CardPrintOptions(
  // Selected options toggles (Prompt: "selects what user want on the visiting card")
  val includePhoto: Boolean = true,
  val includeName: Boolean = true,
  val includeBcnId: Boolean = true,
  val includeBarcode: Boolean = true,
  val includeCompanyName: Boolean = true,
  val includeEmail: Boolean = true,
  val includeWebsite: Boolean = true,
  val includeFacebook: Boolean = true,
  val includeInstagram: Boolean = true,
  val includeWhatsApp: Boolean = true,
  val includeCompanyWords: Boolean = true,
  val includePhone: Boolean = true,
  val includeAddress: Boolean = true,

  // Editable content fields (Prompt: "add the content on the card")
  val photoUrl: String = "",
  val name: String = "",
  val bcnId: String = "",
  val barcodeData: String = "",
  val companyName: String = "",
  val companyWords: String = "",
  val email: String = "",
  val website: String = "",
  val facebook: String = "",
  val instagram: String = "",
  val whatsApp: String = "",
  val phone: String = "",
  val address: String = "",

  // Styling & layout
  val theme: CardPrintTheme = CardPrintTheme.EXECUTIVE_MATTE,
  val cornerStyle: CardCornerStyle = CardCornerStyle.ROUNDED_3MM,
  val cardOrientation: String = "LANDSCAPE", // LANDSCAPE or PORTRAIT
  val aiGeneratedTagline: String = "",
  val aiOptimizedLayout: Boolean = true
) {
  companion object {
    fun fromCard(card: DigitalCardEntity): CardPrintOptions {
      val cleanBcn = card.busyCardNetId.ifBlank { "IN-MH-MU-26-581ELEC347" }
      val defaultWords = if (card.about.isNotBlank()) {
        card.about.take(120)
      } else {
        "Excellence, Integrity & Trusted Professional Solutions"
      }

      val initialTheme = when (card.templateStyle) {
        "LUXURY_GOLD" -> CardPrintTheme.LUXURY_GOLD
        "MINIMAL_LIGHT" -> CardPrintTheme.MINIMAL_LIGHT
        "CREATIVE_CYAN", "MODERN_DARK" -> CardPrintTheme.CYBER_TECH
        "CORPORATE" -> CardPrintTheme.EXECUTIVE_MATTE
        else -> CardPrintTheme.EXECUTIVE_MATTE
      }

      return CardPrintOptions(
        includePhoto = card.photoUrl.isNotBlank(),
        includeName = true,
        includeBcnId = true,
        includeBarcode = true,
        includeCompanyName = true,
        includeEmail = card.email.isNotBlank(),
        includeWebsite = card.website.isNotBlank(),
        includeFacebook = card.facebook.isNotBlank(),
        includeInstagram = card.instagram.isNotBlank(),
        includeWhatsApp = card.whatsapp.isNotBlank(),
        includeCompanyWords = true,
        includePhone = card.phone.isNotBlank(),
        includeAddress = card.address.isNotBlank(),

        photoUrl = card.photoUrl,
        name = card.name.ifBlank { "Professional Member" },
        bcnId = cleanBcn,
        barcodeData = cleanBcn,
        companyName = card.businessName.ifBlank { card.name },
        companyWords = defaultWords,
        email = card.email.ifBlank { "contact@busycardnet.in" },
        website = card.website.ifBlank { "www.busycardnet.in/card/${cleanBcn}" },
        facebook = card.facebook.ifBlank { "facebook.com/${card.name.replace(" ", "").lowercase()}" },
        instagram = card.instagram.ifBlank { "@${card.name.replace(" ", "").lowercase()}" },
        whatsApp = card.whatsapp.ifBlank { card.phone.ifBlank { "+91 98201 23456" } },
        phone = card.phone.ifBlank { "+91 98201 23456" },
        address = card.address.ifBlank { "Andheri East, Mumbai, Maharashtra 400069" },
        theme = initialTheme,
        cornerStyle = CardCornerStyle.ROUNDED_3MM
      )
    }
  }
}

/**
 * Shipping and delivery address model.
 */
data class CardDeliveryAddress(
  val recipientName: String = "",
  val phone: String = "",
  val streetAddress: String = "",
  val locality: String = "",
  val city: String = "Mumbai",
  val state: String = "Maharashtra",
  val pinCode: String = "400001",
  val deliveryNotes: String = "",
  val isExpressCourier: Boolean = false
) {
  val fullAddressLine: String
    get() = buildString {
      append(streetAddress.trim())
      if (locality.isNotBlank()) append(", ").append(locality.trim())
      if (city.isNotBlank()) append(", ").append(city.trim())
      if (state.isNotBlank()) append(", ").append(state.trim())
      if (pinCode.isNotBlank()) append(" - ").append(pinCode.trim())
    }
}

/**
 * A completed or saved physical card print order.
 */
data class CardPrintOrder(
  val orderId: String,
  val cardId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val options: CardPrintOptions,
  val manufacturer: CardManufacturer,
  val quality: CardQualityOption,
  val cornerStyle: CardCornerStyle,
  val quantity: Int,
  val basePrice: Double,
  val deliveryFee: Double,
  val totalPrice: Double,
  val deliveryAddress: CardDeliveryAddress,
  val estimatedDeliveryDays: Int,
  val trackingStatus: String = "ORDER_CONFIRMED" // ORDER_CONFIRMED, PRINTING_QUEUE, IN_PRODUCTION, SHIPPED, DELIVERED
)
