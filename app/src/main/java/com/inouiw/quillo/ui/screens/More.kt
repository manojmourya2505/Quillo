package com.inouiw.quillo.ui.screens

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.ui.components.ChevronRow
import com.inouiw.quillo.ui.components.QCardBox
import com.inouiw.quillo.ui.components.ScreenTitle
import com.inouiw.quillo.ui.components.TonalButton
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QSurface

@Composable
fun ProfileScreen(onBack: () -> Unit, onSignOut: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenTitle(title = stringResource(R.string.profile), subtitle = stringResource(R.string.profile_subtitle), onBack = onBack)
        }
        item {
            QCardBox(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(QInk),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(AppData.profile.initials, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(AppData.profile.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = QInk)
                        Text(AppData.profile.email, fontSize = 13.sp, color = QMuted)
                    }
                }
            }
        }
        item {
            QCardBox(modifier = Modifier.fillMaxWidth()) {
                ChevronRow(stringResource(R.string.personal_details), Icons.Filled.AccountCircle)
                ChevronRow(stringResource(R.string.security_passcode), Icons.Filled.Security)
                ChevronRow(stringResource(R.string.notifications), Icons.Filled.Notifications)
                ChevronRow(stringResource(R.string.statements), Icons.Filled.Description)
                ChevronRow(stringResource(R.string.help_support), Icons.AutoMirrored.Filled.HelpOutline)
            }
        }
        item {
            TonalButton(
                text = stringResource(R.string.sign_out),
                onClick = onSignOut,
                icon = Icons.AutoMirrored.Filled.Logout,
            )
        }
    }
}
