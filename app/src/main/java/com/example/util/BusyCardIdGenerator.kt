package com.example.util

import java.util.UUID
import kotlin.random.Random

/**
 * BusyCardNet ID Specification:
 * Format: COUNTRY-STATE-CITY-YEAR-RANDOM-PROFESSION-RANDOM
 * Example: IN-MH-MU-26-581ELEC347
 *
 * Guaranteed to appear on every BusyCardNet digital visiting card at the BOTTOM-RIGHT CORNER.
 * Never exposes the user's private mobile number or internal UUID.
 */
object BusyCardIdGenerator {

  data class ParsedBusyCardId(
    val country: String,
    val state: String,
    val city: String,
    val year: String,
    val firstRandom: String,
    val professionCode: String,
    val secondRandom: String,
    val rawId: String
  )

  fun generateId(
    countryCode: String = "IN",
    stateCode: String = "MH",
    cityCode: String = "MU",
    yearSuffix: String = "26",
    professionCode: String = "BIZ"
  ): String {
    val cleanProfession = professionCode.uppercase().take(4).padEnd(4, 'X')
    val rand1 = Random.nextInt(100, 999).toString()
    val rand2 = Random.nextInt(100, 999).toString()
    return "${countryCode.uppercase()}-${stateCode.uppercase()}-${cityCode.uppercase()}-$yearSuffix-$rand1$cleanProfession$rand2"
  }

  fun generateInternalUuid(): String {
    return UUID.randomUUID().toString()
  }

  fun parse(busyCardNetId: String): ParsedBusyCardId? {
    val parts = busyCardNetId.split("-")
    if (parts.size != 5) return null

    val country = parts[0]
    val state = parts[1]
    val city = parts[2]
    val year = parts[3]
    val composite = parts[4]

    if (composite.length < 7) return null
    val firstRandom = composite.take(3)
    val remainder = composite.drop(3)
    if (remainder.length < 4) return null
    val secondRandom = remainder.takeLast(3)
    val professionCode = remainder.dropLast(3)

    return ParsedBusyCardId(
      country = country,
      state = state,
      city = city,
      year = year,
      firstRandom = firstRandom,
      professionCode = professionCode,
      secondRandom = secondRandom,
      rawId = busyCardNetId
    )
  }

  fun validate(busyCardNetId: String): Boolean {
    return parse(busyCardNetId) != null
  }

  fun getShortUrl(busyCardNetId: String): String {
    val parts = busyCardNetId.split("-")
    val identifier = if (parts.size >= 5) parts[4] else busyCardNetId
    return "busycardnet.com/c/$identifier"
  }
}
