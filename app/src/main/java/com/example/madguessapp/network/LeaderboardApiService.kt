package com.example.madguessapp.network

import com.example.madguessapp.model.LeaderboardResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LeaderboardApiService {

    @GET("3nPdw8GvDk2w3s_Mhn0WWwvC0i2elQFkWXYzhQ9ATc7Q/add/{userName}/{score}/{time}")
    suspend fun submitScore(
        @Path("userName") userName: String,
        @Path("score") score: Int,
        @Path("time") time: String
    ): Response<Void>

    @GET("671384b18f40bc122c27934d/json-seconds-asc")
    suspend fun fetchLeaderboard(): Response<LeaderboardResponse>


}