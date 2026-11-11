package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Executive Light Theme Palette
// Balanced high-contrast scheme with crisp readable typography and elegant depth
private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF1E40AF),             // Refined Executive Royal Sapphire
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),    // Clean ice-sapphire container
    onPrimaryContainer = Color(0xFF1E3A8A),  // Deep sapphire text
    secondary = Color(0xFF0284C7),           // Ocean azure for energetic accents
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),  // Soft azure container
    onSecondaryContainer = Color(0xFF0369A1),// Deep azure text
    tertiary = Color(0xFFD97706),            // Luxury Amber Bronze
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),          // Premium slate-pearl background
    onBackground = Color(0xFF0F172A),        // Obsidian black-slate text (100% readable)
    surface = Color(0xFFFFFFFF),             // Pure crisp luxury surface
    onSurface = Color(0xFF0F172A),           // Obsidian black-slate text on surface
    surfaceVariant = Color(0xFFE2E8F0),      // Slate-tinted card and tab container
    onSurfaceVariant = Color(0xFF334155),    // Crisp charcoal text for unselected tabs & subtle labels
    outline = Color(0xFFCBD5E1),             // Refined slate outline
    outlineVariant = Color(0xFFE2E8F0)
  )

@Composable
fun BusyCardNetTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
