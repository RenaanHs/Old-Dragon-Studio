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
}