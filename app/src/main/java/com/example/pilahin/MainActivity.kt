package com.example.pilahin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pilahin.navigation.AppNavigation
import com.example.pilahin.ui.theme.PilahinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PilahinTheme {
                AppNavigation()
            }
        }
    }
}