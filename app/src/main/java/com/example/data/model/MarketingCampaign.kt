package com.example.data.model

import java.util.UUID

/**
 * Facebook-style Marketing Campaign Model for Local Shops, Professionals, and Services.
 * Allows hyper-targeted ad configuration by category, plan, duration, area radius, and target audience.
 */
data class MarketingCampaign(
  val id: String = "camp-" + UUID.randomUUID().toString().take(8),
  val title: String,
  val description: String,
  val adCategory: String, // Code e.g. "SHOPS_RETAIL", "PROFESSIONALS"
  val adCategoryDisplayName: String,
  val adSubCategory: String = "",
  val planName: String, // "Starter Boost", "Professional Growth", "Local Dominance", "Enterprise Super-Reach"
  val planPriceRupees: Int,
  val durationDays: Int,
  val targetArea: String, // e.g. "Kurla West, Mumbai"
  val targetRadiusMeters: Int, // 500, 1000, 3000, 5000, 10000, 25000
  val targetAudienceCategory: String, // "Local Residents & Families", "Working Professionals", etc.
  val targetAgeGroup: String = "All Ages (18-65+)",
  val ctaType: String = "WHATSAPP", // "WHATSAPP", "CALL", "VIEW_CARD", "DIRECTIONS", "WEBSITE"
  val ctaContact: String = "+91 98765 43210",
  val totalBudgetRupees: Int,
  val dailyBudgetRupees: Int,
  val impressionsCount: Int = 0,
  val clicksCount: Int = 0,
  val inquiriesCount: Int = 0,
  val startDate: Long = System.currentTimeMillis(),
  val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
  val imageUrl: String = ""
)

enum class MarketingAdCategory(
  val code: String,
  val displayName: String,
  val subtitle: String,
  val emoji: String,
  val subCategories: List<String>
) {
  SHOPS_RETAIL(
    code = "SHOPS_RETAIL",
    displayName = "Local Shops & Retail",
    subtitle = "Grocery, electronics, garments, pharmacies & supermarkets",
    emoji = "🏪",
    subCategories = listOf("Grocery & Supermarket", "Clothing & Fashion", "Electronics & Mobiles", "Pharmacy & Medical", "Bakery & Confectionery", "Jewelry & Gold", "Hardware & Paints", "General Store")
  ),
  PROFESSIONALS(
    code = "PROFESSIONALS",
    displayName = "Professionals & Consultants",
    subtitle = "Doctors, lawyers, chartered accountants, architects & tutors",
    emoji = "💼",
    subCategories = listOf("Doctors & Specialists", "Lawyers & Legal Advisors", "Chartered Accountants & Tax", "Architects & Civil Engineers", "Teachers & Coaching Tutors", "Photographers & Creators", "Financial Advisors", "Tech & IT Experts")
  ),
  HOME_SERVICES(
    code = "HOME_SERVICES",
    displayName = "Home & Skilled Services",
    subtitle = "Electricians, plumbers, carpenters, AC technicians & mechanics",
    emoji = "🛠️",
    subCategories = listOf("Electricians", "Plumbers", "Carpenters", "AC Service & Repair", "Painters & Decorators", "Auto & Bike Mechanics", "House Cleaning", "Pest Control & Sanitation")
  ),
  FOOD_DINING(
    code = "FOOD_DINING",
    displayName = "Restaurants & Dining",
    subtitle = "Cafes, family dining, sweet shops, bakeries & cloud kitchens",
    emoji = "🍽️",
    subCategories = listOf("Family Restaurants", "Cafes & Coffee Bars", "Street Food & Snacks", "Sweet Shops & Mithai", "Cloud Kitchens", "Catering & Events")
  ),
  HEALTH_WELLNESS(
    code = "HEALTH_WELLNESS",
    displayName = "Health, Beauty & Salons",
    subtitle = "Beauty parlours, spas, gyms, yoga & dental clinics",
    emoji = "💇",
    subCategories = listOf("Beauty Parlours & Salons", "Gyms & Fitness Centres", "Spas & Massages", "Yoga & Meditation", "Dental Clinics", "Physiotherapy & Rehab")
  ),
  REAL_ESTATE(
    code = "REAL_ESTATE",
    displayName = "Real Estate & Builders",
    subtitle = "Property agents, rental brokers, contractors & interior designers",
    emoji = "🏢",
    subCategories = listOf("Property Buy & Sell", "Rental Brokers", "Interior Designers", "Building Contractors", "Architectural Planning")
  ),
  B2B_SUPPLIERS(
    code = "B2B_SUPPLIERS",
    displayName = "B2B & Wholesale",
    subtitle = "Raw materials, packaging, wholesale distributors & logistics",
    emoji = "🏭",
    subCategories = listOf("Wholesale Distributors", "Packaging & Boxes", "Industrial Raw Materials", "Commercial Printing", "Logistics & Transport")
  )
}

data class MarketingPlan(
  val id: String,
  val name: String,
  val priceRupees: Int,
  val guaranteedImpressions: Int,
  val maxRadiusMeters: Int,
  val radiusLabel: String,
  val features: List<String>,
  val badgeText: String? = null
)

val MARKETING_PLANS = listOf(
  MarketingPlan(
    id = "STARTER",
    name = "Starter Boost",
    priceRupees = 299,
    guaranteedImpressions = 2500,
    maxRadiusMeters = 1000,
    radiusLabel = "500m - 1 km",
    features = listOf(
      "2,500 Guaranteed Local Views",
      "Immediate neighborhood targeting",
      "Placement in Local Home Feed",
      "Direct Click-to-WhatsApp Action"
    )
  ),
  MarketingPlan(
    id = "PROFESSIONAL",
    name = "Professional Growth",
    priceRupees = 699,
    guaranteedImpressions = 7500,
    maxRadiusMeters = 5000,
    radiusLabel = "Up to 5 km",
    features = listOf(
      "7,500 Guaranteed Local Views",
      "Top-of-Search Category Priority",
      "Verified Gold 'Sponsored' Badge",
      "5 km Locality & Ward Geofencing",
      "Priority WhatsApp & Call Buttons"
    ),
    badgeText = "MOST POPULAR"
  ),
  MarketingPlan(
    id = "DOMINANCE",
    name = "Local Dominance",
    priceRupees = 1499,
    guaranteedImpressions = 20000,
    maxRadiusMeters = 15000,
    radiusLabel = "Up to 15 km",
    features = listOf(
      "20,000 Guaranteed Local Views",
      "Vicinity Radar Push to Cardholders",
      "Featured placement in Discover Screen",
      "Multi-locality geofencing",
      "Lead collection analytics dashboard"
    ),
    badgeText = "BEST VALUE"
  ),
  MarketingPlan(
    id = "ENTERPRISE",
    name = "Enterprise Super-Reach",
    priceRupees = 2999,
    guaranteedImpressions = 50000,
    maxRadiusMeters = 25000,
    radiusLabel = "Full 25 km Metro",
    features = listOf(
      "50,000 Guaranteed Local Views",
      "Video Feed sponsored placement",
      "Full city & metropolitan radius",
      "Dedicated high-intent lead routing",
      "Real-time ROI & conversion reports"
    )
  )
)

data class TargetAudienceSegment(
  val id: String,
  val title: String,
  val emoji: String,
  val description: String
)

val TARGET_AUDIENCE_SEGMENTS = listOf(
  TargetAudienceSegment(
    id = "RESIDENTS",
    title = "Local Residents & Families",
    emoji = "👨‍👩‍👧‍👦",
    description = "Households, neighborhood shoppers, and local community members."
  ),
  TargetAudienceSegment(
    id = "PROFESSIONALS",
    title = "Working Professionals & Cardholders",
    emoji = "💼",
    description = "Corporate executives, office workers, and registered digital cardholders."
  ),
  TargetAudienceSegment(
    id = "MERCHANTS",
    title = "Shop Owners & Local Retailers",
    emoji = "🏪",
    description = "Local business owners, merchants, traders, and commercial store proprietors."
  ),
  TargetAudienceSegment(
    id = "YOUTH",
    title = "Youth & College Students (18-25)",
    emoji = "🎓",
    description = "Young demographic, college students, tech-savvy mobile users, and trend buyers."
  ),
  TargetAudienceSegment(
    id = "HOMEOWNERS",
    title = "Homeowners & High Spenders",
    emoji = "🏡",
    description = "Property owners, renovation seekers, high purchasing power consumers."
  ),
  TargetAudienceSegment(
    id = "ALL_NETWORK",
    title = "All Local Network Members (Broad)",
    emoji = "🌐",
    description = "Maximum reach across all profiles, visitors, and active radius cardholders."
  )
)
