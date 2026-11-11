package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val internalUserId: String,
  val mobile: String,
  val fullName: String,
  val avatarUrl: String,
  val userType: String, // Individual, Professional, Business, Creator, Seller, Supplier, Organization
  val professionCode: String,
  val businessName: String,
  val address: String,
  val locality: String,
  val city: String,
  val state: String,
  val country: String,
  val pinCode: String,
  val about: String,
  val email: String,
  val website: String,
  val rating: Float = 4.8f,
  val reviewCount: Int = 12,
  val availabilityStatus: String = "AVAILABLE" // AVAILABLE, BUSY, OFFLINE
)

@Entity(tableName = "digital_cards")
data class DigitalCardEntity(
  @PrimaryKey val cardId: String,
  val internalUserId: String,
  val busyCardNetId: String, // e.g. IN-MH-MU-26-581ELEC347
  val name: String,
  val designation: String,
  val professionCode: String,
  val businessName: String,
  val services: String, // comma separated
  val phone: String,
  val whatsapp: String,
  val email: String,
  val website: String,
  val address: String,
  val workingHours: String,
  val about: String,
  val templateStyle: String, // CORPORATE, MODERN_DARK, LUXURY_GOLD, CREATIVE_CYAN, ELECTRIC_AMBER, MINIMAL_LIGHT
  val accentColorHex: String,
  val isPrimary: Boolean = true,
  val viewsCount: Int = 184,
  val sharesCount: Int = 42,
  val scansCount: Int = 67,
  val leadsCount: Int = 19,
  val linkedin: String = "https://linkedin.com",
  val facebook: String = "https://facebook.com",
  val instagram: String = "https://instagram.com",
  val photoUrl: String = "",
  val latitude: Double = 19.0760,
  val longitude: Double = 72.8777,
  val distanceMeters: Int = 180,
  val isConnected: Boolean = false,
  val connectionStatus: String = "NONE" // NONE, PENDING, CONNECTED
)

@Entity(tableName = "professions")
data class ProfessionCategoryEntity(
  @PrimaryKey val code: String,
  val title: String,
  val iconEmoji: String,
  val categoryGroup: String,
  val isActive: Boolean = true
)

@Entity(tableName = "feed_posts")
data class FeedPostEntity(
  @PrimaryKey val id: String,
  val authorUserId: String,
  val authorName: String,
  val authorBusyCardNetId: String,
  val authorProfession: String,
  val content: String,
  val mediaType: String, // TEXT, PHOTO, VIDEO, AD, OFFER
  val mediaUrl: String,
  val locationName: String,
  val targetRadiusMeters: Int,
  val postScope: String, // LOCAL, PUBLIC, FOLLOWERS, ADVERTISEMENT
  val likesCount: Int = 0,
  val commentsCount: Int = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val isLikedByMe: Boolean = false,
  val ctaType: String = "NONE", // NONE, CALL, WHATSAPP, VIEW_CARD
  val ctaPhone: String = ""
)

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
  @PrimaryKey val requestId: String,
  val requesterUserId: String,
  val requesterName: String,
  val requesterBusyCardNetId: String = "IN-MH-MU-26-108RES390",
  val requesterLocality: String,
  val professionCode: String,
  val problemDescription: String,
  val photoAttached: Boolean,
  val radiusMeters: Int = 500, // 500m default
  val status: String = "CREATED", // CREATED, SENT, INTERESTED, ACCEPTED, COMPLETED, CANCELLED
  val respondingProfessionalId: String? = null,
  val respondingProfessionalName: String? = null,
  val respondingProfessionalBusyCardNetId: String? = null,
  val respondingProfessionalRating: Float = 4.8f,
  val distanceMeters: Int = 250,
  val createdAt: Long = System.currentTimeMillis(),
  val expiryMinutes: Int = 30,
  val title: String = "",
  val category: String = "Plumbing",
  val categoryCode: String = "PLUMBING",
  val budget: String = "Negotiable",
  val urgency: String = "STANDARD", // URGENT, STANDARD, FLEXIBLE
  val tags: String = "", // Comma-separated tags
  val serviceMode: String = "ON_SITE" // ON_SITE, REMOTE, HYBRID
) {
  val displayTitle: String
    get() = if (title.isNotBlank()) title else "$category Request: ${problemDescription.take(35)}..."

  val tagList: List<String>
    get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "marketplace_products")
data class MarketplaceProductEntity(
  @PrimaryKey val id: String,
  val sellerUserId: String,
  val sellerName: String,
  val sellerBusyCardNetId: String,
  val title: String,
  val description: String,
  val price: Double,
  val condition: String, // NEW, USED
  val category: String, // ELECTRONICS, FURNITURE, TOOLS, VEHICLES, HOME, OTHER
  val locationName: String,
  val distanceMeters: Int,
  val pickupOption: String, // PICKUP, DELIVERY, BOTH
  val contactPhone: String,
  val imageUrl: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "card_templates")
data class CardTemplateEntity(
  @PrimaryKey val id: String,
  val creatorUserId: String,
  val creatorName: String,
  val creatorBusyCardNetId: String = "IN-MH-MU-26-441ARCH109",
  val title: String,
  val professionCode: String,
  val price: Double,
  val rating: Float,
  val downloadsCount: Int,
  val previewStyle: String,
  val hasWebsiteIcon: Boolean = true,
  val hasEmailIcon: Boolean = true,
  val hasPhoneIcon: Boolean = true,
  val hasPhotoSlot: Boolean = true,
  val hasBusyCardNetLink: Boolean = true,
  val hasLinkedInIcon: Boolean = true,
  val hasFacebookIcon: Boolean = true,
  val hasWhatsAppIcon: Boolean = true,
  val hasInstagramIcon: Boolean = true,
  val layoutFormat: String = "HORIZONTAL_MODERN"
)

@Entity(tableName = "card_connections")
data class CardConnectionEntity(
  @PrimaryKey val id: String,
  val requesterUserId: String,
  val requesterCardId: String,
  val requesterName: String,
  val requesterBusyCardNetId: String,
  val requesterProfession: String,
  val targetUserId: String,
  val targetCardId: String,
  val targetName: String,
  val targetBusyCardNetId: String,
  val targetProfession: String,
  val status: String = "CONNECTED", // PENDING, CONNECTED, REJECTED
  val bridgeLevel: String = "BRIDGE", // BRIDGE, TRUSTED_BRIDGE, BUSINESS_ALLIANCE
  val note: String = "Connected via BusyCardNet",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_statuses")
data class UserStatusEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val busyCardNetId: String,
  val authorName: String,
  val authorProfession: String,
  val statusText: String,
  val statusEmoji: String = "⚡",
  val mediaUrl: String = "",
  val pageLink: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val activeUntil: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)

@Entity(tableName = "physical_suppliers")
data class PhysicalSupplierEntity(
  @PrimaryKey val id: String,
  val companyName: String,
  val busyCardNetId: String,
  val cardTypesOffered: String,
  val minOrderQty: Int,
  val pricePerUnit: Double,
  val rating: Float,
  val deliveryDays: Int,
  val location: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey val id: String,
  val serviceRequestId: String,
  val senderId: String,
  val senderName: String,
  val senderBusyCardNetId: String = "IN-MH-MU-26-108RES390",
  val messageText: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isFromMe: Boolean
)

@Entity(tableName = "saved_businesses")
data class SavedBusinessEntity(
  @PrimaryKey val businessId: String,
  val userId: String,
  val businessName: String,
  val ownerName: String = "",
  val busyCardNetId: String = "",
  val professionCode: String = "",
  val categoryTitle: String = "",
  val phone: String = "",
  val whatsapp: String = "",
  val email: String = "",
  val website: String = "",
  val address: String = "",
  val operatingHours: String = "9:00 AM - 8:00 PM",
  val rating: Float = 4.8f,
  val reviewCount: Int = 18,
  val distanceMeters: Int = 250,
  val isVerified: Boolean = true,
  val servicesList: String = "",
  val note: String = "",
  val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "auction_items")
data class AuctionItemEntity(
  @PrimaryKey val id: String,
  val sellerUserId: String,
  val sellerName: String,
  val sellerPhone: String,
  val sellerBusyCardNetId: String = "IN-MH-MU-26-581ELEC347",
  val title: String,
  val description: String,
  val category: String, // ELECTRONICS, TOOLS, VEHICLES, COLLECTIBLES, JEWELRY, ART
  val imageUrl: String = "",
  val startingPrice: Double,
  val currentBid: Double,
  val highestBidderId: String? = null,
  val highestBidderName: String? = null,
  val upfrontPercent: Int = 15, // 10% to 20%
  val durationDays: Int = 3,
  val startTime: Long = System.currentTimeMillis(),
  val endTime: Long,
  val status: String = "ACTIVE", // ACTIVE, ENDED, SOLD, CANCELLED
  val winningBidderId: String? = null,
  val winningBidderName: String? = null,
  val totalBids: Int = 0,
  val locationName: String = "Local Market",
  val createdAt: Long = System.currentTimeMillis(),
  // BCN Storage & Buyer Delivery Logistics Cycle
  val logisticsStatus: String = "AUCTION_ACTIVE", // AUCTION_ACTIVE, AWAITING_SELLER_HANDOVER, IN_BCN_STORAGE, OUT_FOR_DELIVERY, DELIVERED_TO_BUYER
  val bcnStorageHub: String = "BCN Central Logistics Hub #04, Sector 4, Mumbai",
  val storageInwardCode: String = "BCN-WH-MH-7741",
  val storageShelfSlot: String = "BAY-3-SHELF-B12",
  val storageReceivedAt: Long = 0L,
  val dispatchTrackingId: String = "BCN-EXP-90248",
  val buyerDeliveryAddress: String = "Flat 402, Green Meadows, Mumbai",
  val deliveredAt: Long = 0L,
  val escrowReleased: Boolean = false
)

@Entity(tableName = "auction_bids")
data class AuctionBidEntity(
  @PrimaryKey val id: String,
  val auctionItemId: String,
  val bidderUserId: String,
  val bidderName: String,
  val bidderBusyCardNetId: String = "IN-MH-MU-26-108RES390",
  val bidderPhone: String,
  val bidAmount: Double,
  val upfrontDepositPaid: Double,
  val timestamp: Long = System.currentTimeMillis(),
  val isWinningBid: Boolean = false,
  val refundStatus: String = "HELD" // HELD, REFUNDED, WON_FINALIZED
)

@Entity(tableName = "product_quotations")
data class ProductQuotationEntity(
  @PrimaryKey val id: String,
  val productId: String,
  val buyerUserId: String,
  val buyerName: String,
  val buyerBusyCardNetId: String = "IN-MH-MU-26-108RES390",
  val buyerPhone: String,
  val offeredPrice: Double,
  val message: String = "",
  val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "auction_notifications")
data class AuctionNotificationEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val auctionItemId: String,
  val itemTitle: String,
  val previousBid: Double,
  val newHighestBid: Double,
  val outbidByName: String,
  val outbidByBusyCardNetId: String = "IN-MH-MU-26-204PLUM512",
  val refundAmount: Double,
  val message: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

@Entity(
  tableName = "business_reviews",
  indices = [
    Index(value = ["businessId"]),
    Index(value = ["busyCardNetId"])
  ]
)
data class BusinessReviewEntity(
  @PrimaryKey val id: String,
  val businessId: String,
  val businessName: String,
  val busyCardNetId: String,
  val reviewerUserId: String,
  val reviewerName: String,
  val reviewerBusyCardNetId: String = "IN-MH-MU-26-108RES390",
  val rating: Int, // 1 to 5 stars
  val reviewText: String,
  val serviceCategory: String = "General Service",
  val helpfulCount: Int = 0,
  val isVerifiedClient: Boolean = true,
  val timestamp: Long = System.currentTimeMillis()
)

/**
 * Cached Business Card Entity in Room Database.
 * Caches basic business profile data so users can view previously visited BCN cards even when offline.
 */
@Entity(
  tableName = "cached_business_cards",
  indices = [
    Index(value = ["busyCardNetId"], unique = true),
    Index(value = ["lastVisitedTimestamp"])
  ]
)
data class CachedBusinessCardEntity(
  @PrimaryKey val busyCardNetId: String,
  val cardId: String,
  val internalUserId: String,
  val businessName: String,
  val ownerName: String,
  val designation: String,
  val professionCode: String,
  val professionTitle: String,
  val services: String,
  val phone: String,
  val whatsapp: String,
  val email: String,
  val website: String,
  val address: String,
  val locality: String,
  val workingHours: String,
  val about: String,
  val rating: Float = 4.8f,
  val reviewCount: Int = 12,
  val templateStyle: String = "CORPORATE",
  val accentColorHex: String = "#0284C7",
  val photoUrl: String = "",
  val lastVisitedTimestamp: Long = System.currentTimeMillis(),
  val isOfflineAvailable: Boolean = true
) {
  fun toDigitalCard(): DigitalCardEntity {
    return DigitalCardEntity(
      cardId = cardId.ifBlank { "cached_${busyCardNetId}" },
      internalUserId = internalUserId.ifBlank { "user_${busyCardNetId}" },
      busyCardNetId = busyCardNetId,
      name = ownerName.ifBlank { businessName },
      designation = designation.ifBlank { professionTitle },
      professionCode = professionCode.ifBlank { "GEN" },
      businessName = businessName,
      services = services,
      phone = phone,
      whatsapp = whatsapp.ifBlank { phone },
      email = email,
      website = website,
      address = address,
      workingHours = workingHours.ifBlank { "9:00 AM - 8:00 PM" },
      about = about,
      templateStyle = templateStyle,
      accentColorHex = accentColorHex,
      isPrimary = false,
      viewsCount = 120,
      sharesCount = 35,
      scansCount = 50,
      leadsCount = 14,
      photoUrl = photoUrl
    )
  }
}

fun DigitalCardEntity.toCachedBusinessCard(rating: Float = 4.8f, reviewCount: Int = 14): CachedBusinessCardEntity {
  return CachedBusinessCardEntity(
    busyCardNetId = this.busyCardNetId,
    cardId = this.cardId,
    internalUserId = this.internalUserId,
    businessName = this.businessName.ifBlank { this.name },
    ownerName = this.name,
    designation = this.designation,
    professionCode = this.professionCode,
    professionTitle = this.designation.ifBlank { this.professionCode },
    services = this.services,
    phone = this.phone,
    whatsapp = this.whatsapp,
    email = this.email,
    website = this.website,
    address = this.address,
    locality = this.address.split(",").firstOrNull()?.trim() ?: "Mumbai",
    workingHours = this.workingHours,
    about = this.about,
    rating = rating,
    reviewCount = reviewCount,
    templateStyle = this.templateStyle,
    accentColorHex = this.accentColorHex,
    photoUrl = this.photoUrl,
    lastVisitedTimestamp = System.currentTimeMillis(),
    isOfflineAvailable = true
  )
}

