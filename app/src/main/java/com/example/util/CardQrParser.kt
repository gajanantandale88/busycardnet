package com.example.util

import com.example.data.local.DigitalCardEntity
import org.json.JSONObject
import java.util.UUID

/**
 * Utility that extracts and parses digital business cards from QR code payloads.
 * Supports:
 * - BusyCardNet unique ID / short URLs (e.g. https://busycard.net/c/IN-MH-MU-26-581ELEC347)
 * - Structured JSON card payloads (for cross-device instant offline transfer)
 * - Standard vCard format (BEGIN:VCARD ... END:VCARD)
 */
object CardQrParser {

  private val BCN_ID_REGEX = Regex("[A-Z]{2}-[A-Z]{2}-[A-Z]{2}-[0-9]{2}-[0-9A-Z]{6,14}")

  fun extractBusyCardNetId(text: String): String? {
    val trimmed = text.trim()
    val match = BCN_ID_REGEX.find(trimmed)
    return match?.value
  }

  fun parse(rawQrContent: String, fallbackUserId: String = "imported-user"): DigitalCardEntity? =
    parseToCard(rawQrContent, fallbackUserId)

  fun parseToCard(rawQrContent: String, fallbackUserId: String = "imported-user"): DigitalCardEntity? {
    val trimmed = rawQrContent.trim()

    // 1. JSON Card Format
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
      try {
        val json = JSONObject(trimmed)
        val bcnId = json.optString("busyCardNetId", "").ifEmpty {
          json.optString("id", "").ifEmpty {
            extractBusyCardNetId(trimmed) ?: "IN-MH-MU-26-${UUID.randomUUID().toString().take(8).uppercase()}"
          }
        }
        val name = json.optString("name", json.optString("fullName", "Digital Cardholder"))
        val businessName = json.optString("businessName", json.optString("company", name))
        val designation = json.optString("designation", json.optString("title", "Professional"))
        val phone = json.optString("phone", json.optString("tel", "+91 98765 43210"))
        val whatsapp = json.optString("whatsapp", phone)
        val email = json.optString("email", "")
        val website = json.optString("website", "")
        val services = json.optString("services", "Consultation, Services")
        val address = json.optString("address", "Mumbai, India")
        val about = json.optString("about", "Imported via CameraX QR Scanner")
        val profCode = json.optString("professionCode", "BIZ")
        val templateStyle = json.optString("templateStyle", "MODERN_DARK")
        val accentColor = json.optString("accentColorHex", "#38BDF8")

        return DigitalCardEntity(
          cardId = "imp-" + UUID.randomUUID().toString().take(8),
          internalUserId = fallbackUserId,
          busyCardNetId = bcnId,
          name = name,
          designation = designation,
          professionCode = profCode,
          businessName = businessName,
          services = services,
          phone = phone,
          whatsapp = whatsapp,
          email = email,
          website = website,
          address = address,
          workingHours = "9:00 AM - 8:00 PM",
          about = about,
          templateStyle = templateStyle,
          accentColorHex = accentColor,
          isPrimary = false,
          viewsCount = 1,
          sharesCount = 0,
          scansCount = 1,
          leadsCount = 0
        )
      } catch (e: Exception) {
        // Fallback to next parser
      }
    }

    // 2. Standard vCard format
    if (trimmed.contains("BEGIN:VCARD", ignoreCase = true)) {
      try {
        var name = "Card Contact"
        var org = ""
        var title = "Contact"
        var phone = ""
        var email = ""
        var url = ""
        var note = ""

        trimmed.lines().forEach { line ->
          val upper = line.uppercase()
          when {
            upper.startsWith("FN:") -> name = line.substring(3).trim()
            upper.startsWith("ORG:") -> org = line.substring(4).trim()
            upper.startsWith("TITLE:") -> title = line.substring(6).trim()
            upper.startsWith("TEL") && upper.contains(":") -> phone = line.substringAfter(":").trim()
            upper.startsWith("EMAIL") && upper.contains(":") -> email = line.substringAfter(":").trim()
            upper.startsWith("URL") && upper.contains(":") -> url = line.substringAfter(":").trim()
            upper.startsWith("NOTE:") -> note = line.substring(5).trim()
          }
        }

        val bcnId = extractBusyCardNetId(trimmed) ?: "IN-MH-MU-26-VCARD${UUID.randomUUID().toString().take(6).uppercase()}"

        return DigitalCardEntity(
          cardId = "vcard-" + UUID.randomUUID().toString().take(8),
          internalUserId = fallbackUserId,
          busyCardNetId = bcnId,
          name = name,
          designation = title,
          professionCode = "BIZ",
          businessName = org.ifEmpty { name },
          services = "Professional Services",
          phone = phone.ifEmpty { "+91 98765 43210" },
          whatsapp = phone.ifEmpty { "+91 98765 43210" },
          email = email,
          website = url,
          address = "Locally Shared vCard",
          workingHours = "10:00 AM - 7:00 PM",
          about = note.ifEmpty { "Imported directly from vCard QR code" },
          templateStyle = "CORPORATE",
          accentColorHex = "#0284C7",
          isPrimary = false,
          viewsCount = 1,
          sharesCount = 0,
          scansCount = 1,
          leadsCount = 0
        )
      } catch (e: Exception) {
        // Fallback
      }
    }

    // 3. Raw BCN ID or URL format
    val bcnId = extractBusyCardNetId(trimmed)
    if (bcnId != null) {
      val parsedId = BusyCardIdGenerator.parse(bcnId)
      val profCode = parsedId?.professionCode ?: "BIZ"
      return DigitalCardEntity(
        cardId = "imp-" + UUID.randomUUID().toString().take(8),
        internalUserId = fallbackUserId,
        busyCardNetId = bcnId,
        name = "$profCode Professional",
        designation = "$profCode Specialist",
        professionCode = profCode,
        businessName = "BusyCardNet Partner",
        services = "Verified Services, Direct BCN Contact",
        phone = "+91 98000 00000",
        whatsapp = "+91 98000 00000",
        email = "contact@busycardnet.com",
        website = "https://busycardnet.com/c/$bcnId",
        address = "${parsedId?.city ?: "Mumbai"}, ${parsedId?.country ?: "India"}",
        workingHours = "9:00 AM - 8:00 PM",
        about = "Scanned and verified BusyCardNet member card.",
        templateStyle = "MODERN_DARK",
        accentColorHex = "#38BDF8",
        isPrimary = false
      )
    }

    // 4. Generic Contact / Text / URL Fallback
    if (trimmed.length in 4..1000) {
      return parseGenericContact(trimmed, fallbackUserId)
    }

    return null
  }

  fun parseGenericContact(text: String, fallbackUserId: String = "imported-user"): DigitalCardEntity? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null

    val phoneRegex = Regex("""(\+?[0-9]{1,3}[- ]?)?([0-9]{10}|[0-9]{3}[- ][0-9]{3}[- ][0-9]{4})""")
    val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}""")
    val urlRegex = Regex("""https?://[^\s]+""")

    val foundPhone = phoneRegex.find(trimmed)?.value ?: "+91 98765 43210"
    val foundEmail = emailRegex.find(trimmed)?.value ?: ""
    val foundUrl = urlRegex.find(trimmed)?.value ?: ""

    val firstLine = trimmed.lines().firstOrNull()?.take(40) ?: "Scanned Contact"
    val bcnId = extractBusyCardNetId(trimmed) ?: "IN-MH-MU-26-IMP${UUID.randomUUID().toString().take(6).uppercase()}"

    return DigitalCardEntity(
      cardId = "imp-" + UUID.randomUUID().toString().take(8),
      internalUserId = fallbackUserId,
      busyCardNetId = bcnId,
      name = firstLine,
      designation = "Imported Contact",
      professionCode = "BIZ",
      businessName = firstLine,
      services = "General Services",
      phone = foundPhone,
      whatsapp = foundPhone,
      email = foundEmail,
      website = foundUrl,
      address = "Imported via CameraX QR Scanner",
      workingHours = "9:00 AM - 6:00 PM",
      about = trimmed.take(200),
      templateStyle = "MODERN_DARK",
      accentColorHex = "#38BDF8",
      isPrimary = false
    )
  }

  /**
   * Generates a portable JSON payload for a digital card that can be encoded in QR code.
   */
  fun generateQrPayload(card: DigitalCardEntity): String {
    val json = JSONObject().apply {
      put("busyCardNetId", card.busyCardNetId)
      put("name", card.name)
      put("businessName", card.businessName)
      put("designation", card.designation)
      put("professionCode", card.professionCode)
      put("services", card.services)
      put("phone", card.phone)
      put("whatsapp", card.whatsapp)
      put("email", card.email)
      put("website", card.website)
      put("address", card.address)
      put("about", card.about)
      put("templateStyle", card.templateStyle)
      put("accentColorHex", card.accentColorHex)
    }
    return json.toString()
  }
}
