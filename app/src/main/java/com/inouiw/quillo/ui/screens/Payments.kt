package com.inouiw.quillo.ui.screens

import androidx.compose.material.icons.filled.Close

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.ModalBottomSheet

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.material3.OutlinedTextField


import com.inouiw.quillo.data.RazorpayCheckout

import com.inouiw.quillo.data.Bill
import com.inouiw.quillo.data.QCardInfo

import kotlinx.coroutines.launch

import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.rememberCoroutineScope

import androidx.fragment.app.FragmentActivity

import android.widget.Toast

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.formatInr
import com.inouiw.quillo.ui.components.FieldBox
import com.inouiw.quillo.ui.components.IconCircle
import com.inouiw.quillo.ui.components.PrimaryButton
import com.inouiw.quillo.ui.components.QCardBox
import com.inouiw.quillo.ui.components.ScreenTitle
import com.inouiw.quillo.ui.components.TonalButton
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmSheet(title: String, body: String, confirmText: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = QSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = QInk)
            Text(body, fontSize = 15.sp, color = QMuted)
            Spacer(Modifier.height(4.dp))
            PrimaryButton(text = confirmText, onClick = onConfirm, icon = Icons.AutoMirrored.Filled.Send)
            TonalButton(text = stringResource(R.string.send_cancel), onClick = onDismiss, icon = Icons.Filled.Close)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardPickerSheet(cards: List<QCardInfo>, onPick: (QCardInfo) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = QSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.choose_card), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = QInk)
            if (cards.isEmpty()) {
                Text(stringResource(R.string.no_cards_yet), fontSize = 14.sp, color = QMuted)
            }
            cards.forEach { card ->
                QCardBox(modifier = Modifier.fillMaxWidth().clickable { onPick(card) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconCircle(Icons.Filled.CreditCard)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(card.label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = QInk)
                            Text(stringResource(R.string.card_mask, card.last4), fontSize = 12.sp, color = QMuted)
                        }
                        if (card.frozen) {
                            Text(stringResource(R.string.frozen_label), fontSize = 12.sp, color = QMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SendScreen(onBack: () -> Unit) {
    var digits by remember { mutableStateOf("") }
    var recipient by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var reviewing by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<PaymentOutcome?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val amount = digits.toDoubleOrNull() ?: 0.0

    fun press(key: String) {
        digits = when (key) {
            "⌫" -> digits.dropLast(1)
            else -> if (digits.length < 7) digits + key else digits
        }
    }

    if (processing) {
        PaymentProcessingScreen()
        return
    }
    val current = outcome
    if (current != null) {
        PaymentResultScreen(outcome = current) {
            outcome = null
            if (current.success) onBack()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle(title = stringResource(R.string.send_money), subtitle = stringResource(R.string.send_money_subtitle), onBack = onBack)

        QCardBox(color = QInk, bordered = false, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.amount), fontSize = 12.sp, color = QMint)
            Text(formatInr(amount, paise = true), fontSize = 36.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(stringResource(R.string.available_amount, formatInr(AppData.summary.availableDebit, paise = true)), fontSize = 12.sp, color = QMint)
        }

        OutlinedTextField(
            value = recipient,
            onValueChange = { recipient = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.to)) },
            placeholder = { Text(stringResource(R.string.enter_upi_or_phone)) },
            leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
            singleLine = true,
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.note)) },
            placeholder = { Text(stringResource(R.string.add_a_note)) },
            leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
            singleLine = true,
        )

        Spacer(Modifier.weight(1f))

        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "⌫"))
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(width = 96.dp, height = 46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = key.isNotEmpty()) { press(key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (key == "⌫") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = stringResource(R.string.delete), tint = QInk)
                        } else if (key.isNotEmpty()) {
                            Text(key, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = QInk)
                        }
                    }
                }
            }
        }

        PrimaryButton(
            text = stringResource(R.string.review_payment),
            onClick = {
                val message = when {
                    recipient.isBlank() -> R.string.send_need_recipient
                    amount <= 0.0 -> R.string.send_need_amount
                    amount > AppData.summary.availableDebit -> R.string.send_insufficient
                    else -> null
                }
                if (message != null) {
                    Toast.makeText(context, context.getString(message), Toast.LENGTH_SHORT).show()
                } else {
                    reviewing = true
                }
            },
            icon = Icons.AutoMirrored.Filled.Send,
        )
    }

    if (reviewing) {
        ConfirmSheet(
            title = stringResource(R.string.send_review_title),
            body = stringResource(R.string.send_review_body, formatInr(amount, paise = true), recipient.trim()),
            confirmText = stringResource(R.string.send_confirm),
            onConfirm = {
                reviewing = false
                processing = true
                val who = recipient.trim()
                val sum = amount
                val noteText = note.trim()
                scope.launch {
                    val started = System.currentTimeMillis()
                    val result = try {
                        AppData.recordSend(who, sum, noteText)
                        PaymentOutcome(
                            true,
                            context.getString(R.string.title_send_ok),
                            context.getString(R.string.send_success, formatInr(sum, paise = true), who),
                        )
                    } catch (e: Exception) {
                        PaymentOutcome(false, context.getString(R.string.title_payment_failed), context.getString(R.string.send_failed))
                    }
                    awaitMinimum(started)
                    outcome = result
                    processing = false
                }
            },
            onDismiss = { reviewing = false },
        )
    }
}

@Composable
fun RequestScreen(onBack: () -> Unit) {
    var from by rememberSaveable { mutableStateOf("") }
    var digits by rememberSaveable { mutableStateOf("") }
    var purpose by rememberSaveable { mutableStateOf("") }
    var processing by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<PaymentOutcome?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val amount = digits.toDoubleOrNull() ?: 0.0

    if (processing) {
        PaymentProcessingScreen()
        return
    }
    val current = outcome
    if (current != null) {
        PaymentResultScreen(outcome = current) {
            outcome = null
            if (current.success) onBack()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle(title = stringResource(R.string.request_money), subtitle = stringResource(R.string.request_subtitle), onBack = onBack)
        OutlinedTextField(
            value = from,
            onValueChange = { from = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.from)) },
            placeholder = { Text(stringResource(R.string.enter_upi_or_phone)) },
            leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
            singleLine = true,
        )
        OutlinedTextField(
            value = digits,
            onValueChange = { digits = it.filter { c -> c.isDigit() || c == '.' } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.amount)) },
            placeholder = { Text(stringResource(R.string.enter_amount)) },
            leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            singleLine = true,
        )
        OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.for_label)) },
            placeholder = { Text(stringResource(R.string.what_for)) },
            leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
            singleLine = true,
        )
        TonalButton(text = stringResource(R.string.copy_payment_link), onClick = {}, icon = Icons.Filled.ContentCopy)

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = stringResource(R.string.send_request),
            onClick = {
                val message = when {
                    from.isBlank() -> R.string.send_need_recipient
                    amount <= 0.0 -> R.string.send_need_amount
                    else -> null
                }
                if (message != null) {
                    Toast.makeText(context, context.getString(message), Toast.LENGTH_SHORT).show()
                } else {
                    val who = from.trim()
                    val purposeText = purpose.trim()
                    processing = true
                    scope.launch {
                        val started = System.currentTimeMillis()
                        val result = try {
                            AppData.recordRequest(who, amount, purposeText)
                            PaymentOutcome(
                                true,
                                context.getString(R.string.title_request_ok),
                                context.getString(R.string.request_success, formatInr(amount, paise = true), who),
                            )
                        } catch (e: Exception) {
                            PaymentOutcome(false, context.getString(R.string.title_request_failed), context.getString(R.string.request_failed))
                        }
                        awaitMinimum(started)
                        outcome = result
                        processing = false
                    }
                }
            },
            icon = Icons.AutoMirrored.Filled.Send,
        )
    }
}

@Composable
fun TopUpScreen(onBack: () -> Unit) {
    val options = listOf(500, 1000, 5000)
    var selected by remember { mutableStateOf(1000) }
    var cardId by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingCard by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<PaymentOutcome?>(null) }
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val scope = rememberCoroutineScope()
    val card = AppData.cards.firstOrNull { it.id == cardId }

    fun startTopUp() {
        val chosen = card
        if (chosen == null) {
            Toast.makeText(context, context.getString(R.string.card_required), Toast.LENGTH_SHORT).show()
            return
        }
        if (chosen.frozen) {
            Toast.makeText(context, context.getString(R.string.card_frozen), Toast.LENGTH_SHORT).show()
            return
        }
        val amount = selected.toDouble()
        RazorpayCheckout.open(activity, amount, context.getString(R.string.razorpay_topup)) { ok ->
            if (ok) {
                processing = true
                scope.launch {
                    val started = System.currentTimeMillis()
                    val result = try {
                        AppData.recordTopUp(amount, chosen.label)
                        PaymentOutcome(
                            true,
                            context.getString(R.string.title_topup_ok),
                            context.getString(R.string.topup_success, formatInr(amount, paise = true), chosen.label),
                        )
                    } catch (e: Exception) {
                        PaymentOutcome(false, context.getString(R.string.title_payment_failed), context.getString(R.string.save_failed))
                    }
                    awaitMinimum(started)
                    outcome = result
                    processing = false
                }
            } else {
                outcome = PaymentOutcome(false, context.getString(R.string.title_payment_failed), context.getString(R.string.topup_failed))
            }
        }
    }

    if (processing) {
        PaymentProcessingScreen()
        return
    }
    val current = outcome
    if (current != null) {
        PaymentResultScreen(outcome = current) {
            outcome = null
            if (current.success) onBack()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle(title = stringResource(R.string.top_up), subtitle = stringResource(R.string.top_up_subtitle), onBack = onBack)
        FieldBox(
            label = stringResource(R.string.from_card),
            value = card?.let { "${it.label} · ${stringResource(R.string.card_mask, it.last4)}" } ?: stringResource(R.string.choose_card),
            icon = Icons.Filled.CreditCard,
            modifier = Modifier.clickable { pickingCard = true },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { amount ->
                FilterChip(
                    selected = selected == amount,
                    onClick = { selected = amount },
                    label = { Text(formatInr(amount.toDouble())) },
                )
            }
        }
        FieldBox(stringResource(R.string.amount), formatInr(selected.toDouble(), paise = true), Icons.Filled.AccountBalanceWallet)

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = stringResource(R.string.top_up_amount, formatInr(selected.toDouble(), paise = true)),
            onClick = { startTopUp() },
            icon = Icons.Filled.AddCircle,
        )
    }

    if (pickingCard) {
        CardPickerSheet(
            cards = AppData.cards.toList(),
            onPick = {
                cardId = it.id
                pickingCard = false
            },
            onDismiss = { pickingCard = false },
        )
    }
}

@Composable
fun PayBillScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val scope = rememberCoroutineScope()
    var processing by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<PaymentOutcome?>(null) }

    fun startPayment(bill: Bill) {
        RazorpayCheckout.open(activity, bill.amount.toDouble(), bill.name) { ok ->
            if (ok) {
                processing = true
                scope.launch {
                    val started = System.currentTimeMillis()
                    val result = try {
                        AppData.recordBillPayment(bill)
                        PaymentOutcome(
                            true,
                            context.getString(R.string.title_bill_ok),
                            context.getString(R.string.bill_success, bill.name, formatInr(bill.amount.toDouble(), paise = true)),
                        )
                    } catch (e: Exception) {
                        PaymentOutcome(false, context.getString(R.string.title_payment_failed), context.getString(R.string.save_failed))
                    }
                    awaitMinimum(started)
                    outcome = result
                    processing = false
                }
            } else {
                outcome = PaymentOutcome(false, context.getString(R.string.title_payment_failed), context.getString(R.string.bill_failed))
            }
        }
    }

    if (processing) {
        PaymentProcessingScreen()
        return
    }
    val current = outcome
    if (current != null) {
        PaymentResultScreen(outcome = current) {
            outcome = null
            if (current.success) onBack()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenTitle(title = stringResource(R.string.pay_bill), subtitle = stringResource(R.string.pay_bill_subtitle), onBack = onBack)
        }
        item {
            QCardBox(color = QMint, bordered = false, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.next_bill_due), fontSize = 12.sp, color = QGreenInk, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCircle(Icons.Filled.Bolt, background = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.bill_line, AppData.summary.nextBillName, formatInr(AppData.summary.nextBillAmount)),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = QInk,
                    )
                }
            }
        }
        items(AppData.bills) { bill ->
            QCardBox(modifier = Modifier.fillMaxWidth().clickable { startPayment(bill) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCircle(billIcon(bill.name))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bill.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = QInk)
                        Text(stringResource(R.string.bill_line, formatInr(bill.amount), bill.due), fontSize = 12.sp, color = QMuted)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = QMuted)
                }
            }
        }
        item {
            PrimaryButton(
                text = stringResource(R.string.pay_now),
                onClick = { AppData.bills.firstOrNull()?.let { startPayment(it) } },
                icon = Icons.Filled.Receipt,
            )
        }
    }
}

private fun billIcon(name: String): ImageVector = when {
    name.contains("Mobile", ignoreCase = true) -> Icons.Filled.PhoneAndroid
    name.contains("Broadband", ignoreCase = true) -> Icons.Filled.Laptop
    else -> Icons.Filled.AccountBalance
}
