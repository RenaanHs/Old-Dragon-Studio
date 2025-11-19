package com.olddragon

/* android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.olddragon.controller.CharacterController
import com.olddragon.ui.screen.CharacterCreationScreen
import com.olddragon.ui.theme.OldDragonTheme

class MainActivity : ComponentActivity() {
    private val controller = CharacterController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OldDragonTheme {
                CharacterCreationScreen(controller)
            }
        }
    }
}*/

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.olddragon.ui.navigation.AppNavigation
import com.olddragon.ui.theme.OldDragonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OldDragonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // 🔥 USA O SISTEMA DE NAVEGAÇÃO
                }
            }
        }
    }
}
