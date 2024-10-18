package com.example.madguessapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {
    private const val BASE_URL = "https://random-word-api.herokuapp.com/" // Replace with your actual API base URL

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val nameApiService: NameApiService by lazy {
        retrofit.create(NameApiService::class.java)
    }

    // Function to get a random name from the API
    suspend fun getRandomName(): String? {
        return try {
            val names: List<String> = nameApiService.getRandomName()
            names.firstOrNull() // Return the first name from the list
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null in case of exception
        }
    }
}