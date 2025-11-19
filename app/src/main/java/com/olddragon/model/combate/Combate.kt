package com.olddragon.model.combate

/**
 * Representa o estado completo de um combate
 */
data class Combate(
    val id: String,
    val combatentesAliados: List<Combatente>, // Jogadores/aliados
    val combatentesInimigos: List<Combatente>, // Inimigos
    var rodadaAtual: Int = 0,
    var fase: FaseCombate = FaseCombate.PREPARACAO,
    var historico: MutableList<EventoCombate> = mutableListOf(),
    var vencedor: LadoCombate? = null
) {
    fun todosOsCombatentes() = combatentesAliados + combatentesInimigos
    
    fun combatentesPorId(): Map<String, Combatente> {
        return todosOsCombatentes().associateBy { it.personagem.nome }
    }
    
    fun combatentesAtivos(): List<Combatente> {
        return todosOsCombatentes().filter { it.estaVivo() && it.estaConsciente() }
    }
    
    fun combatentesAtivosPorLado(lado: LadoCombate): List<Combatente> {
        val lista = when (lado) {
            LadoCombate.ALIADOS -> combatentesAliados
            LadoCombate.INIMIGOS -> combatentesInimigos
        }
        return lista.filter { it.estaVivo() && it.estaConsciente() }
    }
    
    fun verificarFimDeCombate(): LadoCombate? {
        val aliadosAtivos = combatentesAtivosPorLado(LadoCombate.ALIADOS)
        val inimigosAtivos = combatentesAtivosPorLado(LadoCombate.INIMIGOS)
        
        return when {
            aliadosAtivos.isEmpty() && inimigosAtivos.isNotEmpty() -> LadoCombate.INIMIGOS
            inimigosAtivos.isEmpty() && aliadosAtivos.isNotEmpty() -> LadoCombate.ALIADOS
            aliadosAtivos.isEmpty() && inimigosAtivos.isEmpty() -> null // Empate
            else -> null // Combate continua
        }
    }
    
    fun adicionarEvento(evento: EventoCombate) {
        historico.add(evento)
    }
}

/**
 * Fases do combate
 */
enum class FaseCombate {
    PREPARACAO,          // Antes de iniciar
    VERIFICACAO_SURPRESA, // Verificando se alguém está surpreso
    DETERMINACAO_INICIATIVA, // Rolando iniciativa
    EXECUCAO_ACOES,      // Executando ações dos combatentes
    FIM_RODADA,          // Verificações de fim de rodada
    FINALIZADO           // Combate encerrado
}

/**
 * Lado do combate
 */
enum class LadoCombate {
    ALIADOS,
    INIMIGOS
}

/**
 * Evento que ocorreu durante o combate (para histórico)
 */
sealed class EventoCombate {
    abstract val rodada: Int
    abstract val descricao: String
    
    data class InicioCombate(
        override val rodada: Int,
        override val descricao: String = "O combate começou!"
    ) : EventoCombate()
    
    data class Surpresa(
        override val rodada: Int,
        val ladoSurpreendido: LadoCombate,
        override val descricao: String
    ) : EventoCombate()
    
    data class Iniciativa(
        override val rodada: Int,
        val combatente: String,
        val resultado: Boolean,
        val rolagem: Int,
        override val descricao: String
    ) : EventoCombate()
    
    data class Ataque(
        override val rodada: Int,
        val atacante: String,
        val alvo: String,
        val rolagemAtaque: Int,
        val totalAtaque: Int,
        val acertou: Boolean,
        val critico: Boolean,
        val dano: Int,
        override val descricao: String
    ) : EventoCombate()
    
    data class Movimento(
        override val rodada: Int,
        val combatente: String,
        val distancia: Int,
        override val descricao: String
    ) : EventoCombate()
    
    data class Dano(
        override val rodada: Int,
        val alvo: String,
        val quantidade: Int,
        val pvRestante: Int,
        override val descricao: String
    ) : EventoCombate()
    
    data class Morte(
        override val rodada: Int,
        val combatente: String,
        override val descricao: String
    ) : EventoCombate()
    
    data class TesteAgonizar(
        override val rodada: Int,
        val combatente: String,
        val rolagem: Int,
        val sucesso: Boolean,
        override val descricao: String
    ) : EventoCombate()
    
    data class TesteMoral(
        override val rodada: Int,
        val lado: LadoCombate,
        val sucesso: Boolean,
        override val descricao: String
    ) : EventoCombate()
    
    data class Fuga(
        override val rodada: Int,
        val combatente: String,
        override val descricao: String
    ) : EventoCombate()
    
    data class FimCombate(
        override val rodada: Int,
        val vencedor: LadoCombate?,
        override val descricao: String
    ) : EventoCombate()
}
