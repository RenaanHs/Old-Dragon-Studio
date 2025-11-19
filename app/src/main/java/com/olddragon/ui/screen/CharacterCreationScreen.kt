package com.olddragon.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olddragon.controller.CharacterController
import com.olddragon.model.Personagem
import com.olddragon.model.atributos.Atributos
import com.olddragon.service.Dado
import com.olddragon.ui.components.DropdownMenuBox
import com.olddragon.ui.components.AtributoSlider
import com.olddragon.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olddragon.viewmodel.PersonagemViewModel
import com.olddragon.viewmodel.SalvamentoStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    controller: CharacterController,
    viewModel: PersonagemViewModel = viewModel(),
    onNavigateToList: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var nome by remember { mutableStateOf("") }

    // Estilo de criação
    val estilos = listOf("Clássico", "Aventureiro", "Heróico")
    var estiloSelecionado by remember { mutableStateOf(estilos.first()) }

    // Listas de opções
    val racas = listOf("Humano", "Elfo", "Anão", "Halfling")
    val classes = listOf("Guerreiro", "Mago", "Clérigo", "Ladino")

    var racaSelecionada by remember { mutableStateOf(racas.first()) }
    var classeSelecionada by remember { mutableStateOf(classes.first()) }

    // Atributos
    var forca by remember { mutableStateOf(10f) }
    var destreza by remember { mutableStateOf(10f) }
    var constituicao by remember { mutableStateOf(10f) }
    var inteligencia by remember { mutableStateOf(10f) }
    var sabedoria by remember { mutableStateOf(10f) }
    var carisma by remember { mutableStateOf(10f) }

    // Estados para rolagem de dados
    var valoresRolados by remember { mutableStateOf<List<Int>>(emptyList()) }
    var mostrarDistribuicao by remember { mutableStateOf(false) }
    var atributosDistribuidos by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var logRolagem by remember { mutableStateOf<List<String>>(emptyList()) }

    // Estado do personagem criado
    var personagemCriado by remember { mutableStateOf<Personagem?>(null) }

    // Observe o status de salvamento
    val salvamentoStatus by viewModel.salvamentoStatus.collectAsState()

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabeçalho épico
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = GryffindorGold,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GryffindorRed,
                                GryffindorBurgundy
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🐉 CRIAÇÃO DE PERSONAGEM\nOLD DRAGON 🗡️",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.2.sp
                    ),
                    color = GryffindorLightGold
                )
            }
        }
        }

        Spacer(Modifier.height(20.dp))

        // Campo Nome com estilo pergaminho
        PergaminhoCard {
            Column {
                Text(
                    "📜 NOME DO HERÓI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GryffindorRed
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    placeholder = { Text("Digite o nome do personagem", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GryffindorGold,
                        unfocusedBorderColor = MedievalBrown,
                        focusedLabelColor = GryffindorRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Seleção de estilo com visual medieval
        PergaminhoCard {
            Column {
                Text(
                    "⚔️ ESTILO DE GERAÇÃO DOS ATRIBUTOS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GryffindorRed
                    )
                )
                Spacer(Modifier.height(12.dp))

                estilos.forEach { estilo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (estiloSelecionado == estilo)
                                GryffindorGold.copy(alpha = 0.2f)
                            else
                                Color.Transparent
                        ),
                        border = if (estiloSelecionado == estilo)
                            androidx.compose.foundation.BorderStroke(2.dp, GryffindorGold)
                        else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = estiloSelecionado == estilo,
                                onClick = {
                                    estiloSelecionado = estilo
                                    valoresRolados = emptyList()
                                    mostrarDistribuicao = false
                                    atributosDistribuidos = emptyMap()
                                    logRolagem = emptyList()
                                    personagemCriado = null
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GryffindorGold,
                                    unselectedColor = MedievalBrown
                                )
                            )
                            Column {
                                Text(
                                    estilo,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    when(estilo) {
                                        "Clássico" -> "3d6 na ordem dos atributos (modo tradicional)"
                                        "Aventureiro" -> "3d6 seis vezes e você distribui como quiser"
                                        "Heróico" -> "4d6 descartando o menor (personagens mais fortes)"
                                        else -> estilo
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botão épico de rolagem
        Button(
            onClick = {
                logRolagem = mutableListOf()
                when (estiloSelecionado) {
                    "Clássico" -> {
                        val log = mutableListOf<String>()
                        forca = rolarComLog("Força", 3, 6, log).toFloat()
                        destreza = rolarComLog("Destreza", 3, 6, log).toFloat()
                        constituicao = rolarComLog("Constituição", 3, 6, log).toFloat()
                        inteligencia = rolarComLog("Inteligência", 3, 6, log).toFloat()
                        sabedoria = rolarComLog("Sabedoria", 3, 6, log).toFloat()
                        carisma = rolarComLog("Carisma", 3, 6, log).toFloat()
                        logRolagem = log
                    }
                    "Aventureiro" -> {
                        val valores = rolarVariosValores(6, 3, 6)
                        valoresRolados = valores.first
                        logRolagem = valores.second
                        mostrarDistribuicao = true
                        atributosDistribuidos = emptyMap()
                    }
                    "Heróico" -> {
                        val valores = rolarVariosValoresDescartaMenor(6)
                        valoresRolados = valores.first
                        logRolagem = valores.second
                        mostrarDistribuicao = true
                        atributosDistribuidos = emptyMap()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GryffindorRed,
                contentColor = GryffindorLightGold
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "🎲 ROLAR OS DADOS DO DESTINO 🎲",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // Log de rolagem com visual de pergaminho
        if (logRolagem.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            PergaminhoCard {
                Column {
                    Text(
                        "📊 REGISTRO DAS ROLAGENS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GryffindorRed
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    logRolagem.forEach { log ->
                        Text(
                            log,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Interface baseada no estilo selecionado - só aparece DEPOIS de rolar dados
        if (logRolagem.isNotEmpty() || mostrarDistribuicao) {
            when {
                estiloSelecionado == "Clássico" && logRolagem.isNotEmpty() -> {
                    AtributosClassicoDisplay(forca, destreza, constituicao, inteligencia, sabedoria, carisma)
                    Spacer(Modifier.height(16.dp))
                    RPGDropdownCard("🏰 ESCOLHA SUA RAÇA", racas, racaSelecionada) { racaSelecionada = it }
                }

                mostrarDistribuicao && valoresRolados.isNotEmpty() -> {
                    DistribuicaoAtributosRPG(
                        valoresDisponiveis = valoresRolados,
                        atributosDistribuidos = atributosDistribuidos,
                        onAtributosChanged = { atributosDistribuidos = it }
                    )

                    Spacer(Modifier.height(16.dp))
                    RPGDropdownCard("🏰 ESCOLHA SUA RAÇA", racas, racaSelecionada) { racaSelecionada = it }
                    Spacer(Modifier.height(12.dp))
                    RPGDropdownCard("⚔️ ESCOLHA SUA CLASSE", classes, classeSelecionada) { classeSelecionada = it }
                }
            }
        } else {
            // Mensagem informativa quando ainda não rolou dados
            PergaminhoCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🎲",
                        fontSize = 48.sp
                    )
                    Text(
                        "Role os dados para começar!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GryffindorRed,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        "Escolha um estilo e clique no botão acima",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Botão épico de criação
        val podecriar = nome.isNotBlank() && when {
            estiloSelecionado == "Clássico" -> logRolagem.isNotEmpty()
            mostrarDistribuicao -> {
                val todosDistribuidos = atributosDistribuidos.size == 6
                val temTodosAtributos = listOf("Força", "Destreza", "Constituição", "Inteligência", "Sabedoria", "Carisma")
                    .all { atributosDistribuidos.containsKey(it) }
                todosDistribuidos && temTodosAtributos
            }
            else -> true
        }

        Button(
            onClick = {
                val atributos = when {
                    estiloSelecionado == "Clássico" -> {
                        Atributos(
                            forca.toInt(), destreza.toInt(), constituicao.toInt(),
                            inteligencia.toInt(), sabedoria.toInt(), carisma.toInt()
                        )
                    }
                    mostrarDistribuicao && atributosDistribuidos.size == 6 -> {
                        Atributos(
                            atributosDistribuidos["Força"] ?: 10,
                            atributosDistribuidos["Destreza"] ?: 10,
                            atributosDistribuidos["Constituição"] ?: 10,
                            atributosDistribuidos["Inteligência"] ?: 10,
                            atributosDistribuidos["Sabedoria"] ?: 10,
                            atributosDistribuidos["Carisma"] ?: 10
                        )
                    }
                    else -> {
                        Atributos(
                            forca.toInt(), destreza.toInt(), constituicao.toInt(),
                            inteligencia.toInt(), sabedoria.toInt(), carisma.toInt()
                        )
                    }
                }

                val classeParaUsar = if (estiloSelecionado == "Clássico") {
                    escolherClasseAutomatica(atributos)
                } else {
                    classeSelecionada
                }

                val personagem = controller.criarPersonagem(
                    nome = nome,
                    raca = racaSelecionada,
                    classe = classeParaUsar,
                    atributos = atributos
                )

                personagemCriado = personagem

                // 🔥 SALVAR NO BANCO DE DADOS
                viewModel.salvarPersonagem(personagem)
            },
            enabled = podecriar,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (podecriar) GryffindorGold else Color.Gray,
                contentColor = if (podecriar) DragonBlack else Color.LightGray
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                if (podecriar) "⚔️ FORJAR O HERÓI ⚔️" else "🚫 COMPLETE OS DADOS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            )
        }

        // FEEDBACK DE SALVAMENTO
        Spacer(Modifier.height(16.dp))

        when (salvamentoStatus) {
            is SalvamentoStatus.Salvando -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = GryffindorGold
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Salvando personagem...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            is SalvamentoStatus.Sucesso -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✅",
                            fontSize = 24.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Personagem salvo com sucesso!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        )
                    }
                }
            }

            is SalvamentoStatus.Erro -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.2f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF44336))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "❌",
                                fontSize = 24.sp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Erro ao salvar",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            )
                        }
                        Text(
                            (salvamentoStatus as SalvamentoStatus.Erro).mensagem,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            else -> { /* Idle - não mostra nada */ }
        }
        if (salvamentoStatus is SalvamentoStatus.Sucesso) {
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.resetarStatus()
                    onNavigateToList()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GryffindorGold,
                    contentColor = DragonBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "📜 VER PERSONAGENS SALVOS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Ficha épica do personagem
        personagemCriado?.let { personagem ->
            Spacer(Modifier.height(24.dp))
            FichaPersonagemEpica(personagem)
        }
    }
}

@Composable
fun PergaminhoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = ScrollParchment
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MedievalBrown.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RPGDropdownCard(
    titulo: String,
    opcoes: List<String>,
    selecionado: String,
    onSelecao: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    PergaminhoCard {
        Column {
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GryffindorRed
                )
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selecionado,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GryffindorGold,
                        unfocusedBorderColor = MedievalBrown
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    opcoes.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao) },
                            onClick = {
                                onSelecao(opcao)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AtributosClassicoDisplay(
    forca: Float, destreza: Float, constituicao: Float,
    inteligencia: Float, sabedoria: Float, carisma: Float
) {
    PergaminhoCard {
        Column {
            Text(
                "🧬 ATRIBUTOS DO HERÓI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GryffindorRed
                )
            )
            Spacer(Modifier.height(12.dp))

            val atributos = listOf(
                "⚡ Força" to forca.toInt(),
                "🏃 Destreza" to destreza.toInt(),
                "💪 Constituição" to constituicao.toInt(),
                "🧠 Inteligência" to inteligencia.toInt(),
                "🦉 Sabedoria" to sabedoria.toInt(),
                "✨ Carisma" to carisma.toInt()
            )

            atributos.forEach { (nome, valor) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GryffindorGold.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GryffindorGold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            nome,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "$valor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GryffindorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DistribuicaoAtributosRPG(
    valoresDisponiveis: List<Int>,
    atributosDistribuidos: Map<String, Int>,
    onAtributosChanged: (Map<String, Int>) -> Unit
) {
    val atributos = listOf("Força", "Destreza", "Constituição", "Inteligência", "Sabedoria", "Carisma")
    val icones = mapOf(
        "Força" to "⚡",
        "Destreza" to "🏃",
        "Constituição" to "💪",
        "Inteligência" to "🧠",
        "Sabedoria" to "🦉",
        "Carisma" to "✨"
    )

    // Calcula valores restantes
    val valoresUsados = atributosDistribuidos.values.toList()
    val valoresRestantes = valoresDisponiveis.toMutableList()
    valoresUsados.forEach { valorUsado ->
        valoresRestantes.remove(valorUsado)
    }

    PergaminhoCard {
        Column {
            Text(
                "🎯 DISTRIBUA OS VALORES ROLADOS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GryffindorRed
                )
            )
            Spacer(Modifier.height(8.dp))

            Text(
                "Valores disponíveis: ${valoresRestantes.sorted().joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(12.dp))

            atributos.forEach { atributo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (atributosDistribuidos.containsKey(atributo))
                            GryffindorGold.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (atributosDistribuidos.containsKey(atributo))
                        androidx.compose.foundation.BorderStroke(2.dp, GryffindorGold)
                    else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Nome do atributo sempre visível
                        Text(
                            "${icones[atributo]} $atributo",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        // Valor selecionado ou botões de seleção
                        if (atributosDistribuidos.containsKey(atributo)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Valor selecionado:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                GryffindorRed,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            "${atributosDistribuidos[atributo]}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val novoMapa = atributosDistribuidos.toMutableMap()
                                            novoMapa.remove(atributo)
                                            onAtributosChanged(novoMapa)
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = GryffindorRed
                                        )
                                    ) {
                                        Text("Trocar")
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Escolha um valor:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))

                            if (valoresRestantes.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(valoresRestantes.sorted()) { valor ->
                                        Button(
                                            onClick = {
                                                val novoMapa = atributosDistribuidos.toMutableMap()
                                                novoMapa[atributo] = valor
                                                onAtributosChanged(novoMapa)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = GryffindorGold,
                                                contentColor = DragonBlack
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                "$valor",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    "Nenhum valor disponível",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Indicador de progresso melhorado
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (atributosDistribuidos.size == 6)
                        GryffindorGold.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (atributosDistribuidos.size == 6)
                    androidx.compose.foundation.BorderStroke(2.dp, GryffindorGold)
                else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (atributosDistribuidos.size == 6) "✅" else "⏳",
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Progresso: ${atributosDistribuidos.size}/6 atributos",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (atributosDistribuidos.size == 6)
                                GryffindorRed
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FichaPersonagemEpica(personagem: Personagem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(3.dp, GryffindorGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GryffindorRed.copy(alpha = 0.9f),
                            GryffindorBurgundy.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Cabeçalho da ficha
            Text(
                "⚔️ FICHA DE PERSONAGEM ⚔️",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = GryffindorLightGold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Informações básicas
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ScrollParchment
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRowEpica("👤 Nome", personagem.nome)
                    InfoRowEpica("🏰 Raça", personagem.raca.nome)
                    InfoRowEpica("⚔️ Classe", personagem.classe.nome)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Atributos
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ScrollParchment
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🧬 ATRIBUTOS HEROICOS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GryffindorRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    val atributos = listOf(
                        "⚡ Força" to personagem.atributos.forca,
                        "🏃 Destreza" to personagem.atributos.destreza,
                        "💪 Constituição" to personagem.atributos.constituicao,
                        "🧠 Inteligência" to personagem.atributos.inteligencia,
                        "🦉 Sabedoria" to personagem.atributos.sabedoria,
                        "✨ Carisma" to personagem.atributos.carisma
                    )

                    atributos.forEach { (nome, valor) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                nome,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        GryffindorGold,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "$valor",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = DragonBlack
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Habilidades de Raça
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ScrollParchment
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🏰 HABILIDADES RACIAIS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GryffindorRed
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    personagem.raca.habilidadesEspeciais().forEach { habilidade ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "• ",
                                color = GryffindorGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                habilidade,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Habilidades de Classe
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ScrollParchment
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚔️ HABILIDADES DE CLASSE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GryffindorRed
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    personagem.classe.habilidadesDeClasse().forEach { habilidade ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "• ",
                                color = GryffindorGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                habilidade,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Rodapé épico
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🐉 QUE A AVENTURA COMECE! 🐉",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp
                    ),
                    color = GryffindorLightGold
                )
            }
        }
    }
}

@Composable
fun InfoRowEpica(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = GryffindorRed
        )
    }
}

// Funções auxiliares para rolagem
private fun rolarComLog(nome: String, qtd: Int, faces: Int, log: MutableList<String>): Int {
    val (dados, total) = Dado.rolarComTotal(qtd, faces)
    log.add("$nome: $dados → $total")
    return total
}

private fun rolarVariosValores(qtdValores: Int, qtdDados: Int, faces: Int): Pair<List<Int>, List<String>> {
    val resultados = mutableListOf<Int>()
    val log = mutableListOf<String>()

    repeat(qtdValores) { i ->
        val (dados, total) = Dado.rolarComTotal(qtdDados, faces)
        log.add("Rolagem ${i+1}: $dados → $total")
        resultados.add(total)
    }

    return Pair(resultados, log)
}

private fun rolarVariosValoresDescartaMenor(qtdValores: Int): Pair<List<Int>, List<String>> {
    val resultados = mutableListOf<Int>()
    val log = mutableListOf<String>()

    repeat(qtdValores) { i ->
        val (dados, total) = Dado.rolarDescartaMenor(4, 6)
        log.add("Rolagem ${i+1}: $dados → Descarta menor → $total")
        resultados.add(total)
    }

    return Pair(resultados, log)
}

private fun escolherClasseAutomatica(atributos: Atributos): String {
    val possiveis = mutableListOf<String>()

    if (atributos.forca >= 9) possiveis.add("Guerreiro")
    if (atributos.destreza >= 9) possiveis.add("Ladino")
    if (atributos.inteligencia >= 9) possiveis.add("Mago")
    if (atributos.sabedoria >= 9) possiveis.add("Clérigo")

    return if (possiveis.isEmpty()) "Ladino" else possiveis.first()
}