package com.vanta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vanta.app.ui.navigation.VantaNavHost
import com.vanta.app.ui.theme.VantaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as VantaApplication).container

        setContent {
            VantaTheme {
                VantaNavHost(container = container)
            }
        }
    }
}
