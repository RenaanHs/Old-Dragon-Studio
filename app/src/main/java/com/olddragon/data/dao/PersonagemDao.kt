package com.olddragon.data.dao

import androidx.room.*
import com.olddragon.data.entity.PersonagemEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO - Interface de acesso aos dados de personagens
 */
@Dao
interface PersonagemDao {

    /**
     * Inserir novo personagem
     * @return ID do personagem inserido
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(personagem: PersonagemEntity): Long

    /**
     * Atualizar personagem existente
     */
    @Update
    suspend fun atualizar(personagem: PersonagemEntity)

    /**
     * Deletar personagem
     */
    @Delete
    suspend fun deletar(personagem: PersonagemEntity)

    /**
     * Buscar personagem por ID
     */
    @Query("SELECT * FROM personagens WHERE id = :id")
    suspend fun buscarPorId(id: Long): PersonagemEntity?

    /**
     * Buscar personagem por nome exato
     */
    @Query("SELECT * FROM personagens WHERE nome = :nome LIMIT 1")
    suspend fun buscarPorNomeExato(nome: String): PersonagemEntity?

    /**
     * Buscar todos os personagens
     * Flow permite observar mudanças em tempo real
     */
    @Query("SELECT * FROM personagens ORDER BY dataCriacao DESC")
    fun buscarTodos(): Flow<List<PersonagemEntity>>

    /**
     * Buscar personagens por raça
     */
    @Query("SELECT * FROM personagens WHERE raca = :raca ORDER BY nome")
    fun buscarPorRaca(raca: String): Flow<List<PersonagemEntity>>

    /**
     * Buscar personagens por classe
     */
    @Query("SELECT * FROM personagens WHERE classe = :classe ORDER BY nome")
    fun buscarPorClasse(classe: String): Flow<List<PersonagemEntity>>

    /**
     * Buscar personagem por nome (busca parcial)
     */
    @Query("SELECT * FROM personagens WHERE nome LIKE '%' || :nome || '%'")
    fun buscarPorNome(nome: String): Flow<List<PersonagemEntity>>

    /**
     * Deletar todos os personagens
     */
    @Query("DELETE FROM personagens")
    suspend fun deletarTodos()

    /**
     * Contar total de personagens
     */
    @Query("SELECT COUNT(*) FROM personagens")
    suspend fun contarPersonagens(): Int
}
