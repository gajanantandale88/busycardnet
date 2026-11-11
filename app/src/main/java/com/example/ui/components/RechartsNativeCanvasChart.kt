package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPerformanceMetric

enum class RechartsChartMode {
  OVERVIEW_VIEWS_CTR,
  IMPRESSIONS_VS_CLICKS,
  CTR_TRENDLINE
}

@Composable
fun RechartsNativeCanvasChart(
  metrics: List<DailyPerformanceMetric>,
  mode: RechartsChartMode = RechartsChartMode.OVERVIEW_VIEWS_CTR,
  modifier: Modifier = Modifier
) {
  if (metrics.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(240.dp)
        .background(Color(0xFF0F172A), RoundedCornerShape(14.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text("No metrics data available", color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
    return
  }

  var selectedIndex by remember { mutableStateOf<Int?>(null) }
  val activeIndex = selectedIndex?.coerceIn(0, metrics.size - 1)
  val hoveredItem = if (activeIndex != null) metrics[activeIndex] else metrics.lastOrNull()

  val animProgress by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(durationMillis = 650),
    label = "recharts_chart_anim"
  )

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("recharts_native_canvas_chart")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header with Title & Scrubber Active Indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = when (mode) {
              RechartsChartMode.OVERVIEW_VIEWS_CTR -> "Profile Views & Ad CTR (%)"
              RechartsChartMode.IMPRESSIONS_VS_CLICKS -> "Ad Impressions & Clicks Volume"
              RechartsChartMode.CTR_TRENDLINE -> "Ad Click-Through Rate (CTR) Curve"
            },
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Recharts-inspired responsive canvas with touch inspection",
            fontSize = 10.sp,
            color = Color(0xFF94A3B8)
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF0284C7).copy(alpha = 0.2f)
        ) {
          Text(
            text = if (selectedIndex != null) "Scrubbing ${hoveredItem?.date}" else "Touch to inspect",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF38BDF8),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Interactive Canvas Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .pointerInput(metrics) {
            detectTapGestures(
              onTap = { offset ->
                val step = size.width / (metrics.size - 1).coerceAtLeast(1)
                val idx = (offset.x / step).toInt().coerceIn(0, metrics.size - 1)
                selectedIndex = idx
              }
            )
          }
          .pointerInput(metrics) {
            detectDragGestures(
              onDragStart = { offset ->
                val step = size.width / (metrics.size - 1).coerceAtLeast(1)
                val idx = (offset.x / step).toInt().coerceIn(0, metrics.size - 1)
                selectedIndex = idx
              },
              onDrag = { change, _ ->
                val step = size.width / (metrics.size - 1).coerceAtLeast(1)
                val idx = (change.position.x / step).toInt().coerceIn(0, metrics.size - 1)
                selectedIndex = idx
              },
              onDragEnd = { /* keep last or clear */ },
              onDragCancel = { selectedIndex = null }
            )
          }
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val paddingLeft = 40.dp.toPx()
          val paddingRight = 40.dp.toPx()
          val paddingTop = 20.dp.toPx()
          val paddingBottom = 26.dp.toPx()

          val chartW = canvasW - paddingLeft - paddingRight
          val chartH = canvasH - paddingTop - paddingBottom
          val n = metrics.size

          // 1. Draw Cartesian Grid Lines (4 horizontal dashed lines)
          val gridLineColor = Color(0xFF1E293B)
          val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

          for (i in 0..4) {
            val y = paddingTop + (chartH / 4) * i
            drawLine(
              color = gridLineColor,
              start = Offset(paddingLeft, y),
              end = Offset(canvasW - paddingRight, y),
              strokeWidth = 1f,
              pathEffect = dashEffect
            )
          }

          if (mode == RechartsChartMode.OVERVIEW_VIEWS_CTR) {
            val maxViews = (metrics.maxOfOrNull { it.profileViews } ?: 100) * 1.2f
            val maxCtr = ((metrics.maxOfOrNull { it.clickThroughRate } ?: 5f) * 1.25f).coerceAtLeast(6f)

            // Calculate points for Views Area & Line
            val viewPoints = metrics.mapIndexed { i, m ->
              val x = paddingLeft + (chartW / (n - 1).coerceAtLeast(1)) * i
              val y = paddingTop + chartH - ((m.profileViews / maxViews) * chartH * animProgress)
              Offset(x, y)
            }

            // Draw Area Gradient for Profile Views
            val areaPath = Path().apply {
              moveTo(viewPoints.first().x, paddingTop + chartH)
              viewPoints.forEach { pt -> lineTo(pt.x, pt.y) }
              lineTo(viewPoints.last().x, paddingTop + chartH)
              close()
            }
            drawPath(
              path = areaPath,
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF38BDF8).copy(alpha = 0.45f),
                  Color(0xFF38BDF8).copy(alpha = 0.02f)
                ),
                startY = paddingTop,
                endY = paddingTop + chartH
              )
            )

            // Draw Profile Views Line
            val linePath = Path().apply {
              moveTo(viewPoints.first().x, viewPoints.first().y)
              for (i in 1 until viewPoints.size) {
                val prev = viewPoints[i - 1]
                val curr = viewPoints[i]
                val midX = (prev.x + curr.x) / 2
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
              }
            }
            drawPath(
              path = linePath,
              color = Color(0xFF38BDF8),
              style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Calculate points for CTR Curve
            val ctrPoints = metrics.mapIndexed { i, m ->
              val x = paddingLeft + (chartW / (n - 1).coerceAtLeast(1)) * i
              val y = paddingTop + chartH - ((m.clickThroughRate / maxCtr) * chartH * animProgress)
              Offset(x, y)
            }

            val ctrLinePath = Path().apply {
              moveTo(ctrPoints.first().x, ctrPoints.first().y)
              for (i in 1 until ctrPoints.size) {
                val prev = ctrPoints[i - 1]
                val curr = ctrPoints[i]
                val midX = (prev.x + curr.x) / 2
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
              }
            }
            drawPath(
              path = ctrLinePath,
              color = Color(0xFFF59E0B),
              style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // CTR dot markers
            ctrPoints.forEach { pt ->
              drawCircle(Color(0xFF0F172A), radius = 4.5.dp.toPx(), center = pt)
              drawCircle(Color(0xFFF59E0B), radius = 3.dp.toPx(), center = pt)
            }

            // Draw Active Scrubber Line & Dot
            if (activeIndex != null && activeIndex < viewPoints.size) {
              val scrubPt = viewPoints[activeIndex]
              drawLine(
                color = Color(0xFF94A3B8).copy(alpha = 0.6f),
                start = Offset(scrubPt.x, paddingTop),
                end = Offset(scrubPt.x, paddingTop + chartH),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
              )
              drawCircle(Color.White, radius = 5.dp.toPx(), center = scrubPt)
              drawCircle(Color(0xFF38BDF8), radius = 3.5.dp.toPx(), center = scrubPt)
            }

          } else if (mode == RechartsChartMode.IMPRESSIONS_VS_CLICKS) {
            val maxImps = (metrics.maxOfOrNull { it.adImpressions } ?: 1000) * 1.15f
            val maxClicks = (metrics.maxOfOrNull { it.adClicks } ?: 100) * 1.15f
            val slotW = chartW / n
            val barW = (slotW * 0.30f).coerceAtLeast(6f)

            metrics.forEachIndexed { i, m ->
              val cx = paddingLeft + slotW * i + slotW / 2

              // Bar 1: Impressions (Indigo)
              val h1 = ((m.adImpressions / maxImps) * chartH * animProgress).coerceAtLeast(4f)
              drawRoundRect(
                brush = Brush.verticalGradient(
                  listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
                ),
                topLeft = Offset(cx - barW - 2f, paddingTop + chartH - h1),
                size = Size(barW, h1),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
              )

              // Bar 2: Clicks (Emerald)
              val h2 = ((m.adClicks / maxClicks) * chartH * 0.85f * animProgress).coerceAtLeast(4f)
              drawRoundRect(
                brush = Brush.verticalGradient(
                  listOf(Color(0xFF10B981), Color(0xFF059669))
                ),
                topLeft = Offset(cx + 2f, paddingTop + chartH - h2),
                size = Size(barW, h2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
              )
            }
          } else {
            // CTR Trendline with 4.5% Benchmark Line
            val maxCtr = ((metrics.maxOfOrNull { it.clickThroughRate } ?: 5f) * 1.2f).coerceAtLeast(6.5f)

            // Benchmark reference line at 4.5%
            val refY = paddingTop + chartH - ((4.5f / maxCtr) * chartH)
            drawLine(
              color = Color(0xFFEF4444),
              start = Offset(paddingLeft, refY),
              end = Offset(canvasW - paddingRight, refY),
              strokeWidth = 1.5.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            val ctrPoints = metrics.mapIndexed { i, m ->
              val x = paddingLeft + (chartW / (n - 1).coerceAtLeast(1)) * i
              val y = paddingTop + chartH - ((m.clickThroughRate / maxCtr) * chartH * animProgress)
              Offset(x, y)
            }

            // Gradient Fill
            val areaPath = Path().apply {
              moveTo(ctrPoints.first().x, paddingTop + chartH)
              ctrPoints.forEach { pt -> lineTo(pt.x, pt.y) }
              lineTo(ctrPoints.last().x, paddingTop + chartH)
              close()
            }
            drawPath(
              path = areaPath,
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFFF59E0B).copy(alpha = 0.35f),
                  Color(0xFFF59E0B).copy(alpha = 0.01f)
                ),
                startY = paddingTop,
                endY = paddingTop + chartH
              )
            )

            // Line
            val linePath = Path().apply {
              moveTo(ctrPoints.first().x, ctrPoints.first().y)
              for (i in 1 until ctrPoints.size) {
                val prev = ctrPoints[i - 1]
                val curr = ctrPoints[i]
                val midX = (prev.x + curr.x) / 2
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
              }
            }
            drawPath(
              path = linePath,
              color = Color(0xFFF59E0B),
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            ctrPoints.forEach { pt ->
              drawCircle(Color(0xFF0F172A), radius = 5.dp.toPx(), center = pt)
              drawCircle(Color(0xFFF59E0B), radius = 3.5.dp.toPx(), center = pt)
            }
          }

          // X-Axis Date Labels using native canvas drawText
          val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
          }

          metrics.forEachIndexed { i, m ->
            val x = paddingLeft + (chartW / (n - 1).coerceAtLeast(1)) * i
            drawContext.canvas.nativeCanvas.drawText(
              m.date,
              x,
              canvasH - 4.dp.toPx(),
              paint
            )
          }
        }
      }

      // Legend Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        when (mode) {
          RechartsChartMode.OVERVIEW_VIEWS_CTR -> {
            LegendItem(color = Color(0xFF38BDF8), label = "Profile Views (Left Axis)")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color(0xFFF59E0B), label = "Ad CTR % (Right Axis)")
          }
          RechartsChartMode.IMPRESSIONS_VS_CLICKS -> {
            LegendItem(color = Color(0xFF6366F1), label = "Ad Impressions")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color(0xFF10B981), label = "Ad Clicks")
          }
          RechartsChartMode.CTR_TRENDLINE -> {
            LegendItem(color = Color(0xFFF59E0B), label = "Actual CTR %")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color(0xFFEF4444), label = "Benchmark Target (4.5%)")
          }
        }
      }

      // Hover / Scrubber Active Detail Inspection Card
      if (hoveredItem != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF1E293B).copy(alpha = 0.85f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "📅 ${hoveredItem.fullDate} (${hoveredItem.date})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Profile Views: ${hoveredItem.profileViews} • Unique: ${hoveredItem.uniqueVisitors}",
                fontSize = 10.5.sp,
                color = Color(0xFF38BDF8)
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "CTR: ${String.format("%.2f", hoveredItem.clickThroughRate)}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF59E0B)
              )
              Text(
                text = "${hoveredItem.adClicks} Clicks / ${hoveredItem.adImpressions} Impr.",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .background(color, RoundedCornerShape(2.dp))
    )
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = label,
      fontSize = 10.5.sp,
      color = Color(0xFFCBD5E1),
      fontWeight = FontWeight.Medium
    )
  }
}
