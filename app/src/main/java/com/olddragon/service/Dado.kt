package com.olddragon.service

import kotlin.random.Random


object Dado {
    private val random = Random.Default

    fun rolar(qtd: Int, faces: Int): List<Int> {
        return List(qtd) { random.nextInt(faces) + 1 }
    }

    fun rolarComTotal(qtd: Int, faces: Int): Pair<List<Int>, Int> {
        val dados = rolar(qtd, faces)
        return dados to dados.sum()
    }

    fun rolarDescartaMenor(qtd: Int, faces: Int): Pair<List<Int>, Int> {
        val dados = rolar(qtd, faces)
        val total = dados.sortedDescending().take(qtd - 1).sum()
        return dados to total
    }
    
    // Atalhos para dados comuns
    fun rolarD4(): Int = rolar(1, 4)[0]
    fun rolarD6(): Int = rolar(1, 6)[0]
    fun rolarD8(): Int = rolar(1, 8)[0]
    fun rolarD10(): Int = rolar(1, 10)[0]
    fun rolarD12(): Int = rolar(1, 12)[0]
    fun rolarD20(): Int = rolar(1, 20)[0]
    fun rolarD100(): Int = rolar(1, 100)[0]
    
    // Rola múltiplos dados e retorna total
    fun rolarXd6(quantidade: Int): Int = rolar(quantidade, 6).sum()
    fun rolarXd8(quantidade: Int): Int = rolar(quantidade, 8).sum()
    fun rolarXd10(quantidade: Int): Int = rolar(quantidade, 10).sum()
    fun rolarXd20(quantidade: Int): Int = rolar(quantidade, 20).sum()
}