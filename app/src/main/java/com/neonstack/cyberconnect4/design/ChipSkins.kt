package com.neonstack.cyberconnect4.design

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.android.InternalPlatformTextApi

/**
 * Describes a playable character with theme-driven visuals that can be reused throughout the UI.
 */
data class CharacterProfile(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val accentColor: Color,
    val avatar: Painter,
    val backgroundEffect: Brush,
)

/**
 * Enumerates the available chip renderings.
 */
enum class ChipSkinType { PLASMA, BIOHAZARD, CIRCUIT, GLITCH }

/**
 * Encapsulates the drawing logic for a chip skin. The [renderLogic] is invoked from [ChipRenderer]
 * inside a Canvas draw scope and must only rely on the provided [Color].
 */
data class ChipSkin(
    val type: ChipSkinType,
    val renderLogic: DrawScope.(Color) -> Unit,
) {
    companion object {
        /** Creates a plasma-inspired glowing core. */
        fun plasma() = ChipSkin(ChipSkinType.PLASMA) { accent -> drawPlasmaCore(accent) }

        /** Creates a biohazard glyph centered on the chip. */
        fun biohazard() = ChipSkin(ChipSkinType.BIOHAZARD) { accent -> drawBiohazardSymbol(accent) }

        /** Creates a circuit board style with etched traces. */
        fun circuit() = ChipSkin(ChipSkinType.CIRCUIT) { accent -> drawCircuitTrace(accent) }

        /** Creates a data-bit overlay with binary glyphs and glitch offsets. */
        fun glitch() = ChipSkin(ChipSkinType.GLITCH) { accent -> drawDataBitOverlay(accent) }
    }
}

/**
 * Renders a composable chip using its skin. The renderer draws a layered base to avoid a flat
 * appearance before delegating to the skin-specific pattern.
 */
@Composable
fun ChipRenderer(
    skin: ChipSkin,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.95f),
                    color.copy(alpha = 0.35f),
                    color.copy(alpha = 0.08f),
                ),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )

        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(color.copy(alpha = 0.7f), color.copy(alpha = 0.25f)),
            ),
            radius = radius * 0.82f,
            center = center,
            style = Stroke(width = radius * 0.12f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.18f)),
                center = center,
                radius = radius * 0.9f,
            ),
            radius = radius * 0.75f,
            center = center,
        )

        with(skin) { renderLogic(color) }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0f)),
                center = center - Offset(0f, radius * 0.18f),
                radius = radius * 0.45f,
            ),
            radius = radius * 0.45f,
            center = center - Offset(0f, radius * 0.18f),
        )
    }
}

private fun DrawScope.drawPlasmaCore(accent: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.8f), accent.copy(alpha = 0.1f)),
            center = center,
            radius = radius * 0.65f,
        ),
        radius = radius * 0.65f,
        center = center,
    )

    drawCircle(
        color = accent.copy(alpha = 0.85f),
        radius = radius * 0.22f,
        center = center,
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.5f), accent.copy(alpha = 0f)),
            center = center,
            radius = radius * 0.4f,
        ),
        radius = radius * 0.4f,
        center = center,
    )
}

private fun DrawScope.drawBiohazardSymbol(accent: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = size.minDimension / 2f
    val leafRadius = outerRadius * 0.35f

    repeat(3) { index ->
        rotate(degrees = index * 120f, pivot = center) {
            val leafCenter = center + Offset(0f, -leafRadius * 0.9f)
            val path = Path().apply {
                moveTo(leafCenter.x, leafCenter.y - leafRadius * 0.5f)
                quadraticBezierTo(
                    leafCenter.x + leafRadius * 0.65f,
                    leafCenter.y - leafRadius * 0.05f,
                    leafCenter.x,
                    leafCenter.y + leafRadius * 0.8f,
                )
                quadraticBezierTo(
                    leafCenter.x - leafRadius * 0.65f,
                    leafCenter.y - leafRadius * 0.05f,
                    leafCenter.x,
                    leafCenter.y - leafRadius * 0.5f,
                )
                close()
            }
            drawPath(
                path = path,
                color = accent.copy(alpha = 0.9f),
            )
            drawPath(
                path = path,
                color = accent.copy(alpha = 0.4f),
                style = Stroke(width = leafRadius * 0.16f),
            )
        }
    }

    drawCircle(
        color = accent.copy(alpha = 0.75f),
        radius = outerRadius * 0.18f,
        center = center,
    )

    drawCircle(
        color = accent.copy(alpha = 0.2f),
        radius = outerRadius * 0.35f,
        center = center,
        style = Stroke(width = outerRadius * 0.08f),
    )
}

private fun DrawScope.drawCircuitTrace(accent: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f
    val traceColor = accent.copy(alpha = 0.9f)
    val glowColor = accent.copy(alpha = 0.25f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor, accent.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )

    val nodeOffsets = listOf(
        Offset(center.x - radius * 0.6f, center.y - radius * 0.1f),
        Offset(center.x + radius * 0.55f, center.y - radius * 0.2f),
        Offset(center.x - radius * 0.15f, center.y + radius * 0.55f),
        Offset(center.x + radius * 0.2f, center.y - radius * 0.6f),
    )

    nodeOffsets.forEach { target ->
        drawLine(
            color = traceColor,
            start = center,
            end = target,
            strokeWidth = radius * 0.08f,
        )
        drawCircle(
            color = accent.copy(alpha = 0.65f),
            radius = radius * 0.12f,
            center = target,
        )
        drawCircle(
            color = accent.copy(alpha = 0.35f),
            radius = radius * 0.17f,
            center = target,
            style = Stroke(width = radius * 0.06f),
        )
    }
}

@OptIn(InternalPlatformTextApi::class)
private fun DrawScope.drawDataBitOverlay(accent: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f
    val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = accent.copy(alpha = 0.5f).toArgb()
        textSize = radius * 0.55f
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.MONOSPACE
    }

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawText("1010", center.x, center.y + radius * 0.18f, textPaint)
        canvas.nativeCanvas.drawText(
            "1010",
            center.x + radius * 0.05f,
            center.y - radius * 0.18f,
            textPaint.apply { alpha = (accent.alpha * 155).toInt() },
        )
    }

    val glitchBars = listOf(-0.45f, -0.1f, 0.28f)
    glitchBars.forEachIndexed { index, offsetY ->
        val barHeight = radius * 0.12f
        val barWidth = size.width * (0.4f + index * 0.12f)
        drawRect(
            color = accent.copy(alpha = 0.16f + 0.08f * index),
            topLeft = Offset((size.width - barWidth) / 2f, center.y + offsetY * radius),
            size = Size(barWidth, barHeight),
        )
    }
}

private fun Color.toArgb(): Int =
    android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt(),
    )
