package com.omni.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omni.ai.ChatViewModel
import com.omni.ai.ai.ChatMessage
import com.omni.ai.ui.components.AiOrb
import com.omni.ai.ui.theme.OmniBg
import com.omni.ai.ui.theme.OmniCyan
import com.omni.ai.ui.theme.OmniError
import com.omni.ai.ui.theme.OmniMuted
import com.omni.ai.ui.theme.OmniSurface
import com.omni.ai.ui.theme.OmniText
import com.omni.ai.ui.theme.OmniViolet

@Composable
fun HomeScreen(vm: ChatViewModel, onOpenSettings: () -> Unit) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "∞ OmNi AI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OmniCyan,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OmniText)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AiOrb(active = busy)
            Text(
                text = when {
                    settings.apiKey.isBlank() -> "API key सेट नाही. Settings उघडा."
                    settings.model.isBlank() -> "मॉडेल निवडलेले नाही."
                    else -> "मॉडेल: ${settings.model}"
                },
                color = OmniMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (messages.isEmpty()) {
                item {
                    Text(
                        text = "नमस्कार! मी ${settings.companionName} आहे. काय बोलायचं?",
                        color = OmniViolet,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
            items(messages) { msg -> Bubble(msg) }
            if (busy) {
                item {
                    Text("विचार करत आहे…", color = OmniCyan, modifier = Modifier.padding(8.dp))
                }
            }
        }

        error?.let { msg ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = msg,
                    color = OmniError,
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                )
                TextButton(onClick = { vm.clearError() }) { Text("बंद") }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("इथे लिहा…") },
                maxLines = 4,
                shape = RoundedCornerShape(20.dp),
                enabled = !busy,
            )
            IconButton(
                onClick = {
                    vm.send(input)
                    input = ""
                },
                enabled = input.isNotBlank() && !busy,
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "पाठवा",
                    tint = if (input.isNotBlank() && !busy) OmniCyan else OmniMuted,
                )
            }
        }
    }
}

@Composable
private fun Bubble(msg: ChatMessage) {
    val isUser = msg.role == ChatMessage.Role.USER
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Text(
            text = msg.text,
            color = OmniText,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isUser) OmniViolet.copy(alpha = 0.25f) else OmniSurface)
                .padding(12.dp),
        )
    }
}
