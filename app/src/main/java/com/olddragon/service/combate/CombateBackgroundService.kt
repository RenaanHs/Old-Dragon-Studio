package com.olddragon.service.combate

import com.olddragon.model.combate.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Serviço em segundo plano para processar combates automaticamente
 * Executa as rodadas de combate de forma assíncrona com delays configuráveis
 */
class CombateBackgroundService {
    
    private val combateService = CombateService()
    private var combateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Estado observável do combate
    private val _combateAtual = MutableStateFlow<Combate?>(null)
    val combateAtual: StateFlow<Combate?> = _combateAtual.asStateFlow()
    
    // Controle de velocidade
    private val _velocidade = MutableStateFlow(VelocidadeCombate.NORMAL)
    val velocidade: StateFlow<VelocidadeCombate> = _velocidade.asStateFlow()
    
    // Estado de execução
    private val _executando = MutableStateFlow(false)
    val executando: StateFlow<Boolean> = _executando.asStateFlow()
    
    /**
     * Inicia um novo combate em segundo plano
     */
    fun iniciarCombate(
        aliados: List<Combatente>,
        inimigos: List<Combatente>,
        autoExecutar: Boolean = true
    ) {
        // Cancela combate anterior se existir
        cancelarCombate()
        
        // Cria novo combate
        val combate = combateService.iniciarCombate(aliados, inimigos)
        _combateAtual.value = combate
        
        if (autoExecutar) {
            executarCombateAutomatico()
        }
    }
    
    /**
     * Executa o combate completo em segundo plano
     */
    fun executarCombateAutomatico(maxRodadas: Int = 20) {
        val combate = _combateAtual.value ?: return
        
        if (_executando.value) {
            return // Já está executando
        }
        
        combateJob = scope.launch {
            _executando.value = true
            
            try {
                var combateAtualizado = combate
                
                // Verifica surpresa
                combateAtualizado = combateService.verificarSurpresa(combateAtualizado)
                _combateAtual.value = combateAtualizado
                delay(_velocidade.value.delayRodada)
                
                // Loop de rodadas
                var rodada = 0
                while (combateAtualizado.fase != FaseCombate.FINALIZADO && rodada < maxRodadas) {
                    // Determina iniciativa
                    combateAtualizado = combateService.determinarIniciativa(combateAtualizado)
                    _combateAtual.value = combateAtualizado
                    delay(_velocidade.value.delayIniciativa)
                    
                    // Executa rodada
                    combateAtualizado = combateService.executarRodada(combateAtualizado)
                    _combateAtual.value = combateAtualizado
                    delay(_velocidade.value.delayAcao)
                    
                    // Finaliza rodada
                    combateAtualizado = combateService.finalizarRodada(combateAtualizado)
                    _combateAtual.value = combateAtualizado
                    delay(_velocidade.value.delayRodada)
                    
                    rodada++
                }
                
                // Finaliza se atingiu limite de rodadas
                if (combateAtualizado.fase != FaseCombate.FINALIZADO) {
                    combateAtualizado = combateAtualizado.copy(
                        fase = FaseCombate.FINALIZADO
                    )
                    combateAtualizado.adicionarEvento(
                        EventoCombate.FimCombate(
                            rodada = combateAtualizado.rodadaAtual,
                            vencedor = null,
                            descricao = "Combate encerrado por limite de rodadas."
                        )
                    )
                    _combateAtual.value = combateAtualizado
                }
                
            } catch (e: CancellationException) {
                // Combate foi cancelado
            } finally {
                _executando.value = false
            }
        }
    }
    
    /**
     * Executa a próxima rodada manualmente
     */
    suspend fun executarProximaRodada() {
        val combate = _combateAtual.value ?: return
        
        if (combate.fase == FaseCombate.FINALIZADO) {
            return
        }
        
        var combateAtualizado = combate
        
        // Determina iniciativa se necessário
        if (combate.fase == FaseCombate.DETERMINACAO_INICIATIVA || 
            combate.fase == FaseCombate.PREPARACAO) {
            if (combate.fase == FaseCombate.PREPARACAO) {
                combateAtualizado = combateService.verificarSurpresa(combateAtualizado)
            }
            combateAtualizado = combateService.determinarIniciativa(combateAtualizado)
        }
        
        // Executa rodada
        combateAtualizado = combateService.executarRodada(combateAtualizado)
        
        // Finaliza rodada
        combateAtualizado = combateService.finalizarRodada(combateAtualizado)
        
        _combateAtual.value = combateAtualizado
    }
    
    /**
     * Pausa a execução automática
     */
    fun pausar() {
        combateJob?.cancel()
        combateJob = null
        _executando.value = false
    }
    
    /**
     * Retoma a execução automática
     */
    fun retomar() {
        if (!_executando.value && _combateAtual.value?.fase != FaseCombate.FINALIZADO) {
            executarCombateAutomatico()
        }
    }
    
    /**
     * Cancela o combate atual
     */
    fun cancelarCombate() {
        combateJob?.cancel()
        combateJob = null
        _executando.value = false
        _combateAtual.value = null
    }
    
    /**
     * Define a velocidade de execução
     */
    fun setVelocidade(velocidade: VelocidadeCombate) {
        _velocidade.value = velocidade
    }
    
    /**
     * Reinicia o combate com os mesmos participantes
     */
    fun reiniciarCombate() {
        val combate = _combateAtual.value ?: return
        
        // Cria cópias dos combatentes com PV restaurado
        val aliadosRenovados = combate.combatentesAliados.map { it.copy(
            pontosVida = it.pontosVidaMaximo,
            estado = EstadoCombatente.ATIVO,
            ordemIniciativa = OrdemIniciativa.NAO_ROLADO
        )}
        
        val inimigosRenovados = combate.combatentesInimigos.map { it.copy(
            pontosVida = it.pontosVidaMaximo,
            estado = EstadoCombatente.ATIVO,
            ordemIniciativa = OrdemIniciativa.NAO_ROLADO
        )}
        
        iniciarCombate(aliadosRenovados, inimigosRenovados, autoExecutar = false)
    }
    
    /**
     * Libera recursos
     */
    fun dispose() {
        cancelarCombate()
        scope.cancel()
    }
}

/**
 * Velocidades de execução do combate
 */
enum class VelocidadeCombate(
    val delayRodada: Long,
    val delayIniciativa: Long,
    val delayAcao: Long
) {
    MUITO_LENTA(2000L, 1500L, 1000L),
    LENTA(1500L, 1000L, 750L),
    NORMAL(1000L, 750L, 500L),
    RAPIDA(500L, 300L, 200L),
    MUITO_RAPIDA(200L, 100L, 50L),
    INSTANTANEA(0L, 0L, 0L)
}
