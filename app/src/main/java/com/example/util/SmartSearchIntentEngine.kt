package com.example.util

/**
 * Intelligent Intent Recognition Engine for BusyCardNet Search.
 *
 * Automatically parses user problems written in natural language (e.g.,
 * "lights are of and don't know what to do", "power issue", "tap is leaking",
 * "door stuck", "car punctured") and maps them to the appropriate professional
 * category with clear recommendations and single-click radius filtering.
 */
data class SearchIntentResult(
  val rawQuery: String,
  val detectedIssue: String,
  val requiredProfessionCode: String,
  val requiredProfessionTitle: String,
  val iconEmoji: String,
  val explanation: String,
  val actionLabel: String = "Find $requiredProfessionTitle in your radius",
  val matchedKeywords: List<String> = emptyList()
)

object SmartSearchIntentEngine {

  private data class ProfessionIntentRule(
    val code: String,
    val title: String,
    val emoji: String,
    val issueCategory: String,
    val explanationTemplate: String,
    val triggerPhrases: List<String>,
    val triggerKeywords: List<String>
  )

  private val rules: List<ProfessionIntentRule> = listOf(
    // 1. Electrician (Prompt priority requirement: "lights are of", "power issue", etc.)
    ProfessionIntentRule(
      code = "ELEC",
      title = "Electrician",
      emoji = "⚡",
      issueCategory = "Lighting, Electrical & Power Fault",
      explanationTemplate = "Your description points to an electrical or lighting issue. You require a licensed nearby Electrician.",
      triggerPhrases = listOf(
        "lights are of", // user's explicit typo
        "lights are off",
        "light is off",
        "lights off",
        "light off",
        "lights of",
        "light of",
        "no light",
        "no lights",
        "power issue",
        "power cut",
        "power failure",
        "power outage",
        "power problem",
        "no power",
        "short circuit",
        "spark in wire",
        "wire spark",
        "sparking",
        "bulb fused",
        "fused bulb",
        "tube light",
        "switch board",
        "switch broken",
        "switch not working",
        "socket broken",
        "plug not working",
        "mcb trip",
        "mcb tripped",
        "tripped mcb",
        "fan not working",
        "fan stopped",
        "fan slow",
        "ac not cooling",
        "ac not working",
        "ac cooling issue",
        "electric shock",
        "current shock",
        "current issue",
        "electricity issue",
        "electricity problem",
        "geyser not heating",
        "water heater not working",
        "inverter problem",
        "inverter battery",
        "wire burnt",
        "burning smell wire"
      ),
      triggerKeywords = listOf(
        "electrician",
        "electrical",
        "wiring",
        "blackout",
        "voltage"
      )
    ),

    // 2. Plumber
    ProfessionIntentRule(
      code = "PLUM",
      title = "Plumber",
      emoji = "🔧",
      issueCategory = "Water Leakage, Drainage & Pipeline Repair",
      explanationTemplate = "Your description indicates a water leak, drainage or piping failure. You require a certified nearby Plumber.",
      triggerPhrases = listOf(
        "pipe leak",
        "pipe leaking",
        "pipe burst",
        "pipe broken",
        "water leak",
        "water leaking",
        "tap leak",
        "tap leaking",
        "tap dripping",
        "tap broken",
        "sink clogged",
        "sink blocked",
        "sink choked",
        "drain clogged",
        "drain choked",
        "drainage blocked",
        "drainage clogged",
        "toilet clogged",
        "toilet blocked",
        "toilet choked",
        "toilet overflow",
        "flush not working",
        "flush broken",
        "low water pressure",
        "water tank overflow",
        "bathroom leakage",
        "bathroom seepage",
        "water dripping",
        "wash basin leak",
        "sewer smell",
        "choked pipe"
      ),
      triggerKeywords = listOf(
        "plumber",
        "plumbing",
        "pipeline",
        "leakage",
        "seepage",
        "drainage"
      )
    ),

    // 3. Carpenter
    ProfessionIntentRule(
      code = "CARP",
      title = "Carpenter",
      emoji = "🪚",
      issueCategory = "Woodwork, Furniture & Door Hardware",
      explanationTemplate = "Your description involves doors, wooden fixtures, or furniture repair. You require a skilled nearby Carpenter.",
      triggerPhrases = listOf(
        "door stuck",
        "door jammed",
        "door broken",
        "door lock broken",
        "door latch",
        "window stuck",
        "window jammed",
        "window latch",
        "furniture broken",
        "chair broken",
        "table broken",
        "table leg",
        "bed broken",
        "bed squeaking",
        "cupboard repair",
        "wardrobe hinge",
        "cabinet broken",
        "drawer stuck",
        "drawer jammed",
        "wood repair",
        "wood polish",
        "sofa frame",
        "plywood repair"
      ),
      triggerKeywords = listOf(
        "carpenter",
        "carpentry",
        "woodwork",
        "furniture"
      )
    ),

    // 4. Mechanic (Automotive)
    ProfessionIntentRule(
      code = "MECH",
      title = "Mechanic",
      emoji = "🧰",
      issueCategory = "Vehicle Breakdown, Puncture & Engine Repair",
      explanationTemplate = "Your description indicates vehicle breakdown or tyre failure. You require a nearby Auto Mechanic.",
      triggerPhrases = listOf(
        "car puncture",
        "bike puncture",
        "tyre puncture",
        "tire puncture",
        "flat tyre",
        "flat tire",
        "puncture shop",
        "car breakdown",
        "car not starting",
        "bike not starting",
        "bike wont start",
        "bike won't start",
        "engine won't start",
        "engine overheating",
        "brake failure",
        "brakes not working",
        "battery dead car",
        "battery dead bike",
        "jump start",
        "clutch wire broken",
        "accelerator cable",
        "vehicle towing",
        "towing service",
        "scooter repair",
        "oil change"
      ),
      triggerKeywords = listOf(
        "mechanic",
        "puncture",
        "breakdown"
      )
    ),

    // 5. Doctor (Healthcare)
    ProfessionIntentRule(
      code = "DOCT",
      title = "Doctor",
      emoji = "👨‍⚕️",
      issueCategory = "Medical Consultation & Health Care",
      explanationTemplate = "Your description points to illness or medical symptoms. You require a verified nearby Doctor / Clinic.",
      triggerPhrases = listOf(
        "fever and cold",
        "high fever",
        "cough and cold",
        "headache and dizziness",
        "chest pain",
        "stomach pain",
        "stomach ache",
        "severe vomiting",
        "food poisoning",
        "feeling sick",
        "feeling unwell",
        "blood pressure check",
        "bp high",
        "bp low",
        "diabetes test",
        "sugar test",
        "doctor appointment",
        "medical checkup",
        "prescription refill",
        "wound dressing",
        "sprain ankle"
      ),
      triggerKeywords = listOf(
        "doctor",
        "physician",
        "clinic",
        "consultation"
      )
    ),

    // 6. Computer / IT Tech
    ProfessionIntentRule(
      code = "TECH",
      title = "Computer Tech",
      emoji = "💻",
      issueCategory = "Laptop, PC & Network Hardware Repair",
      explanationTemplate = "Your description relates to computer hardware, screens, or network connectivity. You require a Computer Technician.",
      triggerPhrases = listOf(
        "laptop slow",
        "laptop screen broken",
        "laptop not charging",
        "laptop not starting",
        "pc not starting",
        "blue screen",
        "virus removal",
        "malware attack",
        "wifi not working",
        "router not connecting",
        "internet issue",
        "printer not printing",
        "printer paper jam",
        "windows format",
        "data recovery",
        "hard drive crash"
      ),
      triggerKeywords = listOf(
        "computer technician",
        "laptop repair",
        "it support"
      )
    ),

    // 7. Contractor / Painter
    ProfessionIntentRule(
      code = "CONT",
      title = "Contractor",
      emoji = "🏗️",
      issueCategory = "Painting, Civil Work & Wall Renovation",
      explanationTemplate = "Your description points to home renovation, masonry, or wall dampness. You require a local Contractor / Painter.",
      triggerPhrases = listOf(
        "house painting",
        "wall painting",
        "peeling paint",
        "wall dampness",
        "wall cracks",
        "ceiling seepage",
        "tile broken",
        "tile replacement",
        "flooring repair",
        "home renovation",
        "masonry work",
        "plaster repair"
      ),
      triggerKeywords = listOf(
        "contractor",
        "painter",
        "renovation"
      )
    ),

    // 8. Lawyer (Legal)
    ProfessionIntentRule(
      code = "LAWY",
      title = "Lawyer",
      emoji = "⚖️",
      issueCategory = "Legal Disputes, Contracts & Advisory",
      explanationTemplate = "Your description involves legal disputes or formal agreements. You require a verified nearby Lawyer / Legal Counsel.",
      triggerPhrases = listOf(
        "property dispute",
        "tenant dispute",
        "landlord dispute",
        "contract breach",
        "agreement drafting",
        "court notice",
        "police complaint",
        "affidavit notary",
        "legal notice",
        "legal advice",
        "consumer complaint",
        "divorce lawyer"
      ),
      triggerKeywords = listOf(
        "lawyer",
        "advocate",
        "attorney"
      )
    ),

    // 9. Accountant / CA
    ProfessionIntentRule(
      code = "ACCT",
      title = "Accountant / CA",
      emoji = "📊",
      issueCategory = "Taxes, GST & Financial Bookkeeping",
      explanationTemplate = "Your description points to tax filings or audit requirements. You require a nearby Chartered Accountant / Tax Consultant.",
      triggerPhrases = listOf(
        "gst filing",
        "gst notice",
        "income tax return",
        "itr filing",
        "tax audit",
        "tds refund",
        "balance sheet audit",
        "accounting books",
        "ca consultation"
      ),
      triggerKeywords = listOf(
        "accountant",
        "chartered accountant",
        "tax consultant"
      )
    ),

    // 10. Hardware & Retail Shop
    ProfessionIntentRule(
      code = "SHOP",
      title = "Retail Shop",
      emoji = "🏪",
      issueCategory = "Tools, Parts & Hardware Supplies",
      explanationTemplate = "You are looking for parts, tools, or physical items to purchase. You require a nearby Hardware & Retail Store.",
      triggerPhrases = listOf(
        "buy tools",
        "buy drill",
        "buy hammer",
        "buy cement",
        "buy screws",
        "buy nails",
        "buy wires",
        "hardware store",
        "hardware mart"
      ),
      triggerKeywords = listOf(
        "hardware shop",
        "tools mart"
      )
    )
  )

  /**
   * Analyzes the search query using multi-tier intent matching:
   * 1. Normalized multi-word phrase matching (e.g. "lights are of", "power issue")
   * 2. Keyword and token boundary matching
   * Returns a [SearchIntentResult] if a clear trade/service need is detected.
   */
  fun analyzeQuery(query: String): SearchIntentResult? {
    if (query.isBlank() || query.trim().length < 3) return null

    // Normalize: lowercase, remove punctuation, collapse whitespaces
    val normalized = query.lowercase()
      .replace(Regex("[^a-z0-9 ]"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()

    // 1. Check phrase matches first (higher precision)
    for (rule in rules) {
      for (phrase in rule.triggerPhrases) {
        val normPhrase = phrase.lowercase()
          .replace(Regex("[^a-z0-9 ]"), " ")
          .replace(Regex("\\s+"), " ")
          .trim()

        if (normalized.contains(normPhrase)) {
          return SearchIntentResult(
            rawQuery = query.trim(),
            detectedIssue = rule.issueCategory,
            requiredProfessionCode = rule.code,
            requiredProfessionTitle = rule.title,
            iconEmoji = rule.emoji,
            explanation = rule.explanationTemplate,
            actionLabel = "Find ${rule.title} in your radius",
            matchedKeywords = listOf(phrase)
          )
        }
      }
    }

    // 2. Check individual keyword matches
    val tokens = normalized.split(" ").filter { it.length >= 3 }
    for (rule in rules) {
      for (kw in rule.triggerKeywords) {
        val normKw = kw.lowercase().trim()
        if (tokens.contains(normKw) || normalized.contains(normKw)) {
          return SearchIntentResult(
            rawQuery = query.trim(),
            detectedIssue = rule.issueCategory,
            requiredProfessionCode = rule.code,
            requiredProfessionTitle = rule.title,
            iconEmoji = rule.emoji,
            explanation = rule.explanationTemplate,
            actionLabel = "Find ${rule.title} in your radius",
            matchedKeywords = listOf(kw)
          )
        }
      }
    }

    return null
  }

  /**
   * Checks if a professional belongs to the detected intent profession.
   */
  fun matchesIntent(professionCode: String, intent: SearchIntentResult?): Boolean {
    if (intent == null) return true
    return professionCode.equals(intent.requiredProfessionCode, ignoreCase = true)
  }
}
