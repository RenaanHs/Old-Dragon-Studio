package com.olddragon.model.raca

class Halfling : Raca {
    override val nome = "Halfling"
    override val movimento = 6
    override val infravisao = "Não possui"
    override val alinhamento = "Ordeiro ou Neutro"

    override fun aplicarBonusAtributos(atributos: MutableMap<String, Int>) {
        atributos["Destreza"] = atributos["Destreza"]!! + 2
        atributos["Força"] = atributos["Força"]!! - 1
    }

    override fun habilidadesEspeciais() = listOf("Furtividade natural", "Bônus contra criaturas grandes")
}