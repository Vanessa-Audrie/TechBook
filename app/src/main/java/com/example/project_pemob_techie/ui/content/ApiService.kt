package com.example.project_pemob_techie.ui.content
import retrofit2.http.GET

interface ApiService {
    @GET("get_data.php")
    suspend fun getBooks(): List<BookResponse>
}


