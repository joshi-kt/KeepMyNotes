package com.example.keepmynotes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey val id : String,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Serializable
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val deviceHash : String = ""
)