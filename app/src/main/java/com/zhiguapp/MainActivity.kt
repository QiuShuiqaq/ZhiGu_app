package com.zhiguapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zhiguapp.ui.ZhiGuApp
import com.zhiguapp.ui.theme.ZhiGuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZhiGuTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ZhiGuApp()
                }
            }
        }
    }
}
