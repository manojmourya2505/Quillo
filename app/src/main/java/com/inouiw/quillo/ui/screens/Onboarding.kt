package com.inouiw.quillo.ui.screens

import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.material3.IconButton

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.ui.components.PrimaryButton
import com.inouiw.quillo.ui.components.QuilloMark
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreen
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QSurface
import com.inouiw.quillo.ui.theme.QTonal
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.AppPrefs
import com.inouiw.quillo.data.BiometricGate
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

/** Splash: the mark draws itself in stages, then the wordmark rises in. */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val base = remember { Animatable(0f) }
    val disc = remember { Animatable(0f) }
    val pen = remember { Animatable(0f) }
    val dot = remember { Animatable(0f) }
    val word = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { base.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch {
            delay(350)
            disc.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
        }
        launch {
            delay(950)
            pen.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1550)
            dot.animateTo(1f, spring(dampingRatio = 0.5f))
        }
        launch {
            delay(1750)
            word.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
        delay(3400)
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QMint),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(modifier = Modifier.size(132.dp)) {
            val s = size.minDimension / 512f
            drawRoundRect(
                color = QInk,
                alpha = base.value,
                cornerRadius = CornerRadius(120f * s, 120f * s),
            )
            drawCircle(
                color = QGreen,
                radius = 150f * s * disc.value,
                center = Offset(256f * s, 256f * s),
            )
            val start = Offset(150f * s, 362f * s)
            val fullEnd = Offset(366f * s, 146f * s)
            val end = Offset(
                start.x + (fullEnd.x - start.x) * pen.value,
                start.y + (fullEnd.y - start.y) * pen.value,
            )
            drawLine(
                color = QInk,
                start = start,
                end = end,
                strokeWidth = 40f * s,
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = Color.White,
                alpha = dot.value,
                radius = 22f * s * dot.value,
                center = fullEnd,
            )
        }
        Spacer(Modifier.height(26.dp))
        Text(
            stringResource(R.string.app_name),
            modifier = Modifier.graphicsLayer {
                alpha = word.value
                translationY = (1f - word.value) * 26.dp.toPx()
            },
            fontSize = 42.sp,
            fontWeight = FontWeight.SemiBold,
            color = QInk,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.tagline),
            modifier = Modifier.graphicsLayer { alpha = word.value },
            fontSize = 16.sp,
            color = QGreenInk,
        )
    }
}

private data class IntroPage(val title: Int, val body: Int)

private val introPages = listOf(
    IntroPage(
        R.string.intro_title_1,
        R.string.intro_body_1,
    ),
    IntroPage(
        R.string.intro_title_2,
        R.string.intro_body_2,
    ),
    IntroPage(
        R.string.intro_title_3,
        R.string.intro_body_3,
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { introPages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == introPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onFinish) {
                Text(stringResource(R.string.skip), color = QGreenInk, fontWeight = FontWeight.SemiBold)
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            Column(modifier = Modifier.fillMaxSize()) {
                IntroArt(page)
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(introPages[page].title),
                    fontSize = 30.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = QInk,
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(introPages[page].body), fontSize = 16.sp, color = QMuted)
            }
        }
        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(introPages.size) { index ->
                val active = index == pagerState.currentPage
                val width by animateDpAsState(if (active) 24.dp else 8.dp, label = "dot")
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) QInk else QMint),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = if (isLast) stringResource(R.string.get_started) else stringResource(R.string.next),
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                if (isLast) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
        )
    }
}

@Composable
private fun IntroArt(page: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(QMint),
    ) {
        when (page) {
            0 -> Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 248.dp, height = 156.dp)
                    .graphicsLayer { rotationZ = -8f }
                    .clip(RoundedCornerShape(22.dp))
                    .background(QInk)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.sample_card_debit), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.sample_card_mask), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            1 -> Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 248.dp, height = 160.dp)
                    .graphicsLayer { rotationZ = 4f }
                    .clip(RoundedCornerShape(22.dp))
                    .background(QCard)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.sample_goal_name), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = QInk)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.62f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = QGreen,
                    trackColor = QTonal,
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.sample_goal_progress), fontSize = 13.sp, color = QMuted)
            }
            else -> Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        stringResource(R.string.sample_question),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(QInk)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                    )
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(
                        stringResource(R.string.sample_answer),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(QCard)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        color = QInk,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onSignIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var signingIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val activity = ctx as FragmentActivity
    var offerBiometric by remember { mutableStateOf(false) }

    fun signIn() {
        if (signingIn) return
        signingIn = true
        error = null
        scope.launch {
            try {
                Firebase.auth.signInWithEmailAndPassword(email.trim(), password).await()
                AppData.refresh()
                if (!AppPrefs.biometricAsked(ctx) && BiometricGate.isAvailable(activity)) {
                    offerBiometric = true
                } else {
                    onSignIn()
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: ctx.getString(R.string.sign_in_failed)
            } finally {
                signingIn = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (offerBiometric) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.offer_biometric_title)) },
                text = { Text(stringResource(R.string.offer_biometric_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        AppPrefs.setBiometric(ctx, true)
                        onSignIn()
                    }) { Text(stringResource(R.string.enable)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        AppPrefs.setBiometric(ctx, false)
                        onSignIn()
                    }) { Text(stringResource(R.string.not_now)) }
                },
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(QMint),
            contentAlignment = Alignment.Center,
        ) {
            QuilloMark(size = 72.dp)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(QCard)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Text(stringResource(R.string.welcome_back), fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = QInk)
                Text(stringResource(R.string.sign_in_subtitle), fontSize = 13.sp, color = QMuted)
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = stringResource(if (showPassword) R.string.hide_password else R.string.show_password),
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            error?.let { message ->
                Text(message, fontSize = 13.sp, color = Color(0xFFB3261E))
            }
            PrimaryButton(
                text = if (signingIn) stringResource(R.string.signing_in) else stringResource(R.string.sign_in),
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = { signIn() },
            )
        }
    }
}
