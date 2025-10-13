package com.example.comp2008assignment2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: LocalBook)

    @Query("SELECT * FROM books")
    fun getLocalBooks(): Flow<List<LocalBook>>

    @Query("SELECT * FROM books WHERE `key` = :key")
    suspend fun getBookByKey(key: String): LocalBook?

    @Query("DELETE FROM books WHERE `key` = :key")
    suspend fun deleteBook(key: String)
}
