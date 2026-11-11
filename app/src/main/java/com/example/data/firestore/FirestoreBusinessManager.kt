package com.example.data.firestore

import android.location.Location
import android.util.Log
import com.example.data.local.DigitalCardEntity
import com.example.ui.map.LocalMapPlaceItem
import com.example.ui.map.LocalPlaceDataProvider
import com.example.ui.map.PlaceType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages Local Business and Digital Visiting Card profiles in Firebase Firestore.
 * Collection: `local_businesses`
 * User Profile Card: `users/{userUid}/visiting_card`
 *
 * Visualizes local businesses within 500m, 1000m, 2000m, 5000m, 10000m radius of the user.
 */
object FirestoreBusinessManager {
  private const val TAG = "FirestoreBusinessMgr"
  private const val COLLECTION_LOCAL_BUSINESSES = "local_businesses"
  private const val COLLECTION_USERS = "users"
  private const val SUBCOLLECTION_CARD = "visiting_card"

  private fun getDb(): FirebaseFirestore? {
    return try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "Firestore instance unavailable (offline or test environment): ${e.message}")
      null
    }
  }

  /**
   * Calculates distance between two geographic coordinates in meters.
   */
  fun calculateDistanceMeters(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double
  ): Int {
    val results = FloatArray(1)
    Location.distanceBetween(startLat, startLng, endLat, endLng, results)
    return results[0].toInt()
  }

  /**
   * Saves or registers a business / visiting card profile to Cloud Firestore.
   * Persists both to `local_businesses/{busyCardNetId}` and `users/{userUid}/visiting_card`.
   */
  suspend fun saveBusinessProfile(
    card: DigitalCardEntity,
    userUid: String? = null
  ): Boolean {
    val db = getDb() ?: return false
    return try {
      val docId = card.busyCardNetId.ifBlank { card.cardId }
      val data = hashMapOf(
        "id" to card.cardId,
        "name" to card.businessName.ifBlank { card.name },
        "ownerOrContact" to card.name,
        "busyCardNetId" to card.busyCardNetId,
        "placeType" to if (card.professionCode in listOf("SHOP", "REST", "HOTE", "BIZ", "SALE", "RETL")) "LOCAL_BUSINESS" else "SERVICE_PROVIDER",
        "professionCode" to card.professionCode,
        "categoryTitle" to card.designation,
        "emoji" to if (card.professionCode in listOf("SHOP", "REST", "HOTE", "BIZ", "SALE", "RETL")) "🏢" else "🔧",
        "latitude" to card.latitude,
        "longitude" to card.longitude,
        "address" to card.address,
        "phone" to card.phone,
        "whatsapp" to card.whatsapp,
        "email" to card.email,
        "website" to card.website,
        "rating" to 4.9,
        "reviewsCount" to 18,
        "isOpenNow" to true,
        "operatingHours" to card.workingHours.ifBlank { "9:00 AM - 8:00 PM" },
        "services" to card.services.split(",").map { it.trim() }.filter { it.isNotBlank() },
        "about" to card.about,
        "templateStyle" to card.templateStyle,
        "accentColorHex" to card.accentColorHex,
        "ownerUid" to (userUid ?: ""),
        "updatedAt" to System.currentTimeMillis()
      )

      // 1. Write to public local_businesses collection for nearby discovery
      db.collection(COLLECTION_LOCAL_BUSINESSES)
        .document(docId)
        .set(data, SetOptions.merge())
        .await()

      // 2. Write to user's private card profile if authenticated
      if (!userUid.isNullOrBlank()) {
        db.collection(COLLECTION_USERS)
          .document(userUid)
          .collection(SUBCOLLECTION_CARD)
          .document("profile")
          .set(data, SetOptions.merge())
          .await()
      }

      Log.d(TAG, "Business profile ${card.busyCardNetId} successfully synchronized to Firestore")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save business profile to Firestore: ${e.message}", e)
      false
    }
  }

  /**
   * Fetches local businesses from Firestore within a given radius (500m, 1000m, 2000m, 5000m, 10000m).
   * Automatically seeds initial curated businesses if Firestore collection is empty.
   */
  suspend fun fetchBusinessesWithinRadius(
    userLat: Double,
    userLng: Double,
    radiusMeters: Int
  ): List<LocalMapPlaceItem> {
    val db = getDb()
    if (db == null) {
      // Fallback to local curated provider if Firestore is unavailable (e.g. offline)
      return LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
        .filter { it.distanceMeters <= radiusMeters }
    }

    return try {
      val snapshot = db.collection(COLLECTION_LOCAL_BUSINESSES)
        .get()
        .await()

      if (snapshot.isEmpty) {
        // Seed curated businesses into Firestore so live cloud documents exist
        seedInitialBusinessesToFirestore(db)
        return LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
          .filter { it.distanceMeters <= radiusMeters }
      }

      val items = mutableListOf<LocalMapPlaceItem>()
      for (doc in snapshot.documents) {
        val lat = doc.getDouble("latitude") ?: continue
        val lng = doc.getDouble("longitude") ?: continue
        val distance = calculateDistanceMeters(userLat, userLng, lat, lng)

        if (distance <= radiusMeters) {
          val placeTypeStr = doc.getString("placeType") ?: "LOCAL_BUSINESS"
          val placeType = if (placeTypeStr == "SERVICE_PROVIDER") PlaceType.SERVICE_PROVIDER else PlaceType.LOCAL_BUSINESS
          val servicesList = (doc.get("services") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

          items.add(
            LocalMapPlaceItem(
              id = doc.getString("id") ?: doc.id,
              name = doc.getString("name") ?: "Local Enterprise",
              ownerOrContact = doc.getString("ownerOrContact") ?: "",
              busyCardNetId = doc.getString("busyCardNetId") ?: doc.id,
              placeType = placeType,
              professionCode = doc.getString("professionCode") ?: "BIZ",
              categoryTitle = doc.getString("categoryTitle") ?: "Business",
              emoji = doc.getString("emoji") ?: if (placeType == PlaceType.LOCAL_BUSINESS) "🏢" else "🔧",
              latitude = lat,
              longitude = lng,
              distanceMeters = distance,
              address = doc.getString("address") ?: "Local Area",
              phone = doc.getString("phone") ?: "",
              whatsapp = doc.getString("whatsapp") ?: "",
              email = doc.getString("email") ?: "",
              website = doc.getString("website") ?: "",
              rating = (doc.getDouble("rating") ?: 4.8).toFloat(),
              reviewsCount = (doc.getLong("reviewsCount") ?: 12).toInt(),
              isOpenNow = doc.getBoolean("isOpenNow") ?: true,
              operatingHours = doc.getString("operatingHours") ?: "9:00 AM - 8:00 PM",
              servicesOrProducts = servicesList,
              isConnectedBridge = false,
              about = doc.getString("about") ?: ""
            )
          )
        }
      }

      // If Firestore had no places within radius, supplement with curated places
      if (items.isEmpty()) {
        LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
          .filter { it.distanceMeters <= radiusMeters }
      } else {
        items.sortedBy { it.distanceMeters }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error querying Firestore businesses: ${e.message}, using curated data")
      LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
        .filter { it.distanceMeters <= radiusMeters }
    }
  }

  /**
   * Real-time Flow of businesses within radius from Firestore.
   */
  fun listenToBusinessesWithinRadius(
    userLat: Double,
    userLng: Double,
    radiusMeters: Int
  ): Flow<List<LocalMapPlaceItem>> = callbackFlow {
    val db = getDb()
    if (db == null) {
      trySend(
        LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
          .filter { it.distanceMeters <= radiusMeters }
      )
      awaitClose {}
      return@callbackFlow
    }

    var registration: ListenerRegistration? = null
    try {
      registration = db.collection(COLLECTION_LOCAL_BUSINESSES)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(TAG, "Snapshot error listening to businesses: ${error.message}")
            trySend(
              LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
                .filter { it.distanceMeters <= radiusMeters }
            )
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            val items = mutableListOf<LocalMapPlaceItem>()
            for (doc in snapshot.documents) {
              val lat = doc.getDouble("latitude") ?: continue
              val lng = doc.getDouble("longitude") ?: continue
              val distance = calculateDistanceMeters(userLat, userLng, lat, lng)

              if (distance <= radiusMeters) {
                val placeTypeStr = doc.getString("placeType") ?: "LOCAL_BUSINESS"
                val placeType = if (placeTypeStr == "SERVICE_PROVIDER") PlaceType.SERVICE_PROVIDER else PlaceType.LOCAL_BUSINESS
                val servicesList = (doc.get("services") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                items.add(
                  LocalMapPlaceItem(
                    id = doc.getString("id") ?: doc.id,
                    name = doc.getString("name") ?: "Local Enterprise",
                    ownerOrContact = doc.getString("ownerOrContact") ?: "",
                    busyCardNetId = doc.getString("busyCardNetId") ?: doc.id,
                    placeType = placeType,
                    professionCode = doc.getString("professionCode") ?: "BIZ",
                    categoryTitle = doc.getString("categoryTitle") ?: "Business",
                    emoji = doc.getString("emoji") ?: if (placeType == PlaceType.LOCAL_BUSINESS) "🏢" else "🔧",
                    latitude = lat,
                    longitude = lng,
                    distanceMeters = distance,
                    address = doc.getString("address") ?: "Local Area",
                    phone = doc.getString("phone") ?: "",
                    whatsapp = doc.getString("whatsapp") ?: "",
                    email = doc.getString("email") ?: "",
                    website = doc.getString("website") ?: "",
                    rating = (doc.getDouble("rating") ?: 4.8).toFloat(),
                    reviewsCount = (doc.getLong("reviewsCount") ?: 12).toInt(),
                    isOpenNow = doc.getBoolean("isOpenNow") ?: true,
                    operatingHours = doc.getString("operatingHours") ?: "9:00 AM - 8:00 PM",
                    servicesOrProducts = servicesList,
                    isConnectedBridge = false,
                    about = doc.getString("about") ?: ""
                  )
                )
              }
            }
            trySend(items.sortedBy { it.distanceMeters })
          } else {
            trySend(
              LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
                .filter { it.distanceMeters <= radiusMeters }
            )
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error setting up listener: ${e.message}")
      trySend(
        LocalPlaceDataProvider.getCuratedPlaces(userLat, userLng)
          .filter { it.distanceMeters <= radiusMeters }
      )
    }

    awaitClose {
      registration?.remove()
    }
  }

  /**
   * Seeds curated businesses into Firestore so the database is populated with rich data.
   */
  private suspend fun seedInitialBusinessesToFirestore(db: FirebaseFirestore) {
    try {
      val places = LocalPlaceDataProvider.getCuratedPlaces()
      val batch = db.batch()
      for (place in places) {
        val docRef = db.collection(COLLECTION_LOCAL_BUSINESSES).document(place.busyCardNetId)
        val data = hashMapOf(
          "id" to place.id,
          "name" to place.name,
          "ownerOrContact" to place.ownerOrContact,
          "busyCardNetId" to place.busyCardNetId,
          "placeType" to place.placeType.name,
          "professionCode" to place.professionCode,
          "categoryTitle" to place.categoryTitle,
          "emoji" to place.emoji,
          "latitude" to place.latitude,
          "longitude" to place.longitude,
          "address" to place.address,
          "phone" to place.phone,
          "whatsapp" to place.whatsapp,
          "email" to place.email,
          "website" to place.website,
          "rating" to place.rating.toDouble(),
          "reviewsCount" to place.reviewsCount,
          "isOpenNow" to place.isOpenNow,
          "operatingHours" to place.operatingHours,
          "services" to place.servicesOrProducts,
          "about" to place.about,
          "updatedAt" to System.currentTimeMillis()
        )
        batch.set(docRef, data, SetOptions.merge())
      }
      batch.commit().await()
      Log.d(TAG, "Seeded ${places.size} businesses to Firestore")
    } catch (e: Exception) {
      Log.w(TAG, "Failed seeding businesses to Firestore: ${e.message}")
    }
  }
}
