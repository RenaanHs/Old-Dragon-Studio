package com.olddragon.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olddragon.model.Personagem
import com.olddragon.ui.theme.*
import com.olddragon.viewmodel.PersonagemViewModel

@Composable
fun PersonagensListScreen(
    viewModel: PersonagemViewModel = viewModel(),
    onPersonagemClick: (Personagem) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onCreateNew: () -> Unit = {},
    onBattle: (Personagem) -> Unit = {}
) {
    val personagens by viewModel.personagens.collectAsState()
    var personagemParaExcluir by remember { mutableStateOf<Personagem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog de confirmação de exclusão
    if (showDeleteDialog && personagemParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Exclusão", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente excluir ${personagemParaExcluir?.nome}? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        personagemParaExcluir?.let { viewModel.deletarPersonagem(it) }
                        showDeleteDialog = false
                        personagemParaExcluir = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GryffindorRed
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    personagemParaExcluir = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Cabeçalho com botão voltar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = GryffindorGold
                )
            }

            Text(
                "PERSONAGENS SALVOS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = GryffindorRed
                )
            )

            IconButton(onClick = onCreateNew) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Criar Novo",
                    tint = GryffindorGold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Lista de personagens
        if (personagens.isEmpty()) {
            // Estado vazio
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = ScrollParchment
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🐉",
                        fontSize = 72.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Nenhum personagem criado ainda",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GryffindorRed
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Crie seu primeiro herói para começar a aventura!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Lista com personagens
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(personagens) { personagem ->
                    PersonagemCard(
                        personagem = personagem,
                        onClick = { onPersonagemClick(personagem) },
                        onDelete = {
                            personagemParaExcluir = personagem
                            showDeleteDialog = true
                        },
                        onBattle = { onBattle(personagem) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Informações de contagem
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GryffindorGold.copy(alpha = 0.2f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GryffindorGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "⚔️ Total de heróis criados: ${personagens.size}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = GryffindorRed
                    )
                }
            }
        }
    }
}

@Composable
fun PersonagemCard(
    personagem: Personagem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onBattle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = ScrollParchment
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MedievalBrown.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Ícone da raça
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            GryffindorGold.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when(personagem.raca.nome) {
                            "Elfo" -> "🧝"
                            "Anão" -> "⛏️"
                            "Halfling" -> "🌾"
                            else -> "👤"
                        },
                        fontSize = 32.sp
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Informações do personagem
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        personagem.nome,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = GryffindorRed
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🏰 ${personagem.raca.nome}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "⚔️ ${personagem.classe.nome}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Resumo de atributos
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AtributoChip("FOR", personagem.atributos.forca)
                        AtributoChip("DES", personagem.atributos.destreza)
                        AtributoChip("CON", personagem.atributos.constituicao)
                    }
                }

                // Ícone da classe
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            GryffindorRed.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when(personagem.classe.nome) {
                            "Guerreiro" -> "⚔️"
                            "Mago" -> "🔮"
                            "Clérigo" -> "✝️"
                            "Ladino" -> "🗡️"
                            else -> "⚔️"
                        },
                        fontSize = 32.sp
                    )
                }
            }

            // Botões de ação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão Batalhar
                Button(
                    onClick = onBattle,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GryffindorRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Batalhar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Batalhar", fontSize = 14.sp)
                }

                // Botão Excluir
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(0.4f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GryffindorRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GryffindorRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AtributoChip(nome: String, valor: Int) {
    Box(
        modifier = Modifier
            .background(
                GryffindorGold.copy(alpha = 0.3f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            "$nome: $valor",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
