package com.olddragon.model

import com.olddragon.model.atributos.Atributos
import com.olddragon.model.classes.ClassePersonagem
import com.olddragon.model.raca.Raca

data class Personagem(
    val nome: String,
    val raca: Raca,
    val classe: ClassePersonagem,
    val atributos: Atributos
)
