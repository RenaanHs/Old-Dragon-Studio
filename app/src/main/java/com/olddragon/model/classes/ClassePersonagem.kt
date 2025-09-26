package com.olddragon.model.classes

interface ClassePersonagem {
    val nome: String
    fun aplicarBonusClasse(atributos: MutableMap<String, Int>)
    fun habilidadesDeClasse(): List<String>
}
