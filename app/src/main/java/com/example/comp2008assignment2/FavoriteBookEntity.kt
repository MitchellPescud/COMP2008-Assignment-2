package com.example.comp2008assignment2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_books")
data class FavoriteBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val author: String,
    val year: Int?,
    val coverUrl: String?,
    val personalPhotoUri: String?,
    val createdAt: Long = System.currentTimeMillis()
)