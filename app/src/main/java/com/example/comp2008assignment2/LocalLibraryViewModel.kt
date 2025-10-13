package com.example.comp2008assignment2

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalLibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BookRepository
    private val allLocalBooks: StateFlow<List<DisplayBook>>
    var filteredBooks: StateFlow<List<DisplayBook>>
    var selectedBook by mutableStateOf<DisplayBook?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var searchType by mutableStateOf(SearchType.GENERAL)
        private set

    init {
        val bookDao = AppDatabase.getDatabase(application).bookDao()
        repository = BookRepository(bookDao, application)
        allLocalBooks = repository.localBooks.map { list -> list.map { it.toDisplayBook() } }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        filteredBooks = allLocalBooks
    }

    fun updateSearchQuery(query: String, type: SearchType) {
        searchQuery = query
        searchType = type
        val lowerQuery = query.lowercase()
        filteredBooks = if (query.isBlank()) {
            allLocalBooks
        } else {
            allLocalBooks.map { books ->
                books.filter { book ->
                    when (searchType) {
                        SearchType.GENERAL -> {
                            book.title?.lowercase()?.contains(lowerQuery) == true ||
                                    book.authors?.lowercase()?.contains(lowerQuery) == true
                        }
                        SearchType.TITLE -> book.title?.lowercase()?.contains(lowerQuery) == true
                        SearchType.AUTHOR -> book.authors?.lowercase()?.contains(lowerQuery) == true
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    fun resetSearch() {
        searchQuery = ""
        filteredBooks = allLocalBooks
    }

    fun selectBook(book: DisplayBook) {
        selectedBook = book
    }

    fun deleteBook(key: String) = viewModelScope.launch {
        repository.deleteBook(key)
    }

    fun saveBook(book: DisplayBook) = viewModelScope.launch {
        val originalBook = Book(
            key = book.key,
            title = book.title,
            author_name = book.authors?.split(", ") ?: emptyList(),
            first_publish_year = book.publishYear,
            cover_i = book.coverId,
            description = book.description
        )
        repository.insertBook(originalBook)
    }

    fun addBook(book: LocalBook) = viewModelScope.launch {
        repository.insertLocalBook(book)
    }
}