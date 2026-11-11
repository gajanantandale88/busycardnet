package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ProfileAnalyticsSummary

/**
 * Android WebView hosting Recharts library for rich business profile views
 * and ad click-through rate interactive visualizations.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebView(
  summary: ProfileAnalyticsSummary,
  modifier: Modifier = Modifier
) {
  val jsonPayload = remember(summary) { summary.toJsonString() }

  AndroidView(
    modifier = modifier
      .fillMaxWidth()
      .height(490.dp)
      .clip(RoundedCornerShape(14.dp))
      .testTag("recharts_webview_container"),
    factory = { ctx ->
      WebView(ctx).apply {
        setBackgroundColor(AndroidColor.parseColor("#0B1120"))
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          loadWithOverviewMode = true
          useWideViewPort = true
          builtInZoomControls = false
          displayZoomControls = false
          cacheMode = WebSettings.LOAD_DEFAULT
        }
        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
          override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val escapedJson = jsonPayload.replace("'", "\\'")
            view?.evaluateJavascript("window.updateDashboardData('$escapedJson');", null)
          }
        }
        loadUrl("file:///android_asset/recharts_dashboard.html")
      }
    },
    update = { webView ->
      val escapedJson = jsonPayload.replace("'", "\\'")
      webView.evaluateJavascript("window.updateDashboardData('$escapedJson');", null)
    }
  )
}
