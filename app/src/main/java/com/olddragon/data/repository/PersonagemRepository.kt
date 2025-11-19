package com.olddragon.data.repository

import com.olddragon.data.dao.PersonagemDao
import com.olddragon.data.entity.PersonagemEntity
import com.olddragon.data.entity.toEntity
import com.olddragon.data.entity.toPersonagem
import com.olddragon.model.Personagem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository - Camada intermediária entre ViewModel e Database
 * Encapsula a lógica de acesso aos dados
 */
class PersonagemRepository(private val personagemDao: PersonagemDao) {

    /**
     * Observa todos os personagens salvos
     */
    val todosPersonagens: Flow<List<Personagem>> =
        personagemDao.buscarTodos().map { entities ->
            entities.map { it.toPersonagem() }
        }

    /**
     * Salva um novo personagem
     * @return ID do personagem salvo
     */
    suspend fun salvar(personagem: Personagem): Long {
        return personagemDao.inserir(personagem.toEntity())
    }

    /**
     * Atualiza personagem existente
     */
    suspend fun atualizar(personagem: Personagem, id: Long) {
        personagemDao.atualizar(personagem.toEntity(id))
    }

    /**
     * Deleta personagem convertendo de Personagem para Entity
     */
    suspend fun deletar(personagem: Personagem) {
        // Busca a entity pelo nome do personagem para pegar o ID correto
        val entity = personagemDao.buscarPorNomeExato(personagem.nome)
        entity?.let {
            personagemDao.deletar(it)
        }
    }

    /**
     * Deleta personagem entity diretamente
     */
    suspend fun deletarEntity(personagem: PersonagemEntity) {
        personagemDao.deletar(personagem)
    }

    /**
     * Busca personagem por ID
     */
    suspend fun buscarPorId(id: Long): Personagem? {
        return personagemDao.buscarPorId(id)?.toPersonagem()
    }

    /**
     * Busca personagens por raça
     */
    fun buscarPorRaca(raca: String): Flow<List<Personagem>> {
        return personagemDao.buscarPorRaca(raca).map { entities ->
            entities.map { it.toPersonagem() }
        }
    }

    /**
     * Busca personagens por classe
     */
    fun buscarPorClasse(classe: String): Flow<List<Personagem>> {
        return personagemDao.buscarPorClasse(classe).map { entities ->
            entities.map { it.toPersonagem() }
        }
    }

    /**
     * Busca personagens por nome
     */
    fun buscarPorNome(nome: String): Flow<List<Personagem>> {
        return personagemDao.buscarPorNome(nome).map { entities ->
            entities.map { it.toPersonagem() }
        }
    }

    /**
     * Conta total de personagens salvos
     */
    suspend fun contarPersonagens(): Int {
        return personagemDao.contarPersonagens()
    }

    /**
     * Deleta todos os personagens
     */
    suspend fun deletarTodos() {
        personagemDao.deletarTodos()
    }
}
