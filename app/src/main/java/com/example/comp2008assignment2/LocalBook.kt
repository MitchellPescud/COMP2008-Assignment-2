package com.example.comp2008assignment2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class LocalBook(
    @PrimaryKey
    val key: String,
    val title: String?,
    val author_name: String?,
    val first_publish_year: Int?,
    val cover_i: Int?,
    val description: String?,
    var cover_image_path: String?
)
