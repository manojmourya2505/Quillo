package com.inouiw.quillo.ui.screens

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.inouiw.quillo.data.BiometricGate
import com.inouiw.quillo.ui.components.PrimaryButton
import com.inouiw.quillo.ui.components.QuilloMark
import com.inouiw.quillo.ui.components.TonalButton
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QSurface

/** Shown on later launches when fingerprint/face unlock is turned on. */
@Composable
fun UnlockScreen(onUnlocked: () -> Unit, onUsePassword: () -> Unit) {
    val activity = LocalContext.current as FragmentActivity
    var message by remember { mutableStateOf<String?>(null) }

    fun unlock() {
        BiometricGate.prompt(activity, onSuccess = onUnlocked, onFailure = { message = it })
    }

    LaunchedEffect(Unit) { unlock() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        QuilloMark(size = 72.dp)
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.unlock_quillo), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = QInk)
        Spacer(Modifier.height(8.dp))
        Text(
            message ?: stringResource(R.string.unlock_hint),
            fontSize = 13.sp,
            color = QMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = stringResource(R.string.unlock), icon = Icons.Filled.Fingerprint, onClick = { unlock() })
        Spacer(Modifier.height(12.dp))
        TonalButton(text = stringResource(R.string.use_password), icon = Icons.Filled.Lock, onClick = onUsePassword)
    }
}
