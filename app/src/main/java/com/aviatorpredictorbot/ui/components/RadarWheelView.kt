package com.aviatorpredictorbot.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.R
import com.aviatorpredictorbot.model.PredictedNextRound
import com.aviatorpredictorbot.ui.theme.ChipBlack
import com.aviatorpredictorbot.ui.theme.ChipPink
import com.aviatorpredictorbot.ui.theme.ChipRed
import com.aviatorpredictorbot.ui.theme.ChipRedLight
import com.aviatorpredictorbot.ui.theme.NeonCrimson
import java.util.Locale
import kotlin.math.min

/** Predictions below this multiplier are treated as "no signal" on the wheel. */
private const val WAITING_THRESHOLD = 1.50

/**
 * Casino-chip radar wheel.
 *
 * - The chip turns slowly all the time and makes one extra full turn whenever a new predicted
 *   round (new round ID) comes in.
 * - The red glow pulses; faster while the pink 10x+ threshold is active.
 * - The black core shows ONLY the predicted next round (multiplier, round ID, SAFE target and
 *   confidence %). It never shows the live flight; below 1.50x it shows "Waiting".
 */
@Composable
fun RadarWheelView(
    predictedNext: PredictedNextRound?,
    isPinkHighlightActive: Boolean,
    modifier: Modifier = Modifier
) {
    // Extra full turn each time the predicted round changes.
    val roundSpin = remember { Animatable(0f) }
    val roundKey = predictedNext?.nextRoundId
    LaunchedEffect(roundKey) {
        if (roundKey != null) {
            roundSpin.animateTo(
                targetValue = roundSpin.value + 360f,
                animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
            )
            roundSpin.snapTo(roundSpin.value % 360f)
        }
    }

    val transition = rememberInfiniteTransition(label = "wheel")
    val idleSpin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing)),
        label = "idleSpin"
    )
    val glow by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPinkHighlightActive) 600 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val predictedValue = predictedNext?.predictedMultiplier?.takeIf { it >= WAITING_THRESHOLD }
    val isWaiting = predictedValue == null || predictedNext == null
    val mainText = if (isWaiting) "Waiting" else String.format(Locale.US, "%.2fx", predictedValue)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("radar_wheel_arena"),
        contentAlignment = Alignment.Center
    ) {
        val arenaSize = min(maxWidth.value, 360f).dp

        Box(
            modifier = Modifier.size(arenaSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) { drawGlow(glow) }

            // The real chip artwork from the design, turning slowly
            Image(
                painter = painterResource(R.drawable.chip_wheel),
                contentDescription = null,
                modifier = Modifier
                    .size(arenaSize * 0.915f)
                    .rotate(idleSpin + roundSpin.value)
            )

            Canvas(modifier = Modifier.fillMaxSize()) { drawPointer() }

            // Black core: ONLY the predicted next round (never the live flight)
            Column(
                modifier = Modifier
                    .size(arenaSize * 0.46f)
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isWaiting) "NEXT ROUND" else "PREDICTED",
                    color = NeonCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = mainText,
                    color = NeonCrimson,
                    fontSize = when {
                        isWaiting -> 30.sp
                        mainText.length > 7 -> 34.sp
                        else -> 44.sp
                    },
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.testTag("core_predicted_multiplier")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Canvas(modifier = Modifier.size(width = 24.dp, height = 18.dp)) {
                    drawCrown(NeonCrimson.copy(alpha = 0.55f + 0.45f * glow))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isWaiting || predictedNext == null) "AWAITING SIGNAL" else "ROUND ${predictedNext.nextRoundId}",
                    color = NeonCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )

                if (!isWaiting && predictedNext != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SAFE: ${predictedNext.safeCashoutTarget} (${predictedNext.confidencePercentage}%)",
                        color = NeonCrimson,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.testTag("core_confidence_chip")
                    )
                }
            }
        }
    }
}

// ---- drawing helpers ----

private fun DrawScope.drawGlow(pulse: Float) {
    val r = size.minDimension / 2f
    drawCircle(
        brush = Brush.radialGradient(
                0.0f to Color.Transparent,
                0.70f to ChipRed.copy(alpha = 0.55f * pulse),
                0.86f to ChipRed.copy(alpha = 0.22f * pulse),
                1.0f to Color.Transparent,
            center = center,
            radius = r
        ),
        radius = r,
        center = center
    )
}

private fun DrawScope.drawPointer() {
    val outer = size.minDimension * 0.90f / 2f
    val top = center.y - outer - 4f
    val w = size.minDimension * 0.05f
    val h = size.minDimension * 0.055f
    val path = Path().apply {
        moveTo(center.x - w, top - h * 0.2f)
        lineTo(center.x + w, top - h * 0.2f)
        lineTo(center.x, top + h)
        close()
    }
    drawPath(path = path, color = ChipBlack, style = Fill)
    drawPath(path = path, color = ChipRed, style = Stroke(width = 2.5f))
}

private fun DrawScope.drawCrown(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.30f)
        lineTo(w * 0.25f, h * 0.60f)
        lineTo(w * 0.50f, 0f)
        lineTo(w * 0.75f, h * 0.60f)
        lineTo(w, h * 0.30f)
        lineTo(w, h)
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}
