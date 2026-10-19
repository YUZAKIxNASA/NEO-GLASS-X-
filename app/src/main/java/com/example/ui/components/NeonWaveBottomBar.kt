package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtmosphereDeep
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.IndigoAura
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletGaze
import com.example.ui.viewmodel.AppTab

@Composable
fun NeoGlassBottomBar(
  currentTab: AppTab,
  onTabSelected: (AppTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // Floating glass navigation bar container
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .clip(RoundedCornerShape(32.dp))
        .background(
          brush = Brush.verticalGradient(
            listOf(
              Color(0xCC1A265B),
              AtmosphereDeep.copy(alpha = 0.92f)
            )
          )
        )
        .border(
          width = 1.dp,
          brush = Brush.verticalGradient(
            listOf(
              GlassBorderLight,
              GlassBorderSubtle
            )
          ),
          shape = RoundedCornerShape(32.dp)
        )
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavItem(
        label = "Home",
        selected = currentTab == AppTab.HOME,
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home,
        testTag = "nav_home",
        onClick = { onTabSelected(AppTab.HOME) }
      )

      NavItem(
        label = "Downloader",
        selected = currentTab == AppTab.DOWNLOADER,
        activeIcon = Icons.Filled.Download,
        inactiveIcon = Icons.Outlined.Download,
        testTag = "nav_downloader",
        onClick = { onTabSelected(AppTab.DOWNLOADER) }
      )

      NavItem(
        label = "Library",
        selected = currentTab == AppTab.LIBRARY,
        activeIcon = Icons.Filled.LibraryMusic,
        inactiveIcon = Icons.Outlined.LibraryMusic,
        testTag = "nav_library",
        onClick = { onTabSelected(AppTab.LIBRARY) }
      )
    }
  }
}

@Composable
private fun NavItem(
  label: String,
  selected: Boolean,
  activeIcon: ImageVector,
  inactiveIcon: ImageVector,
  testTag: String,
  onClick: () -> Unit
) {
  val iconColor by animateColorAsState(
    targetValue = if (selected) TextPrimary else TextMuted,
    animationSpec = tween(250),
    label = "nav_icon_color"
  )

  if (selected) {
    // Active glowing pill indicator matching Reference Image
    Row(
      modifier = Modifier
        .clip(CircleShape)
        .background(
          brush = Brush.horizontalGradient(
            listOf(
              RoyalBlue,
              VioletGaze
            )
          )
        )
        .border(
          width = 1.dp,
          color = CyanHighlight.copy(alpha = 0.40f),
          shape = CircleShape
        )
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(color = CyanHighlight),
          onClick = onClick
        )
        .padding(horizontal = 18.dp, vertical = 10.dp)
        .testTag(testTag),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = activeIcon,
        contentDescription = label,
        tint = TextPrimary,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = label,
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  } else {
    // Inactive tab item
    Box(
      modifier = Modifier
        .clip(CircleShape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(color = VioletGaze),
          onClick = onClick
        )
        .padding(horizontal = 16.dp, vertical = 10.dp)
        .testTag(testTag),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = inactiveIcon,
          contentDescription = label,
          tint = iconColor,
          modifier = Modifier.size(22.dp)
        )
        Text(
          text = label,
          color = iconColor,
          fontSize = 13.sp,
          fontWeight = FontWeight.Normal
        )
      }
    }
  }
}

@Composable
fun NeonWaveBottomBar(
  currentTab: AppTab,
  onTabSelected: (AppTab) -> Unit,
  modifier: Modifier = Modifier
) {
  NeoGlassBottomBar(currentTab = currentTab, onTabSelected = onTabSelected, modifier = modifier)
}
