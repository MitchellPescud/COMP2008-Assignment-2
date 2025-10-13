package com.example.comp2008assignment2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class BookRepository(private val bookDao: BookDao, private val context: Context) {

    val localBooks: Flow<List<LocalBook>> = bookDao.getLocalBooks()

    suspend fun getBookByKey(key: String): LocalBook? {
        return bookDao.getBookByKey(key)
    }

    suspend fun insertBook(book: Book) {
        val coverImagePath = book.cover_i?.let { saveCoverImage(it) }
        val localBook = LocalBook(
            key = book.key!!,
            title = book.title,
            author_name = book.author_name?.joinToString(", "),
            first_publish_year = book.first_publish_year,
            cover_i = book.cover_i,
            description = book.description,
            cover_image_path = coverImagePath
        )
        bookDao.insertBook(localBook)
    }

    suspend fun insertLocalBook(book: LocalBook) {
        bookDao.insertBook(book)
    }

    suspend fun deleteBook(key: String) {
        val book = bookDao.getBookByKey(key)
        book?.cover_image_path?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
        bookDao.deleteBook(key)
    }

    private suspend fun saveCoverImage(coverId: Int): String? {
        val imageUrl = "https://covers.openlibrary.org/b/id/$coverId-L.jpg"
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false) // Required for bitmap processing
            .build()

        val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
        val bitmap = (result as? BitmapDrawable)?.bitmap

        return bitmap?.let {
            val file = File(context.filesDir, "$coverId.jpg")
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            file.absolutePath
        }
    }
}
