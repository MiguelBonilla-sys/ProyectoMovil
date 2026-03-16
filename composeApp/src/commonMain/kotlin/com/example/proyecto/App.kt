package com.example.proyecto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.proyecto.data.AppContainer
import com.example.proyecto.data.model.User
import com.example.proyecto.navigation.AppNavGraph
import com.example.proyecto.ui.theme.LexSignTheme
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import proyecto.composeapp.generated.resources.Res

private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    LaunchedEffect(Unit) {
        // Load seed data from resources
        val seedJson = Res.readBytes("files/users.json").decodeToString()
        val seedUsers = json.decodeFromString<List<User>>(seedJson)
        
        // Initialize repository with seed data
        AppContainer.userRepository.init(seedUsers)
    }
    
    LexSignTheme {
        AppNavGraph()
    }
}