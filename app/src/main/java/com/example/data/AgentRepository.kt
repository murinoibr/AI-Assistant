package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AgentRepository(
    private val agentDao: AgentDao,
    private val messageDao: MessageDao
) {
    val allAgents: Flow<List<AgentEntity>> = agentDao.getAllAgents()

    fun getMessages(agentId: Long): Flow<List<MessageEntity>> = messageDao.getMessagesForAgent(agentId)

    suspend fun insertAgent(agent: AgentEntity): Long {
        return agentDao.insertAgent(agent)
    }

    suspend fun deleteAgent(id: Long) {
        agentDao.deleteAgent(id)
    }

    suspend fun saveMessage(agentId: Long, sender: String, text: String) {
        messageDao.insertMessage(MessageEntity(agentId = agentId, sender = sender, text = text))
    }

    suspend fun seedDefaultAgents() {
        // We can check if agents exist or insert defaults if empty
    }
}
