package com.example.keepmynotes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.keepmynotes.data.local.dao.TodoDAO
import com.example.keepmynotes.model.TodoItem

@Database(entities = [TodoItem::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {

    companion object {
        const val NAME = "todo_database"
    }

    abstract fun getTodoDAO() : TodoDAO

}