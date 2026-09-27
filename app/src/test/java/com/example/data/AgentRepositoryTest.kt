package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeAgentDao : AgentDao {
    val insertedAgents = mutableListOf<AgentEntity>()
    var nextInsertId = 1L

    override fun getAllAgents(): Flow<List<AgentEntity>> {
        return emptyFlow()
    }

    override suspend fun insertAgent(agent: AgentEntity): Long {
        insertedAgents.add(agent)
        return nextInsertId++
    }

    override suspend fun deleteAgent(id: Long) {
        // Not used in this test
    }
}

class FakeMessageDao : MessageDao {
    override fun getMessagesForAgent(agentId: Long): Flow<List<MessageEntity>> {
        return emptyFlow()
    }

    override suspend fun insertMessage(message: MessageEntity) {
        // Not used
    }

    override suspend fun clearMessages(agentId: Long) {
        // Not used
    }
}

class AgentRepositoryTest {

    @Test
    fun `insertAgent delegates to agentDao and returns id`() = runTest {
        // Arrange
        val fakeAgentDao = FakeAgentDao()
        val fakeMessageDao = FakeMessageDao()
        val repository = AgentRepository(fakeAgentDao, fakeMessageDao)

        val agent = AgentEntity(
            name = "Test Agent",
            description = "Test Description",
            systemPrompt = "Test Prompt",
            emoji = "🤖",
            category = "Test"
        )

        // Act
        val resultId = repository.insertAgent(agent)

        // Assert
        assertEquals(1L, resultId)
        assertEquals(1, fakeAgentDao.insertedAgents.size)
        assertEquals(agent, fakeAgentDao.insertedAgents[0])
    }
}
