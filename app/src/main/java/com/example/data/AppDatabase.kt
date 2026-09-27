package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val emoji: String,
    val category: String,
    val isCustom: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agentId: Long,
    val sender: String, // "user" or "agent"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents ORDER BY id DESC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity): Long

    @Query("DELETE FROM agents WHERE id = :id AND isCustom = 1")
    suspend fun deleteAgent(id: Long)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE agentId = :agentId ORDER BY timestamp ASC")
    fun getMessagesForAgent(agentId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE agentId = :agentId")
    suspend fun clearMessages(agentId: Long)
}

@Database(entities = [AgentEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vozia_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
