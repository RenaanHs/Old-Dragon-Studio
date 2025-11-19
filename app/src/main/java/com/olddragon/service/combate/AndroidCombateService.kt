package com.olddragon.service.combate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.olddragon.MainActivity
import com.olddragon.model.combate.*
import kotlinx.coroutines.*

/**
 * Service Android que executa combates em segundo plano,
 * mesmo quando o aplicativo está fechado
 */
class AndroidCombateService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "combate_channel"
        private const val CHANNEL_NAME = "Combates Old Dragon"
        
        const val ACTION_START_COMBAT = "com.olddragon.START_COMBAT"
        const val ACTION_STOP_COMBAT = "com.olddragon.STOP_COMBAT"
        
        const val EXTRA_PLAYER_NAME = "player_name"
        const val EXTRA_PLAYER_HP = "player_hp"
        const val EXTRA_PLAYER_AC = "player_ac"
        const val EXTRA_NUM_ENEMIES = "num_enemies"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var combateJob: Job? = null
    private var combateProcessor: CombateBackgroundService? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        combateProcessor = CombateBackgroundService()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_COMBAT -> {
                val notification = createNotification("Combate iniciado", "Preparando para batalha...")
                startForeground(NOTIFICATION_ID, notification)
                
                // Inicia o combate
                iniciarCombateAutomatico(intent)
            }
            ACTION_STOP_COMBAT -> {
                encerrarCombate()
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        combateProcessor?.dispose()
    }
    
    private fun iniciarCombateAutomatico(intent: Intent) {
        combateJob?.cancel()
        combateJob = serviceScope.launch {
            try {
                // Configura combate a partir dos parâmetros
                val numEnemies = intent.getIntExtra(EXTRA_NUM_ENEMIES, 2)
                
                // Gera combatentes
                val aliados = GeradorCombatente.gerarGrupoAliados(1, 1)
                val inimigos = GeradorCombatente.gerarGrupoInimigos(numEnemies, 1)
                
                // Inicia combate
                combateProcessor?.iniciarCombate(aliados, inimigos, autoExecutar = false)
                
                // Observa o combate
                combateProcessor?.combateAtual?.collect { combate ->
                    if (combate != null) {
                        atualizarNotificacao(combate)
                        
                        // Executa próxima rodada
                        if (combate.fase != FaseCombate.FINALIZADO) {
                            delay(1000)
                            combateProcessor?.executarProximaRodada()
                        } else {
                            // Combate finalizado
                            verificarMortePersonagem(combate, aliados.first())
                            stopSelf()
                        }
                    }
                }
            } catch (e: Exception) {
                mostrarNotificacaoErro()
                stopSelf()
            }
        }
    }
    
    private fun verificarMortePersonagem(combate: Combate, personagemPrincipal: Combatente) {
        val personagemMorreu = combate.combatentesAliados
            .find { it.personagem.nome == personagemPrincipal.personagem.nome }
            ?.let { !it.estaVivo() } == true
        
        if (personagemMorreu) {
            mostrarNotificacaoMorte(personagemPrincipal.personagem.nome)
        } else {
            mostrarNotificacaoVitoria()
        }
    }
    
    private fun encerrarCombate() {
        combateJob?.cancel()
        combateProcessor?.cancelarCombate()
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de combate"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun atualizarNotificacao(combate: Combate) {
        val aliadosVivos = combate.combatentesAliados.count { it.estaVivo() }
        val inimigosVivos = combate.combatentesInimigos.count { it.estaVivo() }
        
        val title = "⚔️ Combate - Rodada ${combate.rodadaAtual}"
        val content = "Aliados: $aliadosVivos | Inimigos: $inimigosVivos"
        
        val notification = createNotification(title, content)
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun mostrarNotificacaoMorte(nomePersonagem: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("💀 $nomePersonagem morreu!")
            .setContentText("Seu personagem foi derrotado em combate")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    private fun mostrarNotificacaoVitoria() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 Vitória!")
            .setContentText("Seu personagem venceu o combate!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
    
    private fun mostrarNotificacaoErro() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Erro no combate")
            .setContentText("Ocorreu um erro durante o combate")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 3, notification)
    }
}
