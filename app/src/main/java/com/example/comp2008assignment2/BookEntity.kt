package com.example.comp2008assignment2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LocalBooks")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // new local ID
    val key: String?,                                   // nullable for API/manual
    val title: String?,
    val author_name: String?,
    val first_publish_year: Int?,
    val cover_i: Int?,
    val description: String?,
    val localCoverPath: String?,
    val isFavourite: Boolean = false
)