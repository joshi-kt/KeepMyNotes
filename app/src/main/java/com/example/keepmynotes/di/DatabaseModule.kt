package com.example.keepmynotes.di

import android.content.Context
import androidx.room.Room
import com.example.keepmynotes.data.local.dao.TodoDAO
import com.example.keepmynotes.data.local.database.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext appContext: Context): TodoDatabase {
        return Room.databaseBuilder(
            appContext,
            TodoDatabase::class.java,
            "todo_database"
        ).build()
    }

    @Provides
    fun provideTodoDao(todoDatabase: TodoDatabase): TodoDAO {
        return todoDatabase.getTodoDAO()
    }
}