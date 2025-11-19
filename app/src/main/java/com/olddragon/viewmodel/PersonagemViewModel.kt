package com.olddragon.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.olddragon.data.database.AppDatabase
import com.olddragon.data.repository.PersonagemRepository
import com.olddragon.model.Personagem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar personagens
 * Sobrevive a mudanças de configuração (rotação de tela, etc)
 */
class PersonagemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PersonagemRepository

    // Lista de personagens salvos
    private val _personagens = MutableStateFlow<List<Personagem>>(emptyList())
    val personagens: StateFlow<List<Personagem>> = _personagens.asStateFlow()

    // Estado de salvamento
    private val _salvamentoStatus = MutableStateFlow<SalvamentoStatus>(SalvamentoStatus.Idle)
    val salvamentoStatus: StateFlow<SalvamentoStatus> = _salvamentoStatus.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).personagemDao()
        repository = PersonagemRepository(dao)

        // Carrega personagens automaticamente
        carregarPersonagens()
    }

    /**
     * Carrega todos os personagens do banco
     */
    private fun carregarPersonagens() {
        viewModelScope.launch {
            repository.todosPersonagens.collect { lista ->
                _personagens.value = lista
            }
        }
    }

    /**
     * Salva novo personagem
     */
    fun salvarPersonagem(personagem: Personagem) {
        viewModelScope.launch {
            try {
                _salvamentoStatus.value = SalvamentoStatus.Salvando
                val id = repository.salvar(personagem)
                _salvamentoStatus.value = SalvamentoStatus.Sucesso(id)
            } catch (e: Exception) {
                _salvamentoStatus.value = SalvamentoStatus.Erro(e.message ?: "Erro desconhecido")
            }
        }
    }

    /**
     * Atualiza personagem existente
     */
    fun atualizarPersonagem(personagem: Personagem, id: Long) {
        viewModelScope.launch {
            try {
                _salvamentoStatus.value = SalvamentoStatus.Salvando
                repository.atualizar(personagem, id)
                _salvamentoStatus.value = SalvamentoStatus.Sucesso(id)
            } catch (e: Exception) {
                _salvamentoStatus.value = SalvamentoStatus.Erro(e.message ?: "Erro ao atualizar")
            }
        }
    }

    /**
     * Deleta um personagem específico
     */
    fun deletarPersonagem(personagem: Personagem) {
        viewModelScope.launch {
            try {
                repository.deletar(personagem)
            } catch (e: Exception) {
                _salvamentoStatus.value = SalvamentoStatus.Erro(e.message ?: "Erro ao excluir")
            }
        }
    }

    /**
     * Busca personagem por ID
     */
    suspend fun buscarPersonagemPorId(id: Long): Personagem? {
        return repository.buscarPorId(id)
    }

    /**
     * Filtra personagens por raça
     */
    fun filtrarPorRaca(raca: String) {
        viewModelScope.launch {
            repository.buscarPorRaca(raca).collect { lista ->
                _personagens.value = lista
            }
        }
    }

    /**
     * Filtra personagens por classe
     */
    fun filtrarPorClasse(classe: String) {
        viewModelScope.launch {
            repository.buscarPorClasse(classe).collect { lista ->
                _personagens.value = lista
            }
        }
    }

    /**
     * Busca personagens por nome
     */
    fun buscarPorNome(nome: String) {
        viewModelScope.launch {
            repository.buscarPorNome(nome).collect { lista ->
                _personagens.value = lista
            }
        }
    }

    /**
     * Reseta status de salvamento
     */
    fun resetarStatus() {
        _salvamentoStatus.value = SalvamentoStatus.Idle
    }

    /**
     * Deleta todos os personagens
     */
    fun deletarTodos() {
        viewModelScope.launch {
            repository.deletarTodos()
        }
    }
}

/**
 * Estados possíveis do salvamento
 */
sealed class SalvamentoStatus {
    object Idle : SalvamentoStatus()
    object Salvando : SalvamentoStatus()
    data class Sucesso(val id: Long) : SalvamentoStatus()
    data class Erro(val mensagem: String) : SalvamentoStatus()
}
