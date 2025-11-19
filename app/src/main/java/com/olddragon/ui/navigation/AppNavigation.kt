package com.olddragon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.olddragon.controller.CharacterController
import com.olddragon.ui.screen.CharacterCreationScreen
import com.olddragon.ui.screen.CombateScreen
import com.olddragon.ui.screen.HomeScreen
import com.olddragon.ui.screen.PersonagensListScreen

/**
 * Rotas de navegação do app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CharacterCreation : Screen("character_creation")
    object CharacterList : Screen("character_list")
    object Combat : Screen("combat/{personagemNome}") {
        fun createRoute(personagemNome: String) = "combat/$personagemNome"
    }
}

/**
 * Configuração de navegação do app
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val controller = CharacterController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Tela inicial (Home)
        composable(Screen.Home.route) {
            HomeScreen(
                onCreateCharacter = {
                    navController.navigate(Screen.CharacterCreation.route)
                },
                onSelectCharacter = {
                    navController.navigate(Screen.CharacterList.route)
                },
                onBattle = {
                    navController.navigate(Screen.CharacterList.route)
                }
            )
        }
        
        // Tela de criação de personagem
        composable(Screen.CharacterCreation.route) {
            CharacterCreationScreen(
                controller = controller,
                onNavigateToList = {
                    navController.navigate(Screen.CharacterList.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Tela de lista de personagens
        composable(Screen.CharacterList.route) {
            PersonagensListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCreateNew = {
                    navController.navigate(Screen.CharacterCreation.route)
                },
                onBattle = { personagem ->
                    navController.navigate(Screen.Combat.createRoute(personagem.nome))
                }
            )
        }
        
        // Tela de combate
        composable(
            route = Screen.Combat.route,
            arguments = listOf(
                navArgument("personagemNome") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val personagemNome = backStackEntry.arguments?.getString("personagemNome") ?: ""
            CombateScreen(
                personagemNome = personagemNome,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
