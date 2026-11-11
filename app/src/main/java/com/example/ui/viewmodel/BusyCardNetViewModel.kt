package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuctionBidEntity
import com.example.data.local.AuctionItemEntity
import com.example.data.local.AuctionNotificationEntity
import com.example.data.local.BusinessReviewEntity
import com.example.data.local.BusyCardNetDatabase
import com.example.data.local.CachedBusinessCardEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DigitalCardEntity
import com.example.data.local.FeedPostEntity
import com.example.data.local.MarketplaceProductEntity
import com.example.data.local.PhysicalSupplierEntity
import com.example.data.local.ProductQuotationEntity
import com.example.data.local.ProfessionCategoryEntity
import com.example.data.local.SavedBusinessEntity
import com.example.data.local.ServiceRequestEntity
import com.example.data.local.UserEntity
import com.example.data.model.BusinessRatingSummary
import com.example.data.model.CardPrintOrder
import com.example.data.model.MarketingCampaign
import com.example.data.datastore.UserCreditDataStore
import com.example.data.model.CreditTransaction
import com.example.data.model.KycVerificationData
import com.example.data.model.KycVerificationStatus
import com.example.data.model.MarketingServicePackage
import com.example.data.model.ProximityAlertNotification
import com.example.data.model.SocietyMaintenancePaymentRecord
import com.example.data.model.ProximityVicinityTier
import com.example.data.model.RequestUrgencyFilter
import com.example.data.model.ServiceDeliveryMode
import com.example.data.model.ServiceRequestCategory
import com.example.data.model.ServiceRequestFilterState
import com.example.data.repository.BusyCardNetRepository
import com.example.util.AuctionNotificationHelper
import com.example.util.AuthManager
import com.example.util.DemoUserPersona
import com.example.util.ServiceRequestNotificationHelper
import com.example.util.UserSessionState
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FeedFilter {
  ALL, NEARBY, BUSINESSES, PRODUCTS, SERVICES, VIDEO_ADS, ADVERTISEMENTS
}

enum class AppLanguage(val code: String, val displayName: String) {
  ENGLISH("en", "English"),
  HINDI("hi", "हिन्दी (Hindi)"),
  MARATHI("mr", "मराठी (Marathi)")
}

class BusyCardNetViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: BusyCardNetRepository

  init {
    val db = BusyCardNetDatabase.getDatabase(application, viewModelScope)
    repository = BusyCardNetRepository(db.busyCardNetDao())

    // Background timer to automatically check and expire active auctions whose deadline has passed
    viewModelScope.launch {
      while (true) {
        delay(3000)
        try {
          checkAndExpireAuctions()
        } catch (_: Exception) {}
      }
    }
  }

  val currentUser: StateFlow<UserEntity?> = repository.currentUser
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allCards: StateFlow<List<DigitalCardEntity>> = repository.allCards
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val primaryCard: StateFlow<DigitalCardEntity?> = repository.primaryCard
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allCachedBusinessCards: StateFlow<List<CachedBusinessCardEntity>> = repository.allCachedBusinessCards
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allProfessions: StateFlow<List<ProfessionCategoryEntity>> = repository.allProfessions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _printOrders = MutableStateFlow<List<CardPrintOrder>>(emptyList())
  val printOrders: StateFlow<List<CardPrintOrder>> = _printOrders.asStateFlow()

  // Marketing Campaigns State (Facebook-style hyper-targeted campaigns for shops & professionals)
  private val _marketingCampaigns = MutableStateFlow<List<MarketingCampaign>>(
    listOf(
      MarketingCampaign(
        id = "camp-elec-01",
        title = "Emergency Electrical Repairs & Home Wiring",
        description = "24x7 Certified Electrical Repairs, Short-circuit inspection, MCB installation & inverter servicing across Kurla & Bandra. Fast response in 20 mins!",
        adCategory = "HOME_SERVICES",
        adCategoryDisplayName = "🛠️ Home & Skilled Services",
        adSubCategory = "Electricians",
        planName = "Professional Growth",
        planPriceRupees = 699,
        durationDays = 7,
        targetArea = "Kurla West, Mumbai",
        targetRadiusMeters = 3000,
        targetAudienceCategory = "Local Residents & Families",
        targetAgeGroup = "All Ages (18-65+)",
        ctaType = "WHATSAPP",
        ctaContact = "+91 98201 12345",
        totalBudgetRupees = 699,
        dailyBudgetRupees = 100,
        impressionsCount = 4820,
        clicksCount = 390,
        inquiriesCount = 28,
        status = "ACTIVE"
      ),
      MarketingCampaign(
        id = "camp-shop-02",
        title = "Super Grocery Weekend Bonanza - Flat 25% Off",
        description = "Fresh vegetables, dairy, grains and daily essentials delivered to your doorstep within 30 minutes! Free delivery on orders above ₹299.",
        adCategory = "SHOPS_RETAIL",
        adCategoryDisplayName = "🏪 Local Shops & Retail",
        adSubCategory = "Grocery & Supermarket",
        planName = "Starter Boost",
        planPriceRupees = 299,
        durationDays = 3,
        targetArea = "Kurla West, Mumbai",
        targetRadiusMeters = 1000,
        targetAudienceCategory = "Local Residents & Families",
        targetAgeGroup = "All Ages (18-65+)",
        ctaType = "CALL",
        ctaContact = "+91 98765 43210",
        totalBudgetRupees = 299,
        dailyBudgetRupees = 100,
        impressionsCount = 2410,
        clicksCount = 180,
        inquiriesCount = 19,
        status = "ACTIVE"
      )
    )
  )
  val marketingCampaigns: StateFlow<List<MarketingCampaign>> = _marketingCampaigns.asStateFlow()

  private val _hasActiveShopSubscription = MutableStateFlow(false)
  val hasActiveShopSubscription: StateFlow<Boolean> = _hasActiveShopSubscription.asStateFlow()

  fun activateShopSubscription(onComplete: () -> Unit = {}) {
    _hasActiveShopSubscription.value = true
    onComplete()
  }

  // -------------------------------------------------------------------------
  // DataStore Implementation: User Marketing Credits & Multi-step KYC Records
  // -------------------------------------------------------------------------
  private val userCreditDataStore = UserCreditDataStore(application)

  val creditBalance: StateFlow<Int> = userCreditDataStore.creditBalance
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserCreditDataStore.DEFAULT_STARTING_CREDITS)

  val totalEarnedCredits: StateFlow<Int> = userCreditDataStore.totalEarned
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserCreditDataStore.DEFAULT_STARTING_CREDITS)

  val totalSpentCredits: StateFlow<Int> = userCreditDataStore.totalSpent
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val shareCount: StateFlow<Int> = userCreditDataStore.shareCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val kycStatus: StateFlow<KycVerificationStatus> = userCreditDataStore.kycStatus
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KycVerificationStatus.UNVERIFIED)

  val kycData: StateFlow<KycVerificationData> = userCreditDataStore.kycData
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KycVerificationData())

  val creditTransactions: StateFlow<List<CreditTransaction>> = userCreditDataStore.transactions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val societyMaintenancePayments: StateFlow<List<SocietyMaintenancePaymentRecord>> =
    userCreditDataStore.societyMaintenancePayments
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun rewardCreditForShare(
    platform: String = "App Share",
    amount: Int = UserCreditDataStore.DEFAULT_SHARE_REWARD_CREDITS,
    onRewarded: (Int) -> Unit = {}
  ) {
    viewModelScope.launch {
      val newBalance = userCreditDataStore.incrementCreditsForShare(platform, amount)
      onRewarded(newBalance)
    }
  }

  fun spendCreditsForMarketing(
    servicePackage: MarketingServicePackage,
    onResult: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      val success = userCreditDataStore.spendCreditsForMarketing(
        serviceId = servicePackage.id,
        serviceTitle = servicePackage.title,
        creditCost = servicePackage.creditCost
      )
      onResult(success)
    }
  }

  fun topUpWallet(
    amount: Int,
    paymentMethod: String = "UPI (Instant)",
    packName: String = "Wallet Recharge",
    onComplete: (Int) -> Unit = {}
  ) {
    viewModelScope.launch {
      val newBalance = userCreditDataStore.addWalletCredits(amount, paymentMethod, packName)
      onComplete(newBalance)
    }
  }

  fun spendWalletCreditsForAd(
    campaignTitle: String,
    creditCost: Int,
    onComplete: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      val success = userCreditDataStore.spendCreditsForAdCampaign(campaignTitle, creditCost)
      onComplete(success)
    }
  }

  fun spendWalletCreditsForMarketplace(
    itemTitle: String,
    creditCost: Int,
    orderId: String = "",
    onComplete: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      val success = userCreditDataStore.spendCreditsForMarketplace(itemTitle, creditCost, orderId)
      onComplete(success)
    }
  }

  fun transferWalletCredits(
    recipientBcnId: String,
    amount: Int,
    note: String = "",
    onComplete: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      val success = userCreditDataStore.transferCreditsToUser(recipientBcnId, amount, note)
      onComplete(success)
    }
  }

  /**
   * Pays Society Maintenance bill either via BusyCardNet Wallet Credits or Direct Payment (UPI / Card / NetBanking).
   */
  fun paySocietyMaintenance(
    societyName: String,
    registrationNumber: String,
    unitOrFlatNumber: String,
    memberName: String,
    memberBcnId: String,
    billingPeriod: String,
    amountRupees: Int,
    payWithWalletCredits: Boolean,
    directPaymentMethod: String = "UPI / Google Pay",
    breakdownSummary: String = "",
    onResult: (SocietyMaintenancePaymentRecord?) -> Unit = {}
  ) {
    viewModelScope.launch {
      val receipt = if (payWithWalletCredits) {
        userCreditDataStore.paySocietyMaintenanceWithCredits(
          societyName = societyName,
          registrationNumber = registrationNumber,
          unitOrFlatNumber = unitOrFlatNumber,
          memberName = memberName,
          memberBcnId = memberBcnId,
          billingPeriod = billingPeriod,
          amountRupees = amountRupees,
          breakdownSummary = breakdownSummary
        )
      } else {
        userCreditDataStore.recordDirectSocietyMaintenancePayment(
          societyName = societyName,
          registrationNumber = registrationNumber,
          unitOrFlatNumber = unitOrFlatNumber,
          memberName = memberName,
          memberBcnId = memberBcnId,
          billingPeriod = billingPeriod,
          amountRupees = amountRupees,
          paymentMethod = directPaymentMethod,
          breakdownSummary = breakdownSummary
        )
      }
      onResult(receipt)
    }
  }

  fun earnFreeCredits(
    amount: Int,
    source: String,
    onComplete: (Int) -> Unit = {}
  ) {
    viewModelScope.launch {
      val newBalance = userCreditDataStore.earnFreeCredits(amount, source)
      onComplete(newBalance)
    }
  }

  fun submitKycVerification(
    data: KycVerificationData,
    onSubmitted: () -> Unit = {}
  ) {
    viewModelScope.launch {
      userCreditDataStore.submitKycVerification(data)
      onSubmitted()
    }
  }

  fun simulateApproveKycVerification(
    notes: String = "Identity and Business Certificate verified by BusyCardNet compliance team.",
    onApproved: () -> Unit = {}
  ) {
    viewModelScope.launch {
      userCreditDataStore.approveKycVerification(notes)
      onApproved()
    }
  }

  fun rejectKycVerification(
    reason: String,
    onRejected: () -> Unit = {}
  ) {
    viewModelScope.launch {
      userCreditDataStore.rejectKycVerification(reason)
      onRejected()
    }
  }

  fun placePrintOrder(order: CardPrintOrder, onSuccess: (CardPrintOrder) -> Unit = {}) {
    viewModelScope.launch {
      _printOrders.value = listOf(order) + _printOrders.value
      onSuccess(order)
    }
  }

  private val _feedFilter = MutableStateFlow(FeedFilter.ALL)
  val feedFilter: StateFlow<FeedFilter> = _feedFilter.asStateFlow()

  val allPosts: StateFlow<List<FeedPostEntity>> = repository.allPosts
    .combine(_feedFilter) { posts, filter ->
      when (filter) {
        FeedFilter.ALL -> posts
        FeedFilter.NEARBY -> posts.filter { it.targetRadiusMeters <= 3000 }
        FeedFilter.BUSINESSES -> posts.filter { it.authorProfession.contains("Restaurant", true) || it.authorProfession.contains("Shop", true) || it.authorProfession.contains("Business", true) }
        FeedFilter.PRODUCTS -> posts.filter { it.content.contains("stock", true) || it.content.contains("tools", true) }
        FeedFilter.SERVICES -> posts.filter { it.authorProfession.contains("Electrician", true) || it.authorProfession.contains("Plumber", true) }
        FeedFilter.VIDEO_ADS -> posts.filter { it.mediaType == "VIDEO" }
        FeedFilter.ADVERTISEMENTS -> posts.filter { it.postScope == "ADVERTISEMENT" || it.mediaType == "AD" }
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allServiceRequests: StateFlow<List<ServiceRequestEntity>> = repository.allServiceRequests
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val myServiceRequests: StateFlow<List<ServiceRequestEntity>> = combine(
    repository.allServiceRequests,
    currentUser
  ) { requests, user ->
    val uid = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11"
    val mine = requests.filter {
      it.requesterUserId == uid ||
        it.requesterUserId == "user-123" ||
        it.requesterUserId.startsWith("user-") ||
        it.requesterName.contains("Me", ignoreCase = true) ||
        it.requesterName.contains("Rohan", ignoreCase = true)
    }
    if (mine.isNotEmpty()) mine else requests
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Service Request Filtering State for Marketplace & Discovery
  private val _serviceRequestFilter = MutableStateFlow(ServiceRequestFilterState())
  val serviceRequestFilter: StateFlow<ServiceRequestFilterState> = _serviceRequestFilter.asStateFlow()

  val filteredServiceRequests: StateFlow<List<ServiceRequestEntity>> = combine(
    repository.allServiceRequests,
    _serviceRequestFilter
  ) { requests, filter ->
    requests.filter { request ->
      // 1. Category filter
      val matchesCategory = filter.category.matches(request)

      // 2. Urgency filter
      val matchesUrgency = when (filter.urgency) {
        RequestUrgencyFilter.ALL -> true
        RequestUrgencyFilter.URGENT -> request.urgency.equals("URGENT", ignoreCase = true)
        RequestUrgencyFilter.STANDARD -> request.urgency.equals("STANDARD", ignoreCase = true)
        RequestUrgencyFilter.FLEXIBLE -> request.urgency.equals("FLEXIBLE", ignoreCase = true)
      }

      // 3. Mode filter
      val matchesMode = when (filter.mode) {
        ServiceDeliveryMode.ALL -> true
        ServiceDeliveryMode.ON_SITE -> request.serviceMode.equals("ON_SITE", ignoreCase = true) || request.serviceMode.equals("HYBRID", ignoreCase = true)
        ServiceDeliveryMode.REMOTE -> request.serviceMode.equals("REMOTE", ignoreCase = true) || request.serviceMode.equals("HYBRID", ignoreCase = true)
      }

      // 4. Tag filter
      val matchesTag = filter.selectedTag == null || request.tags.contains(filter.selectedTag, ignoreCase = true)

      // 5. Search query filter
      val matchesSearch = filter.searchQuery.isBlank() ||
        request.title.contains(filter.searchQuery, ignoreCase = true) ||
        request.problemDescription.contains(filter.searchQuery, ignoreCase = true) ||
        request.category.contains(filter.searchQuery, ignoreCase = true) ||
        request.tags.contains(filter.searchQuery, ignoreCase = true) ||
        request.requesterLocality.contains(filter.searchQuery, ignoreCase = true) ||
        request.requesterName.contains(filter.searchQuery, ignoreCase = true)

      matchesCategory && matchesUrgency && matchesMode && matchesTag && matchesSearch
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val serviceCategoryCounts: StateFlow<Map<ServiceRequestCategory, Int>> = repository.allServiceRequests.map { requests ->
    ServiceRequestCategory.entries.associateWith { cat ->
      if (cat == ServiceRequestCategory.ALL) requests.size
      else requests.count { cat.matches(it) }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  val allProducts: StateFlow<List<MarketplaceProductEntity>> = repository.allProducts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allAuctionItems: StateFlow<List<AuctionItemEntity>> = repository.allAuctionItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeAuctionItems: StateFlow<List<AuctionItemEntity>> = repository.activeAuctionItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSavedBusinesses: StateFlow<List<SavedBusinessEntity>> = repository.allSavedBusinesses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allReviews: StateFlow<List<BusinessReviewEntity>> = repository.allReviews
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val auctionNotifications: StateFlow<List<AuctionNotificationEntity>> = currentUser.flatMapLatest { user ->
    val uid = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11"
    repository.getAuctionNotifications(uid)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _latestOutbidAlert = MutableStateFlow<AuctionNotificationEntity?>(null)
  val latestOutbidAlert: StateFlow<AuctionNotificationEntity?> = _latestOutbidAlert.asStateFlow()

  fun dismissOutbidAlert() {
    _latestOutbidAlert.value = null
  }

  val allTemplates = repository.allTemplates
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSuppliers = repository.allSuppliers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Video Posts & Pages Showcase
  val videoPosts: StateFlow<List<FeedPostEntity>> = repository.videoPosts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Bridges & Card Connections
  val allConnections: StateFlow<List<com.example.data.local.CardConnectionEntity>> = repository.allConnections
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val connectedBridges: StateFlow<List<com.example.data.local.CardConnectionEntity>> = repository.connectedBridges
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pendingRequests: StateFlow<List<com.example.data.local.CardConnectionEntity>> = repository.pendingRequests
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Shared 24h Statuses between Connected Bridges
  val allStatuses: StateFlow<List<com.example.data.local.UserStatusEntity>> = repository.allStatuses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Current Location & Radius Configuration
  private val _selectedLocation = MutableStateFlow("Kurla West, Mumbai")
  val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

  private val _selectedRadiusMeters = MutableStateFlow(3000)
  val selectedRadiusMeters: StateFlow<Int> = _selectedRadiusMeters.asStateFlow()

  // Proximity Service Request Alert Notification System
  // Vicinities supported: 500m, 1000m, 2000m, 5000m, 10000m, +10000m (999999)
  private val _proximityAlertVicinityMeters = MutableStateFlow(1000)
  val proximityAlertVicinityMeters: StateFlow<Int> = _proximityAlertVicinityMeters.asStateFlow()

  private val _proximityAlertsEnabled = MutableStateFlow(true)
  val proximityAlertsEnabled: StateFlow<Boolean> = _proximityAlertsEnabled.asStateFlow()

  private val _activeProximityAlert = MutableStateFlow<ProximityAlertNotification?>(null)
  val activeProximityAlert: StateFlow<ProximityAlertNotification?> = _activeProximityAlert.asStateFlow()

  private val _proximityAlertsList = MutableStateFlow<List<ProximityAlertNotification>>(ProximityAlertNotification.initialSamples())
  val proximityAlertsList: StateFlow<List<ProximityAlertNotification>> = _proximityAlertsList.asStateFlow()

  val unreadProximityAlertsCount: StateFlow<Int> = _proximityAlertsList.map { list ->
    list.count { !it.isRead }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

  fun setProximityAlertVicinity(radiusMeters: Int) {
    _proximityAlertVicinityMeters.value = radiusMeters
  }

  fun setProximityAlertsEnabled(enabled: Boolean) {
    _proximityAlertsEnabled.value = enabled
  }

  fun dismissActiveProximityAlert() {
    _activeProximityAlert.value = null
  }

  fun markProximityAlertRead(alertId: String) {
    _proximityAlertsList.value = _proximityAlertsList.value.map {
      if (it.id == alertId) it.copy(isRead = true) else it
    }
  }

  fun clearAllProximityAlerts() {
    _proximityAlertsList.value = emptyList()
    _activeProximityAlert.value = null
  }

  fun triggerProximityAlert(notification: ProximityAlertNotification) {
    if (!_proximityAlertsEnabled.value) return
    val maxVicinity = _proximityAlertVicinityMeters.value
    // If request falls within the configured vicinity tier (or user enabled +10000m regional)
    if (notification.distanceMeters <= maxVicinity || maxVicinity >= 999999) {
      _activeProximityAlert.value = notification
      _proximityAlertsList.value = listOf(notification) + _proximityAlertsList.value.filter { it.id != notification.id }

      // Fire Android system push notification via ServiceRequestNotificationHelper
      ServiceRequestNotificationHelper.sendProximityAlertNotification(
        context = getApplication(),
        requestId = notification.requestId,
        title = notification.title,
        category = notification.category,
        requesterName = notification.requesterName,
        requesterBcnId = notification.requesterBcnId,
        distanceMeters = notification.distanceMeters,
        locality = notification.locality,
        budget = notification.budget,
        urgency = notification.urgency
      )
    }
  }

  /**
   * Simulates an incoming service request from another user in the local area
   * for any of the 6 vicinity tiers: 500m, 1000m, 2000m, 5000m, 10000m, +10000m.
   */
  fun simulateProximityAlert(vicinityRadiusMeters: Int) {
    val distance = when (vicinityRadiusMeters) {
      500 -> 320 // 320m (Immediate)
      1000 -> 820 // 820m (1 km)
      2000 -> 1650 // 1.65 km (2 km)
      5000 -> 3900 // 3.9 km (5 km)
      10000 -> 8400 // 8.4 km (10 km)
      else -> 14800 // 14.8 km (+10000m)
    }

    val payload = when (vicinityRadiusMeters) {
      500 -> listOf(
        "Urgent 3-Phase Main Breaker Tripping",
        "Electrician",
        "ELEC",
        "Pooja Sharma",
        "IN-MH-MU-26-904RES112",
        _selectedLocation.value.ifBlank { "Kurla West, Block C" },
        "₹750 - ₹1,200",
        "Main 32A MCB sparks and trips every time water heater or AC turns on. Need immediate certified electrician within 30 mins.",
        "URGENT"
      )
      1000 -> listOf(
        "Bathroom Concealed Diverter Valve Leaking",
        "Plumbing",
        "PLUM",
        "Karan Malhotra",
        "IN-MH-MU-26-318RES440",
        "Station Road, Ward 5",
        "₹600 - ₹950",
        "Water seeping through wall tile grout from concealed shower mixer. Need diagnostic and replacement today.",
        "TODAY"
      )
      2000 -> listOf(
        "Commercial Router & Firewall Gateway Config",
        "IT & Network",
        "IT",
        "Nexus Workspace BKC",
        "IN-MH-MU-26-772BIZ099",
        "Bandra-Kurla Complex (BKC)",
        "₹3,500 - ₹5,000",
        "Dual-WAN failover setup on Mikrotik / Cisco router for 35 coworking members.",
        "STANDARD"
      )
      5000 -> listOf(
        "Storefront Toughened Glass Door Hinge Repair",
        "Carpentry & Glass",
        "CARP",
        "Milan Fashion Store",
        "IN-MH-MU-26-553BIZ882",
        "Central Market Mall",
        "₹1,800 - ₹2,800",
        "Heavy frameless 12mm glass floor spring hinge jammed and not closing properly.",
        "STANDARD"
      )
      10000 -> listOf(
        "15kVA Silent Diesel Generator Annual Service",
        "Generator & Heavy Electrical",
        "ELEC",
        "Mahindra Cold Chain Logistics",
        "IN-MH-MU-26-119IND554",
        "MIDC Industrial Corridor",
        "₹6,500 - ₹11,000",
        "Filters, engine oil replacement, alternator brushes and battery check for backup generator.",
        "FLEXIBLE"
      )
      else -> listOf(
        "Turnkey Home Automation & IoT Lighting",
        "Smart Automation",
        "ELEC",
        "Heritage Hills Villa",
        "IN-MH-MU-26-004EST331",
        "Western Express Highway Outskirts",
        "₹25,000 - ₹45,000",
        "Complete 4BHK architectural smart dimmer switches, RGB scene controllers, and voice integration.",
        "FLEXIBLE"
      )
    }

    val title = payload[0]
    val category = payload[1]
    val code = payload[2]
    val requester = payload[3]
    val bcnId = payload[4]
    val locality = payload[5]
    val budget = payload[6]
    val desc = payload[7]
    val urgency = payload[8]

    val simulatedNotification = ProximityAlertNotification(
      id = "alert-${System.currentTimeMillis()}",
      requestId = "req-sim-${System.currentTimeMillis()}",
      title = title,
      category = category,
      categoryCode = code,
      requesterName = requester,
      requesterBcnId = bcnId,
      distanceMeters = distance,
      locality = locality,
      budget = budget,
      problemDescription = desc,
      urgency = urgency,
      timestamp = System.currentTimeMillis(),
      isRead = false
    )

    // Also persist into repository so it shows up in service request feeds/chat
    viewModelScope.launch {
      try {
        repository.createServiceRequest(
          requesterUserId = "sim-user-${System.currentTimeMillis()}",
          requesterName = requester,
          requesterLocality = locality,
          professionCode = code,
          problemDescription = "$title - $desc (Budget: $budget)",
          photoAttached = true,
          initialRadiusMeters = vicinityRadiusMeters
        )
      } catch (_: Exception) {}
    }

    triggerProximityAlert(simulatedNotification)
  }

  // Language state
  private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
  val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

  // Global Search
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchedCard = MutableStateFlow<DigitalCardEntity?>(null)
  val searchedCard: StateFlow<DigitalCardEntity?> = _searchedCard.asStateFlow()

  // Active Chat Session
  private val _activeChatRequestId = MutableStateFlow<String?>(null)
  val activeChatRequestId: StateFlow<String?> = _activeChatRequestId.asStateFlow()

  // Inspected Card Popup (Clicking any BCN Card Hyperlink across any screen)
  private val _viewingCard = MutableStateFlow<DigitalCardEntity?>(null)
  val viewingCard: StateFlow<DigitalCardEntity?> = _viewingCard.asStateFlow()

  fun openCardByBusyCardNetId(bcnId: String) {
    if (bcnId.isBlank()) return
    viewModelScope.launch {
      val found = repository.findCardByBusyCardNetId(bcnId.trim())
      _viewingCard.value = found
    }
  }

  fun openCard(card: DigitalCardEntity) {
    _viewingCard.value = card
  }

  fun closeViewingCard() {
    _viewingCard.value = null
  }

  fun setFeedFilter(filter: FeedFilter) {
    _feedFilter.value = filter
  }

  fun setLocation(locality: String) {
    _selectedLocation.value = locality
  }

  fun setRadius(radiusMeters: Int) {
    _selectedRadiusMeters.value = radiusMeters
  }

  fun setLanguage(language: AppLanguage) {
    _currentLanguage.value = language
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
    if (query.isNotBlank()) {
      viewModelScope.launch {
        // Direct BusyCardNet ID lookup or title search
        _searchedCard.value = repository.findCardByBusyCardNetId(query.trim())
      }
    } else {
      _searchedCard.value = null
    }
  }

  suspend fun resolveCardFromQr(qrData: String): DigitalCardEntity? {
    return repository.resolveCardFromQr(qrData)
  }

  fun importCard(card: DigitalCardEntity, onComplete: (DigitalCardEntity) -> Unit = {}) {
    viewModelScope.launch {
      val imported = repository.importCard(card)
      onComplete(imported)
    }
  }

  /**
   * Adds a scanned contact's Digital Visiting Card directly into the user's network
   * (Digital Cards + Offline Cache + Saved Contacts + Connected Bridge).
   */
  fun addCardToNetworkViaQr(card: DigitalCardEntity, onComplete: (DigitalCardEntity) -> Unit = {}) {
    val myCard = primaryCard.value
    val user = currentUser.value
    viewModelScope.launch {
      val imported = repository.addCardToNetworkViaQr(
        card = card,
        myUserId = user?.internalUserId ?: myCard?.internalUserId ?: "user-123",
        myCardId = myCard?.cardId ?: "card-primary",
        myName = myCard?.name ?: user?.fullName ?: "BusyCardNet Member",
        myBusyCardNetId = myCard?.busyCardNetId ?: "IN-MH-MU-26-374BIZ691",
        myProfession = myCard?.designation ?: "Professional"
      )
      onComplete(imported)
    }
  }

  fun recordCardShare(cardId: String) {
    viewModelScope.launch {
      repository.recordCardShare(cardId)
    }
  }

  fun updateAvailability(status: String) {
    val user = currentUser.value ?: return
    viewModelScope.launch {
      repository.updateAvailabilityStatus(user.internalUserId, status)
    }
  }

  fun createServiceRequest(
    professionCode: String,
    problemDescription: String,
    photoAttached: Boolean,
    radiusMeters: Int
  ) {
    val user = currentUser.value
    viewModelScope.launch {
      repository.createServiceRequest(
        requesterUserId = user?.internalUserId ?: "guest-user",
        requesterName = user?.fullName ?: "Local Resident",
        requesterLocality = _selectedLocation.value,
        professionCode = professionCode,
        problemDescription = problemDescription,
        photoAttached = photoAttached,
        initialRadiusMeters = radiusMeters
      )
    }
  }

  fun respondToRequest(requestId: String) {
    val card = primaryCard.value ?: return
    viewModelScope.launch {
      repository.respondToServiceRequest(
        requestId = requestId,
        professionalId = card.internalUserId,
        professionalName = card.businessName,
        professionalBusyCardNetId = card.busyCardNetId,
        rating = 4.9f
      )
    }
  }

  fun updateRequestStatus(requestId: String, status: String) {
    viewModelScope.launch {
      repository.updateRequestStatus(requestId, status)
    }
  }

  fun updateServiceRequestStatus(requestId: String, status: String) {
    updateRequestStatus(requestId, status)
  }

  fun setServiceRequestCategory(category: ServiceRequestCategory) {
    _serviceRequestFilter.value = _serviceRequestFilter.value.copy(category = category, selectedTag = null)
  }

  fun setServiceRequestUrgency(urgency: RequestUrgencyFilter) {
    _serviceRequestFilter.value = _serviceRequestFilter.value.copy(urgency = urgency)
  }

  fun setServiceRequestDeliveryMode(mode: ServiceDeliveryMode) {
    _serviceRequestFilter.value = _serviceRequestFilter.value.copy(mode = mode)
  }

  fun setServiceRequestSearchQuery(query: String) {
    _serviceRequestFilter.value = _serviceRequestFilter.value.copy(searchQuery = query)
  }

  fun setServiceRequestTag(tag: String?) {
    _serviceRequestFilter.value = _serviceRequestFilter.value.copy(
      selectedTag = if (_serviceRequestFilter.value.selectedTag == tag) null else tag
    )
  }

  fun clearServiceRequestFilters() {
    _serviceRequestFilter.value = ServiceRequestFilterState()
  }

  fun createCategorizedServiceRequest(
    title: String,
    category: String,
    categoryCode: String,
    problemDescription: String,
    budget: String,
    urgency: String,
    serviceMode: String,
    tags: String,
    radiusMeters: Int = 500
  ) {
    val user = currentUser.value
    viewModelScope.launch {
      repository.createCategorizedServiceRequest(
        title = title,
        category = category,
        categoryCode = categoryCode,
        problemDescription = problemDescription,
        budget = budget,
        urgency = urgency,
        serviceMode = serviceMode,
        tags = tags,
        requesterUserId = user?.internalUserId ?: "guest-user",
        requesterName = user?.fullName ?: "Local Resident",
        requesterLocality = _selectedLocation.value,
        radiusMeters = radiusMeters
      )
    }
  }

  fun broadcastProximityServiceRequest(
    title: String,
    category: String,
    categoryCode: String,
    problemDescription: String,
    budget: String = "Negotiable",
    urgency: String = "URGENT",
    serviceMode: String = "ON_SITE",
    tags: String = "",
    radiusMeters: Int = 500,
    photoAttached: Boolean = false,
    customLocality: String? = null,
    onResult: ((ServiceRequestEntity) -> Unit)? = null
  ) {
    val user = currentUser.value
    viewModelScope.launch {
      val entity = repository.createCategorizedServiceRequest(
        title = title,
        category = category,
        categoryCode = categoryCode,
        problemDescription = problemDescription,
        budget = budget,
        urgency = urgency,
        serviceMode = serviceMode,
        tags = tags,
        requesterUserId = user?.internalUserId ?: "guest-user",
        requesterName = user?.fullName ?: "Local Resident",
        requesterLocality = customLocality ?: _selectedLocation.value,
        photoAttached = photoAttached,
        radiusMeters = radiusMeters,
        status = "SENT"
      )
      val nearbyCount = try {
        com.example.ui.map.LocalPlaceDataProvider.getCuratedPlaces()
          .count { it.distanceMeters <= radiusMeters }
      } catch (_: Exception) { 5 }
      ServiceRequestNotificationHelper.sendBroadcastNotification(
        context = getApplication(),
        requestId = entity.requestId,
        title = entity.title,
        category = entity.category,
        radiusMeters = radiusMeters,
        notifiedBusinessCount = nearbyCount,
        locality = entity.requesterLocality,
        firestoreDocId = entity.requestId
      )
      onResult?.invoke(entity)
    }
  }

  fun expandRequestRadius(requestId: String, newRadius: Int) {
    viewModelScope.launch {
      repository.expandRequestRadius(requestId, newRadius)
    }
  }

  fun editServiceRequest(
    requestId: String,
    title: String,
    problemDescription: String,
    budget: String,
    urgency: String,
    radiusMeters: Int
  ) {
    viewModelScope.launch {
      repository.editServiceRequest(
        requestId = requestId,
        title = title,
        problemDescription = problemDescription,
        budget = budget,
        urgency = urgency,
        radiusMeters = radiusMeters
      )
    }
  }

  fun deleteServiceRequest(requestId: String) {
    viewModelScope.launch {
      repository.deleteServiceRequest(requestId)
    }
  }

  fun createAdvertisement(
    content: String,
    radiusMeters: Int,
    durationDays: Int,
    budgetRupees: Int,
    ctaType: String
  ) {
    val card = primaryCard.value
    val user = currentUser.value
    viewModelScope.launch {
      repository.createAdvertisement(
        authorUserId = user?.internalUserId ?: "user-123",
        authorName = card?.businessName ?: user?.fullName ?: "Local Business",
        authorBusyCardNetId = card?.busyCardNetId ?: "IN-MH-MU-26-374BIZ691",
        authorProfession = card?.designation ?: "Business",
        content = content,
        radiusMeters = radiusMeters,
        locationName = _selectedLocation.value,
        ctaType = ctaType,
        ctaPhone = card?.phone ?: "+91 98765 43210"
      )
    }
  }

  fun launchMarketProductCampaign(campaign: MarketingCampaign) {
    _marketingCampaigns.value = listOf(campaign) + _marketingCampaigns.value
    // Broadcast as active sponsored advertisement across the local network
    createAdvertisement(
      content = "${campaign.title}\n${campaign.description}",
      radiusMeters = campaign.targetRadiusMeters,
      durationDays = campaign.durationDays,
      budgetRupees = campaign.totalBudgetRupees,
      ctaType = campaign.ctaType
    )
  }

  fun toggleCampaignStatus(campaignId: String) {
    _marketingCampaigns.value = _marketingCampaigns.value.map {
      if (it.id == campaignId) {
        val nextStatus = if (it.status == "ACTIVE") "PAUSED" else "ACTIVE"
        it.copy(status = nextStatus)
      } else it
    }
  }

  fun publishShopNotification(
    title: String,
    body: String,
    bannerBitmap: Bitmap?,
    radiusMeters: Int,
    durationDays: Int,
    ctaType: String,
    ctaPhone: String,
    paidWithCredits: Boolean,
    paidWithSubscription: Boolean,
    cost: Int,
    onComplete: (Boolean) -> Unit = {}
  ) {
    val card = primaryCard.value
    val user = currentUser.value
    viewModelScope.launch {
      // 1. Handle payment debit if using wallet credits
      if (paidWithCredits && !paidWithSubscription) {
        val success = userCreditDataStore.spendCreditsForAdCampaign(title, cost)
        if (!success) {
          onComplete(false)
          return@launch
        }
      }

      // 2. Save banner bitmap locally if present
      var mediaUrl = ""
      if (bannerBitmap != null) {
        try {
          val filename = "shop_ad_${System.currentTimeMillis()}.png"
          val file = File(getApplication<Application>().cacheDir, filename)
          FileOutputStream(file).use { out ->
            bannerBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
          }
          val uri = FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file
          )
          mediaUrl = uri.toString()
        } catch (_: Exception) {}
      }

      // 3. Register campaign in marketing campaign list
      val campaign = MarketingCampaign(
        title = title,
        description = body,
        adCategory = "SHOPS_RETAIL",
        adCategoryDisplayName = "Shop Notification & News",
        adSubCategory = card?.designation ?: "Local Shop Announcement",
        planName = if (paidWithSubscription) "DCN Subscription Pass" else "Direct Online Notification",
        planPriceRupees = cost,
        durationDays = durationDays,
        targetArea = _selectedLocation.value,
        targetRadiusMeters = radiusMeters,
        targetAudienceCategory = "Local Residents & Cardholders",
        ctaType = ctaType,
        ctaContact = ctaPhone,
        totalBudgetRupees = cost,
        dailyBudgetRupees = (cost / durationDays.coerceAtLeast(1)),
        imageUrl = mediaUrl,
        status = "ACTIVE"
      )
      _marketingCampaigns.value = listOf(campaign) + _marketingCampaigns.value

      // 4. Publish as sponsored news feed post on Home feed
      repository.createAdvertisement(
        authorUserId = user?.internalUserId ?: "user-123",
        authorName = card?.businessName ?: user?.fullName ?: "Shop Notification",
        authorBusyCardNetId = card?.busyCardNetId ?: "IN-MH-MU-26-374BIZ691",
        authorProfession = card?.designation ?: "Shopkeeper",
        content = "$title\n\n$body",
        radiusMeters = radiusMeters,
        locationName = _selectedLocation.value,
        ctaType = ctaType,
        ctaPhone = ctaPhone,
        mediaUrl = mediaUrl
      )

      onComplete(true)
    }
  }

  fun deleteCampaign(campaignId: String) {
    _marketingCampaigns.value = _marketingCampaigns.value.filter { it.id != campaignId }
  }

  fun sellProduct(
    title: String,
    description: String,
    price: Double,
    condition: String,
    category: String,
    pickup: String,
    imageUrl: String = ""
  ) {
    val user = currentUser.value
    val card = primaryCard.value
    viewModelScope.launch {
      repository.sellProduct(
        sellerUserId = user?.internalUserId ?: "user-123",
        sellerName = user?.fullName ?: "Local Resident",
        sellerBusyCardNetId = card?.busyCardNetId ?: "IN-MH-MU-26-441SALE902",
        title = title,
        description = description,
        price = price,
        condition = condition,
        category = category,
        locationName = _selectedLocation.value,
        pickupOption = pickup,
        contactPhone = user?.mobile ?: "+91 98765 43210",
        imageUrl = imageUrl
      )
    }
  }

  fun createOrUpdateCard(
    name: String,
    designation: String,
    professionCode: String,
    businessName: String,
    services: String,
    phone: String,
    whatsapp: String,
    email: String,
    website: String,
    address: String,
    workingHours: String,
    about: String,
    templateStyle: String,
    accentColorHex: String
  ) {
    val user = currentUser.value
    viewModelScope.launch {
      repository.createNewCard(
        userId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        name = name,
        designation = designation,
        professionCode = professionCode,
        businessName = businessName,
        services = services,
        phone = phone,
        whatsapp = whatsapp,
        email = email,
        website = website,
        address = address,
        workingHours = workingHours,
        about = about,
        templateStyle = templateStyle,
        accentColorHex = accentColorHex
      )
    }
  }

  fun registerOrUpdateVisitingCardProfile(
    name: String,
    designation: String,
    professionCode: String,
    businessName: String,
    services: String,
    phone: String,
    whatsapp: String,
    email: String,
    website: String,
    address: String,
    workingHours: String,
    about: String,
    templateStyle: String,
    accentColorHex: String,
    latitude: Double,
    longitude: Double,
    onSuccess: (DigitalCardEntity) -> Unit = {}
  ) {
    val user = currentUser.value
    val authManager = com.example.util.AuthManager.getInstance()
    viewModelScope.launch {
      val card = repository.createNewCard(
        userId = user?.internalUserId ?: authManager.userUid,
        name = name,
        designation = designation,
        professionCode = professionCode,
        businessName = businessName,
        services = services,
        phone = phone,
        whatsapp = whatsapp,
        email = email,
        website = website,
        address = address,
        workingHours = workingHours,
        about = about,
        templateStyle = templateStyle,
        accentColorHex = accentColorHex
      )
      val updatedCard = card.copy(latitude = latitude, longitude = longitude)
      repository.updateCard(updatedCard)
      try {
        com.example.data.firestore.FirestoreBusinessManager.saveBusinessProfile(
          card = updatedCard,
          userUid = authManager.userUid
        )
      } catch (_: Exception) {}
      onSuccess(updatedCard)
    }
  }

  fun likePost(postId: String) {
    viewModelScope.launch {
      repository.likePost(postId)
    }
  }

  fun openChatForRequest(requestId: String) {
    _activeChatRequestId.value = requestId
  }

  fun closeChat() {
    _activeChatRequestId.value = null
  }

  fun getChatMessages(requestId: String) = repository.getChatMessages(requestId)

  fun sendChatMessage(requestId: String, messageText: String) {
    val user = currentUser.value
    viewModelScope.launch {
      repository.sendChatMessage(
        requestId = requestId,
        senderId = user?.internalUserId ?: "me",
        senderName = user?.fullName ?: "Rajesh Sharma",
        message = messageText,
        isFromMe = true
      )
    }
  }

  fun sendConnectRequest(
    targetUserId: String,
    targetCardId: String,
    targetName: String,
    targetBusyCardNetId: String,
    targetProfession: String,
    note: String = ""
  ) {
    val user = currentUser.value
    val card = primaryCard.value
    viewModelScope.launch {
      repository.sendConnectRequest(
        myUserId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        myCardId = card?.cardId ?: "card-elec-001",
        myName = card?.name ?: (user?.fullName ?: "Rajesh Sharma"),
        myBusyCardNetId = card?.busyCardNetId ?: "IN-MH-MU-26-581ELEC347",
        myProfession = card?.designation ?: (user?.professionCode ?: "Electrician"),
        targetUserId = targetUserId,
        targetCardId = targetCardId,
        targetName = targetName,
        targetBusyCardNetId = targetBusyCardNetId,
        targetProfession = targetProfession,
        note = note
      )
    }
  }

  fun acceptConnectRequest(connectionId: String) {
    viewModelScope.launch {
      repository.acceptConnectRequest(connectionId)
    }
  }

  fun rejectConnectRequest(connectionId: String) {
    viewModelScope.launch {
      repository.rejectConnectRequest(connectionId)
    }
  }

  fun shareUserStatus(statusText: String, emoji: String = "⚡", pageLink: String = "") {
    val user = currentUser.value
    val card = primaryCard.value
    viewModelScope.launch {
      repository.shareUserStatus(
        userId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        busyCardNetId = card?.busyCardNetId ?: "IN-MH-MU-26-581ELEC347",
        authorName = card?.name ?: "Rajesh Sharma",
        authorProfession = card?.designation ?: "Electrician",
        statusText = statusText,
        statusEmoji = emoji,
        pageLink = pageLink
      )
    }
  }

  fun createBroadcastPost(
    content: String,
    mediaUrl: String = "",
    durationHours: Int = 24
  ) {
    shareUserStatus(statusText = content, emoji = "📸", pageLink = mediaUrl)
  }

  fun postVideo(
    content: String,
    videoUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    ctaType: String = "CALL",
    ctaPhone: String = "+91 98765 43210"
  ) {
    val user = currentUser.value
    val card = primaryCard.value
    viewModelScope.launch {
      repository.postVideoOrPage(
        authorUserId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        authorName = card?.name ?: "Rajesh Sharma",
        authorBusyCardNetId = card?.busyCardNetId ?: "IN-MH-MU-26-581ELEC347",
        authorProfession = card?.designation ?: "Electrician",
        content = content,
        videoUrl = videoUrl,
        locationName = selectedLocation.value,
        radiusMeters = selectedRadiusMeters.value,
        scope = "VIDEO",
        ctaType = ctaType,
        ctaPhone = ctaPhone
      )
    }
  }

  fun publishCreatorTemplate(
    title: String,
    professionCode: String,
    price: Double,
    previewStyle: String,
    layoutFormat: String = "HORIZONTAL_MODERN"
  ) {
    val user = currentUser.value
    viewModelScope.launch {
      repository.publishCreatorTemplate(
        creatorUserId = user?.internalUserId ?: "creator-me",
        creatorName = user?.fullName ?: "Rajesh Sharma (Creator)",
        title = title,
        professionCode = professionCode,
        price = price,
        previewStyle = previewStyle,
        layoutFormat = layoutFormat
      )
    }
  }

  // ==========================================
  // AUCTION OPERATIONS, TIMER & OUTBID NOTIFICATIONS
  // ==========================================
  fun expireAuction(itemId: String) {
    viewModelScope.launch {
      repository.closeExpiredAuction(itemId)
    }
  }

  fun triggerExpireCheck() {
    viewModelScope.launch {
      checkAndExpireAuctions()
    }
  }

  fun handoverAuctionToStorage(itemId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.handoverAuctionToBcnStorage(itemId)
      onComplete()
    }
  }

  fun dispatchAuctionFromStorage(itemId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.dispatchAuctionFromStorage(itemId)
      onComplete()
    }
  }

  fun confirmAuctionDelivery(itemId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.confirmAuctionDeliveryToBuyer(itemId)
      onComplete()
    }
  }

  suspend fun checkAndExpireAuctions() {
    val activeList = allAuctionItems.value.filter { it.status == "ACTIVE" }
    val now = System.currentTimeMillis()
    for (item in activeList) {
      if (now >= item.endTime) {
        repository.closeExpiredAuction(item.id)
        val user = currentUser.value
        val myId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11"
        if (item.highestBidderId == myId) {
          AuctionNotificationHelper.sendAuctionEndedNotification(
            getApplication(),
            itemTitle = item.title,
            isWinner = true,
            winningBid = item.currentBid,
            refundAmount = 0.0
          )
        }
      }
    }
  }

  fun createAuction(
    title: String,
    description: String,
    category: String,
    imageUrl: String,
    startingPrice: Double,
    upfrontPercent: Int,
    durationDays: Int,
    onSuccess: (String) -> Unit = {}
  ) {
    val user = currentUser.value
    val card = primaryCard.value
    viewModelScope.launch {
      val id = repository.createAuctionItem(
        sellerUserId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        sellerName = user?.fullName ?: card?.name ?: "Local Seller",
        sellerPhone = user?.mobile ?: card?.phone ?: "+91 98765 43210",
        title = title,
        description = description,
        category = category,
        imageUrl = imageUrl,
        startingPrice = startingPrice,
        upfrontPercent = upfrontPercent.coerceIn(10, 20),
        durationDays = durationDays,
        locationName = _selectedLocation.value
      )
      onSuccess(id)
    }
  }

  fun placeAuctionBid(
    itemId: String,
    bidAmount: Double,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
  ) {
    val user = currentUser.value
    val card = primaryCard.value
    val bidderId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11"
    val bidderName = user?.fullName ?: card?.name ?: "Rajesh Sharma"
    val bidderPhone = user?.mobile ?: card?.phone ?: "+91 98111 22334"

    viewModelScope.launch {
      val outbidResult = repository.placeAuctionBid(
        itemId = itemId,
        bidderUserId = bidderId,
        bidderName = bidderName,
        bidderPhone = bidderPhone,
        bidAmount = bidAmount
      )

      if (outbidResult != null) {
        AuctionNotificationHelper.sendOutbidNotification(
          context = getApplication(),
          itemTitle = outbidResult.itemTitle,
          newBid = bidAmount,
          outbidByName = bidderName,
          refundAmount = outbidResult.refundAmount,
          auctionItemId = itemId
        )
      }
      onSuccess()
    }
  }

  /**
   * Simulates a competing bidder outbidding the current user on the specified item.
   * Demonstrates the real-time push notification and automatic escrow deposit refund notification.
   */
  fun simulateOutbidOnItem(itemId: String) {
    val item = allAuctionItems.value.find { it.id == itemId } ?: return
    val newBid = item.currentBid + 500.0
    val competitorNames = listOf("Vikram Rane", "Pooja Mehta", "Bharat Tools Mart", "Suresh K.")
    val chosenCompetitor = competitorNames.random()
    val chosenPhone = "+91 98" + (10000000..99999999).random()

    viewModelScope.launch {
      val outbidResult = repository.placeAuctionBid(
        itemId = itemId,
        bidderUserId = "competitor-" + chosenCompetitor.lowercase().take(5),
        bidderName = chosenCompetitor,
        bidderPhone = chosenPhone,
        bidAmount = newBid
      )

      if (outbidResult != null) {
        AuctionNotificationHelper.sendOutbidNotification(
          context = getApplication(),
          itemTitle = outbidResult.itemTitle,
          newBid = newBid,
          outbidByName = chosenCompetitor,
          refundAmount = outbidResult.refundAmount,
          auctionItemId = itemId
        )

        val alert = AuctionNotificationEntity(
          id = "sim-alert-" + System.currentTimeMillis(),
          userId = outbidResult.previousBidderId ?: "me",
          auctionItemId = itemId,
          itemTitle = outbidResult.itemTitle,
          previousBid = outbidResult.previousBidAmount,
          newHighestBid = newBid,
          outbidByName = chosenCompetitor,
          refundAmount = outbidResult.refundAmount,
          message = "⚠️ You were outbid by $chosenCompetitor at ₹${newBid.toInt()} on '${outbidResult.itemTitle}'! Your deposit of ₹${outbidResult.refundAmount.toInt()} will be refunded.",
          timestamp = System.currentTimeMillis(),
          isRead = false
        )
        _latestOutbidAlert.value = alert
      }
    }
  }

  fun getBidsForItem(itemId: String) = repository.getBidsForItem(itemId)

  fun submitProductQuotation(
    productId: String,
    offeredPrice: Double,
    message: String,
    onSuccess: () -> Unit = {}
  ) {
    val user = currentUser.value
    val card = primaryCard.value
    viewModelScope.launch {
      repository.submitProductQuotation(
        productId = productId,
        buyerUserId = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        buyerName = user?.fullName ?: card?.name ?: "Prospective Buyer",
        buyerPhone = user?.mobile ?: card?.phone ?: "+91 98111 22334",
        offeredPrice = offeredPrice,
        message = message
      )
      onSuccess()
    }
  }

  fun getQuotationsForProduct(productId: String) = repository.getQuotationsForProduct(productId)

  fun toggleSaveBusiness(business: SavedBusinessEntity, onResult: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val isSaved = allSavedBusinesses.value.any { it.businessId == business.businessId }
      if (isSaved) {
        repository.removeSavedBusiness(business.businessId)
        onResult(false)
      } else {
        repository.saveBusiness(business)
        onResult(true)
      }
    }
  }

  fun markNotificationRead(id: String) {
    viewModelScope.launch {
      repository.markNotificationRead(id)
    }
  }

  // ==========================================
  // BUSINESS STAR RATING & REVIEW SYSTEM
  // ==========================================

  fun getRatingSummary(
    businessId: String,
    businessName: String = "",
    busyCardNetId: String = "",
    defaultRating: Float = 4.8f,
    defaultReviewsCount: Int = 12
  ): BusinessRatingSummary {
    val reviews = allReviews.value.filter {
      (it.businessId.isNotBlank() && it.businessId == businessId) ||
      (busyCardNetId.isNotBlank() && it.busyCardNetId == busyCardNetId) ||
      (it.businessName.isNotBlank() && businessName.isNotBlank() && it.businessName.equals(businessName, ignoreCase = true))
    }

    if (reviews.isNotEmpty()) {
      val avg = reviews.map { it.rating }.average().toFloat()
      val five = reviews.count { it.rating == 5 }
      val four = reviews.count { it.rating == 4 }
      val three = reviews.count { it.rating == 3 }
      val two = reviews.count { it.rating == 2 }
      val one = reviews.count { it.rating == 1 }

      return BusinessRatingSummary(
        businessId = businessId,
        businessName = businessName,
        busyCardNetId = busyCardNetId,
        averageRating = (Math.round(avg * 10f) / 10f),
        totalReviewsCount = reviews.size,
        fiveStarCount = five,
        fourStarCount = four,
        threeStarCount = three,
        twoStarCount = two,
        oneStarCount = one,
        reviews = reviews
      )
    }

    // Default simulated distribution for businesses without specific user reviews yet
    val baseFive = (defaultReviewsCount * 0.75).toInt().coerceAtLeast(1)
    val baseFour = (defaultReviewsCount * 0.20).toInt().coerceAtLeast(1)
    val baseThree = (defaultReviewsCount - baseFive - baseFour).coerceAtLeast(0)

    return BusinessRatingSummary(
      businessId = businessId,
      businessName = businessName,
      busyCardNetId = busyCardNetId,
      averageRating = defaultRating,
      totalReviewsCount = defaultReviewsCount,
      fiveStarCount = baseFive,
      fourStarCount = baseFour,
      threeStarCount = baseThree,
      twoStarCount = 0,
      oneStarCount = 0,
      reviews = emptyList()
    )
  }

  fun submitBusinessReview(
    businessId: String,
    businessName: String,
    busyCardNetId: String,
    rating: Int,
    reviewText: String,
    serviceCategory: String = "General Service",
    onSuccess: (BusinessReviewEntity) -> Unit = {}
  ) {
    viewModelScope.launch {
      val user = currentUser.value
      val primaryCard = primaryCard.value
      val reviewerUid = user?.internalUserId ?: "01991B72-8F4A-7C21-B6D3-91A2F45C8E11"
      val reviewerName = user?.fullName?.ifBlank { "Rajesh Sharma" } ?: "Rajesh Sharma"
      val reviewerBcnId = primaryCard?.busyCardNetId ?: "IN-MH-MU-26-581ELEC347"

      val review = repository.addReview(
        businessId = businessId,
        businessName = businessName,
        busyCardNetId = busyCardNetId,
        reviewerUserId = reviewerUid,
        reviewerName = reviewerName,
        reviewerBusyCardNetId = reviewerBcnId,
        rating = rating,
        reviewText = reviewText,
        serviceCategory = serviceCategory
      )
      onSuccess(review)
    }
  }

  fun markReviewHelpful(reviewId: String) {
    viewModelScope.launch {
      repository.markReviewHelpful(reviewId)
    }
  }

  fun deleteReview(reviewId: String) {
    viewModelScope.launch {
      repository.deleteReview(reviewId)
    }
  }

  // -------------------------------------------------------------
  // Firebase Auth & Credential Manager Session Management
  // -------------------------------------------------------------
  val authManager: AuthManager = AuthManager.getInstance()
  val userSession: StateFlow<UserSessionState?> = authManager.sessionStateFlow

  suspend fun syncSessionToLocalUser(
    session: UserSessionState,
    professionCode: String = "ELEC",
    businessName: String = "BusyCardNet Member"
  ) {
    val existing = currentUser.value
    val updatedUser = UserEntity(
      internalUserId = session.uid,
      mobile = existing?.mobile ?: "+91 98201 54321",
      fullName = session.displayName?.ifBlank { "BusyCardNet Member" } ?: "BusyCardNet Member",
      avatarUrl = session.photoUrl ?: existing?.avatarUrl ?: "",
      userType = existing?.userType ?: "Professional",
      professionCode = existing?.professionCode ?: professionCode,
      businessName = if (!existing?.businessName.isNullOrBlank()) existing!!.businessName else businessName,
      address = existing?.address ?: "Shop 4, Linking Road, Bandra West, Mumbai 400050",
      locality = existing?.locality ?: "Bandra West",
      city = existing?.city ?: "Mumbai",
      state = existing?.state ?: "Maharashtra",
      country = existing?.country ?: "India",
      pinCode = existing?.pinCode ?: "400050",
      about = existing?.about ?: "Verified BusyCardNet professional member.",
      email = session.email ?: existing?.email ?: "member@busycardnet.in",
      website = existing?.website ?: "https://busycardnet.web.app",
      rating = existing?.rating ?: 4.9f,
      reviewCount = existing?.reviewCount ?: 18,
      availabilityStatus = existing?.availabilityStatus ?: "AVAILABLE"
    )
    repository.insertOrUpdateUser(updatedUser)
  }

  fun signInWithGoogle(
    context: Context,
    onSuccess: (UserSessionState) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.signInWithGoogleCredentialManager(context)
      if (result.isSuccess) {
        val session = result.getOrNull()!!
        syncSessionToLocalUser(session)
        onSuccess(session)
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Google Sign-In failed.")
      }
    }
  }

  fun switchGoogleAccount(
    context: Context,
    onSuccess: (UserSessionState) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.switchGoogleAccount(context)
      if (result.isSuccess) {
        val session = result.getOrNull()!!
        syncSessionToLocalUser(session)
        onSuccess(session)
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Account switch was cancelled or failed.")
      }
    }
  }

  fun oneTapGoogleSignIn(
    context: Context,
    onSuccess: (UserSessionState) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.oneTapGoogleSignIn(context)
      if (result.isSuccess) {
        val session = result.getOrNull()!!
        syncSessionToLocalUser(session)
        onSuccess(session)
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "One-tap sign-in failed.")
      }
    }
  }

  fun updateUserProfile(
    displayName: String?,
    photoUrl: String?,
    onSuccess: (UserSessionState) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.updateUserProfile(displayName, photoUrl)
      if (result.isSuccess) {
        val updated = result.getOrNull()!!
        syncSessionToLocalUser(updated)
        onSuccess(updated)
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to update profile.")
      }
    }
  }

  fun sendEmailVerification(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.sendEmailVerification()
      if (result.isSuccess) {
        onSuccess()
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Could not send email verification.")
      }
    }
  }

  fun deleteAccount(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.deleteUserAccount(context)
      if (result.isSuccess) {
        onSuccess()
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to delete account.")
      }
    }
  }

  fun signInWithEmailPassword(
    context: Context,
    email: String,
    password: String,
    onSuccess: (UserSessionState) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.signInWithEmailPassword(email, password, context)
      if (result.isSuccess) {
        val session = result.getOrNull()!!
        syncSessionToLocalUser(session)
        onSuccess(session)
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Invalid credentials. Please check email/password.")
      }
    }
  }

  fun signUpWithEmailPassword(
    context: Context,
    email: String,
    password: String,
    displayName: String,
    profession: String,
    saveToCredentialManager: Boolean,
    onSuccess: (UserSessionState) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.signUpWithEmailPassword(
        context = context,
        email = email,
        password = password,
        displayName = displayName,
        profession = profession,
        saveToCredentialManager = saveToCredentialManager
      )
      if (result.isSuccess) {
        val session = result.getOrNull()!!
        syncSessionToLocalUser(session, professionCode = profession.take(4).uppercase())
        onSuccess(session)
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Registration failed.")
      }
    }
  }

  fun sendPasswordReset(
    email: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = authManager.sendPasswordResetEmail(email)
      if (result.isSuccess) {
        onSuccess()
      } else {
        onError(result.exceptionOrNull()?.localizedMessage ?: "Could not send reset email.")
      }
    }
  }

  fun signInWithDemoPersona(
    persona: DemoUserPersona,
    onSuccess: (UserSessionState) -> Unit
  ) {
    viewModelScope.launch {
      val session = authManager.signInWithDemoPersona(persona)
      syncSessionToLocalUser(session, professionCode = "ELEC", businessName = persona.businessName)
      onSuccess(session)
    }
  }

  fun signOut(context: Context, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      authManager.signOut(context)
      onComplete()
    }
  }

  fun refreshSessionToken(onComplete: (String?) -> Unit = {}) {
    viewModelScope.launch {
      val token = authManager.refreshSessionToken()
      onComplete(token)
    }
  }
}
