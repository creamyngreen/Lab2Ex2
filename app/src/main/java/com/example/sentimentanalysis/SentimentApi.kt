package com.example.sentimentanalysis

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class RequestBody(val text: String)
data class SentimentResponse(val label: String, val confidence: Float)

interface SentimentApi {
    @Headers("Content-Type: application/json")
    @POST("predict")
    fun analyzeSentiment(@Body body: RequestBody): Call<SentimentResponse>
}
