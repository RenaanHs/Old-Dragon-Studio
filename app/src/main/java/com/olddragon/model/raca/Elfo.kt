package com.olddragon.model.raca

class Elfo : Raca {
    override val nome = "Elfo"
    override val movimento = 9
    override val infravisao = "18 metros"
    override val alinhamento = "Tendem a neutralidade"

    override fun aplicarBonusAtributos(atributos: MutableMap<String, Int>) {
        atributos["Destreza"] = atributos["Destreza"]!! + 2
        atributos["Constituição"] = atributos["Constituição"]!! - 1
    }

    override fun habilidadesEspeciais() = listOf("Visão aguçada", "Resistência a encantamentos")
}