package com.inouiw.quillo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.R
import com.inouiw.quillo.ui.components.PrimaryButton
import com.inouiw.quillo.ui.components.QuilloMark
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QSurface
import kotlinx.coroutines.delay

/** Result of a payment or request, shown on the full-screen result page. */
class PaymentOutcome(val success: Boolean, val title: String, val message: String)

/** Waits so the processing page is seen for at least [minMillis], even if saving was quick. */
suspend fun awaitMinimum(startedAt: Long, minMillis: Long = 1600L) {
    val elapsed = System.currentTimeMillis() - startedAt
    if (elapsed < minMillis) delay(minMillis - elapsed)
}

@Composable
fun PaymentProcessingScreen() {
    val pulse = rememberInfiniteTransition(label = "processing")
    val logoScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
        label = "logoScale",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(170.dp),
                color = QGreenInk,
                trackColor = QMint,
                strokeWidth = 4.dp,
            )
            QuilloMark(size = 110.dp, modifier = Modifier.graphicsLayer { scaleX = logoScale; scaleY = logoScale })
        }
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.processing_title), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = QInk)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.processing_body), fontSize = 14.sp, color = QMuted, textAlign = TextAlign.Center)
    }
}

@Composable
fun PaymentResultScreen(outcome: PaymentOutcome, onDone: () -> Unit) {
    val logoScale = remember { Animatable(0.3f) }
    val badgeScale = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(outcome) {
        logoScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(outcome) {
        delay(350)
        badgeScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
    }
    LaunchedEffect(outcome) {
        reveal.animateTo(1f, tween(durationMillis = 450, delayMillis = 450))
    }
    LaunchedEffect(outcome) {
        if (!outcome.success) {
            delay(400)
            repeat(3) {
                shake.animateTo(14f, tween(durationMillis = 60))
                shake.animateTo(-14f, tween(durationMillis = 60))
            }
            shake.animateTo(0f, tween(durationMillis = 60))
        }
    }

    val accent = if (outcome.success) QGreenInk else Color(0xFFB3261E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                QuilloMark(
                    size = 120.dp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        translationX = shake.value
                    },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(52.dp)
                        .graphicsLayer {
                            scaleX = badgeScale.value
                            scaleY = badgeScale.value
                        }
                        .background(accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (outcome.success) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = outcome.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = QInk,
                modifier = Modifier.graphicsLayer {
                    alpha = reveal.value
                    translationY = (1f - reveal.value) * 24f
                },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = outcome.message,
                fontSize = 15.sp,
                color = QMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .graphicsLayer {
                        alpha = reveal.value
                        translationY = (1f - reveal.value) * 24f
                    },
            )
        }
        PrimaryButton(text = stringResource(R.string.result_ok), onClick = onDone, icon = Icons.Filled.Check)
    }
}
