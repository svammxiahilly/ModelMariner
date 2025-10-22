package com.pranshulgg.clockmaster

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.yield
import kotlin.math.max
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pranshulgg.clockmaster.helpers.PreferencesHelper

class ScreenSaverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(
            WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars()
        )
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val screenSaverBrightness = PreferencesHelper.getInt("ScreenSaverBrightness") ?: 30
        val isAnalog = PreferencesHelper.getString("ScreenSaverClockStyle") == "Analog"

        val layoutParams = window.attributes
        layoutParams.screenBrightness = screenSaverBrightness / 100f
        window.attributes = layoutParams
        setContent {
            MaterialTheme {
                ScreenSaverScreen(
                    isAnalog = isAnalog,
                    is24hr = false,
                    onScreenTapped = { finish() })
            }
        }
    }
}


@Composable
fun ScreenSaverScreen(isAnalog: Boolean, is24hr: Boolean, onScreenTapped: () -> Unit) {
    val density = LocalDensity.current

    var targetOffsetPx by remember { mutableStateOf(Pair(0f, 0f)) }

    var childSize by remember { mutableStateOf(IntSize.Zero) }

    val alphaAnim = remember { Animatable(1f) }

    var currentMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val displayMillis = 5000L
    val fadeOutMillis = 1500
    val fadeInMillis = 1500

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentMillis = System.currentTimeMillis()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press ||
                            event.type == PointerEventType.Release ||
                            event.type == PointerEventType.Move
                        ) {
                            onScreenTapped()
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopStart
    ) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        val fallbackChildWidthPx = with(density) { (if (isAnalog) 150.dp else 200.dp).toPx() }
        val fallbackChildHeightPx = with(density) { (if (isAnalog) 150.dp else 60.dp).toPx() }

        LaunchedEffect(containerWidthPx, containerHeightPx) {
            if (containerWidthPx <= 0f || containerHeightPx <= 0f) return@LaunchedEffect

            val cw0 = if (childSize.width > 0) childSize.width.toFloat() else fallbackChildWidthPx
            val ch0 =
                if (childSize.height > 0) childSize.height.toFloat() else fallbackChildHeightPx
            val maxX0 = max(0f, containerWidthPx - cw0)
            val maxY0 = max(0f, containerHeightPx - ch0)
            targetOffsetPx = Pair(Random.nextFloat() * maxX0, Random.nextFloat() * maxY0)

            while (true) {
                delay(displayMillis)

                alphaAnim.animateTo(0f, animationSpec = tween(durationMillis = fadeOutMillis))

                val cw =
                    if (childSize.width > 0) childSize.width.toFloat() else fallbackChildWidthPx
                val ch =
                    if (childSize.height > 0) childSize.height.toFloat() else fallbackChildHeightPx
                val maxX = max(0f, containerWidthPx - cw)
                val maxY = max(0f, containerHeightPx - ch)

                targetOffsetPx = Pair(Random.nextFloat() * maxX, Random.nextFloat() * maxY)

                yield()

                alphaAnim.animateTo(1f, animationSpec = tween(durationMillis = fadeInMillis))
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        targetOffsetPx.first.roundToInt(),
                        targetOffsetPx.second.roundToInt()
                    )
                }
                .alpha(alphaAnim.value)
                .onGloballyPositioned { coords ->
                    childSize = coords.size
                }
        ) {
            if (isAnalog) {
                AnalogClockComposable(currentMillis = currentMillis)
            } else {
                DigitalClockComposable(currentMillis = currentMillis, is24hr = is24hr)
            }
        }
    }
}


@Composable
fun DigitalClockComposable(currentMillis: Long, is24hr: Boolean) {
    val calendar =
        remember(currentMillis) { Calendar.getInstance().apply { timeInMillis = currentMillis } }

    val dateStr = remember(currentMillis) {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(currentMillis))
    }

    val useNightMode = PreferencesHelper.getBool("useFullBlackForScreenSaver") ?: false

    val timeStr = remember(currentMillis) {
        SimpleDateFormat(if (is24hr) "HH:mm" else "hh:mm", Locale.getDefault()).format(
            Date(
                currentMillis
            )
        )
    }

    val amPmStr = remember(currentMillis) {
        if (!is24hr) SimpleDateFormat("a", Locale.getDefault()).format(Date(currentMillis)) else ""
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = timeStr,
                fontSize = 68.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .alignByBaseline()
                    .alpha(if (useNightMode) 0.3f else 1f)
            )

            if (!is24hr) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = amPmStr,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(if (useNightMode) 0.3f else 1f)
                )
            }
        }

        Text(
            text = dateStr,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.alpha(if (useNightMode) 0.3f else 1f)
        )
    }
}

@Composable
fun AnalogClockComposable(currentMillis: Long) {
    val useNightMode = PreferencesHelper.getBool("useFullBlackForScreenSaver") ?: false

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(if (useNightMode) 0.3f else 1f)
    ) {
        Surface(
            modifier = Modifier
                .size(200.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, clip = false),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cal = Calendar.getInstance().apply { timeInMillis = currentMillis }
                    val seconds = cal.get(Calendar.SECOND)
                    val minutes = cal.get(Calendar.MINUTE)
                    val hours12 = cal.get(Calendar.HOUR)

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f * 0.88f

                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = radius + 8f,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.02f),
                        radius = radius,
                        center = center
                    )

                    val tickLength = radius * 0.10f
                    val longTickLength = radius * 0.18f
                    val tickStroke = 3f
                    for (i in 0 until 12) {
                        val angleDeg = i * 30f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val outer = Offset(
                            x = center.x + (radius * kotlin.math.sin(angleRad)).toFloat(),
                            y = center.y - (radius * kotlin.math.cos(angleRad)).toFloat()
                        )
                        val inner = Offset(
                            x = center.x + ((radius - (if (i % 3 == 0) longTickLength else tickLength)) * kotlin.math.sin(
                                angleRad
                            )).toFloat(),
                            y = center.y - ((radius - (if (i % 3 == 0) longTickLength else tickLength)) * kotlin.math.cos(
                                angleRad
                            )).toFloat()
                        )
                        drawLine(
                            color = Color.White.copy(alpha = if (i % 3 == 0) 0.95f else 0.65f),
                            start = inner,
                            end = outer,
                            strokeWidth = if (i % 3 == 0) (tickStroke + 1f) else tickStroke,
                            cap = StrokeCap.Round
                        )
                    }

                    val minuteFraction = minutes + seconds / 60f
                    val hourFraction = (hours12 % 12) + minuteFraction / 60f

                    val minuteAngleDeg = minuteFraction * 6f
                    val hourAngleDeg = hourFraction * 30f

                    val hourLength = radius * 0.55f
                    val hourAngleRad = Math.toRadians(hourAngleDeg.toDouble())
                    val hourEnd = Offset(
                        x = center.x + (hourLength * kotlin.math.sin(hourAngleRad)).toFloat(),
                        y = center.y - (hourLength * kotlin.math.cos(hourAngleRad)).toFloat()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.95f),
                        start = center,
                        end = hourEnd,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    val minuteLength = radius * 0.78f
                    val minuteAngleRad = Math.toRadians(minuteAngleDeg.toDouble())
                    val minuteEnd = Offset(
                        x = center.x + (minuteLength * kotlin.math.sin(minuteAngleRad)).toFloat(),
                        y = center.y - (minuteLength * kotlin.math.cos(minuteAngleRad)).toFloat()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.9f),
                        start = center,
                        end = minuteEnd,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.95f),
                        radius = 3.5f,
                        center = center
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        val dateStr = remember(currentMillis) {
            SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(currentMillis))
        }

        Text(
            text = dateStr,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}


