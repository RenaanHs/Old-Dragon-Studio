package com.olddragon.model.classes

class Clerigo : ClassePersonagem {
    override val nome = "Clérigo"

    override fun aplicarBonusClasse(atributos: MutableMap<String, Int>) {
        atributos["Sabedoria"] = atributos["Sabedoria"]!! + 2
    }

    override fun habilidadesDeClasse() = listOf(
        "Uso de magias divinas",
        "Expulsar mortos-vivos",
        "Proficiente com armaduras médias e leves"
    )
}