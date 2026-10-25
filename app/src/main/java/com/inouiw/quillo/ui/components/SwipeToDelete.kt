package com.inouiw.quillo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.quillo.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val DeleteBg = Color(0xFFFBE9E7)
private val DeleteFg = Color(0xFFB3261E)

/**
 * Swipe the card left to reveal a soft Delete button. The button fades and grows
 * in as the card moves, and tapping it calls [onDelete]. Tapping the card closes it.
 */
@Composable
fun SwipeToDeleteCard(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val actionWidth = 104.dp
    val actionPx = with(density) { actionWidth.toPx() }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val reveal = (-offset.value / actionPx).coerceIn(0f, 1f)

    fun settle(open: Boolean) {
        scope.launch {
            offset.animateTo(
                targetValue = if (open) -actionPx else 0f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(actionWidth - 16.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        alpha = reveal
                        scaleX = 0.85f + 0.15f * reveal
                        scaleY = 0.85f + 0.15f * reveal
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(DeleteBg)
                    .clickable(onClick = onDelete),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = DeleteFg)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.delete), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeleteFg)
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .clickable(enabled = reveal > 0f, onClick = { settle(false) })
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offset.snapTo((offset.value + delta).coerceIn(-actionPx, 0f)) }
                    },
                    onDragStopped = { settle(offset.value < -actionPx / 3f) },
                ),
        ) {
            content()
        }
    }
}
