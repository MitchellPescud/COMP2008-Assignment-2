package com.example.comp2008assignment2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM bookentity WHERE title LIKE :query OR author_name LIKE :query")
    suspend fun searchBooks(query: String): List<BookEntity>

    @Query("SELECT * FROM bookentity WHERE title LIKE :query")
    suspend fun searchBooksByTitle(query: String): List<BookEntity>

    @Query("SELECT * FROM bookentity WHERE author_name LIKE :query")
    suspend fun searchBooksByAuthor(query: String): List<BookEntity>

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM bookentity WHERE key = :key)")
    suspend fun isBookInLibrary(key: String): Boolean
}