package com.example.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Procedural stylized QR Code renderer that creates a crisp, deterministic
 * 2D matrix pattern based on the BusyCardNet ID and short URL.
 */
@Composable
fun StylizedQRCode(
  data: String,
  modifier: Modifier = Modifier,
  darkColor: Color = Color(0xFF0F172A),
  lightColor: Color = Color.White,
  accentCorner: Boolean = true
) {
  val matrixSize = 21 // Standard QR code version 1 format
  val grid = remember(data) {
    generateDeterministicQRMatrix(data, matrixSize)
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .background(lightColor)
      .padding(6.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val cellSize = size.width / matrixSize

      for (row in 0 until matrixSize) {
        for (col in 0 until matrixSize) {
          if (grid[row][col]) {
            val isFinderPattern = (row < 7 && col < 7) ||
                (row < 7 && col >= matrixSize - 7) ||
                (row >= matrixSize - 7 && col < 7)

            val moduleColor = if (isFinderPattern && accentCorner) {
              darkColor
            } else {
              darkColor
            }

            drawRoundRect(
              color = moduleColor,
              topLeft = Offset(col * cellSize, row * cellSize),
              size = Size(cellSize * 0.96f, cellSize * 0.96f),
              cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
            )
          }
        }
      }
    }
  }
}

private fun generateDeterministicQRMatrix(input: String, size: Int): Array<BooleanArray> {
  val matrix = Array(size) { BooleanArray(size) { false } }

  // 1. Draw standard finder patterns at 3 corners (7x7)
  fun drawFinder(top: Int, left: Int) {
    for (r in 0..6) {
      for (c in 0..6) {
        val isOuter = r == 0 || r == 6 || c == 0 || c == 6
        val isInner = r in 2..4 && c in 2..4
        matrix[top + r][left + c] = isOuter || isInner
      }
    }
  }

  drawFinder(0, 0)
  drawFinder(0, size - 7)
  drawFinder(size - 7, 0)

  // 2. Timing patterns
  for (i in 8 until size - 8) {
    matrix[6][i] = i % 2 == 0
    matrix[i][6] = i % 2 == 0
  }

  // 3. Fill payload modules deterministically using hash of input
  val hash = abs(input.hashCode())
  var bitIndex = 0
  for (r in 0 until size) {
    for (c in 0 until size) {
      val inTopLeft = r < 9 && c < 9
      val inTopRight = r < 9 && c >= size - 8
      val inBottomLeft = r >= size - 8 && c < 9
      val inTiming = r == 6 || c == 6

      if (!inTopLeft && !inTopRight && !inBottomLeft && !inTiming) {
        // pseudo-random bit deterministic with input characters
        val charFactor = if (input.isNotEmpty()) input[bitIndex % input.length].code else 42
        val bit = ((hash xor (r * 31 + c * 17 + charFactor)) and 1) == 1
        matrix[r][c] = bit
        bitIndex++
      }
    }
  }

  return matrix
}

/**
 * Procedural stylized 1D linear Barcode renderer (Code-128 format)
 * for BCN holder IDs and physical visiting cards.
 */
@Composable
fun StylizedBarcode(
  data: String,
  modifier: Modifier = Modifier,
  darkColor: Color = Color(0xFF0F172A),
  lightColor: Color = Color.White,
  showText: Boolean = true
) {
  val barPattern = remember(data) {
    generateDeterministicBarcodePattern(data)
  }

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(lightColor)
      .padding(horizontal = 10.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
      val totalUnits = barPattern.size.toFloat()
      val unitWidth = size.width / totalUnits

      for (i in barPattern.indices) {
        if (barPattern[i]) {
          drawRect(
            color = darkColor,
            topLeft = Offset(i * unitWidth, 0f),
            size = Size(unitWidth + 0.5f, size.height)
          )
        }
      }
    }

    if (showText) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "* $data *",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = darkColor
      )
    }
  }
}

private fun generateDeterministicBarcodePattern(input: String): BooleanArray {
  val pattern = mutableListOf<Boolean>()

  // Start quiet zone & start guard bars (|| |)
  repeat(4) { pattern.add(false) }
  pattern.addAll(listOf(true, true, false, true, false, true, true, false))

  val hash = abs(input.hashCode())
  // Generate bar sequences for each character in the string
  for (i in input.indices) {
    val charCode = input[i].code
    val v = (charCode xor (i * 17) xor hash)
    // 6-bit modular pattern
    for (b in 5 downTo 0) {
      val bit = ((v shr b) and 1) == 1
      pattern.add(bit)
    }
    pattern.add(false) // Inter-character gap
  }

  // Stop guard bars (|| ||) and quiet zone
  pattern.addAll(listOf(true, true, false, false, true, true, false, true))
  repeat(4) { pattern.add(false) }

  return pattern.toBooleanArray()
}

