package com.olddragon.service.combate

import com.olddragon.model.Personagem
import com.olddragon.model.atributos.Atributos
import com.olddragon.model.classes.Guerreiro
import com.olddragon.model.combate.Arma
import com.olddragon.model.combate.Combatente
import com.olddragon.model.combate.TipoArma
import com.olddragon.model.raca.Humano

/**
 * Factory para criar combatentes de exemplo para testes
 */
object CombatenteFactory {
    
    /**
     * Cria um Guerreiro Humano básico
     */
    fun criarGuerreiroBasico(
        nome: String = "Guerreiro",
        pv: Int = 12,
        forca: Int = 16,
        destreza: Int = 12,
        constituicao: Int = 14,
        sabedoria: Int = 10
    ): Combatente {
        val atributos = Atributos(
            forca = forca,
            destreza = destreza,
            constituicao = constituicao,
            inteligencia = 10,
            sabedoria = sabedoria,
            carisma = 10
        )
        
        val personagem = Personagem(
            nome = nome,
            raca = Humano(),
            classe = Guerreiro(),
            atributos = atributos
        )
        
        val armaEspada = Arma(
            nome = "Espada Longa",
            dadoDano = "1d8",
            tipo = TipoArma.CORPO_A_CORPO,
            criticoMultiplicador = 2
        )
        
        return Combatente(
            personagem = personagem,
            pontosVida = pv,
            pontosVidaMaximo = pv,
            classeArmadura = 16, // CA com armadura
            baseAtaqueCorpoACorpo = 4, // BAC nível 1
            baseAtaqueDistancia = 3, // BAD nível 1
            modificadorForca = calcularModificador(forca),
            modificadorDestreza = calcularModificador(destreza),
            modificadorSabedoria = calcularModificador(sabedoria),
            jogadaProtecaoConstitui = 10 + calcularModificador(constituicao),
            jogadaProtecaoSabedoria = 10 + calcularModificador(sabedoria),
            arma = armaEspada
        )
    }
    
    /**
     * Cria um inimigo genérico (Goblin)
     */
    fun criarGoblin(
        nome: String = "Goblin"
    ): Combatente {
        val atributos = Atributos(
            forca = 10,
            destreza = 14,
            constituicao = 10,
            inteligencia = 8,
            sabedoria = 8,
            carisma = 6
        )
        
        val personagem = Personagem(
            nome = nome,
            raca = Humano(), // Placeholder
            classe = Guerreiro(), // Placeholder
            atributos = atributos
        )
        
        val armaAdaga = Arma(
            nome = "Adaga",
            dadoDano = "1d4",
            tipo = TipoArma.CORPO_A_CORPO
        )
        
        return Combatente(
            personagem = personagem,
            pontosVida = 5,
            pontosVidaMaximo = 5,
            classeArmadura = 13,
            baseAtaqueCorpoACorpo = 1,
            baseAtaqueDistancia = 1,
            modificadorForca = calcularModificador(10),
            modificadorDestreza = calcularModificador(14),
            modificadorSabedoria = calcularModificador(8),
            jogadaProtecaoConstitui = 10,
            jogadaProtecaoSabedoria = 9,
            arma = armaAdaga
        )
    }
    
    /**
     * Cria um combate de exemplo: 2 guerreiros vs 3 goblins
     */
    fun criarCombateExemplo(): Pair<List<Combatente>, List<Combatente>> {
        val aliados = listOf(
            criarGuerreiroBasico("Guerreiro A", pv = 12, forca = 16),
            criarGuerreiroBasico("Guerreiro B", pv = 10, forca = 14)
        )
        
        val inimigos = listOf(
            criarGoblin("Goblin 1"),
            criarGoblin("Goblin 2"),
            criarGoblin("Goblin 3")
        )
        
        return aliados to inimigos
    }
    
    /**
     * Recria o exemplo do documento: Guerreiro A vs Guerreiro B
     */
    fun criarCombateDocumento(): Pair<List<Combatente>, List<Combatente>> {
        // Guerreiro A: PV 12. CA 16. BAC 4. Arma: Espada Longa (Dano 1d8+2 FOR).
        val guerreiroA = criarGuerreiroBasico(
            nome = "Guerreiro A",
            pv = 12,
            forca = 16 // +2 MOD
        ).copy(
            classeArmadura = 16,
            baseAtaqueCorpoACorpo = 4,
            arma = Arma(
                nome = "Espada Longa",
                dadoDano = "1d8",
                tipo = TipoArma.CORPO_A_CORPO
            )
        )
        
        // Guerreiro B: PV 10. CA 14. BAC 3. Arma: Machado (Dano 1d8+1 FOR).
        val guerreiroB = criarGuerreiroBasico(
            nome = "Guerreiro B",
            pv = 10,
            forca = 14 // +1 MOD
        ).copy(
            classeArmadura = 14,
            baseAtaqueCorpoACorpo = 3,
            arma = Arma(
                nome = "Machado",
                dadoDano = "1d8",
                tipo = TipoArma.CORPO_A_CORPO
            )
        )
        
        return listOf(guerreiroA) to listOf(guerreiroB)
    }
    
    /**
     * Calcula o modificador de atributo (Old Dragon 2)
     * 3-5: -2, 6-8: -1, 9-12: 0, 13-15: +1, 16-18: +2
     */
    private fun calcularModificador(atributo: Int): Int {
        return when (atributo) {
            in 3..5 -> -2
            in 6..8 -> -1
            in 9..12 -> 0
            in 13..15 -> 1
            in 16..18 -> 2
            else -> 0
        }
    }
}
