package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * BcnLocationManager handles Google Play Services Location discovery
 * for finding 100m, 500m, and local registered BCN cardholders.
 */
class BcnLocationManager private constructor(private val context: Context) {

  private val fusedLocationClient: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(context.applicationContext)

  // Default coordinate (Kurla West, Mumbai: 19.0760 N, 72.8777 E)
  private val _currentLocation = MutableStateFlow(
    Location("default_provider").apply {
      latitude = 19.0760
      longitude = 72.8777
    }
  )
  val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

  private val _isGpsActive = MutableStateFlow(false)
  val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

  init {
    requestDeviceLocation()
  }

  /**
   * Request high-accuracy GPS coordinates using Google Play Services Location
   */
  @SuppressLint("MissingPermission")
  fun requestDeviceLocation() {
    try {
      val cancellationTokenSource = CancellationTokenSource()
      fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
      ).addOnSuccessListener { location: Location? ->
        if (location != null) {
          _currentLocation.value = location
          _isGpsActive.value = true
        } else {
          // Fall back to last known location
          fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
            if (lastLoc != null) {
              _currentLocation.value = lastLoc
              _isGpsActive.value = true
            }
          }
        }
      }.addOnFailureListener {
        // Fallback default remains active
        _isGpsActive.value = false
      }
    } catch (e: SecurityException) {
      // Permission not yet granted, fallback is retained
      _isGpsActive.value = false
    } catch (e: Exception) {
      _isGpsActive.value = false
    }
  }

  /**
   * Calculate distance in meters between user location and target coordinates
   */
  fun calculateDistanceMeters(targetLat: Double, targetLng: Double): Int {
    val current = _currentLocation.value
    val results = FloatArray(1)
    Location.distanceBetween(
      current.latitude,
      current.longitude,
      targetLat,
      targetLng,
      results
    )
    return results[0].toInt()
  }

  /**
   * Launch native Google Maps application or browser with turn-by-turn navigation
   */
  fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String) {
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    mapIntent.setPackage("com.google.android.apps.maps")
    try {
      context.startActivity(mapIntent)
    } catch (e: Exception) {
      // Fallback to web Google Maps
      val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
      val webIntent = Intent(Intent.ACTION_VIEW, webUri)
      try {
        context.startActivity(webIntent)
      } catch (ex: Exception) {
        Toast.makeText(context, "Could not launch Google Maps", Toast.LENGTH_SHORT).show()
      }
    }
  }

  /**
   * Launch Google Maps Turn-by-Turn Navigation directly
   */
  fun navigateWithGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    try {
      context.startActivity(mapIntent)
    } catch (e: Exception) {
      val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
      try {
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
      } catch (ex: Exception) {
        Toast.makeText(context, "Could not start Google Maps navigation", Toast.LENGTH_SHORT).show()
      }
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: BcnLocationManager? = null

    fun getInstance(context: Context): BcnLocationManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: BcnLocationManager(context).also { INSTANCE = it }
      }
    }
  }
}
