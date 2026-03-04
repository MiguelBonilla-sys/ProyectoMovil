package com.example.proyecto

import androidx.compose.runtime.Composable
import com.example.proyecto.navigation.AppNavGraph
import com.example.proyecto.ui.theme.LexSignTheme

@Composable
fun App() {
    LexSignTheme {
        AppNavGraph()
    }
}