package com.example.fourthofficial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fourthofficial.ui.theme.FourthOfficialTheme
import com.example.fourthofficial.ui.AppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FourthOfficialTheme {
                AppRoot()
            }
        }
    }
}