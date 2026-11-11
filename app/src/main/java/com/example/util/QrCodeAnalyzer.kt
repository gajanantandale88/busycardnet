package com.example.util

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * CameraX ImageAnalysis Analyzer that processes live camera frames to decode QR codes and Barcodes
 * using ZXing. Handles YUV plane rowStride padding and portrait sensor rotation smoothly on a
 * dedicated background thread without dropping UI frames, and supports decoding from gallery Bitmaps.
 */
class QrCodeAnalyzer(
  private val onQrCodeScanned: (resultText: String) -> Unit
) : ImageAnalysis.Analyzer {

  private val reader = MultiFormatReader().apply {
    setHints(buildDecodeHints())
  }

  private var lastScanTimestamp = 0L

  @OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    val currentTimestamp = System.currentTimeMillis()
    // Throttle to avoid duplicate rapid-fire scans within 1.1 seconds
    if (currentTimestamp - lastScanTimestamp < 1100) {
      imageProxy.close()
      return
    }

    val mediaImage = imageProxy.image
    if (mediaImage == null) {
      imageProxy.close()
      return
    }

    try {
      val planes = mediaImage.planes
      if (planes.isNotEmpty()) {
        val yPlane = planes[0]
        val yBuffer: ByteBuffer = yPlane.buffer
        yBuffer.rewind()

        val width = imageProxy.width
        val height = imageProxy.height
        val rowStride = yPlane.rowStride

        // Extract tightly-packed luminance bytes respecting hardware rowStride padding
        val yBytes = ByteArray(width * height)
        if (rowStride == width) {
          yBuffer.get(yBytes, 0, width * height)
        } else {
          val rowBuffer = ByteArray(rowStride)
          for (row in 0 until height) {
            val bytesToRead = if (row == height - 1) {
              minOf(rowStride, yBuffer.remaining())
            } else {
              rowStride
            }
            yBuffer.get(rowBuffer, 0, bytesToRead)
            System.arraycopy(rowBuffer, 0, yBytes, row * width, minOf(width, bytesToRead))
          }
        }

        // 1. Try decoding in sensor's native orientation
        var decodedText = tryDecodeLuminance(yBytes, width, height)

        // 2. If portrait-rotated frame (90/270 deg) had a 1D barcode or angled QR, try 90-deg rotated luminance
        if (decodedText == null && (imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270)) {
          val rotated = ByteArray(width * height)
          for (y in 0 until height) {
            for (x in 0 until width) {
              rotated[x * height + (height - y - 1)] = yBytes[y * width + x]
            }
          }
          decodedText = tryDecodeLuminance(rotated, height, width)
        }

        if (!decodedText.isNullOrBlank()) {
          lastScanTimestamp = currentTimestamp
          onQrCodeScanned(decodedText.trim())
        }
      }
    } catch (_: Exception) {
      // Frame didn't contain a readable QR code; continue smoothly to the next frame
    } finally {
      reader.reset()
      imageProxy.close()
    }
  }

  private fun tryDecodeLuminance(luminance: ByteArray, dataWidth: Int, dataHeight: Int): String? {
    return try {
      val source = PlanarYUVLuminanceSource(
        luminance,
        dataWidth,
        dataHeight,
        0,
        0,
        dataWidth,
        dataHeight,
        false
      )
      val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
      reader.decodeWithState(binaryBitmap)?.text
    } catch (_: Exception) {
      reader.reset()
      null
    }
  }

  companion object {
    private fun buildDecodeHints(): Map<DecodeHintType, Any> = mapOf(
      DecodeHintType.POSSIBLE_FORMATS to listOf(
        BarcodeFormat.QR_CODE,
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.DATA_MATRIX
      ),
      DecodeHintType.TRY_HARDER to true,
      DecodeHintType.ALSO_INVERTED to true
    )

    /**
     * Decodes a QR code or barcode from a static Bitmap (e.g. selected via Android Photo Picker).
     */
    fun decodeFromBitmap(bitmap: Bitmap): String? {
      return try {
        val scaled = if (bitmap.width > 1200 || bitmap.height > 1200) {
          val ratio = minOf(1200f / bitmap.width, 1200f / bitmap.height)
          Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true
          )
        } else {
          bitmap
        }

        val width = scaled.width
        val height = scaled.height
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val singleReader = MultiFormatReader().apply { setHints(buildDecodeHints()) }
        singleReader.decodeWithState(binaryBitmap)?.text?.trim()
      } catch (_: Exception) {
        null
      }
    }
  }
}
