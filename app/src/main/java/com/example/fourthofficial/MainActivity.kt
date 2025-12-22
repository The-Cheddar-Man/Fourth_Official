package com.example.fourthofficial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fourthofficial.ui.AppRoot
import com.example.fourthofficial.ui.theme.FourthOfficialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FourthOfficialTheme {
                AppRoot()
                // BitBranch test
            }
        }
    }
}