package com.example

import com.example.ui.map.LocalMapPlaceItem
import com.example.ui.map.LocalPlaceDataProvider
import com.example.ui.map.PlaceType
import org.junit.Assert.*
import org.junit.Test

/**
 * Local unit tests verifying radius filtering across 500m, 1000m, 2000m, 5000m, 10000m
 * and business place data integrity.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testRadiusFilteringAcrossAllSupportedTiers() {
    val samplePlaces = listOf(
      LocalMapPlaceItem(
        id = "p1",
        name = "Near Cafe",
        ownerOrContact = "Alex",
        busyCardNetId = "BCN-1",
        placeType = PlaceType.LOCAL_BUSINESS,
        professionCode = "REST",
        categoryTitle = "Cafe",
        emoji = "☕",
        latitude = 19.0760,
        longitude = 72.8777,
        distanceMeters = 350,
        address = "Market Rd",
        phone = "9876543210"
      ),
      LocalMapPlaceItem(
        id = "p2",
        name = "Mid Electrician",
        ownerOrContact = "Bob",
        busyCardNetId = "BCN-2",
        placeType = PlaceType.SERVICE_PROVIDER,
        professionCode = "ELEC",
        categoryTitle = "Electrician",
        emoji = "⚡",
        latitude = 19.0780,
        longitude = 72.8790,
        distanceMeters = 850,
        address = "Station Rd",
        phone = "9876543211"
      ),
      LocalMapPlaceItem(
        id = "p3",
        name = "Far Hardware",
        ownerOrContact = "Charlie",
        busyCardNetId = "BCN-3",
        placeType = PlaceType.LOCAL_BUSINESS,
        professionCode = "SHOP",
        categoryTitle = "Hardware",
        emoji = "🏪",
        latitude = 19.0820,
        longitude = 72.8820,
        distanceMeters = 1600,
        address = "Highway Junction",
        phone = "9876543212"
      ),
      LocalMapPlaceItem(
        id = "p4",
        name = "Metro Auto Garage",
        ownerOrContact = "Dave",
        busyCardNetId = "BCN-4",
        placeType = PlaceType.SERVICE_PROVIDER,
        professionCode = "MECH",
        categoryTitle = "Mechanic",
        emoji = "🚗",
        latitude = 19.0950,
        longitude = 72.8900,
        distanceMeters = 4200,
        address = "Industrial Zone",
        phone = "9876543213"
      ),
      LocalMapPlaceItem(
        id = "p5",
        name = "Outskirts Supplier Hub",
        ownerOrContact = "Elena",
        busyCardNetId = "BCN-5",
        placeType = PlaceType.LOCAL_BUSINESS,
        professionCode = "BIZ",
        categoryTitle = "Wholesale Supplier",
        emoji = "🏢",
        latitude = 19.1200,
        longitude = 72.9100,
        distanceMeters = 8500,
        address = "Outer Ring Rd",
        phone = "9876543214"
      )
    )

    // 500m radius
    val within500 = LocalPlaceDataProvider.filterPlacesWithinRadius(samplePlaces, 500)
    assertEquals(1, within500.size)
    assertEquals("Near Cafe", within500[0].name)

    // 1000m radius
    val within1000 = LocalPlaceDataProvider.filterPlacesWithinRadius(samplePlaces, 1000)
    assertEquals(2, within1000.size)

    // 2000m radius
    val within2000 = LocalPlaceDataProvider.filterPlacesWithinRadius(samplePlaces, 2000)
    assertEquals(3, within2000.size)

    // 5000m radius
    val within5000 = LocalPlaceDataProvider.filterPlacesWithinRadius(samplePlaces, 5000)
    assertEquals(4, within5000.size)

    // 10000m radius
    val within10000 = LocalPlaceDataProvider.filterPlacesWithinRadius(samplePlaces, 10000)
    assertEquals(5, within10000.size)
  }
}
