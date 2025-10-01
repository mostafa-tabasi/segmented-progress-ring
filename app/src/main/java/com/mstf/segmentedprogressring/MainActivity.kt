package com.mstf.segmentedprogressring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            CircleAvatarWithSegments(
                                resId = R.drawable.ic_launcher_background,
                                state.progressList,
                                modifier = Modifier.padding(innerPadding),
                                gapAngle = 16f,
                            )
                        }
                        Button(onClick = {
                            viewModel.addProgress()
                        }) {
                            Text("Add Story")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircleAvatarWithSegments(
    @DrawableRes resId: Int,
    progresses: List<Float>, // N segments, each 0f..1f
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    gapAngle: Float = 6f, // margin in degrees between segments
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    contentDescription: String? = null
) {
    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
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
                .size(size - strokeWidth - 4.dp) // keeps it inside the ring
                .clip(CircleShape)
        )
    }
}