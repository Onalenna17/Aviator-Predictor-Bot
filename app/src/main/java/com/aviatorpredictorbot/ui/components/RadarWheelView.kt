package com.aviatorpredictorbot.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.PredictedNextRound
import com.aviatorpredictorbot.ui.theme.AccentGold
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.CrimsonPrimary
import com.aviatorpredictorbot.ui.theme.DeepDarkBackground
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.PinkMultiplierGlow
import com.aviatorpredictorbot.ui.theme.PurpleMultiplier
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The Big Wheel is strictly dedicated to displaying ONLY the predicted real next round
 * target multiplier, confidence metrics, and upcoming round ID.
 */
@Composable
fun RadarWheelView(
    predictedNext: PredictedNextRound?,
    isPinkHighlightActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    // Radar sweep rotation
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    // Breathing glow animation
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    val isPinkPredicted = (predictedNext?.predictedMultiplier ?: 0.0) >= 10.0 || isPinkHighlightActive
    val activeGlowColor = if (isPinkPredicted) PinkMultiplier else CrimsonPrimary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("radar_wheel_arena"),
        contentAlignment = Alignment.Center
    ) {
        val arenaSize = min(maxWidth.value, 310f).dp

        Box(
            modifier = Modifier
                .size(arenaSize)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pointer Needle at Top (12 o'clock)
            TopPointer(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            )

            // Circular Glowing Radar Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .clip(CircleShape)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Outer Glowing Gradient Rim
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DeepDarkBackground,
                            Color(0xFF26030B),
                            activeGlowColor.copy(alpha = 0.55f * pulseGlow),
                            activeGlowColor
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius
                )

                // High-tech Conic Slices
                val segmentCount = 16
                val step = 360f / segmentCount
                for (i in 0 until segmentCount) {
                    val start = i * step
                    val color = when (i % 4) {
                        0 -> CrimsonPrimary.copy(alpha = 0.8f)
                        1 -> if (isPinkPredicted) PinkMultiplier.copy(alpha = 0.85f) else CrimsonLight.copy(alpha = 0.85f)
                        2 -> Color(0xFF2B040D)
                        else -> Color(0xFF0F0205)
                    }
                    drawArc(
                        color = color,
                        startAngle = start,
                        sweepAngle = step,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                }

                // Concentric Radar Distance Rings
                val rings = listOf(0.92f, 0.78f, 0.65f)
                rings.forEach { factor ->
                    drawCircle(
                        color = if (isPinkPredicted) PinkMultiplier.copy(alpha = 0.5f) else CrimsonLight.copy(alpha = 0.45f),
                        radius = radius * factor,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Rotating Radar Sweep Line
                val sweepRad = Math.toRadians(sweepAngle.toDouble())
                val endX = center.x + radius * 0.92f * cos(sweepRad).toFloat()
                val endY = center.y + radius * 0.92f * sin(sweepRad).toFloat()

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(activeGlowColor.copy(alpha = 0.1f), activeGlowColor)
                    ),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Digital HUD Ticks
                for (deg in 0 until 360 step 30) {
                    val rad = Math.toRadians(deg.toDouble())
                    val tickOuter = radius * 0.95f
                    val tickInner = radius * 0.88f
                    val x1 = center.x + tickOuter * cos(rad).toFloat()
                    val y1 = center.y + tickOuter * sin(rad).toFloat()
                    val x2 = center.x + tickInner * cos(rad).toFloat()
                    val y2 = center.y + tickInner * sin(rad).toFloat()
                    drawLine(
                        color = activeGlowColor.copy(alpha = 0.6f),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // Big Wheel Central HUD Core: Exclusively Displays Predicted Real Next Round
            Box(
                modifier = Modifier
                    .size(arenaSize * 0.68f)
                    .clip(CircleShape)
                    .background(Color(0xFF030102))
                    .border(
                        width = if (isPinkHighlightActive) 4.dp else 3.dp,
                        color = if (isPinkPredicted) PinkMultiplier else CrimsonLight,
                        shape = CircleShape
                    )
                    .shadow(
                        elevation = if (isPinkHighlightActive) 28.dp else 18.dp,
                        shape = CircleShape,
                        spotColor = activeGlowColor
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: PREDICTED NEXT ROUND
                    Text(
                        text = "PREDICTED NEXT",
                        color = if (isPinkPredicted) PinkMultiplierGlow else CrimsonLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.5.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // HUGE PREDICTED NEXT ROUND MULTIPLIER
                    val targetMultStr = if (predictedNext != null) {
                        String.format(Locale.US, "%.2fx", predictedNext.predictedMultiplier)
                    } else {
                        "2.35x"
                    }

                    Text(
                        text = targetMultStr,
                        color = if (isPinkPredicted) PinkMultiplier else CrimsonPrimary,
                        fontSize = if (targetMultStr.length > 6) 24.sp else 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.testTag("core_predicted_multiplier")
                    )

                    // Crown ♛ with Glow
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "♛",
                            color = if (isPinkPredicted) PinkMultiplierGlow else AccentGold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = " ${predictedNext?.nextRoundId ?: "#894105"}",
                            color = CrimsonLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Confidence & Target Zone
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E050B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SAFE: ${predictedNext?.safeCashoutTarget ?: "1.45x"} (${predictedNext?.confidencePercentage ?: 82}%)",
                            color = Color(0xFF35E98B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopPointer(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .width(26.dp)
            .height(24.dp)
    ) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        drawPath(
            path = path,
            color = CrimsonPrimary
        )
    }
}
