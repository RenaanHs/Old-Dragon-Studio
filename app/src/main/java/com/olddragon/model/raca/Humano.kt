package com.olddragon.model.raca

class Humano : Raca {
    override val nome = "Humano"
    override val movimento = 9
    override val infravisao = "Não possui"
    override val alinhamento = "Qualquer"

    override fun aplicarBonusAtributos(atributos: MutableMap<String, Int>) {
        // Humanos recebem +1 em todos os atributos
        atributos.keys.forEach { atributos[it] = atributos[it]!! + 1 }
    }

    override fun habilidadesEspeciais() = listOf("Versatilidade")
}