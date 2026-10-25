package com.inouiw.quillo.ui.screens

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.inouiw.quillo.data.AgentApi
import com.inouiw.quillo.data.formatInr
import com.inouiw.quillo.ui.components.ScreenTitle
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QOutline
import com.inouiw.quillo.ui.theme.QSurface
import com.inouiw.quillo.ui.theme.QTonal

private data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
    val breakdown: List<Pair<String, Double>> = emptyList(),
)

@Composable
fun AskScreen(onBack: () -> Unit) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var draft by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val errorText = stringResource(R.string.ask_error)

    fun send(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || busy) return
        val history = messages.takeLast(10).map {
            AgentApi.Turn(if (it.fromUser) "user" else "assistant", it.text)
        }
        messages.add(ChatMessage(clean, fromUser = true))
        draft = ""
        busy = true
        scope.launch {
            val reply = try {
                AgentApi.ask(clean, history)
            } catch (e: Exception) {
                errorText
            }
            messages.add(ChatMessage(reply, fromUser = false))
            busy = false
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    ) {
        ScreenTitle(title = stringResource(R.string.ask_quillo), subtitle = stringResource(R.string.ask_subtitle), onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 14.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(messages) { _, message ->
                ChatBubble(message)
            }
        }

        if (busy) {
            Text(stringResource(R.string.ask_thinking), fontSize = 13.sp, color = QMuted, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(10.dp))

        InputBar(
            value = draft,
            onValueChange = { draft = it },
            onSend = { send(draft) },
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    if (message.fromUser) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                message.text,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(QInk)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color.White,
                fontSize = 15.sp,
            )
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(QCard)
                    .border(1.dp, QOutline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = QGreenInk,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(stringResource(R.string.app_name), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = QGreenInk)
                }
                Text(message.text, fontSize = 15.sp, color = QInk)
                if (message.breakdown.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(QTonal)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        message.breakdown.forEach { (label, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = QInk)
                                Text(formatInr(amount), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = QInk)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(QCard)
            .border(1.dp, QOutline, RoundedCornerShape(999.dp))
            .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, color = QInk),
            cursorBrush = SolidColor(QInk),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(stringResource(R.string.ask_placeholder), fontSize = 15.sp, color = QMuted)
                }
                inner()
            },
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(QInk)
                .clickable(onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send), tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
