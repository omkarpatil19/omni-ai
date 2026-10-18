package com.omni.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.omni.ai.ui.OmniAppRoot
import com.omni.ai.ui.theme.OmniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OmniTheme {
                OmniAppRoot()
            }
        }
    }
}
