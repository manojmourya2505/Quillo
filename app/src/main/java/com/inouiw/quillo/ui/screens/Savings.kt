package com.inouiw.quillo.ui.screens

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.clickable


import androidx.compose.foundation.layout.navigationBarsPadding

import kotlinx.coroutines.launch

import android.widget.Toast

import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue

import androidx.compose.runtime.getValue

import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.ModalBottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.data.Goal
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.formatInr
import com.inouiw.quillo.ui.components.IconCircle
import com.inouiw.quillo.ui.components.PrimaryButton
import com.inouiw.quillo.ui.components.ScreenTitle
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreen
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QOutline
import com.inouiw.quillo.ui.theme.QSurface
import com.inouiw.quillo.ui.theme.QTonal
import kotlin.math.roundToInt

@Composable
fun SavingsScreen() {
    var showNewGoal by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<PaymentOutcome?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var addingTo by remember { mutableStateOf<Goal?>(null) }

    fun saveMoney(goal: Goal, amount: Double) {
        addingTo = null
        processing = true
        scope.launch {
            val started = System.currentTimeMillis()
            val result = try {
                AppData.addToGoal(goal, amount)
                PaymentOutcome(
                    true,
                    context.getString(R.string.title_money_ok),
                    context.getString(R.string.goal_money_success, goal.name, formatInr(amount, paise = true)),
                )
            } catch (e: Exception) {
                PaymentOutcome(false, context.getString(R.string.title_money_failed), context.getString(R.string.goal_money_failed))
            }
            awaitMinimum(started)
            outcome = result
            processing = false
        }
    }

    fun saveGoal(name: String, target: Double) {
        showNewGoal = false
        processing = true
        scope.launch {
            val started = System.currentTimeMillis()
            val result = try {
                AppData.addGoal(name, target)
                PaymentOutcome(
                    true,
                    context.getString(R.string.title_goal_ok),
                    context.getString(R.string.goal_success, name, formatInr(target)),
                )
            } catch (e: Exception) {
                PaymentOutcome(false, context.getString(R.string.title_goal_failed), context.getString(R.string.goal_failed))
            }
            awaitMinimum(started)
            outcome = result
            processing = false
        }
    }

    if (processing) {
        PaymentProcessingScreen()
        return
    }
    val current = outcome
    if (current != null) {
        PaymentResultScreen(outcome = current) { outcome = null }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenTitle(
                title = stringResource(R.string.savings),
                subtitle = stringResource(R.string.goals_and_pots),
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(QMint)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Savings, contentDescription = null, tint = QGreenInk, modifier = Modifier.size(22.dp))
                    Text(stringResource(R.string.saved_this_year), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = QGreenInk)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    formatInr(AppData.summary.savedThisYear, paise = true),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    color = QInk,
                )
            }
        }
        items(AppData.goals) { goal ->
            GoalCard(goal, onAddMoney = { addingTo = goal })
        }
        item {
            PrimaryButton(text = stringResource(R.string.new_goal), onClick = { showNewGoal = true }, icon = Icons.Filled.Add)
            if (showNewGoal) {
                NewGoalSheet(onDismiss = { showNewGoal = false }, onSave = { name, target -> saveGoal(name, target) })
            }
            addingTo?.let { goal ->
                AddMoneySheet(goal = goal, onDismiss = { addingTo = null }, onSave = { amount -> saveMoney(goal, amount) })
            }
        }
    }
}

@Composable
private fun GoalCard(goal: Goal, onAddMoney: () -> Unit) {
    val fraction = (goal.saved / goal.target).toFloat().coerceIn(0f, 1f)
    val percent = (fraction * 100).roundToInt()
    val icon: ImageVector = when (goal.name) {
        "Goa trip" -> Icons.Filled.Flight
        "Emergency fund" -> Icons.Filled.VerifiedUser
        else -> Icons.Filled.Laptop
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(QCard)
            .border(1.dp, QOutline, RoundedCornerShape(24.dp))
            .clickable(onClick = onAddMoney)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconCircle(icon, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = QInk)
                Text(
                    stringResource(R.string.goal_progress, formatInr(goal.saved), formatInr(goal.target)),
                    fontSize = 12.sp,
                    color = QMuted,
                )
            }
            Text(stringResource(R.string.percent, percent), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = QGreenInk)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = QGreen,
            trackColor = QTonal,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewGoalSheet(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = QSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.new_goal), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = QInk)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.goal_name)) },
                placeholder = { Text(stringResource(R.string.goal_name_hint)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = target,
                onValueChange = { target = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.goal_target)) },
                placeholder = { Text(stringResource(R.string.goal_target_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            PrimaryButton(
                text = stringResource(R.string.goal_save),
                onClick = {
                    val amount = target.toDoubleOrNull() ?: 0.0
                    when {
                        name.isBlank() -> Toast.makeText(context, context.getString(R.string.goal_need_name), Toast.LENGTH_SHORT).show()
                        amount <= 0.0 -> Toast.makeText(context, context.getString(R.string.goal_need_target), Toast.LENGTH_SHORT).show()
                        else -> onSave(name.trim(), amount)
                    }
                },
                icon = Icons.Filled.Add,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMoneySheet(goal: Goal, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var digits by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = QSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(goal.name, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = QInk)
            Text(
                stringResource(R.string.available_amount, formatInr(AppData.summary.availableDebit, paise = true)),
                fontSize = 13.sp,
                color = QMuted,
            )
            OutlinedTextField(
                value = digits,
                onValueChange = { digits = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.amount)) },
                placeholder = { Text(stringResource(R.string.enter_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            PrimaryButton(
                text = stringResource(R.string.add_money),
                onClick = {
                    val amount = digits.toDoubleOrNull() ?: 0.0
                    when {
                        amount <= 0.0 -> Toast.makeText(context, context.getString(R.string.send_need_amount), Toast.LENGTH_SHORT).show()
                        amount > AppData.summary.availableDebit -> Toast.makeText(context, context.getString(R.string.send_insufficient), Toast.LENGTH_SHORT).show()
                        else -> onSave(amount)
                    }
                },
                icon = Icons.Filled.Add,
            )
        }
    }
}
