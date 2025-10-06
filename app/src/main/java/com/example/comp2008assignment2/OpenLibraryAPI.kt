package com.example.comp2008assignment2

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String
    ): Response<BookSearchResponse>

    @GET("search.json")
    suspend fun searchByTitle(
        @Query("title") title: String
    ): Response<BookSearchResponse>

    @GET("search.json")
    suspend fun searchByAuthor(
        @Query("author") author: String
    ): Response<BookSearchResponse>

    @GET("works/{workKey}.json")
    suspend fun getBookDetails(
        @Path("workKey") workKey: String
    ): Response<Book>
}