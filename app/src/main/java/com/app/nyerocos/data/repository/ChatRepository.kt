package com.app.nyerocos.data.repository

import android.content.Context
import com.app.nyerocos.data.local.NyerocosDatabase
import com.app.nyerocos.data.local.entity.ConversationEntity
import com.app.nyerocos.data.local.entity.MessageEntity
import com.app.nyerocos.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ChatRepository(context: Context) {
    private val db = NyerocosDatabase.getDatabase(context)
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()


    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    suspend fun createConversation(mode: String): String {
        val id = UUID.randomUUID().toString()
        val conversation = ConversationEntity(
            id = id,
            mode = mode,
            title = "$mode Session"
        )
        conversationDao.insertConversation(conversation)
        return id
    }

    suspend fun updateConversationTitle(id: String, firstMessage: String) {
        val title = if (firstMessage.length > 30) {
            firstMessage.take(30) + "..."
        } else {
            firstMessage
        }
        conversationDao.updateTitle(id, title)
    }

    suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversation(id)
    }

    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForConversation(conversationId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    content = entity.content,
                    isFromUser = entity.isFromUser,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun saveMessage(
        conversationId: String,
        content: String,
        isFromUser: Boolean
    ): MessageEntity {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            content = content,
            isFromUser = isFromUser
        )
        messageDao.insertMessage(message)

        conversationDao.updateLastMessageTime(
            conversationId,
            System.currentTimeMillis()
        )

        val count = messageDao.getMessageCount(conversationId)
        if (count == 1 && isFromUser) {
            updateConversationTitle(conversationId, content)
        }

        return message
    }
}
