package com.example.comp2008assignment2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM bookentity WHERE title LIKE :query OR author_name LIKE :query")
    suspend fun searchBooks(query: String): List<BookEntity>
}