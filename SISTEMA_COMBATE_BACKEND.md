# Sistema de Combate Old Dragon 2 - Backend

## 📋 Visão Geral

Este documento descreve a implementação completa do sistema de combate para Old Dragon 2 no backend Android, incluindo execução em segundo plano e notificações.

## 🏗️ Arquitetura

### Camadas Implementadas

```
┌─────────────────────────────────────────────────┐
│              UI Layer (Compose)                  │
│  CombateScreen.kt - Interface do usuário        │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│         ViewModel Layer                          │
│  CombateViewModel.kt - Gerencia estado da UI    │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│         Service Layer                            │
│  ├─ CombateBackgroundService.kt (In-Memory)     │
│  ├─ AndroidCombateService.kt (Android Service)  │
│  ├─ CombateService.kt (Regras de negócio)       │
│  ├─ GeradorCombatente.kt                        │
│  └─ CombatenteFactory.kt                        │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│         Model Layer                              │
│  ├─ Combate.kt - Estado do combate              │
│  ├─ Combatente.kt - Entidade combatente         │
│  ├─ EventoCombate.kt - Eventos                  │
│  ├─ Arma.kt, FaseCombate.kt, etc.              │
│  └─ Personagem.kt                                │
└─────────────────────────────────────────────────┘
```

## ⚔️ Regras de Combate Implementadas

### 1. Verificação de Surpresa (✅ Implementado)
- Chance de 1-2 em 1d6 (33%)
- 1-3 em 1d6 se furtivo (50%)
- Lado surpreendido perde primeira rodada

### 2. Determinação de Iniciativa (✅ Implementado)
- Teste: 1d20 vs maior entre Destreza ou Sabedoria
- Ordem: Sucessos → Inimigos → Falhas
- Mantém ordem durante o combate

### 3. Execução das Ações (✅ Implementado)
- Cada combatente: Movimento + Ataque (10 segundos)
- Alternativa: Movimento + Movimento

### 4. Resolução do Ataque (✅ Implementado)
- Rolagem: 1d20 + BA (BAC ou BAD)
- Acerto: Resultado ≥ CA do alvo
- **Crítico (20 natural)**: Dobra dado de dano
- **Erro Crítico (1 natural)**: Falha automática

### 5. Cálculo do Dano (✅ Implementado)
- Dado da arma + modificador de Força
- Dano mínimo: 1 PV
- Críticos dobram o **dado**, não o modificador

### 6. Fim da Rodada (✅ Implementado)
- **Morte**: PV ≤ 0 → Teste de Agonizar (JPC ou JPS)
- **Teste de Moral**: Metade do grupo derrotado
- Verificação de fim de combate

## 📦 Componentes Principais

### 1. `AndroidCombateService` (NEW! ⭐)

Service Android que executa combates em background com notificações.

```kotlin
// Iniciar combate em background
combateViewModel.iniciarCombateBackground(
    context = context,
    personagem = personagemSelecionado,
    quantidadeInimigos = 3
)
```

**Características:**
- ✅ Executa mesmo com app fechado
- ✅ Notificações em tempo real
- ✅ Notificação especial se personagem morrer
- ✅ Foreground Service com prioridade

### 2. `CombateBackgroundService`

Processa combates em memória com coroutines.

```kotlin
val service = CombateBackgroundService()

// Iniciar combate
service.iniciarCombate(aliados, inimigos, autoExecutar = true)

// Observar estado
service.combateAtual.collect { combate ->
    // Atualizar UI
}
```

### 3. `CombateService`

Implementa todas as regras de combate de Old Dragon 2.

```kotlin
val service = CombateService()

// Criar combate
val combate = service.iniciarCombate(aliados, inimigos)

// Verificar surpresa
val combateComSurpresa = service.verificarSurpresa(combate)

// Determinar iniciativa
val combateComIniciativa = service.determinarIniciativa(combate)

// Executar rodada
val combateAtualizado = service.executarRodada(combate)
```

### 4. `GeradorCombatente`

Gera combatentes aleatórios ou converte `Personagem` para `Combatente`.

```kotlin
// Gerar aliados aleatórios
val aliados = GeradorCombatente.gerarGrupoAliados(2, nivel = 3)

// Gerar inimigos
val inimigos = GeradorCombatente.gerarGrupoInimigos(4, desafio = 2)

// Converter personagem
val combatente = GeradorCombatente.criarCombatente(personagem, nivel = 5)
```

### 5. `CombateViewModel`

Gerencia estado de combate para a UI.

```kotlin
val viewModel: CombateViewModel = viewModel()

// Combate com personagem específico
viewModel.iniciarCombateComPersonagem(
    personagem = meuPersonagem,
    quantidadeInimigos = 3,
    desafioInimigos = 2
)

// Combate em background (continua com app fechado)
viewModel.iniciarCombateBackground(
    context = context,
    personagem = meuPersonagem,
    quantidadeInimigos = 4
)

// Controles
viewModel.executarProximaRodada()
viewModel.pausar()
viewModel.retomar()
viewModel.encerrarCombate()
```

## 🎯 Funcionalidades Implementadas

### ✅ Combate Básico
- [x] Sistema de iniciativa
- [x] Ataques corpo a corpo
- [x] Cálculo de dano
- [x] Críticos e erros críticos
- [x] Sistema de morte/agonizar
- [x] Teste de moral

### ✅ Execução em Background
- [x] CombateBackgroundService com coroutines
- [x] AndroidCombateService (Foreground Service)
- [x] Notificações em tempo real
- [x] Execução com app fechado
- [x] Notificação de morte do personagem

### ✅ Interface e Controles
- [x] Tela de combate com visualização
- [x] Controles de velocidade
- [x] Histórico de eventos
- [x] Estatísticas em tempo real
- [x] Seleção de personagem
- [x] Níveis de dificuldade

### ✅ Estatísticas
- [x] Por combatente (PV, ataques, dano, etc.)
- [x] Gerais do combate
- [x] Precisão e taxa de críticos
- [x] Dano médio por ataque

## 🔔 Sistema de Notificações

### Tipos de Notificação

1. **Notificação de Progresso**
   - Aparece durante o combate
   - Mostra rodada atual
   - Aliados vivos vs Inimigos vivos

2. **Notificação de Morte** 💀
   - Prioridade ALTA
   - Alerta quando personagem morre
   - Ícone de alerta vermelho

3. **Notificação de Vitória** 🎉
   - Aparece ao vencer
   - Auto-cancelável

4. **Notificação de Erro** ⚠️
   - Caso ocorra erro no combate
   - Auto-cancelável

## 🚀 Como Usar

### Exemplo 1: Combate Na UI (app aberto)

```kotlin
@Composable
fun MinhaTela() {
    val viewModel: CombateViewModel = viewModel()
    val personagem = lembrarPersonagem()
    
    Button(onClick = {
        viewModel.iniciarCombateComPersonagem(
            personagem = personagem,
            quantidadeInimigos = 3
        )
    }) {
        Text("Iniciar Combate")
    }
}
```

### Exemplo 2: Combate em Background (app pode fechar)

```kotlin
@Composable
fun MinhaTela() {
    val context = LocalContext.current
    val viewModel: CombateViewModel = viewModel()
    val personagem = lembrarPersonagem()
    
    Button(onClick = {
        viewModel.iniciarCombateBackground(
            context = context,
            personagem = personagem,
            quantidadeInimigos = 4
        )
        // Usuário pode fechar o app!
        // Receberá notificação se personagem morrer
    }) {
        Text("Combate em Background")
    }
}
```

### Exemplo 3: Combate Manual (passo a passo)

```kotlin
@Composable
fun CombateManual() {
    val viewModel: CombateViewModel = viewModel()
    val combate by viewModel.estadoUI.collectAsState()
    
    Column {
        when (val estado = combate) {
            is EstadoCombateUI.EmCombate -> {
                Text("Rodada: ${estado.combate.rodadaAtual}")
                
                Button(onClick = {
                    viewModel.executarProximaRodada()
                }) {
                    Text("Próxima Rodada")
                }
            }
        }
    }
}
```

## 🎲 Exemplos de Combate

### Exemplo Completo (do livro)

```kotlin
// Guerreiro A
val guerreiroA = Personagem(
    nome = "Guerreiro A",
    raca = Humano(),
    classe = Guerreiro(),
    atributos = Atributos(forca = 16, destreza = 14, ...)
)

// Guerreiro B
val guerreiroB = Personagem(
    nome = "Guerreiro B",
    raca = Humano(),
    classe = Guerreiro(),
    atributos = Atributos(forca = 14, destreza = 12, ...)
)

// Converter para combatentes
val combatenteA = GeradorCombatente.criarCombatente(guerreiroA, nivel = 1)
val combatenteB = GeradorCombatente.criarCombatente(guerreiroB, nivel = 1)

// Iniciar combate
viewModel.iniciarCombate(
    aliados = listOf(combatenteA),
    inimigos = listOf(combatenteB),
    autoExecutar = true
)

// Resultado esperado similar ao exemplo do livro:
// Rodada 1: G.A erra, G.B acerta (7 de dano)
// Rodada 2: G.A crítico (12 de dano), G.B morre
```

## 📊 Modelo de Dados

### Combatente
```kotlin
data class Combatente(
    val personagem: Personagem,
    var pontosVida: Int,
    val pontosVidaMaximo: Int,
    val classeArmadura: Int,
    val baseAtaqueCorpoACorpo: Int,
    val baseAtaqueDistancia: Int,
    val modificadorForca: Int,
    val modificadorDestreza: Int,
    val modificadorSabedoria: Int,
    val jogadaProtecaoConstitui: Int,
    val jogadaProtecaoSabedoria: Int,
    val arma: Arma,
    var ordemIniciativa: OrdemIniciativa = NAO_ROLADO,
    var estado: EstadoCombatente = ATIVO
)
```

### EventoCombate (sealed class)
```kotlin
sealed class EventoCombate {
    data class InicioCombate(...)
    data class Surpresa(...)
    data class Iniciativa(...)
    data class Ataque(
        val atacante: String,
        val alvo: String,
        val rolagemAtaque: Int,
        val bonusAtaque: Int,
        val ca: Int,
        val acertou: Boolean,
        val critico: Boolean,
        val dano: Int,
        ...
    )
    data class Morte(...)
    data class FimCombate(...)
    // ... outros eventos
}
```

## 🔧 Configuração

### Velocidades de Combate

```kotlin
enum class VelocidadeCombate(
    val delayRodada: Long,
    val delayIniciativa: Long,
    val delayAcao: Long
) {
    MUITO_LENTA(2000L, 1500L, 1000L),
    LENTA(1500L, 1000L, 750L),
    NORMAL(1000L, 750L, 500L),
    RAPIDA(500L, 300L, 200L),
    MUITO_RAPIDA(200L, 100L, 50L),
    INSTANTANEA(0L, 0L, 0L)
}

// Usar:
viewModel.setVelocidade(VelocidadeCombate.RAPIDA)
```

## ⚙️ Permissões Necessárias

O `AndroidManifest.xml` foi configurado com:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC"/>
```

## 🧪 Testes

### Teste Rápido
```kotlin
fun testeRapido() {
    val viewModel = CombateViewModel()
    
    // Combate de teste
    viewModel.iniciarCombateTeste(
        quantidadeAliados = 2,
        quantidadeInimigos = 2,
        nivelAliados = 1,
        desafioInimigos = 1
    )
}
```

## 📝 Próximas Melhorias

### Em Desenvolvimento
- [ ] Ataques à distância
- [ ] Magias e habilidades especiais
- [ ] Sistema de itens/poções
- [ ] Terreno e posicionamento
- [ ] Grupos de NPCs

### Planejado
- [ ] IA inteligente para inimigos
- [ ] Sistema de experiência
- [ ] Salvamento de histórico
- [ ] Replay de combates
- [ ] Multiplayer local

## 🐛 Debugging

### Logs Úteis
O sistema gera eventos detalhados em `EventoCombate`. Use:

```kotlin
combate.historico.forEach { evento ->
    when (evento) {
        is EventoCombate.Ataque -> {
            println("${evento.atacante} atacou ${evento.alvo}")
            println("Rolagem: ${evento.rolagemAtaque} + ${evento.bonusAtaque} vs CA ${evento.ca}")
            if (evento.acertou) {
                println("ACERTOU! Dano: ${evento.dano}")
                if (evento.critico) println("CRÍTICO!")
            }
        }
        // ...
    }
}
```

## 👥 Contribuindo

Para adicionar novas funcionalidades ao sistema de combate:

1. Adicione nova regra em `CombateService.kt`
2. Crie evento correspondente em `EventoCombate.kt`
3. Atualize `CombateBackgroundService` se necessário
4. Adicione testes

## 📚 Referências

- **Old Dragon 2**: Regras oficiais do sistema
- **Documento de Combate**: Especificação detalhada (COMBATE_README.md)
- **Exemplo do Livro**: Combate entre Guerreiro A e Guerreiro B

---

**Status**: ✅ Sistema Completo e Funcional
**Versão**: 1.0.0
**Data**: 2024
