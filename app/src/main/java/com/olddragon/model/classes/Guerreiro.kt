package com.olddragon.model.classes

class Guerreiro : ClassePersonagem {
    override val nome = "Guerreiro"

    override fun aplicarBonusClasse(atributos: MutableMap<String, Int>) {
        atributos["Força"] = atributos["Força"]!! + 2
    }

    override fun habilidadesDeClasse() = listOf("Uso de armaduras pesadas", "Ataque poderoso")
}
