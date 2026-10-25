package com.inouiw.quillo.ui.components

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.Goal
import com.inouiw.quillo.data.formatInr
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreen
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QOutline
import com.inouiw.quillo.ui.theme.QTonal
import kotlin.math.abs

/** A white (or tinted) rounded card with the Quillo outline. */
@Composable
fun QCardBox(
    modifier: Modifier = Modifier,
    color: Color = QCard,
    bordered: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .then(if (bordered) Modifier.border(1.dp, QOutline, RoundedCornerShape(24.dp)) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = QInk, contentColor = Color.White),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun TonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = QTonal, contentColor = QInk),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun IconCircle(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = QTonal,
    tint: Color = QInk,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun ScreenTitle(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, QOutline, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = QInk)
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = QInk)
            if (subtitle != null) {
                Text(subtitle, fontSize = 13.sp, color = QMuted)
            }
        }
        trailing()
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = QInk)
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action, color = QGreenInk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun BalanceHero(balance: Double, monthlyChange: Double) {
    QCardBox(color = QMint, bordered = false, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.total_balance), fontSize = 12.sp, color = QGreenInk, fontWeight = FontWeight.Medium)
            Text(
                stringResource(R.string.change_this_month, "+" + formatInr(monthlyChange)),
                fontSize = 12.sp,
                color = QGreenInk,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            formatInr(balance, paise = true),
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            color = QInk,
        )
    }
}

@Composable
fun GoalTile(goal: Goal, modifier: Modifier = Modifier) {
    val fraction = (goal.saved / goal.target).toFloat().coerceIn(0f, 1f)
    QCardBox(modifier = modifier) {
        Text(goal.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = QInk)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = QGreen,
            trackColor = QTonal,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.goal_progress, formatInr(goal.saved), formatInr(goal.target)),
            fontSize = 11.sp,
            color = QMuted,
        )
    }
}

@Composable
fun TxRow(
    title: String,
    subtitle: String,
    amount: Double,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = QInk)
            Text(subtitle, fontSize = 12.sp, color = QMuted)
        }
        val sign = if (amount < 0) "-" else "+"
        Text(
            sign + formatInr(abs(amount), paise = abs(amount) % 1.0 != 0.0),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (amount > 0) QGreenInk else QInk,
        )
    }
}

@Composable
fun FieldBox(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, QOutline, RoundedCornerShape(16.dp))
            .background(QCard, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = QInk)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = QMuted, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 16.sp, color = QInk)
        }
    }
}

@Composable
fun ChevronRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(icon)
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = QInk)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = QMuted)
    }
}

/** Static Quillo mark, drawn in code so it stays crisp at any size. */
@Composable
fun QuilloMark(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension / 512f
        drawRoundRect(
            color = QInk,
            cornerRadius = CornerRadius(120f * s, 120f * s),
        )
        drawCircle(color = QGreen, radius = 150f * s, center = Offset(256f * s, 256f * s))
        drawLine(
            color = QInk,
            start = Offset(150f * s, 362f * s),
            end = Offset(366f * s, 146f * s),
            strokeWidth = 40f * s,
            cap = StrokeCap.Round,
        )
        drawCircle(color = Color.White, radius = 22f * s, center = Offset(366f * s, 146f * s))
    }
}
