package com.example.comp2008assignment2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM LocalBooks WHERE title LIKE :query OR author_name LIKE :query")
    suspend fun searchBooks(query: String): List<BookEntity>

    @Query("SELECT * FROM LocalBooks WHERE title LIKE :query")
    suspend fun searchBooksByTitle(query: String): List<BookEntity>

    @Query("SELECT * FROM LocalBooks WHERE author_name LIKE :query")
    suspend fun searchBooksByAuthor(query: String): List<BookEntity>

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM LocalBooks WHERE key = :key)")
    suspend fun isBookInLibrary(key: String): Boolean

    @Query("SELECT * FROM LocalBooks WHERE key = :key")
    suspend fun getBookByKey(key: String): BookEntity?

    @Query("SELECT * FROM LocalBooks")
    fun observeAllBooks(): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM LocalBooks
        WHERE title       LIKE '%' || :q || '%' COLLATE NOCASE
           OR author_name LIKE '%' || :q || '%' COLLATE NOCASE
    """)
    fun observeSearch(q: String): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM LocalBooks
        WHERE title       LIKE '%' || :q || '%' COLLATE NOCASE
           OR author_name LIKE '%' || :q || '%' COLLATE NOCASE
    """)
    suspend fun searchBooksFuzzy(q: String): List<BookEntity>

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("DELETE FROM LocalBooks WHERE key = :key")
    suspend fun deleteByKey(key: String)
}