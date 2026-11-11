package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    DigitalCardEntity::class,
    ProfessionCategoryEntity::class,
    FeedPostEntity::class,
    ServiceRequestEntity::class,
    MarketplaceProductEntity::class,
    CardTemplateEntity::class,
    PhysicalSupplierEntity::class,
    ChatMessageEntity::class,
    CardConnectionEntity::class,
    UserStatusEntity::class,
    SavedBusinessEntity::class,
    AuctionItemEntity::class,
    AuctionBidEntity::class,
    ProductQuotationEntity::class,
    AuctionNotificationEntity::class,
    BusinessReviewEntity::class,
    CachedBusinessCardEntity::class
  ],
  version = 8,
  exportSchema = false
)
abstract class BusyCardNetDatabase : RoomDatabase() {

  abstract fun busyCardNetDao(): BusyCardNetDao

  companion object {
    @Volatile
    private var INSTANCE: BusyCardNetDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): BusyCardNetDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BusyCardNetDatabase::class.java,
          "busycardnet_db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(BusyCardNetDatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class BusyCardNetDatabaseCallback(
    private val scope: CoroutineScope
  ) : Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database.busyCardNetDao())
        }
      }
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
      super.onDestructiveMigration(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database.busyCardNetDao())
        }
      }
    }

    suspend fun populateInitialData(dao: BusyCardNetDao) {
      // 1. Initial User
      val defaultUser = UserEntity(
        internalUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        mobile = "+919876543210",
        fullName = "Rajesh Sharma",
        avatarUrl = "",
        userType = "Professional",
        professionCode = "ELEC",
        businessName = "Raj Electrical & Contracting",
        address = "Shop 4, Station Road, Kurla West",
        locality = "Kurla",
        city = "Mumbai",
        state = "Maharashtra",
        country = "India",
        pinCode = "400070",
        about = "Licensed electrical contractor with 8+ years experience in domestic, industrial wiring, inverter & solar installations.",
        email = "rajesh.elec@busycardnet.com",
        website = "www.rajelectricals.in",
        rating = 4.9f,
        reviewCount = 38,
        availabilityStatus = "AVAILABLE"
      )
      dao.insertUser(defaultUser)

      // 2. Initial Digital Cards (User has primary + additional card)
      val card1 = DigitalCardEntity(
        cardId = "card-elec-001",
        internalUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        busyCardNetId = "IN-MH-MU-26-581ELEC347",
        name = "Rajesh Sharma",
        designation = "Master Electrician & Contractor",
        professionCode = "ELEC",
        businessName = "Raj Electrical Services",
        services = "Home Wiring, Circuit Breaker Repairs, Inverter Setup, 24/7 Emergency Repairs, Industrial Panel Wiring",
        phone = "+91 98765 43210",
        whatsapp = "+91 98765 43210",
        email = "rajesh.elec@busycardnet.com",
        website = "https://rajelectricals.in",
        address = "Kurla West, Mumbai 400070",
        workingHours = "Mon - Sat: 8:00 AM - 9:30 PM (24/7 Emergency)",
        about = "Trusted electrician serving 500m to 10km radius with fast on-site assistance.",
        templateStyle = "ELECTRIC_AMBER",
        accentColorHex = "#F59E0B",
        isPrimary = true,
        viewsCount = 342,
        sharesCount = 68,
        scansCount = 94,
        leadsCount = 28
      )

      val card2 = DigitalCardEntity(
        cardId = "card-biz-002",
        internalUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
        busyCardNetId = "IN-MH-MU-26-374BIZ691",
        name = "Rajesh Sharma",
        designation = "Managing Director",
        professionCode = "BIZ",
        businessName = "Apex Power Technologies",
        services = "Solar Panel Rooftop, Commercial Transformers, AMC Maintenance",
        phone = "+91 98765 43210",
        whatsapp = "+91 98765 43210",
        email = "sales@apexpowertech.in",
        website = "https://apexpowertech.in",
        address = "Andheri MIDC, Mumbai",
        workingHours = "Mon - Fri: 9:00 AM - 6:00 PM",
        about = "Renewable energy solutions and power management.",
        templateStyle = "MODERN_DARK",
        accentColorHex = "#2563EB",
        isPrimary = false,
        viewsCount = 120,
        sharesCount = 18,
        scansCount = 33,
        leadsCount = 9
      )

      dao.insertCard(card1)
      dao.insertCard(card2)

      // 3. Centralized Professions
      val professions = listOf(
        ProfessionCategoryEntity("ELEC", "Electrician", "⚡", "Trade"),
        ProfessionCategoryEntity("PLUM", "Plumber", "🔧", "Trade"),
        ProfessionCategoryEntity("DOCT", "Doctor", "👨‍⚕️", "Healthcare"),
        ProfessionCategoryEntity("LAWY", "Lawyer", "⚖️", "Legal"),
        ProfessionCategoryEntity("CARP", "Carpenter", "🪚", "Trade"),
        ProfessionCategoryEntity("TEAC", "Teacher / Tutor", "📚", "Education"),
        ProfessionCategoryEntity("ENGR", "Engineer", "📐", "Professional"),
        ProfessionCategoryEntity("ARCH", "Architect", "🏛️", "Design"),
        ProfessionCategoryEntity("ACCT", "Accountant / CA", "📊", "Finance"),
        ProfessionCategoryEntity("PHOT", "Photographer", "📸", "Creative"),
        ProfessionCategoryEntity("DESG", "Designer", "🎨", "Creative"),
        ProfessionCategoryEntity("DRIV", "Driver", "🚗", "Logistics"),
        ProfessionCategoryEntity("MECH", "Mechanic", "🧰", "Automotive"),
        ProfessionCategoryEntity("TECH", "Computer Tech", "💻", "Technology"),
        ProfessionCategoryEntity("CONT", "Contractor", "🏗️", "Construction"),
        ProfessionCategoryEntity("REST", "Restaurant", "🍴", "Hospitality"),
        ProfessionCategoryEntity("HOTE", "Hotel", "🏨", "Hospitality"),
        ProfessionCategoryEntity("SHOP", "Retail Shop", "🏪", "Retail"),
        ProfessionCategoryEntity("BIZ", "Business", "🏢", "Corporate"),
        ProfessionCategoryEntity("SALE", "Seller", "🏷️", "Marketplace")
      )
      dao.insertProfessions(professions)

      // 4. Initial Feed Posts & Targeted Local Advertisements
      val initialPosts = listOf(
        FeedPostEntity(
          id = "post-ad-001",
          authorUserId = "rest-kurla-user",
          authorName = "Kurla Spice & Tandoor",
          authorBusyCardNetId = "IN-MH-MU-26-374REST691",
          authorProfession = "Restaurant",
          content = "Weekend Family Feast Special! 20% discount on all Royal Biryani & Tandoori platters. Located right opposite Kurla station. Dine-in or direct WhatsApp orders.",
          mediaType = "AD",
          mediaUrl = "",
          locationName = "Kurla West",
          targetRadiusMeters = 3000,
          postScope = "ADVERTISEMENT",
          likesCount = 89,
          commentsCount = 14,
          timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
          ctaType = "WHATSAPP",
          ctaPhone = "+91 98200 11223"
        ),
        FeedPostEntity(
          id = "post-vid-002",
          authorUserId = "plum-anil-user",
          authorName = "QuickFix Plumbing Solutions",
          authorBusyCardNetId = "IN-MH-MU-26-735PLUM402",
          authorProfession = "Plumber",
          content = "Quick video guide: How to prevent bathroom drain backflow during monsoon! Available for emergency repairs within 500m - 5km in Kurla & Chembur.",
          mediaType = "VIDEO",
          mediaUrl = "",
          locationName = "Kurla East",
          targetRadiusMeters = 2000,
          postScope = "LOCAL",
          likesCount = 142,
          commentsCount = 27,
          timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
          ctaType = "CALL",
          ctaPhone = "+91 97654 32109"
        ),
        FeedPostEntity(
          id = "post-003",
          authorUserId = "doc-sharma-user",
          authorName = "Dr. Anita Deshmukh (MD)",
          authorBusyCardNetId = "IN-MH-MU-26-264DOCT918",
          authorProfession = "Doctor",
          content = "Health advisory: Seasonal viral fever screening clinic open everyday 9 AM - 1 PM. Free basic blood pressure and glucose test this Saturday.",
          mediaType = "POST",
          mediaUrl = "",
          locationName = "Bandra West",
          targetRadiusMeters = 5000,
          postScope = "PUBLIC",
          likesCount = 65,
          commentsCount = 8,
          timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
          ctaType = "VIEW_CARD",
          ctaPhone = "+91 99887 66554"
        ),
        FeedPostEntity(
          id = "post-ad-004",
          authorUserId = "ad-hardware-user",
          authorName = "Bharat Tools & Hardware Mart",
          authorBusyCardNetId = "IN-MH-MU-26-891SHOP512",
          authorProfession = "Retail Shop",
          content = "Heavy duty Bosch & Stanley power drills back in stock! Wholesale prices for registered local technicians & carpenters. Visit our Kurla depot.",
          mediaType = "AD",
          mediaUrl = "",
          locationName = "Kurla Depot",
          targetRadiusMeters = 5000,
          postScope = "ADVERTISEMENT",
          likesCount = 54,
          commentsCount = 5,
          timestamp = System.currentTimeMillis() - 1000 * 60 * 360,
          ctaType = "CALL",
          ctaPhone = "+91 98111 22334"
        )
      )
      initialPosts.forEach { dao.insertPost(it) }

      // 5. Initial Categorized Service Requests for Marketplace & Local Discovery
      val categorizedRequests = listOf(
        ServiceRequestEntity(
          requestId = "req-plum-leak-01",
          requesterUserId = "user-requester-arjun",
          requesterName = "Arjun Singhania",
          requesterLocality = "Kurla West (Near Station)",
          professionCode = "PLUM",
          title = "Urgent Bathroom Main Pipe Leakage & Valve Repair",
          category = "Plumbing",
          categoryCode = "PLUMBING",
          problemDescription = "Main flush valve leaking heavily onto bathroom tiles. Need urgent washer replacement and pipe joint sealing before water damage spreads.",
          budget = "₹600 - ₹1,200",
          urgency = "URGENT",
          tags = "Leakage Repair, Valve Fix, Bathroom, Pipe Joint",
          serviceMode = "ON_SITE",
          photoAttached = true,
          radiusMeters = 500,
          status = "CREATED",
          distanceMeters = 180,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 15,
          expiryMinutes = 30
        ),
        ServiceRequestEntity(
          requestId = "req-free-app-02",
          requesterUserId = "user-requester-priya",
          requesterName = "Priya Deshmukh (FinTech Founder)",
          requesterLocality = "Bandra Kurla Complex",
          professionCode = "TECH",
          title = "Android Jetpack Compose UI & State Architecture",
          category = "Freelance",
          categoryCode = "FREELANCE",
          problemDescription = "Looking for an expert freelance Android developer to build 4 Compose screens with Material 3 theming, Room persistence, and offline sync.",
          budget = "₹25,000 Fixed",
          urgency = "STANDARD",
          tags = "Android Dev, Jetpack Compose, Kotlin, UI/UX Design",
          serviceMode = "REMOTE",
          photoAttached = false,
          radiusMeters = 2000,
          status = "CREATED",
          distanceMeters = 650,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 45,
          expiryMinutes = 120
        ),
        ServiceRequestEntity(
          requestId = "req-cons-gst-03",
          requesterUserId = "user-requester-vikas",
          requesterName = "Vikas Mehta (Retail Enterprises)",
          requesterLocality = "Kurla East Commercial Hub",
          professionCode = "CA",
          title = "GST Annual Audit & Input Tax Credit Consultation",
          category = "Consulting",
          categoryCode = "CONSULTING",
          problemDescription = "Seeking certified Chartered Accountant or Tax Consultant for Q4 GST filing, ITC mismatch reconciliation, and corporate compliance audit.",
          budget = "₹6,000 - ₹10,000",
          urgency = "FLEXIBLE",
          tags = "GST & Tax Audit, Tax Planning, Chartered Accountant, Compliance",
          serviceMode = "HYBRID",
          photoAttached = false,
          radiusMeters = 5000,
          status = "CREATED",
          distanceMeters = 420,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 60,
          expiryMinutes = 180
        ),
        ServiceRequestEntity(
          requestId = "req-elec-500m",
          requesterUserId = "user-requester-kavita",
          requesterName = "Kavita Verma",
          requesterLocality = "Kurla West",
          professionCode = "ELEC",
          title = "Kitchen Switchboard Sparking & MCB Tripping",
          category = "Electrical",
          categoryCode = "ELECTRICAL",
          problemDescription = "My kitchen switchboard is sparking when microwave turns on. Need urgent inspection of internal wiring and MCB breaker.",
          budget = "₹500 - ₹900",
          urgency = "URGENT",
          tags = "Switchboard, Short Circuit, MCB Tripping, Wiring",
          serviceMode = "ON_SITE",
          photoAttached = true,
          radiusMeters = 500,
          status = "INTERESTED",
          respondingProfessionalId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          respondingProfessionalName = "Raj Electrical Services",
          respondingProfessionalBusyCardNetId = "IN-MH-MU-26-581ELEC347",
          respondingProfessionalRating = 4.9f,
          distanceMeters = 250,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 10,
          expiryMinutes = 30
        ),
        ServiceRequestEntity(
          requestId = "req-free-brand-05",
          requesterUserId = "user-requester-aditya",
          requesterName = "Aditya Kapoor (Café Green)",
          requesterLocality = "Kalina, Santacruz",
          professionCode = "DESIGN",
          title = "Brand Identity & Digital Visiting Card Graphics",
          category = "Freelance",
          categoryCode = "FREELANCE",
          problemDescription = "Need a freelance brand designer for minimalist café logo refresh, menu layout, and NFC visiting card vector templates.",
          budget = "₹12,000 Fixed",
          urgency = "STANDARD",
          tags = "Logo Design, Branding, Vector Graphics, UI/UX Design",
          serviceMode = "REMOTE",
          photoAttached = false,
          radiusMeters = 1500,
          status = "CREATED",
          distanceMeters = 800,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 90,
          expiryMinutes = 120
        ),
        ServiceRequestEntity(
          requestId = "req-cons-legal-06",
          requesterUserId = "user-requester-neha",
          requesterName = "Adv. Neha & Partners",
          requesterLocality = "BKC G-Block",
          professionCode = "LEGAL",
          title = "Founders' Agreement & IP Assignment Legal Advisory",
          category = "Consulting",
          categoryCode = "CONSULTING",
          problemDescription = "Need corporate legal counsel to review SaaS co-founder equity vesting agreement, proprietary tech assignment clauses, and NDA draft.",
          budget = "₹15,000 Fixed",
          urgency = "FLEXIBLE",
          tags = "Legal Contract, Business Strategy, Startup Advisory, IP Rights",
          serviceMode = "REMOTE",
          photoAttached = false,
          radiusMeters = 3000,
          status = "CREATED",
          distanceMeters = 950,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 120,
          expiryMinutes = 240
        ),
        ServiceRequestEntity(
          requestId = "req-plum-tank-07",
          requesterUserId = "user-requester-sunil",
          requesterName = "Sunil Patil (Om Shivam CHS)",
          requesterLocality = "Kurla West",
          professionCode = "PLUM",
          title = "Overhead Society Water Tank Float Valve & Booster Pump",
          category = "Plumbing",
          categoryCode = "PLUMBING",
          problemDescription = "Overhead 5,000L water tank overflow sensor ballcock is broken. Need experienced commercial plumber with booster pump experience.",
          budget = "₹1,800 - ₹3,500",
          urgency = "URGENT",
          tags = "Water Tank, Pump Check, Pipe Replacement, Valve Fix",
          serviceMode = "ON_SITE",
          photoAttached = true,
          radiusMeters = 500,
          status = "CREATED",
          distanceMeters = 320,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 25,
          expiryMinutes = 45
        ),
        ServiceRequestEntity(
          requestId = "req-carp-door-08",
          requesterUserId = "user-requester-ramesh",
          requesterName = "Ramesh Iyer",
          requesterLocality = "Phoenix Mall Road",
          professionCode = "CARP",
          title = "Teak Main Door Smart Digital Lock Mortise Fitting",
          category = "Carpentry",
          categoryCode = "CARPENTRY",
          problemDescription = "Need skilled carpenter to carve precise lock mortise hole and secure smart fingerprint lock onto heavy teak door without damaging veneer.",
          budget = "₹900 - ₹1,500",
          urgency = "STANDARD",
          tags = "Door Lock Fix, Wood Polishing, Furniture Repair",
          serviceMode = "ON_SITE",
          photoAttached = true,
          radiusMeters = 800,
          status = "CREATED",
          distanceMeters = 400,
          createdAt = System.currentTimeMillis() - 1000 * 60 * 80,
          expiryMinutes = 60
        )
      )
      categorizedRequests.forEach { dao.insertServiceRequest(it) }

      // Initial Chat message for this request
      dao.insertChatMessage(
        ChatMessageEntity(
          id = "chat-msg-1",
          serviceRequestId = "req-elec-500m",
          senderId = "user-requester-kavita",
          senderName = "Kavita Verma",
          messageText = "Hello Rajesh ji, can you come check the kitchen switch? We switched off the MCB for safety.",
          timestamp = System.currentTimeMillis() - 1000 * 60 * 8,
          isFromMe = false
        )
      )
      dao.insertChatMessage(
        ChatMessageEntity(
          id = "chat-msg-2",
          serviceRequestId = "req-elec-500m",
          senderId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          senderName = "Rajesh Sharma",
          messageText = "Yes Kavita ji, I am just 250m away near Station Road. Arriving in 10 minutes with replacement MCB and switch kit.",
          timestamp = System.currentTimeMillis() - 1000 * 60 * 6,
          isFromMe = true
        )
      )

      // 6. Marketplace Products (Prompt Example: Normal person selling used office chair, etc.)
      val products = listOf(
        MarketplaceProductEntity(
          id = "prod-chair-01",
          sellerUserId = "user-amit-kurla",
          sellerName = "Amit Patil",
          sellerBusyCardNetId = "IN-MH-MU-26-441SALE902",
          title = "Ergonomic Mesh Office Chair",
          description = "High-back adjustable lumbar support, 360-degree swivel, barely used for 6 months. Moving out of flat.",
          price = 2500.0,
          condition = "USED",
          category = "FURNITURE",
          locationName = "Kurla West",
          distanceMeters = 350,
          pickupOption = "PICKUP",
          contactPhone = "+91 98333 44556"
        ),
        MarketplaceProductEntity(
          id = "prod-drill-02",
          sellerUserId = "user-suresh-elec",
          sellerName = "Suresh K.",
          sellerBusyCardNetId = "IN-MH-MU-26-118TECH501",
          title = "DeWalt Cordless Impact Drill 18V",
          description = "Original DeWalt drill with 2 batteries and charger kit. Fully working condition.",
          price = 4800.0,
          condition = "USED",
          category = "TOOLS",
          locationName = "Kurla East",
          distanceMeters = 800,
          pickupOption = "BOTH",
          contactPhone = "+91 98444 55667"
        ),
        MarketplaceProductEntity(
          id = "prod-table-03",
          sellerUserId = "user-vikram",
          sellerName = "Vikram Rane",
          sellerBusyCardNetId = "IN-MH-MU-26-923SALE119",
          title = "Solid Sheesham Wood Study Table",
          description = "Sturdy wooden table with 2 drawers. Dimensions 4x2 feet, perfect for work from home.",
          price = 1500.0,
          condition = "USED",
          category = "FURNITURE",
          locationName = "Bandra West",
          distanceMeters = 2400,
          pickupOption = "PICKUP",
          contactPhone = "+91 98555 66778"
        ),
        MarketplaceProductEntity(
          id = "prod-monitor-04",
          sellerUserId = "user-tech-hub",
          sellerName = "Pooja Mehta",
          sellerBusyCardNetId = "IN-MH-MU-26-302TECH884",
          title = "Dell 24-inch IPS FHD Monitor",
          description = "Brand new sealed box, HDMI & DisplayPort, 75Hz refresh rate with 3-year brand warranty.",
          price = 7200.0,
          condition = "NEW",
          category = "ELECTRONICS",
          locationName = "Chembur",
          distanceMeters = 3100,
          pickupOption = "DELIVERY",
          contactPhone = "+91 98666 77889"
        )
      )
      products.forEach { dao.insertProduct(it) }

      // 7. Creator Card Templates (Prompt Section 12)
      val templates = listOf(
        CardTemplateEntity(
          id = "tmpl-elec-pro",
          creatorUserId = "creator-priya",
          creatorName = "Priya Deshmukh (UI Designer)",
          title = "Electric Pro High-Voltage Card",
          professionCode = "ELEC",
          price = 149.0,
          rating = 4.9f,
          downloadsCount = 1280,
          previewStyle = "ELECTRIC_AMBER"
        ),
        CardTemplateEntity(
          id = "tmpl-corp-navy",
          creatorUserId = "creator-arjun",
          creatorName = "Arjun Verma Studios",
          title = "Executive Obsidian & Gold",
          professionCode = "BIZ",
          price = 249.0,
          rating = 4.8f,
          downloadsCount = 2150,
          previewStyle = "LUXURY_GOLD"
        ),
        CardTemplateEntity(
          id = "tmpl-doc-clinic",
          creatorUserId = "creator-priya",
          creatorName = "Priya Deshmukh (UI Designer)",
          title = "Medical Clinic Minimalist White",
          professionCode = "DOCT",
          price = 0.0, // Free template
          rating = 4.7f,
          downloadsCount = 3400,
          previewStyle = "MINIMAL_LIGHT"
        ),
        CardTemplateEntity(
          id = "tmpl-lawyer-crimson",
          creatorUserId = "creator-rohan",
          creatorName = "Rohan Graphic Works",
          title = "Chambers Legal Heritage",
          professionCode = "LAWY",
          price = 199.0,
          rating = 4.9f,
          downloadsCount = 890,
          previewStyle = "CORPORATE"
        )
      )
      dao.insertTemplates(templates)

      // 8. Physical Card Suppliers & Manufacturers (Prompt Section 13)
      val suppliers = listOf(
        PhysicalSupplierEntity(
          id = "sup-metro-print",
          companyName = "Metro Print & Card Works",
          busyCardNetId = "IN-MH-MU-26-891SUPP204",
          cardTypesOffered = "100 Paper Cards, 500 Premium Velvet, PVC QR Cards",
          minOrderQty = 100,
          pricePerUnit = 2.5,
          rating = 4.8f,
          deliveryDays = 3,
          location = "Lower Parel, Mumbai"
        ),
        PhysicalSupplierEntity(
          id = "sup-smart-nfc",
          companyName = "SmartTap NFC Technologies",
          busyCardNetId = "IN-MH-MU-26-312SUPP883",
          cardTypesOffered = "Matte Black NFC Cards, Metal Smart Cards, Wood Hybrid Cards",
          minOrderQty = 1,
          pricePerUnit = 499.0,
          rating = 4.9f,
          deliveryDays = 2,
          location = "Andheri East, Mumbai"
        ),
        PhysicalSupplierEntity(
          id = "sup-quick-cards",
          companyName = "Express Visiting Card Factory",
          busyCardNetId = "IN-MH-MU-26-449SUPP611",
          cardTypesOffered = "Gloss UV Visiting Cards, Embossed Gold Foil, Plastic Cards",
          minOrderQty = 200,
          pricePerUnit = 1.8,
          rating = 4.6f,
          deliveryDays = 4,
          location = "Dadar, Mumbai"
        )
      )
      dao.insertSuppliers(suppliers)

      // 9. Initial Bridge Connections (Prompt: Connect request & bridge building)
      val initialConnections = listOf(
        CardConnectionEntity(
          id = "conn-001",
          requesterUserId = "user-anil-plum",
          requesterCardId = "card-plum-002",
          requesterName = "Anil Kadam",
          requesterBusyCardNetId = "IN-MH-MU-26-735PLUM402",
          requesterProfession = "Plumber",
          targetUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          targetCardId = "card-elec-001",
          targetName = "Rajesh Sharma",
          targetBusyCardNetId = "IN-MH-MU-26-581ELEC347",
          targetProfession = "Electrician",
          status = "CONNECTED",
          bridgeLevel = "TRUSTED_BRIDGE",
          note = "Active Cross-Trade Bridge (Plumbing & Electrical)"
        ),
        CardConnectionEntity(
          id = "conn-002",
          requesterUserId = "user-hardware-bharat",
          requesterCardId = "card-shop-003",
          requesterName = "Bharat Tools Mart",
          requesterBusyCardNetId = "IN-MH-MU-26-891SHOP512",
          requesterProfession = "Retail Shop",
          targetUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          targetCardId = "card-elec-001",
          targetName = "Rajesh Sharma",
          targetBusyCardNetId = "IN-MH-MU-26-581ELEC347",
          targetProfession = "Electrician",
          status = "CONNECTED",
          bridgeLevel = "BUSINESS_ALLIANCE",
          note = "Hardware supplier & contractor bridge"
        ),
        CardConnectionEntity(
          id = "conn-003",
          requesterUserId = "user-dr-anita",
          requesterCardId = "card-doc-004",
          requesterName = "Dr. Anita Deshmukh",
          requesterBusyCardNetId = "IN-MH-MU-26-264DOCT918",
          requesterProfession = "Doctor",
          targetUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          targetCardId = "card-elec-001",
          targetName = "Rajesh Sharma",
          targetBusyCardNetId = "IN-MH-MU-26-581ELEC347",
          targetProfession = "Electrician",
          status = "PENDING",
          bridgeLevel = "BRIDGE",
          note = "Clinic electrical maintenance connect request"
        )
      )
      initialConnections.forEach { dao.insertConnection(it) }

      // 10. Initial 24h User Statuses across Bridges
      val statuses = listOf(
        UserStatusEntity(
          id = "status-001",
          userId = "user-anil-plum",
          busyCardNetId = "IN-MH-MU-26-735PLUM402",
          authorName = "Anil Kadam (Plumber)",
          authorProfession = "Plumber",
          statusText = "Available for urgent leakages & bathroom fittings in Kurla / Chembur area. 30-min response time! 🔧💧",
          statusEmoji = "💧",
          pageLink = "https://busycardnet.com/in-mh-mu-26-735plum402"
        ),
        UserStatusEntity(
          id = "status-002",
          userId = "user-hardware-bharat",
          busyCardNetId = "IN-MH-MU-26-891SHOP512",
          authorName = "Bharat Tools Mart",
          authorProfession = "Supplier",
          statusText = "Special discount for all connected cardholder bridges today: 15% off Havells copper wires & switchgears! ⚡📦",
          statusEmoji = "🏷️",
          pageLink = "https://busycardnet.com/in-mh-mu-26-891shop512"
        ),
        UserStatusEntity(
          id = "status-003",
          userId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          busyCardNetId = "IN-MH-MU-26-581ELEC347",
          authorName = "Rajesh Sharma (Electrician)",
          authorProfession = "Electrician",
          statusText = "On-site industrial rewiring project in Kurla West. Open for emergency calls after 5:00 PM. ⚡",
          statusEmoji = "⚡"
        )
      )
      statuses.forEach { dao.insertStatus(it) }

      // 11. Initial Video Posts & Business Page Showcases
      val videoPosts = listOf(
        FeedPostEntity(
          id = "video-post-001",
          authorUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          authorName = "Raj Electrical Services",
          authorBusyCardNetId = "IN-MH-MU-26-581ELEC347",
          authorProfession = "Electrician",
          content = "⚡ Live Demo: Complete Distribution Board & MCB Tripping Overhaul. See how properly calibrated RCCB protects homes from kitchen switchboard sparks.",
          mediaType = "VIDEO",
          mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
          locationName = "Kurla West",
          targetRadiusMeters = 3000,
          postScope = "VIDEO",
          likesCount = 89,
          commentsCount = 14,
          timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
          ctaType = "CALL",
          ctaPhone = "+91 98765 43210"
        ),
        FeedPostEntity(
          id = "video-post-002",
          authorUserId = "user-anil-plum",
          authorName = "QuickFix Plumbing Solutions",
          authorBusyCardNetId = "IN-MH-MU-26-735PLUM402",
          authorProfession = "Plumber",
          content = "🚿 Video Showcase: Concealed pipe thermal detection & non-destructive leakage repair in high-rise apartments.",
          mediaType = "VIDEO",
          mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
          locationName = "Bandra East",
          targetRadiusMeters = 5000,
          postScope = "VIDEO",
          likesCount = 124,
          commentsCount = 22,
          timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
          ctaType = "WHATSAPP",
          ctaPhone = "+91 97654 32109"
        ),
        FeedPostEntity(
          id = "video-post-003",
          authorUserId = "user-creator-priya",
          authorName = "Priya Deshmukh Studios",
          authorBusyCardNetId = "IN-MH-MU-26-902DSGN118",
          authorProfession = "Card Designer",
          content = "🎨 Creator Spotlight: Introducing the 'Electric Pro Neon' visiting card template with active 1-tap WhatsApp, LinkedIn & NFC smart tap integration!",
          mediaType = "VIDEO",
          mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
          locationName = "Mumbai Central",
          targetRadiusMeters = 10000,
          postScope = "VIDEO",
          likesCount = 215,
          commentsCount = 38,
          timestamp = System.currentTimeMillis() - 1000 * 60 * 360,
          ctaType = "VIEW_CARD",
          ctaPhone = ""
        )
      )
      videoPosts.forEach { dao.insertPost(it) }

      // 12. Initial Auction Items & Bids
      val now = System.currentTimeMillis()
      val auctions = listOf(
        AuctionItemEntity(
          id = "auc-camera-01",
          sellerUserId = "user-creator-priya",
          sellerName = "Priya Deshmukh",
          sellerPhone = "+91 98200 11223",
          sellerBusyCardNetId = "IN-MH-MU-26-804PHO112",
          title = "Sony Alpha A7 III Full-Frame Camera + 28-70mm Lens Kit",
          description = "Pristine condition professional mirrorless camera with 24.2MP Exmor R CMOS sensor, 4K HDR video, 2 original Sony batteries, 64GB Extreme Pro SD card, and camera rig. Shutter count under 8,200.",
          category = "ELECTRONICS",
          imageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=800&q=80",
          startingPrice = 55000.0,
          currentBid = 68500.0,
          highestBidderId = "user-suresh-elec",
          highestBidderName = "Suresh K.",
          upfrontPercent = 15,
          durationDays = 3,
          startTime = now - (1000 * 60 * 60 * 12),
          endTime = now + (1000 * 60 * 60 * 48), // 2 days left
          status = "ACTIVE",
          totalBids = 6,
          locationName = "Kurla West, Mumbai"
        ),
        AuctionItemEntity(
          id = "auc-tools-02",
          sellerUserId = "user-hardware-bharat",
          sellerName = "Bharat Tools Mart",
          sellerPhone = "+91 98444 55667",
          sellerBusyCardNetId = "IN-MH-MU-26-440TOOL901",
          title = "Bosch Heavy Duty 18V Brushless Combo Kit (Drill + Grinder + 2x 4Ah)",
          description = "Original Bosch Heavy-Duty power tool kit including GSB 18V brushless impact drill, GWS 18V cordless angle grinder, quick charger GAL 18V-40, and heavy L-BOXX carry case. Brand warranty intact.",
          category = "TOOLS",
          imageUrl = "https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=800&q=80",
          startingPrice = 9000.0,
          currentBid = 13500.0,
          highestBidderId = "user-anil-plum",
          highestBidderName = "Anil Kadam",
          upfrontPercent = 15,
          durationDays = 2,
          startTime = now - (1000 * 60 * 60 * 24),
          endTime = now + (1000 * 60 * 60 * 8), // 8 hours left
          status = "ACTIVE",
          totalBids = 9,
          locationName = "Dadar East, Mumbai"
        ),
        AuctionItemEntity(
          id = "auc-mac-03",
          sellerUserId = "user-tech-hub",
          sellerName = "Pooja Mehta",
          sellerPhone = "+91 98666 77889",
          sellerBusyCardNetId = "IN-MH-MU-26-610TECH554",
          title = "Apple MacBook Air M2 (16GB Unified Memory, 512GB SSD, Space Gray)",
          description = "Liquid Retina display with True Tone, 1080p FaceTime HD camera, MagSafe 3 charging port, 35W Dual USB-C port power adapter. Battery health 98% (42 cycles). Flawless body with skin applied.",
          category = "ELECTRONICS",
          imageUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80",
          startingPrice = 48000.0,
          currentBid = 62000.0,
          highestBidderId = "user-vikram",
          highestBidderName = "Vikram Rane",
          upfrontPercent = 10,
          durationDays = 4,
          startTime = now - (1000 * 60 * 60 * 6),
          endTime = now + (1000 * 60 * 60 * 72), // 3 days left
          status = "ACTIVE",
          totalBids = 11,
          locationName = "Bandra West, Mumbai"
        ),
        AuctionItemEntity(
          id = "auc-watch-04",
          sellerUserId = "user-dr-anita",
          sellerName = "Dr. Anita Deshmukh",
          sellerPhone = "+91 98777 88990",
          sellerBusyCardNetId = "IN-MH-MU-26-773DOC209",
          title = "Vintage Swiss Tissot Heritage 1973 Chronograph Watch (Limited Edition)",
          description = "Authentic Swiss automatic mechanical chronograph, sapphire crystal dome, genuine perforated racing leather strap. Comes with original inner and outer box, warranty card, and certificate of authenticity.",
          category = "COLLECTIBLES",
          imageUrl = "https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=800&q=80",
          startingPrice = 20000.0,
          currentBid = 31500.0,
          highestBidderId = "user-suresh-elec",
          highestBidderName = "Suresh K.",
          upfrontPercent = 20,
          durationDays = 1,
          startTime = now - (1000 * 60 * 60 * 20),
          endTime = now + (1000 * 60 * 18), // 18 minutes left (Live fast ending demonstration!)
          status = "ACTIVE",
          totalBids = 7,
          locationName = "Colaba, Mumbai"
        )
      )
      dao.insertAuctionItems(auctions)

      // Sample Initial Bids
      val initialBids = listOf(
        AuctionBidEntity(
          id = "bid-c1",
          auctionItemId = "auc-camera-01",
          bidderUserId = "user-anil-plum",
          bidderName = "Anil Kadam",
          bidderPhone = "+91 98765 43210",
          bidAmount = 60000.0,
          upfrontDepositPaid = 9000.0,
          timestamp = now - (1000 * 60 * 60 * 8),
          isWinningBid = false,
          refundStatus = "REFUNDED"
        ),
        AuctionBidEntity(
          id = "bid-c2",
          auctionItemId = "auc-camera-01",
          bidderUserId = "user-suresh-elec",
          bidderName = "Suresh K.",
          bidderPhone = "+91 98444 55667",
          bidAmount = 68500.0,
          upfrontDepositPaid = 10275.0,
          timestamp = now - (1000 * 60 * 60 * 2),
          isWinningBid = true,
          refundStatus = "HELD"
        ),
        AuctionBidEntity(
          id = "bid-w1",
          auctionItemId = "auc-watch-04",
          bidderUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          bidderName = "Rajesh Sharma",
          bidderPhone = "+91 98111 22334",
          bidAmount = 26000.0,
          upfrontDepositPaid = 5200.0,
          timestamp = now - (1000 * 60 * 60 * 5),
          isWinningBid = false,
          refundStatus = "REFUNDED"
        ),
        AuctionBidEntity(
          id = "bid-w2",
          auctionItemId = "auc-watch-04",
          bidderUserId = "user-suresh-elec",
          bidderName = "Suresh K.",
          bidderPhone = "+91 98444 55667",
          bidAmount = 31500.0,
          upfrontDepositPaid = 6300.0,
          timestamp = now - (1000 * 60 * 30),
          isWinningBid = true,
          refundStatus = "HELD"
        )
      )
      initialBids.forEach { dao.insertAuctionBid(it) }

      // Initial Verified Business Reviews (Star Rating & Review System)
      val seedReviews = listOf(
        BusinessReviewEntity(
          id = "rev-elec-01",
          businessId = "sp-elec-01",
          businessName = "Raj Electrical Services",
          busyCardNetId = "IN-MH-MU-26-581ELEC347",
          reviewerUserId = "user-vikram",
          reviewerName = "Vikram Rane",
          reviewerBusyCardNetId = "IN-MH-MU-26-442MECH119",
          rating = 5,
          reviewText = "Arrived in 12 minutes during a sudden main MCB power blackout! Diagnosed the short circuit behind the kitchen stove safely. Extremely professional and fair pricing.",
          serviceCategory = "Emergency Wiring",
          helpfulCount = 14,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 2)
        ),
        BusinessReviewEntity(
          id = "rev-elec-02",
          businessId = "sp-elec-01",
          businessName = "Raj Electrical Services",
          busyCardNetId = "IN-MH-MU-26-581ELEC347",
          reviewerUserId = "user-dr-anita",
          reviewerName = "Dr. Anita Deshmukh",
          reviewerBusyCardNetId = "IN-MH-MU-26-264DOCT918",
          rating = 5,
          reviewText = "Rajesh installed our clinic backup inverter and surge protector. Flawless workmanship, neat conduit piping, and very courteous demeanor.",
          serviceCategory = "Inverter Setup",
          helpfulCount = 9,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 5)
        ),
        BusinessReviewEntity(
          id = "rev-elec-03",
          businessId = "sp-elec-01",
          businessName = "Raj Electrical Services",
          busyCardNetId = "IN-MH-MU-26-581ELEC347",
          reviewerUserId = "user-anil-plum",
          reviewerName = "Anil Kadam",
          reviewerBusyCardNetId = "IN-MH-MU-26-735PLUM402",
          rating = 4,
          reviewText = "Collaborated on several residential renovation projects. Rajesh is always on time, transparent with quotation, and carries top-grade copper wire.",
          serviceCategory = "Commercial Wiring",
          helpfulCount = 6,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 9)
        ),
        BusinessReviewEntity(
          id = "rev-plum-01",
          businessId = "sp-plum-01",
          businessName = "QuickFix Plumbing Solutions",
          busyCardNetId = "IN-MH-MU-26-735PLUM402",
          reviewerUserId = "01991B72-8F4A-7C21-B6D3-91A2F45C8E11",
          reviewerName = "Rajesh Sharma",
          reviewerBusyCardNetId = "IN-MH-MU-26-581ELEC347",
          rating = 5,
          reviewText = "Fixed a high-pressure pipe rupture in our bathroom within 20 minutes of placing the request. Brought all necessary CPVC brass fittings with him.",
          serviceCategory = "Emergency Leak Fix",
          helpfulCount = 11,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 3)
        ),
        BusinessReviewEntity(
          id = "rev-plum-02",
          businessId = "sp-plum-01",
          businessName = "QuickFix Plumbing Solutions",
          busyCardNetId = "IN-MH-MU-26-735PLUM402",
          reviewerUserId = "user-shinde",
          reviewerName = "Adv. Vikrant Shinde",
          reviewerBusyCardNetId = "IN-MH-MU-26-421LAWY683",
          rating = 5,
          reviewText = "Installed premium sanitary fittings and overhead tank automatic float valve for our office. Clean work with zero post-installation dripping.",
          serviceCategory = "Sanitary Installation",
          helpfulCount = 8,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 7)
        ),
        BusinessReviewEntity(
          id = "rev-doc-01",
          businessId = "sp-doc-01",
          businessName = "City Health Clinic",
          busyCardNetId = "IN-MH-MU-26-264DOCT918",
          reviewerUserId = "user-suresh-elec",
          reviewerName = "Suresh K.",
          reviewerBusyCardNetId = "IN-MH-MU-26-108RES390",
          rating = 5,
          reviewText = "Dr. Anita listened with genuine patience and provided accurate clinical care. Minimal waiting time and very hygienic premises.",
          serviceCategory = "General Consultation",
          helpfulCount = 16,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 4)
        ),
        BusinessReviewEntity(
          id = "rev-law-01",
          businessId = "sp-law-01",
          businessName = "Shinde & Associates Law Firm",
          busyCardNetId = "IN-MH-MU-26-421LAWY683",
          reviewerUserId = "user-tech-hub",
          reviewerName = "Pooja Mehta",
          reviewerBusyCardNetId = "IN-MH-MU-26-610TECH554",
          rating = 5,
          reviewText = "Adv. Vikrant drafted our commercial shop lease agreement with great attention to detail and protected our rights. Highly recommended for property matters.",
          serviceCategory = "Legal Documentation",
          helpfulCount = 7,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 6)
        ),
        BusinessReviewEntity(
          id = "rev-hard-01",
          businessId = "biz-hard-01",
          businessName = "Royal Hardware & Tools Mart",
          busyCardNetId = "IN-MH-MU-26-302HARD811",
          reviewerUserId = "user-anil-plum",
          reviewerName = "Anil Kadam",
          reviewerBusyCardNetId = "IN-MH-MU-26-735PLUM402",
          rating = 5,
          reviewText = "Best hardware inventory in Kurla West. Authentic Bosch and Stanley power tools with manufacturer warranty and direct wholesale discount.",
          serviceCategory = "Tools & Supplies",
          helpfulCount = 12,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 1)
        ),
        BusinessReviewEntity(
          id = "rev-tech-01",
          businessId = "biz-tech-01",
          businessName = "TechFix Laptop Care & IT Solutions",
          busyCardNetId = "IN-MH-MU-26-884TECH701",
          reviewerUserId = "user-vikram",
          reviewerName = "Vikram Rane",
          reviewerBusyCardNetId = "IN-MH-MU-26-442MECH119",
          rating = 5,
          reviewText = "Upgraded my Dell workstation to 1TB NVMe SSD and cloned my Windows drive in 40 minutes flat. Excellent technical competence!",
          serviceCategory = "Hardware Upgrade",
          helpfulCount = 10,
          isVerifiedClient = true,
          timestamp = now - (1000L * 60 * 60 * 24 * 3)
        )
      )
      dao.insertReviews(seedReviews)

      // Seed Offline Cached Business Cards
      val seedCachedCards = listOf(
        CachedBusinessCardEntity(
          busyCardNetId = "IN-MH-MU-26-581ELEC347",
          cardId = "card-elec-01",
          internalUserId = "user-ramesh-elec",
          businessName = "Sharma Electrical & Smart Automation",
          ownerName = "Ramesh Sharma",
          designation = "Licensed Master Electrician & IoT Integrator",
          professionCode = "ELEC",
          professionTitle = "Master Electrician",
          services = "Residential Wiring, Inverter Installations, Industrial Panels, Short Circuit Diagnostics, Smart Home Lighting",
          phone = "+91 98201 54321",
          whatsapp = "+91 98201 54321",
          email = "ramesh.elec@busycardnet.in",
          website = "https://sharma-electricals.bcn.in",
          address = "Shop 4, Star Plaza, LBS Marg, Kurla West, Mumbai 400070",
          locality = "Kurla West, Mumbai",
          workingHours = "8:00 AM - 10:00 PM (Emergency 24x7)",
          about = "Govt. Licensed Grade-A Electrical contractor with 14+ years experience solving power tripping, phase faults, and industrial wire diagnostics.",
          rating = 4.9f,
          reviewCount = 38,
          templateStyle = "MODERN",
          accentColorHex = "#2563EB",
          photoUrl = "",
          lastVisitedTimestamp = now - 1000L * 60 * 30,
          isOfflineAvailable = true
        ),
        CachedBusinessCardEntity(
          busyCardNetId = "IN-MH-MU-26-735PLUM402",
          cardId = "card-plum-01",
          internalUserId = "user-vijay-plum",
          businessName = "Apex Plumbing & High-Pressure Drainage Solutions",
          ownerName = "Vijay Kumar",
          designation = "Sanitary Engineer & Pipeline Specialist",
          professionCode = "PLUM",
          professionTitle = "Plumber & Sanitary Specialist",
          services = "Seepage Detection, Hydro-jetting Clogs, Concealed Bathroom Fittings, Water Motor Setup, Overhead Tank Piping",
          phone = "+91 98192 67890",
          whatsapp = "+91 98192 67890",
          email = "apexplumbing.mum@busycardnet.in",
          website = "https://apexplumbing.bcn.in",
          address = "Gala 12, Municipal Industrial Estate, Kurla West, Mumbai 400070",
          locality = "Kurla West, Mumbai",
          workingHours = "7:30 AM - 9:00 PM",
          about = "Fast 15-minute emergency plumbing arrival for pipeline leaks, toilet blockages, and low-pressure repairs.",
          rating = 4.8f,
          reviewCount = 29,
          templateStyle = "CORPORATE",
          accentColorHex = "#0284C7",
          photoUrl = "",
          lastVisitedTimestamp = now - 1000L * 60 * 60 * 2,
          isOfflineAvailable = true
        ),
        CachedBusinessCardEntity(
          busyCardNetId = "IN-MH-MU-26-318DOCT920",
          cardId = "card-doc-01",
          internalUserId = "user-dr-ananya",
          businessName = "Sen PolyClinic, Diagnostic & Child Wellness",
          ownerName = "Dr. Ananya Sen",
          designation = "MBBS, MD (Family Medicine & Diagnostics)",
          professionCode = "DOCT",
          professionTitle = "Doctor / Physician",
          services = "General Consultation, ECG, Diabetes Monitoring, Pediatric Care, Preventative Health Screening",
          phone = "+91 98334 11223",
          whatsapp = "+91 98334 11223",
          email = "dr.ananyasen@busycardnet.in",
          website = "https://senclinic.bcn.in",
          address = "First Floor, Crystal Point, Near Station Road, Kurla West, Mumbai 400070",
          locality = "Kurla West, Mumbai",
          workingHours = "10:00 AM - 2:00 PM, 5:30 PM - 9:30 PM",
          about = "Compassionate community clinical consultation with in-house diagnostic reports and immediate digital prescription delivery.",
          rating = 5.0f,
          reviewCount = 44,
          templateStyle = "MINIMAL",
          accentColorHex = "#059669",
          photoUrl = "",
          lastVisitedTimestamp = now - 1000L * 60 * 60 * 5,
          isOfflineAvailable = true
        ),
        CachedBusinessCardEntity(
          busyCardNetId = "IN-MH-MU-26-421LAWY683",
          cardId = "card-law-01",
          internalUserId = "user-vikrant-law",
          businessName = "Shinde & Associates Advocates & Corporate Consultants",
          ownerName = "Adv. Vikrant Shinde",
          designation = "Senior High Court Advocate & Property Counsel",
          professionCode = "LAWY",
          professionTitle = "Lawyer & Property Counsel",
          services = "Property Title Search, Commercial Agreements, Civil Litigation, Consumer Protection, Corporate Compliance",
          phone = "+91 98205 99881",
          whatsapp = "+91 98205 99881",
          email = "vshinde.law@busycardnet.in",
          website = "https://shindeadvocates.bcn.in",
          address = "Suite 302, K Raheja Prime, Kurla West, Mumbai 400070",
          locality = "Kurla West, Mumbai",
          workingHours = "10:00 AM - 7:30 PM",
          about = "Expert legal counsel providing transparent advice, document verification, and litigation assistance.",
          rating = 4.9f,
          reviewCount = 22,
          templateStyle = "LUXURY",
          accentColorHex = "#7C3AED",
          photoUrl = "",
          lastVisitedTimestamp = now - 1000L * 60 * 60 * 12,
          isOfflineAvailable = true
        ),
        CachedBusinessCardEntity(
          busyCardNetId = "IN-MH-MU-26-302HARD811",
          cardId = "card-hard-01",
          internalUserId = "user-hard-store",
          businessName = "Royal Hardware & Tools Mart",
          ownerName = "Haresh Patel",
          designation = "Proprietor & Industrial Hardware Wholesaler",
          professionCode = "SUPP",
          professionTitle = "Hardware Supplier",
          services = "Power Tools, PVC Pipes & Fittings, Electrical Wiring Spools, Architectural Door Hinges, Paints",
          phone = "+91 98211 44556",
          whatsapp = "+91 98211 44556",
          email = "royalhardware@busycardnet.in",
          website = "https://royalhardware.bcn.in",
          address = "Opposite Phoenix Marketcity Gate 3, LBS Road, Kurla West, Mumbai",
          locality = "Kurla West, Mumbai",
          workingHours = "9:00 AM - 9:00 PM (Closed Sundays)",
          about = "Comprehensive hardware wholesale distributor stocking leading brands with instant same-day contractor delivery.",
          rating = 4.8f,
          reviewCount = 18,
          templateStyle = "CREATIVE",
          accentColorHex = "#D97706",
          photoUrl = "",
          lastVisitedTimestamp = now - 1000L * 60 * 60 * 18,
          isOfflineAvailable = true
        )
      )
      dao.insertCachedBusinessCards(seedCachedCards)
    }
  }
}
