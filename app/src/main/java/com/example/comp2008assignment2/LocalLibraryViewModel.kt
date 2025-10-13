package com.example.comp2008assignment2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookRepository

    val localBooks: StateFlow<List<LocalBook>>

    init {
        val bookDao = AppDatabase.getDatabase(application).bookDao()
        repository = BookRepository(bookDao, application)
        localBooks = repository.localBooks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun deleteBook(key: String) = viewModelScope.launch {
        repository.deleteBook(key)
    }

    fun saveBook(book: Book) = viewModelScope.launch {
        repository.insertBook(book)
    }
}
