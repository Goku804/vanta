package com.vanta.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Small "now playing" indicator: three bars pulsing at slightly different
 * phases, standing in for real audio-reactive amplitude until Phase 9 wires
 * an actual audio visualizer into the floating widget / player. Deliberately
 * tiny and unobtrusive — this marks state, it doesn't compete for attention
 * (see spec section 11: identifiable without being noisy).
 */
@Composable
fun EqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barHeight: Dp = 14.dp
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val bar0 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eqBar0"
    )
    val bar1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(560, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eqBar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eqBar2"
    )

    val fractions = listOf(bar0, bar1, bar2)

    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        fractions.forEachIndexed { index, value ->
            val fraction = if (isPlaying) value else 0.22f
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier
                    .padding(start = if (index == 0) 0.dp else 2.dp)
                    .width(3.dp)
                    .height(barHeight * fraction)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
        }
    }
}
