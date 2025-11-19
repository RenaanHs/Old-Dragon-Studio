package com.olddragon.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olddragon.model.combate.*
import com.olddragon.viewmodel.CombateViewModel
import com.olddragon.viewmodel.EstadoCombateUI
import com.olddragon.viewmodel.PersonagemViewModel
import kotlinx.coroutines.launch

@Composable
fun CombateScreen(
    personagemNome: String = "",
    combateViewModel: CombateViewModel = viewModel(),
    personagemViewModel: PersonagemViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val estadoUI by combateViewModel.estadoUI.collectAsState()
    val historicoRecente by combateViewModel.historicoRecente.collectAsState()
    val personagens by personagemViewModel.personagens.collectAsState()
    
    // Busca o personagem selecionado
    val personagemSelecionado = personagens.find { it.nome == personagemNome }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
            .padding(16.dp)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFe94560)
                )
            ) {
                Text("← Voltar")
            }
            
            Text(
                text = "⚔️ Sistema de Combate",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFffd700)
            )
            
            Spacer(modifier = Modifier.width(80.dp))
        }
        
        when (val estado = estadoUI) {
            is EstadoCombateUI.SemCombate -> {
                TelaInicial(personagemSelecionado, combateViewModel)
            }
            is EstadoCombateUI.EmCombate -> {
                TelaCombate(estado.combate, historicoRecente, combateViewModel, onNavigateBack)
            }
        }
    }
}

@Composable
fun TelaInicial(
    personagemSelecionado: com.olddragon.model.Personagem?,
    viewModel: CombateViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213e)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (personagemSelecionado != null) {
                    Text(
                        text = "Personagem Selecionado",
                        fontSize = 18.sp,
                        color = Color(0xFFffd700),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = personagemSelecionado.nome,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "${personagemSelecionado.raca.nome} ${personagemSelecionado.classe.nome}",
                        fontSize = 16.sp,
                        color = Color(0xFFaaaaaa),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Divider(
                        color = Color(0xFF0f3460),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                
                Text(
                    text = if (personagemSelecionado != null) {
                        "Escolha o tipo de combate"
                    } else {
                        "Nenhum personagem selecionado"
                    },
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
                
                if (personagemSelecionado != null) {
                    Button(
                        onClick = { viewModel.iniciarCombateComPersonagem(personagemSelecionado, 2) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFe94560)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚔️ Combate Simples (vs 2 inimigos)", fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { viewModel.iniciarCombateComPersonagem(personagemSelecionado, 4) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFff6b6b)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔥 Combate Épico (vs 4 inimigos)", fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "ou",
                        fontSize = 14.sp,
                        color = Color(0xFFaaaaaa),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Button(
                    onClick = { viewModel.iniciarCombateTeste(2, 2, 1, 1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4a5568)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎲 Combate Teste (sem personagem)", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TelaCombate(
    combate: Combate,
    historicoRecente: List<EventoCombate>,
    viewModel: CombateViewModel,
    onNavigateBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll quando há novos eventos
    LaunchedEffect(historicoRecente.size) {
        if (historicoRecente.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Informações do Combate
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213e)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Rodada ${combate.rodadaAtual}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFffd700)
                    )
                    Text(
                        text = combate.fase.name,
                        fontSize = 14.sp,
                        color = Color(0xFFaaaaaa)
                    )
                }
                
                if (combate.fase == FaseCombate.FINALIZADO && combate.vencedor != null) {
                    Text(
                        text = when (combate.vencedor) {
                            LadoCombate.ALIADOS -> "🏆 VITÓRIA!"
                            LadoCombate.INIMIGOS -> "💀 DERROTA"
                            else -> "⚔️ EMPATE"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (combate.vencedor) {
                            LadoCombate.ALIADOS -> Color(0xFF00ff00)
                            LadoCombate.INIMIGOS -> Color(0xFFff0000)
                            else -> Color(0xFFffff00)
                        }
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Coluna Aliados
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "👥 ALIADOS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00ff00),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(combate.combatentesAliados) { combatente ->
                        CombatenteCard(combatente, isAliado = true)
                    }
                }
            }
            
            // Coluna Inimigos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "💀 INIMIGOS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFff0000),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(combate.combatentesInimigos) { combatente ->
                        CombatenteCard(combatente, isAliado = false)
                    }
                }
            }
        }
        
        // Histórico de eventos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0f3460)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "📜 Histórico de Eventos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFffd700),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(historicoRecente.reversed()) { evento ->
                        EventoItem(evento)
                    }
                }
            }
        }
    }
}

@Composable
fun CombatenteCard(combatente: Combatente, isAliado: Boolean) {
    val corFundo = if (isAliado) Color(0xFF1a4d2e) else Color(0xFF4d1a1a)
    val corBorda = if (combatente.estaVivo()) {
        if (isAliado) Color(0xFF00ff00) else Color(0xFFff0000)
    } else {
        Color.Gray
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, corBorda, RoundedCornerShape(8.dp))
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = corFundo
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = combatente.personagem.nome,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = when {
                        !combatente.estaVivo() -> "💀"
                        combatente.estaMorrendo() -> "🩹"
                        else -> when (combatente.ordemIniciativa) {
                            OrdemIniciativa.SUCESSO -> "⚡"
                            OrdemIniciativa.FALHA -> "🐌"
                            else -> ""
                        }
                    },
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Barra de PV
            val percentualPV = (combatente.pontosVida.toFloat() / combatente.pontosVidaMaximo.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.DarkGray, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentualPV)
                        .fillMaxHeight()
                        .background(
                            when {
                                percentualPV > 0.5f -> Color(0xFF00ff00)
                                percentualPV > 0.25f -> Color(0xFFffaa00)
                                else -> Color(0xFFff0000)
                            },
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Text(
                text = "PV: ${combatente.pontosVida}/${combatente.pontosVidaMaximo}",
                fontSize = 11.sp,
                color = Color(0xFFcccccc),
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Text(
                text = "CA: ${combatente.classeArmadura} | ${combatente.arma.nome}",
                fontSize = 10.sp,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
fun EventoItem(evento: EventoCombate) {
    val cor = when (evento) {
        is EventoCombate.Ataque -> if (evento.acertou) Color(0xFFffaa00) else Color.Gray
        is EventoCombate.Dano -> Color(0xFFff6666)
        is EventoCombate.Morte -> Color(0xFFff0000)
        is EventoCombate.InicioCombate -> Color(0xFF00ff00)
        is EventoCombate.FimCombate -> Color(0xFFffd700)
        is EventoCombate.Iniciativa -> if (evento.resultado) Color(0xFF00ff00) else Color(0xFFffaa00)
        else -> Color.White
    }
    
    Text(
        text = "• ${evento.descricao}",
        fontSize = 12.sp,
        color = cor,
        lineHeight = 16.sp
    )
}
