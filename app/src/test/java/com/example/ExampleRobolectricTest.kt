package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BusyCardIdGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BusyCardNet", appName)
  }

  @Test
  fun `generate and validate BusyCardNet ID format`() {
    val id = BusyCardIdGenerator.generateId(
      countryCode = "IN",
      stateCode = "MH",
      cityCode = "MU",
      yearSuffix = "26",
      professionCode = "ELEC"
    )

    // Example format: IN-MH-MU-26-581ELEC347
    assertTrue("Generated ID should be valid: $id", BusyCardIdGenerator.validate(id))
    assertTrue("Generated ID should start with IN-MH-MU-26-", id.startsWith("IN-MH-MU-26-"))
    assertTrue("Generated ID should contain ELEC", id.contains("ELEC"))

    val parsed = BusyCardIdGenerator.parse(id)
    assertNotNull("Parsed ID must not be null", parsed)
    assertEquals("IN", parsed?.country)
    assertEquals("MH", parsed?.state)
    assertEquals("MU", parsed?.city)
    assertEquals("26", parsed?.year)
    assertEquals("ELEC", parsed?.professionCode)
  }

  @Test
  fun `generate short url for sharing`() {
    val testId = "IN-MH-MU-26-581ELEC347"
    val url = BusyCardIdGenerator.getShortUrl(testId)
    assertEquals("busycardnet.com/c/581ELEC347", url)
  }

  @Test
  fun `parse QR code from JSON payload`() {
    val jsonPayload = """
      {
        "busyCardNetId": "IN-MH-MU-26-581ELEC347",
        "name": "Rajesh Sharma",
        "designation": "Master Electrician",
        "businessName": "Sharma Electricals",
        "phone": "+919876543210",
        "whatsapp": "+919876543210",
        "email": "rajesh@sharma.in",
        "city": "Mumbai",
        "category": "Electrical & Home Power"
      }
    """.trimIndent()

    val parsedCard = com.example.util.CardQrParser.parse(jsonPayload)
    assertNotNull("Parsed card should not be null", parsedCard)
    assertEquals("IN-MH-MU-26-581ELEC347", parsedCard?.busyCardNetId)
    assertEquals("Rajesh Sharma", parsedCard?.name)
    assertEquals("Sharma Electricals", parsedCard?.businessName)
    assertTrue("CardId should start with imp-", parsedCard?.cardId?.startsWith("imp-") == true)
  }

  @Test
  fun `parse QR code from vCard text`() {
    val vcardText = """
      BEGIN:VCARD
      VERSION:3.0
      FN:Ananya Sen
      ORG:Apex Consulting
      TITLE:Senior Advisor
      TEL:+919822334455
      EMAIL:ananya@apex.com
      ADR:;;Pune;;;;
      NOTE:BCN_ID:IN-MH-PU-26-999CONS123
      END:VCARD
    """.trimIndent()

    val parsedCard = com.example.util.CardQrParser.parse(vcardText)
    assertNotNull("Parsed card should not be null", parsedCard)
    assertEquals("Ananya Sen", parsedCard?.name)
    assertEquals("Apex Consulting", parsedCard?.businessName)
    assertEquals("Senior Advisor", parsedCard?.designation)
    assertEquals("+919822334455", parsedCard?.phone)
    assertEquals("IN-MH-PU-26-999CONS123", parsedCard?.busyCardNetId)
  }

  @Test
  fun `parse QR code from raw BCN ID format`() {
    val rawId = "IN-MH-MU-26-581ELEC347"
    val parsedCard = com.example.util.CardQrParser.parse(rawId)
    assertNotNull("Parsed card should not be null", parsedCard)
    assertEquals("IN-MH-MU-26-581ELEC347", parsedCard?.busyCardNetId)
    assertEquals("ELEC Professional", parsedCard?.name)
  }

  @Test
  fun `google maps displays businesses and service providers within 500m radius`() {
    val places = com.example.ui.map.LocalPlaceDataProvider.getCuratedPlaces(
      currentLat = 19.0760,
      currentLng = 72.8777
    )
    assertTrue("Should contain curated places", places.isNotEmpty())

    // 1. 500m Radius filter
    val places500m = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(
      places = places,
      radiusMeters = 500
    )
    assertTrue("Should have places within 500m", places500m.isNotEmpty())
    places500m.forEach { place ->
      assertTrue("Distance ${place.distanceMeters}m must be <= 500m", place.distanceMeters <= 500)
    }

    // 2. Contains both Businesses and Service Providers within 500m
    val businesses500m = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(
      places = places,
      radiusMeters = 500,
      placeType = com.example.ui.map.PlaceType.LOCAL_BUSINESS
    )
    val providers500m = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(
      places = places,
      radiusMeters = 500,
      placeType = com.example.ui.map.PlaceType.SERVICE_PROVIDER
    )
    assertTrue("Must have local businesses within 500m", businesses500m.isNotEmpty())
    assertTrue("Must have service providers within 500m", providers500m.isNotEmpty())

    assertTrue("Businesses must be tagged LOCAL_BUSINESS", businesses500m.all { it.placeType == com.example.ui.map.PlaceType.LOCAL_BUSINESS })
    assertTrue("Providers must be tagged SERVICE_PROVIDER", providers500m.all { it.placeType == com.example.ui.map.PlaceType.SERVICE_PROVIDER })

    // 3. Search query filtering within 500m
    val elecSearch = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(
      places = places,
      radiusMeters = 500,
      searchQuery = "electrician"
    )
    assertTrue("Searching electrician within 500m should find Raj Electrical", elecSearch.any { it.name.contains("Electrical", true) })

    val plumbSearch = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(
      places = places,
      radiusMeters = 500,
      searchQuery = "plumbing"
    )
    assertTrue("Searching plumbing within 500m should find QuickFix Plumbing", plumbSearch.any { it.name.contains("Plumbing", true) })
  }

  @Test
  fun `service request category data model and filtering for plumbing, freelance, consulting`() {
    val plumbingReq = com.example.data.local.ServiceRequestEntity(
      requestId = "req-plum-test",
      requesterUserId = "user-1",
      requesterName = "Arjun Singhania",
      requesterLocality = "Kurla West",
      professionCode = "PLUM",
      title = "Bathroom pipe leak repair",
      category = "Plumbing",
      categoryCode = "PLUMBING",
      problemDescription = "Main flush valve leaking heavily onto floor tiles",
      budget = "₹800",
      urgency = "URGENT",
      tags = "Leakage Repair, Pipe Joint, Bathroom",
      serviceMode = "ON_SITE",
      photoAttached = true
    )

    val freelanceReq = com.example.data.local.ServiceRequestEntity(
      requestId = "req-free-test",
      requesterUserId = "user-2",
      requesterName = "Priya Deshmukh",
      requesterLocality = "BKC",
      professionCode = "TECH",
      title = "Android Jetpack Compose UI",
      category = "Freelance",
      categoryCode = "FREELANCE",
      problemDescription = "Need Compose developer to build 4 screens with Material 3",
      budget = "₹25,000",
      urgency = "STANDARD",
      tags = "Android Dev, Jetpack Compose, Kotlin",
      serviceMode = "REMOTE",
      photoAttached = false
    )

    val consultingReq = com.example.data.local.ServiceRequestEntity(
      requestId = "req-cons-test",
      requesterUserId = "user-3",
      requesterName = "Vikas Mehta",
      requesterLocality = "Kurla East",
      professionCode = "CA",
      title = "GST Annual Audit & Input Tax Credit",
      category = "Consulting",
      categoryCode = "CONSULTING",
      problemDescription = "CA or Tax Consultant for Q4 GST filing and ITC audit",
      budget = "₹8,000",
      urgency = "FLEXIBLE",
      tags = "GST & Tax Audit, Tax Planning, Compliance",
      serviceMode = "HYBRID",
      photoAttached = false
    )

    val allRequests = listOf(plumbingReq, freelanceReq, consultingReq)

    // 1. Verify Category matches
    assertTrue("Plumbing category matches plumbing request", com.example.data.model.ServiceRequestCategory.PLUMBING.matches(plumbingReq))
    org.junit.Assert.assertFalse("Freelance category does not match plumbing request", com.example.data.model.ServiceRequestCategory.FREELANCE.matches(plumbingReq))
    assertTrue("Freelance category matches freelance request", com.example.data.model.ServiceRequestCategory.FREELANCE.matches(freelanceReq))
    assertTrue("Consulting category matches consulting request", com.example.data.model.ServiceRequestCategory.CONSULTING.matches(consultingReq))
    assertTrue("ALL category matches every request", com.example.data.model.ServiceRequestCategory.ALL.matches(plumbingReq))

    // 2. Verify filter by Plumbing
    val plumbingFiltered = allRequests.filter { com.example.data.model.ServiceRequestCategory.PLUMBING.matches(it) }
    assertEquals(1, plumbingFiltered.size)
    assertEquals("req-plum-test", plumbingFiltered.first().requestId)

    // 3. Verify filter by Freelance
    val freelanceFiltered = allRequests.filter { com.example.data.model.ServiceRequestCategory.FREELANCE.matches(it) }
    assertEquals(1, freelanceFiltered.size)
    assertEquals("req-free-test", freelanceFiltered.first().requestId)

    // 4. Verify filter by Consulting
    val consultingFiltered = allRequests.filter { com.example.data.model.ServiceRequestCategory.CONSULTING.matches(it) }
    assertEquals(1, consultingFiltered.size)
    assertEquals("req-cons-test", consultingFiltered.first().requestId)

    // 5. Verify Urgency filter (Urgent)
    val urgentFiltered = allRequests.filter { it.urgency.equals("URGENT", true) }
    assertEquals(1, urgentFiltered.size)
    assertEquals("req-plum-test", urgentFiltered.first().requestId)

    // 6. Verify Mode filter (Remote)
    val remoteFiltered = allRequests.filter { it.serviceMode.equals("REMOTE", true) || it.serviceMode.equals("HYBRID", true) }
    assertEquals(2, remoteFiltered.size)

    // 7. Verify Tag list helper
    assertEquals(listOf("Leakage Repair", "Pipe Joint", "Bathroom"), plumbingReq.tagList)
  }

  @Test
  fun `broadcast task or request to businesses within 500m proximity`() {
    // 1. Verify Broadcast Preset Catalog exists with rich categories and quick presets
    val categories = com.example.ui.components.BroadcastPresetCatalog.categories
    assertTrue("Should contain broadcast categories", categories.isNotEmpty())

    val plumbingPreset = categories.find { it.code == "PLUM" }
    assertNotNull("Plumbing category preset must be available", plumbingPreset)
    assertEquals(14, plumbingPreset?.estimatedNearbyPros)
    assertTrue("Should have quick task presets", plumbingPreset?.quickTasks?.isNotEmpty() == true)

    val electricalPreset = categories.find { it.code == "ELEC" }
    assertNotNull("Electrical category preset must be available", electricalPreset)
    assertEquals(18, electricalPreset?.estimatedNearbyPros)

    // 2. Validate default 500m radius selection and request construction
    val broadcastRequest = com.example.data.local.ServiceRequestEntity(
      requestId = "req-500m-test",
      requesterUserId = "user-local-01",
      requesterName = "Rohan Verma",
      requesterLocality = "Andheri East, Sector 4, Mumbai",
      professionCode = "PLUM",
      title = "Kitchen Pipe Leakage Under Sink",
      category = "Plumbing",
      categoryCode = "PLUM",
      problemDescription = "Water leaking heavily under sink connector. Need plumber within 30 mins.",
      budget = "₹350 - ₹600",
      urgency = "URGENT",
      tags = "Leakage, Pipe Fix",
      serviceMode = "ON_SITE",
      photoAttached = true,
      radiusMeters = 500,
      status = "SENT",
      distanceMeters = 150
    )

    assertEquals(500, broadcastRequest.radiusMeters)
    assertEquals("SENT", broadcastRequest.status)
    assertTrue("Distance must be within 500m", broadcastRequest.distanceMeters <= 500)
    assertTrue("Photo should be attached", broadcastRequest.photoAttached)
    assertEquals("URGENT", broadcastRequest.urgency)
  }

  @Test
  fun `verify service request broadcast across 500m 1000m 2000m 5000m 10000m radii`() {
    val supportedRadii = listOf(500, 1000, 2000, 5000, 10000)
    val curatedPlaces = com.example.ui.map.LocalPlaceDataProvider.getCuratedPlaces()

    assertTrue("Curated places should exist for testing", curatedPlaces.isNotEmpty())

    for (radius in supportedRadii) {
      val placesWithin = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(
        places = curatedPlaces,
        radiusMeters = radius
      )
      // At least places within each radius should be valid and have distance <= radius
      assertTrue("Places count should be >= 0 for radius $radius", placesWithin.size >= 0)
      for (place in placesWithin) {
        assertTrue(
          "Place ${place.name} distance ${place.distanceMeters} must be <= $radius",
          place.distanceMeters <= radius
        )
      }

      val request = com.example.data.local.ServiceRequestEntity(
        requestId = "req-radius-$radius",
        requesterUserId = "user-test",
        requesterName = "Test User",
        requesterLocality = "Kurla West, Mumbai",
        professionCode = "PLUM",
        title = "Urgent Request ($radius m)",
        category = "Plumbing",
        categoryCode = "PLUM",
        problemDescription = "Broadcasting to nearby businesses in $radius meters",
        budget = "₹500",
        urgency = "URGENT",
        tags = "Plumbing",
        serviceMode = "ON_SITE",
        photoAttached = false,
        radiusMeters = radius,
        status = "SENT",
        distanceMeters = 100
      )

      assertEquals(radius, request.radiusMeters)
    }

    // Larger radius should return equal or more businesses than smaller radius
    val places500 = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(curatedPlaces, 500)
    val places10000 = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(curatedPlaces, 10000)
    assertTrue(
      "10000m should include at least as many places as 500m",
      places10000.size >= places500.size
    )
  }

  @Test
  fun `verify AuthManager demo persona authentication and session lifecycle`() = kotlinx.coroutines.test.runTest {
    val authManager = com.example.util.AuthManager.getInstance()

    // Test persona sign-in
    val persona = com.example.util.DEMO_PERSONAS.first()
    val session = authManager.signInWithDemoPersona(persona)

    assertNotNull("Session must be created", session)
    assertEquals(persona.name, session.displayName)
    assertEquals(persona.email, session.email)
    assertEquals("demo.busycardnet", session.providerId)
    assertTrue("Session UID should be set", session.uid.isNotBlank())
    assertTrue("Session token snippet should not be empty", session.tokenSnippet?.isNotBlank() == true)

    // Test token renewal
    val refreshedToken = authManager.refreshSessionToken()
    assertNotNull("Refreshed token must not be null", refreshedToken)

    // Test sign out
    authManager.signOut()
    val afterSignOut = authManager.sessionStateFlow.value
    org.junit.Assert.assertNull("Session must be cleared after sign-out", afterSignOut)
  }

  @Test
  fun `verify DigitalCard and VisitingCardQrGenerator payload generation`() {
    val card = com.example.data.local.DigitalCardEntity(
      cardId = "TEST-CARD-001",
      internalUserId = "user_001",
      busyCardNetId = "IN-MH-MU-26-999ELEC001",
      name = "Vikram Malhotra",
      designation = "Principal Cloud Architect",
      professionCode = "ELEC",
      businessName = "Malhotra Technologies",
      services = "Architecture, Cloud",
      phone = "+919876543210",
      whatsapp = "+919876543210",
      email = "vikram@malhotra.tech",
      website = "https://malhotra.tech",
      address = "Powai Tech Park, Mumbai",
      workingHours = "9 AM - 6 PM",
      about = "Experienced IT Consultant",
      templateStyle = "MODERN_DARK",
      accentColorHex = "#38BDF8"
    )

    // Verify QR payload building for all formats
    val webPayload = com.example.util.VisitingCardQrGenerator.buildEncodedPayload(
      card = card,
      type = com.example.util.VisitingCardQrGenerator.QrUrlType.FIRESTORE_WEB_PROFILE
    )
    val vcardPayload = com.example.util.VisitingCardQrGenerator.buildEncodedPayload(
      card = card,
      type = com.example.util.VisitingCardQrGenerator.QrUrlType.VCARD_WITH_FIRESTORE
    )
    val appPayload = com.example.util.VisitingCardQrGenerator.buildEncodedPayload(
      card = card,
      type = com.example.util.VisitingCardQrGenerator.QrUrlType.UNIVERSAL_DEEPLINK
    )

    assertTrue("Web payload must contain card ID", webPayload.contains(card.busyCardNetId))
    assertTrue("vCard payload must contain BEGIN:VCARD", vcardPayload.startsWith("BEGIN:VCARD"))
    assertTrue("vCard payload must contain name", vcardPayload.contains(card.name))
    assertTrue("App link must contain busycard.net domain", appPayload.contains("busycard.net"))

    // Verify QR bitmap generation does not crash
    val qrBitmap = com.example.util.VisitingCardQrGenerator.generateQrBitmap(
      content = webPayload,
      size = 200
    )
    assertNotNull("Generated QR bitmap should not be null", qrBitmap)
    assertEquals(200, qrBitmap.width)
    assertEquals(200, qrBitmap.height)
  }

  @Test
  fun `verify Firebase and Credential Manager account management lifecycle`() = kotlinx.coroutines.test.runTest {
    val authManager = com.example.util.AuthManager.getInstance()

    // 1. Sign in with simulated Google credentials
    val signInSuccess = authManager.signInDirect(
      displayName = "Aarav Patel",
      email = "aarav.patel@gmail.com",
      photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
    )
    assertTrue("Direct Google sign in should succeed", signInSuccess)

    val activeSession = authManager.sessionStateFlow.value
    assertNotNull("Active session should exist", activeSession)
    assertEquals("Aarav Patel", activeSession?.displayName)
    assertEquals("aarav.patel@gmail.com", activeSession?.email)
    assertEquals("google.com", activeSession?.providerId)
    assertTrue(activeSession?.isCredentialManagerSaved == true)

    // 2. Test profile updating
    val updateResult = authManager.updateUserProfile(
      displayName = "Aarav K. Patel",
      photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"
    )
    assertTrue("Profile update should succeed", updateResult.isSuccess)
    val updatedSession = updateResult.getOrNull()
    assertEquals("Aarav K. Patel", updatedSession?.displayName)

    // 3. Test email verification dispatch
    val emailResult = authManager.sendEmailVerification()
    assertTrue("Email verification request should succeed", emailResult.isSuccess)

    // 4. Test account deletion & session cleanup
    val deleteResult = authManager.deleteUserAccount()
    assertTrue("Account deletion should succeed", deleteResult.isSuccess)
    org.junit.Assert.assertNull("Session should be null after account deletion", authManager.sessionStateFlow.value)
  }

  @Test
  fun `AI visiting card generator produces tailored company words and theme`() = kotlinx.coroutines.runBlocking {
    val suggestion = com.example.util.VisitingCardAiGenerator.generateCardDesign(
      businessName = "Apex Electrical Solutions",
      ownerName = "Rajesh Sharma",
      professionOrCategory = "ELEC",
      customPrompt = "Executive luxury corporate style"
    )

    assertNotNull("AI suggestion must not be null", suggestion)
    assertTrue("Should generate primary company words", suggestion.primaryCompanyWords.isNotBlank())
    assertTrue("Should produce alternative slogans", suggestion.alternativeSlogans.isNotEmpty())
    assertEquals(com.example.data.model.CardPrintTheme.LUXURY_GOLD, suggestion.recommendedTheme)
  }

  @Test
  fun `card print options correctly handles element selection and content overrides`() {
    val testCard = com.example.data.local.DigitalCardEntity(
      cardId = "card-test-1",
      internalUserId = "user-test",
      busyCardNetId = "IN-MH-MU-26-108ELEC347",
      name = "Gajanan Sharma",
      designation = "Lead Electrical Contractor",
      professionCode = "ELEC",
      businessName = "Gajanan Electrical Works",
      services = "Wiring, Industrial Automation",
      phone = "+91 98200 99887",
      whatsapp = "+91 98200 99887",
      email = "gajanansibm@gmail.com",
      website = "https://gajananelectrical.com",
      facebook = "facebook.com/gajananworks",
      instagram = "@gajanan_works",
      address = "Shop 14, Station Road, Andheri East, Mumbai",
      workingHours = "9 AM - 8 PM",
      about = "Trusted electrical solutions since 2012.",
      photoUrl = "https://example.com/photo.jpg",
      templateStyle = "MODERN_DARK",
      accentColorHex = "#38BDF8",
      viewsCount = 120,
      sharesCount = 45,
      isPrimary = true
    )

    val options = com.example.data.model.CardPrintOptions.fromCard(testCard)

    // Verify all requested fields are initialized
    assertTrue(options.includePhoto)
    assertTrue(options.includeName)
    assertTrue(options.includeBcnId)
    assertTrue(options.includeBarcode)
    assertTrue(options.includeCompanyName)
    assertTrue(options.includeEmail)
    assertTrue(options.includeWebsite)
    assertTrue(options.includeFacebook)
    assertTrue(options.includeInstagram)
    assertTrue(options.includeWhatsApp)
    assertTrue(options.includeCompanyWords)
    assertTrue(options.includePhone)
    assertTrue(options.includeAddress)

    assertEquals("Gajanan Sharma", options.name)
    assertEquals("IN-MH-MU-26-108ELEC347", options.bcnId)
    assertEquals("Gajanan Electrical Works", options.companyName)
    assertEquals("gajanansibm@gmail.com", options.email)
    assertEquals("+91 98200 99887", options.whatsApp)
    assertEquals("https://gajananelectrical.com", options.website)
  }

  @Test
  fun `card print order workflow calculates pricing and records delivery address`() {
    val mfg = com.example.data.model.CardManufacturer(
      id = "mfg-metro",
      name = "Metro Print & Card Works",
      rating = 4.8f,
      deliveryDays = 3,
      location = "Lower Parel, Mumbai",
      basePrice100 = 250.0,
      description = "Official BCN Certified Partner",
      isOfficialBcn = true
    )

    val quality = com.example.data.model.CardQualityOption(
      id = "qual-velvet-450",
      name = "Velvet Soft-Touch",
      paperWeight = "450 GSM",
      finishDescription = "Suede-like luxury finish",
      priceMultiplier = 1.5
    )

    val address = com.example.data.model.CardDeliveryAddress(
      recipientName = "Gajanan Sharma",
      phone = "+91 98200 99887",
      streetAddress = "Flat 402, Royal Palms, Aarey Road",
      locality = "Goregaon East",
      city = "Mumbai",
      state = "Maharashtra",
      pinCode = "400065",
      deliveryNotes = "Call before delivery"
    )

    val order = com.example.data.model.CardPrintOrder(
      orderId = "BCN-PRNT-2026-9012",
      cardId = "card-test-1",
      options = com.example.data.model.CardPrintOptions(name = "Gajanan Sharma", bcnId = "IN-MH-MU-26-108ELEC347"),
      manufacturer = mfg,
      quality = quality,
      cornerStyle = com.example.data.model.CardCornerStyle.ROUNDED_3MM,
      quantity = 250,
      basePrice = 530.0,
      deliveryFee = 0.0,
      totalPrice = 530.0,
      deliveryAddress = address,
      estimatedDeliveryDays = 3
    )

    assertEquals("BCN-PRNT-2026-9012", order.orderId)
    assertEquals(250, order.quantity)
    assertEquals("Metro Print & Card Works", order.manufacturer.name)
    assertEquals("Velvet Soft-Touch", order.quality.name)
    assertTrue(order.deliveryAddress.fullAddressLine.contains("Goregaon East"))
    assertTrue(order.deliveryAddress.fullAddressLine.contains("400065"))
  }

  @Test
  fun `google maps sdk displays pins for businesses within 500m radius of dynamic user gps location`() {
    // Test with Mountain View, California coordinates (e.g. streaming emulator location)
    val testLat = 37.4220
    val testLng = -122.0841

    val placesAtGps = com.example.ui.map.LocalPlaceDataProvider.getCuratedPlaces(
      currentLat = testLat,
      currentLng = testLng
    )

    assertTrue("Should generate places at user GPS", placesAtGps.isNotEmpty())

    // Filter strictly within 500m
    val placesWithin500m = com.example.ui.map.LocalPlaceDataProvider.filterPlacesWithinRadius(
      places = placesAtGps,
      radiusMeters = 500
    )

    assertTrue("Must have businesses & services within 500m of user GPS", placesWithin500m.isNotEmpty())

    // Verify all filtered places are within 500m
    placesWithin500m.forEach { place ->
      assertTrue(
        "Place ${place.name} distance (${place.distanceMeters}m) must be <= 500m",
        place.distanceMeters <= 500
      )
    }

    // Verify businesses specifically within 500m
    val businesses500m = placesWithin500m.filter { it.isBusiness }
    assertTrue("Should have local businesses within 500m", businesses500m.isNotEmpty())
    businesses500m.forEach { biz ->
      assertEquals(com.example.ui.map.PlaceType.LOCAL_BUSINESS, biz.placeType)
      assertTrue(biz.name.isNotBlank())
      assertTrue(biz.phone.isNotBlank())
      assertTrue(biz.distanceMeters <= 500)
    }

    // Verify service providers specifically within 500m
    val providers500m = placesWithin500m.filter { it.isServiceProvider }
    assertTrue("Should have service providers within 500m", providers500m.isNotEmpty())
    providers500m.forEach { prov ->
      assertEquals(com.example.ui.map.PlaceType.SERVICE_PROVIDER, prov.placeType)
      assertTrue(prov.name.isNotBlank())
      assertTrue(prov.distanceMeters <= 500)
    }
  }

  @Test
  fun `verify premium typography and high-contrast readable color tokens`() {
    // Verify custom bundled Google fonts and Typography
    assertNotNull(com.example.ui.theme.PlusJakartaSansFontFamily)
    assertNotNull(com.example.ui.theme.OutfitFontFamily)
    val typography = com.example.ui.theme.Typography
    assertNotNull(typography.displayLarge)
    assertNotNull(typography.headlineMedium)
    assertNotNull(typography.bodyLarge)
    assertNotNull(typography.labelLarge)

    // Verify executive readable font tokens
    assertEquals(androidx.compose.ui.graphics.Color(0xFF0F172A), com.example.ui.theme.FontNavyHeadings)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF1E293B), com.example.ui.theme.FontRoyalBlue)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF2563EB), com.example.ui.theme.FontElectricBlue)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF64748B), com.example.ui.theme.FontMutedBlue)
  }
}

