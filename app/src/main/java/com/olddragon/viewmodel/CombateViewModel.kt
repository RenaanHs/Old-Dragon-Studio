package com.olddragon.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olddragon.model.combate.*
import com.olddragon.service.combate.AndroidCombateService
import com.olddragon.service.combate.CombateBackgroundService
import com.olddragon.service.combate.GeradorCombatente
import com.olddragon.service.combate.VelocidadeCombate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar o estado de combate
 * Utiliza CombateBackgroundService para processar combates em segundo plano
 */
class CombateViewModel : ViewModel() {
    
    private val backgroundService = CombateBackgroundService()
    
    // Estado do combate atual (observável)
    val combateAtual: StateFlow<Combate?> = backgroundService.combateAtual
    
    // Estado de execução
    val executando: StateFlow<Boolean> = backgroundService.executando
    
    // Velocidade atual
    val velocidade: StateFlow<VelocidadeCombate> = backgroundService.velocidade
    
    // Estado da UI
    private val _estadoUI = MutableStateFlow<EstadoCombateUI>(EstadoCombateUI.SemCombate)
    val estadoUI: StateFlow<EstadoCombateUI> = _estadoUI.asStateFlow()
    
    // Histórico filtrado (últimos N eventos)
    private val _historicoRecente = MutableStateFlow<List<EventoCombate>>(emptyList())
    val historicoRecente: StateFlow<List<EventoCombate>> = _historicoRecente.asStateFlow()
    
    // Estatísticas do combate
    private val _estatisticas = MutableStateFlow<EstatisticasCombate?>(null)
    val estatisticas: StateFlow<EstatisticasCombate?> = _estatisticas.asStateFlow()
    
    init {
        // Observa mudanças no combate
        viewModelScope.launch {
            combateAtual.collect { combate ->
                if (combate != null) {
                    _estadoUI.value = EstadoCombateUI.EmCombate(combate)
                    _historicoRecente.value = combate.historico.takeLast(20)
                    _estatisticas.value = calcularEstatisticas(combate)
                } else {
                    _estadoUI.value = EstadoCombateUI.SemCombate
                    _historicoRecente.value = emptyList()
                    _estatisticas.value = null
                }
            }
        }
    }
    
    // ==================== Controle de Combate ====================
    
    /**
     * Inicia um combate de teste com grupos aleatórios
     */
    fun iniciarCombateTeste(
        quantidadeAliados: Int = 2,
        quantidadeInimigos: Int = 2,
        nivelAliados: Int = 1,
        desafioInimigos: Int = 1,
        autoExecutar: Boolean = true
    ) {
        val aliados = GeradorCombatente.gerarGrupoAliados(quantidadeAliados, nivelAliados)
        val inimigos = GeradorCombatente.gerarGrupoInimigos(quantidadeInimigos, desafioInimigos)
        
        backgroundService.iniciarCombate(aliados, inimigos, autoExecutar)
    }
    
    /**
     * Inicia um combate com um personagem específico contra inimigos aleatórios
     */
    fun iniciarCombateComPersonagem(
        personagem: com.olddragon.model.Personagem,
        quantidadeInimigos: Int = 2,
        desafioInimigos: Int = 1,
        autoExecutar: Boolean = true
    ) {
        // Converte o personagem para combatente
        val personagemCombatente = GeradorCombatente.criarCombatente(personagem, nivel = 1)
        
        // Gera os inimigos
        val inimigos = GeradorCombatente.gerarGrupoInimigos(quantidadeInimigos, desafioInimigos)
        
        // Inicia o combate
        backgroundService.iniciarCombate(listOf(personagemCombatente), inimigos, autoExecutar)
    }
    
    /**
     * Inicia um combate em segundo plano usando Android Service
     * Este combate continuará mesmo se o app for fechado
     */
    fun iniciarCombateBackground(
        context: Context,
        personagem: com.olddragon.model.Personagem,
        quantidadeInimigos: Int = 2
    ) {
        val intent = Intent(context, AndroidCombateService::class.java).apply {
            action = AndroidCombateService.ACTION_START_COMBAT
            putExtra(AndroidCombateService.EXTRA_PLAYER_NAME, personagem.nome)
            putExtra(AndroidCombateService.EXTRA_NUM_ENEMIES, quantidadeInimigos)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    
    /**
     * Encerra o combate em segundo plano
     */
    fun encerrarCombateBackground(context: Context) {
        val intent = Intent(context, AndroidCombateService::class.java).apply {
            action = AndroidCombateService.ACTION_STOP_COMBAT
        }
        context.startService(intent)
    }
    
    /**
     * Inicia um combate customizado
     */
    fun iniciarCombate(
        aliados: List<Combatente>, 
        inimigos: List<Combatente>,
        autoExecutar: Boolean = true
    ) {
        backgroundService.iniciarCombate(aliados, inimigos, autoExecutar)
    }
    
    /**
     * Executa o combate completo automaticamente
     */
    fun executarAutomatico(maxRodadas: Int = 20) {
        backgroundService.executarCombateAutomatico(maxRodadas)
    }
    
    /**
     * Executa a próxima rodada manualmente
     */
    fun executarProximaRodada() {
        viewModelScope.launch {
            backgroundService.executarProximaRodada()
        }
    }
    
    /**
     * Pausa a execução automática
     */
    fun pausar() {
        backgroundService.pausar()
    }
    
    /**
     * Retoma a execução automática
     */
    fun retomar() {
        backgroundService.retomar()
    }
    
    /**
     * Encerra o combate atual
     */
    fun encerrarCombate() {
        backgroundService.cancelarCombate()
    }
    
    /**
     * Reinicia o combate com os mesmos participantes
     */
    fun reiniciarCombate() {
        backgroundService.reiniciarCombate()
    }
    
    // ==================== Configurações ====================
    
    /**
     * Define a velocidade de execução
     */
    fun setVelocidade(velocidade: VelocidadeCombate) {
        backgroundService.setVelocidade(velocidade)
    }
    
    // ==================== Estatísticas ====================
    
    /**
     * Calcula estatísticas do combate
     */
    private fun calcularEstatisticas(combate: Combate): EstatisticasCombate {
        val totalDanoAliados = combate.historico.filterIsInstance<EventoCombate.Dano>()
            .filter { evento ->
                combate.combatentesAliados.any { it.personagem.nome == evento.alvo }
            }.sumOf { it.quantidade }
        
        val totalDanoInimigos = combate.historico.filterIsInstance<EventoCombate.Dano>()
            .filter { evento ->
                combate.combatentesInimigos.any { it.personagem.nome == evento.alvo }
            }.sumOf { it.quantidade }
        
        val totalAtaques = combate.historico.filterIsInstance<EventoCombate.Ataque>().size
        val ataquesAcertados = combate.historico.filterIsInstance<EventoCombate.Ataque>()
            .count { it.acertou }
        val criticos = combate.historico.filterIsInstance<EventoCombate.Ataque>()
            .count { it.critico }
        val errosCriticos = combate.historico.filterIsInstance<EventoCombate.Ataque>()
            .count { it.rolagemAtaque == 1 }
        
        val mortosAliados = combate.combatentesAliados.count { !it.estaVivo() }
        val mortosInimigos = combate.combatentesInimigos.count { !it.estaVivo() }
        
        val precisao = if (totalAtaques > 0) (ataquesAcertados * 100.0 / totalAtaques) else 0.0
        val taxaCritico = if (totalAtaques > 0) (criticos * 100.0 / totalAtaques) else 0.0
        
        return EstatisticasCombate(
            rodadas = combate.rodadaAtual,
            totalAtaques = totalAtaques,
            ataquesAcertados = ataquesAcertados,
            criticos = criticos,
            errosCriticos = errosCriticos,
            totalDanoAliados = totalDanoAliados,
            totalDanoInimigos = totalDanoInimigos,
            mortosAliados = mortosAliados,
            mortosInimigos = mortosInimigos,
            precisaoGeral = precisao,
            taxaCritico = taxaCritico,
            danoMedioPorAtaque = if (ataquesAcertados > 0) 
                ((totalDanoAliados + totalDanoInimigos).toDouble() / ataquesAcertados) 
                else 0.0
        )
    }
    
    /**
     * Obtém estatísticas por combatente
     */
    fun obterEstatisticasPorCombatente(): Map<String, EstatisticaCombatente> {
        val combate = combateAtual.value ?: return emptyMap()
        
        return combate.todosOsCombatentes().associate { combatente ->
            val nome = combatente.personagem.nome
            
            val ataques = combate.historico.filterIsInstance<EventoCombate.Ataque>()
                .filter { it.atacante == nome }
            
            val danosRecebidos = combate.historico.filterIsInstance<EventoCombate.Dano>()
                .filter { it.alvo == nome }
            
            val estatistica = EstatisticaCombatente(
                nome = nome,
                pvAtual = combatente.pontosVida,
                pvMaximo = combatente.pontosVidaMaximo,
                estado = combatente.estado,
                totalAtaques = ataques.size,
                ataquesAcertados = ataques.count { it.acertou },
                criticos = ataques.count { it.critico },
                danoCausado = ataques.filter { it.acertou }.sumOf { it.dano },
                danoRecebido = danosRecebidos.sumOf { it.quantidade }
            )
            
            nome to estatistica
        }
    }
    
    /**
     * Obtém o resumo do combate
     */
    fun obterResumoCombate(): ResumoCombate? {
        val combate = combateAtual.value ?: return null
        val stats = estatisticas.value ?: return null
        
        return ResumoCombate(
            id = combate.id,
            rodadas = combate.rodadaAtual,
            vencedor = combate.vencedor,
            aliadosIniciais = combate.combatentesAliados.size,
            inimigosIniciais = combate.combatentesInimigos.size,
            aliadosSobreviventes = combate.combatentesAliados.count { it.estaVivo() },
            inimigosSobreviventes = combate.combatentesInimigos.count { it.estaVivo() },
            estatisticas = stats,
            eventosImportantes = combate.historico.filter { evento ->
                evento is EventoCombate.Morte ||
                evento is EventoCombate.InicioCombate ||
                evento is EventoCombate.FimCombate ||
                (evento is EventoCombate.Ataque && evento.critico)
            }
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        backgroundService.dispose()
    }
}

// ==================== Estados e Dados ====================

/**
 * Estado da UI de combate
 */
sealed class EstadoCombateUI {
    object SemCombate : EstadoCombateUI()
    data class EmCombate(val combate: Combate) : EstadoCombateUI()
}

/**
 * Estatísticas gerais do combate
 */
data class EstatisticasCombate(
    val rodadas: Int,
    val totalAtaques: Int,
    val ataquesAcertados: Int,
    val criticos: Int,
    val errosCriticos: Int,
    val totalDanoAliados: Int,
    val totalDanoInimigos: Int,
    val mortosAliados: Int,
    val mortosInimigos: Int,
    val precisaoGeral: Double,
    val taxaCritico: Double,
    val danoMedioPorAtaque: Double
)

/**
 * Estatísticas individuais de um combatente
 */
data class EstatisticaCombatente(
    val nome: String,
    val pvAtual: Int,
    val pvMaximo: Int,
    val estado: EstadoCombatente,
    val totalAtaques: Int,
    val ataquesAcertados: Int,
    val criticos: Int,
    val danoCausado: Int,
    val danoRecebido: Int
) {
    val precisao: Double get() = if (totalAtaques > 0) (ataquesAcertados * 100.0 / totalAtaques) else 0.0
    val taxaCritico: Double get() = if (totalAtaques > 0) (criticos * 100.0 / totalAtaques) else 0.0
    val percentualVida: Double get() = if (pvMaximo > 0) (pvAtual * 100.0 / pvMaximo) else 0.0
}

/**
 * Resumo completo do combate
 */
data class ResumoCombate(
    val id: String,
    val rodadas: Int,
    val vencedor: LadoCombate?,
    val aliadosIniciais: Int,
    val inimigosIniciais: Int,
    val aliadosSobreviventes: Int,
    val inimigosSobreviventes: Int,
    val estatisticas: EstatisticasCombate,
    val eventosImportantes: List<EventoCombate>
)
