package com.example.data.firestore

import android.util.Log
import com.example.data.local.ServiceRequestEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Manages Service Requests stored in Firebase Firestore collection `service_requests`.
 * Provides cloud synchronization, realtime updates, editing, and deletion of requests.
 */
object FirestoreServiceRequestManager {
  private const val TAG = "FirestoreSRManager"
  private const val COLLECTION_SERVICE_REQUESTS = "service_requests"

  private fun getDb(): FirebaseFirestore? {
    return try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "Firestore instance unavailable (running offline or in test): ${e.message}")
      null
    }
  }

  /**
   * Saves or synchronizes a ServiceRequestEntity to Firestore, attaching notified nearby businesses.
   */
  suspend fun saveServiceRequest(request: ServiceRequestEntity): Boolean {
    val db = getDb() ?: return false
    return try {
      val nearbyBusinesses = try {
        com.example.ui.map.LocalPlaceDataProvider.getCuratedPlaces()
          .filter { it.distanceMeters <= request.radiusMeters }
          .map { place ->
            hashMapOf<String, Any>(
              "id" to place.id,
              "name" to place.name,
              "busyCardNetId" to place.busyCardNetId,
              "categoryTitle" to place.categoryTitle,
              "distanceMeters" to place.distanceMeters,
              "phone" to place.phone,
              "whatsapp" to place.whatsapp,
              "notifiedAt" to System.currentTimeMillis()
            )
          }
      } catch (_: Exception) {
        emptyList()
      }

      val data = hashMapOf(
        "requestId" to request.requestId,
        "requesterUserId" to request.requesterUserId,
        "requesterName" to request.requesterName,
        "requesterLocality" to request.requesterLocality,
        "professionCode" to request.professionCode,
        "title" to request.title,
        "category" to request.category,
        "categoryCode" to request.categoryCode,
        "problemDescription" to request.problemDescription,
        "budget" to request.budget,
        "urgency" to request.urgency,
        "serviceMode" to request.serviceMode,
        "tags" to request.tags,
        "photoAttached" to request.photoAttached,
        "radiusMeters" to request.radiusMeters,
        "status" to request.status,
        "distanceMeters" to request.distanceMeters,
        "createdAt" to request.createdAt,
        "expiryMinutes" to request.expiryMinutes,
        "respondingProfessionalId" to (request.respondingProfessionalId ?: ""),
        "respondingProfessionalName" to (request.respondingProfessionalName ?: ""),
        "respondingProfessionalRating" to request.respondingProfessionalRating.toDouble(),
        "notifiedBusinesses" to nearbyBusinesses,
        "notifiedBusinessesCount" to nearbyBusinesses.size,
        "broadcastRadiusMeters" to request.radiusMeters,
        "firestoreSynced" to true,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_SERVICE_REQUESTS)
        .document(request.requestId)
        .set(data, SetOptions.merge())
        .await()
      Log.d(TAG, "Service request ${request.requestId} successfully stored in Firestore with ${nearbyBusinesses.size} notified businesses")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error saving service request to Firestore: ${e.message}", e)
      false
    }
  }

  /**
   * Updates an existing service request in Firestore.
   */
  suspend fun updateServiceRequest(
    requestId: String,
    title: String,
    problemDescription: String,
    budget: String,
    urgency: String,
    radiusMeters: Int,
    status: String? = null
  ): Boolean {
    val db = getDb() ?: return false
    return try {
      val updates = mutableMapOf<String, Any>(
        "title" to title,
        "problemDescription" to problemDescription,
        "budget" to budget,
        "urgency" to urgency,
        "radiusMeters" to radiusMeters,
        "updatedAt" to System.currentTimeMillis()
      )
      if (status != null) {
        updates["status"] = status
      }
      db.collection(COLLECTION_SERVICE_REQUESTS)
        .document(requestId)
        .update(updates)
        .await()
      Log.d(TAG, "Service request $requestId successfully updated in Firestore")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error updating service request in Firestore: ${e.message}", e)
      false
    }
  }

  /**
   * Deletes a service request from Firestore.
   */
  suspend fun deleteServiceRequest(requestId: String): Boolean {
    val db = getDb() ?: return false
    return try {
      db.collection(COLLECTION_SERVICE_REQUESTS)
        .document(requestId)
        .delete()
        .await()
      Log.d(TAG, "Service request $requestId successfully deleted from Firestore")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error deleting service request from Firestore: ${e.message}", e)
      false
    }
  }

  /**
   * Realtime listener for service requests of a particular user or locality.
   */
  fun listenToMyRequests(
    userId: String,
    onRequestsUpdated: (List<ServiceRequestEntity>) -> Unit
  ): ListenerRegistration? {
    val db = getDb() ?: return null
    return try {
      db.collection(COLLECTION_SERVICE_REQUESTS)
        .whereEqualTo("requesterUserId", userId)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Listen failed for user requests: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null) {
            val list = snapshot.documents.mapNotNull { doc ->
              try {
                ServiceRequestEntity(
                  requestId = doc.getString("requestId") ?: doc.id,
                  requesterUserId = doc.getString("requesterUserId") ?: userId,
                  requesterName = doc.getString("requesterName") ?: "Me",
                  requesterLocality = doc.getString("requesterLocality") ?: "Mumbai",
                  professionCode = doc.getString("professionCode") ?: "PLUM",
                  problemDescription = doc.getString("problemDescription") ?: "",
                  photoAttached = doc.getBoolean("photoAttached") ?: false,
                  radiusMeters = (doc.getLong("radiusMeters") ?: 500L).toInt(),
                  status = doc.getString("status") ?: "SENT",
                  respondingProfessionalId = doc.getString("respondingProfessionalId"),
                  respondingProfessionalName = doc.getString("respondingProfessionalName"),
                  respondingProfessionalRating = (doc.getDouble("respondingProfessionalRating") ?: 4.8).toFloat(),
                  distanceMeters = (doc.getLong("distanceMeters") ?: 200L).toInt(),
                  createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                  expiryMinutes = (doc.getLong("expiryMinutes") ?: 30L).toInt(),
                  title = doc.getString("title") ?: "",
                  category = doc.getString("category") ?: "Service",
                  categoryCode = doc.getString("categoryCode") ?: "PLUM",
                  budget = doc.getString("budget") ?: "Negotiable",
                  urgency = doc.getString("urgency") ?: "STANDARD",
                  tags = doc.getString("tags") ?: "",
                  serviceMode = doc.getString("serviceMode") ?: "ON_SITE"
                )
              } catch (e: Exception) {
                null
              }
            }
            onRequestsUpdated(list)
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error attaching Firestore listener: ${e.message}", e)
      null
    }
  }
}
