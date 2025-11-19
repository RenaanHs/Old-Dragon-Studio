package com.olddragon.service.combate

import com.olddragon.model.combate.*
import com.olddragon.service.Dado

/**
 * Serviço responsável por executar a lógica de combate do Old Dragon 2
 * Implementa o sistema de combate por turnos com rodadas de 10 segundos
 */
class CombateService {
    
    /**
     * Inicia um novo combate
     */
    fun iniciarCombate(
        aliados: List<Combatente>,
        inimigos: List<Combatente>,
        combateId: String = java.util.UUID.randomUUID().toString()
    ): Combate {
        val combate = Combate(
            id = combateId,
            combatentesAliados = aliados,
            combatentesInimigos = inimigos,
            rodadaAtual = 0,
            fase = FaseCombate.PREPARACAO
        )
        
        combate.adicionarEvento(
            EventoCombate.InicioCombate(
                rodada = 0,
                descricao = "O combate entre ${aliados.size} aliados e ${inimigos.size} inimigos começou!"
            )
        )
        
        return combate
    }
    
    /**
     * Executa um combate completo de forma automática
     */
    fun executarCombateAutomatico(
        aliados: List<Combatente>,
        inimigos: List<Combatente>,
        maxRodadas: Int = 20
    ): Combate {
        var combate = iniciarCombate(aliados, inimigos)
        combate = verificarSurpresa(combate)
        
        var rodada = 0
        while (combate.fase != FaseCombate.FINALIZADO && rodada < maxRodadas) {
            combate = determinarIniciativa(combate)
            combate = executarRodada(combate)
            combate = finalizarRodada(combate)
            rodada++
        }
        
        if (combate.fase != FaseCombate.FINALIZADO) {
            combate.adicionarEvento(
                EventoCombate.FimCombate(
                    rodada = combate.rodadaAtual,
                    vencedor = null,
                    descricao = "Combate encerrado por limite de rodadas (empate)."
                )
            )
            combate = combate.copy(fase = FaseCombate.FINALIZADO)
        }
        
        return combate
    }
    
    /**
     * Passo 1: Verificação de Surpresa
     * Determina se algum lado foi pego de surpresa
     */
    fun verificarSurpresa(combate: Combate): Combate {
        val combateAtualizado = combate.copy()
        
        // Rola 1d6 para cada lado (1-2 = surpreso)
        val (rolagemAliados, _) = Dado.rolarComTotal(1, 6)
        val (rolagemInimigos, _) = Dado.rolarComTotal(1, 6)
        
        val aliadosSurpresos = rolagemAliados[0] <= 2
        val inimigosSurpresos = rolagemInimigos[0] <= 2
        
        when {
            aliadosSurpresos && !inimigosSurpresos -> {
                combateAtualizado.adicionarEvento(
                    EventoCombate.Surpresa(
                        rodada = combate.rodadaAtual,
                        ladoSurpreendido = LadoCombate.ALIADOS,
                        descricao = "Os aliados foram pegos de surpresa! (rolou ${rolagemAliados[0]})"
                    )
                )
                combateAtualizado.combatentesAliados.forEach { 
                    it.ordemIniciativa = OrdemIniciativa.FALHA 
                }
            }
            inimigosSurpresos && !aliadosSurpresos -> {
                combateAtualizado.adicionarEvento(
                    EventoCombate.Surpresa(
                        rodada = combate.rodadaAtual,
                        ladoSurpreendido = LadoCombate.INIMIGOS,
                        descricao = "Os inimigos foram pegos de surpresa! (rolou ${rolagemInimigos[0]})"
                    )
                )
                combateAtualizado.combatentesInimigos.forEach { 
                    it.ordemIniciativa = OrdemIniciativa.FALHA 
                }
            }
            aliadosSurpresos && inimigosSurpresos -> {
                combateAtualizado.adicionarEvento(
                    EventoCombate.Surpresa(
                        rodada = combate.rodadaAtual,
                        ladoSurpreendido = LadoCombate.ALIADOS,
                        descricao = "Ambos os lados foram pegos de surpresa! Ninguém age com vantagem."
                    )
                )
            }
            else -> {
                combateAtualizado.adicionarEvento(
                    EventoCombate.InicioCombate(
                        rodada = combate.rodadaAtual,
                        descricao = "Nenhum lado foi pego de surpresa. Todos estão prontos!"
                    )
                )
            }
        }
        
        return combateAtualizado.copy(fase = FaseCombate.DETERMINACAO_INICIATIVA)
    }
    
    /**
     * Passo 2: Determinação da Iniciativa
     * Cada combatente rola 1d20 contra Destreza ou Sabedoria (o maior)
     * Sucesso: age antes dos inimigos
     * Falha: age depois dos inimigos
     */
    fun determinarIniciativa(combate: Combate): Combate {
        val combateAtualizado = combate.copy()
        
        combateAtualizado.todosOsCombatentes()
            .filter { it.ordemIniciativa == OrdemIniciativa.NAO_ROLADO }
            .forEach { combatente ->
                val (rolagem, _) = Dado.rolarComTotal(1, 20)
                val d20 = rolagem[0]
                val atributoBase = 10 + combatente.modificadorIniciativa()
                val sucesso = d20 <= atributoBase
                
                combatente.ordemIniciativa = if (sucesso) OrdemIniciativa.SUCESSO else OrdemIniciativa.FALHA
                
                combateAtualizado.adicionarEvento(
                    EventoCombate.Iniciativa(
                        rodada = combate.rodadaAtual,
                        combatente = combatente.personagem.nome,
                        resultado = sucesso,
                        rolagem = d20,
                        descricao = "${combatente.personagem.nome} rolou $d20 contra $atributoBase: ${if(sucesso) "SUCESSO (age primeiro)" else "FALHA (age depois)"}"
                    )
                )
            }
        
        return combateAtualizado.copy(fase = FaseCombate.EXECUCAO_ACOES)
    }
    
    /**
     * Passo 3/4/5: Execução das Ações
     * Executa os turnos de todos os combatentes na ordem:
     * 1. Sucessos na iniciativa
     * 2. Falhas na iniciativa
     */
    fun executarRodada(combate: Combate): Combate {
        val combateAtualizado = combate.copy(rodadaAtual = combate.rodadaAtual + 1)
        
        // Monta a ordem de ação
        val ordem = mutableListOf<Combatente>()
        ordem.addAll(combateAtualizado.combatentesAtivos().filter { it.ordemIniciativa == OrdemIniciativa.SUCESSO })
        ordem.addAll(combateAtualizado.combatentesAtivos().filter { it.ordemIniciativa == OrdemIniciativa.FALHA })
        
        // Executa turno de cada combatente
        ordem.forEach { atacante ->
            if (atacante.estaConsciente()) {
                executarTurno(combateAtualizado, atacante)
            }
        }
        
        return combateAtualizado.copy(fase = FaseCombate.FIM_RODADA)
    }
    
    /**
     * Executa o turno de um combatente individual
     */
    private fun executarTurno(combate: Combate, combatente: Combatente) {
        val ladoCombatente = if (combate.combatentesAliados.contains(combatente)) 
            LadoCombate.ALIADOS else LadoCombate.INIMIGOS
        val ladoInimigo = if (ladoCombatente == LadoCombate.ALIADOS) 
            LadoCombate.INIMIGOS else LadoCombate.ALIADOS
        
        // Seleciona alvo aleatório entre inimigos ativos
        val alvosDisponiveis = combate.combatentesAtivosPorLado(ladoInimigo)
        if (alvosDisponiveis.isNotEmpty()) {
            val alvo = alvosDisponiveis.random()
            executarAtaque(combate, combatente, alvo)
        }
    }
    
    /**
     * Passo 4: Resolução do Ataque
     * Rola 1d20 + BA vs CA do alvo
     * Natural 20 = Crítico (sempre acerta, dano dobrado)
     * Natural 1 = Erro Crítico (sempre erra)
     */
    private fun executarAtaque(combate: Combate, atacante: Combatente, alvo: Combatente) {
        val (rolagem, _) = Dado.rolarComTotal(1, 20)
        val d20 = rolagem[0]
        
        // Determina BA baseado no tipo de arma
        val ba = when (atacante.arma.tipo) {
            TipoArma.CORPO_A_CORPO -> atacante.baseAtaqueCorpoACorpo
            else -> atacante.baseAtaqueDistancia
        }
        
        val totalAtaque = d20 + ba
        val critico = d20 == 20
        val erroCritico = d20 == 1
        val acertou = critico || (!erroCritico && totalAtaque >= alvo.classeArmadura)
        
        var dano = 0
        if (acertou) {
            // Passo 5: Cálculo do Dano
            dano = if (critico) {
                // Crítico: dobra o dano do dado, depois adiciona modificador
                atacante.arma.rolarDanoCritico()
            } else {
                atacante.arma.rolarDano()
            }
            
            // Adiciona modificador de Força para ataques corpo a corpo e arremesso
            if (atacante.arma.tipo == TipoArma.CORPO_A_CORPO || 
                atacante.arma.tipo == TipoArma.ARREMESSO) {
                dano += atacante.modificadorForca
            }
            
            // Dano mínimo é sempre 1
            dano = maxOf(1, dano)
            
            // Aplica dano no alvo
            alvo.sofrerDano(dano)
            
            combate.adicionarEvento(
                EventoCombate.Dano(
                    rodada = combate.rodadaAtual,
                    alvo = alvo.personagem.nome,
                    quantidade = dano,
                    pvRestante = alvo.pontosVida,
                    descricao = "${alvo.personagem.nome} sofre $dano de dano (PV: ${alvo.pontosVida}/${alvo.pontosVidaMaximo})"
                )
            )
        }
        
        // Registra o ataque
        val descricao = when {
            critico -> "${atacante.personagem.nome} acerta CRÍTICO em ${alvo.personagem.nome}! Rolou 20! Dano: $dano"
            erroCritico -> "${atacante.personagem.nome} comete ERRO CRÍTICO! Rolou 1!"
            acertou -> "${atacante.personagem.nome} acerta ${alvo.personagem.nome} ($d20+$ba=$totalAtaque vs CA ${alvo.classeArmadura}). Dano: $dano"
            else -> "${atacante.personagem.nome} erra ${alvo.personagem.nome} ($d20+$ba=$totalAtaque vs CA ${alvo.classeArmadura})"
        }
        
        combate.adicionarEvento(
            EventoCombate.Ataque(
                rodada = combate.rodadaAtual,
                atacante = atacante.personagem.nome,
                alvo = alvo.personagem.nome,
                rolagemAtaque = d20,
                totalAtaque = totalAtaque,
                acertou = acertou,
                critico = critico,
                dano = dano,
                descricao = descricao
            )
        )
    }
    
    /**
     * Passo 6: Fim da Rodada
     * Verifica condições, testes de agonizar, testes de moral e fim de combate
     */
    fun finalizarRodada(combate: Combate): Combate {
        val combateAtualizado = combate.copy()
        
        // Realiza testes de agonizar para combatentes morrendo
        combateAtualizado.todosOsCombatentes()
            .filter { it.estaMorrendo() }
            .forEach { realizarTesteAgonizar(combateAtualizado, it) }
        
        // Verifica teste de moral (se metade ou mais de um lado foi derrotado)
        verificarTesteMoral(combateAtualizado, LadoCombate.ALIADOS)
        verificarTesteMoral(combateAtualizado, LadoCombate.INIMIGOS)
        
        // Verifica fim de combate
        val vencedor = combateAtualizado.verificarFimDeCombate()
        if (vencedor != null) {
            combateAtualizado.vencedor = vencedor
            combateAtualizado.adicionarEvento(
                EventoCombate.FimCombate(
                    rodada = combateAtualizado.rodadaAtual,
                    vencedor = vencedor,
                    descricao = "COMBATE FINALIZADO! ${if(vencedor == LadoCombate.ALIADOS) "Aliados" else "Inimigos"} vencem a batalha!"
                )
            )
            return combateAtualizado.copy(fase = FaseCombate.FINALIZADO)
        }
        
        // Prepara para próxima rodada: reseta iniciativas
        combateAtualizado.todosOsCombatentes()
            .filter { it.estaConsciente() }
            .forEach { it.ordemIniciativa = OrdemIniciativa.NAO_ROLADO }
        
        return combateAtualizado.copy(fase = FaseCombate.DETERMINACAO_INICIATIVA)
    }
    
    /**
     * Realiza o Teste de Agonizar
     * Rola 1d20, se resultado > JP (maior entre JPC e JPS), o personagem morre
     */
    private fun realizarTesteAgonizar(combate: Combate, combatente: Combatente) {
        val jpBase = maxOf(combatente.jogadaProtecaoConstitui, combatente.jogadaProtecaoSabedoria)
        val (rolagem, _) = Dado.rolarComTotal(1, 20)
        val d20 = rolagem[0]
        val sucesso = d20 <= jpBase
        
        if (!sucesso) {
            // Falhou no teste: morre
            combatente.estado = EstadoCombatente.MORTO
            combate.adicionarEvento(
                EventoCombate.Morte(
                    rodada = combate.rodadaAtual,
                    combatente = combatente.personagem.nome,
                    descricao = "${combatente.personagem.nome} falha no Teste de Agonizar ($d20 > $jpBase) e MORRE!"
                )
            )
        } else {
            combate.adicionarEvento(
                EventoCombate.TesteAgonizar(
                    rodada = combate.rodadaAtual,
                    combatente = combatente.personagem.nome,
                    rolagem = d20,
                    sucesso = true,
                    descricao = "${combatente.personagem.nome} passa no Teste de Agonizar ($d20 <= $jpBase) e continua lutando pela vida!"
                )
            )
        }
    }
    
    /**
     * Verifica se um lado precisa fazer Teste de Moral
     * Ocorre quando metade ou mais do grupo foi derrotado
     */
    private fun verificarTesteMoral(combate: Combate, lado: LadoCombate) {
        val combatentes = when (lado) {
            LadoCombate.ALIADOS -> combate.combatentesAliados
            LadoCombate.INIMIGOS -> combate.combatentesInimigos
        }
        
        val total = combatentes.size
        val derrotados = combatentes.count { !it.estaVivo() || !it.estaConsciente() }
        
        // Se metade ou mais foram derrotados
        if (derrotados >= total / 2.0 && combatentes.any { it.estaConsciente() }) {
            val (rolagem, _) = Dado.rolarComTotal(1, 20)
            val sucesso = rolagem[0] <= 10 // Simplificado: sucesso em 10 ou menos
            
            if (!sucesso) {
                // Falha no teste de moral: sobreviventes fogem ou se rendem
                combatentes.filter { it.estaConsciente() }.forEach { 
                    it.estado = EstadoCombatente.FUGINDO 
                }
                
                combate.adicionarEvento(
                    EventoCombate.TesteMoral(
                        rodada = combate.rodadaAtual,
                        lado = lado,
                        sucesso = false,
                        descricao = "${if(lado == LadoCombate.ALIADOS) "Aliados" else "Inimigos"} falham no Teste de Moral e fogem da batalha!"
                    )
                )
            }
        }
    }
}
