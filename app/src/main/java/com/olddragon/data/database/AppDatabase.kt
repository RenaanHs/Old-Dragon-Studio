package com.olddragon.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.olddragon.data.dao.PersonagemDao
import com.olddragon.data.entity.PersonagemEntity

/**
 * Banco de dados Room da aplicação
 */
@Database(
    entities = [PersonagemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun personagemDao(): PersonagemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtém instância única do banco de dados (Singleton)
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "olddragon_database"
                )
                    .fallbackToDestructiveMigration() // Em produção, use migrations adequadas
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Limpa a instância do banco (útil para testes)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}