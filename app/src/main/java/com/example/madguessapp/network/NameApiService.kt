package com.example.madguessapp.network

import retrofit2.http.GET

interface NameApiService {
    @GET("word") // Adjust the endpoint according to your API
    suspend fun getRandomName(): List<String> // Expecting a list of strings as the response
}