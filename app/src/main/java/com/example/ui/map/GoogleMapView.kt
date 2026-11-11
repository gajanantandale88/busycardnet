package com.example.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.ui.components.BcnCardHyperlink
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

enum class MapLayerMode(val label: String, val googleMapType: Int) {
  NORMAL("🗺️ Default", GoogleMap.MAP_TYPE_NORMAL),
  SATELLITE("🛰️ Satellite", GoogleMap.MAP_TYPE_SATELLITE),
  TERRAIN("🏔️ Terrain", GoogleMap.MAP_TYPE_TERRAIN),
  RADAR_GRID("🎯 BCN Radar", GoogleMap.MAP_TYPE_NORMAL)
}

/**
 * Custom JSON Map Styling tailored for BusyCardNet branding:
 * Deep Slate / Midnight Navy canvas (#0F172A, #1E293B), vibrant cyan highways (#38BDF8),
 * deep indigo waters (#082F49), muted POIs, and crisp silver typography (#CBD5E1).
 */
val BUSY_CARD_NET_MAP_STYLE_JSON = """
[
  {
    "elementType": "geometry",
    "stylers": [{ "color": "#0f172a" }]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#94a3b8" }]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#020617" }]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [{ "color": "#334155" }]
  },
  {
    "featureType": "administrative.country",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#cbd5e1" }]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#e2e8f0" }]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#64748b" }]
  },
  {
    "featureType": "poi",
    "elementType": "geometry",
    "stylers": [{ "color": "#1e293b" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [{ "color": "#064e3b" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#34d399" }]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [{ "color": "#1e293b" }]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#0f172a" }]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#94a3b8" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [{ "color": "#0369a1" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#0c4a6e" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#38bdf8" }]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [{ "color": "#1e293b" }]
  },
  {
    "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#38bdf8" }]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [{ "color": "#082f49" }]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#38bdf8" }]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#020617" }]
  }
]
""".trimIndent()

/**
 * BusyCardNetMapPinHelper renders distinct, high-DPI branded vector pins
 * for Google Maps SDK with custom gradients, badge icons, and selected highlights.
 */
object BusyCardNetMapPinHelper {
  private val pinCache = mutableMapOf<String, BitmapDescriptor>()
  private var userBeaconDescriptor: BitmapDescriptor? = null

  fun getPinDescriptor(
    context: Context,
    place: LocalMapPlaceItem,
    isSelected: Boolean
  ): BitmapDescriptor {
    val cacheKey = "${place.placeType.name}_${place.emoji}_${isSelected}"
    return pinCache.getOrPut(cacheKey) {
      createDistinctPinBitmap(context, place, isSelected)
    }
  }

  fun getUserLocationDescriptor(context: Context): BitmapDescriptor {
    return userBeaconDescriptor ?: synchronized(this) {
      userBeaconDescriptor ?: createUserLocationBeacon(context).also { userBeaconDescriptor = it }
    }
  }

  private fun createDistinctPinBitmap(
    context: Context,
    place: LocalMapPlaceItem,
    isSelected: Boolean
  ): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val width = ((if (isSelected) 56 else 44) * density).toInt()
    val height = ((if (isSelected) 68 else 54) * density).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = width / 2f
    val headRadius = (if (isSelected) 21f else 17f) * density
    val headCenterY = headRadius + (3f * density)
    val tipY = height - (4f * density)

    // 1. Shadow beneath pin tip
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.argb(80, 0, 0, 0)
      style = Paint.Style.FILL
    }
    canvas.drawOval(
      cx - (9f * density),
      tipY - (1.5f * density),
      cx + (9f * density),
      tipY + (3.5f * density),
      shadowPaint
    )

    // 2. Smooth Teardrop Pin Path
    val pinPath = Path().apply {
      moveTo(cx, tipY)
      quadTo(
        cx - (headRadius * 1.15f),
        headCenterY + (headRadius * 0.75f),
        cx - headRadius,
        headCenterY
      )
      arcTo(
        RectF(cx - headRadius, headCenterY - headRadius, cx + headRadius, headCenterY + headRadius),
        180f,
        180f,
        false
      )
      quadTo(
        cx + (headRadius * 1.15f),
        headCenterY + (headRadius * 0.75f),
        cx,
        tipY
      )
      close()
    }

    // 3. Branded Gradient Fill (Cyan for Local Businesses, Emerald/Amber for Service Providers)
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      isDither = true
      style = Paint.Style.FILL
      shader = if (place.isBusiness) {
        LinearGradient(
          cx, headCenterY - headRadius, cx, tipY,
          android.graphics.Color.parseColor("#38BDF8"), // Cyan
          android.graphics.Color.parseColor("#0284C7"), // Deep Cobalt
          Shader.TileMode.CLAMP
        )
      } else {
        LinearGradient(
          cx, headCenterY - headRadius, cx, tipY,
          android.graphics.Color.parseColor("#10B981"), // Emerald
          android.graphics.Color.parseColor("#047857"), // Forest Green
          Shader.TileMode.CLAMP
        )
      }
    }
    canvas.drawPath(pinPath, fillPaint)

    // 4. White or Glowing Border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = if (isSelected) 3.5f * density else 2.2f * density
      color = if (isSelected) android.graphics.Color.parseColor("#E0F2FE") else android.graphics.Color.WHITE
    }
    canvas.drawPath(pinPath, borderPaint)

    // 5. Inner Contrast Badge Circle (Slate Midnight #0F172A)
    val badgeRadius = headRadius * 0.66f
    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.parseColor("#0F172A")
      style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, headCenterY, badgeRadius, badgePaint)

    // 6. Category Emoji or Icon in Center
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textSize = badgeRadius * 1.15f
      textAlign = Paint.Align.CENTER
    }
    val fontMetrics = textPaint.fontMetrics
    val textBaselineY = headCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
    val glyph = place.emoji.ifBlank { if (place.isBusiness) "🏢" else "🔧" }
    canvas.drawText(glyph, cx, textBaselineY, textPaint)

    // 7. Selected Glow Halo
    if (isSelected) {
      val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = android.graphics.Color.parseColor("#38BDF8")
      }
      canvas.drawCircle(cx, headCenterY, headRadius + (3.5f * density), glowPaint)
    }

    return BitmapDescriptorFactory.fromBitmap(bitmap)
  }

  private fun createUserLocationBeacon(context: Context): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val size = (38 * density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = size / 2f
    val cy = size / 2f

    // Outer translucent pulse ring
    val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.argb(70, 56, 189, 248)
      style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 18f * density, pulsePaint)

    // Middle white ring
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.WHITE
      style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 11f * density, borderPaint)

    // Inner vibrant blue dot
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.parseColor("#0284C7")
      style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 8.5f * density, dotPaint)

    // White center pinpoint
    val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.WHITE
      style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 2.5f * density, centerPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
  }
}

/**
 * Native Google Maps integration using MapView from Play Services Maps.
 * Displays local businesses and service providers filtered across
 * 500m, 1000m, 2000m, 5000m, and 10000m radii with custom branded styling.
 */
@Composable
fun GoogleMapView(
  userLatitude: Double,
  userLongitude: Double,
  radiusMeters: Double = 500.0,
  places: List<LocalMapPlaceItem>,
  selectedPlace: LocalMapPlaceItem?,
  onPlaceSelected: (LocalMapPlaceItem?) -> Unit,
  layerMode: MapLayerMode = MapLayerMode.NORMAL,
  zoomScale: Float = 1f,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle

  // Determine if MapView can be safely instantiated (graceful fallback in Robolectric unit tests)
  var isNativeMapSupported by remember {
    mutableStateOf(
      try {
        MapView(context)
        true
      } catch (t: Throwable) {
        false
      }
    )
  }

  if (isNativeMapSupported && layerMode != MapLayerMode.RADAR_GRID) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .testTag("google_map_view_container")
    ) {
      val mapView = rememberMapViewWithLifecycle(context, lifecycle)
      var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
      var currentCircle by remember { mutableStateOf<Circle?>(null) }
      var userLocationMarker by remember { mutableStateOf<Marker?>(null) }
      val markersList = remember { mutableListOf<Marker>() }

      AndroidView(
        factory = { ctx ->
          mapView.apply {
            getMapAsync { gMap ->
              googleMapInstance = gMap
              setupGoogleMap(ctx, gMap, userLatitude, userLongitude, radiusMeters, places, selectedPlace, onPlaceSelected)
            }
          }
        },
        update = {
          googleMapInstance?.let { gMap ->
            try {
              gMap.mapType = layerMode.googleMapType
              if (layerMode == MapLayerMode.NORMAL) {
                gMap.setMapStyle(MapStyleOptions(BUSY_CARD_NET_MAP_STYLE_JSON))
              } else {
                gMap.setMapStyle(null)
              }
            } catch (_: Exception) {}
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      // Update markers, circle, style, and camera whenever places, location, radius, or selection changes
      LaunchedEffect(googleMapInstance, places, userLatitude, userLongitude, radiusMeters, selectedPlace, layerMode) {
        val gMap = googleMapInstance ?: return@LaunchedEffect
        val userLatLng = LatLng(userLatitude, userLongitude)

        // Apply BusyCardNet Custom Map Styling
        try {
          if (layerMode == MapLayerMode.NORMAL) {
            gMap.setMapStyle(MapStyleOptions(BUSY_CARD_NET_MAP_STYLE_JSON))
          } else {
            gMap.setMapStyle(null)
          }
          gMap.mapType = layerMode.googleMapType
        } catch (_: Exception) {}

        // Redraw Dynamic Radius Circle with BusyCardNet Cyan Accent
        try {
          currentCircle?.remove()
          val circle = gMap.addCircle(
            CircleOptions()
              .center(userLatLng)
              .radius(radiusMeters)
              .strokeWidth(5f)
              .strokeColor(android.graphics.Color.parseColor("#38BDF8")) // Electric Cyan
              .fillColor(android.graphics.Color.parseColor("#260284C7")) // 15% opacity sky blue fill
          )
          currentCircle = circle
        } catch (_: Exception) {}

        // User Location GPS Beacon
        try {
          userLocationMarker?.remove()
          userLocationMarker = gMap.addMarker(
            MarkerOptions()
              .position(userLatLng)
              .title("Your GPS Location")
              .snippet("Center of ${if (radiusMeters >= 1000) "${(radiusMeters / 1000).toInt()}km" else "${radiusMeters.toInt()}m"} Radius")
              .anchor(0.5f, 0.5f)
              .icon(BusyCardNetMapPinHelper.getUserLocationDescriptor(context))
          )
        } catch (_: Exception) {}

        // Clear and reload distinct branded business markers
        markersList.forEach { it.remove() }
        markersList.clear()

        places.forEach { place ->
          val isSelected = selectedPlace?.id == place.id
          val pinDescriptor = BusyCardNetMapPinHelper.getPinDescriptor(context, place, isSelected)

          val markerOptions = MarkerOptions()
            .position(LatLng(place.latitude, place.longitude))
            .title(place.name)
            .snippet("${if (place.isBusiness) "🏢 Business" else "🔧 Service"} • ${place.distanceMeters}m • ★ ${place.rating}")
            .anchor(0.5f, 1.0f)
            .icon(pinDescriptor)

          val marker = gMap.addMarker(markerOptions)
          if (marker != null) {
            marker.tag = place
            markersList.add(marker)

            if (isSelected) {
              marker.showInfoWindow()
            }
          }
        }

        // Camera Pan & Zoom Adjustment for Selected Radius
        try {
          if (selectedPlace != null) {
            gMap.animateCamera(
              CameraUpdateFactory.newLatLng(LatLng(selectedPlace.latitude, selectedPlace.longitude))
            )
          } else {
            val targetZoom = when {
              radiusMeters <= 500.0 -> 16.2f
              radiusMeters <= 1000.0 -> 15.2f
              radiusMeters <= 2000.0 -> 14.2f
              radiusMeters <= 5000.0 -> 12.8f
              else -> 11.5f
            }
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, targetZoom))
          }
        } catch (_: Exception) {}
      }
    }
  } else {
    // High-Contrast Radar / Fallback Vector Grid (used in offline mode, tactical mode, or JVM unit tests)
    FallbackRadarMapView(
      userLatitude = userLatitude,
      userLongitude = userLongitude,
      radiusMeters = radiusMeters,
      places = places,
      selectedPlace = selectedPlace,
      onPlaceSelected = onPlaceSelected,
      zoomScale = zoomScale,
      layerMode = layerMode,
      modifier = modifier
    )
  }
}

private fun setupGoogleMap(
  context: Context,
  googleMap: GoogleMap,
  userLatitude: Double,
  userLongitude: Double,
  radiusMeters: Double,
  places: List<LocalMapPlaceItem>,
  selectedPlace: LocalMapPlaceItem?,
  onPlaceSelected: (LocalMapPlaceItem?) -> Unit
) {
  try {
    googleMap.uiSettings.isZoomControlsEnabled = false
    googleMap.uiSettings.isCompassEnabled = true
    googleMap.uiSettings.isMyLocationButtonEnabled = false
    googleMap.uiSettings.isMapToolbarEnabled = false

    // Apply custom BusyCardNet Map Style
    try {
      googleMap.setMapStyle(MapStyleOptions(BUSY_CARD_NET_MAP_STYLE_JSON))
    } catch (_: Exception) {}

    val userLatLng = LatLng(userLatitude, userLongitude)
    val initialZoom = when {
      radiusMeters <= 500.0 -> 16.5f
      radiusMeters <= 1000.0 -> 15.2f
      radiusMeters <= 2000.0 -> 14.2f
      radiusMeters <= 5000.0 -> 12.8f
      else -> 11.5f
    }
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, initialZoom))

    // Set custom styled Info Window for business pins
    googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
      override fun getInfoWindow(marker: Marker): android.view.View? = null
      override fun getInfoContents(marker: Marker): android.view.View? {
        val place = marker.tag as? LocalMapPlaceItem ?: return null
        val density = context.resources.displayMetrics.density
        val padH = (14 * density).toInt()
        val padV = (10 * density).toInt()

        val layout = android.widget.LinearLayout(context).apply {
          orientation = android.widget.LinearLayout.VERTICAL
          setPadding(padH, padV, padH, padV)
          val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#0F172A"))
            cornerRadius = 12 * density
            setStroke((1.5f * density).toInt(), android.graphics.Color.parseColor("#38BDF8"))
          }
          background = bgDrawable
        }

        val titleView = android.widget.TextView(context).apply {
          text = "${place.emoji} ${place.name}"
          setTextColor(android.graphics.Color.WHITE)
          textSize = 13f
          setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subView = android.widget.TextView(context).apply {
          text = "${if (place.isBusiness) "🏢 Business" else "🔧 Service"} • ${place.distanceMeters}m away • ★ ${place.rating}"
          setTextColor(android.graphics.Color.parseColor("#38BDF8"))
          textSize = 11f
          setPadding(0, (2 * density).toInt(), 0, (2 * density).toInt())
        }

        val bcnView = android.widget.TextView(context).apply {
          text = "ID: ${place.busyCardNetId}"
          setTextColor(android.graphics.Color.parseColor("#94A3B8"))
          textSize = 10f
        }

        val tapView = android.widget.TextView(context).apply {
          text = "Tap to open profile & services"
          setTextColor(android.graphics.Color.parseColor("#34D399"))
          textSize = 10f
          setTypeface(null, android.graphics.Typeface.BOLD)
          setPadding(0, (3 * density).toInt(), 0, 0)
        }

        layout.addView(titleView)
        layout.addView(subView)
        layout.addView(bcnView)
        layout.addView(tapView)
        return layout
      }
    })

    // Set marker click listener to select place
    googleMap.setOnMarkerClickListener { marker ->
      val place = marker.tag as? LocalMapPlaceItem
      if (place != null) {
        onPlaceSelected(place)
        marker.showInfoWindow()
      }
      true
    }

    // Set map click listener to deselect
    googleMap.setOnMapClickListener {
      onPlaceSelected(null)
    }
  } catch (_: Exception) {}
}

@Composable
private fun rememberMapViewWithLifecycle(context: Context, lifecycle: Lifecycle): MapView {
  val mapView = remember {
    try {
      com.google.android.gms.maps.MapsInitializer.initialize(context)
    } catch (_: Exception) {}
    MapView(context).apply {
      onCreate(Bundle())
    }
  }

  DisposableEffect(lifecycle, mapView) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START -> mapView.onStart()
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_STOP -> mapView.onStop()
        Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
        else -> {}
      }
    }
    lifecycle.addObserver(observer)

    // Synchronize if already started or resumed in the Compose tree
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
      try { mapView.onStart() } catch (_: Exception) {}
    }
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
      try { mapView.onResume() } catch (_: Exception) {}
    }

    onDispose {
      lifecycle.removeObserver(observer)
      try {
        mapView.onPause()
        mapView.onStop()
        mapView.onDestroy()
      } catch (_: Exception) {}
    }
  }

  return mapView
}

/**
 * Fallback / Tactical Radar View with dynamic concentric radius circles (500m, 1000m, 2000m, 5000m, 10000m),
 * user GPS beacon, and distinct branded business pins.
 */
@Composable
private fun FallbackRadarMapView(
  userLatitude: Double,
  userLongitude: Double,
  radiusMeters: Double,
  places: List<LocalMapPlaceItem>,
  selectedPlace: LocalMapPlaceItem?,
  onPlaceSelected: (LocalMapPlaceItem?) -> Unit,
  zoomScale: Float,
  layerMode: MapLayerMode,
  modifier: Modifier = Modifier
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A))
      .testTag("fallback_radar_map_view")
  ) {
    val canvasWidth = constraints.maxWidth.toFloat()
    val canvasHeight = constraints.maxHeight.toFloat()
    val centerOffset = Offset(canvasWidth / 2f, canvasHeight / 2f)

    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(places, zoomScale, radiusMeters) {
          detectTapGestures { tapOffset ->
            val tapped = places.find { place ->
              // Scale coordinates according to selected radius
              val scaleFactor = (500.0 / radiusMeters).toFloat().coerceIn(0.2f, 1.2f)
              val pinPxX = centerOffset.x + (place.normX * (canvasWidth / 2.2f) * zoomScale * scaleFactor)
              val pinPxY = centerOffset.y + (place.normY * (canvasHeight / 2.2f) * zoomScale * scaleFactor)
              val dx = tapOffset.x - pinPxX
              val dy = tapOffset.y - pinPxY
              (dx * dx + dy * dy) <= (32.dp.toPx() * 32.dp.toPx())
            }
            onPlaceSelected(tapped)
          }
        }
    ) {
      val maxRadiusPx = (minOf(size.width, size.height) / 2.2f) * zoomScale

      // Background grid lines (BusyCardNet Slate theme)
      val gridColor = Color(0xFF1E293B)
      for (i in -4..4) {
        val y = centerOffset.y + (i * 70f * zoomScale)
        drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.2f)
        val x = centerOffset.x + (i * 70f * zoomScale)
        drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.2f)
      }

      // Dynamic Radius Boundary Rings
      val primaryRingPx = maxRadiusPx * 0.75f
      val innerRingPx = maxRadiusPx * 0.38f
      val ringColor = Color(0xFF38BDF8) // Electric Cyan

      // Outer Radius translucent wash
      drawCircle(
        color = ringColor.copy(alpha = 0.08f),
        radius = primaryRingPx,
        center = centerOffset
      )
      // Outer Radius solid boundary
      drawCircle(
        color = ringColor,
        radius = primaryRingPx,
        center = centerOffset,
        style = Stroke(width = 3.5f)
      )
      // Inner distance reference ring (dashed)
      drawCircle(
        color = ringColor.copy(alpha = 0.35f),
        radius = innerRingPx,
        center = centerOffset,
        style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
      )

      // Center User Beacon
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFF0284C7).copy(alpha = 0.35f), Color.Transparent),
          center = centerOffset,
          radius = 36f
        ),
        radius = 36f,
        center = centerOffset
      )
      drawCircle(color = Color.White, radius = 9f, center = centerOffset)
      drawCircle(color = Color(0xFF0284C7), radius = 6.5f, center = centerOffset)

      // Render Distinct Business & Service Pins
      val scaleFactor = (500.0 / radiusMeters).toFloat().coerceIn(0.2f, 1.2f)
      places.forEach { place ->
        val pinPxX = centerOffset.x + (place.normX * (canvasWidth / 2.2f) * zoomScale * scaleFactor)
        val pinPxY = centerOffset.y + (place.normY * (canvasHeight / 2.2f) * zoomScale * scaleFactor)
        val isSelected = selectedPlace?.id == place.id

        val pinColor = if (place.isBusiness) Color(0xFF38BDF8) else Color(0xFF10B981)
        val pinRadius = if (isSelected) 18.dp.toPx() else 14.dp.toPx()

        // Pin shadow
        drawCircle(
          color = Color.Black.copy(alpha = 0.4f),
          radius = pinRadius * 0.9f,
          center = Offset(pinPxX, pinPxY + 4.dp.toPx())
        )
        // Outer pin body
        drawCircle(
          color = pinColor,
          radius = pinRadius,
          center = Offset(pinPxX, pinPxY)
        )
        // High-contrast white border
        drawCircle(
          color = if (isSelected) Color(0xFFE0F2FE) else Color.White,
          radius = pinRadius,
          center = Offset(pinPxX, pinPxY),
          style = Stroke(width = if (isSelected) 3.dp.toPx() else 2.dp.toPx())
        )
        // Inner slate circle
        drawCircle(
          color = Color(0xFF0F172A),
          radius = pinRadius * 0.68f,
          center = Offset(pinPxX, pinPxY)
        )

        // Selected Pulsing Ring
        if (isSelected) {
          drawCircle(
            color = Color(0xFF38BDF8),
            radius = pinRadius + 5.dp.toPx(),
            center = Offset(pinPxX, pinPxY),
            style = Stroke(width = 2.dp.toPx())
          )
        }

        // Draw Emoji Glyph inside canvas
        val glyph = place.emoji.ifBlank { if (place.isBusiness) "🏢" else "🔧" }
        drawContext.canvas.nativeCanvas.drawText(
          glyph,
          pinPxX,
          pinPxY + 4.dp.toPx(),
          android.graphics.Paint().apply {
            textSize = pinRadius * 0.9f
            textAlign = android.graphics.Paint.Align.CENTER
          }
        )
      }
    }

    // Selected Place Quick Floating Preview Card
    if (selectedPlace != null) {
      val place = selectedPlace
      Card(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(14.dp)
          .testTag("selected_place_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (place.isBusiness) Color(0xFF0284C7) else Color(0xFF10B981)
              ) {
                Text(
                  text = if (place.isBusiness) "🏢 BUSINESS" else "🔧 SERVICE",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${place.distanceMeters}m away",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF38BDF8)
              )
            }
            IconButton(onClick = { onPlaceSelected(null) }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = place.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "${place.ownerOrContact} • ${place.categoryTitle}",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
          )
          BcnCardHyperlink(
            bcnId = place.busyCardNetId,
            color = Color(0xFF38BDF8),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF0284C7),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF1E293B),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Directions, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Navigate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
              }
            }
          }
        }
      }
    }
  }
}
