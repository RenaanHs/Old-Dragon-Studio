package com.olddragon.controller

import com.olddragon.model.Personagem
import com.olddragon.model.atributos.Atributos
import com.olddragon.model.classes.*
import com.olddragon.model.raca.*
import com.olddragon.service.Dado

class CharacterController {

    // Listas de dados (parte do controle de dados)
    private val racasDisponiveis = listOf(
        Humano(),
        Elfo(),
        Anao(),
        Halfling()
    )

    private val classesDisponiveis = listOf(
        Guerreiro(),
        Mago(),
        Ladino(),
        Clerigo()
    )

    /**
     * MÉTODO ORIGINAL - mantém compatibilidade total com sua tela atual
     */
    fun criarPersonagem(
        nome: String,
        raca: String,
        classe: String,
        atributos: Atributos
    ): Personagem {
        val racaObj = when (raca) {
            "Elfo" -> Elfo()
            "Anão" -> Anao()
            "Halfling" -> Halfling()
            else -> Humano()
        }

        val classeObj = when (classe) {
            "Mago" -> Mago()
            "Clérigo" -> Clerigo()
            "Ladino" -> Ladino()
            else -> Guerreiro()
        }

        return Personagem(
            nome = nome,
            raca = racaObj,
            classe = classeObj,
            atributos = atributos
        )
    }

    // ===== MÉTODOS ADICIONAIS PARA COMPLETAR MVC =====
    // (Sua tela não usa estes, mas eles completam a arquitetura)

    /**
     * Versão com validação e tratamento de erros (MVC completo)
     */
    fun criarPersonagemComValidacao(
        nome: String,
        raca: String,
        classe: String,
        atributos: Atributos
    ): Result<Personagem> {

        // Validações do Controller
        if (nome.isBlank()) {
            return Result.failure(IllegalArgumentException("Nome não pode estar vazio"))
        }

        if (!validarAtributos(atributos)) {
            return Result.failure(IllegalArgumentException("Atributos devem estar entre 3 e 18"))
        }

        val racaObj = obterRacaPorNome(raca)
        val classeObj = obterClassePorNome(classe)

        val personagem = Personagem(
            nome = nome,
            raca = racaObj,
            classe = classeObj,
            atributos = atributos
        )

        return Result.success(personagem)
    }

    /**
     * Gera atributos no estilo clássico
     */
    fun gerarAtributosClassico(): Pair<Atributos, List<String>> {
        val logs = mutableListOf<String>()

        val forca = rolarComLog("Força", 3, 6, logs)
        val destreza = rolarComLog("Destreza", 3, 6, logs)
        val constituicao = rolarComLog("Constituição", 3, 6, logs)
        val inteligencia = rolarComLog("Inteligência", 3, 6, logs)
        val sabedoria = rolarComLog("Sabedoria", 3, 6, logs)
        val carisma = rolarComLog("Carisma", 3, 6, logs)

        val atributos = Atributos(forca, destreza, constituicao, inteligencia, sabedoria, carisma)

        return Pair(atributos, logs)
    }

    /**
     * Gera valores para distribuição (aventureiro)
     */
    fun gerarValoresAventureiro(): Pair<List<Int>, List<String>> {
        val valores = mutableListOf<Int>()
        val logs = mutableListOf<String>()

        repeat(6) { i ->
            val (dados, total) = Dado.rolarComTotal(3, 6)
            logs.add("Rolagem ${i+1}: $dados → $total")
            valores.add(total)
        }

        return Pair(valores, logs)
    }

    /**
     * Gera valores heróicos (4d6 descarta menor)
     */
    fun gerarValoresHeroico(): Pair<List<Int>, List<String>> {
        val valores = mutableListOf<Int>()
        val logs = mutableListOf<String>()

        repeat(6) { i ->
            val (dados, total) = Dado.rolarDescartaMenor(4, 6)
            logs.add("Rolagem ${i+1}: $dados → Descarta menor → $total")
            valores.add(total)
        }

        return Pair(valores, logs)
    }

    /**
     * Escolhe classe automaticamente baseada nos atributos (modo clássico)
     */
    fun escolherClasseAutomatica(atributos: Atributos): String {
        val possiveis = mutableListOf<String>()

        if (atributos.forca >= 9) possiveis.add("Guerreiro")
        if (atributos.destreza >= 9) possiveis.add("Ladino")
        if (atributos.inteligencia >= 9) possiveis.add("Mago")
        if (atributos.sabedoria >= 9) possiveis.add("Clérigo")

        return if (possiveis.isEmpty()) "Ladino" else possiveis.first()
    }

    /**
     * Valida se personagem atende requisitos mínimos da classe
     */
    fun validarRequisitosClasse(classe: String, atributos: Atributos): Boolean {
        return when (classe) {
            "Guerreiro" -> atributos.forca >= 9
            "Ladino" -> atributos.destreza >= 9
            "Mago" -> atributos.inteligencia >= 9
            "Clérigo" -> atributos.sabedoria >= 9
            else -> true
        }
    }

    /**
     * Obtém lista de raças disponíveis
     */
    fun getRacasDisponiveis(): List<String> {
        return racasDisponiveis.map { it.nome }
    }

    /**
     * Obtém lista de classes disponíveis
     */
    fun getClassesDisponiveis(): List<String> {
        return classesDisponiveis.map { it.nome }
    }

    // ===== MÉTODOS PRIVADOS DE APOIO =====

    private fun obterRacaPorNome(nome: String): Raca {
        return when (nome) {
            "Elfo" -> Elfo()
            "Anão" -> Anao()
            "Halfling" -> Halfling()
            else -> Humano()
        }
    }

    private fun obterClassePorNome(nome: String): ClassePersonagem {
        return when (nome) {
            "Mago" -> Mago()
            "Clérigo" -> Clerigo()
            "Ladino" -> Ladino()
            else -> Guerreiro()
        }
    }

    private fun validarAtributos(atributos: Atributos): Boolean {
        val valores = listOf(
            atributos.forca,
            atributos.destreza,
            atributos.constituicao,
            atributos.inteligencia,
            atributos.sabedoria,
            atributos.carisma
        )

        // Verifica se todos os valores estão entre 3 e 18 (range típico de RPG)
        return valores.all { it in 3..18 }
    }

    private fun rolarComLog(nome: String, qtd: Int, faces: Int, logs: MutableList<String>): Int {
        val (dados, total) = Dado.rolarComTotal(qtd, faces)
        logs.add("$nome: $dados → $total")
        return total
    }
}