package com.example.util

import com.example.data.model.CardCornerStyle
import com.example.data.model.CardPrintOptions
import com.example.data.model.CardPrintTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI Visiting Card Generator & Copywriting Engine.
 * Automatically crafts impactful company words (slogans, taglines, elevator pitches)
 * and harmonizes card color palettes, typography, and element layouts.
 */
object VisitingCardAiGenerator {

  data class AiCardDesignSuggestion(
    val recommendedTheme: CardPrintTheme,
    val recommendedCornerStyle: CardCornerStyle,
    val primaryCompanyWords: String,
    val alternativeSlogans: List<String>,
    val suggestedAccentHex: String,
    val designSummary: String
  )

  /**
   * Generates tailored company words and card design parameters based on the business
   * details and optional user guidance prompt.
   */
  suspend fun generateCardDesign(
    businessName: String,
    ownerName: String,
    professionOrCategory: String,
    customPrompt: String = ""
  ): AiCardDesignSuggestion = withContext(Dispatchers.Default) {
    val bName = businessName.ifBlank { ownerName.ifBlank { "Enterprise Solutions" } }
    val pCategory = professionOrCategory.lowercase()
    val promptLower = customPrompt.lowercase()

    // 1. Detect Category Profile
    val (slogans, theme, accent) = when {
      promptLower.contains("gold") || promptLower.contains("luxury") || promptLower.contains("vip") || promptLower.contains("prestige") -> {
        Triple(
          listOf(
            "Curating Extraordinary Distinction & Bespoke Luxury for $bName.",
            "Refined Heritage, Uncompromising Standards & Timeless Craftsmanship.",
            "Where Prestige Meets Flawless Execution.",
            "Elevating Every Detail with Masterful Distinction."
          ),
          CardPrintTheme.LUXURY_GOLD,
          "#EAB308"
        )
      }
      promptLower.contains("tech") || promptLower.contains("cyber") || promptLower.contains("cloud") || promptLower.contains("modern") || pCategory.contains("it") || pCategory.contains("software") -> {
        Triple(
          listOf(
            "Architecting Next-Generation Digital Systems & Cloud Intelligence.",
            "Engineering Resilient Solutions for Tomorrow's Scalable Enterprises.",
            "Smart Code, Scalable Infrastructure, Transformative Results.",
            "Empowering Businesses through High-Speed Technology & Innovation."
          ),
          CardPrintTheme.CYBER_TECH,
          "#06B6D4"
        )
      }
      promptLower.contains("clean") || promptLower.contains("minimal") || promptLower.contains("white") || promptLower.contains("simple") -> {
        Triple(
          listOf(
            "Purity in Design, Absolute Precision in Execution.",
            "Clarity, Purpose, and Unwavering Commitment to Quality.",
            "Less Noise, More Impact: Intentional Solutions by $bName.",
            "Thoughtfully Designed Solutions Built to Last."
          ),
          CardPrintTheme.MINIMAL_LIGHT,
          "#2563EB"
        )
      }
      pCategory.contains("elec") || pCategory.contains("electric") || pCategory.contains("power") || pCategory.contains("solar") -> {
        Triple(
          listOf(
            "Powering Reliable Electrical Solutions & Certified Safety 24/7.",
            "Precision Wiring, Industrial Automation & Dependable Power Systems.",
            "Zero-Failure Electrical Engineering for Homes & Commercial Facilities.",
            "Energizing Your Tomorrow with Safe, Certified Electrical Mastery."
          ),
          CardPrintTheme.EXECUTIVE_MATTE,
          "#F59E0B"
        )
      }
      pCategory.contains("plum") || pCategory.contains("water") || pCategory.contains("pipe") -> {
        Triple(
          listOf(
            "Mastering Hydro-Engineering & Flawless Plumbing Infrastructure.",
            "Rapid Response Leak Detection, High-Pressure Solutions & Clean Living.",
            "Reliable Flow, Certified Craftsmanship, 100% Guaranteed Reliability.",
            "Modern Piping & Sustainable Water Solutions for Urban Spaces."
          ),
          CardPrintTheme.EXECUTIVE_MATTE,
          "#38BDF8"
        )
      }
      pCategory.contains("doc") || pCategory.contains("health") || pCategory.contains("med") || pCategory.contains("care") -> {
        Triple(
          listOf(
            "Dedicated to Compassionate Healing, Advanced Science & Wellness.",
            "Your Trusted Partner in Clinical Excellence & Holistic Healthcare.",
            "Empowering Healthier Lives through Evidence-Based Patient Care.",
            "Restoring Vitality with Empathy, Innovation & Precision Medicine."
          ),
          CardPrintTheme.EMERALD_CORP,
          "#10B981"
        )
      }
      pCategory.contains("law") || pCategory.contains("advoc") || pCategory.contains("legal") || pCategory.contains("audit") || pCategory.contains("ca") -> {
        Triple(
          listOf(
            "Strategic Legal Counsel, Staunch Advocacy & Trusted Integrity.",
            "Safeguarding Your Rights, Assets & Commercial Transactions.",
            "Clarity in Law, Rigor in Defense, Excellence in Strategy.",
            "Corporate Governance & Principled Representation You Can Bank On."
          ),
          CardPrintTheme.ROYAL_PURPLE,
          "#C084FC"
        )
      }
      pCategory.contains("build") || pCategory.contains("construct") || pCategory.contains("arch") || pCategory.contains("interior") -> {
        Triple(
          listOf(
            "Transforming Visionary Architectural Blueprints into Enduring Structures.",
            "Bespoke Spatial Design, Sustainable Building & Structural Perfection.",
            "Crafting Timeless Living Spaces with Engineered Balance.",
            "Building Foundations of Trust, Quality & Modern Urban Aesthetics."
          ),
          CardPrintTheme.EXECUTIVE_MATTE,
          "#F97316"
        )
      }
      else -> {
        Triple(
          listOf(
            "Excellence, Integrity & Trusted Professional Solutions at $bName.",
            "Delivering Measurable Impact with Uncompromising Quality.",
            "Your Reliable Partner for Dedicated Growth, Value & Innovation.",
            "Passionate Craftsmanship, Rapid Response, Proven Results."
          ),
          CardPrintTheme.EXECUTIVE_MATTE,
          "#38BDF8"
        )
      }
    }

    AiCardDesignSuggestion(
      recommendedTheme = theme,
      recommendedCornerStyle = CardCornerStyle.ROUNDED_3MM,
      primaryCompanyWords = slogans.first(),
      alternativeSlogans = slogans,
      suggestedAccentHex = accent,
      designSummary = "AI selected ${theme.displayName} with high-contrast hierarchy tailored for $bName."
    )
  }

  /**
   * Applies the AI suggestion onto existing card print options.
   */
  fun applySuggestion(
    currentOptions: CardPrintOptions,
    suggestion: AiCardDesignSuggestion,
    selectedSloganIndex: Int = 0
  ): CardPrintOptions {
    val chosenWords = suggestion.alternativeSlogans.getOrElse(selectedSloganIndex) {
      suggestion.primaryCompanyWords
    }

    return currentOptions.copy(
      theme = suggestion.recommendedTheme,
      cornerStyle = suggestion.recommendedCornerStyle,
      companyWords = chosenWords,
      aiGeneratedTagline = chosenWords,
      includeCompanyWords = true,
      aiOptimizedLayout = true
    )
  }
}
