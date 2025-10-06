package com.example.comp2008assignment2

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

enum class SearchType {
    GENERAL, TITLE, AUTHOR
}

class BookViewModel : ViewModel() {
    var books by mutableStateOf<List<Book>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _bookDetail = MutableStateFlow<Book?>(null)
    val bookDetail: StateFlow<Book?> = _bookDetail

    private val api: OpenLibraryApi = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .registerTypeAdapter(Book::class.java, BookDeserializer())
                    .create()
            )
        )
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
                    books = response.body()?.docs?.filter { it.title != null } ?: emptyList()
                    if (books.isEmpty()) {
                        errorMessage = "No books found for this query."
                    } else {
                        Log.d("BookViewModel", "Search response: ${response.body()?.docs}")
                    }
                } else {
                    errorMessage = when (response.code()) {
                        404 -> "No books found for this query."
                        500 -> "Server error, please try again later."
                        else -> "Failed to fetch books: ${response.message()} (Code: ${response.code()})"
                    }
                    Log.e("BookViewModel", "Search error: $errorMessage")
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                Log.e("BookViewModel", "Search exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchBookDetails(bookKey: String) {
        if (bookKey.isBlank()) {
            errorMessage = "Invalid book key."
            Log.e("BookViewModel", "Invalid book key: $bookKey")
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Clean the key by removing /works/ prefix
                val cleanKey = bookKey.replace("/works/", "")
                Log.d("BookViewModel", "Fetching details for key: $cleanKey (original: $bookKey)")
                val response = api.getBookDetails(cleanKey)
                Log.d("BookViewModel", "API response code: ${response.code()}")
                if (response.isSuccessful) {
                    val book = response.body()
                    Log.d("BookViewModel", "Book details: $book")
                    _bookDetail.value = book
                    if (book == null) {
                        errorMessage = "No details found for this book."
                        Log.e("BookViewModel", "Book details null for key: $cleanKey")
                    }
                } else {
                    errorMessage = when (response.code()) {
                        404 -> "Book details not available for this title."
                        500 -> "Server error, please try again later."
                        else -> "Failed to fetch book details: ${response.message()} (Code: ${response.code()})"
                    }
                    Log.e("BookViewModel", "API error: $errorMessage")
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                Log.e("BookViewModel", "Exception fetching book details: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
}

class BookDeserializer : JsonDeserializer<Book> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Book {
        if (json == null || !json.isJsonObject) {
            Log.e("BookDeserializer", "Invalid JSON: $json")
            return Book(null, null, null, null, null, null)
        }

        val jsonObject = json.asJsonObject
        val description = when {
            jsonObject.has("description") && jsonObject.get("description").isJsonObject ->
                jsonObject.getAsJsonObject("description").get("value")?.asString
            jsonObject.has("description") && jsonObject.get("description").isJsonPrimitive ->
                jsonObject.get("description").asString
            else -> null
        }

        val authorNames = when {
            jsonObject.has("authors") && jsonObject.get("authors").isJsonArray -> {
                val authorsArray = jsonObject.getAsJsonArray("authors")
                authorsArray.mapNotNull { authorElement ->
                    authorElement.asJsonObject?.get("author")?.asJsonObject?.get("name")?.asString
                        ?: authorElement.asJsonObject?.get("author")?.asJsonObject?.get("key")?.asString
                }
            }
            jsonObject.has("author_name") && jsonObject.get("author_name").isJsonArray -> {
                jsonObject.getAsJsonArray("author_name").mapNotNull { it.asString }
            }
            else -> null
        }

        val coverId = when {
            jsonObject.has("covers") && jsonObject.get("covers").isJsonArray ->
                jsonObject.getAsJsonArray("covers").firstOrNull()?.asInt
            jsonObject.has("cover_i") && jsonObject.get("cover_i").isJsonPrimitive ->
                jsonObject.get("cover_i").asInt
            else -> null
        }

        return Book(
            key = jsonObject.get("key")?.asString,
            title = jsonObject.get("title")?.asString,
            author_name = authorNames,
            first_publish_year = jsonObject.get("first_publish_year")?.asInt,
            cover_i = coverId,
            description = description
        )
    }
}