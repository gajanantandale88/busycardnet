package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdCampaignPerformance
import com.example.data.model.ProfileAnalyticsSummary
import com.example.ui.components.BcnCardHyperlink
import com.example.ui.components.RechartsChartMode
import com.example.ui.components.RechartsNativeCanvasChart
import com.example.ui.components.RechartsWebView
import com.example.ui.viewmodel.BusyCardNetViewModel

enum class ChartEngineMode {
  RECHARTS_WEB,
  NATIVE_CANVAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsDashboardScreen(
  viewModel: BusyCardNetViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val primaryCard by viewModel.primaryCard.collectAsStateWithLifecycle()

  var selectedTimeframe by remember { mutableStateOf("7D") }
  var chartEngine by remember { mutableStateOf(ChartEngineMode.RECHARTS_WEB) }
  var nativeCanvasMode by remember { mutableStateOf(RechartsChartMode.OVERVIEW_VIEWS_CTR) }

  val businessName = primaryCard?.businessName ?: "Sharma Electrical & Smart Automation"
  val bcnId = primaryCard?.busyCardNetId ?: "IN-MH-MU-26-581ELEC347"

  val analyticsSummary = remember(selectedTimeframe, businessName, bcnId) {
    ProfileAnalyticsSummary.sampleData(
      timeframe = selectedTimeframe,
      businessName = businessName,
      bcnId = bcnId
    )
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Statistics Dashboard",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Powered by Recharts",
                fontSize = 11.sp,
                color = Color(0xFF0284C7),
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = " • ",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              BcnCardHyperlink(
                bcnId = bcnId,
                fontSize = 11.sp
              )
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("stats_dashboard_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              Toast.makeText(context, "🔄 Analytics refreshed with live BCN network metrics!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("stats_refresh_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
          IconButton(
            onClick = {
              Toast.makeText(context, "📄 Analytics report exported to PDF / CSV!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("stats_export_button")
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = "Export Report",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // 1. Controls Row: Timeframe Filter + Chart Engine Toggle
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Timeframe Selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "PERFORMANCE TIMEFRAME",
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.8.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf("7D", "30D", "90D", "1Y").forEach { tf ->
                val isSelected = selectedTimeframe == tf
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .clickable { selectedTimeframe = tf }
                    .testTag("timeframe_pill_$tf")
                ) {
                  Text(
                    text = tf,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                  )
                }
              }
            }
          }

          // Engine Toggle: Recharts Web Engine vs Native Canvas
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              EngineToggleButton(
                label = "📊 Recharts Engine (Interactive Web)",
                selected = chartEngine == ChartEngineMode.RECHARTS_WEB,
                onClick = { chartEngine = ChartEngineMode.RECHARTS_WEB },
                modifier = Modifier.weight(1f).testTag("recharts_web_engine_tab")
              )
              EngineToggleButton(
                label = "⚡ Native Canvas",
                selected = chartEngine == ChartEngineMode.NATIVE_CANVAS,
                onClick = { chartEngine = ChartEngineMode.NATIVE_CANVAS },
                modifier = Modifier.weight(1f).testTag("native_canvas_engine_tab")
              )
            }
          }
        }
      }

      // 2. Key Performance Indicators (KPI) Summary Grid
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "EXECUTIVE METRICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            KpiMetricCard(
              title = "Profile Views",
              value = analyticsSummary.totalProfileViews.toString(),
              growth = "+${analyticsSummary.profileViewsGrowth}%",
              sub = "${analyticsSummary.totalUniqueVisitors} unique visitors",
              icon = Icons.Default.Visibility,
              color = Color(0xFF0284C7),
              modifier = Modifier.weight(1f).testTag("kpi_card_profile_views")
            )
            KpiMetricCard(
              title = "Ad Click Rate (CTR)",
              value = "${String.format("%.2f", analyticsSummary.averageCtr)}%",
              growth = "+${analyticsSummary.ctrGrowth}%",
              sub = "Target: 4.50%",
              icon = Icons.Default.AdsClick,
              color = Color(0xFFF59E0B),
              modifier = Modifier.weight(1f).testTag("kpi_card_ctr")
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            KpiMetricCard(
              title = "Ad Impressions",
              value = analyticsSummary.totalAdImpressions.toString(),
              growth = "High Reach",
              sub = "Search & feed placements",
              icon = Icons.Default.AutoGraph,
              color = Color(0xFF6366F1),
              modifier = Modifier.weight(1f).testTag("kpi_card_impressions")
            )
            KpiMetricCard(
              title = "Inbound Leads",
              value = analyticsSummary.totalLeads.toString(),
              growth = "${String.format("%.1f", analyticsSummary.conversionRate)}% conv.",
              sub = "${analyticsSummary.totalAdClicks} total clicks",
              icon = Icons.Default.CheckCircle,
              color = Color(0xFF10B981),
              modifier = Modifier.weight(1f).testTag("kpi_card_leads")
            )
          }
        }
      }

      // 3. The Chart Visualization (Recharts Engine or Native Canvas Engine)
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "PERFORMANCE VISUALIZATION",
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.8.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF0284C7).copy(alpha = 0.15f)
            ) {
              Text(
                text = if (chartEngine == ChartEngineMode.RECHARTS_WEB) "Recharts v2.12 SVG" else "Compose 60fps Canvas",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (chartEngine == ChartEngineMode.RECHARTS_WEB) {
            // Recharts Web Engine
            RechartsWebView(
              summary = analyticsSummary,
              modifier = Modifier.fillMaxWidth()
            )
          } else {
            // Native Canvas Mode: Sub-mode Selector + Native Canvas Chart
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                listOf(
                  RechartsChartMode.OVERVIEW_VIEWS_CTR to "Views & CTR",
                  RechartsChartMode.IMPRESSIONS_VS_CLICKS to "Impr. vs Clicks",
                  RechartsChartMode.CTR_TRENDLINE to "CTR % Curve"
                ).forEach { (mode, label) ->
                  val isSelected = nativeCanvasMode == mode
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                      .weight(1f)
                      .clickable { nativeCanvasMode = mode }
                  ) {
                    Text(
                      text = label,
                      fontSize = 10.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(vertical = 6.dp),
                      textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                  }
                }
              }

              RechartsNativeCanvasChart(
                metrics = analyticsSummary.dailyMetrics,
                mode = nativeCanvasMode,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }

      // 4. Sponsored Ad Campaigns Performance Breakdown
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "ACTIVE AD CAMPAIGNS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.8.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${analyticsSummary.campaigns.size} Running",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF10B981)
            )
          }

          analyticsSummary.campaigns.forEach { campaign ->
            AdCampaignCard(campaign = campaign)
          }
        }
      }

      // 5. Smart CTR Optimization Advice Card
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "BCN AI CTR Optimization Recommendation",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "Your average CTR is 5.38% (exceeding the industry 4.5% benchmark). Profiles showing complete service price ranges and verified emergency availability generate 42% more inquiries through search intent banners.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
              )
            }
          }
        }
      }

      // Bottom Spacer
      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun EngineToggleButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
    shadowElevation = if (selected) 2.dp else 0.dp,
    modifier = modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      fontSize = 10.5.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
      color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(vertical = 8.dp),
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
  }
}

@Composable
private fun KpiMetricCard(
  title: String,
  value: String,
  growth: String,
  sub: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(26.dp)
            .background(color.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(15.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = color.copy(alpha = 0.12f)
        ) {
          Text(
            text = growth,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
        Text(
          text = sub,
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun AdCampaignCard(campaign: AdCampaignPerformance) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth().testTag("campaign_card_${campaign.campaignId}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = campaign.title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = campaign.placement,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF10B981).copy(alpha = 0.15f)
        ) {
          Text(
            text = "● ${campaign.status}",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF047857),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Metrics Row: Impressions, Clicks, CTR %, Leads
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        CampaignMetricItem(label = "Impressions", value = campaign.impressions.toString())
        CampaignMetricItem(label = "Clicks", value = campaign.clicks.toString())
        CampaignMetricItem(label = "CTR %", value = "${campaign.ctr}%", highlightColor = Color(0xFFF59E0B))
        CampaignMetricItem(label = "Leads", value = campaign.leads.toString(), highlightColor = Color(0xFF10B981))
      }
    }
  }
}

@Composable
private fun CampaignMetricItem(
  label: String,
  value: String,
  highlightColor: Color? = null
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      fontSize = 9.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.ExtraBold,
      color = highlightColor ?: MaterialTheme.colorScheme.onSurface
    )
  }
}
