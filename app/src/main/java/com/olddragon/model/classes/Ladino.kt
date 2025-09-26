package com.olddragon.model.classes

class Ladino : ClassePersonagem {
    override val nome = "Ladrão"

    override fun aplicarBonusClasse(atributos: MutableMap<String, Int>) {
        atributos["Destreza"] = atributos["Destreza"]!! + 2
    }

    override fun habilidadesDeClasse() = listOf("Furtividade", "Desarmar armadilhas")
}