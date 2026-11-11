package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.util.Log
import com.example.data.model.CardPrintOptions
import java.io.FileOutputStream
import java.io.IOException

/**
 * Native Android PrintManager helper to print visiting cards directly to
 * WiFi/Bluetooth printers or export to standard PDF.
 */
object CardPrintHelper {

  private const val TAG = "CardPrintHelper"

  /**
   * Triggers Android Print Spooler with the current visiting card options.
   */
  fun printVisitingCard(
    context: Context,
    options: CardPrintOptions,
    onFinished: () -> Unit = {}
  ) {
    try {
      val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
      if (printManager == null) {
        Log.e(TAG, "PrintManager service is unavailable on this device.")
        return
      }

      val jobName = "VisitingCard_${options.bcnId.replace("-", "_")}"
      val adapter = VisitingCardPrintDocumentAdapter(options)

      val printAttributes = PrintAttributes.Builder()
        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()

      printManager.print(jobName, adapter, printAttributes)
      onFinished()
    } catch (e: Exception) {
      Log.e(TAG, "Error initiating card print: ${e.message}", e)
    }
  }

  private class VisitingCardPrintDocumentAdapter(
    private val options: CardPrintOptions
  ) : PrintDocumentAdapter() {

    private var totalPages = 1

    override fun onLayout(
      oldAttributes: PrintAttributes?,
      newAttributes: PrintAttributes?,
      cancellationSignal: CancellationSignal?,
      callback: LayoutResultCallback?,
      extras: Bundle?
    ) {
      if (cancellationSignal?.isCanceled == true) {
        callback?.onLayoutCancelled()
        return
      }

      totalPages = 2 // Page 1: Card Front (with cutting crop marks), Page 2: Card Back

      val info = PrintDocumentInfo.Builder("Visiting_Card_${options.bcnId}.pdf")
        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
        .setPageCount(totalPages)
        .build()

      callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
      pages: Array<out PageRange>?,
      destination: ParcelFileDescriptor?,
      cancellationSignal: CancellationSignal?,
      callback: WriteResultCallback?
    ) {
      val pdfDocument = PdfDocument()

      try {
        // Standard ISO business card 3.5 in x 2.0 in (at 72 dpi: 252 x 144 points; standard A4 is 595 x 842 pt)
        val pageWidth = 595
        val pageHeight = 842

        // Page 1: Front
        val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(page1Info)
        drawCardPage(page1.canvas, pageWidth, pageHeight, isFront = true)
        pdfDocument.finishPage(page1)

        // Page 2: Back
        val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = pdfDocument.startPage(page2Info)
        drawCardPage(page2.canvas, pageWidth, pageHeight, isFront = false)
        pdfDocument.finishPage(page2)

        if (cancellationSignal?.isCanceled == true) {
          callback?.onWriteCancelled()
          return
        }

        destination?.fileDescriptor?.let { fd ->
          FileOutputStream(fd).use { outStream ->
            pdfDocument.writeTo(outStream)
          }
        }

        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
      } catch (e: IOException) {
        Log.e(TAG, "Error writing PDF: ${e.message}", e)
        callback?.onWriteFailed(e.message)
      } finally {
        pdfDocument.close()
      }
    }

    private fun drawCardPage(
      canvas: Canvas,
      pageWidth: Int,
      pageHeight: Int,
      isFront: Boolean
    ) {
      val cardWidthPt = 320f
      val cardHeightPt = 190f
      val left = (pageWidth - cardWidthPt) / 2f
      val top = (pageHeight - cardHeightPt) / 2f
      val cardRect = RectF(left, top, left + cardWidthPt, top + cardHeightPt)

      val paint = Paint(Paint.ANTI_ALIAS_FLAG)

      // 1. Draw Card Background
      paint.color = if (options.theme.isDark) AndroidColor.parseColor(options.theme.primaryBgHex) else AndroidColor.WHITE
      canvas.drawRoundRect(cardRect, 10f, 10f, paint)

      // 2. Card Border / Accent
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 2f
      paint.color = AndroidColor.parseColor(options.theme.accentColorHex)
      canvas.drawRoundRect(cardRect, 10f, 10f, paint)

      // Reset paint for text
      paint.style = Paint.Style.FILL

      if (isFront) {
        // Front View
        paint.color = if (options.theme.isDark) AndroidColor.WHITE else AndroidColor.DKGRAY
        paint.textSize = 14f
        paint.isFakeBoldText = true

        var currentY = top + 34f

        if (options.includeCompanyName && options.companyName.isNotBlank()) {
          paint.textSize = 15f
          paint.color = AndroidColor.parseColor(options.theme.accentColorHex)
          canvas.drawText(options.companyName.uppercase(), left + 20f, currentY, paint)
          currentY += 18f
        }

        if (options.includeCompanyWords && options.companyWords.isNotBlank()) {
          paint.textSize = 9f
          paint.isFakeBoldText = false
          paint.color = if (options.theme.isDark) AndroidColor.LTGRAY else AndroidColor.GRAY
          val wordsSnippet = options.companyWords.take(65)
          canvas.drawText(wordsSnippet, left + 20f, currentY, paint)
          currentY += 24f
        }

        if (options.includeName && options.name.isNotBlank()) {
          paint.textSize = 14f
          paint.isFakeBoldText = true
          paint.color = if (options.theme.isDark) AndroidColor.WHITE else AndroidColor.BLACK
          canvas.drawText(options.name, left + 20f, currentY, paint)
          currentY += 16f
        }

        if (options.includeBcnId && options.bcnId.isNotBlank()) {
          paint.textSize = 9f
          paint.isFakeBoldText = false
          paint.color = AndroidColor.parseColor(options.theme.accentColorHex)
          canvas.drawText("BCN ID: ${options.bcnId}", left + 20f, currentY, paint)
          currentY += 18f
        }

        paint.textSize = 8.5f
        paint.color = if (options.theme.isDark) AndroidColor.LTGRAY else AndroidColor.DKGRAY
        if (options.includePhone && options.phone.isNotBlank()) {
          canvas.drawText("Tel: ${options.phone}", left + 20f, currentY, paint)
          currentY += 13f
        }
        if (options.includeWhatsApp && options.whatsApp.isNotBlank()) {
          canvas.drawText("WA: ${options.whatsApp}", left + 20f, currentY, paint)
          currentY += 13f
        }
        if (options.includeEmail && options.email.isNotBlank()) {
          canvas.drawText("Email: ${options.email}", left + 20f, currentY, paint)
          currentY += 13f
        }
        if (options.includeWebsite && options.website.isNotBlank()) {
          canvas.drawText("Web: ${options.website}", left + 20f, currentY, paint)
        }
      } else {
        // Back View
        var currentY = top + 36f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.color = AndroidColor.parseColor(options.theme.accentColorHex)
        canvas.drawText("BUSYCARDNET SMART IDENTITY", left + 20f, currentY, paint)
        currentY += 20f

        if (options.includeBarcode && options.bcnId.isNotBlank()) {
          paint.textSize = 9f
          paint.isFakeBoldText = false
          paint.color = if (options.theme.isDark) AndroidColor.LTGRAY else AndroidColor.GRAY
          canvas.drawText("[ BARCODE / QR SCAN CODE ]", left + 20f, currentY, paint)
          currentY += 14f
          paint.textSize = 10f
          paint.color = if (options.theme.isDark) AndroidColor.WHITE else AndroidColor.BLACK
          canvas.drawText("|||| | ||||| || |||||| | || ${options.bcnId} ||", left + 20f, currentY, paint)
          currentY += 26f
        }

        paint.textSize = 8.5f
        paint.color = if (options.theme.isDark) AndroidColor.LTGRAY else AndroidColor.DKGRAY
        if (options.includeInstagram && options.instagram.isNotBlank()) {
          canvas.drawText("Instagram: ${options.instagram}", left + 20f, currentY, paint)
          currentY += 14f
        }
        if (options.includeFacebook && options.facebook.isNotBlank()) {
          canvas.drawText("Facebook: ${options.facebook}", left + 20f, currentY, paint)
          currentY += 14f
        }
        if (options.includeAddress && options.address.isNotBlank()) {
          canvas.drawText("Address: ${options.address.take(50)}", left + 20f, currentY, paint)
        }
      }

      // 3. Print Cut Marks (corner guides for trimming)
      paint.color = AndroidColor.GRAY
      paint.strokeWidth = 0.5f
      paint.style = Paint.Style.STROKE
      // Top-Left crop marks
      canvas.drawLine(left - 15f, top, left - 4f, top, paint)
      canvas.drawLine(left, top - 15f, left, top - 4f, paint)
      // Top-Right crop marks
      canvas.drawLine(cardRect.right + 4f, top, cardRect.right + 15f, top, paint)
      canvas.drawLine(cardRect.right, top - 15f, cardRect.right, top - 4f, paint)
      // Bottom-Left crop marks
      canvas.drawLine(left - 15f, cardRect.bottom, left - 4f, cardRect.bottom, paint)
      canvas.drawLine(left, cardRect.bottom + 4f, left, cardRect.bottom + 15f, paint)
      // Bottom-Right crop marks
      canvas.drawLine(cardRect.right + 4f, cardRect.bottom, cardRect.right + 15f, cardRect.bottom, paint)
      canvas.drawLine(cardRect.right, cardRect.bottom + 4f, cardRect.right, cardRect.bottom + 15f, paint)

      // Footer note
      paint.style = Paint.Style.FILL
      paint.textSize = 8f
      paint.color = AndroidColor.LTGRAY
      val sideLabel = if (isFront) "FRONT SIDE (Trim at crop marks - 3.5\" x 2.0\")" else "BACK SIDE (Trim at crop marks - 3.5\" x 2.0\")"
      canvas.drawText(sideLabel, left, cardRect.bottom + 25f, paint)
    }
  }
}
