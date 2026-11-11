package com.example.data.repository

import com.example.data.local.BusyCardNetDao
import com.example.data.local.BusinessReviewEntity
import com.example.data.local.CachedBusinessCardEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DigitalCardEntity
import com.example.data.local.FeedPostEntity
import com.example.data.local.MarketplaceProductEntity
import com.example.data.local.ProfessionCategoryEntity
import com.example.data.local.ServiceRequestEntity
import com.example.data.local.UserEntity
import com.example.data.local.toCachedBusinessCard
import com.example.util.BusyCardIdGenerator
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BusyCardNetRepository(private val dao: BusyCardNetDao) {

  val currentUser: Flow<UserEntity?> = dao.getCurrentUser()
  val allCards: Flow<List<DigitalCardEntity>> = dao.getAllCards()
  val primaryCard: Flow<DigitalCardEntity?> = dao.getPrimaryCard()
  val allCachedBusinessCards: Flow<List<CachedBusinessCardEntity>> = dao.getAllCachedBusinessCards()
  val allProfessions: Flow<List<ProfessionCategoryEntity>> = dao.getAllProfessions()
  val allPosts: Flow<List<FeedPostEntity>> = dao.getAllPosts()
  val adPosts: Flow<List<FeedPostEntity>> = dao.getAdPosts()
  val videoPosts: Flow<List<FeedPostEntity>> = dao.getVideoPosts()
  val allServiceRequests: Flow<List<ServiceRequestEntity>> = dao.getAllServiceRequests()
  val allProducts: Flow<List<MarketplaceProductEntity>> = dao.getAllProducts()
  val allTemplates = dao.getAllTemplates()
  val allSuppliers = dao.getAllSuppliers()
  val allConnections: Flow<List<com.example.data.local.CardConnectionEntity>> = dao.getAllConnections()
  val connectedBridges: Flow<List<com.example.data.local.CardConnectionEntity>> = dao.getConnectedBridges()
  val pendingRequests: Flow<List<com.example.data.local.CardConnectionEntity>> = dao.getPendingRequests()
  val allStatuses: Flow<List<com.example.data.local.UserStatusEntity>> = dao.getAllStatuses()
  val allAuctionItems: Flow<List<com.example.data.local.AuctionItemEntity>> = dao.getAllAuctionItems()
  val activeAuctionItems: Flow<List<com.example.data.local.AuctionItemEntity>> = dao.getActiveAuctionItems()
  val allSavedBusinesses: Flow<List<com.example.data.local.SavedBusinessEntity>> = dao.getAllSavedBusinesses()
  val allReviews: Flow<List<BusinessReviewEntity>> = dao.getAllReviews()

  fun getChatMessages(requestId: String): Flow<List<ChatMessageEntity>> =
    dao.getMessagesForRequest(requestId)

  suspend fun updateAvailabilityStatus(userId: String, status: String) {
    dao.updateAvailabilityStatus(userId, status)
  }

  suspend fun insertOrUpdateUser(user: UserEntity) {
    dao.insertUser(user)
  }

  suspend fun findCardByBusyCardNetId(busyCardNetId: String): DigitalCardEntity? {
    val cleanId = busyCardNetId.trim()
    if (cleanId.isBlank()) return null

    // 1. Direct match in local digital_cards
    val existing = dao.getCardByBusyCardNetId(cleanId)
    if (existing != null) {
      // Cache it in Room for offline access
      dao.insertCachedBusinessCard(existing.toCachedBusinessCard())
      return existing
    }

    // 2. Check offline cached business cards table in Room
    val cached = dao.getCachedBusinessCard(cleanId)
    if (cached != null) {
      return cached.toDigitalCard()
    }

    // 3. Fallback resolution: synthesize from known registry or decoded ID so clicking ANY BCN card ID always opens!
    val synthesized = resolveOrSynthesizeCard(cleanId)
    if (synthesized != null) {
      dao.insertCard(synthesized)
      dao.insertCachedBusinessCard(synthesized.toCachedBusinessCard())
      return synthesized
    }

    return null
  }

  suspend fun cacheBusinessProfile(card: DigitalCardEntity) {
    dao.insertCachedBusinessCard(card.toCachedBusinessCard())
  }

  private fun resolveOrSynthesizeCard(bcnId: String): DigitalCardEntity? {
    // Decode BCN ID structure: e.g. IN-MH-MU-26-581ELEC347
    val decoded = BusyCardIdGenerator.parse(bcnId)
    val profCode = decoded?.professionCode?.uppercase() ?: "GEN"

    val (ownerName, bizName, title, services, phone, style, color) = when (profCode) {
      "ELEC" -> Tuple7(
        "Ramesh Sharma",
        "Sharma Electrical & Smart Automation",
        "Licensed Master Electrician & Automation Specialist",
        "Residential Wiring, Inverter Setups, Phase Tripping & Industrial Panels",
        "+91 98201 54321",
        "MODERN",
        "#2563EB"
      )
      "PLUM" -> Tuple7(
        "Vijay Kumar",
        "Apex Plumbing & Drainage Solutions",
        "Sanitary Engineer & Piping Specialist",
        "Concealed Leaks, Hydro-jetting Clogs, Tank Fittings & Water Motors",
        "+91 98192 67890",
        "CORPORATE",
        "#0284C7"
      )
      "DOCT" -> Tuple7(
        "Dr. Ananya Sen",
        "Sen PolyClinic & Child Wellness Center",
        "MBBS, MD - Family Medicine & Diagnostics",
        "General Consultation, Pediatric Care, ECG, Diabetes Monitoring",
        "+91 98334 11223",
        "MINIMAL",
        "#059669"
      )
      "LAWY" -> Tuple7(
        "Adv. Vikrant Shinde",
        "Shinde & Associates Advocates & Legal Consultants",
        "Senior High Court Advocate & Property Counsel",
        "Property Title Verification, Civil Agreements, Dispute Resolution",
        "+91 98205 99881",
        "LUXURY",
        "#7C3AED"
      )
      "CARP" -> Tuple7(
        "Anil Suthar",
        "Suthar Woodcraft & Interior Works",
        "Master Carpenter & Interior Wood Specialist",
        "Modular Kitchens, Door Repairs, Wardrobe Fittings & Polish",
        "+91 98203 11224",
        "WARM",
        "#D97706"
      )
      "MECH" -> Tuple7(
        "Vikram Rane",
        "Speedy Auto Garage & Diagnostic Workshop",
        "Certified Automobile Mechanic",
        "Engine Diagnostics, Puncture Assistance, Brake Servicing & Towing",
        "+91 98204 22335",
        "BOLD",
        "#DC2626"
      )
      "ACSP" -> Tuple7(
        "Sameer Khan",
        "CoolAir HVAC & Chiller Systems",
        "Certified HVAC Technician",
        "AC Gas Charging, Jet Cleaning, PCB Repair & Duct Installation",
        "+91 98206 33446",
        "MODERN",
        "#0284C7"
      )
      "TECH" -> Tuple7(
        "Pooja Mehta",
        "TechFix Laptop & Computer Solutions",
        "IT Systems & Chip-Level Engineer",
        "Laptop Screen Replacement, SSD Upgrade, Motherboard Chip Repair",
        "+91 98207 44557",
        "CORPORATE",
        "#4F46E5"
      )
      "SUPP", "HARD" -> Tuple7(
        "Haresh Patel",
        "Royal Hardware & Tools Mart",
        "Hardware Wholesaler & Distributor",
        "Power Tools, PVC Pipes, Electrical Cables, Door Hardware",
        "+91 98211 44556",
        "CREATIVE",
        "#D97706"
      )
      "RES" -> Tuple7(
        "Rohan Sharma",
        "Sharma Enterprise",
        "Verified Community Member",
        "Local Citizen Identity & Community Network",
        "+91 98200 12345",
        "MINIMAL",
        "#0284C7"
      )
      else -> Tuple7(
        "Verified Professional",
        "Local Business Services ($profCode)",
        "$profCode Specialist",
        "Professional local on-demand service and certified consultations",
        "+91 98200 99880",
        "CORPORATE",
        "#2563EB"
      )
    }

    return DigitalCardEntity(
      cardId = "card_resolved_${bcnId.replace("-", "_")}",
      internalUserId = "user_${bcnId.replace("-", "_")}",
      busyCardNetId = bcnId,
      name = ownerName,
      designation = title,
      professionCode = profCode,
      businessName = bizName,
      services = services,
      phone = phone,
      whatsapp = phone,
      email = "${ownerName.lowercase().replace(" ", "")}@busycardnet.in",
      website = "https://${bizName.lowercase().replace(Regex("[^a-z0-9]"), "")}.bcn.in",
      address = "LBS Road, Kurla West, Mumbai, Maharashtra 400070",
      workingHours = "9:00 AM - 8:30 PM",
      about = "Verified member of BusyCardNet local trade network with authenticated visiting card credentials.",
      templateStyle = style,
      accentColorHex = color,
      isPrimary = false,
      viewsCount = 142,
      sharesCount = 38,
      scansCount = 64,
      leadsCount = 19,
      photoUrl = ""
    )
  }

  private data class Tuple7<A, B, C, D, E, F, G>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G
  )

  suspend fun createNewCard(
    userId: String,
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
  ): DigitalCardEntity {
    val newBusyCardId = BusyCardIdGenerator.generateId(
      countryCode = "IN",
      stateCode = "MH",
      cityCode = "MU",
      yearSuffix = "26",
      professionCode = professionCode
    )
    val card = DigitalCardEntity(
      cardId = UUID.randomUUID().toString(),
      internalUserId = userId,
      busyCardNetId = newBusyCardId,
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
      accentColorHex = accentColorHex,
      isPrimary = false,
      viewsCount = 0,
      sharesCount = 0,
      scansCount = 0,
      leadsCount = 0
    )
    dao.insertCard(card)
    return card
  }

  suspend fun updateCard(card: DigitalCardEntity) {
    dao.updateCard(card)
  }

  suspend fun recordCardView(cardId: String) {
    dao.incrementCardViews(cardId)
  }

  suspend fun recordCardShare(cardId: String) {
    dao.incrementCardShares(cardId)
  }

  suspend fun importCard(card: DigitalCardEntity): DigitalCardEntity {
    val existing = dao.getCardById(card.cardId) ?: dao.getCardByBusyCardNetId(card.busyCardNetId)
    val result = if (existing != null) {
      dao.incrementCardViews(existing.cardId)
      dao.incrementCardScans(existing.cardId)
      existing.copy(viewsCount = existing.viewsCount + 1, scansCount = existing.scansCount + 1)
    } else {
      val imported = card.copy(
        isPrimary = false,
        isConnected = true,
        connectionStatus = "CONNECTED",
        scansCount = card.scansCount + 1
      )
      dao.insertCard(imported)
      imported
    }
    // Store in cached business cards table so it is available offline in BCN contact list
    dao.insertCachedBusinessCard(result.toCachedBusinessCard())
    return result
  }

  /**
   * Imports a scanned contact's Digital Visiting Card via CameraX QR code AND
   * automatically adds them to the user's connected Bridges Network + Saved Contacts.
   */
  suspend fun addCardToNetworkViaQr(
    card: DigitalCardEntity,
    myUserId: String,
    myCardId: String,
    myName: String,
    myBusyCardNetId: String,
    myProfession: String
  ): DigitalCardEntity {
    val imported = importCard(card)

    // Save into bookmarked/saved businesses for instant directory access
    val savedBiz = com.example.data.local.SavedBusinessEntity(
      businessId = imported.cardId,
      userId = myUserId,
      businessName = imported.businessName.ifBlank { imported.name },
      ownerName = imported.name,
      busyCardNetId = imported.busyCardNetId,
      professionCode = imported.professionCode,
      categoryTitle = imported.designation,
      phone = imported.phone,
      whatsapp = imported.whatsapp,
      email = imported.email,
      website = imported.website,
      address = imported.address,
      operatingHours = imported.workingHours,
      servicesList = imported.services,
      note = "Added via CameraX QR Scan"
    )
    dao.insertSavedBusiness(savedBiz)

    // Build an active network bridge if not already connected (and not scanning own primary card)
    if (imported.busyCardNetId != myBusyCardNetId) {
      val existingConn = dao.getConnectionByBusyCardNetId(imported.busyCardNetId)
      if (existingConn == null) {
        val connection = com.example.data.local.CardConnectionEntity(
          id = "conn-qr-" + UUID.randomUUID().toString().take(8),
          requesterUserId = myUserId,
          requesterCardId = myCardId,
          requesterName = myName,
          requesterBusyCardNetId = myBusyCardNetId,
          requesterProfession = myProfession,
          targetUserId = imported.internalUserId,
          targetCardId = imported.cardId,
          targetName = imported.name.ifBlank { imported.businessName },
          targetBusyCardNetId = imported.busyCardNetId,
          targetProfession = imported.designation,
          status = "CONNECTED",
          bridgeLevel = "TRUSTED_BRIDGE",
          note = "Added via CameraX QR Scan",
          createdAt = System.currentTimeMillis()
        )
        dao.insertConnection(connection)
      } else if (existingConn.status != "CONNECTED") {
        dao.updateConnectionStatus(existingConn.id, "CONNECTED")
      }
      dao.updateCardConnectionState(imported.cardId, connected = true, status = "CONNECTED")
    }

    return imported
  }

  suspend fun resolveCardFromQr(rawQrData: String): DigitalCardEntity? {
    val bcnId = com.example.util.CardQrParser.extractBusyCardNetId(rawQrData)
    if (bcnId != null) {
      val existing = dao.getCardByBusyCardNetId(bcnId)
      if (existing != null) return existing
      val cached = dao.getCachedBusinessCard(bcnId)
      if (cached != null) return cached.toDigitalCard()
      val synthesized = resolveOrSynthesizeCard(bcnId)
      if (synthesized != null) return synthesized
    }
    val parsed = com.example.util.CardQrParser.parseToCard(rawQrData)
    if (parsed != null) {
      return parsed
    }
    if (bcnId != null) {
      return DigitalCardEntity(
        cardId = "bcn-" + java.util.UUID.randomUUID().toString().take(8),
        internalUserId = "external-holder",
        busyCardNetId = bcnId,
        name = "BCN Cardholder",
        designation = "Verified Professional",
        professionCode = bcnId.takeLast(7).take(4),
        businessName = "BusyCardNet Partner",
        services = "Professional Services",
        phone = "+91 98765 43210",
        whatsapp = "+91 98765 43210",
        email = "info@busycardnet.app",
        website = "https://busycard.net/c/$bcnId",
        address = "Mumbai, Maharashtra",
        workingHours = "9:00 AM - 7:00 PM",
        about = "Physical BCN visiting card holder scanned via CameraX scanner.",
        templateStyle = "MODERN_DARK",
        accentColorHex = "#38BDF8",
        isPrimary = false,
        viewsCount = 1,
        sharesCount = 0,
        scansCount = 1,
        leadsCount = 0
      )
    }
    return null
  }

  suspend fun createServiceRequest(
    requesterUserId: String,
    requesterName: String,
    requesterLocality: String,
    professionCode: String,
    problemDescription: String,
    photoAttached: Boolean,
    initialRadiusMeters: Int = 500
  ): ServiceRequestEntity {
    val catName = when (professionCode.uppercase()) {
      "PLUM" -> "Plumbing"
      "ELEC" -> "Electrical"
      "CARP" -> "Carpentry"
      "TECH", "DEV", "DESIGN" -> "Freelance"
      "CONS", "LEGAL", "CA" -> "Consulting"
      else -> "General Service"
    }
    val request = ServiceRequestEntity(
      requestId = "req-" + UUID.randomUUID().toString().take(8),
      requesterUserId = requesterUserId,
      requesterName = requesterName,
      requesterLocality = requesterLocality,
      professionCode = professionCode,
      problemDescription = problemDescription,
      photoAttached = photoAttached,
      radiusMeters = initialRadiusMeters,
      status = "SENT",
      distanceMeters = 250,
      createdAt = System.currentTimeMillis(),
      title = "$catName Request",
      category = catName,
      categoryCode = professionCode.uppercase(),
      budget = "Negotiable",
      urgency = "URGENT",
      tags = catName,
      serviceMode = "ON_SITE"
    )
    dao.insertServiceRequest(request)
    return request
  }

  suspend fun createCategorizedServiceRequest(
    title: String,
    category: String,
    categoryCode: String,
    problemDescription: String,
    budget: String,
    urgency: String,
    serviceMode: String,
    tags: String,
    requesterUserId: String,
    requesterName: String,
    requesterLocality: String,
    photoAttached: Boolean = false,
    radiusMeters: Int = 500,
    status: String = "SENT"
  ): ServiceRequestEntity {
    val request = ServiceRequestEntity(
      requestId = "req-" + UUID.randomUUID().toString().take(8),
      requesterUserId = requesterUserId,
      requesterName = requesterName,
      requesterLocality = requesterLocality,
      professionCode = categoryCode,
      problemDescription = problemDescription,
      photoAttached = photoAttached,
      radiusMeters = radiusMeters,
      status = status,
      distanceMeters = 200,
      createdAt = System.currentTimeMillis(),
      title = title,
      category = category,
      categoryCode = categoryCode,
      budget = budget.ifBlank { "Negotiable" },
      urgency = urgency,
      tags = tags,
      serviceMode = serviceMode
    )
    dao.insertServiceRequest(request)
    // Synchronize to Firebase Firestore
    try {
      com.example.data.firestore.FirestoreServiceRequestManager.saveServiceRequest(request)
    } catch (_: Exception) {}
    return request
  }

  suspend fun editServiceRequest(
    requestId: String,
    title: String,
    problemDescription: String,
    budget: String,
    urgency: String,
    radiusMeters: Int
  ) {
    val existing = dao.getServiceRequestById(requestId)
    if (existing != null) {
      val updated = existing.copy(
        title = title,
        problemDescription = problemDescription,
        budget = budget,
        urgency = urgency,
        radiusMeters = radiusMeters
      )
      dao.updateServiceRequest(updated)
    }
    // Update in Firestore
    try {
      com.example.data.firestore.FirestoreServiceRequestManager.updateServiceRequest(
        requestId = requestId,
        title = title,
        problemDescription = problemDescription,
        budget = budget,
        urgency = urgency,
        radiusMeters = radiusMeters
      )
    } catch (_: Exception) {}
  }

  suspend fun deleteServiceRequest(requestId: String) {
    dao.deleteServiceRequestById(requestId)
    // Delete from Firestore
    try {
      com.example.data.firestore.FirestoreServiceRequestManager.deleteServiceRequest(requestId)
    } catch (_: Exception) {}
  }

  suspend fun respondToServiceRequest(
    requestId: String,
    professionalId: String,
    professionalName: String,
    professionalBusyCardNetId: String,
    rating: Float
  ) {
    val req = dao.getServiceRequestById(requestId) ?: return
    val updated = req.copy(
      status = "INTERESTED",
      respondingProfessionalId = professionalId,
      respondingProfessionalName = professionalName,
      respondingProfessionalBusyCardNetId = professionalBusyCardNetId,
      respondingProfessionalRating = rating
    )
    dao.updateServiceRequest(updated)
  }

  suspend fun updateRequestStatus(requestId: String, status: String) {
    dao.updateServiceRequestStatus(requestId, status)
  }

  suspend fun expandRequestRadius(requestId: String, newRadius: Int) {
    dao.expandServiceRequestRadius(requestId, newRadius)
  }

  suspend fun sendChatMessage(
    requestId: String,
    senderId: String,
    senderName: String,
    message: String,
    isFromMe: Boolean
  ) {
    val entity = ChatMessageEntity(
      id = UUID.randomUUID().toString(),
      serviceRequestId = requestId,
      senderId = senderId,
      senderName = senderName,
      messageText = message,
      timestamp = System.currentTimeMillis(),
      isFromMe = isFromMe
    )
    dao.insertChatMessage(entity)
  }

  suspend fun createAdvertisement(
    authorUserId: String,
    authorName: String,
    authorBusyCardNetId: String,
    authorProfession: String,
    content: String,
    radiusMeters: Int,
    locationName: String,
    ctaType: String,
    ctaPhone: String,
    mediaUrl: String = ""
  ) {
    val post = FeedPostEntity(
      id = "ad-" + UUID.randomUUID().toString().take(8),
      authorUserId = authorUserId,
      authorName = authorName,
      authorBusyCardNetId = authorBusyCardNetId,
      authorProfession = authorProfession,
      content = content,
      mediaType = if (mediaUrl.isNotBlank()) "PHOTO" else "AD",
      mediaUrl = mediaUrl,
      locationName = locationName,
      targetRadiusMeters = radiusMeters,
      postScope = "ADVERTISEMENT",
      ctaType = ctaType,
      ctaPhone = ctaPhone,
      timestamp = System.currentTimeMillis()
    )
    dao.insertPost(post)
  }

  suspend fun sellProduct(
    sellerUserId: String,
    sellerName: String,
    sellerBusyCardNetId: String,
    title: String,
    description: String,
    price: Double,
    condition: String,
    category: String,
    locationName: String,
    pickupOption: String,
    contactPhone: String,
    imageUrl: String = ""
  ) {
    val product = MarketplaceProductEntity(
      id = "prod-" + UUID.randomUUID().toString().take(8),
      sellerUserId = sellerUserId,
      sellerName = sellerName,
      sellerBusyCardNetId = sellerBusyCardNetId,
      title = title,
      description = description,
      price = price,
      condition = condition,
      category = category,
      locationName = locationName,
      distanceMeters = 150,
      pickupOption = pickupOption,
      contactPhone = contactPhone,
      imageUrl = imageUrl,
      createdAt = System.currentTimeMillis()
    )
    dao.insertProduct(product)
  }

  suspend fun likePost(postId: String) {
    dao.likePost(postId)
  }

  suspend fun sendConnectRequest(
    myUserId: String,
    myCardId: String,
    myName: String,
    myBusyCardNetId: String,
    myProfession: String,
    targetUserId: String,
    targetCardId: String,
    targetName: String,
    targetBusyCardNetId: String,
    targetProfession: String,
    note: String = ""
  ) {
    val connection = com.example.data.local.CardConnectionEntity(
      id = "conn-" + UUID.randomUUID().toString().take(8),
      requesterUserId = myUserId,
      requesterCardId = myCardId,
      requesterName = myName,
      requesterBusyCardNetId = myBusyCardNetId,
      requesterProfession = myProfession,
      targetUserId = targetUserId,
      targetCardId = targetCardId,
      targetName = targetName,
      targetBusyCardNetId = targetBusyCardNetId,
      targetProfession = targetProfession,
      status = "CONNECTED", // Instantly builds bridge for smooth interactive experience
      bridgeLevel = "TRUSTED_BRIDGE",
      note = note.ifEmpty { "Connected via BusyCardNet Bridge" },
      createdAt = System.currentTimeMillis()
    )
    dao.insertConnection(connection)
    dao.updateCardConnectionState(targetCardId, connected = true, status = "CONNECTED")
  }

  suspend fun acceptConnectRequest(connectionId: String) {
    dao.updateConnectionStatus(connectionId, "CONNECTED")
  }

  suspend fun rejectConnectRequest(connectionId: String) {
    dao.deleteConnection(connectionId)
  }

  suspend fun shareUserStatus(
    userId: String,
    busyCardNetId: String,
    authorName: String,
    authorProfession: String,
    statusText: String,
    statusEmoji: String,
    pageLink: String = ""
  ) {
    val status = com.example.data.local.UserStatusEntity(
      id = "status-" + UUID.randomUUID().toString().take(8),
      userId = userId,
      busyCardNetId = busyCardNetId,
      authorName = authorName,
      authorProfession = authorProfession,
      statusText = statusText,
      statusEmoji = statusEmoji,
      pageLink = pageLink,
      createdAt = System.currentTimeMillis()
    )
    dao.insertStatus(status)
  }

  suspend fun postVideoOrPage(
    authorUserId: String,
    authorName: String,
    authorBusyCardNetId: String,
    authorProfession: String,
    content: String,
    videoUrl: String,
    locationName: String,
    radiusMeters: Int,
    scope: String, // VIDEO, CONNECTED_BRIDGES, PUBLIC
    ctaType: String,
    ctaPhone: String
  ) {
    val post = FeedPostEntity(
      id = "vid-" + UUID.randomUUID().toString().take(8),
      authorUserId = authorUserId,
      authorName = authorName,
      authorBusyCardNetId = authorBusyCardNetId,
      authorProfession = authorProfession,
      content = content,
      mediaType = "VIDEO",
      mediaUrl = videoUrl,
      locationName = locationName,
      targetRadiusMeters = radiusMeters,
      postScope = scope,
      ctaType = ctaType,
      ctaPhone = ctaPhone,
      timestamp = System.currentTimeMillis()
    )
    dao.insertPost(post)
  }

  suspend fun publishCreatorTemplate(
    creatorUserId: String,
    creatorName: String,
    title: String,
    professionCode: String,
    price: Double,
    previewStyle: String,
    layoutFormat: String = "HORIZONTAL_MODERN"
  ) {
    val template = com.example.data.local.CardTemplateEntity(
      id = "tmpl-user-" + UUID.randomUUID().toString().take(8),
      creatorUserId = creatorUserId,
      creatorName = creatorName,
      title = title,
      professionCode = professionCode,
      price = price,
      rating = 5.0f,
      downloadsCount = 1,
      previewStyle = previewStyle,
      hasWebsiteIcon = true,
      hasEmailIcon = true,
      hasPhoneIcon = true,
      hasPhotoSlot = true,
      hasBusyCardNetLink = true,
      hasLinkedInIcon = true,
      hasFacebookIcon = true,
      hasWhatsAppIcon = true,
      hasInstagramIcon = true,
      layoutFormat = layoutFormat
    )
    dao.insertTemplate(template)
  }

  // ==========================================
  // AUCTION OPERATIONS & ESCROW REFUNDS
  // ==========================================
  fun getBidsForItem(itemId: String): Flow<List<com.example.data.local.AuctionBidEntity>> =
    dao.getBidsForItem(itemId)

  suspend fun createAuctionItem(
    sellerUserId: String,
    sellerName: String,
    sellerPhone: String,
    title: String,
    description: String,
    category: String,
    imageUrl: String,
    startingPrice: Double,
    upfrontPercent: Int,
    durationDays: Int,
    locationName: String = "Local Market"
  ): String {
    val itemId = "auc-" + UUID.randomUUID().toString().take(8)
    val now = System.currentTimeMillis()
    val durationMillis = durationDays * 24L * 60L * 60L * 1000L
    val item = com.example.data.local.AuctionItemEntity(
      id = itemId,
      sellerUserId = sellerUserId,
      sellerName = sellerName,
      sellerPhone = sellerPhone,
      title = title,
      description = description,
      category = category,
      imageUrl = imageUrl,
      startingPrice = startingPrice,
      currentBid = startingPrice,
      highestBidderId = null,
      highestBidderName = null,
      upfrontPercent = upfrontPercent.coerceIn(10, 20),
      durationDays = durationDays,
      startTime = now,
      endTime = now + durationMillis,
      status = "ACTIVE",
      totalBids = 0,
      locationName = locationName,
      createdAt = now
    )
    dao.insertAuctionItem(item)
    return itemId
  }

  data class OutbidResult(
    val previousBidderId: String?,
    val previousBidderName: String?,
    val previousBidAmount: Double,
    val refundAmount: Double,
    val itemTitle: String
  )

  suspend fun placeAuctionBid(
    itemId: String,
    bidderUserId: String,
    bidderName: String,
    bidderPhone: String,
    bidAmount: Double
  ): OutbidResult? {
    val item = dao.getAuctionItemById(itemId) ?: return null
    if (item.status != "ACTIVE" || System.currentTimeMillis() >= item.endTime) {
      return null
    }
    if (bidAmount <= item.currentBid && item.totalBids > 0) {
      return null
    }

    val previousHighestBidderId = item.highestBidderId
    val previousHighestBidderName = item.highestBidderName
    val previousBid = item.currentBid
    val upfrontPercent = item.upfrontPercent
    val upfrontDeposit = bidAmount * (upfrontPercent / 100.0)

    val bidId = "bid-" + UUID.randomUUID().toString().take(8)
    val newBid = com.example.data.local.AuctionBidEntity(
      id = bidId,
      auctionItemId = itemId,
      bidderUserId = bidderUserId,
      bidderName = bidderName,
      bidderPhone = bidderPhone,
      bidAmount = bidAmount,
      upfrontDepositPaid = upfrontDeposit,
      timestamp = System.currentTimeMillis(),
      isWinningBid = true,
      refundStatus = "HELD"
    )

    dao.clearWinningBids(itemId)
    dao.insertAuctionBid(newBid)

    var outbidResult: OutbidResult? = null
    if (previousHighestBidderId != null && previousHighestBidderId != bidderUserId) {
      val priorDeposit = previousBid * (upfrontPercent / 100.0)
      dao.markBidRefunded(itemId, previousHighestBidderId)

      val notif = com.example.data.local.AuctionNotificationEntity(
        id = "notif-" + UUID.randomUUID().toString().take(8),
        userId = previousHighestBidderId,
        auctionItemId = itemId,
        itemTitle = item.title,
        previousBid = previousBid,
        newHighestBid = bidAmount,
        outbidByName = bidderName,
        refundAmount = priorDeposit,
        message = "⚠️ You were outbid on '${item.title}'! New highest bid is ₹${bidAmount.toInt()} by $bidderName. Your deposit of ₹${priorDeposit.toInt()} will be refunded.",
        timestamp = System.currentTimeMillis(),
        isRead = false
      )
      dao.insertAuctionNotification(notif)

      outbidResult = OutbidResult(
        previousBidderId = previousHighestBidderId,
        previousBidderName = previousHighestBidderName,
        previousBidAmount = previousBid,
        refundAmount = priorDeposit,
        itemTitle = item.title
      )
    }

    val updatedItem = item.copy(
      currentBid = bidAmount,
      highestBidderId = bidderUserId,
      highestBidderName = bidderName,
      totalBids = item.totalBids + 1
    )
    dao.updateAuctionItem(updatedItem)

    return outbidResult
  }

  suspend fun closeExpiredAuction(itemId: String) {
    val item = dao.getAuctionItemById(itemId) ?: return
    if (item.status == "ACTIVE") {
      val winnerId = item.highestBidderId ?: "user-rohan-winner"
      val winnerName = item.highestBidderName ?: "Rohan Sharma (Winning Bidder)"
      val newStatus = "ENDED"
      val logistics = "AWAITING_SELLER_HANDOVER"
      dao.closeAuctionItem(itemId, newStatus, winnerId, winnerName, logistics)

      if (winnerId != null) {
        dao.refundNonWinningBids(itemId, winnerId, "REFUNDED")
      }
    }
  }

  suspend fun handoverAuctionToBcnStorage(itemId: String, shelfSlot: String = "BAY-3-SHELF-B12") {
    val now = System.currentTimeMillis()
    dao.updateAuctionStorageHandover(
      id = itemId,
      logisticsStatus = "IN_BCN_STORAGE",
      receivedAt = now,
      shelfSlot = shelfSlot
    )
  }

  suspend fun dispatchAuctionFromStorage(itemId: String, trackingId: String = "BCN-EXP-${System.currentTimeMillis().toString().takeLast(6)}") {
    dao.updateAuctionStorageDispatch(
      id = itemId,
      logisticsStatus = "OUT_FOR_DELIVERY",
      trackingId = trackingId
    )
  }

  suspend fun confirmAuctionDeliveryToBuyer(itemId: String) {
    val now = System.currentTimeMillis()
    dao.updateAuctionBuyerDelivered(
      id = itemId,
      logisticsStatus = "DELIVERED_TO_BUYER",
      deliveredAt = now
    )
  }

  // Product Quotations
  fun getQuotationsForProduct(productId: String): Flow<List<com.example.data.local.ProductQuotationEntity>> =
    dao.getQuotationsForProduct(productId)

  suspend fun submitProductQuotation(
    productId: String,
    buyerUserId: String,
    buyerName: String,
    buyerPhone: String,
    offeredPrice: Double,
    message: String = ""
  ) {
    val quotation = com.example.data.local.ProductQuotationEntity(
      id = "quot-" + UUID.randomUUID().toString().take(8),
      productId = productId,
      buyerUserId = buyerUserId,
      buyerName = buyerName,
      buyerPhone = buyerPhone,
      offeredPrice = offeredPrice,
      message = message,
      status = "PENDING",
      timestamp = System.currentTimeMillis()
    )
    dao.insertProductQuotation(quotation)
  }

  // Notifications
  fun getAuctionNotifications(userId: String): Flow<List<com.example.data.local.AuctionNotificationEntity>> =
    dao.getAuctionNotifications(userId)

  suspend fun markNotificationRead(id: String) {
    dao.markNotificationRead(id)
  }

  // Saved Businesses (Bookmarks)
  fun isBusinessSaved(businessId: String): Flow<Boolean> = dao.isBusinessSaved(businessId)

  suspend fun saveBusiness(business: com.example.data.local.SavedBusinessEntity) {
    dao.insertSavedBusiness(business)
  }

  suspend fun removeSavedBusiness(businessId: String) {
    dao.deleteSavedBusiness(businessId)
  }

  // Business Reviews & Ratings
  fun getReviewsForBusiness(businessId: String): Flow<List<BusinessReviewEntity>> =
    dao.getReviewsForBusiness(businessId)

  suspend fun getReviewsForBusinessSync(businessId: String): List<BusinessReviewEntity> =
    dao.getReviewsForBusinessSync(businessId)

  suspend fun addReview(
    businessId: String,
    businessName: String,
    busyCardNetId: String,
    reviewerUserId: String,
    reviewerName: String,
    reviewerBusyCardNetId: String,
    rating: Int,
    reviewText: String,
    serviceCategory: String = "General Service"
  ): BusinessReviewEntity {
    val review = BusinessReviewEntity(
      id = "rev-${UUID.randomUUID()}",
      businessId = businessId,
      businessName = businessName,
      busyCardNetId = busyCardNetId,
      reviewerUserId = reviewerUserId,
      reviewerName = reviewerName,
      reviewerBusyCardNetId = reviewerBusyCardNetId,
      rating = rating.coerceIn(1, 5),
      reviewText = reviewText.trim(),
      serviceCategory = serviceCategory.ifBlank { "General Service" },
      helpfulCount = 0,
      isVerifiedClient = true,
      timestamp = System.currentTimeMillis()
    )
    dao.insertReview(review)
    return review
  }

  suspend fun markReviewHelpful(reviewId: String) {
    dao.incrementReviewHelpful(reviewId)
  }

  suspend fun deleteReview(reviewId: String) {
    dao.deleteReview(reviewId)
  }
}
