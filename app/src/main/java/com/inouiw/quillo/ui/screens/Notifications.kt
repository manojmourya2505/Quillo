package com.inouiw.quillo.ui.screens

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.Notice
import com.inouiw.quillo.ui.components.ScreenTitle
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreen
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QOutline
import com.inouiw.quillo.ui.theme.QSurface
import com.inouiw.quillo.ui.theme.QTonal

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notices = AppData.notices
    val unread = notices.count { it.unread }
    val today = notices.filter { it.group == "Today" }
    val earlier = notices.filter { it.group == "Earlier" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenTitle(title = stringResource(R.string.notifications), subtitle = stringResource(R.string.notifications_new_count, unread), onBack = onBack)
        }
        if (today.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.section_today)) }
            item { NoticeGroup(today) }
        }
        if (earlier.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.section_earlier)) }
            item { NoticeGroup(earlier) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = QMuted,
    )
}

@Composable
private fun NoticeGroup(notices: List<Notice>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(QCard)
            .border(1.dp, QOutline, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        notices.forEach { notice ->
            NoticeRow(notice)
        }
    }
}

private fun noticeIcon(key: String): ImageVector = when (key) {
    "arrow" -> Icons.Filled.ArrowOutward
    "freeze" -> Icons.Filled.AcUnit
    "savings" -> Icons.Filled.Savings
    "security" -> Icons.Filled.VerifiedUser
    else -> Icons.Filled.Notifications
}

@Composable
private fun NoticeRow(notice: Notice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (notice.unread) QMint else QTonal),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                noticeIcon(notice.icon),
                contentDescription = null,
                tint = QInk,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = if (notice.icon == "arrow") 180f else 0f },
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(notice.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = QInk)
            Text(notice.body, fontSize = 13.sp, color = QMuted)
            Text(notice.time, fontSize = 12.sp, color = QMuted)
        }
        if (notice.unread) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(QGreen),
            )
        }
    }
}
