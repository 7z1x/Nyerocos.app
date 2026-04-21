package com.app.nyerocos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.nyerocos.data.local.dao.ConversationDao
import com.app.nyerocos.data.local.dao.MessageDao
import com.app.nyerocos.data.local.entity.ConversationEntity
import com.app.nyerocos.data.local.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NyerocosDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: NyerocosDatabase? = null

        fun getDatabase(context: Context): NyerocosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NyerocosDatabase::class.java,
                    "nyerocos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
