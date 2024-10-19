package com.example.madguessapp.network

import retrofit2.http.GET

interface NameApiService {
    @GET("word")
    suspend fun getRandomName(): List<String>
}