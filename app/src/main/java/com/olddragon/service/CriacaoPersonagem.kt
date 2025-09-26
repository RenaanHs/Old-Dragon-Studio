package com.olddragon.service

import com.olddragon.model.Personagem
import com.olddragon.model.atributos.Atributos
import com.olddragon.model.classes.ClassePersonagem
import com.olddragon.model.raca.Raca
import com.olddragon.model.raca.*
import com.olddragon.model.classes.*
import java.util.Scanner

// Instâncias de raças
private val racasDisponiveis = listOf(
    Elfo(),
    Anao(),
    Humano(),
    Halfling()
)

// Instâncias de classes
private val classesDisponiveis = listOf(
    Guerreiro(),
    Mago(),
    Ladino(),
    Clerigo()
)

object CriacaoPersonagem {

    private val scanner = Scanner(System.`in`)

    fun criarPersonagem(): Personagem {
        println("=== Criação de Personagem OldDragon ===")

        print("Digite o nome do personagem: ")
        val nome = scanner.nextLine()

        val estilo = escolherEstiloAtributos()
        val atributos: Atributos
        val classe: ClassePersonagem

        if (estilo == "CLÁSSICO") {
            atributos = gerarClassico()
            classe = escolherClasseAutomaticamente(atributos)
        } else {
            val valores = if (estilo == "AVENTUREIRO") {
                rolarVarias(6, 3, 6)
            } else {
                rolarVariasDescartaMenor(6)
            }
            atributos = gerarComDistribuicao(valores)
            classe = escolherClasse()
        }

        val raca = escolherRaca()

        return Personagem(nome, raca, classe, atributos)
    }

    private fun lerNumero(): Int {
        return try {
            scanner.nextLine().toInt()
        } catch (e: NumberFormatException) {
            -1
        }
    }

    private fun escolherRaca(): Raca {
        while (true) {
            println("Escolha a raça:")
            racasDisponiveis.forEachIndexed { i, r -> println("${i + 1} - ${r.nome}") }

            val escolha = lerNumero()
            if (escolha in 1..racasDisponiveis.size) {
                return racasDisponiveis[escolha - 1]
            } else {
                println("❌ Opção inválida! Digite um número entre 1 e ${racasDisponiveis.size}.")
            }
        }
    }

    private fun escolherClasse(): ClassePersonagem {
        while (true) {
            println("\nEscolha a classe:")
            classesDisponiveis.forEachIndexed { i, c -> println("${i + 1} - ${c.nome}") }

            val escolha = lerNumero()
            if (escolha in 1..classesDisponiveis.size) {
                return classesDisponiveis[escolha - 1]
            }
            println("❌ Opção inválida! Digite um número entre 1 e ${classesDisponiveis.size}.")
        }
    }

    private fun escolherClasseAutomaticamente(atributos: Atributos): ClassePersonagem {
        println("\n=== Escolha automática de classe ===")
        println("Seus atributos: $atributos")

        val possiveis = mutableListOf<ClassePersonagem>()

        if (atributos.forca >= 9) possiveis.add(Guerreiro())
        if (atributos.destreza >= 9) possiveis.add(Ladino())
        if (atributos.inteligencia >= 9) possiveis.add(Mago())
        if (atributos.sabedoria >= 9) possiveis.add(Clerigo())

        if (possiveis.isEmpty()) {
            println("Nenhuma classe atende aos requisitos, você será um LADINO por padrão.")
            return Ladino()
        }

        println("Com base nos seus atributos, você pode ser:")
        possiveis.forEach { println("- ${it.nome}") }

        return possiveis.first()
    }

    private fun escolherEstiloAtributos(): String {
        println("\nEscolha o estilo de geração de atributos:")
        println("1 - Clássico (3d6 na ordem)")
        println("2 - Aventureiro (3d6 e distribuir)")
        println("3 - Heróico (4d6 descartando o menor e distribuir)")

        val escolha = try {
            scanner.nextInt()
        } catch (e: Exception) {
            scanner.nextLine() // limpa buffer
            -1 // valor inválido
        }

        scanner.nextLine() // limpa buffer de quebra de linha

        return when (escolha) {
            1 -> "CLÁSSICO"
            2 -> "AVENTUREIRO"
            3 -> "HERÓICO"
            else -> {
                println("Opção inválida! Estilo definido como CLÁSSICO por padrão.")
                "CLÁSSICO"
            }
        }
    }

    // ======== Estilo Clássico ========
    private fun gerarClassico(): Atributos {
        println("\nEstilo Clássico - Rolando 3d6 na ordem")
        return Atributos(
            rolarComExibicao("Força", 3, 6),
            rolarComExibicao("Destreza", 3, 6),
            rolarComExibicao("Constituição", 3, 6),
            rolarComExibicao("Inteligência", 3, 6),
            rolarComExibicao("Sabedoria", 3, 6),
            rolarComExibicao("Carisma", 3, 6)
        )
    }

    // ======== Estilos com Distribuição ========
    private fun gerarComDistribuicao(valores: MutableList<Int>): Atributos {
        println("\nValores obtidos: $valores")
        val atributos = mutableMapOf<String, Int>()

        val nomesAtributos = listOf("Força", "Destreza", "Constituição", "Inteligência", "Sabedoria", "Carisma")
        nomesAtributos.forEach { atributo ->
            println("\nEscolha um valor para $atributo: ")
            valores.forEachIndexed { i, v -> println("${i + 1} - $v") }
            val escolha = scanner.nextInt()
            scanner.nextLine()
            atributos[atributo] = valores.removeAt(escolha - 1)
        }

        return Atributos(
            atributos["Força"]!!,
            atributos["Destreza"]!!,
            atributos["Constituição"]!!,
            atributos["Inteligência"]!!,
            atributos["Sabedoria"]!!,
            atributos["Carisma"]!!
        )
    }

    // ======== Funções de rolagem ========
    private fun rolarComExibicao(nome: String, qtd: Int, faces: Int): Int {
        val (dados, total) = Dado.rolarComTotal(qtd, faces)
        println("$nome: Rolou $dados → Total: $total")
        return total
    }

    private fun rolarVarias(qtdValores: Int, qtdDados: Int, faces: Int): MutableList<Int> {
        val resultados = mutableListOf<Int>()
        repeat(qtdValores) {
            val (dados, total) = Dado.rolarComTotal(qtdDados, faces)
            println("Rolagem: $dados → Total: $total")
            resultados.add(total)
        }
        return resultados
    }

    private fun rolarVariasDescartaMenor(qtdValores: Int): MutableList<Int> {
        val resultados = mutableListOf<Int>()
        repeat(qtdValores) {
            val (dados, total) = Dado.rolarDescartaMenor(4, 6)
            println("Rolagem: $dados → Descarta menor → Total: $total")
            resultados.add(total)
        }
        return resultados
    }
}
