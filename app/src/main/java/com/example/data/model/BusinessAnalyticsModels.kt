package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Data model for daily performance metrics tracking Business Profile Views and Ad Click-Through Rates.
 */
data class DailyPerformanceMetric(
  val date: String,
  val fullDate: String,
  val profileViews: Int,
  val uniqueVisitors: Int,
  val adImpressions: Int,
  val adClicks: Int,
  val clickThroughRate: Float, // in percentage, e.g. 5.2f for 5.2%
  val leadsCount: Int
) {
  fun toJsonObject(): JSONObject {
    return JSONObject().apply {
      put("date", date)
      put("fullDate", fullDate)
      put("profileViews", profileViews)
      put("views", profileViews)
      put("uniqueVisitors", uniqueVisitors)
      put("adImpressions", adImpressions)
      put("impressions", adImpressions)
      put("adClicks", adClicks)
      put("clicks", adClicks)
      put("ctr", String.format("%.2f", clickThroughRate).toDoubleOrNull() ?: clickThroughRate)
      put("leads", leadsCount)
    }
  }
}

/**
 * Performance metrics for individual sponsored ad campaigns.
 */
data class AdCampaignPerformance(
  val campaignId: String,
  val title: String,
  val placement: String,
  val impressions: Int,
  val clicks: Int,
  val ctr: Float,
  val spend: Double,
  val leads: Int,
  val status: String // "ACTIVE", "PAUSED", "COMPLETED"
) {
  fun toJsonObject(): JSONObject {
    return JSONObject().apply {
      put("campaignId", campaignId)
      put("title", title)
      put("placement", placement)
      put("impressions", impressions)
      put("clicks", clicks)
      put("ctr", String.format("%.2f", ctr).toDoubleOrNull() ?: ctr)
      put("spend", spend)
      put("leads", leads)
      put("status", status)
    }
  }
}

/**
 * Complete summary of Business Profile Views and Ad CTR performance.
 */
data class ProfileAnalyticsSummary(
  val cardId: String,
  val businessName: String,
  val bcnId: String,
  val timeframe: String, // "7D", "30D", "90D", "1Y"
  val totalProfileViews: Int,
  val profileViewsGrowth: Float,
  val totalUniqueVisitors: Int,
  val totalAdImpressions: Int,
  val totalAdClicks: Int,
  val averageCtr: Float,
  val ctrGrowth: Float,
  val totalLeads: Int,
  val conversionRate: Float,
  val dailyMetrics: List<DailyPerformanceMetric>,
  val campaigns: List<AdCampaignPerformance>
) {
  fun toJsonString(): String {
    val json = JSONObject().apply {
      put("cardId", cardId)
      put("businessName", businessName)
      put("bcnId", bcnId)
      put("timeframe", timeframe)
      put("totalProfileViews", totalProfileViews)
      put("profileViewsGrowth", profileViewsGrowth)
      put("totalUniqueVisitors", totalUniqueVisitors)
      put("totalAdImpressions", totalAdImpressions)
      put("totalAdClicks", totalAdClicks)
      put("averageCtr", String.format("%.2f", averageCtr).toDoubleOrNull() ?: averageCtr)
      put("ctrGrowth", ctrGrowth)
      put("totalLeads", totalLeads)
      put("conversionRate", conversionRate)

      val metricsArray = JSONArray()
      dailyMetrics.forEach { metricsArray.put(it.toJsonObject()) }
      put("dailyMetrics", metricsArray)

      val campaignsArray = JSONArray()
      campaigns.forEach { campaignsArray.put(it.toJsonObject()) }
      put("campaigns", campaignsArray)
    }
    return json.toString()
  }

  companion object {
    fun sampleData(
      timeframe: String = "7D",
      businessName: String = "Sharma Electrical & Smart Automation",
      bcnId: String = "IN-MH-MU-26-581ELEC347"
    ): ProfileAnalyticsSummary {
      val metrics = when (timeframe) {
        "7D" -> listOf(
          DailyPerformanceMetric("Mon", "2026-09-08", profileViews = 142, uniqueVisitors = 118, adImpressions = 890, adClicks = 42, clickThroughRate = 4.72f, leadsCount = 9),
          DailyPerformanceMetric("Tue", "2026-09-09", profileViews = 188, uniqueVisitors = 152, adImpressions = 1120, adClicks = 58, clickThroughRate = 5.18f, leadsCount = 14),
          DailyPerformanceMetric("Wed", "2026-09-10", profileViews = 165, uniqueVisitors = 139, adImpressions = 980, adClicks = 46, clickThroughRate = 4.69f, leadsCount = 11),
          DailyPerformanceMetric("Thu", "2026-09-11", profileViews = 210, uniqueVisitors = 176, adImpressions = 1350, adClicks = 74, clickThroughRate = 5.48f, leadsCount = 18),
          DailyPerformanceMetric("Fri", "2026-09-12", profileViews = 245, uniqueVisitors = 205, adImpressions = 1580, adClicks = 92, clickThroughRate = 5.82f, leadsCount = 22),
          DailyPerformanceMetric("Sat", "2026-09-13", profileViews = 290, uniqueVisitors = 248, adImpressions = 1890, adClicks = 115, clickThroughRate = 6.08f, leadsCount = 28),
          DailyPerformanceMetric("Sun", "2026-09-14", profileViews = 270, uniqueVisitors = 224, adImpressions = 1720, adClicks = 98, clickThroughRate = 5.70f, leadsCount = 24)
        )
        "30D" -> {
          val days = listOf(
            "Aug 16", "Aug 19", "Aug 22", "Aug 25", "Aug 28", "Aug 31",
            "Sep 03", "Sep 06", "Sep 09", "Sep 12", "Sep 14"
          )
          days.mapIndexed { i, label ->
            val factor = 1.0f + (i * 0.08f)
            val views = (130 * factor).toInt()
            val imps = (850 * factor).toInt()
            val clicks = (40 * factor * (1.0f + (i % 3) * 0.05f)).toInt()
            val ctr = (clicks.toFloat() / imps.coerceAtLeast(1)) * 100f
            DailyPerformanceMetric(
              date = label,
              fullDate = "2026-$label",
              profileViews = views,
              uniqueVisitors = (views * 0.82f).toInt(),
              adImpressions = imps,
              adClicks = clicks,
              clickThroughRate = ctr,
              leadsCount = (clicks * 0.22f).toInt()
            )
          }
        }
        "90D" -> {
          val months = listOf(
            "W1 Jun", "W2 Jun", "W3 Jun", "W4 Jun",
            "W1 Jul", "W2 Jul", "W3 Jul", "W4 Jul",
            "W1 Aug", "W2 Aug", "W3 Aug", "W4 Aug"
          )
          months.mapIndexed { i, label ->
            val views = 450 + (i * 45) + ((i % 2) * 30)
            val imps = 3200 + (i * 280)
            val clicks = 150 + (i * 18)
            val ctr = (clicks.toFloat() / imps.coerceAtLeast(1)) * 100f
            DailyPerformanceMetric(
              date = label,
              fullDate = "2026-Q3-$i",
              profileViews = views,
              uniqueVisitors = (views * 0.8f).toInt(),
              adImpressions = imps,
              adClicks = clicks,
              clickThroughRate = ctr,
              leadsCount = (clicks * 0.24f).toInt()
            )
          }
        }
        else -> listOf(
          DailyPerformanceMetric("Q1", "2026-Q1", profileViews = 3800, uniqueVisitors = 3100, adImpressions = 24000, adClicks = 1120, clickThroughRate = 4.67f, leadsCount = 260),
          DailyPerformanceMetric("Q2", "2026-Q2", profileViews = 5100, uniqueVisitors = 4250, adImpressions = 32500, adClicks = 1680, clickThroughRate = 5.17f, leadsCount = 390),
          DailyPerformanceMetric("Q3", "2026-Q3", profileViews = 6850, uniqueVisitors = 5600, adImpressions = 41200, adClicks = 2380, clickThroughRate = 5.78f, leadsCount = 540)
        )
      }

      val totalViews = metrics.sumOf { it.profileViews }
      val totalVisitors = metrics.sumOf { it.uniqueVisitors }
      val totalImps = metrics.sumOf { it.adImpressions }
      val totalClicks = metrics.sumOf { it.adClicks }
      val avgCtr = if (totalImps > 0) (totalClicks.toFloat() / totalImps) * 100f else 0f
      val totalLeads = metrics.sumOf { it.leadsCount }
      val conversionRate = if (totalClicks > 0) (totalLeads.toFloat() / totalClicks) * 100f else 0f

      val campaigns = listOf(
        AdCampaignPerformance(
          campaignId = "camp-01",
          title = "Emergency Short-Circuit & Power Fault",
          placement = "Search Intent Top Banner (Electrician)",
          impressions = (totalImps * 0.45).toInt(),
          clicks = (totalClicks * 0.52).toInt(),
          ctr = 6.24f,
          spend = 1450.0,
          leads = (totalLeads * 0.54).toInt(),
          status = "ACTIVE"
        ),
        AdCampaignPerformance(
          campaignId = "camp-02",
          title = "Monsoon Home Wiring Inspection Special",
          placement = "Home Feed Featured Sponsored Card",
          impressions = (totalImps * 0.35).toInt(),
          clicks = (totalClicks * 0.32).toInt(),
          ctr = 4.85f,
          spend = 980.0,
          leads = (totalLeads * 0.30).toInt(),
          status = "ACTIVE"
        ),
        AdCampaignPerformance(
          campaignId = "camp-03",
          title = "Proximity 1km Smart Meter Automation",
          placement = "Discover Radar Map Sponsored Pin",
          impressions = (totalImps * 0.20).toInt(),
          clicks = (totalClicks * 0.16).toInt(),
          ctr = 4.15f,
          spend = 520.0,
          leads = (totalLeads * 0.16).toInt(),
          status = "ACTIVE"
        )
      )

      return ProfileAnalyticsSummary(
        cardId = "card-elec-01",
        businessName = businessName,
        bcnId = bcnId,
        timeframe = timeframe,
        totalProfileViews = totalViews,
        profileViewsGrowth = 24.8f,
        totalUniqueVisitors = totalVisitors,
        totalAdImpressions = totalImps,
        totalAdClicks = totalClicks,
        averageCtr = avgCtr,
        ctrGrowth = 1.6f,
        totalLeads = totalLeads,
        conversionRate = conversionRate,
        dailyMetrics = metrics,
        campaigns = campaigns
      )
    }
  }
}
