# Sistema de Combate - Old Dragon 2

## Visão Geral

Este sistema implementa as regras de combate do Old Dragon 2, seguindo fielmente o processo descrito no manual do jogo. O combate é organizado em ciclos de rodadas de 10 segundos, onde cada participante realiza suas ações seguindo uma ordem determinada pela iniciativa.

## Arquitetura

### Camada de Modelo (`model/combate`)

- **`Combatente`**: Representa um participante do combate com todos seus atributos
- **`Combate`**: Estado completo do combate incluindo participantes, rodada atual e histórico
- **`Arma`**: Equipamento usado pelos combatentes
- **`EventoCombate`**: Eventos que ocorrem durante o combate (ataques, dano, morte, etc.)

### Camada de Serviço (`service/combate`)

- **`CombateService`**: Lógica central do sistema de combate
- **`CombateBackgroundService`**: Execução de combates em segundo plano com controle de velocidade
- **`GeradorCombatente`**: Gera combatentes aleatórios para testes
- **`CombatenteFactory`**: Cria combatentes específicos para cenários

### Camada de ViewModel (`viewmodel`)

- **`CombateViewModel`**: Gerencia o estado do combate na UI com observação reativa

## Processo de Combate

O combate segue 6 passos principais:

### 1. Verificação de Surpresa
```kotlin
combate = combateService.verificarSurpresa(combate)
```
- Rola 1d6 para cada lado
- Resultado 1-2: lado é pego de surpresa
- Lado surpreso perde ação na primeira rodada

### 2. Determinação da Iniciativa
```kotlin
combate = combateService.determinarIniciativa(combate)
```
- Cada combatente rola 1d20
- Testa contra atributo (Destreza ou Sabedoria, o maior)
- **Sucesso**: age antes dos inimigos
- **Falha**: age depois dos inimigos

### 3. Execução das Ações
```kotlin
combate = combateService.executarRodada(combate)
```
- Ordem: Sucessos na iniciativa → Inimigos → Falhas
- Cada combatente pode fazer Movimento + Ataque ou Movimento + Movimento

### 4. Resolução do Ataque
- Rola 1d20 + Base de Ataque (BAC/BAD)
- Compara com Classe de Armadura (CA) do alvo
- **Natural 20**: Acerto Crítico (dano dobrado)
- **Natural 1**: Erro Crítico (sempre erra)

### 5. Cálculo do Dano
- Rola dado da arma (ex: 1d8)
- Adiciona modificador de Força (corpo a corpo/arremesso)
- Dano mínimo sempre 1 PV
- Crítico: dobra o dano do dado, depois adiciona modificador

### 6. Fim da Rodada
```kotlin
combate = combateService.finalizarRodada(combate)
```
- Verifica combatentes morrendo (PV ≤ 0)
- Realiza Testes de Agonizar
- Verifica Teste de Moral
- Determina fim do combate

## Uso Básico

### Combate Automático Simples
```kotlin
val service = CombateService()

// Cria combatentes
val aliados = listOf(
    CombatenteFactory.criarGuerreiroBasico("Herói")
)
val inimigos = listOf(
    CombatenteFactory.criarGoblin("Goblin 1"),
    CombatenteFactory.criarGoblin("Goblin 2")
)

// Executa combate completo
val combate = service.executarCombateAutomatico(
    aliados = aliados,
    inimigos = inimigos,
    maxRodadas = 10
)

// Resultado
println("Vencedor: ${combate.vencedor}")
println("Rodadas: ${combate.rodadaAtual}")
```

### Combate em Segundo Plano
```kotlin
val backgroundService = CombateBackgroundService()

// Configura velocidade
backgroundService.setVelocidade(VelocidadeCombate.NORMAL)

// Inicia combate com execução automática
backgroundService.iniciarCombate(aliados, inimigos, autoExecutar = true)

// Observa mudanças
backgroundService.combateAtual.collect { combate ->
    // Atualiza UI com novo estado
}

// Controles
backgroundService.pausar()
backgroundService.retomar()
backgroundService.executarProximaRodada()
```

### Usando o ViewModel
```kotlin
class CombateViewModel : ViewModel() {
    
    // Inicia combate de teste
    fun iniciarTeste() {
        iniciarCombateTeste(
            quantidadeAliados = 2,
            quantidadeInimigos = 3,
            nivelAliados = 1,
            desafioInimigos = 1
        )
    }
    
    // Observa estado
    combateAtual.collect { combate ->
        // Atualiza UI
    }
    
    // Controles
    fun pausar()
    fun retomar()
    fun executarProximaRodada()
    fun setVelocidade(velocidade)
}
```

## Velocidades de Execução

```kotlin
enum class VelocidadeCombate {
    MUITO_LENTA,  // 2s por rodada, 1.5s por iniciativa, 1s por ação
    LENTA,        // 1.5s, 1s, 0.75s
    NORMAL,       // 1s, 0.75s, 0.5s (padrão)
    RAPIDA,       // 0.5s, 0.3s, 0.2s
    MUITO_RAPIDA, // 0.2s, 0.1s, 0.05s
    INSTANTANEA   // Sem delays
}
```

## Geração de Combatentes

### Aleatórios
```kotlin
// Gera um grupo de aliados
val aliados = GeradorCombatente.gerarGrupoAliados(
    quantidade = 3,
    nivel = 2
)

// Gera inimigos
val inimigos = GeradorCombatente.gerarGrupoInimigos(
    quantidade = 4,
    desafio = 1
)
```

### Customizados
```kotlin
// Cria um guerreiro específico
val guerreiro = CombatenteFactory.criarGuerreiroBasico(
    nome = "Thorin",
    pv = 15,
    forca = 18,
    destreza = 14
)

// Ou cria a partir de um Personagem existente
val combatente = GeradorCombatente.criarCombatente(
    personagem = meuPersonagem,
    nivel = 3
)
```

## Estatísticas

### Estatísticas Gerais
```kotlin
val stats = viewModel.estatisticas.value

println("Rodadas: ${stats.rodadas}")
println("Precisão: ${stats.precisaoGeral}%")
println("Taxa de Crítico: ${stats.taxaCritico}%")
println("Dano médio por ataque: ${stats.danoMedioPorAtaque}")
```

### Estatísticas por Combatente
```kotlin
val statsPorCombatente = viewModel.obterEstatisticasPorCombatente()

statsPorCombatente.forEach { (nome, stats) ->
    println("$nome:")
    println("  PV: ${stats.pvAtual}/${stats.pvMaximo}")
    println("  Ataques: ${stats.totalAtaques}")
    println("  Precisão: ${stats.precisao}%")
    println("  Dano causado: ${stats.danoCausado}")
    println("  Dano recebido: ${stats.danoRecebido}")
}
```

## Histórico de Eventos

```kotlin
// Obtém histórico completo
val historico = combate.historico

// Filtra tipos específicos
val ataques = historico.filterIsInstance<EventoCombate.Ataque>()
val mortes = historico.filterIsInstance<EventoCombate.Morte>()
val criticos = ataques.filter { it.critico }

// Eventos recentes (últimos 10)
val recentes = historico.takeLast(10)
```

## Exemplos Completos

Veja o arquivo `ExemplosCombate.kt` para exemplos completos de uso:

1. **Combate Automático Simples**: Execução instantânea de um combate
2. **Combate do Documento**: Recria o exemplo do manual (Guerreiro A vs B)
3. **Combate Passo a Passo**: Executa cada fase manualmente
4. **Serviço em Segundo Plano**: Combate com velocidade configurável
5. **Batalha Épica**: Combate com múltiplos participantes

## Regras Especiais

### Teste de Agonizar
Quando um combatente atinge 0 ou menos PV:
- Estado muda para MORRENDO
- A cada fim de rodada, rola 1d20
- Se resultado > maior JP (JPC ou JPS): morre
- Senão: continua agonizando

### Teste de Moral
Quando metade ou mais de um grupo é derrotado:
- Rola teste de moral (1d20 ≤ 10)
- Falha: sobreviventes fogem ou se rendem

### Críticos
- **Acerto Crítico (20 natural)**:
  - Sempre acerta, independente da CA
  - Dano = (dado da arma × 2) + modificador
  - Exemplo: 1d8 rola 5 → (5×2)+2 = 12 dano

- **Erro Crítico (1 natural)**:
  - Sempre erra, independente da BA
  - Pode gerar consequências negativas

## Integridade do Sistema

O sistema foi desenvolvido seguindo fielmente as regras do Old Dragon 2:
- ✅ Rodadas de 10 segundos
- ✅ Sistema de iniciativa com d20
- ✅ Ordem de ação correta
- ✅ Cálculo de dano exato
- ✅ Críticos e erros críticos
- ✅ Teste de agonizar
- ✅ Teste de moral
- ✅ Verificação de surpresa

## Performance

- Combates são executados em coroutines (não bloqueiam UI)
- Suporte para combates com dezenas de participantes
- Histórico completo mantido em memória
- Velocidades configuráveis (instantânea a muito lenta)

## Extensibilidade

O sistema é facilmente extensível para:
- Novas ações de combate
- Magias e habilidades especiais
- Efeitos de status
- Diferentes tipos de inimigos
- Regras customizadas
