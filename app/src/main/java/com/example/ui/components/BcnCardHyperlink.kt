package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Global CompositionLocal providing the handler to open and inspect a Digital Visiting Card
 * for any given BusyCardNet ID.
 */
val LocalBcnCardOpener = staticCompositionLocalOf<(String) -> Unit> {
  { /* Default fallback */ }
}

/**
 * Standardized BCN Card Hyperlink Component.
 * Compulsory on every page under any user name or company name.
 * Clicking this hyperlink immediately displays the user's visiting or business card.
 */
@Composable
fun BcnCardHyperlink(
  bcnId: String,
  modifier: Modifier = Modifier,
  prefix: String = "BCN: ",
  fontSize: TextUnit = 11.sp,
  color: Color = Color(0xFF0284C7),
  fontWeight: FontWeight = FontWeight.Bold,
  underline: Boolean = true,
  showIcon: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  if (bcnId.isBlank()) return
  val globalOpener = LocalBcnCardOpener.current
  val action = onClick ?: { globalOpener(bcnId.trim()) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clickable(onClick = action)
      .testTag("bcn_hyperlink_$bcnId")
  ) {
    if (showIcon) {
      Icon(
        imageVector = Icons.Default.CreditCard,
        contentDescription = "View Visiting Card",
        tint = color,
        modifier = Modifier.size((fontSize.value + 2).dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
    }
    Text(
      text = "$prefix$bcnId",
      fontSize = fontSize,
      fontFamily = FontFamily.Monospace,
      fontWeight = fontWeight,
      color = color,
      textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None
    )
  }
}
