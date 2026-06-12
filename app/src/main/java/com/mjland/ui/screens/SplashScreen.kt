package com.mjland.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 60f
        ),
        label = "progress"
    )

    val sweepAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "sweep"
    )
    
    val fillAlphaAnim by animateFloatAsState(
        targetValue = if (sweepAnim > 0.6f) 1f else 0f,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "fillAlpha"
    )

    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
        delay(2500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val sweepWidth = 800f
        val currentX = sweepAnim * (sweepWidth * 2) - (sweepWidth / 2f)

        val strokeBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                Color.White.copy(alpha = 0.8f),
                Color.White,
                Color.White.copy(alpha = 0.8f),
                Color.Transparent,
                Color.Transparent
            ),
            start = androidx.compose.ui.geometry.Offset(currentX - sweepWidth / 2f, 0f),
            end = androidx.compose.ui.geometry.Offset(currentX + sweepWidth / 2f, 0f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .graphicsLayer {
                    val scale = 0.8f + (0.2f * progress)
                    scaleX = scale
                    scaleY = scale
                    translationY = 50f * (1f - progress)
                    alpha = progress.coerceIn(0f, 1f)
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                
                Text(
                    text = "CornCastle",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        drawStyle = Stroke(
                            width = 2f,
                            join = StrokeJoin.Round,
                            cap = StrokeCap.Round
                        )
                    ),
                    color = Color.White.copy(alpha = 0.1f)
                )

                
                Text(
                    text = "CornCastle",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        brush = strokeBrush,
                        drawStyle = Stroke(
                            width = 3f,
                            join = StrokeJoin.Round,
                            cap = StrokeCap.Round
                        )
                    ),
                    color = Color.Unspecified,
                    modifier = Modifier.graphicsLayer { alpha = 0.9f }
                )
                
                
                Text(
                    text = "CornCastle",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp
                    ),
                    color = Color.White.copy(alpha = fillAlphaAnim)
                )
            }
        }
    }
}

