package com.mediqueue.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mediqueue.app.navigation.AppNavigation
import com.mediqueue.app.ui.theme.MediQueueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediQueueTheme {
                AppNavigation()
            }
        }
    }
}