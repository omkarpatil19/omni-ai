package com.omni.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.omni.ai.ChatViewModel
import com.omni.ai.data.AppSettings
import com.omni.ai.ui.theme.OmniBg
import com.omni.ai.ui.theme.OmniCyan
import com.omni.ai.ui.theme.OmniError
import com.omni.ai.ui.theme.OmniMuted
import com.omni.ai.ui.theme.OmniText
import com.omni.ai.ui.theme.OmniViolet
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: ChatViewModel, onBack: () -> Unit) {
    val saved by vm.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var apiKey by remember(saved) { mutableStateOf(saved.apiKey) }
    var companion by remember(saved) { mutableStateOf(saved.companionName) }
    var model by remember(saved) { mutableStateOf(saved.model) }
    var temperature by remember(saved) { mutableFloatStateOf(saved.temperature) }
    var reveal by remember { mutableStateOf(false) }
    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var saveNote by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "मागे", tint = OmniText)
            }
            Text("Settings", style = MaterialTheme.typography.titleLarge, color = OmniCyan, fontWeight = FontWeight.Bold)
        }

        Text("Gemini API", style = MaterialTheme.typography.titleMedium, color = OmniViolet)

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it.trim() },
            label = { Text("Gemini API key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { reveal = !reveal }) {
                    Text(if (reveal) "लपवा" else "दाखवा")
                }
            },
        )

        Text(
            "Key फक्त या फोनवर साठवली जाते. ती कोणालाही पाठवू नका.",
            style = MaterialTheme.typography.bodySmall,
            color = OmniMuted,
        )

        Button(
            onClick = {
                scope.launch {
                    testing = true
                    val result = vm.fetchModels(apiKey)
                    testing = false
                    result
                        .onSuccess { list ->
                            models = list
                            if (model !in list) model = list.firstOrNull().orEmpty()
                            testResult = true to "✓ जोडणी यशस्वी. ${list.size} मॉडेल्स मिळाले."
                        }
                        .onFailure { e ->
                            models = emptyList()
                            testResult = false to (e.message ?: "अज्ञात त्रुटी")
                        }
                }
            },
            enabled = !testing && apiKey.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (testing) "तपासत आहे…" else "जोडणी तपासा (Test connection)")
        }

        testResult?.let { (ok, msg) ->
            Text(msg, color = if (ok) OmniCyan else OmniError)
        } ?: Text("अद्याप जोडणी तपासलेली नाही.", color = OmniMuted, style = MaterialTheme.typography.bodySmall)

        if (models.isNotEmpty()) {
            Text("मॉडेल निवडा", style = MaterialTheme.typography.titleMedium, color = OmniViolet)
            models.forEach { name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { model = name }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = name == model, onClick = { model = name })
                    Text(name, color = OmniText)
                }
            }
        } else if (model.isNotBlank()) {
            Text("सध्या निवडलेले मॉडेल: $model", color = OmniMuted)
        }

        Text("व्यक्तिमत्त्व", style = MaterialTheme.typography.titleMedium, color = OmniViolet)

        OutlinedTextField(
            value = companion,
            onValueChange = { companion = it.take(30) },
            label = { Text("सहाय्यकाचे नाव") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("उत्तरातील बदल (temperature): ${"%.2f".format(temperature)}", color = OmniText)
        Slider(value = temperature, onValueChange = { temperature = it }, valueRange = 0f..1f)

        Button(
            onClick = {
                scope.launch {
                    vm.saveSettings(
                        AppSettings(
                            apiKey = apiKey,
                            model = model,
                            temperature = temperature,
                            companionName = companion.trim().ifBlank { "OmNi" },
                        )
                    )
                    saveNote = "सेव्ह झाले ✓"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("सेव्ह करा")
        }

        saveNote?.let { Text(it, color = OmniCyan) }

        TextButton(onClick = { vm.clearChat() }) {
            Text("चॅट साफ करा", color = OmniError)
        }
    }
}
