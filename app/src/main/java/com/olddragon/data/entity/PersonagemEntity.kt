package com.olddragon.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.olddragon.model.Personagem
import com.olddragon.model.atributos.Atributos
import com.olddragon.model.classes.*
import com.olddragon.model.raca.*

/**
 * Entidade Room para salvar personagens no banco de dados local
 */
@Entity(tableName = "personagens")
data class PersonagemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nome: String,
    val raca: String,
    val classe: String,

    // Atributos
    val forca: Int,
    val destreza: Int,
    val constituicao: Int,
    val inteligencia: Int,
    val sabedoria: Int,
    val carisma: Int,

    // Metadata
    val dataCriacao: Long = System.currentTimeMillis()
)

/**
 * Conversores entre PersonagemEntity e Personagem
 */
fun PersonagemEntity.toPersonagem(): Personagem {
    val racaObj = when (raca) {
        "Elfo" -> Elfo()
        "Anão" -> Anao()
        "Halfling" -> Halfling()
        else -> Humano()
    }

    val classeObj = when (classe) {
        "Mago" -> Mago()
        "Clérigo" -> Clerigo()
        "Ladino" -> Ladino()
        else -> Guerreiro()
    }

    val atributos = Atributos(
        forca = forca,
        destreza = destreza,
        constituicao = constituicao,
        inteligencia = inteligencia,
        sabedoria = sabedoria,
        carisma = carisma
    )

    return Personagem(
        nome = nome,
        raca = racaObj,
        classe = classeObj,
        atributos = atributos
    )
}

fun Personagem.toEntity(id: Long = 0): PersonagemEntity {
    return PersonagemEntity(
        id = id,
        nome = nome,
        raca = raca.nome,
        classe = classe.nome,
        forca = atributos.forca,
        destreza = atributos.destreza,
        constituicao = atributos.constituicao,
        inteligencia = atributos.inteligencia,
        sabedoria = atributos.sabedoria,
        carisma = atributos.carisma
    )
}