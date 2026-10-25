package com.inouiw.quillo.ui.screens

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.QCardInfo
import com.inouiw.quillo.ui.Routes
import com.inouiw.quillo.ui.components.BalanceHero
import com.inouiw.quillo.ui.components.GoalTile
import com.inouiw.quillo.ui.components.IconCircle
import com.inouiw.quillo.ui.components.QCardBox
import com.inouiw.quillo.ui.components.SectionHeader
import com.inouiw.quillo.ui.components.TxRow
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QSurface

/** Home shows balance, quick actions, goals and activity in both states. The add-card prompt shows only when no card exists. */
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val cards = AppData.cards

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.good_evening, AppData.profile.name.substringBefore(' ')), fontSize = 13.sp, color = QMuted)
                    Text(stringResource(R.string.your_money), fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = QInk)
                }
                IconButton(
                    onClick = { onNavigate(Routes.NOTIFICATIONS) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(QCard)
                        .border(1.dp, QInk.copy(alpha = 0.08f), CircleShape),
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = stringResource(R.string.notifications_cd), tint = QInk)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(QInk)
                        .clickable { onNavigate(Routes.PROFILE) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(AppData.profile.initials, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            BalanceHero(balance = AppData.summary.balance, monthlyChange = AppData.summary.monthlyChange)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickAction(stringResource(R.string.quick_send), Icons.Filled.ArrowUpward, Modifier.weight(1f)) { onNavigate(Routes.SEND) }
                QuickAction(stringResource(R.string.quick_request), Icons.Filled.ArrowDownward, Modifier.weight(1f)) { onNavigate(Routes.REQUEST) }
                QuickAction(stringResource(R.string.top_up), Icons.Filled.AddCircle, Modifier.weight(1f)) { onNavigate(Routes.TOP_UP) }
                QuickAction(stringResource(R.string.quick_pay_bill), Icons.Filled.Receipt, Modifier.weight(1f)) { onNavigate(Routes.PAY_BILL) }
            }
        }

        if (cards.isEmpty()) {
            item {
                AddCardPrompt(onClick = { onNavigate(Routes.ADD_CARD) })
            }
        } else {
            item {
                SectionHeader(stringResource(R.string.your_cards), stringResource(R.string.see_all)) { onNavigate(Routes.CARDS) }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cards) { card ->
                        CardTile(card, modifier = Modifier.width(260.dp))
                    }
                }
            }
        }

        item {
            SectionHeader(stringResource(R.string.savings_goals), stringResource(R.string.see_all)) { onNavigate(Routes.SAVINGS) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppData.goals.take(2).forEach { goal ->
                    GoalTile(goal, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeader(stringResource(R.string.recent_activity), stringResource(R.string.see_all)) { onNavigate(Routes.ACTIVITY) }
        }
        item {
            QCardBox(modifier = Modifier.fillMaxWidth()) {
                AppData.transactions.take(3).forEach { txn ->
                    TxRow(
                        title = txn.title,
                        subtitle = listOf(txn.subtitle, txn.time).filter { it.isNotBlank() }.joinToString(" · "),
                        amount = txn.amount,
                        icon = iconFor(txn.category),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(QCard)
            .border(1.dp, QInk.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconCircle(icon, size = 36.dp)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = QInk)
    }
}

@Composable
private fun AddCardPrompt(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(QCard)
            .border(1.dp, Color(0xFF9ED9B9), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(Icons.Filled.CreditCard, background = QMint, size = 48.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.add_a_card), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = QInk)
            Text(stringResource(R.string.card_networks_hint), fontSize = 13.sp, color = QMuted)
        }
        IconCircle(Icons.Filled.Add, background = QInk, tint = Color.White, size = 36.dp)
    }
}

@Composable
fun CardTile(card: QCardInfo, modifier: Modifier = Modifier) {
    val dark = card.isCredit
    val main = if (dark) Color.White else QInk
    val sub = if (dark) QMint else QGreenInk
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (dark) QInk else QMint)
            .padding(16.dp)
            .height(180.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                card.label,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = main,
            )
            Icon(Icons.Filled.Contactless, contentDescription = null, tint = main)
        }
        Column {
            Text(
                stringResource(R.string.card_mask, card.last4),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = main,
            )
            Text(
                listOf(card.holder, card.expiry).filter { it.isNotBlank() }.joinToString(" · ").ifEmpty { card.detail },
                fontSize = 12.sp,
                color = sub,
            )
        }
    }
}

/** Picks an icon for a transaction category. */
fun iconFor(category: String): ImageVector = when (category) {
    "Groceries" -> Icons.Filled.Receipt
    "Income" -> Icons.Filled.AccountBalanceWallet
    "Cards" -> Icons.Filled.CreditCard
    "Travel" -> Icons.Filled.Flight
    "Shield" -> Icons.Filled.Shield
    else -> Icons.Filled.LocalCafe
}
