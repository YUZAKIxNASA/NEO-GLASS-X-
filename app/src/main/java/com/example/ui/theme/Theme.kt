package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonWaveColorScheme = darkColorScheme(
  primary = VioletGaze,
  onPrimary = TextPrimary,
  primaryContainer = IndigoAura,
  onPrimaryContainer = TextPrimary,
  secondary = CyanHighlight,
  onSecondary = AtmosphereDeep,
  secondaryContainer = RoyalBlue,
  onSecondaryContainer = TextPrimary,
  tertiary = LavenderGlass,
  onTertiary = AtmosphereDeep,
  background = AtmosphereDeep,
  onBackground = TextPrimary,
  surface = AtmosphereMid,
  onSurface = TextPrimary,
  surfaceVariant = GlassSurfaceDark,
  onSurfaceVariant = TextSecondary,
  outline = GlassBorderLight,
  outlineVariant = GlassBorderSubtle,
)

@Composable
fun NeonWaveTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NeonWaveColorScheme,
    typography = Typography,
    content = content
  )
}

// Alias for backwards compatibility
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  NeonWaveTheme(content = content)
}
