package com.pranshulgg.clockmaster.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.ColorUtils
import com.pranshulgg.clockmaster.R
import com.pranshulgg.clockmaster.helpers.PreferencesHelper
import com.pranshulgg.clockmaster.models.TimerItem
import com.pranshulgg.clockmaster.models.TimerState
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalTextApi::class
)
@Composable
fun TimerItemRow(
    timer: TimerItem,
    onPauseResume: (String) -> Unit,
    onReset: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEditLabel: (String, String) -> Unit,
    onOpenFullscreen: (TimerItem) -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf(timer.label) }

    val progress = if (timer.initialMillis > 0) {
        1f - (timer.remainingMillis.toFloat() / timer.initialMillis.toFloat())
    } else 0f
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val thickStrokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) { Stroke(width = thickStrokeWidth, cap = StrokeCap.Round) }

    var useExpressiveColor by remember {
        mutableStateOf(
            PreferencesHelper.getBool("useExpressiveColor") ?: true
        )
    }

    val tileColor =
        if (timer.remainingMillis.toInt() == 0) if (useExpressiveColor) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceBright

    val animatedProgress by
    animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,

        )
    Surface(
        color = tileColor,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                onClick = { onOpenFullscreen(timer) },

                )

    ) {
        Box(
            Modifier
                .padding(end = 10.dp, top = 10.dp, bottom = 10.dp, start = 10.dp)
                .height(80.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(80.dp),
                        stroke = thickStroke,
                        amplitude = { _ -> (progress.coerceIn(0f, 1f) * 1.2f).coerceAtMost(1f) },

                        trackStroke = thickStroke,
                    )
                    IconButton(
                        onClick = {
                            onPauseResume(timer.id)
                        }
                    ) {
                        if (timer.state == TimerState.Paused) {
                            Symbol(
                                R.drawable.play_arrow,
                                size = 28.dp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Symbol(
                                R.drawable.pause,
                                size = 28.dp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        timer.label,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width((screenWidth.value / 2.1).dp)
                    )
                    Text(
                        formatMillis(timer.remainingMillis),
                        fontSize = 40.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily(
                            Font(
                                R.font.roboto_flex,
                                variationSettings = FontVariation.Settings(
                                    FontVariation.width(150f),
                                    FontVariation.weight(1000)
                                )
                            )
                        ),
                    )
                }


                Spacer(Modifier.weight(1f))

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Tooltip(
                        "Edit label",
                        preferredPosition = TooltipAnchorPosition.Below,
                        spacing = 10.dp
                    ) {
                        IconButton(
                            modifier = Modifier.size(42.dp, 35.dp),
                            onClick = { showEdit = true },
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Symbol(
                                R.drawable.edit,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 18.dp
                            )
                        }
                    }

                    Tooltip(
                        "Reset",
                        preferredPosition = TooltipAnchorPosition.Below,
                        spacing = 10.dp
                    ) {
                        IconButton(
                            modifier = Modifier.size(42.dp, 35.dp),
                            onClick = {
                                onReset(timer.id)
                            },
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Symbol(
                                R.drawable.refresh,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 18.dp
                            )
                        }
                    }

                }
            }

        }

        if (showEdit) {
            AlertDialog(
                onDismissRequest = { showEdit = false },
                confirmButton = {
                    TextButton(onClick = {
                        onEditLabel(timer.id, newLabel.trim().ifEmpty { timer.label })
                        showEdit = false
                    }, shapes = ButtonDefaults.shapes()) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEdit = false },
                        shapes = ButtonDefaults.shapes()
                    ) { Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp) }
                },
                title = { Text("Edit label") },
                text = {
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        placeholder = { Text("Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )

        }


//        )
    }
}


private fun formatMillis(ms: Long): String {
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val h = TimeUnit.MILLISECONDS.toHours(ms)

    return when {
        h > 0 -> String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        m > 0 -> String.format(Locale.US, "%d:%02d", m, s)
        else -> String.format(Locale.US, "%02d", s)
    }
}
