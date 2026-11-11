package com.example.data.model

import java.util.UUID

/**
 * Vicinity distance tiers for service request notifications.
 * Covers: 500m (Immediate), 1000m (1 km), 2000m (2 km), 5000m (5 km), 10000m (10 km), +10000m (Regional).
 */
enum class ProximityVicinityTier(
  val radiusMeters: Int,
  val label: String,
  val shortLabel: String,
  val description: String
) {
  TIER_500M(500, "500m Immediate", "500m", "Instant hyper-local neighborhood (within 500m)"),
  TIER_1000M(1000, "1000m (1 km)", "1000m", "Local ward / residential sector (within 1 km)"),
  TIER_2000M(2000, "2000m (2 km)", "2000m", "Neighborhood & adjacent areas (within 2 km)"),
  TIER_5000M(5000, "5000m (5 km)", "5000m", "Entire town / commercial district (within 5 km)"),
  TIER_10000M(10000, "10000m (10 km)", "10000m", "Suburban & metro corridor (within 10 km)"),
  TIER_PLUS_10000M(999999, "+10000m Regional", "+10000m", "Regional, metro-wide & interstate (+10 km)");

  companion object {
    fun fromRadius(meters: Int): ProximityVicinityTier {
      return when {
        meters <= 500 -> TIER_500M
        meters <= 1000 -> TIER_1000M
        meters <= 2000 -> TIER_2000M
        meters <= 5000 -> TIER_5000M
        meters <= 10000 -> TIER_10000M
        else -> TIER_PLUS_10000M
      }
    }

    fun forDistance(distanceMeters: Int): ProximityVicinityTier {
      return when {
        distanceMeters <= 500 -> TIER_500M
        distanceMeters <= 1000 -> TIER_1000M
        distanceMeters <= 2000 -> TIER_2000M
        distanceMeters <= 5000 -> TIER_5000M
        distanceMeters <= 10000 -> TIER_10000M
        else -> TIER_PLUS_10000M
      }
    }
  }
}

/**
 * Data model for a proximity service request alert.
 */
data class ProximityAlertNotification(
  val id: String = UUID.randomUUID().toString(),
  val requestId: String,
  val title: String,
  val category: String,
  val categoryCode: String,
  val requesterName: String,
  val requesterBcnId: String,
  val distanceMeters: Int,
  val locality: String,
  val budget: String,
  val problemDescription: String,
  val urgency: String = "URGENT",
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
) {
  val tier: ProximityVicinityTier
    get() = ProximityVicinityTier.forDistance(distanceMeters)

  val formattedDistance: String
    get() = when {
      distanceMeters < 1000 -> "${distanceMeters}m"
      else -> String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000f)
    }

  val distanceBadgeLabel: String
    get() = when {
      distanceMeters <= 500 -> "⚡ ${distanceMeters}m (Immediate 500m)"
      distanceMeters <= 1000 -> "📍 ${distanceMeters}m (1000m Vicinity)"
      distanceMeters <= 2000 -> "📍 ${distanceMeters}m (2000m Vicinity)"
      distanceMeters <= 5000 -> "📍 ${formattedDistance} (5000m Vicinity)"
      distanceMeters <= 10000 -> "📍 ${formattedDistance} (10000m Vicinity)"
      else -> "🌐 ${formattedDistance} (+10000m Vicinity)"
    }

  companion object {
    fun initialSamples(): List<ProximityAlertNotification> = listOf(
      ProximityAlertNotification(
        requestId = "req-sample-500m",
        title = "Urgent Main Line MCB Tripping",
        category = "Electrician",
        categoryCode = "ELEC",
        requesterName = "Aarav Mehta",
        requesterBcnId = "IN-MH-MU-26-108RES390",
        distanceMeters = 340,
        locality = "Kurla West, Block C",
        budget = "₹800 - ₹1,200",
        problemDescription = "Main distribution board trips repeatedly when inverter switches over. Need immediate inspection.",
        urgency = "URGENT",
        timestamp = System.currentTimeMillis() - (4 * 60 * 1000)
      ),
      ProximityAlertNotification(
        requestId = "req-sample-1000m",
        title = "Water Tank Float Valve Replacement",
        category = "Plumbing",
        categoryCode = "PLUM",
        requesterName = "Sunita Deshmukh",
        requesterBcnId = "IN-MH-MU-26-441RES102",
        distanceMeters = 880,
        locality = "Kurla East, Nehru Nagar",
        budget = "₹600 - ₹900",
        problemDescription = "Overhead tank overflowing due to jammed brass float valve. Need replacement today.",
        urgency = "TODAY",
        timestamp = System.currentTimeMillis() - (28 * 60 * 1000)
      ),
      ProximityAlertNotification(
        requestId = "req-sample-2000m",
        title = "Office Ethernet LAN Structured Cabling",
        category = "IT & Tech Services",
        categoryCode = "IT",
        requesterName = "Vikram Tech Solutions",
        requesterBcnId = "IN-MH-MU-26-892BIZ771",
        distanceMeters = 1850,
        locality = "Bandra-Kurla Complex (BKC)",
        budget = "₹4,500 - ₹8,000",
        problemDescription = "12-node Cat6 cabling and 24-port patch panel crimping required for new workstation floor.",
        urgency = "STANDARD",
        timestamp = System.currentTimeMillis() - (85 * 60 * 1000)
      )
    )
  }
}
