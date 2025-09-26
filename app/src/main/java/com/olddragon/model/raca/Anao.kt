package com.olddragon.model.raca

class Anao : Raca {
    override val nome = "Anão"
    override val movimento = 6
    override val infravisao = "18 metros"
    override val alinhamento = "Ordeiro"

    override fun aplicarBonusAtributos(atributos: MutableMap<String, Int>) {
        atributos["Constituição"] = atributos["Constituição"]!! + 2
        atributos["Carisma"] = atributos["Carisma"]!! - 1
    }

    override fun habilidadesEspeciais() = listOf("Resistência a venenos", "Conhecimento de pedra")
}