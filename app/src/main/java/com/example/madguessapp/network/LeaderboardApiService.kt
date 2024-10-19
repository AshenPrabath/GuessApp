package com.example.madguessapp.network

import com.example.madguessapp.model.LeaderboardResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LeaderboardApiService {

    @GET("add/{userName}/{score}/{time}")
    suspend fun submitScore(
        @Path("userName") userName: String,
        @Path("score") score: Int,
        @Path("time") time: String
    ): Response<Void>

    @GET("json-seconds-asc")
    suspend fun fetchLeaderboard(): Response<LeaderboardResponse>


}