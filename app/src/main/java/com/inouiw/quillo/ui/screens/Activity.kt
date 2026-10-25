package com.inouiw.quillo.ui.screens

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.Txn
import com.inouiw.quillo.data.formatInr
import com.inouiw.quillo.ui.components.IconCircle
import com.inouiw.quillo.ui.components.ScreenTitle
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QOutline
import com.inouiw.quillo.ui.theme.QSurface
import kotlin.math.abs

private val activityFilters = listOf(
    "All" to R.string.filter_all,
    "Cards" to R.string.filter_cards,
    "Groceries" to R.string.filter_groceries,
    "Income" to R.string.filter_income,
)

@Composable
fun ActivityScreen() {
    var filter by remember { mutableStateOf("All") }
    val groups = AppData.transactions
        .filter { filter == "All" || it.category == filter }
        .groupBy { it.day }
        .entries
        .toList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ScreenTitle(
                title = stringResource(R.string.activity),
                subtitle = stringResource(R.string.activity_subtitle),
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(QCard)
                            .border(1.dp, QOutline, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.filter), tint = QInk)
                    }
                },
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                activityFilters.forEach { (key, labelRes) ->
                    FilterPill(label = stringResource(labelRes), selected = filter == key) { filter = key }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(QCard)
                    .border(1.dp, QOutline, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                groups.forEach { group ->
                    Text(
                        group.key,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = QMuted,
                        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                    )
                    group.value.forEach { txn ->
                        TransactionItem(txn)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) QInk else QCard)
            .border(1.dp, if (selected) QInk else QOutline, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else QInk,
        )
    }
}

@Composable
private fun TransactionItem(txn: Txn) {
    val positive = txn.amount > 0
    val amount = (if (positive) "+" else "−") +
        formatInr(abs(txn.amount), paise = positive)
    val subtitle = listOf(txn.subtitle, txn.time).filter { it.isNotBlank() }.joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(iconFor(txn.category), size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(txn.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = QInk)
            Text(subtitle, fontSize = 12.sp, color = QMuted)
        }
        Text(
            amount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (positive) QGreenInk else QInk,
        )
    }
}
