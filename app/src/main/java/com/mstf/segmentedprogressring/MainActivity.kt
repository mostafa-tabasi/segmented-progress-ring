package com.mstf.segmentedprogressring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mstf.segmentedprogressring.ui.theme.SegmentedProgressRingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SegmentedProgressRingTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircleAvatarWithSegments(
                                resId = R.drawable.profile,
                                progresses = state.progressList,
                                modifier = Modifier.padding(innerPadding),
                                gapAngle = state.segmentGap,
                                strokeWidth = state.segmentStrokeWidth.dp,
                                size = state.avatarSize.dp,
                                avatarPadding = state.avatarPadding.dp,
                            )
                        }
                        Column(
                            Modifier.weight(2f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Button(onClick = { viewModel.addProgress() }) { Text("Add Segment") }
                            Spacer(Modifier.height(8.dp))
                            SliderWithText(
                                label = "Avatar size",
                                value = state.avatarSize,
                                valueRange = 48f..256f,
                                onValueChange = {
                                    viewModel.updateAvatarSize(it)
                                })
                            SliderWithText(
                                label = "Avatar padding",
                                value = state.avatarPadding,
                                valueRange = 4f..24f,
                                onValueChange = {
                                    viewModel.updateAvatarPadding(it)
                                })
                            SliderWithText(
                                label = "Segment width",
                                value = state.segmentStrokeWidth,
                                valueRange = 4f..16f,
                                onValueChange = {
                                    viewModel.updateSegmentStrokeWidth(it)
                                })
                            SliderWithText(
                                label = "Segment gap",
                                value = state.segmentGap,
                                valueRange = 1f..32f,
                                onValueChange = {
                                    viewModel.updateSegmentGap(it)
                                })
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    viewModel.toggleControlCheckbox()
                                }) {
                                Checkbox(
                                    checked = state.controlAllSegmentsWithOneSlider,
                                    onCheckedChange = { viewModel.toggleControlCheckbox() }
                                )
                                Text("Control all segments with one slider")
                            }
                            if (state.controlAllSegmentsWithOneSlider) {
                                if (state.progressList.isNotEmpty()) {
                                    Slider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        value = state.totalProgress,
                                        onValueChange = { viewModel.updateTotalProgress(it) },
                                        valueRange = 0f..state.progressList.size.toFloat()
                                    )
                                }
                            } else {
                                state.progressList.forEachIndexed { i, progress ->
                                    Slider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        value = progress,
                                        onValueChange = {
                                            viewModel.updateProgress(i, it)
                                        })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SliderWithText(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.padding(start = 16.dp),
        )
        Slider(
            modifier = Modifier.padding(horizontal = 16.dp),
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange
        )
    }
}

@Composable
fun CircleAvatarWithSegments(
    @DrawableRes resId: Int,
    progresses: List<Float>, // N segments, each 0f..1f
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    avatarPadding: Dp = 4.dp,
    strokeWidth: Dp = 6.dp,
    gapAngle: Float = 6f, // margin in degrees between segments
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
    contentDescription: String? = null
) {
    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        if (progresses.isNotEmpty()) {
            Canvas(modifier = Modifier.size(size + strokeWidth * 2)) {
                val arcSize = Size(size.toPx(), size.toPx())
                val inset = strokeWidth.toPx()

                val sweepPerSegment = (360f / progresses.size) - gapAngle

                // Background arcs
                progresses.forEachIndexed { i, _ ->
                    val start = -90f + i * (360f / progresses.size) + gapAngle / 2
                    drawArc(
                        color = trackColor,
                        startAngle = start,
                        sweepAngle = sweepPerSegment,
                        useCenter = false,
                        style = stroke,
                        size = arcSize,
                        topLeft = Offset(inset, inset)
                    )
                }

                // Progress arcs
                progresses.forEachIndexed { i, progress ->
                    val start = -90f + i * (360f / progresses.size) + gapAngle / 2
                    drawArc(
                        color = progressColor,
                        startAngle = start,
                        sweepAngle = sweepPerSegment * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = stroke,
                        size = arcSize,
                        topLeft = Offset(inset, inset)
                    )
                }
            }
        }

        // Avatar in the center
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size - strokeWidth - avatarPadding) // keeps it inside the ring
                .clip(CircleShape)
        )
    }
}