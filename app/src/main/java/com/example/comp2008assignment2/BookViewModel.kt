package com.example.comp2008assignment2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

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

    var localBooks by mutableStateOf<List<Book>>(emptyList())
        private set
    var localIsLoading by mutableStateOf(false)
        private set
    var localErrorMessage by mutableStateOf<String?>(null)
        private set

    var selectedOnlineBook by mutableStateOf<Book?>(null)
        private set
    var onlineDescLoading by mutableStateOf(false)
        private set
    var onlineDescError by mutableStateOf<String?>(null)
        private set

    var selectedLocalBook by mutableStateOf<Book?>(null)
        private set
    var localDescLoading by mutableStateOf(false)
        private set
    var localDescError by mutableStateOf<String?>(null)
        private set

    private val api: OpenLibraryApi = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenLibraryApi::class.java)

    private val dao = MainActivity.bookDao

    fun searchBooks(query: String, searchType: SearchType) {
        if (query.isBlank()) {
            errorMessage = "Please enter a search term."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val trimmedQuery = query.trim()
            try {
                searchBooksWithRetry(trimmedQuery, searchType)
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun searchBooksWithRetry(query: String, searchType: SearchType, retries: Int = 3) {
        repeat(retries) { attempt ->
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
                    }
                    return // Success, exit retry loop
                } else {
                    if (response.code() == 500 && attempt < retries - 1) {
                        delay(1000L * (attempt + 1)) // Exponential backoff
                    } else {
                        errorMessage = when (response.code()) {
                            404 -> "No books found for this query."
                            500 -> "Server error - Code: ${response.code()}. Please try again."
                            else -> "Failed to fetch books: ${response.message()} (Code: ${response.code()})"
                        }
                        return
                    }
                }
            } catch (e: Exception) {
                if (attempt == retries - 1) {
                    errorMessage = "Error: ${e.message}"
                    return
                }
                delay(1000L * (attempt + 1))
            }
        }
    }

    fun loadLocalBooks(query: String = "", searchType: SearchType = SearchType.GENERAL) {
        viewModelScope.launch {
            localIsLoading = true
            localErrorMessage = null
            try {
                val q = "%${query.trim()}%"
                val entities = withContext(Dispatchers.IO) {
                    when (searchType) {
                        SearchType.GENERAL -> dao.searchBooks(q)
                        SearchType.TITLE -> dao.searchBooksByTitle(q)
                        SearchType.AUTHOR -> dao.searchBooksByAuthor(q)
                    }
                }
                localBooks = entities.map { entity ->
                    Book(
                        key = entity.key,
                        title = entity.title,
                        author_name = entity.author_name?.split(", ") ?: emptyList(),
                        first_publish_year = entity.first_publish_year,
                        cover_i = entity.cover_i,
                        description = entity.description,
                        localCoverPath = entity.localCoverPath
                    )
                }
                if (localBooks.isEmpty() && query.isNotBlank()) {
                    localErrorMessage = "No local books found for this query."
                }
            } catch (e: Exception) {
                localErrorMessage = "Error loading local books: ${e.message}"
            } finally {
                localIsLoading = false
            }
        }
    }

    fun selectOnlineBook(book: Book) {
        selectedOnlineBook = book
        if (book.description == null) {
            fetchDescription(book, isOnline = true)
        }
    }

    fun selectLocalBook(book: Book) {
        selectedLocalBook = book
        if (book.description == null) {
            fetchDescription(book, isOnline = false)
        }
    }

    fun addToLocalLibrary(book: Book) {
        viewModelScope.launch {
            var updatedBook = book
            if (book.description == null) {
                updatedBook = fetchBookDescription(book)
            }

            var localPath: String? = null
            book.cover_i?.let { coverId ->
                try {
                    val url = URL("https://covers.openlibrary.org/b/id/$coverId-M.jpg")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    val input = connection.inputStream
                    val file = File(MainActivity.app.filesDir, "cover_${coverId}.jpg")
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                    localPath = file.absolutePath
                } catch (e: Exception) {
                    // Proceed without cover if download fails
                }
            }

            val entity = BookEntity(
                key = updatedBook.key ?: return@launch,
                title = updatedBook.title,
                author_name = updatedBook.author_name?.joinToString(", "),
                first_publish_year = updatedBook.first_publish_year,
                cover_i = updatedBook.cover_i,
                description = updatedBook.description,
                localCoverPath = localPath
            )

            withContext(Dispatchers.IO) {
                dao.insertBook(entity)
            }
            loadLocalBooks() // Refresh local books after adding
        }
    }

    fun removeFromLocalLibrary(book: Book, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                book.key?.let { key ->
                    dao.getBookByKey(key)?.let { entity ->
                        entity.localCoverPath?.let { path ->
                            try {
                                File(path).delete()
                            } catch (e: Exception) {
                                // Log or handle error if deletion fails
                            }
                        }
                        dao.deleteBook(entity)
                    }
                }
            }
            loadLocalBooks() // Refresh local books after removal
            onSuccess()
        }
    }


    fun addManualBook(
        title: String,
        author: String,
        year: Int?,
        localCoverPath: String? = null,
        description: String = "No Description Available",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val localKey = "manual_${System.currentTimeMillis()}"
            val desc = description.trim().ifBlank { null }
            val entity = BookEntity(
                key = localKey,
                title = title.trim(),
                author_name = author.trim(),
                first_publish_year = year,
                cover_i = null,
                description = desc,
                localCoverPath = localCoverPath
            )
            withContext(Dispatchers.IO) {
                dao.insertBook(entity)
            }
            loadLocalBooks()
            onSuccess()
        }
    }

    suspend fun isBookInLibrary(book: Book): Boolean {
        return book.key?.let { key ->
            withContext(Dispatchers.IO) {
                dao.isBookInLibrary(key)
            }
        } ?: false
    }

    private suspend fun fetchBookDescription(book: Book): Book {
        if (book.key == null) {
            return book.copy(description = "No description available.")
        }

        return try {
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
                book.copy(description = description)
            } else {
                book.copy(description = "No description available.")
            }
        } catch (e: Exception) {
            book.copy(description = "No description available.")
        }
    }

    private fun fetchDescription(book: Book, isOnline: Boolean) {
        if (book.key == null) {
            updateBookDescription(book, "No description available.", isOnline)
            return
        }

        viewModelScope.launch {
            if (isOnline) {
                onlineDescLoading = true
                onlineDescError = null
            } else {
                localDescLoading = true
                localDescError = null
            }
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
                    updateBookDescription(book, description, isOnline)
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Description not found."
                        500 -> "Server error: ${response.message()}. Please try again."
                        else -> "Failed to fetch description: ${response.message()} (Code: ${response.code()})"
                    }
                    if (isOnline) {
                        onlineDescError = errorMessage
                    } else {
                        localDescError = errorMessage
                    }
                    updateBookDescription(book, "No description available.", isOnline)
                }
            } catch (e: Exception) {
                if (isOnline) {
                    onlineDescError = "Error: ${e.message}"
                } else {
                    localDescError = "Error: ${e.message}"
                }
                updateBookDescription(book, "No description available.", isOnline)
            } finally {
                if (isOnline) {
                    onlineDescLoading = false
                } else {
                    localDescLoading = false
                }
            }
        }
    }

    private fun updateBookDescription(book: Book, description: String, isOnline: Boolean) {
        if (isOnline) {
            books = books.map { if (it == book) it.copy(description = description) else it }
            if (selectedOnlineBook == book) {
                selectedOnlineBook = selectedOnlineBook?.copy(description = description)
            }
        } else {
            localBooks = localBooks.map { if (it == book) it.copy(description = description) else it }
            if (selectedLocalBook == book) {
                selectedLocalBook = selectedLocalBook?.copy(description = description)
            }
        }
    }
}