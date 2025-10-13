package com.example.comp2008assignment2

data class BookSearchResponse(
    val docs: List<Book>
)

data class Book(
    val key: String,
    val title: String?,
    val author_name: List<String>?,
    val first_publish_year: Int?,
    val cover_i: Int?, // Cover ID for image
    val description: String? = null // Description for detailed view
)
