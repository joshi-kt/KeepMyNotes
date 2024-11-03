package com.example.keepmynotes.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.keepmynotes.model.TodoItem
import com.example.keepmynotes.utils.RestrictedAPI

@Dao
interface TodoDAO {

    @Query("SELECT * FROM todo_items WHERE is_deleted == 0 ORDER BY created_at DESC")
    fun getAllTodo() : LiveData<List<TodoItem>>

    @Insert
    fun addTodo(todoItem: TodoItem)

    @Query("UPDATE TODO_ITEMS SET is_deleted = 1 WHERE id == :id")
    fun deleteTodo(id : String)

    @RestrictedAPI
    @Query("DELETE FROM TODO_ITEMS")
    fun deleteAllTodo()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTodo(todoItems: List<TodoItem>)

    @Query("SELECT * FROM todo_items WHERE (title LIKE :searchText OR description LIKE :searchText) AND is_deleted == 0 ORDER BY created_at DESC")
    fun searchTodo(searchText : String) : LiveData<List<TodoItem>>

}