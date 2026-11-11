package com.example.data.model

import com.example.data.local.ServiceRequestEntity

/**
 * Data model for Categorizing Service Requests in the Marketplace
 * Supports physical trades (Plumbing, Electrical, Carpentry), digital freelancing,
 * and professional advisory/consulting.
 */
enum class ServiceRequestCategory(
  val code: String,
  val displayName: String,
  val iconEmoji: String,
  val description: String,
  val popularTags: List<String>,
  val accentColorHex: Long = 0xFF0284C7
) {
  ALL(
    code = "ALL",
    displayName = "All Requests",
    iconEmoji = "✨",
    description = "Explore all active local and remote service requests across all categories",
    popularTags = listOf("Urgent", "Local (500m)", "Remote", "Verified"),
    accentColorHex = 0xFF38BDF8
  ),
  PLUMBING(
    code = "PLUMBING",
    displayName = "Plumbing",
    iconEmoji = "🔧",
    description = "Pipe repairs, bathroom fittings, water leakage, drainage, pumps & geysers",
    popularTags = listOf("Leakage Repair", "Tap Fitting", "Drain Clog", "Geyser Setup", "Pipe Replacement"),
    accentColorHex = 0xFF06B6D4
  ),
  FREELANCE(
    code = "FREELANCE",
    displayName = "Freelance",
    iconEmoji = "💻",
    description = "Digital design, mobile & web development, content writing, video & tech gigs",
    popularTags = listOf("Android Dev", "UI/UX Design", "Content Writing", "Video Editing", "Logo Design"),
    accentColorHex = 0xFF8B5CF6
  ),
  CONSULTING(
    code = "CONSULTING",
    displayName = "Consulting",
    iconEmoji = "📊",
    description = "Business advisory, legal advice, tax planning, financial audit & corporate strategy",
    popularTags = listOf("GST & Tax Audit", "Business Strategy", "Legal Contract", "Financial Planning", "Startup Advisory"),
    accentColorHex = 0xFFF59E0B
  ),
  ELECTRICAL(
    code = "ELECTRICAL",
    displayName = "Electrical",
    iconEmoji = "⚡",
    description = "Wiring, switchboard sparking, short-circuit, MCB tripping & appliance install",
    popularTags = listOf("Switchboard", "Short Circuit", "MCB Tripping", "Wiring", "Appliance Install"),
    accentColorHex = 0xFFEAB308
  ),
  CARPENTRY(
    code = "CARPENTRY",
    displayName = "Carpentry",
    iconEmoji = "🪚",
    description = "Woodwork, door lock repairs, modular furniture & custom fittings",
    popularTags = listOf("Door Lock Fix", "Furniture Repair", "Modular Kitchen", "Wood Polishing", "Cabinet Hinge"),
    accentColorHex = 0xFFD97706
  ),
  HOME_SERVICES(
    code = "HOME_SERVICES",
    displayName = "Home Maintenance",
    iconEmoji = "🏠",
    description = "Deep cleaning, painting, pest control, waterproofing & general repairs",
    popularTags = listOf("Deep Cleaning", "Wall Painting", "Pest Control", "Waterproofing", "Masonry"),
    accentColorHex = 0xFF10B981
  ),
  HEALTH_WELLNESS(
    code = "HEALTH_WELLNESS",
    displayName = "Health & Wellness",
    iconEmoji = "🩺",
    description = "Physiotherapy, home nursing, fitness training & dietary consults",
    popularTags = listOf("Physiotherapy", "Home Nursing", "Personal Trainer", "Diet Consultation", "Yoga"),
    accentColorHex = 0xFFEC4899
  );

  fun matches(request: ServiceRequestEntity): Boolean {
    if (this == ALL) return true
    if (request.category.equals(displayName, ignoreCase = true)) return true
    if (request.categoryCode.equals(code, ignoreCase = true)) return true
    
    // Mapping legacy or shorthand profession codes
    return when (this) {
      PLUMBING -> request.professionCode.equals("PLUM", ignoreCase = true) || request.tags.contains("Plumb", ignoreCase = true)
      ELECTRICAL -> request.professionCode.equals("ELEC", ignoreCase = true) || request.tags.contains("Elec", ignoreCase = true)
      CARPENTRY -> request.professionCode.equals("CARP", ignoreCase = true) || request.tags.contains("Carp", ignoreCase = true)
      FREELANCE -> request.professionCode.equals("TECH", ignoreCase = true) || 
                   request.professionCode.equals("FREE", ignoreCase = true) ||
                   request.professionCode.equals("DEV", ignoreCase = true) ||
                   request.tags.contains("Freelance", ignoreCase = true) ||
                   request.serviceMode.equals("REMOTE", ignoreCase = true) && !request.category.equals("Consulting", ignoreCase = true)
      CONSULTING -> request.professionCode.equals("CONS", ignoreCase = true) ||
                    request.professionCode.equals("LEGAL", ignoreCase = true) ||
                    request.professionCode.equals("CA", ignoreCase = true) ||
                    request.tags.contains("Consult", ignoreCase = true)
      HEALTH_WELLNESS -> request.professionCode.equals("DOC", ignoreCase = true) || request.professionCode.equals("HLTH", ignoreCase = true)
      HOME_SERVICES -> request.tags.contains("Cleaning", ignoreCase = true) || request.tags.contains("Painting", ignoreCase = true)
      ALL -> true
    }
  }

  companion object {
    fun fromCode(code: String?): ServiceRequestCategory {
      if (code == null) return ALL
      return entries.find { 
        it.code.equals(code, ignoreCase = true) || it.displayName.equals(code, ignoreCase = true) 
      } ?: when (code.uppercase()) {
        "PLUM" -> PLUMBING
        "ELEC" -> ELECTRICAL
        "CARP" -> CARPENTRY
        "FREE", "TECH", "DEV", "DESIGN" -> FREELANCE
        "CONS", "LEGAL", "CA", "FIN" -> CONSULTING
        "DOC", "HLTH" -> HEALTH_WELLNESS
        else -> ALL
      }
    }
  }
}

/**
 * Filter for request urgency level
 */
enum class RequestUrgencyFilter(
  val displayName: String,
  val badgeColorHex: Long
) {
  ALL("All Urgencies", 0xFF64748B),
  URGENT("⚡ Urgent (<2h)", 0xFFEF4444),
  STANDARD("Standard (Today)", 0xFF3B82F6),
  FLEXIBLE("Flexible", 0xFF10B981)
}

/**
 * Filter for on-site local vs remote digital services
 */
enum class ServiceDeliveryMode(
  val displayName: String,
  val iconEmoji: String
) {
  ALL("All Modes", "🌐"),
  ON_SITE("📍 Local On-Site", "📍"),
  REMOTE("💻 Remote Work", "💻")
}

/**
 * State object representing all active marketplace filters
 */
data class ServiceRequestFilterState(
  val category: ServiceRequestCategory = ServiceRequestCategory.ALL,
  val urgency: RequestUrgencyFilter = RequestUrgencyFilter.ALL,
  val mode: ServiceDeliveryMode = ServiceDeliveryMode.ALL,
  val searchQuery: String = "",
  val selectedTag: String? = null
) {
  val hasActiveFilters: Boolean
    get() = category != ServiceRequestCategory.ALL ||
            urgency != RequestUrgencyFilter.ALL ||
            mode != ServiceDeliveryMode.ALL ||
            searchQuery.isNotBlank() ||
            selectedTag != null
}
