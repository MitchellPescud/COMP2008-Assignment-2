package com.example.comp2008assignment2

fun Book.toDisplayBook(): DisplayBook {
    return DisplayBook(
        key = key,
        title = title,
        authors = author_name?.joinToString(", "),
        publishYear = first_publish_year,
        coverId = cover_i,
        description = description
    )
}

fun LocalBook.toDisplayBook(): DisplayBook {
    return DisplayBook(
        key = key,
        title = title,
        authors = author_name,
        publishYear = first_publish_year,
        coverId = cover_i,
        description = description,
        isLocal = true,
        coverPath = cover_image_path
    )
}