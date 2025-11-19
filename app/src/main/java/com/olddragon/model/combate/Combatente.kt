package com.olddragon.model.combate

import com.olddragon.model.Personagem

/**
 * Representa um participante no combate
 */
data class Combatente(
    val personagem: Personagem,
    var pontosVida: Int, // PV atual
    val pontosVidaMaximo: Int, // PV máximo
    val classeArmadura: Int, // CA
    val baseAtaqueCorpoACorpo: Int, // BAC
    val baseAtaqueDistancia: Int, // BAD
    val modificadorForca: Int, // MOD Força (para dano corpo a corpo)
    val modificadorDestreza: Int, // MOD Destreza (para iniciativa)
    val modificadorSabedoria: Int, // MOD Sabedoria (para iniciativa)
    val jogadaProtecaoConstitui: Int, // JPC (para Teste de Agonizar)
    val jogadaProtecaoSabedoria: Int, // JPS (para Teste de Agonizar)
    val arma: Arma,
    var ordemIniciativa: OrdemIniciativa = OrdemIniciativa.NAO_ROLADO,
    var estado: EstadoCombatente = EstadoCombatente.ATIVO,
    var tempoMorrendo: Int = 0, // rodadas em estado Morrendo
    var acoesPendentes: List<AcaoCombate> = emptyList()
) {
    fun estaVivo() = estado != EstadoCombatente.MORTO
    fun estaConsciente() = estado == EstadoCombatente.ATIVO
    fun estaMorrendo() = estado == EstadoCombatente.MORRENDO
    
    fun modificadorIniciativa(): Int {
        return maxOf(modificadorDestreza, modificadorSabedoria)
    }
    
    fun sofrerDano(dano: Int) {
        pontosVida -= dano
        if (pontosVida <= 0) {
            estado = EstadoCombatente.MORRENDO
        }
    }
    
    fun curar(quantidade: Int) {
        pontosVida = minOf(pontosVida + quantidade, pontosVidaMaximo)
        if (pontosVida > 0 && estado == EstadoCombatente.MORRENDO) {
            estado = EstadoCombatente.ATIVO
        }
    }
}

/**
 * Arma utilizada pelo combatente
 */
data class Arma(
    val nome: String,
    val dadoDano: String, // Ex: "1d8", "2d6"
    val tipo: TipoArma,
    val criticoMultiplicador: Int = 2 // multiplicador no crítico
) {
    fun rolarDano(): Int {
        // Parse do dado (ex: "1d8" -> rola 1 dado de 8 faces)
        val partes = dadoDano.split("d")
        val quantidade = partes[0].toInt()
        val faces = partes[1].toInt()
        
        var total = 0
        repeat(quantidade) {
            total += (1..faces).random()
        }
        return total
    }
    
    fun rolarDanoCritico(): Int {
        return rolarDano() * criticoMultiplicador
    }
}

enum class TipoArma {
    CORPO_A_CORPO,
    DISTANCIA,
    ARREMESSO // usa BAD mas adiciona MOD Força no dano
}

/**
 * Ordem de iniciativa do combatente
 */
enum class OrdemIniciativa {
    NAO_ROLADO,
    SUCESSO, // Age antes dos inimigos
    FALHA    // Age depois dos inimigos
}

/**
 * Estado atual do combatente
 */
enum class EstadoCombatente {
    ATIVO,      // Pode agir normalmente
    MORRENDO,   // 0 ou menos PV, precisa fazer Teste de Agonizar
    MORTO,      // Falhou no Teste de Agonizar
    FUGINDO,    // Fugiu do combate
    RENDIDO     // Se rendeu
}

/**
 * Tipo de ação que pode ser realizada
 */
sealed class AcaoCombate {
    data class Atacar(val alvoId: String) : AcaoCombate()
    data class Movimentar(val distanciaMetros: Int) : AcaoCombate()
    object Fugir : AcaoCombate()
    object Render : AcaoCombate()
    data class UsarItem(val itemNome: String) : AcaoCombate()
}
