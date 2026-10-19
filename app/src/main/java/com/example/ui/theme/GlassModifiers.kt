package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Full viewport background matching the Aurora Glass atmosphere from the reference image.
 * Uses a deep navy foundation layered with royal blue and indigo/violet radial ambient light cones.
 */
fun Modifier.auroraBackground(): Modifier = this.drawBehind {
  val width = size.width
  val height = size.height

  // Base background
  drawRect(color = AtmosphereDeep)

  // Top-right violet/indigo ambient light burst
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        IndigoAura.copy(alpha = 0.55f),
        RoyalBlue.copy(alpha = 0.30f),
        Color.Transparent
      ),
      center = Offset(width * 0.85f, height * 0.15f),
      radius = width * 0.85f
    )
  )

  // Mid-left cyan accent light
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        CyanHighlight.copy(alpha = 0.18f),
        RoyalBlue.copy(alpha = 0.15f),
        Color.Transparent
      ),
      center = Offset(width * 0.05f, height * 0.50f),
      radius = width * 0.65f
    )
  )

  // Bottom-center violet glow
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        VioletGaze.copy(alpha = 0.35f),
        AtmosphereMid.copy(alpha = 0.20f),
        Color.Transparent
      ),
      center = Offset(width * 0.50f, height * 0.90f),
      radius = width * 0.75f
    )
  )
}

/**
 * Aurora frosted glass panel modifier with subtle border gradient and translucent surface.
 */
fun Modifier.glassPanel(
  shape: Shape = RoundedCornerShape(24.dp),
  backgroundColor: Color = GlassSurfaceDark,
  borderColor: Color = GlassBorderLight,
  borderWidth: Dp = 1.dp
): Modifier = this
  .clip(shape)
  .background(
    brush = Brush.verticalGradient(
      colors = listOf(
        GlassSurfaceLight.copy(alpha = 0.45f),
        backgroundColor
      )
    ),
    shape = shape
  )
  .border(
    width = borderWidth,
    brush = Brush.verticalGradient(
      colors = listOf(
        borderColor,
        GlassBorderSubtle
      )
    ),
    shape = shape
  )

/**
 * Elevated glass card for prominent items like Now Playing hero card.
 */
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(24.dp),
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val clickModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = ripple(color = LavenderGlass),
      onClick = onClick
    )
  } else Modifier

  Box(
    modifier = modifier
      .glassPanel(shape = shape)
      .then(clickModifier),
    content = content
  )
}

/**
 * Animated audio visualizer equalizer bars in cyan and violet.
 */
@Composable
fun VisualizerBars(
  isPlaying: Boolean,
  modifier: Modifier = Modifier,
  barCount: Int = 5,
  barWidth: Dp = 3.dp,
  maxHeight: Dp = 24.dp,
  minHeight: Dp = 6.dp,
  color: Color = CyanHighlight
) {
  val transition = rememberInfiniteTransition(label = "equalizer")

  val anim1 by transition.animateFloat(
    initialValue = 0.3f, targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar1"
  )
  val anim2 by transition.animateFloat(
    initialValue = 0.8f, targetValue = 0.2f,
    animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar2"
  )
  val anim3 by transition.animateFloat(
    initialValue = 0.2f, targetValue = 0.9f,
    animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar3"
  )
  val anim4 by transition.animateFloat(
    initialValue = 0.9f, targetValue = 0.4f,
    animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar4"
  )
  val anim5 by transition.animateFloat(
    initialValue = 0.4f, targetValue = 0.85f,
    animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "bar5"
  )

  val heights = listOf(anim1, anim2, anim3, anim4, anim5)

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until barCount) {
      val factor = if (isPlaying) heights[i % heights.size] else 0.25f
      val barHeight = minHeight + (maxHeight - minHeight) * factor
      Box(
        modifier = Modifier
          .width(barWidth)
          .height(barHeight)
          .clip(CircleShape)
          .background(
            Brush.verticalGradient(
              listOf(color, VioletGaze)
            )
          )
      )
    }
  }
}
