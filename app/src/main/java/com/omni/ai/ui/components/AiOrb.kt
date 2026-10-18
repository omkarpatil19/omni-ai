package com.omni.ai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.omni.ai.ui.theme.OmniCyan
import com.omni.ai.ui.theme.OmniViolet

@Composable
fun AiOrb(active: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orb")

    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (active) 500 else 1800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (active) 3000 else 12000, easing = LinearEasing),
        ),
        label = "spin",
    )

    Canvas(modifier.size(150.dp)) {
        val base = size.minDimension / 2f
        val r = base * pulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    OmniCyan.copy(alpha = 0.55f),
                    OmniViolet.copy(alpha = 0.25f),
                    Color.Transparent,
                ),
                center = center,
                radius = r,
            ),
            radius = r,
            center = center,
        )

        drawCircle(
            color = OmniCyan,
            radius = base * 0.55f * pulse,
            center = center,
            style = Stroke(width = 3.dp.toPx()),
        )

        rotate(degrees = spin, pivot = center) {
            drawCircle(
                color = OmniViolet,
                radius = base * 0.8f,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
