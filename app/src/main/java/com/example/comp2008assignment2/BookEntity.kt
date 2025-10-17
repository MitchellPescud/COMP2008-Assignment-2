package com.example.comp2008assignment2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookentity")
data class BookEntity(
    @PrimaryKey val key: String,
    val title: String?,
    val author_name: String?,
    val first_publish_year: Int?,
    val cover_i: Int?,
    val description: String?,
    val localCoverPath: String?
)