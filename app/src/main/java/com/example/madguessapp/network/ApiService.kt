package com.example.madguessapp.network
import com.example.madguessapp.model.Entry
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {
    private const val NAME_BASE_URL = "https://random-word-api.herokuapp.com/"
    private const val LEADERBOARD_BASE_URL = "http://dreamlo.com/lb/" // Use this for both submission and fetching

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val nameRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NAME_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val leaderboardRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(LEADERBOARD_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val nameApiService: NameApiService by lazy {
        nameRetrofit.create(NameApiService::class.java)
    }

    private val leaderboardApiService: LeaderboardApiService by lazy {
        leaderboardRetrofit.create(LeaderboardApiService::class.java)
    }

    // Function to get a random name from the API
    suspend fun getRandomName(): String? {
        return try {
            val names: List<String> = nameApiService.getRandomName()
            names.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Function to submit the score to the leaderboard
    suspend fun submitScore(userName: String, score: Int, time: String): Boolean {
        return try {
            val response = leaderboardApiService.submitScore(userName, score, time)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Function to fetch the leaderboard details
    suspend fun fetchLeaderboard(): List<Entry>? {
        return try {
            val response = leaderboardApiService.fetchLeaderboard()
            if (response.isSuccessful) {
                response.body()?.dreamlo?.leaderboard?.entry
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}