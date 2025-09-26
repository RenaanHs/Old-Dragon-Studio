package com.olddragon.model.classes

class Mago : ClassePersonagem {
    override val nome = "Mago"

    override fun aplicarBonusClasse(atributos: MutableMap<String, Int>) {
        atributos["Inteligência"] = atributos["Inteligência"]!! + 2
    }

    override fun habilidadesDeClasse() = listOf("Uso de magias arcanas", "Conhecimento arcano")
}