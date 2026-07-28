package com.example.farmhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.farmhelper.api.RetrofitClient
import com.example.farmhelper.ui.navigation.AppFlow
import com.example.farmhelper.ui.theme.FarmHelperTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(applicationContext)

        enableEdgeToEdge()

        setContent {
            FarmHelperTheme {
                com.example.farmhelper.ui.localization.LocalizedApp(context = this) {
                    AppFlow()
                }
            }
        }
    }
}