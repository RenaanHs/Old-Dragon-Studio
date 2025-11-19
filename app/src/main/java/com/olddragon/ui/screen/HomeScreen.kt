package com.olddragon.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tela inicial do aplicativo Old Dragon
 * Oferece três opções principais: Criar Personagem, Selecionar Personagem, Batalha
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateCharacter: () -> Unit,
    onSelectCharacter: () -> Unit,
    onBattle: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    Scaffold(
        containerColor = Color(0xFF1A1A2E)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header com título
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1000)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(800)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        // Ícone do dragão
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Old Dragon",
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFFE94560)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "OLD DRAGON",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp,
                                letterSpacing = 4.sp
                            ),
                            color = Color(0xFFE94560),
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Sistema de RPG",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF9BA4B4),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Botões principais
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedMenuButton(
                        visible = showContent,
                        delay = 200,
                        icon = Icons.Default.Add,
                        title = "Criar Personagem",
                        description = "Crie um novo herói para suas aventuras",
                        onClick = onCreateCharacter,
                        color = Color(0xFF4CAF50)
                    )
                    
                    AnimatedMenuButton(
                        visible = showContent,
                        delay = 400,
                        icon = Icons.Default.List,
                        title = "Selecionar Personagem",
                        description = "Escolha um personagem existente",
                        onClick = onSelectCharacter,
                        color = Color(0xFF2196F3)
                    )
                    
                    AnimatedMenuButton(
                        visible = showContent,
                        delay = 600,
                        icon = Icons.Default.PlayArrow,
                        title = "Batalha",
                        description = "Inicie um combate épico",
                        onClick = onBattle,
                        color = Color(0xFFE94560)
                    )
                }
                
                // Footer
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 800))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Versão 1.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9BA4B4).copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF9BA4B4).copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Que os dados estejam a seu favor",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9BA4B4).copy(alpha = 0.6f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botão animado do menu principal
 */
@Composable
fun AnimatedMenuButton(
    visible: Boolean,
    delay: Int,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    color: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600, delayMillis = delay)) + 
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(600, delayMillis = delay)
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Button(
                onClick = {
                    isPressed = true
                    onClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Ícone
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(32.dp),
                            tint = color
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // Textos
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9BA4B4)
                        )
                    }
                    
                    // Seta
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
