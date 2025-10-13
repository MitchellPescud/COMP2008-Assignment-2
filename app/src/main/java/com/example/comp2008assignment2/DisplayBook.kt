package com.example.comp2008assignment2

data class DisplayBook(
    val key: String,
    val title: String?,
    val authors: String?,
    val publishYear: Int?,
    val coverId: Int?,
    val description: String?,
    val isLocal: Boolean = false,
    val coverPath: String? = null
)