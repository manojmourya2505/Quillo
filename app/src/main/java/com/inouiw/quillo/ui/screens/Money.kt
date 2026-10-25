package com.inouiw.quillo.ui.screens

import com.inouiw.quillo.ui.components.SwipeToDeleteCard

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Shield
import com.inouiw.quillo.ui.components.IconCircle
import com.inouiw.quillo.ui.theme.QTonal
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.QCardInfo
import com.inouiw.quillo.data.formatInr
import com.inouiw.quillo.ui.Routes
import com.inouiw.quillo.ui.components.PrimaryButton
import com.inouiw.quillo.ui.components.QCardBox
import com.inouiw.quillo.ui.components.ScreenTitle
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QOutline
import com.inouiw.quillo.ui.theme.QSurface

@Composable
fun CardsScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val cards = AppData.cards
    val scope = rememberCoroutineScope()
    val debitCard = cards.firstOrNull { !it.isCredit }
    val frozen = debitCard?.frozen ?: false
    val online = AppData.onlinePayments

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenTitle(title = stringResource(R.string.cards), subtitle = stringResource(R.string.cards_subtitle))
        }
        if (cards.isEmpty()) {
            item {
                QCardBox(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.no_cards_yet), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = QInk)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.add_card_hint),
                        fontSize = 13.sp,
                        color = QMuted,
                    )
                }
            }
            item {
                PrimaryButton(
                    text = stringResource(R.string.add_a_card),
                    onClick = { onNavigate(Routes.ADD_CARD) },
                    icon = Icons.Filled.CreditCard,
                )
            }
        } else {
            items(cards) { card ->
                SwipeToDeleteCard(onDelete = { scope.launch { AppData.deleteCard(card.id) } }) {
                    CardTile(card, modifier = Modifier.fillMaxWidth())
                }
            }
            item {
                QCardBox(modifier = Modifier.fillMaxWidth()) {
                    ToggleRow(Icons.Filled.AcUnit, stringResource(R.string.freeze_debit_card), frozen) { value ->
                        debitCard?.let { card -> scope.launch { AppData.setFrozen(card.id, value) } }
                    }
                    ToggleRow(Icons.Filled.Shield, stringResource(R.string.online_payments), online) { value ->
                        scope.launch { AppData.setOnlinePayments(value) }
                    }
                }
            }
            item {
                PrimaryButton(
                    text = stringResource(R.string.add_a_card),
                    onClick = { onNavigate(Routes.ADD_CARD) },
                    icon = Icons.Filled.Add,
                )
            }
        }
    }
}

@Composable
fun AddCardScreen(onSaved: () -> Unit, onBack: () -> Unit) {
    var number by remember { mutableStateOf("6521000000000000") }
    var expiry by remember { mutableStateOf("09 / 29") }
    var cvv by remember { mutableStateOf("123") }
    var holder by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        ScreenTitle(title = stringResource(R.string.add_a_card), subtitle = stringResource(R.string.enter_card_details), onBack = onBack)

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !isCredit, onClick = { isCredit = false }, label = { Text(stringResource(R.string.debit)) })
            FilterChip(selected = isCredit, onClick = { isCredit = true }, label = { Text(stringResource(R.string.credit)) })
        }

        Spacer(Modifier.height(14.dp))

        // Card preview, same proportions as the prototype.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(QMint)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(if (isCredit) stringResource(R.string.credit_card) else stringResource(R.string.debit_card), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = QGreenInk)
            Spacer(Modifier.height(8.dp))
            Text("•••• •••• •••• ••••", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = QInk)
            Spacer(Modifier.height(8.dp))
            Text(
                if (holder.isBlank()) stringResource(R.string.name_on_card_placeholder) else holder.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = QGreenInk,
            )
        }

        Spacer(Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CardField(
                icon = Icons.Filled.CreditCard,
                label = stringResource(R.string.card_number),
                value = number.chunked(4).joinToString(" "),
                onValueChange = { input -> number = input.filter { it.isDigit() }.take(16) },
                keyboardType = KeyboardType.Number,
            )
            CardField(
                icon = Icons.Filled.Person,
                label = stringResource(R.string.name_on_card),
                value = holder,
                onValueChange = { input -> holder = input.take(26) },
                keyboardType = KeyboardType.Text,
            )
            CardField(
                icon = Icons.Filled.DateRange,
                label = stringResource(R.string.expiry),
                value = expiry,
                onValueChange = { input -> expiry = input.take(7) },
                keyboardType = KeyboardType.Number,
            )
            CardField(
                icon = Icons.Filled.Lock,
                label = stringResource(R.string.security_code),
                value = cvv,
                onValueChange = { input -> cvv = input.filter { it.isDigit() }.take(3) },
                keyboardType = KeyboardType.NumberPassword,
                masked = true,
            )
        }

        Spacer(Modifier.weight(1f))

        val scope = rememberCoroutineScope()
        val cardLabel = if (isCredit) stringResource(R.string.credit_card) else stringResource(R.string.debit_card)
        val creditLimit = stringResource(R.string.card_limit, stringResource(R.string.card_limit_amount))
        val linkedAccount = stringResource(R.string.linked_account, formatInr(AppData.summary.availableDebit, paise = true))
        PrimaryButton(
            text = stringResource(R.string.save_card),
            icon = Icons.Filled.Check,
            onClick = {
                val last4 = number.takeLast(4)
                val kind = if (isCredit) "Credit" else "Debit"
                scope.launch {
                    AppData.addCard(
                        QCardInfo(
                        id = "",
                        label = cardLabel,
                        last4 = last4,
                        network = "RuPay",
                        kind = kind,
                        isCredit = isCredit,
                        detail = if (isCredit) creditLimit else linkedAccount,
                        holder = holder.trim().ifEmpty { AppData.profile.name },
                        expiry = expiry,
                    ),
                    )
                    onSaved()
                }
            },
        )
    }
}

@Composable
private fun CardField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    masked: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, QOutline, RoundedCornerShape(16.dp))
            .background(QCard, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = QInk)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = QMuted)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = QInk),
                cursorBrush = SolidColor(QInk),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (masked) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(icon)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = QInk)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = QInk,
                uncheckedThumbColor = QMuted,
                uncheckedTrackColor = QTonal,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}


