package com.olddragon

import android.os.Bundle
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
}
