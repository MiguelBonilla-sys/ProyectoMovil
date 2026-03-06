package com.example.proyecto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.proyecto.data.repository.UserRepository
import com.example.proyecto.navigation.AppNavGraph
import com.example.proyecto.ui.theme.LexSignTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import proyecto.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    LaunchedEffect(Unit) {
        val seedJson = Res.readBytes("files/users.json").decodeToString()
        UserRepository.init(seedJson)
    }
    LexSignTheme {
        AppNavGraph()
    }
}