package com.example.data.model

/**
 * KYC Verification status of the user and business identity.
 */
enum class KycVerificationStatus(val displayName: String, val badgeColorHex: String) {
  UNVERIFIED("Not Verified", "#64748B"),
  PENDING_REVIEW("Pending Verification", "#F59E0B"),
  APPROVED("Verified Business", "#10B981"),
  REJECTED("Verification Action Required", "#EF4444")
}

/**
 * Government Identity document types supported for KYC.
 */
enum class GovernmentIdType(val displayName: String, val idPlaceholder: String) {
  AADHAAR("Aadhaar Card (UIDAI)", "XXXX XXXX 1234"),
  PAN("Permanent Account Number (PAN)", "ABCDE1234F"),
  PASSPORT("Passport", "A1234567"),
  VOTER_ID("Election / Voter ID Card", "ABC1234567"),
  DRIVING_LICENSE("Driving License", "MH0120200012345")
}

/**
 * Business proof / certificate types supported.
 */
enum class BusinessCertificateType(val displayName: String) {
  GST_REGISTRATION("GSTIN Registration Certificate"),
  UDYAM_MSME("Udyam / MSME Certificate"),
  SHOP_ACT("Shop & Establishment Act License"),
  TRADE_LICENSE("Municipal Trade License"),
  CERTIFICATE_OF_INCORPORATION("MCA Certificate of Incorporation (Pvt Ltd / LLP)"),
  PROFESSIONAL_COUNCIL("Bar Council / Medical Council / CA Certificate")
}

/**
 * Captured KYC data bundle.
 */
data class KycVerificationData(
  val idType: GovernmentIdType = GovernmentIdType.AADHAAR,
  val idNumber: String = "",
  val governmentIdPhotoUri: String = "",
  val frontFacePhotoUri: String = "",
  val leftProfilePhotoUri: String = "",
  val rightProfilePhotoUri: String = "",
  val businessCertificateType: BusinessCertificateType = BusinessCertificateType.GST_REGISTRATION,
  val businessCertificateNumber: String = "",
  val businessCertificatePhotoUri: String = "",
  val applicantFullName: String = "",
  val businessName: String = "",
  val submittedTimestamp: Long = 0L,
  val status: KycVerificationStatus = KycVerificationStatus.UNVERIFIED,
  val complianceNotes: String = ""
)

/**
 * Credit Transaction type for user marketing ledger.
 */
enum class CreditTransactionType {
  WELCOME_BONUS,
  APP_SHARE_REWARD,
  REFERRAL_REWARD,
  MARKETING_BOOST_SPEND,
  FEATURED_LISTING_SPEND,
  PROMOTED_BROADCAST_SPEND,
  PREMIUM_CARD_DISCOUNT_SPEND,
  ADMIN_ADJUSTMENT,
  WALLET_TOPUP,
  AD_CAMPAIGN_SPEND,
  MARKETPLACE_PURCHASE_SPEND,
  MARKETPLACE_SALE_EARNING,
  WALLET_TRANSFER_OUT,
  WALLET_TRANSFER_IN,
  SOCIETY_MAINTENANCE_SPEND
}

/**
 * A single credit reward or debit record.
 */
data class CreditTransaction(
  val id: String,
  val timestamp: Long,
  val amount: Int, // positive for credits gained, negative for spent
  val type: CreditTransactionType,
  val description: String,
  val balanceAfter: Int
)

val CreditTransaction.isPositive: Boolean get() = amount > 0
val CreditTransaction.categoryName: String get() = when (type) {
  CreditTransactionType.WALLET_TOPUP -> "Recharges"
  CreditTransactionType.AD_CAMPAIGN_SPEND, CreditTransactionType.MARKETING_BOOST_SPEND, CreditTransactionType.PROMOTED_BROADCAST_SPEND -> "Ad Campaigns"
  CreditTransactionType.MARKETPLACE_PURCHASE_SPEND -> "Marketplace"
  CreditTransactionType.MARKETPLACE_SALE_EARNING -> "Marketplace"
  CreditTransactionType.WALLET_TRANSFER_OUT, CreditTransactionType.WALLET_TRANSFER_IN -> "Transfers"
  CreditTransactionType.SOCIETY_MAINTENANCE_SPEND -> "Society Maintenance"
  else -> "Rewards"
}

/**
 * Registered Cooperative Housing Society (CHS) / RWA / Commercial Complex preset.
 */
data class RegisteredSocietyInfo(
  val id: String,
  val name: String,
  val registrationNumber: String,
  val locality: String,
  val baseMonthlyCharge: Int,
  val waterCharge: Int,
  val sinkingAndRepairFund: Int,
  val securityAndLiftCharge: Int
)

val REGISTERED_SOCIETIES = listOf(
  RegisteredSocietyInfo(
    id = "soc_kohinoor",
    name = "Kohinoor City Co-op Housing Society Ltd.",
    registrationNumber = "BOM/WL/HSG/TC/11482",
    locality = "Kurla West, Mumbai",
    baseMonthlyCharge = 1250,
    waterCharge = 450,
    sinkingAndRepairFund = 350,
    securityAndLiftCharge = 600
  ),
  RegisteredSocietyInfo(
    id = "soc_gokuldham",
    name = "Gokuldham Residency CHS (RWA)",
    registrationNumber = "MH/MUM/CHS/20891",
    locality = "Goregaon / Andheri, Mumbai",
    baseMonthlyCharge = 1100,
    waterCharge = 400,
    sinkingAndRepairFund = 300,
    securityAndLiftCharge = 550
  ),
  RegisteredSocietyInfo(
    id = "soc_phoenix_plaza",
    name = "Phoenix Market Commercial Premises Co-op",
    registrationNumber = "BOM/COM/PREM/08419",
    locality = "LBS Marg, Kurla West",
    baseMonthlyCharge = 1800,
    waterCharge = 500,
    sinkingAndRepairFund = 450,
    securityAndLiftCharge = 750
  ),
  RegisteredSocietyInfo(
    id = "soc_seaview",
    name = "Sea View Heights Co-op Housing Society",
    registrationNumber = "BOM/WB/HSG/04920",
    locality = "Bandra West, Mumbai",
    baseMonthlyCharge = 1600,
    waterCharge = 500,
    sinkingAndRepairFund = 400,
    securityAndLiftCharge = 700
  )
)

/**
 * Verified Society Maintenance payment receipt record stored locally in DataStore.
 */
data class SocietyMaintenancePaymentRecord(
  val receiptId: String,
  val societyName: String,
  val registrationNumber: String,
  val unitOrFlatNumber: String,
  val memberName: String,
  val memberBcnId: String,
  val billingPeriod: String,
  val amountRupees: Int,
  val paymentMethod: String,
  val breakdownSummary: String,
  val timestamp: Long,
  val status: String = "PAID & VERIFIED"
)

data class WalletRechargePack(
  val id: String,
  val name: String,
  val credits: Int,
  val bonusCredits: Int,
  val priceRupees: Int,
  val isPopular: Boolean = false,
  val subtitle: String
) {
  val totalCredits: Int get() = credits + bonusCredits
}

val WALLET_RECHARGE_PACKS = listOf(
  WalletRechargePack(
    id = "pack_starter",
    name = "Starter Top-Up",
    credits = 250,
    bonusCredits = 25,
    priceRupees = 250,
    subtitle = "Ideal for 500m local promotion"
  ),
  WalletRechargePack(
    id = "pack_growth",
    name = "Growth Pack",
    credits = 500,
    bonusCredits = 75,
    priceRupees = 500,
    isPopular = true,
    subtitle = "Best for 7-day campaigns & marketplace goods"
  ),
  WalletRechargePack(
    id = "pack_pro",
    name = "Pro Booster",
    credits = 1000,
    bonusCredits = 200,
    priceRupees = 1000,
    subtitle = "Great for multi-area radius campaigns"
  ),
  WalletRechargePack(
    id = "pack_business",
    name = "Business Dominance",
    credits = 2500,
    bonusCredits = 600,
    priceRupees = 2500,
    subtitle = "Maximum bonus for active businesses & orders"
  )
)

/**
 * Marketing package that can be purchased with marketing credits.
 */
data class MarketingServicePackage(
  val id: String,
  val title: String,
  val description: String,
  val creditCost: Int,
  val durationLabel: String,
  val iconEmoji: String,
  val category: String,
  val targetReach: String
)

