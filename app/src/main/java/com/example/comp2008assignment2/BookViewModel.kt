package com.example.comp2008assignment2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

enum class SearchType {
    GENERAL, TITLE, AUTHOR
}

class BookViewModel : ViewModel() {
    var books by mutableStateOf<List<DisplayBook>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedBook by mutableStateOf<DisplayBook?>(null)
        private set
    var descLoading by mutableStateOf(false)
        private set
    var descError by mutableStateOf<String?>(null)
        private set

    private val api: OpenLibraryApi = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenLibraryApi::class.java)

    fun searchBooks(query: String, searchType: SearchType) {
        if (query.isBlank()) {
            errorMessage = "Please enter a search term."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = when (searchType) {
                    SearchType.GENERAL -> api.searchBooks(query)
                    SearchType.TITLE -> api.searchByTitle(query)
                    SearchType.AUTHOR -> api.searchByAuthor(query)
                }
                if (response.isSuccessful) {
                    books = response.body()?.docs?.filter { it.title != null }?.map { it.toDisplayBook() } ?: emptyList()
                    if (books.isEmpty()) {
                        errorMessage = "No books found for this query."
                    }
                } else {
                    errorMessage = when (response.code()) {
                        404 -> "No books found for this query."
                        500 -> "Server error, please try again later."
                        else -> "Failed to fetch books: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun selectBook(book: DisplayBook) {
        selectedBook = book
        if (book.description == null && !book.isLocal) {
            fetchDescription(book)
        }
    }

    private fun fetchDescription(book: DisplayBook) {
        viewModelScope.launch {
            descLoading = true
            descError = null
            try {
                val response = api.getWorkDetails("${book.key}.json")
                if (response.isSuccessful) {
                    val json: JsonObject? = response.body()
                    val descElement = json?.get("description")
                    val description = when {
                        descElement == null -> "No description available."
                        descElement.isJsonPrimitive -> descElement.asString
                        descElement.isJsonObject -> descElement.asJsonObject.get("value")?.asString ?: "No description available."
                        else -> "No description available."
                    }
                    updateBookDescription(book, description)
                } else {
                    descError = when (response.code()) {
                        404 -> "Description not found."
                        500 -> "Server error, please try again later."
                        else -> "Failed to fetch description: ${response.message()}"
                    }
                    updateBookDescription(book, "No description available.")
                }
            } catch (e: Exception) {
                descError = "Error: ${e.message}"
                updateBookDescription(book, "No description available.")
            } finally {
                descLoading = false
            }
        }
    }

    private fun updateBookDescription(book: DisplayBook, description: String) {
        books = books.map { if (it.key == book.key) it.copy(description = description) else it }
        if (selectedBook?.key == book.key) {
            selectedBook = selectedBook?.copy(description = description)
        }
    }
}