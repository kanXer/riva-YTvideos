package com.nexusdigital.rivavideo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * YouTube-inspired theme (dark/light).
 * Dark: #0F0F0F pure dark + YouTube Red accent + #272727 pill buttons.
 */
private val DarkInk = Color(0xFF0F0F0F)
private val DarkPanel = Color(0xFF1F1F1F)
private val DarkPanelMid = Color(0xFF272727)
private val DarkAccent = Color(0xFFFF0000)
private val DarkText = Color(0xFFF1F1F1)
private val DarkTextDim = Color(0xFFAAAAAA)

private val LightInk = Color(0xFF0F0F0F) // text
private val LightPanel = Color(0xFFFFFFFF)
private val LightBg = Color(0xFFF9F9F9)
private val LightAccent = Color(0xFFCC0000)
private val LightTextDim = Color(0xFF606060)

object RivaL {
    fun bg(dark: Boolean) = if (dark) DarkInk else LightBg
    fun panel(dark: Boolean) = if (dark) DarkPanel else LightPanel
    fun panelMid(dark: Boolean) = if (dark) DarkPanelMid else Color(0xFFF2F2F2)
    fun accent(dark: Boolean) = if (dark) DarkAccent else LightAccent
    fun text(dark: Boolean) = if (dark) DarkText else LightInk
    fun textDim(dark: Boolean) = if (dark) DarkTextDim else LightTextDim
    fun pill(dark: Boolean) = if (dark) Color(0xFF272727) else Color(0xFFE5E5E5)
    fun red(dark: Boolean) = Color(0xFFFF0000)
}

@Composable
fun RivaVideoTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) {
        darkColorScheme(
            primary = DarkAccent,
            onPrimary = Color.White,
            background = DarkInk,
            onBackground = DarkText,
            surface = DarkPanel,
            onSurface = DarkText,
            surfaceVariant = DarkPanelMid,
            onSurfaceVariant = DarkTextDim,
            outline = DarkTextDim.copy(alpha = 0.4f),
        )
    } else {
        lightColorScheme(
            primary = LightAccent,
            onPrimary = Color.White,
            background = LightBg,
            onBackground = LightInk,
            surface = LightPanel,
            onSurface = LightInk,
            surfaceVariant = LightBg,
            onSurfaceVariant = LightTextDim,
            outline = LightTextDim.copy(alpha = 0.4f),
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
