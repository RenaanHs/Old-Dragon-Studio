package com.olddragon.service.combate

import com.olddragon.model.combate.LadoCombate
import kotlinx.coroutines.runBlocking

/**
 * Exemplos de uso do sistema de combate
 * Demonstra diferentes cenários e funcionalidades
 */
object ExemplosCombate {
    
    /**
     * Exemplo 1: Combate automático simples
     * Executa um combate completo de forma instantânea
     */
    fun exemploAutomatico() {
        println("=== EXEMPLO 1: COMBATE AUTOMÁTICO ===\n")
        
        val service = CombateService()
        
        // Cria os combatentes
        val (aliados, inimigos) = CombatenteFactory.criarCombateExemplo()
        
        println("Iniciando combate:")
        println("Aliados: ${aliados.map { it.personagem.nome }}")
        println("Inimigos: ${inimigos.map { it.personagem.nome }}\n")
        
        // Executa combate completo
        val combate = service.executarCombateAutomatico(aliados, inimigos, maxRodadas = 10)
        
        // Exibe histórico
        println("\n=== HISTÓRICO DO COMBATE ===")
        combate.historico.forEach { evento ->
            println("[Rodada ${evento.rodada}] ${evento.descricao}")
        }
        
        // Exibe resultado
        println("\n=== RESULTADO FINAL ===")
        println("Rodadas: ${combate.rodadaAtual}")
        println("Vencedor: ${if (combate.vencedor == LadoCombate.ALIADOS) "Aliados" else "Inimigos"}")
        println("\nAliados sobreviventes:")
        combate.combatentesAliados.filter { it.estaVivo() }.forEach {
            println("  - ${it.personagem.nome}: ${it.pontosVida}/${it.pontosVidaMaximo} PV")
        }
        println("\nInimigos sobreviventes:")
        combate.combatentesInimigos.filter { it.estaVivo() }.forEach {
            println("  - ${it.personagem.nome}: ${it.pontosVida}/${it.pontosVidaMaximo} PV")
        }
    }
    
    /**
     * Exemplo 2: Combate do documento (Guerreiro A vs Guerreiro B)
     * Recria o exemplo detalhado do documento
     */
    fun exemploCombateDocumento() {
        println("\n\n=== EXEMPLO 2: COMBATE DO DOCUMENTO ===")
        println("Guerreiro A vs Guerreiro B\n")
        
        val service = CombateService()
        val (aliados, inimigos) = CombatenteFactory.criarCombateDocumento()
        
        val combate = service.executarCombateAutomatico(aliados, inimigos, maxRodadas = 5)
        
        println("\n=== HISTÓRICO COMPLETO ===")
        combate.historico.forEach { evento ->
            println("[Rodada ${evento.rodada}] ${evento.descricao}")
        }
        
        println("\n=== RESULTADO FINAL ===")
        println("Vencedor: ${combate.vencedor?.let { if (it == LadoCombate.ALIADOS) "Guerreiro A" else "Guerreiro B" }}")
    }
    
    /**
     * Exemplo 3: Combate passo a passo
     * Executa cada fase do combate manualmente
     */
    fun exemploPassoAPasso() {
        println("\n\n=== EXEMPLO 3: COMBATE PASSO A PASSO ===\n")
        
        val service = CombateService()
        val aliados = listOf(CombatenteFactory.criarGuerreiroBasico("Herói", pv = 15, forca = 18))
        val inimigos = listOf(
            CombatenteFactory.criarGoblin("Goblin 1"),
            CombatenteFactory.criarGoblin("Goblin 2")
        )
        
        println("=== FASE 1: INÍCIO ===")
        var combate = service.iniciarCombate(aliados, inimigos)
        println("Combate iniciado!\n")
        
        println("=== FASE 2: VERIFICAÇÃO DE SURPRESA ===")
        combate = service.verificarSurpresa(combate)
        combate.historico.takeLast(1).forEach { println(it.descricao) }
        println()
        
        println("=== FASE 3: DETERMINAÇÃO DA INICIATIVA ===")
        combate = service.determinarIniciativa(combate)
        combate.historico.takeLast(3).forEach { println(it.descricao) }
        println()
        
        var rodada = 0
        while (combate.fase != com.olddragon.model.combate.FaseCombate.FINALIZADO && rodada < 5) {
            rodada++
            println("=== RODADA $rodada ===")
            
            combate = service.executarRodada(combate)
            val eventosRodada = combate.historico.filter { it.rodada == combate.rodadaAtual }
            eventosRodada.forEach { println("  ${it.descricao}") }
            
            combate = service.finalizarRodada(combate)
            println()
            
            if (combate.fase == com.olddragon.model.combate.FaseCombate.FINALIZADO) {
                break
            }
        }
        
        println("=== COMBATE FINALIZADO ===")
        println("Vencedor: ${if (combate.vencedor == LadoCombate.ALIADOS) "Herói" else "Goblins"}")
    }
    
    /**
     * Exemplo 4: Combate em segundo plano com velocidade configurável
     */
    fun exemploBackgroundService(): Unit = runBlocking {
        println("\n\n=== EXEMPLO 4: SERVIÇO EM SEGUNDO PLANO ===\n")
        
        val backgroundService = CombateBackgroundService()
        
        // Gera combatentes aleatórios
        val aliados = GeradorCombatente.gerarGrupoAliados(3, nivel = 2)
        val inimigos = GeradorCombatente.gerarGrupoInimigos(4, desafio = 1)
        
        println("Iniciando combate:")
        println("Aliados: ${aliados.map { it.personagem.nome }}")
        println("Inimigos: ${inimigos.map { it.personagem.nome }}\n")
        
        // Configura velocidade rápida
        backgroundService.setVelocidade(VelocidadeCombate.RAPIDA)
        
        // Inicia combate com execução automática
        backgroundService.iniciarCombate(aliados, inimigos, autoExecutar = true)
        
        // Observa o combate em tempo real
        backgroundService.combateAtual.collect { combate ->
            if (combate != null) {
                // Exibe eventos recentes
                val eventosRecentes = combate.historico.takeLast(1)
                eventosRecentes.forEach { evento ->
                    println("[Rodada ${evento.rodada}] ${evento.descricao}")
                }
                
                // Para quando finalizar
                if (combate.fase == com.olddragon.model.combate.FaseCombate.FINALIZADO) {
                    println("\n=== COMBATE FINALIZADO ===")
                    println("Rodadas: ${combate.rodadaAtual}")
                    println("Vencedor: ${if (combate.vencedor == LadoCombate.ALIADOS) "Aliados" else "Inimigos"}")
                    
                    backgroundService.dispose()
                    return@collect
                }
            }
        }
    }
    
    /**
     * Exemplo 5: Batalha épica - múltiplos combatentes
     */
    fun exemploBatalhaEpica() {
        println("\n\n=== EXEMPLO 5: BATALHA ÉPICA ===")
        println("5 Heróis vs 8 Inimigos\n")
        
        val service = CombateService()
        
        // Gera grupos maiores
        val aliados = GeradorCombatente.gerarGrupoAliados(5, nivel = 3)
        val inimigos = GeradorCombatente.gerarGrupoInimigos(8, desafio = 2)
        
        println("Exército Aliado:")
        aliados.forEach { println("  - ${it.personagem.nome} (${it.personagem.classe.nome}) - CA: ${it.classeArmadura}, PV: ${it.pontosVida}") }
        
        println("\nHorda Inimiga:")
        inimigos.forEach { println("  - ${it.personagem.nome} - CA: ${it.classeArmadura}, PV: ${it.pontosVida}") }
        
        println("\n=== INICIANDO BATALHA ===\n")
        
        val combate = service.executarCombateAutomatico(aliados, inimigos, maxRodadas = 15)
        
        // Estatísticas da batalha
        println("\n=== ESTATÍSTICAS DA BATALHA ===")
        println("Rodadas totais: ${combate.rodadaAtual}")
        
        val totalAtaques = combate.historico.filterIsInstance<com.olddragon.model.combate.EventoCombate.Ataque>().size
        val acertos = combate.historico.filterIsInstance<com.olddragon.model.combate.EventoCombate.Ataque>().count { it.acertou }
        val criticos = combate.historico.filterIsInstance<com.olddragon.model.combate.EventoCombate.Ataque>().count { it.critico }
        
        println("Total de ataques: $totalAtaques")
        println("Acertos: $acertos (${(acertos * 100.0 / totalAtaques).toInt()}%)")
        println("Críticos: $criticos (${(criticos * 100.0 / totalAtaques).toInt()}%)")
        
        val mortosAliados = combate.combatentesAliados.count { !it.estaVivo() }
        val mortosInimigos = combate.combatentesInimigos.count { !it.estaVivo() }
        
        println("\nBaixas:")
        println("  Aliados: $mortosAliados mortos, ${aliados.size - mortosAliados} sobreviventes")
        println("  Inimigos: $mortosInimigos mortos, ${inimigos.size - mortosInimigos} sobreviventes")
        
        println("\n=== VENCEDOR: ${if (combate.vencedor == LadoCombate.ALIADOS) "ALIADOS" else "INIMIGOS"} ===")
    }
}

/**
 * Função principal para executar todos os exemplos
 */
fun main() {
    println("╔════════════════════════════════════════╗")
    println("║  SISTEMA DE COMBATE - OLD DRAGON 2    ║")
    println("║        Exemplos de Uso                 ║")
    println("╚════════════════════════════════════════╝\n")
    
    // Executa exemplos
    ExemplosCombate.exemploAutomatico()
    ExemplosCombate.exemploCombateDocumento()
    ExemplosCombate.exemploPassoAPasso()
    ExemplosCombate.exemploBatalhaEpica()
    
    // Exemplo em background requer suspend function
    // ExemplosCombate.exemploBackgroundService()
    
    println("\n\n╔════════════════════════════════════════╗")
    println("║     EXEMPLOS FINALIZADOS COM SUCESSO   ║")
    println("╚════════════════════════════════════════╝")
}
