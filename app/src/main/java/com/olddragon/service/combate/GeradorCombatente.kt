package com.olddragon.service.combate

import com.olddragon.model.Personagem
import com.olddragon.model.atributos.Atributos
import com.olddragon.model.classes.*
import com.olddragon.model.combate.Arma
import com.olddragon.model.combate.Combatente
import com.olddragon.model.combate.TipoArma
import com.olddragon.model.raca.*
import com.olddragon.service.Dado

/**
 * Gerador de combatentes aleatórios para testes e combates rápidos
 */
object GeradorCombatente {
    
    private val nomesAliados = listOf(
        "Thorin", "Legolas", "Gimli", "Aragorn", "Gandalf",
        "Elara", "Kael", "Theron", "Lyra", "Dorian"
    )
    
    private val nomesInimigos = listOf(
        "Goblin Feroz", "Orc Guerreiro", "Troll das Montanhas", "Esqueleto Guerreiro",
        "Bandido Cruel", "Lobo Selvagem", "Aranha Gigante", "Kobold Trapaceiro",
        "Zumbi Podre", "Gnoll Bárbaro"
    )
    
    /**
     * Gera um combatente aliado (jogador/NPC amigo)
     */
    fun gerarAliado(nivel: Int = 1): Combatente {
        val nome = nomesAliados.random()
        val raca = gerarRacaAleatoria()
        val classe = gerarClasseAleatoria()
        
        val atributos = gerarAtributosAleatorios()
        val personagem = Personagem(nome, raca, classe, atributos)
        
        return criarCombatente(personagem, nivel)
    }
    
    /**
     * Gera um combatente inimigo
     */
    fun gerarInimigo(desafio: Int = 1): Combatente {
        val nome = nomesInimigos.random()
        val raca = gerarRacaAleatoria()
        val classe = Guerreiro() // Maioria dos inimigos são guerreiros
        
        val atributos = gerarAtributosInimigo(desafio)
        val personagem = Personagem(nome, raca, classe, atributos)
        
        return criarCombatente(personagem, desafio)
    }
    
    /**
     * Gera múltiplos aliados
     */
    fun gerarGrupoAliados(quantidade: Int, nivel: Int = 1): List<Combatente> {
        return List(quantidade) { gerarAliado(nivel) }
    }
    
    /**
     * Gera múltiplos inimigos
     */
    fun gerarGrupoInimigos(quantidade: Int, desafio: Int = 1): List<Combatente> {
        return List(quantidade) { gerarInimigo(desafio) }
    }
    
    /**
     * Cria um combatente a partir de um personagem
     */
    fun criarCombatente(personagem: Personagem, nivel: Int = 1): Combatente {
        val classe = personagem.classe
        val atributos = personagem.atributos
        
        // Calcula PV baseado na classe e nível
        val pvMaximo = calcularPV(classe, nivel)
        
        // Calcula CA (10 + modificador de Destreza + bônus de armadura)
        val modDestreza = calcularModificador(atributos.destreza)
        val bonusArmadura = when (classe) {
            is Guerreiro -> 5 // Armadura pesada
            is Clerigo -> 4   // Armadura média
            is Ladino -> 2    // Armadura leve
            is Mago -> 0      // Sem armadura
            else -> 2
        }
        val ca = 10 + modDestreza + bonusArmadura
        
        // Base de Ataque
        val bac = nivel // Simplificado
        val bad = nivel
        
        // Modificadores de atributos
        val modForca = calcularModificador(atributos.forca)
        val modSabedoria = calcularModificador(atributos.sabedoria)
        
        // Jogadas de Proteção (simplificado)
        val jpc = 10 + calcularModificador(atributos.constituicao)
        val jps = 10 + calcularModificador(atributos.sabedoria)
        
        // Arma baseada na classe
        val arma = escolherArma(classe)
        
        return Combatente(
            personagem = personagem,
            pontosVida = pvMaximo,
            pontosVidaMaximo = pvMaximo,
            classeArmadura = ca,
            baseAtaqueCorpoACorpo = bac,
            baseAtaqueDistancia = bad,
            modificadorForca = modForca,
            modificadorDestreza = modDestreza,
            modificadorSabedoria = modSabedoria,
            jogadaProtecaoConstitui = jpc,
            jogadaProtecaoSabedoria = jps,
            arma = arma
        )
    }
    
    // Funções auxiliares privadas
    
    private fun gerarRacaAleatoria(): Raca {
        return when (Dado.rolarD6()) {
            1, 2 -> Humano()
            3 -> Elfo()
            4 -> Anao()
            5 -> Halfling()
            else -> Humano()
        }
    }
    
    private fun gerarClasseAleatoria(): ClassePersonagem {
        return when (Dado.rolarD6()) {
            1, 2, 3 -> Guerreiro()
            4 -> Clerigo()
            5 -> Mago()
            6 -> Ladino()
            else -> Guerreiro()
        }
    }
    
    private fun gerarAtributosAleatorios(): Atributos {
        return Atributos(
            forca = rolarAtributo(),
            destreza = rolarAtributo(),
            constituicao = rolarAtributo(),
            inteligencia = rolarAtributo(),
            sabedoria = rolarAtributo(),
            carisma = rolarAtributo()
        )
    }
    
    private fun gerarAtributosInimigo(desafio: Int): Atributos {
        val bonus = desafio * 2
        return Atributos(
            forca = rolarAtributo() + bonus,
            destreza = rolarAtributo() + bonus,
            constituicao = rolarAtributo() + bonus,
            inteligencia = rolarAtributo(),
            sabedoria = rolarAtributo(),
            carisma = rolarAtributo()
        )
    }
    
    private fun rolarAtributo(): Int {
        // 3d6 para gerar atributo
        return Dado.rolarD6() + Dado.rolarD6() + Dado.rolarD6()
    }
    
    private fun calcularModificador(atributo: Int): Int {
        return when {
            atributo <= 3 -> -3
            atributo <= 5 -> -2
            atributo <= 8 -> -1
            atributo <= 12 -> 0
            atributo <= 15 -> 1
            atributo <= 17 -> 2
            else -> 3
        }
    }
    
    private fun calcularPV(classe: ClassePersonagem, nivel: Int): Int {
        val dadoVida = when (classe) {
            is Guerreiro -> 10
            is Clerigo -> 8
            is Ladino -> 6
            is Mago -> 4
            else -> 8
        }
        
        // Primeiro nível é máximo, depois rola
        var pv = dadoVida
        for (i in 2..nivel) {
            pv += (1..dadoVida).random()
        }
        
        return maxOf(pv, nivel) // Mínimo de 1 PV por nível
    }
    
    private fun escolherArma(classe: ClassePersonagem): Arma {
        return when (classe) {
            is Guerreiro -> Arma(
                nome = "Espada Longa",
                dadoDano = "1d8",
                tipo = TipoArma.CORPO_A_CORPO,
                criticoMultiplicador = 2
            )
            is Clerigo -> Arma(
                nome = "Maça",
                dadoDano = "1d6",
                tipo = TipoArma.CORPO_A_CORPO,
                criticoMultiplicador = 2
            )
            is Ladino -> Arma(
                nome = "Adaga",
                dadoDano = "1d4",
                tipo = TipoArma.CORPO_A_CORPO,
                criticoMultiplicador = 3
            )
            is Mago -> Arma(
                nome = "Cajado",
                dadoDano = "1d4",
                tipo = TipoArma.CORPO_A_CORPO,
                criticoMultiplicador = 2
            )
            else -> Arma(
                nome = "Espada Curta",
                dadoDano = "1d6",
                tipo = TipoArma.CORPO_A_CORPO,
                criticoMultiplicador = 2
            )
        }
    }
}
