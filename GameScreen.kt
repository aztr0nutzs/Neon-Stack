package com.neonstack.arcade

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

private val NeonCyan = Color(0xFF00F0FF)
private val NeonMagenta = Color(0xFFFF0099)
private val NeonPurple = Color(0xFFBD00FF)
private val DeepVoid = Color(0xFF050510)
private val DarkMetal = Color(0xFF12121A)
private val BrushedSteel = Color(0xFF1A1A2E)

private const val columns = 7
private const val rows = 6

/**
 * High-fidelity arcade-inspired Connect 4 surface. The Column order is fixed:
 * MarqueeHeader -> GameBoard -> ControlDeck.
 */
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    board: List<List<PlayerChip>> = List(rows) { List(columns) { PlayerChip.None } },
    onColumnTap: (Int) -> Unit = {}
) {
    Box(
        modifier
            .fillMaxSize()
            .background(DeepVoid)
    ) {
        ArcadeInfiniteHallwayBackground()
        Column(Modifier.fillMaxSize()) {
            MarqueeHeader(modifier = Modifier.fillMaxWidth().weight(0.2f))
            GameBoard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f),
                board = board,
                onColumnTap = onColumnTap
            )
            ControlDeck(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                currentPlayer = currentTurn(board)
            )
        }
    }
}

private fun currentTurn(board: List<List<PlayerChip>>): PlayerChip {
    val flat = board.flatten()
    val p1 = flat.count { it == PlayerChip.Player1 }
    val p2 = flat.count { it == PlayerChip.Player2 }
    return if (p1 <= p2) PlayerChip.Player1 else PlayerChip.Player2
}

@Composable
private fun ArcadeInfiniteHallwayBackground() {
    val transition = rememberInfiniteTransition(label = "hallway")
    val lineOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridScroll"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val vanishingPoint = Offset(centerX, centerY)
        val depthLines = 14
        val spacing = size.height / (depthLines * 1.5f)
        val color = NeonPurple.copy(alpha = 0.45f)

        // Draw vertical perspective grid lines
        for (i in -depthLines..depthLines step 2) {
            val factor = i / depthLines.toFloat()
            val start = Offset(centerX + factor * size.width, 0f)
            val end = Offset(centerX + factor * size.width * 0.3f, size.height)
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = max(1f, 3f * (1f - kotlin.math.abs(factor))),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f), phase = lineOffset * 20f)
            )
        }

        // Animate horizontal grid bands moving outward to fake forward motion.
        val animatedSpacing = spacing * (1f + lineOffset)
        var y = centerY
        while (y < size.height + spacing) {
            val alpha = 1f - (y - centerY) / size.height
            drawLine(
                color = color.copy(alpha = 0.6f * alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3f
            )
            drawLine(
                color = color.copy(alpha = 0.6f * alpha),
                start = Offset(0f, centerY - (y - centerY)),
                end = Offset(size.width, centerY - (y - centerY)),
                strokeWidth = 3f
            )
            y += animatedSpacing
        }

        // Central neon bloom
        drawCircle(
            color = color.copy(alpha = 0.28f),
            radius = min(size.width, size.height) / 5f,
            center = vanishingPoint,
            blendMode = BlendMode.Screen
        )
    }
}

@Composable
private fun MarqueeHeader(modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .neonGlow(NeonPurple)
                .clip(RoundedCornerShape(18.dp))
                .background(DarkMetal.copy(alpha = 0.92f))
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(NeonPurple.copy(alpha = 0.35f), Color.Transparent, NeonPurple.copy(alpha = 0.35f)),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                    )
                }
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CONNECT",
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            ),
                            modifier = Modifier.neonGlow(NeonPurple)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                                .neonGlow(NeonCyan)
                                .align(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "4",
                                color = Color(0xFF00181F),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        }
                    }
                    Text(
                        text = "NEON TOURNAMENT EDITION",
                        color = Color.White.copy(alpha = 0.74f),
                        fontSize = 12.sp,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Row {
                    TicketBadge(text = "TICKET WINNER")
                    Spacer(modifier = Modifier.width(8.dp))
                    TicketBadge(text = "BONUS")
                }
            }
        }
    }
}

@Composable
private fun TicketBadge(text: String) {
    Box(
        modifier = Modifier
            .graphicsLayer(rotationZ = -10f)
            .neonGlow(Color(0xFFFF66CC))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x33FF66CC))
            .borderStroke(1.5.dp, Color(0xFFFF66CC))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun GameBoard(
    modifier: Modifier = Modifier,
    board: List<List<PlayerChip>>,
    onColumnTap: (Int) -> Unit
) {
    val reflection = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
        start = Offset.Zero,
        end = Offset(0f, 320f)
    )

    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .neonGlow(NeonPurple.copy(alpha = 0.65f))
            .clip(RoundedCornerShape(22.dp))
            .background(BrushedSteel)
            .innerMetalShadow(cornerRadius = 22.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val slotWidth = size.width / columns
                    val tappedColumn = (offset.x / slotWidth).toInt().coerceIn(0, columns - 1)
                    onColumnTap(tappedColumn)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val slotSize = Size(size.width / columns, size.height / rows)
            val radius = min(slotSize.width, slotSize.height) / 2.3f

            // Board base with gradient
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(DarkMetal, BrushedSteel, DarkMetal.copy(alpha = 0.9f)),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx(), 22.dp.toPx())
            )

            drawIntoCanvas { canvas ->
                val layer = canvas.saveLayer(Rect(Offset.Zero, size), paint)
                // Punch holes
                for (row in 0 until rows) {
                    for (col in 0 until columns) {
                        val center = Offset(
                            x = col * slotSize.width + slotSize.width / 2f,
                            y = row * slotSize.height + slotSize.height / 2f
                        )
                        val chip = board.getOrNull(row)?.getOrNull(col) ?: PlayerChip.None
                        val glowColor = when (chip) {
                            PlayerChip.Player1 -> NeonCyan
                            PlayerChip.Player2 -> NeonMagenta
                            else -> Color.Transparent
                        }
                        drawCircle(
                            color = Color.Black,
                            radius = radius,
                            center = center,
                            blendMode = BlendMode.Clear
                        )
                        // Inner shadow for depth
                        drawHoleDepth(center, radius, glowColor)

                        if (chip != PlayerChip.None) {
                            drawChip(center, radius * 0.95f, glowColor)
                        }
                    }
                }
                canvas.restoreToCount(layer)
            }

            // Front glass reflection
            drawRoundRect(
                brush = reflection,
                size = Size(width = size.width, height = size.height / 1.8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx(), 22.dp.toPx())
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHoleDepth(
    center: Offset,
    radius: Float,
    glowColor: Color
) {
    val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
        this.color = Color.Black.copy(alpha = 0.8f)
        this.isAntiAlias = true
        this.asFrameworkPaint().apply {
            isDither = true
            maskFilter = android.graphics.BlurMaskFilter(radius / 3f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    }
    drawIntoCanvas {
        it.save()
        it.nativeCanvas.drawCircle(center.x, center.y + radius / 4f, radius, shadowPaint.asFrameworkPaint())
        it.restore()
    }
    if (glowColor != Color.Transparent) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = 0.6f), Color.Transparent),
                center = center,
                radius = radius * 1.4f
            ),
            radius = radius * 1.4f,
            center = center
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChip(
    center: Offset,
    radius: Float,
    color: Color
) {
    val light = color.copy(alpha = 0.9f)
    val dark = color.copy(alpha = 0.5f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(light, dark),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    // Rim reflection
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent),
            start = center - Offset(radius / 2f, radius / 2f),
            end = center + Offset(radius / 2f, radius / 2f)
        ),
        radius = radius * 0.85f,
        center = center
    )
    // Edge neon glow
    drawCircle(
        color = color.copy(alpha = 0.55f),
        radius = radius * 1.2f,
        center = center,
        blendMode = BlendMode.Screen
    )
}

@Composable
private fun ControlDeck(modifier: Modifier = Modifier, currentPlayer: PlayerChip) {
    val indicatorColor by remember(currentPlayer) {
        mutableStateOf(if (currentPlayer == PlayerChip.Player1) NeonCyan else NeonMagenta)
    }
    val animatedIndicator by animateColor(indicatorColor)

    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .neonGlow(animatedIndicator.copy(alpha = 0.5f))
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.15f)
                    lineTo(size.width * 0.1f, 0f)
                    lineTo(size.width * 0.9f, 0f)
                    lineTo(size.width, size.height * 0.15f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        listOf(DarkMetal, BrushedSteel, DarkMetal)
                    )
                )
            }
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerStation(
                title = "PLAYER 1",
                color = NeonCyan,
                alignment = Alignment.Start,
                score = boardScorePlaceholder(currentPlayer, PlayerChip.Player1),
                isActive = currentPlayer == PlayerChip.Player1
            )
            RestartButton()
            PlayerStation(
                title = "PLAYER 2",
                color = NeonMagenta,
                alignment = Alignment.End,
                score = boardScorePlaceholder(currentPlayer, PlayerChip.Player2),
                isActive = currentPlayer == PlayerChip.Player2
            )
        }
    }
}

private fun boardScorePlaceholder(current: PlayerChip, player: PlayerChip): Int {
    // Replace with real score. Using active player bias to avoid mock randomness.
    return if (current == player) 1 else 0
}

@Composable
private fun PlayerStation(
    title: String,
    color: Color,
    alignment: Alignment,
    score: Int,
    isActive: Boolean
) {
    val glowColor by animateColor(color.copy(alpha = if (isActive) 0.8f else 0.3f))
    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.neonGlow(glowColor)
        )
        Box(
            modifier = Modifier
                .size(width = 140.dp, height = 74.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkMetal)
                .neonGlow(glowColor)
                .innerMetalShadow(cornerRadius = 12.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = alignment
            ) {
                Text(
                    text = "SCORE",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = score.toString().padStart(2, '0'),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                StatusLight(isActive = isActive, color = color)
            }
        }
    }
}

@Composable
private fun StatusLight(isActive: Boolean, color: Color) {
    val animatedColor by animateColor(if (isActive) color else Color(0xFF303040))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(animatedColor)
                .neonGlow(animatedColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isActive) "YOUR MOVE" else "IDLE",
            color = Color.White.copy(alpha = if (isActive) 0.9f else 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun RestartButton(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(Color(0xFF1F2B10))
            .neonGlow(Color(0xFFB4FF35))
            .clickable(onClick = onClick)
            .innerMetalShadow(cornerRadius = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFB4FF35), Color(0xFF6FB400))
                    )
                )
                .neonGlow(Color(0xFFB4FF35))
                .drawBehind {
                    drawCircle(
                        brush = Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                            start = Offset.Zero,
                            end = Offset(size.width / 2f, size.height / 2f)
                        ),
                        radius = size.minDimension / 2f
                    )
                }
        ) {
            Text(
                text = "RESTART",
                color = Color(0xFF0C1404),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun Modifier.innerMetalShadow(cornerRadius: Dp) = this.then(
    Modifier.drawBehind {
        val paint = androidx.compose.ui.graphics.Paint().apply {
            color = Color.Black.copy(alpha = 0.4f)
            this.asFrameworkPaint().apply {
                isAntiAlias = true
                maskFilter = android.graphics.BlurMaskFilter(18f, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
        }
        drawIntoCanvas {
            it.save()
            it.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                cornerRadius.toPx(),
                cornerRadius.toPx(),
                paint.asFrameworkPaint()
            )
            it.restore()
        }
    }
)

private fun Modifier.neonGlow(color: Color, radius: Dp = 22.dp): Modifier = this.then(
    Modifier.drawBehind {
        val frameworkPaint = android.graphics.Paint().apply {
            this.color = color.copy(alpha = 0.9f).toArgb()
            isAntiAlias = true
            maskFilter = android.graphics.BlurMaskFilter(radius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        drawIntoCanvas {
            it.save()
            it.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                radius.toPx(),
                radius.toPx(),
                frameworkPaint
            )
            it.restore()
        }
    }
)

private fun Modifier.borderStroke(width: Dp, color: Color) = this.then(
    Modifier.drawBehind {
        val strokeWidth = width.toPx()
        drawRoundRect(
            color = color,
            size = Size(size.width, size.height),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
    }
)

private fun animateColor(target: Color) = androidx.compose.animation.animateColorAsState(
    targetValue = target,
    animationSpec = androidx.compose.animation.core.tween(durationMillis = 420, easing = LinearEasing),
    label = "color"
)

enum class PlayerChip {
    None, Player1, Player2
}
