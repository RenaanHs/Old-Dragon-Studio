package com.olddragon.model.raca

interface Raca {
    val nome: String
    val movimento: Int
    val infravisao: String
    val alinhamento: String
    fun aplicarBonusAtributos(atributos: MutableMap<String, Int>)
    fun habilidadesEspeciais(): List<String>
}