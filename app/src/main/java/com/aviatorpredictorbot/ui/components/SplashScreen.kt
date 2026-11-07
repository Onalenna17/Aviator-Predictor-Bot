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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.R
import com.aviatorpredictorbot.ui.theme.ButtonRed
import com.aviatorpredictorbot.ui.theme.ChipBlack
import com.aviatorpredictorbot.ui.theme.ChipRed
import com.aviatorpredictorbot.ui.theme.NeonCrimson
import java.util.Locale
import kotlin.math.ln
import kotlin.math.exp

private const val SPLASH_MS = 2600

/**
 * Animated launch screen: the radar logo pops in inside a spinning chip ring with a radar sweep,
 * the title fades in, and a crash-style multiplier climbs from 1.00x while the live feed is
 * "armed". Calls [onFinished] when the animation completes.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val logoIn = remember { Animatable(0f) }
    val titleIn = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Staggered intro, run in sequence inside one effect so the timing stays predictable.
        logoIn.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        titleIn.animateTo(1f, tween(450))
    }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(SPLASH_MS, easing = FastOutSlowInEasing))
        onFinished()
    }

    val transition = rememberInfiniteTransition(label = "splash")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "sweep"
    )
    val ringSpin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "ringSpin"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    // Multiplier climbs like a flight: 1.00x -> ~28.99x, slow start then accelerating.
    val multiplier = exp(ln(28.99) * progress.value * progress.value)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChipBlack)
            .pointerInput(Unit) { detectTapGestures { } }   // swallow touches while the splash is up
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient red glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                        0.0f to ChipRed.copy(alpha = 0.40f * pulse),
                        0.6f to ChipRed.copy(alpha = 0.10f * pulse),
                        1.0f to Color.Transparent,
                    center = center,
                    radius = size.maxDimension * 0.55f
                ),
                radius = size.maxDimension * 0.55f,
                center = center
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(0.6f + 0.4f * logoIn.value)
                    .alpha(logoIn.value),
                contentAlignment = Alignment.Center
            ) {
                // Spinning chip ring + radar sweep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 7.dp.toPx()
                    val d = size.minDimension - stroke
                    rotate(ringSpin, pivot = center) {
                        for (i in 0 until 12) {
                            drawArc(
                                color = if (i % 3 == 1) Color(0xFF3A0008) else ChipRed,
                                startAngle = i * 30f + 2f,
                                sweepAngle = 26f,
                                useCenter = false,
                                topLeft = Offset(stroke / 2f, stroke / 2f),
                                size = Size(d, d),
                                style = Stroke(width = stroke, cap = StrokeCap.Butt)
                            )
                        }
                    }
                    rotate(sweep, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                    0.0f to Color.Transparent,
                                    0.85f to Color.Transparent,
                                    1.0f to NeonCrimson.copy(alpha = 0.55f),
                                center = center
                            ),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = true,
                            topLeft = Offset(stroke * 2.2f, stroke * 2.2f),
                            size = Size(size.width - stroke * 4.4f, size.height - stroke * 4.4f)
                        )
                    }
                }
                Image(
                    painter = painterResource(R.drawable.ic_flyx_logo),
                    contentDescription = "App logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AVIATOR PREDICTOR",
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                letterSpacing = 5.sp,
                modifier = Modifier.alpha(titleIn.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = String.format(Locale.US, "%.2fx", multiplier),
                color = NeonCrimson,
                fontSize = 46.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                modifier = Modifier.alpha(titleIn.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A0008))
                    .alpha(titleIn.value)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value.coerceIn(0.001f, 1f))
                        .background(ButtonRed, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "SYNCING LIVE FEED",
                color = Color(0xFF9A6A74),
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(titleIn.value * pulse.coerceIn(0.6f, 1f))
            )
        }
    }
}
